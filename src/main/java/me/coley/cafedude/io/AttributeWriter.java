package me.coley.cafedude.io;

import me.coley.cafedude.ClassFile;
import me.coley.cafedude.Constants;
import me.coley.cafedude.InvalidClassException;
import me.coley.cafedude.attribute.AnnotationDefaultAttribute;
import me.coley.cafedude.attribute.AnnotationsAttribute;
import me.coley.cafedude.attribute.Attribute;
import me.coley.cafedude.attribute.BootstrapMethodsAttribute;
import me.coley.cafedude.attribute.BootstrapMethodsAttribute.BootstrapMethod;
import me.coley.cafedude.attribute.CodeAttribute;
import me.coley.cafedude.attribute.ConstantValueAttribute;
import me.coley.cafedude.attribute.DebugExtensionAttribute;
import me.coley.cafedude.attribute.DefaultAttribute;
import me.coley.cafedude.attribute.EnclosingMethodAttribute;
import me.coley.cafedude.attribute.ExceptionsAttribute;
import me.coley.cafedude.attribute.InnerClassesAttribute;
import me.coley.cafedude.attribute.InnerClassesAttribute.InnerClass;
import me.coley.cafedude.attribute.ModuleAttribute;
import me.coley.cafedude.attribute.ModuleAttribute.Exports;
import me.coley.cafedude.attribute.ModuleAttribute.Opens;
import me.coley.cafedude.attribute.ModuleAttribute.Provides;
import me.coley.cafedude.attribute.ModuleAttribute.Requires;
import me.coley.cafedude.attribute.NestHostAttribute;
import me.coley.cafedude.attribute.NestMembersAttribute;
import me.coley.cafedude.attribute.ParameterAnnotationsAttribute;
import me.coley.cafedude.attribute.SignatureAttribute;
import me.coley.cafedude.attribute.SourceFileAttribute;
import me.coley.cafedude.constant.ConstPoolEntry;
import me.coley.cafedude.constant.CpUtf8;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Attribute writer for all attributes.
 * <br>
 * Annotations delegate to {@link AnnotationWriter} due to complexity.
 *
 * @author Matt Coley
 */
public class AttributeWriter {
	private final ClassFile clazz;

	/**
	 * @param clazz
	 * 		Class to pull info from.
	 */
	public AttributeWriter(ClassFile clazz) {
		this.clazz = clazz;
	}

	/**
	 * Writes the attribute to a {@code byte[]}.
	 *
	 * @param attribute
	 * 		Attribute to write.
	 *
	 * @return Content written.
	 *
	 * @throws IOException
	 * 		When the stream cannot be written to.
	 * @throws InvalidClassException
	 * 		When the class cannot be written.
	 */
	public byte[] writeAttribute(Attribute attribute) throws IOException, InvalidClassException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(baos);
		if (attribute instanceof DefaultAttribute) {
			DefaultAttribute dflt = (DefaultAttribute) attribute;
			out.writeShort(dflt.getNameIndex());
			out.writeInt(dflt.getData().length);
			out.write(dflt.getData());
		} else {
			ConstPoolEntry cpName = clazz.getCp(attribute.getNameIndex());
			if (!(cpName instanceof CpUtf8))
				throw new InvalidClassException("Attribute name index does not point to CP_UTF8");
			// Write common attribute bits
			out.writeShort(attribute.getNameIndex());
			out.writeInt(attribute.computeInternalLength());
			// Write specific bits.
			// Note: Unlike reading, writing is quite streamline and doesn't require many variable declarations
			//   so I don't think its super necessary to break these into separate methods.
			String attrName = ((CpUtf8) cpName).getText();
			switch (attrName) {
				case Constants.Attributes.BOOTSTRAP_METHODS:
					BootstrapMethodsAttribute bsms = (BootstrapMethodsAttribute) attribute;
					out.writeShort(bsms.getBootstrapMethods().size());
					for (BootstrapMethod bsm : bsms.getBootstrapMethods()) {
						out.writeShort(bsm.getBsmMethodref());
						out.writeShort(bsm.getArgs().size());
						for (int arg : bsm.getArgs()) {
							out.writeShort(arg);
						}
					}
					break;
				case Constants.Attributes.CHARACTER_RANGE_TABLE:
					break;
				case Constants.Attributes.CODE:
					CodeAttribute code = (CodeAttribute) attribute;
					out.writeShort(code.getMaxStack());
					out.writeShort(code.getMaxLocals());
					out.writeInt(code.getCode().length);
					out.write(code.getCode());
					out.writeShort(code.getExceptionTable().size());
					for (CodeAttribute.ExceptionTableEntry tableEntry : code.getExceptionTable()) {
						out.writeShort(tableEntry.getStartPc());
						out.writeShort(tableEntry.getEndPc());
						out.writeShort(tableEntry.getHandlerPc());
						out.writeShort(tableEntry.getCatchTypeIndex());
					}
					out.writeShort(code.getAttributes().size());
					for (Attribute subAttribute : code.getAttributes())
						out.write(writeAttribute(subAttribute));
					break;
				case Constants.Attributes.CONSTANT_VALUE:
					out.writeShort(((ConstantValueAttribute) attribute).getConstantValueIndex());
					break;
				case Constants.Attributes.COMPILATION_ID:
					break;
				case Constants.Attributes.DEPRECATED:
				case Constants.Attributes.SYNTHETIC:
					break;
				case Constants.Attributes.ENCLOSING_METHOD:
					EnclosingMethodAttribute enclosingMethodAttribute = (EnclosingMethodAttribute) attribute;
					out.writeShort(enclosingMethodAttribute.getClassIndex());
					out.writeShort(enclosingMethodAttribute.getMethodIndex());
					break;
				case Constants.Attributes.EXCEPTIONS:
					ExceptionsAttribute exceptionsAttribute = (ExceptionsAttribute) attribute;
					out.writeShort(exceptionsAttribute.getExceptionIndexTable().size());
					for (int index : exceptionsAttribute.getExceptionIndexTable()) {
						out.writeShort(index);
					}
					break;
				case Constants.Attributes.INNER_CLASSES:
					InnerClassesAttribute innerClassesAttribute = (InnerClassesAttribute) attribute;
					out.writeShort(innerClassesAttribute.getInnerClasses().size());
					for (InnerClass ic : innerClassesAttribute.getInnerClasses()) {
						out.writeShort(ic.getInnerClassInfoIndex());
						out.writeShort(ic.getOuterClassInfoIndex());
						out.writeShort(ic.getInnerNameIndex());
						out.writeShort(ic.getInnerClassAccessFlags());
					}
					break;
				case Constants.Attributes.LINE_NUMBER_TABLE:
					break;
				case Constants.Attributes.LOCAL_VARIABLE_TABLE:
					break;
				case Constants.Attributes.LOCAL_VARIABLE_TYPE_TABLE:
					break;
				case Constants.Attributes.METHOD_PARAMETERS:
					break;
				case Constants.Attributes.MODULE:
					ModuleAttribute moduleAttribute = (ModuleAttribute) attribute;
					out.writeShort(moduleAttribute.getModuleIndex());
					out.writeShort(moduleAttribute.getFlags());
					out.writeShort(moduleAttribute.getVersionIndex());
					// requires
					out.writeShort(moduleAttribute.getRequires().size());
					for (Requires requires : moduleAttribute.getRequires()) {
						out.writeShort(requires.getIndex());
						out.writeShort(requires.getFlags());
						out.writeShort(requires.getVersionIndex());
					}
					// exports
					out.writeShort(moduleAttribute.getExports().size());
					for (Exports exports : moduleAttribute.getExports()) {
						out.writeShort(exports.getIndex());
						out.writeShort(exports.getFlags());
						out.writeShort(exports.getToIndices().size());
						for (int i : exports.getToIndices())
							out.writeShort(i);
					}
					// opens
					out.writeShort(moduleAttribute.getOpens().size());
					for (Opens opens : moduleAttribute.getOpens()) {
						out.writeShort(opens.getIndex());
						out.writeShort(opens.getFlags());
						out.writeShort(opens.getToIndices().size());
						for (int i : opens.getToIndices())
							out.writeShort(i);
					}
					// uses
					out.writeShort(moduleAttribute.getUses().size());
					for (int i : moduleAttribute.getUses())
						out.writeShort(i);
					// provides
					out.writeShort(moduleAttribute.getProvides().size());
					for (Provides provides : moduleAttribute.getProvides()) {
						out.writeShort(provides.getIndex());
						out.writeShort(provides.getWithIndices().size());
						for (int i : provides.getWithIndices())
							out.writeShort(i);
					}
					break;
				case Constants.Attributes.MODULE_HASHES:
					break;
				case Constants.Attributes.MODULE_MAIN_CLASS:
					break;
				case Constants.Attributes.MODULE_PACKAGES:
					break;
				case Constants.Attributes.MODULE_RESOLUTION:
					break;
				case Constants.Attributes.MODULE_TARGET:
					break;
				case Constants.Attributes.NEST_HOST:
					NestHostAttribute nestHost = (NestHostAttribute) attribute;
					out.writeShort(nestHost.getHostClassIndex());
					break;
				case Constants.Attributes.NEST_MEMBERS:
					NestMembersAttribute nestMembers = (NestMembersAttribute) attribute;
					out.writeShort(nestMembers.getMemberClassIndices().size());
					for (int classIndex : nestMembers.getMemberClassIndices()) {
						out.writeShort(classIndex);
					}
					break;
				case Constants.Attributes.RECORD:
					break;
				case Constants.Attributes.RUNTIME_VISIBLE_ANNOTATIONS:
				case Constants.Attributes.RUNTIME_INVISIBLE_ANNOTATIONS:
					new AnnotationWriter(out).writeAnnotations((AnnotationsAttribute) attribute);
					break;
				case Constants.Attributes.RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS:
				case Constants.Attributes.RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS:
					new AnnotationWriter(out).writeParameterAnnotations((ParameterAnnotationsAttribute) attribute);
					break;
				case Constants.Attributes.RUNTIME_VISIBLE_TYPE_ANNOTATIONS:
				case Constants.Attributes.RUNTIME_INVISIBLE_TYPE_ANNOTATIONS:
					new AnnotationWriter(out).writeTypeAnnotations((AnnotationsAttribute) attribute);
					break;
				case Constants.Attributes.ANNOTATION_DEFAULT:
					new AnnotationWriter(out).writeAnnotationDefault((AnnotationDefaultAttribute) attribute);
					break;
				case Constants.Attributes.PERMITTED_SUBCLASSES:
					break;
				case Constants.Attributes.SIGNATURE:
					SignatureAttribute signatureAttribute = (SignatureAttribute) attribute;
					out.writeShort(signatureAttribute.getSignatureIndex());
					break;
				case Constants.Attributes.SOURCE_DEBUG_EXTENSION:
					DebugExtensionAttribute debugExtension = (DebugExtensionAttribute) attribute;
					out.writeShort(debugExtension.getNameIndex());
					out.writeInt(debugExtension.getDebugExtension().length);
					out.write(debugExtension.getDebugExtension());
					break;
				case Constants.Attributes.SOURCE_FILE:
					SourceFileAttribute sourceFileAttribute = (SourceFileAttribute) attribute;
					out.writeShort(sourceFileAttribute.getSourceFileNameIndex());
					break;
				case Constants.Attributes.SOURCE_ID:
					break;
				case Constants.Attributes.STACK_MAP_TABLE:
					break;
				default:
					break;
			}
		}
		return baos.toByteArray();
	}
}
