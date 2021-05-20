package me.coley.cafedude.attribute;

import me.coley.cafedude.ConstPool;
import me.coley.cafedude.Constants.Attributes;
import me.coley.cafedude.Constants.ConstantPool;
import me.coley.cafedude.annotation.Annotation;
import me.coley.cafedude.annotation.ClassElementValue;
import me.coley.cafedude.annotation.ElementValue;
import me.coley.cafedude.annotation.EnumElementValue;
import me.coley.cafedude.annotation.PrimitiveElementValue;
import me.coley.cafedude.annotation.Utf8ElementValue;
import me.coley.cafedude.attribute.CodeAttribute.ExceptionTableEntry;
import me.coley.cafedude.attribute.InnerClassesAttribute.InnerClass;
import me.coley.cafedude.attribute.ModuleAttribute.Exports;
import me.coley.cafedude.attribute.ModuleAttribute.Opens;
import me.coley.cafedude.attribute.ModuleAttribute.Provides;
import me.coley.cafedude.attribute.ModuleAttribute.Requires;
import me.coley.cafedude.constant.ConstPoolEntry;
import me.coley.cafedude.constant.CpClass;
import me.coley.cafedude.constant.CpUtf8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static me.coley.cafedude.Constants.Attributes.*;

/**
 * A class to validate the correctness of constant pool references in attributes.
 *
 * @author Matt Coley
 */
public class AttributeCpAccessValidator {
	private static final Logger logger = LoggerFactory.getLogger(AttributeCpAccessValidator.class);
	private final Map<Integer, Predicate<Integer>> expectedTypeMasks = new HashMap<>();
	private final Map<Integer, Predicate<ConstPoolEntry>> cpEntryValidators = new HashMap<>();
	private final ConstPool pool;

	/**
	 * @param pool
	 * 		CP to pull from.
	 */
	private AttributeCpAccessValidator(ConstPool pool) {
		this.pool = pool;
	}

	/**
	 * Validate the given attribute.
	 *
	 * @param cp
	 * 		The pool context.
	 * @param attribute
	 * 		The attribute to validate.
	 * @param <T>
	 * 		Some attribute type.
	 *
	 * @return {@code true} when all CP references are validated.
	 * {@code false} if the attribute has invalid CP references.
	 */
	public static <T extends Attribute> boolean isValid(ConstPool cp, T attribute) {
		return new AttributeCpAccessValidator(cp).isValid(attribute);
	}

	private <T extends Attribute> boolean isValid(T attribute) {
		// Check name index
		int maxCpIndex = pool.size();
		if (attribute.getNameIndex() > maxCpIndex)
			return false;
		// Check indices match certain types (key=cp_index, value=mask of allowed cp_tags)
		String name = pool.getUtf(attribute.getNameIndex());
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
					addAnnotationValidation(anno);
				break;
			case RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS:
			case RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS:
				ParameterAnnotationsAttribute paramAnnotations = (ParameterAnnotationsAttribute) attribute;
				Collection<List<Annotation>> parameterAnnos = paramAnnotations.getParameterAnnotations().values();
				for (List<Annotation> annotationList : parameterAnnos)
					for (Annotation anno : annotationList)
						addAnnotationValidation(anno);
				break;
			case ANNOTATION_DEFAULT:
				AnnotationDefaultAttribute annotationDefault = (AnnotationDefaultAttribute) attribute;
				ElementValue elementValue = annotationDefault.getElementValue();
				addElementValueValidation(elementValue);
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
					allow0Case |= innerClass.getInnerClassInfoIndex() == 0 || innerClass.getOuterClassInfoIndex() == 0;
				}
				break;
			case CODE:
				CodeAttribute code = (CodeAttribute) attribute;
				for (Attribute subAttr : code.getAttributes()) {
					if (!isValid(subAttr)) {
						logger.info("Attribute '{}' will be removed since a sub-attribute is not legal!", name);
						return false;
					}
				}
				for (ExceptionTableEntry entry : code.getExceptionTable()) {
					expectedTypeMasks.put(entry.getCatchTypeIndex(), i -> i == 0 ||  i == ConstantPool.CLASS);
					if (entry.getCatchTypeIndex() == 0) {
						allow0Case = true;
					} else {
						cpEntryValidators.put(entry.getCatchTypeIndex(), matchClassType());
					}
				}
				break;
			case SIGNATURE:
				SignatureAttribute signatureAttribute = (SignatureAttribute) attribute;
				expectedTypeMasks.put(signatureAttribute.getSignatureIndex(), i -> i == ConstantPool.UTF8);
				cpEntryValidators.put(signatureAttribute.getSignatureIndex(), isNonEmptyUtf8());
				break;
			case SOURCE_FILE:
				SourceFileAttribute sourceFileAttribute = (SourceFileAttribute) attribute;
				expectedTypeMasks.put(sourceFileAttribute.getSourceFileNameIndex(), i -> i == ConstantPool.UTF8);
				cpEntryValidators.put(sourceFileAttribute.getSourceFileNameIndex(), isNonEmptyUtf8());
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
			case SOURCE_DEBUG_EXTENSION:
			case DEPRECATED:
			case SYNTHETIC:
				// no-op
				break;
			case BOOTSTRAP_METHODS:
			case CHARACTER_RANGE_TABLE:
			case COMPILATION_ID:
			case LINE_NUMBER_TABLE:
			case LOCAL_VARIABLE_TABLE:
			case LOCAL_VARIABLE_TYPE_TABLE:
			case METHOD_PARAMETERS:
			case MODULE_HASHES:
			case MODULE_MAIN_CLASS:
			case MODULE_PACKAGES:
			case MODULE_RESOLUTION:
			case MODULE_TARGET:
			case PERMITTED_SUBCLASSES:
			case RECORD:
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
			if (cpIndex < min || cpIndex >= maxCpIndex) {
				logger.debug("Invalid '{}' attribute, contains CP reference to index out of CP bounds!", name);
				return false;
			}
			// Referenced entry must match type
			if (allow0Case && cpIndex == 0)
				continue; // skip edge case
			// cpEntryValidators
			ConstPoolEntry cpEntry = pool.get(cpIndex);
			if (cpEntry == null) {
				logger.debug("No CP entry at index '{}' in Attribute '{}'", cpIndex, name);
				return false;
			}
			int tag = cpEntry.getTag();
			if (!entry.getValue().test(tag)) {
				logger.debug("Invalid '{}' attribute, contains CP reference to index with wrong type!", name);
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

	private void addAnnotationValidation(Annotation anno) {
		expectedTypeMasks.put(anno.getTypeIndex(), i -> i == ConstantPool.UTF8);
		cpEntryValidators.put(anno.getTypeIndex(), matchUtf8ClassType());
		for (Map.Entry<Integer, ElementValue> entry : anno.getValues().entrySet()) {
			int elementTypeIndex = entry.getKey();
			expectedTypeMasks.put(elementTypeIndex, i -> i == ConstantPool.UTF8);
			cpEntryValidators.put(elementTypeIndex, matchUtf8ClassType());
			addElementValueValidation(entry.getValue());
		}
	}

	private void addElementValueValidation(ElementValue elementValue) {
		if (elementValue instanceof ClassElementValue) {
			int classIndex = ((ClassElementValue) elementValue).getClassIndex();
			expectedTypeMasks.put(classIndex, i -> i == ConstantPool.CLASS);
			cpEntryValidators.put(classIndex, matchClassType());
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
		return isNonEmptyUtf8();
	}

	private Predicate<ConstPoolEntry> isNonEmptyUtf8() {
		return e -> e instanceof CpUtf8 && ((CpUtf8) e).getText().length() > 0;
	}
}
