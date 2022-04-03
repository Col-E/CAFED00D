package me.coley.cafedude.transform;

import me.coley.cafedude.Constants;
import me.coley.cafedude.Constants.Attributes;
import me.coley.cafedude.Constants.ConstantPool;
import me.coley.cafedude.classfile.ClassFile;
import me.coley.cafedude.classfile.Descriptor;
import me.coley.cafedude.classfile.Field;
import me.coley.cafedude.classfile.Method;
import me.coley.cafedude.classfile.annotation.Annotation;
import me.coley.cafedude.classfile.annotation.ClassElementValue;
import me.coley.cafedude.classfile.annotation.ElementValue;
import me.coley.cafedude.classfile.annotation.EnumElementValue;
import me.coley.cafedude.classfile.annotation.PrimitiveElementValue;
import me.coley.cafedude.classfile.annotation.TargetInfo;
import me.coley.cafedude.classfile.annotation.TargetInfo.CatchTargetInfo;
import me.coley.cafedude.classfile.annotation.TargetInfo.SuperTypeTargetInfo;
import me.coley.cafedude.classfile.annotation.TypeAnnotation;
import me.coley.cafedude.classfile.annotation.Utf8ElementValue;
import me.coley.cafedude.classfile.attribute.AnnotationDefaultAttribute;
import me.coley.cafedude.classfile.attribute.AnnotationsAttribute;
import me.coley.cafedude.classfile.attribute.Attribute;
import me.coley.cafedude.classfile.attribute.AttributeContexts;
import me.coley.cafedude.classfile.attribute.BootstrapMethodsAttribute;
import me.coley.cafedude.classfile.attribute.BootstrapMethodsAttribute.BootstrapMethod;
import me.coley.cafedude.classfile.attribute.CodeAttribute;
import me.coley.cafedude.classfile.attribute.CodeAttribute.ExceptionTableEntry;
import me.coley.cafedude.classfile.attribute.ConstantValueAttribute;
import me.coley.cafedude.classfile.attribute.DefaultAttribute;
import me.coley.cafedude.classfile.attribute.EnclosingMethodAttribute;
import me.coley.cafedude.classfile.attribute.ExceptionsAttribute;
import me.coley.cafedude.classfile.attribute.InnerClassesAttribute;
import me.coley.cafedude.classfile.attribute.InnerClassesAttribute.InnerClass;
import me.coley.cafedude.classfile.attribute.LocalVariableTableAttribute;
import me.coley.cafedude.classfile.attribute.LocalVariableTableAttribute.VarEntry;
import me.coley.cafedude.classfile.attribute.LocalVariableTypeTableAttribute;
import me.coley.cafedude.classfile.attribute.LocalVariableTypeTableAttribute.VarTypeEntry;
import me.coley.cafedude.classfile.attribute.ModuleAttribute;
import me.coley.cafedude.classfile.attribute.ModuleAttribute.Exports;
import me.coley.cafedude.classfile.attribute.ModuleAttribute.Opens;
import me.coley.cafedude.classfile.attribute.ModuleAttribute.Provides;
import me.coley.cafedude.classfile.attribute.ModuleAttribute.Requires;
import me.coley.cafedude.classfile.attribute.NestHostAttribute;
import me.coley.cafedude.classfile.attribute.NestMembersAttribute;
import me.coley.cafedude.classfile.attribute.ParameterAnnotationsAttribute;
import me.coley.cafedude.classfile.attribute.PermittedClassesAttribute;
import me.coley.cafedude.classfile.attribute.RecordAttribute;
import me.coley.cafedude.classfile.attribute.RecordAttribute.RecordComponent;
import me.coley.cafedude.classfile.attribute.SignatureAttribute;
import me.coley.cafedude.classfile.attribute.SourceFileAttribute;
import me.coley.cafedude.classfile.behavior.AttributeHolder;
import me.coley.cafedude.classfile.constant.ConstPoolEntry;
import me.coley.cafedude.classfile.constant.CpClass;
import me.coley.cafedude.classfile.constant.CpInt;
import me.coley.cafedude.classfile.constant.CpUtf8;
import me.coley.cafedude.instruction.Instruction;
import me.coley.cafedude.io.AttributeContext;
import me.coley.cafedude.io.InstructionReader;
import me.coley.cafedude.io.InstructionWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Predicate;

import static me.coley.cafedude.Constants.Attributes.*;

/**
 * A transformer to remove illegal attributes and data from a class.
 *
 * @author Matt Coley
 */
public class IllegalStrippingTransformer extends Transformer {
	private static final int FORCE_FAIL = -1;
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
		logger.info("Transforming '{}'", clazz.getName());
		// Record existing CP refs
		Set<Integer> cpAccesses = clazz.cpAccesses();
		// Strip attributes that are not valid
		clazz.getAttributes().removeIf(attribute -> !isValidWrapped(clazz, attribute));
		for (Field field : clazz.getFields())
			field.getAttributes().removeIf(attribute -> !isValidWrapped(field, attribute));
		for (Method method : clazz.getMethods())
			method.getAttributes().removeIf(attribute -> !isValidWrapped(method, attribute));
		// Record filtered CP refs, the difference of the sets are the indices that were referenced
		// by removed attributes/data.
		Set<Integer> filteredCpAccesses = clazz.cpAccesses();
		cpAccesses.removeAll(filteredCpAccesses);
		// Replace with dummy entries (Removing and re-indexing things would be computationally expensive)
		int max = pool.size();
		for (int index : cpAccesses) {
			if (index == 0 || index >= max - 1)
				continue;
			ConstPoolEntry cpe = pool.get(index);
			switch (cpe.getTag()) {
				case ConstantPool.DYNAMIC:
				case ConstantPool.INVOKE_DYNAMIC:
					logger.debug("Removing now unused CP entry: {}={}", index, cpe.getClass().getSimpleName());
					pool.set(index, new CpInt(0));
					break;
				default:
					// TODO: When the full class file specification is complete we can aggressively prune other types.
					//  - for now we only remove specific X_DYNAMIC types since we can be sure removing them is safe
					//    in the context of references to it being removed due to an invalid BootstrapMethodsAttribute
					break;
			}
		}
		// Patch illegal instructions.
		IllegalStrippingInstructionsReader fallbackReader = new IllegalStrippingInstructionsReader(clazz.getPool());
		InstructionReader reader = new InstructionReader(fallbackReader);
		InstructionWriter writer = new InstructionWriter();
		for (Method method : clazz.getMethods()) {
			Optional<Attribute> attribute = method.getAttributes().stream()
					.filter(x -> x instanceof CodeAttribute)
					.findFirst();
			if (attribute.isPresent()) {
				CodeAttribute code = (CodeAttribute) attribute.get();
				fallbackReader.rewritten = false;
				List<Instruction> instructions = reader.read(code);
				if (fallbackReader.rewritten) {
					code.setCode(writer.writeCode(instructions));
				}
			}
		}
	}

	private boolean isValidWrapped(AttributeHolder holder, Attribute attribute) {
		try {
			return isValid(holder, attribute);
		} catch (Exception ex) {
			logger.warn("Encountered exception when parsing attribute '{}' in context '{}', dropping it",
					attribute.getClass().getName(),
					holder.getHolderType().name());
			return false;
		}
	}

	private boolean isValid(AttributeHolder holder, Attribute attribute) {
		Map<Integer, Predicate<Integer>> expectedTypeMasks = new HashMap<>();
		Map<Integer, Predicate<ConstPoolEntry>> cpEntryValidators = new HashMap<>();
		// Check name index
		int maxCpIndex = pool.size();
		if (attribute.getNameIndex() > maxCpIndex)
			return false;
		// Cannot investigate directly unsupported attributes.
		if (attribute instanceof DefaultAttribute)
			return true;
		// Holder must be allowed to hold the given attribute
		String name = pool.getUtf(attribute.getNameIndex());
		// Check for illegal usage contexts
		Collection<AttributeContext> allowedContexts = AttributeContexts.getAllowedContexts(name);
		AttributeContext context = holder.getHolderType();
		if (!allowedContexts.contains(context)) {
			logger.debug("Found '{}' declared in illegal context {}, allowed contexts: {}",
					name, context.name(), allowedContexts);
			return false;
		}
		// Check indices match certain types (key=cp_index, value=mask of allowed cp_tags)
		boolean allow0Case = false;
		switch (name) {
			case CONSTANT_VALUE:
				int valueIndex = ((ConstantValueAttribute) attribute).getConstantValueIndex();
				expectedTypeMasks.put(valueIndex, i -> (i >= ConstantPool.INTEGER && i <= ConstantPool.STRING));
				break;
			case RUNTIME_INVISIBLE_ANNOTATIONS:
			case RUNTIME_VISIBLE_ANNOTATIONS:
			case RUNTIME_INVISIBLE_TYPE_ANNOTATIONS:
			case RUNTIME_VISIBLE_TYPE_ANNOTATIONS:
				AnnotationsAttribute annotations = (AnnotationsAttribute) attribute;
				for (Annotation anno : annotations.getAnnotations())
					addAnnotationValidation(holder, expectedTypeMasks, cpEntryValidators, anno);
				break;
			case RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS:
			case RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS: {
				// Sanity check
				if (context != AttributeContext.METHOD)
					return false;
				ParameterAnnotationsAttribute paramAnnotations = (ParameterAnnotationsAttribute) attribute;
				// Compare against actual number of parameters
				Method method = (Method) holder;
				String desc = pool.getUtf(method.getTypeIndex());
				int parameterCount = Descriptor.from(desc).getParameterCount();
				if (paramAnnotations.getParameterAnnotations().keySet().stream()
						.anyMatch(key -> key >= parameterCount)) {
					String methodName = pool.getUtf(method.getNameIndex());
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
			case ANNOTATION_DEFAULT:
				AnnotationDefaultAttribute annotationDefault = (AnnotationDefaultAttribute) attribute;
				ElementValue elementValue = annotationDefault.getElementValue();
				addElementValueValidation(holder, expectedTypeMasks, cpEntryValidators, elementValue);
				break;
			case NEST_HOST:
				NestHostAttribute nestHost = (NestHostAttribute) attribute;
				expectedTypeMasks.put(nestHost.getHostClassIndex(), i -> i == ConstantPool.CLASS);
				cpEntryValidators.put(nestHost.getHostClassIndex(), matchClassType());
				break;
			case NEST_MEMBERS:
				NestMembersAttribute nestMembers = (NestMembersAttribute) attribute;
				for (int memberIndex : nestMembers.getMemberClassIndices()) {
					expectedTypeMasks.put(memberIndex, i -> i == ConstantPool.CLASS);
					cpEntryValidators.put(memberIndex, matchClassType());
				}
				break;
			case ENCLOSING_METHOD:
				EnclosingMethodAttribute enclosingMethod = (EnclosingMethodAttribute) attribute;
				expectedTypeMasks.put(enclosingMethod.getClassIndex(), i -> i == ConstantPool.CLASS);
				cpEntryValidators.put(enclosingMethod.getClassIndex(), matchClassType());
				// method_index must be zero if the current class was immediately enclosed in source code by an
				//   instance initializer, static initializer, instance variable initializer,
				//   or class variable initializer
				// Otherwise it points to the method name_type value
				expectedTypeMasks.put(enclosingMethod.getMethodIndex(), i -> i == 0 || i == ConstantPool.NAME_TYPE);
				allow0Case = (enclosingMethod.getMethodIndex() == 0);
				break;
			case EXCEPTIONS:
				ExceptionsAttribute exceptions = (ExceptionsAttribute) attribute;
				for (int exceptionTypeIndex : exceptions.getExceptionIndexTable()) {
					expectedTypeMasks.put(exceptionTypeIndex, i -> i == ConstantPool.CLASS);
					cpEntryValidators.put(exceptionTypeIndex, matchClassType());
				}
				break;
			case INNER_CLASSES:
				InnerClassesAttribute innerClasses = (InnerClassesAttribute) attribute;
				for (InnerClass innerClass : innerClasses.getInnerClasses()) {
					expectedTypeMasks.put(innerClass.getInnerClassInfoIndex(), i -> i == 0 || i == ConstantPool.CLASS);
					cpEntryValidators.put(innerClass.getInnerClassInfoIndex(), matchClassType());
					// 0 if the defining class is the top-level class
					expectedTypeMasks.put(innerClass.getOuterClassInfoIndex(), i -> i == 0 || i == ConstantPool.CLASS);
					// 0 if anonymous, otherwise name index
					expectedTypeMasks.put(innerClass.getInnerNameIndex(), i -> i == 0 || i == ConstantPool.UTF8);
					allow0Case |= innerClass.getInnerClassInfoIndex() == 0
							|| innerClass.getOuterClassInfoIndex() == 0
							|| innerClass.getInnerNameIndex() == 0;
				}
				break;
			case CODE: {
				// Sanity check
				if (context != AttributeContext.METHOD)
					return false;
				// Method cannot be abstract
				Method method = (Method) holder;
				if ((method.getAccess() & Constants.ACC_ABSTRACT) > 0) {
					logger.debug("Illegal 'Code' attribute on abstract method {}", pool.getUtf(method.getNameIndex()));
					return false;
				}
				CodeAttribute code = (CodeAttribute) attribute;
				// Prune bad code sub-attributes
				code.getAttributes().removeIf(sub -> !isValid(code, sub));
				// Ensure exception indices are valid
				for (ExceptionTableEntry entry : code.getExceptionTable()) {
					expectedTypeMasks.put(entry.getCatchTypeIndex(), i -> i == 0 || i == ConstantPool.CLASS);
					if (entry.getCatchTypeIndex() == 0) {
						allow0Case = true;
					} else {
						cpEntryValidators.put(entry.getCatchTypeIndex(), matchClassType());
					}
				}
				break;
			}
			case SIGNATURE:
				SignatureAttribute signatureAttribute = (SignatureAttribute) attribute;
				expectedTypeMasks.put(signatureAttribute.getSignatureIndex(), i -> i == ConstantPool.UTF8);
				cpEntryValidators.put(signatureAttribute.getSignatureIndex(), matchNonEmptyUtf8());
				break;
			case SOURCE_FILE:
				SourceFileAttribute sourceFileAttribute = (SourceFileAttribute) attribute;
				expectedTypeMasks.put(sourceFileAttribute.getSourceFileNameIndex(), i -> i == ConstantPool.UTF8);
				cpEntryValidators.put(sourceFileAttribute.getSourceFileNameIndex(), matchNonEmptyUtf8());
				break;
			case Attributes.MODULE:
				ModuleAttribute moduleAttribute = (ModuleAttribute) attribute;
				expectedTypeMasks.put(moduleAttribute.getModuleIndex(), i -> i == ConstantPool.MODULE);
				expectedTypeMasks.put(moduleAttribute.getVersionIndex(), i -> i == 0 || i == ConstantPool.UTF8);
				if (moduleAttribute.getVersionIndex() == 0)
					allow0Case = true;
				for (Requires requires : moduleAttribute.getRequires()) {
					expectedTypeMasks.put(requires.getIndex(), i -> i == ConstantPool.MODULE);
					expectedTypeMasks.put(requires.getVersionIndex(), i -> i == 0 || i == ConstantPool.UTF8);
				}
				for (Exports exports : moduleAttribute.getExports()) {
					expectedTypeMasks.put(exports.getIndex(), i -> i == ConstantPool.PACKAGE);
					for (int moduleIndex : exports.getToIndices())
						expectedTypeMasks.put(moduleIndex, i -> i == ConstantPool.MODULE);
				}
				for (Opens opens : moduleAttribute.getOpens()) {
					expectedTypeMasks.put(opens.getIndex(), i -> i == ConstantPool.PACKAGE);
					for (int moduleIndex : opens.getToIndices())
						expectedTypeMasks.put(moduleIndex, i -> i == ConstantPool.MODULE);
				}
				for (Provides provides : moduleAttribute.getProvides()) {
					expectedTypeMasks.put(provides.getIndex(), i -> i == ConstantPool.CLASS);
					for (int implIndex : provides.getWithIndices())
						expectedTypeMasks.put(implIndex, i -> i == ConstantPool.CLASS);
				}
				for (int use : moduleAttribute.getUses()) {
					expectedTypeMasks.put(use, i -> i == ConstantPool.CLASS);
				}
				break;
			case BOOTSTRAP_METHODS:
				BootstrapMethodsAttribute bootstrapMethodsAttribute = (BootstrapMethodsAttribute) attribute;
				for (BootstrapMethod bsm : bootstrapMethodsAttribute.getBootstrapMethods()) {
					expectedTypeMasks.put(bsm.getBsmMethodref(), i -> i == ConstantPool.METHOD_HANDLE);
					// Arguments must be loadable types
					for (int arg : bsm.getArgs()) {
						expectedTypeMasks.put(arg, i ->
								(i >= ConstantPool.INTEGER && i <= ConstantPool.STRING) ||
										(i >= ConstantPool.METHOD_HANDLE && i <= ConstantPool.DYNAMIC));
					}
				}
				break;
			case LOCAL_VARIABLE_TABLE:
				LocalVariableTableAttribute varTable = (LocalVariableTableAttribute) attribute;
				for (VarEntry entry : varTable.getEntries()) {
					expectedTypeMasks.put(entry.getNameIndex(), i -> i == ConstantPool.UTF8);
					expectedTypeMasks.put(entry.getDescIndex(), i -> i == ConstantPool.UTF8);
					cpEntryValidators.put(entry.getNameIndex(), matchNonEmptyUtf8().and(matchWordUtf8()));
					cpEntryValidators.put(entry.getDescIndex(), matchNonEmptyUtf8());
				}
				break;
			case LOCAL_VARIABLE_TYPE_TABLE:
				LocalVariableTypeTableAttribute typeTable = (LocalVariableTypeTableAttribute) attribute;
				for (VarTypeEntry entry : typeTable.getEntries()) {
					expectedTypeMasks.put(entry.getNameIndex(), i -> i == ConstantPool.UTF8);
					expectedTypeMasks.put(entry.getSignatureIndex(), i -> i == ConstantPool.UTF8);
					cpEntryValidators.put(entry.getNameIndex(), matchNonEmptyUtf8().and(matchWordUtf8()));
					cpEntryValidators.put(entry.getSignatureIndex(), matchNonEmptyUtf8());
				}
				break;
			case PERMITTED_SUBCLASSES:
				PermittedClassesAttribute permittedClassesAttribute = (PermittedClassesAttribute) attribute;
				for (int index : permittedClassesAttribute.getClasses()) {
					expectedTypeMasks.put(index, i -> i == ConstantPool.CLASS);
					cpEntryValidators.put(index, matchClassType());
				}
				break;
			case RECORD:
				RecordAttribute recordAttribute = (RecordAttribute) attribute;
				for (RecordComponent component : recordAttribute.getComponents()) {
					expectedTypeMasks.put(component.getNameIndex(), i -> i == ConstantPool.UTF8);
					cpEntryValidators.put(component.getNameIndex(), matchWordUtf8());
					expectedTypeMasks.put(component.getDescIndex(), i -> i == ConstantPool.UTF8);
					cpEntryValidators.put(component.getDescIndex(), matchNonEmptyUtf8());
				}
				break;
			case LINE_NUMBER_TABLE:
			case SOURCE_DEBUG_EXTENSION:
			case DEPRECATED:
			case SYNTHETIC:
				// no-op
				break;
			case CHARACTER_RANGE_TABLE:
			case COMPILATION_ID:
			case METHOD_PARAMETERS:
			case MODULE_HASHES:
			case MODULE_MAIN_CLASS:
			case MODULE_PACKAGES:
			case MODULE_RESOLUTION:
			case MODULE_TARGET:
			case SOURCE_ID:
			case STACK_MAP_TABLE:
			default:
				// TODO: The rest of these when each has their own attribute class
				break;
		}
		int min = allow0Case ? 0 : 1;
		for (Map.Entry<Integer, Predicate<Integer>> entry : expectedTypeMasks.entrySet()) {
			// Referenced pool entry must be in pool range
			//  - Yes, the CP doesn't start at 0, but there are special cases where it is allowed.
			int cpIndex = entry.getKey();
			if (cpIndex < min || cpIndex > maxCpIndex) {
				logger.debug("Invalid '{}' attribute on {}, contains CP reference to index out of CP bounds!",
						name, context.name());
				return false;
			}
			// Referenced entry must match type
			if (allow0Case && cpIndex == 0)
				continue; // skip edge case
			// cpEntryValidators
			ConstPoolEntry cpEntry = pool.get(cpIndex);
			if (cpEntry == null) {
				logger.debug("No CP entry at index '{}' in Attribute '{}' on {}", cpIndex, name, context.name());
				return false;
			}
			int tag = cpEntry.getTag();
			if (!entry.getValue().test(tag)) {
				logger.debug("Invalid '{}' attribute on {}, contains CP reference to index with wrong type!",
						name, context.name());
				return false;
			}
			if (cpEntryValidators.containsKey(cpIndex) && !cpEntryValidators.get(cpIndex).test(cpEntry)) {
				logger.debug("Invalid '{}' attribute," +
						" contains CP reference to item that does not match criteria at index: {}", name, cpIndex);
				return false;
			}
		}
		return true;
	}

	private void addAnnotationValidation(AttributeHolder holder,
										 Map<Integer, Predicate<Integer>> expectedTypeMasks,
										 Map<Integer, Predicate<ConstPoolEntry>> cpEntryValidators,
										 Annotation anno) {
		expectedTypeMasks.put(anno.getTypeIndex(), i -> i == ConstantPool.UTF8);
		cpEntryValidators.put(anno.getTypeIndex(), matchUtf8ClassType());
		for (Map.Entry<Integer, ElementValue> entry : anno.getValues().entrySet()) {
			int elementTypeIndex = entry.getKey();
			expectedTypeMasks.put(elementTypeIndex, i -> i == ConstantPool.UTF8);
			cpEntryValidators.put(elementTypeIndex, matchUtf8ClassType());
			addElementValueValidation(holder, expectedTypeMasks, cpEntryValidators, entry.getValue());
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
					// TODO: relies on instructions being parsed
					break;
				case SUPERTYPE_TARGET:
					if (holder instanceof ClassFile) {
						SuperTypeTargetInfo superTypeTargetInfo = (SuperTypeTargetInfo) targetInfo;
						if (!superTypeTargetInfo.isExtends()) {
							ClassFile classFile = (ClassFile) holder;
							// Enforce interfaces range
							if (superTypeTargetInfo.getSuperTypeIndex() >= classFile.getInterfaceIndices().size()) {
								expectedTypeMasks.put(FORCE_FAIL, i -> false);
							}
						}
					} else {
						// Illegal target kind for situation
						expectedTypeMasks.put(FORCE_FAIL, i -> false);
					}
					break;

				case CATCH_TARGET:
					if (holder instanceof CodeAttribute) {
						CodeAttribute code = (CodeAttribute) holder;
						CatchTargetInfo catchTargetInfo = (CatchTargetInfo) targetInfo;
						// Enforce table range
						if (catchTargetInfo.getExceptionTableIndex() >= code.getExceptionTable().size()) {
							expectedTypeMasks.put(FORCE_FAIL, i -> false);
						}
					} else {
						// Illegal target kind for situation
						expectedTypeMasks.put(FORCE_FAIL, i -> false);
					}
					break;
				case EMPTY_TARGET:
				default:
					// no-op
					break;
			}
		}
	}

	private void addElementValueValidation(AttributeHolder holder,
										   Map<Integer, Predicate<Integer>> expectedTypeMasks,
										   Map<Integer, Predicate<ConstPoolEntry>> cpEntryValidators,
										   ElementValue elementValue) {
		if (elementValue instanceof ClassElementValue) {
			int classIndex = ((ClassElementValue) elementValue).getClassIndex();
			expectedTypeMasks.put(classIndex, i -> i == ConstantPool.UTF8);
			cpEntryValidators.put(classIndex, matchUtf8ClassType());
		} else if (elementValue instanceof EnumElementValue) {
			EnumElementValue enumElementValue = (EnumElementValue) elementValue;
			expectedTypeMasks.put(enumElementValue.getNameIndex(), i -> i == ConstantPool.UTF8);
			expectedTypeMasks.put(enumElementValue.getTypeIndex(), i -> i == ConstantPool.UTF8);
			cpEntryValidators.put(enumElementValue.getTypeIndex(), matchUtf8ClassType());
		} else if (elementValue instanceof Utf8ElementValue) {
			int utfIndex = ((Utf8ElementValue) elementValue).getUtfIndex();
			expectedTypeMasks.put(utfIndex, i -> i == ConstantPool.UTF8);
		} else if (elementValue instanceof PrimitiveElementValue) {
			int primValueIndex = ((PrimitiveElementValue) elementValue).getValueIndex();
			expectedTypeMasks.put(primValueIndex, i -> (i >= ConstantPool.INTEGER && i <= ConstantPool.DOUBLE));
		}
	}

	private Predicate<ConstPoolEntry> matchClassType() {
		return e -> e instanceof CpClass && matchUtf8ClassType().test(pool.get(((CpClass) e).getIndex()));
	}

	private Predicate<ConstPoolEntry> matchUtf8ClassType() {
		return matchNonEmptyUtf8();
	}

	private Predicate<ConstPoolEntry> matchNonEmptyUtf8() {
		return e -> e instanceof CpUtf8 && ((CpUtf8) e).getText().length() > 0;
	}

	private Predicate<ConstPoolEntry> matchWordUtf8() {
		return e -> e instanceof CpUtf8 && ((CpUtf8) e).getText().matches("[<>;/$\\w]+");
	}
}
