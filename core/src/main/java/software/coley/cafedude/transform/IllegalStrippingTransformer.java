package software.coley.cafedude.transform;

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
import software.coley.cafedude.classfile.constant.CpClass;
import software.coley.cafedude.classfile.constant.CpEntry;
import software.coley.cafedude.classfile.constant.CpUtf8;
import software.coley.cafedude.io.AttributeContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A transformer to remove illegal attributes and data from a class.
 *
 * @author Matt Coley
 */
public class IllegalStrippingTransformer extends Transformer implements ConstantPoolConstants {
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

		// Strip BSM class attribute if it is unused.
		removeInvalidBootstrapMethodAttribute();

		// Strip attributes that are not valid.
		clazz.getAttributes().removeIf(attribute -> !isValidWrapped(clazz, attribute));
		for (Field field : clazz.getFields())
			field.getAttributes().removeIf(attribute -> !isValidWrapped(field, attribute));
		for (Method method : clazz.getMethods())
			method.getAttributes().removeIf(attribute -> !isValidWrapped(method, attribute));

		// Record filtered CP refs, the difference of the sets are the indices that were referenced
		// by removed attributes/data.
		Set<CpEntry> filteredCpAccesses = clazz.cpAccesses();
		cpAccesses.removeAll(filteredCpAccesses);
	}

	private void removeInvalidBootstrapMethodAttribute() {
		// ASM will try to read this attribute if any CP entry exists for DYNAMIC or INVOKE_DYNAMIC.
		// If no methods actually refer to those CP entries, this attribute can be filled with garbage,
		// in which case we will want to remove it.
		BootstrapMethodsAttribute bsmAttribute = clazz.getAttribute(BootstrapMethodsAttribute.class);
		if (bsmAttribute != null) {
			List<CpEntry> dynamicCpAccesses = clazz.getMethods().stream()
					.flatMap(m -> m.cpAccesses().stream())
					.filter(cp -> cp != null && (cp.getTag() == CpEntry.DYNAMIC || cp.getTag() == CpEntry.INVOKE_DYNAMIC))
					.collect(Collectors.toList());
			if (dynamicCpAccesses.isEmpty())
				clazz.getAttributes().remove(bsmAttribute);
		}
	}

	private boolean isValidWrapped(@Nonnull AttributeHolder holder, @Nonnull Attribute attribute) {
		try {
			return isValid(holder, attribute);
		} catch (Exception ex) {
			logger.warn("Encountered exception when parsing attribute '{}' in context '{}', dropping it",
					attribute.getClass().getName(),
					holder.getHolderType().name());
			return false;
		}
	}

	private static boolean isInvalidDesc(@Nonnull CpUtf8 descEntry) {
		try {
			String descUtf8 = descEntry.getText();
			Descriptor parsed = Descriptor.from(descUtf8);
			return parsed.getKind() == Descriptor.Kind.ILLEGAL;
		} catch (Throwable t) {
			return true;
		}
	}

	private boolean isValid(@Nonnull AttributeHolder holder, @Nonnull Attribute attribute) {
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
		Collection<AttributeContext> allowedContexts = AttributeContexts.getAllowedContexts(name);
		AttributeContext context = holder.getHolderType();
		if (!allowedContexts.contains(context)) {
			logger.debug("Found '{}' declared in illegal context {}, allowed contexts: {}",
					name, context.name(), allowedContexts);
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
				if (context != AttributeContext.METHOD)
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
				if (context != AttributeContext.METHOD)
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
						name, context.name());
				return false;
			}
			int tag = cpEntry.getTag();
			if (!entry.getValue().test(tag)) {
				logger.debug("Invalid '{}' attribute on {}, contains CP reference to index with wrong type!",
						name, context.name());
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

	private void addAnnotationValidation(@Nullable AttributeHolder holder,
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

	private void addElementValueValidation(@Nonnull Map<CpEntry, Predicate<Integer>> expectedTypeMasks,
	                                       @Nonnull Map<CpEntry, Predicate<CpEntry>> cpEntryValidators,
	                                       @Nonnull ElementValue elementValue) {
		if (elementValue instanceof ClassElementValue) {
			CpUtf8 classIndex = ((ClassElementValue) elementValue).getClassEntry();
			cpEntryValidators.put(classIndex, matchUtf8ValidQualifiedName());
		} else if (elementValue instanceof EnumElementValue) {
			EnumElementValue enumElementValue = (EnumElementValue) elementValue;
			cpEntryValidators.put(enumElementValue.getType(), matchUtf8FieldDescriptor());
		} else if (elementValue instanceof PrimitiveElementValue) {
			CpEntry primitiveEntry = ((PrimitiveElementValue) elementValue).getValue();
			expectedTypeMasks.put(primitiveEntry, i -> (i >= INTEGER && i <= DOUBLE));
		} else if (elementValue instanceof AnnotationElementValue) {
			Annotation annotation = ((AnnotationElementValue) elementValue).getAnnotation();
			addAnnotationValidation(null, expectedTypeMasks, cpEntryValidators, annotation);
		} else if (elementValue instanceof ArrayElementValue) {
			List<ElementValue> array = ((ArrayElementValue) elementValue).getArray();
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
	private Predicate<CpEntry> matchClass() {
		return e -> e instanceof CpClass && matchUtf8ValidQualifiedName().test(((CpClass) e).getName());
	}

	@Nonnull
	private Predicate<CpEntry> matchUtf8ValidQualifiedName() {
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
	private Predicate<CpEntry> matchUtf8FieldDescriptor() {
		return e -> (e instanceof CpUtf8) && !isInvalidDesc((CpUtf8) e);
	}

	@Nonnull
	private Predicate<CpEntry> matchUtf8NonEmpty() {
		return e -> e instanceof CpUtf8 && !((CpUtf8) e).getText().isEmpty();
	}

	@Nonnull
	private Predicate<CpEntry> matchUtf8Word() {
		return e -> e instanceof CpUtf8 && ((CpUtf8) e).getText().matches("[<>;/$\\w]+");
	}
}
