package software.coley.cafedude.transform;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.coley.cafedude.classfile.ClassFile;
import software.coley.cafedude.classfile.ConstantPoolConstants;
import software.coley.cafedude.classfile.Descriptor;
import software.coley.cafedude.classfile.Field;
import software.coley.cafedude.classfile.Method;
import software.coley.cafedude.classfile.Modifiers;
import software.coley.cafedude.classfile.annotation.Annotation;
import software.coley.cafedude.classfile.annotation.AnnotationElementValue;
import software.coley.cafedude.classfile.annotation.ArrayElementValue;
import software.coley.cafedude.classfile.annotation.ClassElementValue;
import software.coley.cafedude.classfile.annotation.ElementValue;
import software.coley.cafedude.classfile.annotation.EnumElementValue;
import software.coley.cafedude.classfile.annotation.PrimitiveElementValue;
import software.coley.cafedude.classfile.annotation.TargetInfo;
import software.coley.cafedude.classfile.annotation.TargetInfo.CatchTargetInfo;
import software.coley.cafedude.classfile.annotation.TargetInfo.SuperTypeTargetInfo;
import software.coley.cafedude.classfile.annotation.TypeAnnotation;
import software.coley.cafedude.classfile.attribute.AnnotationDefaultAttribute;
import software.coley.cafedude.classfile.attribute.AnnotationsAttribute;
import software.coley.cafedude.classfile.attribute.Attribute;
import software.coley.cafedude.classfile.attribute.AttributeConstants;
import software.coley.cafedude.classfile.attribute.AttributeContexts;
import software.coley.cafedude.classfile.attribute.BootstrapMethodsAttribute;
import software.coley.cafedude.classfile.attribute.BootstrapMethodsAttribute.BootstrapMethod;
import software.coley.cafedude.classfile.attribute.CodeAttribute;
import software.coley.cafedude.classfile.attribute.CodeAttribute.ExceptionTableEntry;
import software.coley.cafedude.classfile.attribute.ConstantValueAttribute;
import software.coley.cafedude.classfile.attribute.DefaultAttribute;
import software.coley.cafedude.classfile.attribute.EnclosingMethodAttribute;
import software.coley.cafedude.classfile.attribute.ExceptionsAttribute;
import software.coley.cafedude.classfile.attribute.InnerClassesAttribute;
import software.coley.cafedude.classfile.attribute.InnerClassesAttribute.InnerClass;
import software.coley.cafedude.classfile.attribute.LocalVariableTableAttribute;
import software.coley.cafedude.classfile.attribute.LocalVariableTableAttribute.VarEntry;
import software.coley.cafedude.classfile.attribute.LocalVariableTypeTableAttribute;
import software.coley.cafedude.classfile.attribute.LocalVariableTypeTableAttribute.VarTypeEntry;
import software.coley.cafedude.classfile.attribute.ModuleAttribute;
import software.coley.cafedude.classfile.attribute.ModuleAttribute.Requires;
import software.coley.cafedude.classfile.attribute.NestHostAttribute;
import software.coley.cafedude.classfile.attribute.NestMembersAttribute;
import software.coley.cafedude.classfile.attribute.ParameterAnnotationsAttribute;
import software.coley.cafedude.classfile.attribute.PermittedClassesAttribute;
import software.coley.cafedude.classfile.attribute.RecordAttribute;
import software.coley.cafedude.classfile.attribute.SignatureAttribute;
import software.coley.cafedude.classfile.attribute.SourceFileAttribute;
import software.coley.cafedude.classfile.behavior.AttributeHolder;
import software.coley.cafedude.classfile.constant.ConstDynamic;
import software.coley.cafedude.classfile.constant.CpClass;
import software.coley.cafedude.classfile.constant.CpEntry;
import software.coley.cafedude.classfile.constant.CpUtf8;
import software.coley.cafedude.classfile.instruction.BasicInstruction;
import software.coley.cafedude.classfile.instruction.CpRefInstruction;
import software.coley.cafedude.classfile.instruction.Instruction;
import software.coley.cafedude.classfile.instruction.IntOperandInstruction;
import software.coley.cafedude.classfile.instruction.LookupSwitchInstruction;
import software.coley.cafedude.classfile.instruction.TableSwitchInstruction;
import software.coley.cafedude.io.AttributeHolderType;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static software.coley.cafedude.classfile.instruction.Opcodes.*;

/**
 * A transformer to remove illegal attributes and data from a class.
 *
 * @author Matt Coley
 */
public class IllegalStrippingTransformer extends Transformer implements ConstantPoolConstants {
	private static final Pattern UTF8_WORD = Pattern.compile("[<>;/$\\w]+");
	private static final Logger logger = LoggerFactory.getLogger(IllegalStrippingTransformer.class);

	/**
	 * @param clazz
	 * 		Class to strip.
	 */
	public IllegalStrippingTransformer(ClassFile clazz) {
		super(clazz);
	}

	// TODO: Make it possible to track if calling transform did remove anything

	@Override
	public void transform() {
		logger.debug("Transforming '{}'", clazz.getName());

		// Record existing CP refs.
		Set<CpEntry> cpAccesses = clazz.cpAccesses();

		// Remove members with illegal descriptors.
		// If any of these exist the class isn't going to ever be loaded (without throwing a LinkageError)
		// so its fair game to toss this stuff just to make ASM happy.
		clazz.getFields().removeIf(field -> isInvalidDesc(field.getType()));
		clazz.getMethods().removeIf(method -> isInvalidDesc(method.getType()));

		// Strip attributes that are not valid.
		clazz.getAttributes().removeIf(attribute -> !isValidWrapped(clazz, attribute));
		for (Field field : clazz.getFields())
			field.getAttributes().removeIf(attribute -> !isValidWrapped(field, attribute));
		Set<ConstDynamic> dynamicCpReferences = Collections.newSetFromMap(new IdentityHashMap<>());
		for (Method method : clazz.getMethods()) {
			method.getAttributes().removeIf(attribute -> !isValidWrapped(method, attribute));
			CodeAttribute code = method.getAttribute(CodeAttribute.class);
			if (code != null) {
				removeInvalidInstructions(code);
				removeInvalidVariables(code);
				collectDynamicCpReferences(code, dynamicCpReferences);
			}
		}

		// Strip BSM class attribute if it is unused.
		removeInvalidBootstrapMethodAttribute(dynamicCpReferences);

		// Record filtered CP refs, the difference of the sets are the indices that were referenced
		// by removed attributes/data.
		Set<CpEntry> filteredCpAccesses = clazz.cpAccesses();
		cpAccesses.removeAll(filteredCpAccesses);
	}

	protected void removeInvalidInstructions(@Nonnull CodeAttribute code) {
		List<Instruction> instructions = code.getInstructions();
		if (instructions.size() <= 1)
			return;
		int maxPc = code.computeOffsetOf(instructions.get(instructions.size() - 1));

		// Remove junk try-catch entries with bogus offsets
		List<ExceptionTableEntry> exceptions = code.getExceptionTable();
		for (int i = exceptions.size() - 1; i >= 0; i--) {
			ExceptionTableEntry entry = exceptions.get(i);
			if (entry.getStartPc() > maxPc || entry.getEndPc() > maxPc || entry.getHandlerPc() > maxPc)
				exceptions.remove(i);
		}

		// Remove any jump instructions that point to bogus offsets
		// - The code is already unverifiable, so when we stub out the instruction with
		//   a 'return' the code is still unverifiable but fixes the ASM crash.
		// - The number of bytes if kept the same, so valid jump instructions elsewhere
		//   in the method are not broken.
		// - This de-syncs the stack-frame entries for some reason I haven't looked into but
		//   since the code that relies on these tricks already requires use of -noverify
		//   I don't really feel like looking into it. Just use SKIP_FRAMES with ASM on these classes.
		for (int i = instructions.size() - 1; i >= 0; i--) {
			Instruction instruction = instructions.get(i);
			int op = instruction.getOpcode();
			if (((op >= IFEQ && op <= JSR) || (op == GOTO_W || op == JSR_W)) && instruction instanceof IntOperandInstruction jump) {
				int jumpOffset = code.computeOffsetOf(jump) + jump.getOperand();
				if (jumpOffset > maxPc || jumpOffset < 0) {
					int size = instruction.computeSize();
					instructions.set(i, new BasicInstruction(RETURN));
					for (int j = 0; j < size - 1; j++)
						instructions.add(i, new BasicInstruction(NOP));
				}
			} else if (instruction instanceof TableSwitchInstruction tswitch) {
				int switchOffset = code.computeOffsetOf(tswitch);
				if (switchOffset + tswitch.getDefault() > maxPc
						|| switchOffset + tswitch.getDefault() < 0
						|| tswitch.getOffsets().stream().anyMatch(o -> switchOffset + o > maxPc)
						|| tswitch.getOffsets().stream().anyMatch(o -> switchOffset + o < 0)) {
					int size = instruction.computeSize();
					instructions.set(i, new BasicInstruction(RETURN));
					for (int j = 0; j < size - 1; j++)
						instructions.add(i, new BasicInstruction(NOP));
				}
			} else if (instruction instanceof LookupSwitchInstruction lswitch) {
				int switchOffset = code.computeOffsetOf(lswitch);
				if (switchOffset + lswitch.getDefault() > maxPc
						|| switchOffset + lswitch.getDefault() < 0
						|| lswitch.getOffsets().stream().anyMatch(o -> switchOffset + o > maxPc)
						|| lswitch.getOffsets().stream().anyMatch(o -> switchOffset + o < 0)) {
					int size = instruction.computeSize();
					instructions.set(i, new BasicInstruction(RETURN));
					for (int j = 0; j < size - 1; j++)
						instructions.add(i, new BasicInstruction(NOP));
				}
			}
		}
	}

	protected void removeInvalidVariables(@Nonnull CodeAttribute code) {
		// In ASM's ClassReader.readCode(...) the startPc + length are used as labels[pc+length] which can be OOB.
		Instruction lastInstruction = code.getInstructions().get(code.getInstructions().size() - 1);
		int maxPc = code.computeOffsetOf(lastInstruction) + lastInstruction.computeSize();

		LocalVariableTableAttribute lvta = code.getAttribute(LocalVariableTableAttribute.class);
		LocalVariableTypeTableAttribute lvtta = code.getAttribute(LocalVariableTypeTableAttribute.class);
		if (lvta != null) lvta.getEntries().removeIf(e -> e.getStartPc() + e.getLength() > maxPc);
		if (lvtta != null) lvtta.getEntries().removeIf(e -> e.getStartPc() + e.getLength() > maxPc);
	}

	protected void collectDynamicCpReferences(@Nonnull CodeAttribute code, @Nonnull Set<ConstDynamic> dynamicCpReferences) {
		for (Instruction instruction : code.getInstructions())
			if (instruction instanceof CpRefInstruction cpRefInstruction
					&& cpRefInstruction.getEntry() instanceof ConstDynamic referencedDynamic)
				dynamicCpReferences.add(referencedDynamic);
	}

	protected void removeInvalidBootstrapMethodAttribute(@Nonnull Set<ConstDynamic> dynamicCpReferences) {
		// ASM will try to read this attribute if any CP entry exists for DYNAMIC or INVOKE_DYNAMIC.
		// If no methods actually refer to those CP entries, this attribute can be filled with garbage,
		// in which case we will want to remove it.
		if (dynamicCpReferences.isEmpty()) {
			// There are no dynamic references found in any method's code. Remove the BSM attribute.
			BootstrapMethodsAttribute bsmAttribute = clazz.getAttribute(BootstrapMethodsAttribute.class);
			if (bsmAttribute != null)
				clazz.getAttributes().remove(bsmAttribute);
		}

		// Remove any unused dynamic cp references in the constant pool.
		pool.removeIf(c -> c instanceof ConstDynamic cd && !dynamicCpReferences.contains(cd));
	}

	protected boolean isValidWrapped(@Nonnull AttributeHolder holder, @Nonnull Attribute attribute) {
		try {
			return isValid(holder, attribute);
		} catch (Exception ex) {
			logger.warn("Encountered exception when parsing attribute '{}' in context '{}', dropping it",
					attribute.getClass().getName(),
					holder.getHolderType().name());
			return false;
		}
	}

	protected boolean isValid(@Nonnull AttributeHolder holder, @Nonnull Attribute attribute) {
		Map<CpEntry, Predicate<Integer>> expectedTypeMasks = new HashMap<>();
		Map<CpEntry, Predicate<CpEntry>> cpEntryValidators = new HashMap<>();

		// Check name index
		int maxCpIndex = pool.size();
		if (attribute.getName().getIndex() > maxCpIndex)
			return false;

		// Cannot investigate directly unsupported attributes.
		if (attribute instanceof DefaultAttribute)
			return true;

		// Holder must be allowed to hold the given attribute
		String name = attribute.getName().getText();

		// Check for illegal usage contexts
		EnumSet<AttributeHolderType> allowedContexts = AttributeContexts.getAllowedContexts(name);
		AttributeHolderType holderType = holder.getHolderType();
		if (!allowedContexts.contains(holderType)) {
			logger.debug("Found '{}' declared in illegal context {}, allowed contexts: {}",
					name, holderType.name(), allowedContexts);
			return false;
		} else if (!Modifiers.has(clazz.getAccess(), Modifiers.ACC_MODULE) && name.toLowerCase().startsWith("module")) {
			logger.debug("Found '{}' in non-module class", name);
			return false;
		}

		// Check indices match certain types (key=cp_index, value=mask of allowed cp_tags)
		boolean allow0Case = false;
		switch (name) {
			case AttributeConstants.CONSTANT_VALUE:
				CpEntry valueIndex = ((ConstantValueAttribute) attribute).getConstantValue();
				expectedTypeMasks.put(valueIndex, i -> (i >= INTEGER && i <= STRING));
				break;
			case AttributeConstants.RUNTIME_INVISIBLE_ANNOTATIONS:
			case AttributeConstants.RUNTIME_VISIBLE_ANNOTATIONS:
			case AttributeConstants.RUNTIME_INVISIBLE_TYPE_ANNOTATIONS:
			case AttributeConstants.RUNTIME_VISIBLE_TYPE_ANNOTATIONS:
				AnnotationsAttribute annotations = (AnnotationsAttribute) attribute;
				for (Annotation anno : annotations.getAnnotations())
					addAnnotationValidation(holder, expectedTypeMasks, cpEntryValidators, anno);
				break;
			case AttributeConstants.RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS:
			case AttributeConstants.RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS: {
				// Sanity check
				if (holderType != AttributeHolderType.METHOD)
					return false;
				ParameterAnnotationsAttribute paramAnnotations = (ParameterAnnotationsAttribute) attribute;

				// Compare against actual number of parameters
				Method method = (Method) holder;
				String desc = method.getType().getText();
				int parameterCount = Descriptor.from(desc).getParameterCount();
				if (paramAnnotations.getParameterAnnotations().keySet().stream()
						.anyMatch(key -> key >= parameterCount)) {
					String methodName = method.getName().getText();
					logger.debug("Out of bounds parameter-annotation indices used on method {}", methodName);
					return false;
				}

				// Filter annotations
				Collection<List<Annotation>> parameterAnnos = paramAnnotations.getParameterAnnotations().values();
				for (List<Annotation> annotationList : parameterAnnos)
					for (Annotation anno : annotationList)
						addAnnotationValidation(holder, expectedTypeMasks, cpEntryValidators, anno);
				break;
			}
			case AttributeConstants.ANNOTATION_DEFAULT:
				AnnotationDefaultAttribute annotationDefault = (AnnotationDefaultAttribute) attribute;
				ElementValue elementValue = annotationDefault.getElementValue();
				addElementValueValidation(expectedTypeMasks, cpEntryValidators, elementValue);
				break;
			case AttributeConstants.NEST_HOST:
				NestHostAttribute nestHost = (NestHostAttribute) attribute;
				cpEntryValidators.put(nestHost.getHostClass(), matchClass());
				break;
			case AttributeConstants.NEST_MEMBERS:
				NestMembersAttribute nestMembers = (NestMembersAttribute) attribute;
				for (CpClass member : nestMembers.getMemberClasses()) {
					cpEntryValidators.put(member, matchClass());
				}
				break;
			case AttributeConstants.ENCLOSING_METHOD:
				EnclosingMethodAttribute enclosingMethod = (EnclosingMethodAttribute) attribute;
				expectedTypeMasks.put(enclosingMethod.getClassEntry(), i -> i == CLASS);
				cpEntryValidators.put(enclosingMethod.getClassEntry(), matchClass());

				// method_index must be zero if the current class was immediately enclosed in source code by an
				//   instance initializer, static initializer, instance variable initializer,
				//   or class variable initializer
				// Otherwise it points to the method name_type value
				expectedTypeMasks.put(enclosingMethod.getMethodEntry(), i -> i == 0 || i == NAME_TYPE);
				allow0Case = (enclosingMethod.getMethodEntry() == null);
				break;
			case AttributeConstants.EXCEPTIONS:
				ExceptionsAttribute exceptions = (ExceptionsAttribute) attribute;
				for (CpClass exceptionTypeIndex : exceptions.getExceptionTable()) {
					cpEntryValidators.put(exceptionTypeIndex, matchClass());
				}
				break;
			case AttributeConstants.INNER_CLASSES:
				InnerClassesAttribute innerClasses = (InnerClassesAttribute) attribute;
				for (InnerClass innerClass : innerClasses.getInnerClasses()) {
					expectedTypeMasks.put(innerClass.getInnerClassInfo(), i -> i == 0 || i == CLASS);
					cpEntryValidators.put(innerClass.getInnerClassInfo(), matchClass());

					// 0 if the defining class is the top-level class
					expectedTypeMasks.put(innerClass.getOuterClassInfo(), i -> i == 0 || i == CLASS);

					// 0 if anonymous, otherwise name index
					expectedTypeMasks.put(innerClass.getInnerName(), i -> i == 0 || i == UTF8);
					allow0Case |= innerClass.getInnerClassInfo() == null
							|| innerClass.getOuterClassInfo() == null
							|| innerClass.getInnerName() == null;
				}
				break;
			case AttributeConstants.CODE: {
				// Sanity check
				if (holderType != AttributeHolderType.METHOD)
					return false;

				// Method cannot be abstract
				Method method = (Method) holder;
				if ((method.getAccess() & Modifiers.ACC_ABSTRACT) > 0) {
					logger.debug("Illegal 'Code' attribute on abstract method {}", method.getName().getText());
					return false;
				}
				CodeAttribute code = (CodeAttribute) attribute;

				// Prune bad code sub-attributes
				code.getAttributes().removeIf(sub -> !isValid(code, sub));

				// Ensure exception indices are valid
				for (ExceptionTableEntry entry : code.getExceptionTable()) {
					CpClass catchType = entry.getCatchType();
					if (catchType == null) {
						allow0Case = true;
					} else {
						cpEntryValidators.put(catchType, matchClass());
						expectedTypeMasks.put(catchType, i -> i == 0 || i == CLASS);
					}
				}
				break;
			}
			case AttributeConstants.SIGNATURE:
				SignatureAttribute signatureAttribute = (SignatureAttribute) attribute;
				cpEntryValidators.put(signatureAttribute.getSignature(), e -> matchSignature((CpUtf8) e, holder));
				break;
			case AttributeConstants.SOURCE_FILE:
				SourceFileAttribute sourceFileAttribute = (SourceFileAttribute) attribute;
				cpEntryValidators.put(sourceFileAttribute.getSourceFilename(), matchUtf8NonEmpty());
				break;
			case AttributeConstants.MODULE:
				ModuleAttribute moduleAttribute = (ModuleAttribute) attribute;
				expectedTypeMasks.put(moduleAttribute.getVersion(), i -> i == 0 || i == UTF8);
				if (moduleAttribute.getVersion() == null)
					allow0Case = true;
				for (Requires requires : moduleAttribute.getRequires()) {
					expectedTypeMasks.put(requires.getVersion(), i -> i == 0 || i == UTF8);
				}
				break;
			case AttributeConstants.BOOTSTRAP_METHODS:
				BootstrapMethodsAttribute bootstrapMethodsAttribute = (BootstrapMethodsAttribute) attribute;
				for (BootstrapMethod bsm : bootstrapMethodsAttribute.getBootstrapMethods()) {
					expectedTypeMasks.put(bsm.getBsmMethodRef(), i -> i == METHOD_HANDLE);
					// Arguments must be loadable types
					for (CpEntry arg : bsm.getArgs()) {
						expectedTypeMasks.put(arg, i ->
								(i >= INTEGER && i <= STRING) ||
										(i >= METHOD_HANDLE && i <= DYNAMIC));
					}
				}
				break;
			case AttributeConstants.LOCAL_VARIABLE_TABLE:
				LocalVariableTableAttribute varTable = (LocalVariableTableAttribute) attribute;
				for (VarEntry entry : varTable.getEntries()) {
					expectedTypeMasks.put(entry.getName(), i -> i == UTF8);
					expectedTypeMasks.put(entry.getDesc(), i -> i == UTF8);
					cpEntryValidators.put(entry.getName(), matchUtf8ValidQualifiedName().and(matchUtf8Word()));
					cpEntryValidators.put(entry.getDesc(), matchUtf8FieldDescriptor());
				}
				break;
			case AttributeConstants.LOCAL_VARIABLE_TYPE_TABLE:
				LocalVariableTypeTableAttribute typeTable = (LocalVariableTypeTableAttribute) attribute;
				for (VarTypeEntry entry : typeTable.getEntries()) {
					cpEntryValidators.put(entry.getName(), matchUtf8ValidQualifiedName().and(matchUtf8Word()));
					cpEntryValidators.put(entry.getSignature(), matchUtf8NonEmpty());
				}
				break;
			case AttributeConstants.PERMITTED_SUBCLASSES:
				PermittedClassesAttribute permittedClassesAttribute = (PermittedClassesAttribute) attribute;
				for (CpClass index : permittedClassesAttribute.getClasses()) {
					cpEntryValidators.put(index, matchClass());
				}
				break;
			case AttributeConstants.RECORD:
				RecordAttribute recordAttribute = (RecordAttribute) attribute;
				for (RecordAttribute.RecordComponent component : recordAttribute.getComponents()) {
					cpEntryValidators.put(component.getName(), matchUtf8Word());
					cpEntryValidators.put(component.getDesc(), matchUtf8FieldDescriptor());

					// Prune bad component sub-attributes (annotations)
					component.getAttributes().removeIf(sub -> !isValid(component, sub));
				}
				break;
			case AttributeConstants.LINE_NUMBER_TABLE:
			case AttributeConstants.SOURCE_DEBUG_EXTENSION:
			case AttributeConstants.DEPRECATED:
			case AttributeConstants.SYNTHETIC:
				// no-op
				break;
			case AttributeConstants.CHARACTER_RANGE_TABLE:
			case AttributeConstants.COMPILATION_ID:
			case AttributeConstants.METHOD_PARAMETERS:
			case AttributeConstants.MODULE_HASHES:
			case AttributeConstants.MODULE_MAIN_CLASS:
			case AttributeConstants.MODULE_PACKAGES:
			case AttributeConstants.MODULE_RESOLUTION:
			case AttributeConstants.MODULE_TARGET:
			case AttributeConstants.SOURCE_ID:
			case AttributeConstants.STACK_MAP_TABLE:
			default:
				// TODO: The rest of these when each has their own attribute class
				break;
		}
		for (Map.Entry<CpEntry, Predicate<Integer>> entry : expectedTypeMasks.entrySet()) {
			// Referenced pool entry must be in pool range
			//  - Yes, the CP doesn't start at 0, but there are special cases where it is allowed.
			CpEntry cpEntry = entry.getKey();

			// Referenced entry must match type
			if (allow0Case && cpEntry == null)
				continue; // skip edge case

			// cpEntryValidators
			if (cpEntry == null) {
				logger.debug("Invalid '{}' attribute on {}, contains CP reference to null!",
						name, holderType.name());
				return false;
			}
			int tag = cpEntry.getTag();
			if (!entry.getValue().test(tag)) {
				logger.debug("Invalid '{}' attribute on {}, contains CP reference to index with wrong type!",
						name, holderType.name());
				return false;
			}
			if (cpEntryValidators.containsKey(cpEntry) && !cpEntryValidators.get(cpEntry).test(cpEntry)) {
				logger.debug("Invalid '{}' attribute," +
						" contains CP reference to item that does not match criteria at index: {}", name, cpEntry);
				return false;
			}
		}
		return true;
	}

	protected void addAnnotationValidation(@Nullable AttributeHolder holder,
	                                       @Nonnull Map<CpEntry, Predicate<Integer>> expectedTypeMasks,
	                                       @Nonnull Map<CpEntry, Predicate<CpEntry>> cpEntryValidators,
	                                       @Nonnull Annotation anno) {
		expectedTypeMasks.put(anno.getType(), i -> i == UTF8);
		cpEntryValidators.put(anno.getType(), matchUtf8FieldDescriptor());
		for (Map.Entry<CpUtf8, ElementValue> entry : anno.getValues().entrySet()) {
			CpUtf8 elementTypeIndex = entry.getKey();
			cpEntryValidators.put(elementTypeIndex, matchUtf8ValidQualifiedName());
			addElementValueValidation(expectedTypeMasks, cpEntryValidators, entry.getValue());
		}
		if (anno instanceof TypeAnnotation) {
			TypeAnnotation typeAnnotation = (TypeAnnotation) anno;
			TargetInfo targetInfo = typeAnnotation.getTargetInfo();
			switch (targetInfo.getTargetTypeKind()) {
				case TYPE_PARAMETER_BOUND_TARGET:
					break;
				case TYPE_PARAMETER_TARGET:
					break;
				case FORMAL_PARAMETER_TARGET:
					break;
				case TYPE_ARGUMENT_TARGET:
					break;
				case LOCALVAR_TARGET:
					// TODO: Ensure variables outline matches what is in code's variables attribute
					break;
				case THROWS_TARGET:
					// TODO: Verify with a sample what the target holder type should be
					break;
				case OFFSET_TARGET:
					// TODO: Compare to code instructions / length
					break;
				case SUPERTYPE_TARGET:
					if (holder instanceof ClassFile) {
						SuperTypeTargetInfo superTypeTargetInfo = (SuperTypeTargetInfo) targetInfo;
						if (!superTypeTargetInfo.isExtends()) {
							ClassFile classFile = (ClassFile) holder;
							// Enforce interfaces range
							if (superTypeTargetInfo.getSuperTypeIndex() >= classFile.getInterfaceClasses().size()) {
								expectedTypeMasks.put(null, i -> false);
							}
						}
					} else {
						// Illegal target kind for situation
						expectedTypeMasks.put(null, i -> false);
					}
					break;

				case CATCH_TARGET:
					if (holder instanceof CodeAttribute) {
						CodeAttribute code = (CodeAttribute) holder;
						CatchTargetInfo catchTargetInfo = (CatchTargetInfo) targetInfo;
						// Enforce table range
						if (catchTargetInfo.getExceptionTableIndex() >= code.getExceptionTable().size()) {
							expectedTypeMasks.put(null, i -> false);
						}
					} else {
						// Illegal target kind for situation
						expectedTypeMasks.put(null, i -> false);
					}
					break;
				case EMPTY_TARGET:
				default:
					// no-op
					break;
			}
		}
	}

	protected void addElementValueValidation(@Nonnull Map<CpEntry, Predicate<Integer>> expectedTypeMasks,
	                                         @Nonnull Map<CpEntry, Predicate<CpEntry>> cpEntryValidators,
	                                         @Nonnull ElementValue elementValue) {
		if (elementValue instanceof ClassElementValue classElement) {
			CpUtf8 classIndex = classElement.getClassEntry();
			cpEntryValidators.put(classIndex, matchUtf8ValidQualifiedName());
		} else if (elementValue instanceof EnumElementValue enumElement) {
			cpEntryValidators.put(enumElement.getType(), matchUtf8FieldDescriptor());
		} else if (elementValue instanceof PrimitiveElementValue primitive) {
			CpEntry primitiveEntry = primitive.getValue();
			expectedTypeMasks.put(primitiveEntry, i -> (i >= INTEGER && i <= DOUBLE));
		} else if (elementValue instanceof AnnotationElementValue annotationElement) {
			Annotation annotation = annotationElement.getAnnotation();
			addAnnotationValidation(null, expectedTypeMasks, cpEntryValidators, annotation);
		} else if (elementValue instanceof ArrayElementValue arrayElement) {
			List<ElementValue> array = arrayElement.getArray();
			for (ElementValue arrayValue : array)
				addElementValueValidation(expectedTypeMasks, cpEntryValidators, arrayValue);
		}
	}

	/**
	 * @param e
	 * 		Signature string constant pool entry.
	 * @param context
	 * 		The context of the signature declaration.
	 *
	 * @return {@code true} when the signature is valid and can be kept.
	 */
	protected boolean matchSignature(@Nonnull CpUtf8 e, @Nonnull AttributeHolder context) {
		return matchUtf8NonEmpty().test(e);
	}

	@Nonnull
	protected Predicate<CpEntry> matchClass() {
		return e -> e instanceof CpClass && matchUtf8ValidQualifiedName().test(((CpClass) e).getName());
	}

	@Nonnull
	protected Predicate<CpEntry> matchUtf8ValidQualifiedName() {
		return e -> {
			if (!(e instanceof CpUtf8)) return false;
			String text = ((CpUtf8) e).getText();
			return !text.isEmpty()
					// From #jvms-4.2.2 - Class names cannot contain any of these characters
					&& text.indexOf('.') < 0
					&& text.indexOf(';') < 0
					&& text.indexOf('[') < 0;
		};
	}

	@Nonnull
	protected Predicate<CpEntry> matchUtf8FieldDescriptor() {
		return e -> (e instanceof CpUtf8) && !isInvalidDesc((CpUtf8) e);
	}

	@Nonnull
	protected Predicate<CpEntry> matchUtf8NonEmpty() {
		return e -> e instanceof CpUtf8 && !((CpUtf8) e).getText().isEmpty();
	}

	@Nonnull
	protected Predicate<CpEntry> matchUtf8Word() {
		return e -> e instanceof CpUtf8 utf8 && UTF8_WORD.matcher(utf8.getText()).matches();
	}

	protected static boolean isInvalidDesc(@Nonnull CpUtf8 descEntry) {
		try {
			String descUtf8 = descEntry.getText();
			Descriptor parsed = Descriptor.from(descUtf8);
			return parsed.getKind() == Descriptor.Kind.ILLEGAL;
		} catch (Throwable t) {
			return true;
		}
	}

}
