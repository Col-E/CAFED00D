package me.coley.cafedude.io;

import me.coley.cafedude.classfile.AttributeConstants;
import me.coley.cafedude.classfile.attribute.*;
import me.coley.cafedude.classfile.attribute.BootstrapMethodsAttribute.BootstrapMethod;
import me.coley.cafedude.classfile.attribute.InnerClassesAttribute.InnerClass;
import me.coley.cafedude.classfile.attribute.LineNumberTableAttribute.LineEntry;
import me.coley.cafedude.classfile.attribute.LocalVariableTableAttribute.VarEntry;
import me.coley.cafedude.classfile.attribute.LocalVariableTypeTableAttribute.VarTypeEntry;
import me.coley.cafedude.classfile.attribute.ModuleAttribute.Exports;
import me.coley.cafedude.classfile.attribute.ModuleAttribute.Opens;
import me.coley.cafedude.classfile.attribute.ModuleAttribute.Provides;
import me.coley.cafedude.classfile.attribute.ModuleAttribute.Requires;
import me.coley.cafedude.classfile.attribute.RecordAttribute.RecordComponent;
import me.coley.cafedude.classfile.attribute.StackMapTableAttribute.StackMapFrame;
import me.coley.cafedude.classfile.attribute.StackMapTableAttribute.TypeInfo;
import me.coley.cafedude.classfile.constant.CpClass;
import me.coley.cafedude.classfile.constant.CpEntry;
import me.coley.cafedude.classfile.constant.CpModule;
import me.coley.cafedude.classfile.constant.CpUtf8;

import javax.annotation.Nullable;
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
	private final ClassFileWriter writer;

	/**
	 * @param writer
	 * 		Parent class writier.
	 */
	public AttributeWriter(ClassFileWriter writer) {
		this.writer = writer;
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
	 */
	public byte[] writeAttribute(Attribute attribute) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(baos);
		if (attribute instanceof DefaultAttribute) {
			DefaultAttribute dflt = (DefaultAttribute) attribute;
			out.writeShort(dflt.getName().getIndex());
			out.writeInt(dflt.getData().length);
			out.write(dflt.getData());
		} else {
			CpUtf8 cpName = attribute.getName();

			// Write common attribute bits
			out.writeShort(cpName.getIndex());
			out.writeInt(attribute.computeInternalLength());

			// Write specific bits.
			// Note: Unlike reading, writing is quite streamline and doesn't require many variable declarations
			//   so I don't think its super necessary to break these into separate methods.
			String attrName = cpName.getText();
			switch (attrName) {
				case AttributeConstants.BOOTSTRAP_METHODS:
					BootstrapMethodsAttribute bsms = (BootstrapMethodsAttribute) attribute;
					out.writeShort(bsms.getBootstrapMethods().size());
					for (BootstrapMethod bsm : bsms.getBootstrapMethods()) {
						out.writeShort(bsm.getBsmMethodref().getIndex());
						out.writeShort(bsm.getArgs().size());
						for (CpEntry arg : bsm.getArgs()) {
							out.writeShort(arg.getIndex());
						}
					}
					break;
				case AttributeConstants.CODE:
					CodeAttribute code = (CodeAttribute) attribute;
					out.writeShort(code.getMaxStack());
					out.writeShort(code.getMaxLocals());
					InstructionWriter instructionWriter = new InstructionWriter(writer.fallbackWriterSupplier.get());
					byte[] codeBytes = instructionWriter.writeCode(code.getInstructions());
					out.writeInt(codeBytes.length);
					out.write(codeBytes);
					out.writeShort(code.getExceptionTable().size());
					for (CodeAttribute.ExceptionTableEntry tableEntry : code.getExceptionTable()) {
						out.writeShort(tableEntry.getStartPc());
						out.writeShort(tableEntry.getEndPc());
						out.writeShort(tableEntry.getHandlerPc());
						out.writeShort(orZero(tableEntry.getCatchType()));
					}
					out.writeShort(code.getAttributes().size());
					for (Attribute subAttribute : code.getAttributes())
						out.write(writeAttribute(subAttribute));
					break;
				case AttributeConstants.CONSTANT_VALUE:
					out.writeShort(((ConstantValueAttribute) attribute).getConstantValue().getIndex());
					break;
				case AttributeConstants.ENCLOSING_METHOD:
					EnclosingMethodAttribute enclosingMethodAttribute = (EnclosingMethodAttribute) attribute;
					out.writeShort(enclosingMethodAttribute.getClassEntry().getIndex());
					out.writeShort(orZero(enclosingMethodAttribute.getMethodEntry()));
					break;
				case AttributeConstants.EXCEPTIONS:
					ExceptionsAttribute exceptionsAttribute = (ExceptionsAttribute) attribute;
					out.writeShort(exceptionsAttribute.getExceptionTable().size());
					for (CpClass index : exceptionsAttribute.getExceptionTable()) {
						out.writeShort(index.getIndex());
					}
					break;
				case AttributeConstants.INNER_CLASSES:
					InnerClassesAttribute innerClassesAttribute = (InnerClassesAttribute) attribute;
					out.writeShort(innerClassesAttribute.getInnerClasses().size());
					for (InnerClass ic : innerClassesAttribute.getInnerClasses()) {
						out.writeShort(ic.getInnerClassInfo().getIndex());
						out.writeShort(orZero(ic.getOuterClassInfo()));
						out.writeShort(orZero(ic.getInnerName()));
						out.writeShort(ic.getInnerClassAccessFlags());
					}
					break;
				case AttributeConstants.LINE_NUMBER_TABLE:
					LineNumberTableAttribute lineNumbers = (LineNumberTableAttribute) attribute;
					out.writeShort(lineNumbers.getEntries().size());
					for (LineEntry entry : lineNumbers.getEntries()) {
						out.writeShort(entry.getStartPc());
						out.writeShort(entry.getLine());
					}
					break;
				case AttributeConstants.LOCAL_VARIABLE_TABLE:
					LocalVariableTableAttribute varTable = (LocalVariableTableAttribute) attribute;
					out.writeShort(varTable.getEntries().size());
					for (VarEntry entry : varTable.getEntries()) {
						out.writeShort(entry.getStartPc());
						out.writeShort(entry.getLength());
						out.writeShort(entry.getName().getIndex());
						out.writeShort(entry.getDesc().getIndex());
						out.writeShort(entry.getIndex());
					}
					break;
				case AttributeConstants.LOCAL_VARIABLE_TYPE_TABLE:
					LocalVariableTypeTableAttribute typeTable = (LocalVariableTypeTableAttribute) attribute;
					out.writeShort(typeTable.getEntries().size());
					for (VarTypeEntry entry : typeTable.getEntries()) {
						out.writeShort(entry.getStartPc());
						out.writeShort(entry.getLength());
						out.writeShort(entry.getName().getIndex());
						out.writeShort(entry.getSignature().getIndex());
						out.writeShort(entry.getIndex());
					}
					break;
				case AttributeConstants.MODULE:
					ModuleAttribute moduleAttribute = (ModuleAttribute) attribute;
					out.writeShort(moduleAttribute.getModule().getIndex());
					out.writeShort(moduleAttribute.getFlags());
					out.writeShort(orZero(moduleAttribute.getVersion()));
					// requires
					out.writeShort(moduleAttribute.getRequires().size());
					for (Requires requires : moduleAttribute.getRequires()) {
						out.writeShort(requires.getModule().getIndex());
						out.writeShort(requires.getFlags());
						out.writeShort(orZero(requires.getVersion()));
					}
					// exports
					out.writeShort(moduleAttribute.getExports().size());
					for (Exports exports : moduleAttribute.getExports()) {
						out.writeShort(exports.getPackageEntry().getIndex());
						out.writeShort(exports.getFlags());
						out.writeShort(exports.getTo().size());
						for (CpModule to : exports.getTo()) {
							out.writeShort(to.getIndex());
						}
					}
					// opens
					out.writeShort(moduleAttribute.getOpens().size());
					for (Opens opens : moduleAttribute.getOpens()) {
						out.writeShort(opens.getPackageEntry().getIndex());
						out.writeShort(opens.getFlags());
						out.writeShort(opens.getTo().size());
						for (CpModule to : opens.getTo()) {
							out.writeShort(to.getIndex());
						}
					}
					// uses
					out.writeShort(moduleAttribute.getUses().size());
					for (CpClass i : moduleAttribute.getUses())
						out.writeShort(i.getIndex());
					// provides
					out.writeShort(moduleAttribute.getProvides().size());
					for (Provides provides : moduleAttribute.getProvides()) {
						out.writeShort(provides.getModule().getIndex());
						out.writeShort(provides.getWith().size());
						for (CpClass i : provides.getWith())
							out.writeShort(i.getIndex());
					}
					break;
				case AttributeConstants.NEST_HOST:
					NestHostAttribute nestHost = (NestHostAttribute) attribute;
					out.writeShort(nestHost.getHostClass().getIndex());
					break;
				case AttributeConstants.NEST_MEMBERS:
					NestMembersAttribute nestMembers = (NestMembersAttribute) attribute;
					out.writeShort(nestMembers.getMemberClasses().size());
					for (CpClass classIndex : nestMembers.getMemberClasses()) {
						out.writeShort(classIndex.getIndex());
					}
					break;
				case AttributeConstants.RECORD:
					RecordAttribute recordAttribute = (RecordAttribute) attribute;
					out.writeShort(recordAttribute.getComponents().size());
					for (RecordComponent component : recordAttribute.getComponents()) {
						out.writeShort(component.getName().getIndex());
						out.writeShort(component.getDesc().getIndex());
						out.writeShort(component.getAttributes().size());
						for (Attribute subAttribute : component.getAttributes())
							out.write(writeAttribute(subAttribute));
					}
					break;
				case AttributeConstants.RUNTIME_VISIBLE_ANNOTATIONS:
				case AttributeConstants.RUNTIME_INVISIBLE_ANNOTATIONS:
					new AnnotationWriter(out).writeAnnotations((AnnotationsAttribute) attribute);
					break;
				case AttributeConstants.RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS:
				case AttributeConstants.RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS:
					new AnnotationWriter(out).writeParameterAnnotations((ParameterAnnotationsAttribute) attribute);
					break;
				case AttributeConstants.RUNTIME_VISIBLE_TYPE_ANNOTATIONS:
				case AttributeConstants.RUNTIME_INVISIBLE_TYPE_ANNOTATIONS:
					new AnnotationWriter(out).writeTypeAnnotations((AnnotationsAttribute) attribute);
					break;
				case AttributeConstants.ANNOTATION_DEFAULT:
					new AnnotationWriter(out).writeAnnotationDefault((AnnotationDefaultAttribute) attribute);
					break;
				case AttributeConstants.PERMITTED_SUBCLASSES:
					PermittedClassesAttribute permittedClasses = (PermittedClassesAttribute) attribute;
					out.writeShort(permittedClasses.getClasses().size());
					for (CpClass classIndex : permittedClasses.getClasses()) {
						out.writeShort(classIndex.getIndex());
					}
					break;
				case AttributeConstants.SIGNATURE:
					SignatureAttribute signatureAttribute = (SignatureAttribute) attribute;
					out.writeShort(signatureAttribute.getSignature().getIndex());
					break;
				case AttributeConstants.SOURCE_DEBUG_EXTENSION:
					SourceDebugExtensionAttribute debugExtension = (SourceDebugExtensionAttribute) attribute;
					out.write(debugExtension.getDebugExtension());
					break;
				case AttributeConstants.SOURCE_FILE:
					SourceFileAttribute sourceFileAttribute = (SourceFileAttribute) attribute;
					out.writeShort(sourceFileAttribute.getSourceFilename().getIndex());
					break;
				case AttributeConstants.STACK_MAP_TABLE:
					StackMapTableAttribute stackMapTable =
							(StackMapTableAttribute) attribute;
					writeStackMapTable(out, stackMapTable);
					break;
				case AttributeConstants.SOURCE_ID:
				case AttributeConstants.MODULE_HASHES:
				case AttributeConstants.MODULE_MAIN_CLASS:
				case AttributeConstants.MODULE_PACKAGES:
				case AttributeConstants.MODULE_RESOLUTION:
				case AttributeConstants.MODULE_TARGET:
				case AttributeConstants.METHOD_PARAMETERS:
				case AttributeConstants.CHARACTER_RANGE_TABLE:
				case AttributeConstants.COMPILATION_ID:
				case AttributeConstants.DEPRECATED:
				case AttributeConstants.SYNTHETIC:
				default:
					break;
			}
		}
		return baos.toByteArray();
	}

	private int orZero(@Nullable CpEntry entry) {
		if (entry == null) return 0;
		return entry.getIndex();
	}

	private void writeVerificationType(DataOutputStream out, StackMapTableAttribute.TypeInfo type) throws IOException {
		out.writeByte(type.getTag());
		if (type instanceof StackMapTableAttribute.ObjectVariableInfo) {
			StackMapTableAttribute.ObjectVariableInfo objVar =
					(StackMapTableAttribute.ObjectVariableInfo) type;
			out.writeShort(objVar.classEntry.getIndex());
		} else if (type instanceof StackMapTableAttribute.UninitializedVariableInfo) {
			StackMapTableAttribute.UninitializedVariableInfo uninitVar =
					(StackMapTableAttribute.UninitializedVariableInfo) type;
			out.writeShort(uninitVar.offset);
		}
	}

	private void writeStackMapTable(DataOutputStream out, StackMapTableAttribute stackMapTable) throws IOException {
		out.writeShort(stackMapTable.getFrames().size());
		for (StackMapFrame frame : stackMapTable.getFrames()) {
			out.writeByte(frame.getFrameType());
			if (frame instanceof StackMapTableAttribute.SameLocalsOneStackItem) {
				StackMapTableAttribute.SameLocalsOneStackItem sameLocals =
						(StackMapTableAttribute.SameLocalsOneStackItem) frame;
				writeVerificationType(out, sameLocals.stack);
			} else if (frame instanceof StackMapTableAttribute.SameLocalsOneStackItemExtended) {
				StackMapTableAttribute.SameLocalsOneStackItemExtended sameLocals =
						(StackMapTableAttribute.SameLocalsOneStackItemExtended) frame;
				out.writeShort(sameLocals.offsetDelta);
				writeVerificationType(out, sameLocals.stack);
			} else if (frame instanceof StackMapTableAttribute.ChopFrame) {
				StackMapTableAttribute.ChopFrame chopFrame =
						(StackMapTableAttribute.ChopFrame) frame;
				out.writeShort(chopFrame.offsetDelta);
			} else if (frame instanceof StackMapTableAttribute.SameFrameExtended) {
				StackMapTableAttribute.SameFrameExtended sameFrame =
						(StackMapTableAttribute.SameFrameExtended) frame;
				out.writeShort(sameFrame.offsetDelta);
			} else if (frame instanceof StackMapTableAttribute.AppendFrame) {
				StackMapTableAttribute.AppendFrame appendFrame =
						(StackMapTableAttribute.AppendFrame) frame;
				out.writeShort(appendFrame.offsetDelta);
				for (TypeInfo type : appendFrame.additionalLocals) {
					writeVerificationType(out, type);
				}
			} else if (frame instanceof StackMapTableAttribute.FullFrame) {
				StackMapTableAttribute.FullFrame fullFrame =
						(StackMapTableAttribute.FullFrame) frame;
				out.writeShort(fullFrame.offsetDelta);
				out.writeShort(fullFrame.locals.size());
				for (TypeInfo type : fullFrame.locals) {
					writeVerificationType(out, type);
				}
				out.writeShort(fullFrame.stack.size());
				for (TypeInfo type : fullFrame.stack) {
					writeVerificationType(out, type);
				}
			}
		}
	}
}
