package software.coley.cafedude.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.coley.cafedude.classfile.ConstPool;
import software.coley.cafedude.classfile.InvalidCpIndexException;
import software.coley.cafedude.classfile.attribute.*;
import software.coley.cafedude.classfile.attribute.BootstrapMethodsAttribute.BootstrapMethod;
import software.coley.cafedude.classfile.attribute.CodeAttribute.ExceptionTableEntry;
import software.coley.cafedude.classfile.attribute.InnerClassesAttribute.InnerClass;
import software.coley.cafedude.classfile.attribute.LineNumberTableAttribute.LineEntry;
import software.coley.cafedude.classfile.attribute.LocalVariableTableAttribute.VarEntry;
import software.coley.cafedude.classfile.attribute.LocalVariableTypeTableAttribute.VarTypeEntry;
import software.coley.cafedude.classfile.attribute.ModuleAttribute.Exports;
import software.coley.cafedude.classfile.attribute.ModuleAttribute.Opens;
import software.coley.cafedude.classfile.attribute.ModuleAttribute.Provides;
import software.coley.cafedude.classfile.attribute.ModuleAttribute.Requires;
import software.coley.cafedude.classfile.constant.CpClass;
import software.coley.cafedude.classfile.constant.CpEntry;
import software.coley.cafedude.classfile.constant.CpMethodHandle;
import software.coley.cafedude.classfile.constant.CpModule;
import software.coley.cafedude.classfile.constant.CpNameType;
import software.coley.cafedude.classfile.constant.CpPackage;
import software.coley.cafedude.classfile.constant.CpUtf8;
import software.coley.cafedude.classfile.instruction.Instruction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Attribute reader for all attributes.
 * <br>
 * Annotations delegate to {@link AnnotationReader} due to complexity.
 *
 * @author Matt Coley
 */
public class AttributeReader {
	private static final Logger logger = LoggerFactory.getLogger(AttributeReader.class);
	private final IndexableByteStream is;
	private final ClassFileReader reader;
	private final ClassBuilder builder;
	private final ConstPool cp;
	// Attribute info
	private final int expectedContentLength;
	private final CpUtf8 name;

	/**
	 * @param reader
	 * 		Parent class reader.
	 * @param builder
	 * 		Class being build/read into.
	 * @param is
	 * 		Parent stream.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	private AttributeReader(@Nonnull ClassFileReader reader, @Nonnull ClassBuilder builder,
	                        @Nonnull DataInputStream is) throws IOException {
		this.reader = reader;
		this.builder = builder;
		this.cp = builder.getPool();

		// Extract name/length
		this.name = (CpUtf8) cp.get(is.readUnsignedShort());
		this.expectedContentLength = is.readInt();

		// Create local stream
		byte[] subsection = new byte[expectedContentLength];
		is.readFully(subsection);
		this.is = new IndexableByteStream(subsection);
	}

	/**
	 * @param reader
	 * 		Parent class reader.
	 * @param builder
	 * 		Class being build/read into.
	 * @param is
	 * 		Parent stream.
	 * @param context
	 * 		Where the attribute is applied to.
	 *
	 * @return Read attribute, or {@code null} if it could not be parsed.
	 */
	@Nullable
	public static Attribute readFromClass(@Nonnull ClassFileReader reader, @Nonnull ClassBuilder builder,
	                                      @Nonnull DataInputStream is, @Nonnull AttributeContext context) {
		try {
			return new AttributeReader(reader, builder, is).read(context);
		} catch (Exception ex) {
			String message = ex.getMessage();
			logger.debug("Dropping attribute on {}: {}", context.name(), message);
			return null;
		}
	}

	/**
	 * @param context
	 * 		Where the attribute is applied to.
	 *
	 * @return Attribute.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	@Nullable
	public Attribute readAttribute(@Nonnull AttributeContext context) throws IOException {
		try {
			Attribute attribute = read(context);
			if (attribute == null)
				return null;
			int read = is.getIndex();
			if (read != expectedContentLength) {
				logger.debug("Invalid '{}' on {}, claimed to be {} bytes, but was {}",
						name.getText(), context.name(), expectedContentLength, read);
				return null;
			}
			return attribute;
		} catch (InvalidCpIndexException cpIndexException) {
			if (name != null) {
				logger.debug("Invalid '{}' on {}, invalid constant pool index: {}", name.getText(), context.name(),
						cpIndexException.getIndex());
			} else {
				logger.debug("Invalid attribute on {}, invalid attribute name index", context.name());
			}

			return null;
		} catch (Exception ex) {
			if (reader.doDropEofAttributes()) {
				if (name != null) {
					logger.debug("Invalid '{}' on {}, EOF thrown when parsing attribute, expected {} bytes",
							name.getText(), context.name(), expectedContentLength);
				} else {
					logger.debug("Invalid attribute on {}, invalid attribute name index", context.name());
				}

				return null;
			} else
				throw ex;
		}
	}

	@Nullable
	private Attribute read(@Nonnull AttributeContext context) throws IOException {
		// Check for illegally inserted attributes from future versions
		String attributeName = name.getText();
		if (reader.doDropForwardVersioned()) {
			int introducedAt = AttributeVersions.getIntroducedVersion(attributeName);
			if (introducedAt > builder.getVersionMajor()) {
				logger.debug("Found '{}' on {} in class version {}, min supported is {}",
						attributeName, context.name(), builder.getVersionMajor(), introducedAt);
				return null;
			}
		}
		// Check for attributes present in the wrong contexts.
		if (reader.doDropBadContextAttributes()) {
			Collection<AttributeContext> allowedContexts = AttributeContexts.getAllowedContexts(attributeName);
			if (!allowedContexts.contains(context)) {
				logger.debug("Found '{}' in invalid context {}", attributeName, context.name());
				return null;
			} else if (!builder.isModule() && attributeName.toLowerCase().startsWith("module")) {
				logger.debug("Found '{}' in non-module class", attributeName);
				return null;
			}
		}
		switch (attributeName) {
			case AttributeConstants.CODE:
				return readCode();
			case AttributeConstants.CONSTANT_VALUE:
				return readConstantValue();
			case AttributeConstants.DEPRECATED:
				return new DeprecatedAttribute(name);
			case AttributeConstants.ENCLOSING_METHOD:
				return readEnclosingMethod();
			case AttributeConstants.EXCEPTIONS:
				return readExceptions();
			case AttributeConstants.INNER_CLASSES:
				return readInnerClasses();
			case AttributeConstants.NEST_HOST:
				return readNestHost();
			case AttributeConstants.NEST_MEMBERS:
				return readNestMembers();
			case AttributeConstants.SOURCE_DEBUG_EXTENSION:
				return readSourceDebugExtension();
			case AttributeConstants.RUNTIME_INVISIBLE_ANNOTATIONS:
				return readAnnotations(context, false);
			case AttributeConstants.RUNTIME_VISIBLE_ANNOTATIONS:
				return readAnnotations(context, true);
			case AttributeConstants.RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS:
				return readParameterAnnotations(context, false);
			case AttributeConstants.RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS:
				return readParameterAnnotations(context, true);
			case AttributeConstants.RUNTIME_INVISIBLE_TYPE_ANNOTATIONS:
				return readTypeAnnotations(context, false);
			case AttributeConstants.RUNTIME_VISIBLE_TYPE_ANNOTATIONS:
				return readTypeAnnotations(context, true);
			case AttributeConstants.ANNOTATION_DEFAULT:
				return readAnnotationDefault(context);
			case AttributeConstants.SYNTHETIC:
				return readSynthetic();
			case AttributeConstants.BOOTSTRAP_METHODS:
				return readBoostrapMethods();
			case AttributeConstants.SIGNATURE:
				return readSignature();
			case AttributeConstants.SOURCE_FILE:
				return readSourceFile();
			case AttributeConstants.METHOD_PARAMETERS:
				return readMethodParameters();
			case AttributeConstants.MODULE:
				return readModule();
			case AttributeConstants.MODULE_MAIN_CLASS:
				return readModuleMainClass();
			case AttributeConstants.MODULE_PACKAGES:
				return readModulePackages();
			case AttributeConstants.MODULE_TARGET:
				return readModuleTarget();
			case AttributeConstants.MODULE_HASHES:
				return readModuleHashes();
			case AttributeConstants.STACK_MAP_TABLE:
				return readStackMapTable();
			case AttributeConstants.LINE_NUMBER_TABLE:
				return readLineNumbers();
			case AttributeConstants.LOCAL_VARIABLE_TABLE:
				return readLocalVariables();
			case AttributeConstants.LOCAL_VARIABLE_TYPE_TABLE:
				return readLocalVariableTypes();
			case AttributeConstants.PERMITTED_SUBCLASSES:
				return readPermittedClasses();
			case AttributeConstants.RECORD:
				return readRecord();
			case AttributeConstants.CHARACTER_RANGE_TABLE:
			case AttributeConstants.COMPILATION_ID:
			case AttributeConstants.MODULE_RESOLUTION:
			case AttributeConstants.SOURCE_ID:
			default:
				break;
		}
		// No known/unhandled attribute length is less than 2.
		// So if that is given, we likely have an intentionally malformed attribute.
		if (expectedContentLength < 2) {
			logger.debug("Invalid attribute, its content length <= 1");
			is.skipBytes(expectedContentLength);
			return null;
		}
		// Default handling, skip remaining bytes
		is.skipBytes(expectedContentLength);
		return new DefaultAttribute(name, is.getBuffer());
	}

	/**
	 * @return Record attribute indicating the current class is a record, and details components of the record.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	@Nonnull
	private RecordAttribute readRecord() throws IOException {
		List<RecordAttribute.RecordComponent> components = new ArrayList<>();
		int count = is.readUnsignedShort();
		for (int i = 0; i < count; i++) {
			CpUtf8 name = (CpUtf8) cp.get(is.readUnsignedShort());
			CpUtf8 descriptor = (CpUtf8) cp.get(is.readUnsignedShort());
			int numAttributes = is.readUnsignedShort();
			List<Attribute> attributes = new ArrayList<>();
			for (int x = 0; x < numAttributes; x++) {
				Attribute attr = new AttributeReader(reader, builder, is).readAttribute(AttributeContext.ATTRIBUTE);
				if (attr != null)
					attributes.add(attr);
			}
			components.add(new RecordAttribute.RecordComponent(name, descriptor, attributes));
		}
		return new RecordAttribute(name, components);
	}

	/**
	 * @return Permitted classes authorized to extend/implement the current class.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	@Nonnull
	private PermittedClassesAttribute readPermittedClasses() throws IOException {
		List<CpClass> entries = new ArrayList<>();
		int count = is.readUnsignedShort();
		for (int i = 0; i < count; i++) {
			CpClass entry = (CpClass) cp.get(is.readUnsignedShort());
			entries.add(entry);
		}
		return new PermittedClassesAttribute(name, entries);
	}

	/**
	 * @return Variable type table.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	@Nonnull
	private LocalVariableTypeTableAttribute readLocalVariableTypes() throws IOException {
		List<VarTypeEntry> entries = new ArrayList<>();
		int count = is.readUnsignedShort();
		for (int i = 0; i < count; i++) {
			int startPc = is.readUnsignedShort();
			int length = is.readUnsignedShort();
			CpUtf8 name = (CpUtf8) cp.get(is.readUnsignedShort());
			CpUtf8 sig = (CpUtf8) cp.get(is.readUnsignedShort());
			int index = is.readUnsignedShort();
			entries.add(new VarTypeEntry(startPc, length, name, sig, index));
		}
		return new LocalVariableTypeTableAttribute(name, entries);
	}

	/**
	 * @return Variable table.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	@Nonnull
	private LocalVariableTableAttribute readLocalVariables() throws IOException {
		List<VarEntry> entries = new ArrayList<>();
		int count = is.readUnsignedShort();
		for (int i = 0; i < count; i++) {
			int startPc = is.readUnsignedShort();
			int length = is.readUnsignedShort();
			CpUtf8 name = (CpUtf8) cp.get(is.readUnsignedShort());
			CpUtf8 desc = (CpUtf8) cp.get(is.readUnsignedShort());
			int index = is.readUnsignedShort();
			entries.add(new VarEntry(startPc, length, name, desc, index));
		}
		return new LocalVariableTableAttribute(name, entries);
	}

	/**
	 * @return Line number table.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	@Nonnull
	private LineNumberTableAttribute readLineNumbers() throws IOException {
		List<LineEntry> entries = new ArrayList<>();
		int count = is.readUnsignedShort();
		for (int i = 0; i < count; i++) {
			int offset = is.readUnsignedShort();
			int line = is.readUnsignedShort();
			entries.add(new LineEntry(offset, line));
		}
		return new LineNumberTableAttribute(name, entries);
	}

	/**
	 * @return MethodParametersAttribute attribute.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	@Nonnull
	private MethodParametersAttribute readMethodParameters() throws IOException {
		List<MethodParametersAttribute.Parameter> entries = new ArrayList<>();
		int count = is.readUnsignedByte();
		for (int i = 0; i < count; i++) {
			CpUtf8 name = orNullInCp(is.readUnsignedShort());
			int accessFlags = is.readUnsignedShort();
			entries.add(new MethodParametersAttribute.Parameter(accessFlags, name));
		}
		return new MethodParametersAttribute(name, entries);
	}

	/**
	 * @return ModuleAttribute attribute.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	@Nullable
	private ModuleAttribute readModule() throws IOException {
		CpModule module = (CpModule) cp.get(is.readUnsignedShort());
		if (module == null)
			return null;
		int flags = is.readUnsignedShort();
		CpUtf8 version = orNullInCp(is.readUnsignedShort());
		List<Requires> requires = new ArrayList<>();
		int count = is.readUnsignedShort();
		for (int i = 0; i < count; i++) {
			CpModule reqModule = (CpModule) cp.get(is.readUnsignedShort());
			if (reqModule != null) {
				int reqFlags = is.readUnsignedShort();
				CpUtf8 reqVersion = orNullInCp(is.readUnsignedShort());
				requires.add(new Requires(reqModule, reqFlags, reqVersion));
			}
		}
		List<Exports> exports = new ArrayList<>();
		count = is.readUnsignedShort();
		for (int i = 0; i < count; i++) {
			CpPackage expPackage = (CpPackage) cp.get(is.readUnsignedShort());
			if (expPackage != null) {
				int expFlags = is.readUnsignedShort();
				int expCount = is.readUnsignedShort();
				List<CpModule> expModules = new ArrayList<>();
				for (int j = 0; j < expCount; j++) {
					CpModule expModule = (CpModule) cp.get(is.readUnsignedShort());
					expModules.add(expModule);
				}
				exports.add(new Exports(expPackage, expFlags, expModules));
			}
		}
		List<Opens> opens = new ArrayList<>();
		count = is.readUnsignedShort();
		for (int i = 0; i < count; i++) {
			CpPackage openPackage = (CpPackage) cp.get(is.readUnsignedShort());
			if (openPackage != null) {
				int openFlags = is.readUnsignedShort();
				int openCount = is.readUnsignedShort();
				List<CpModule> openModules = new ArrayList<>();
				for (int j = 0; j < openCount; j++) {
					CpModule openModule = (CpModule) cp.get(is.readUnsignedShort());
					openModules.add(openModule);
				}
				opens.add(new Opens(openPackage, openFlags, openModules));
			}
		}
		List<CpClass> uses = new ArrayList<>();
		count = is.readUnsignedShort();
		for (int i = 0; i < count; i++) {
			CpClass useClass = (CpClass) cp.get(is.readUnsignedShort());
			uses.add(useClass);
		}
		List<Provides> provides = new ArrayList<>();
		count = is.readUnsignedShort();
		for (int i = 0; i < count; i++) {
			CpClass service = (CpClass) cp.get(is.readUnsignedShort());
			if (service != null) {
				int prvCount = is.readUnsignedShort();
				List<CpClass> providers = new ArrayList<>();
				for (int j = 0; j < prvCount; j++) {
					CpClass provider = (CpClass) cp.get(is.readUnsignedShort());
					providers.add(provider);
				}
				provides.add(new Provides(service, providers));
			}
		}
		return new ModuleAttribute(name, module, flags, version,
				requires, exports, opens, uses, provides);
	}

	/**
	 * @return ModuleMainClassAttribute attribute.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	@Nonnull
	private ModuleMainClassAttribute readModuleMainClass() throws IOException {
		return new ModuleMainClassAttribute(name, (CpClass) cp.get(is.readUnsignedShort()));
	}

	/**
	 * @return ModulePackagesAttribute attribute.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	@Nonnull
	private ModulePackagesAttribute readModulePackages() throws IOException {
		List<CpPackage> packages = new ArrayList<>();
		int count = is.readUnsignedShort();
		for (int i = 0; i < count; i++) {
			packages.add((CpPackage) cp.get(is.readUnsignedShort()));
		}
		return new ModulePackagesAttribute(name, packages);
	}

	/**
	 * @return Module target attribute.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	@Nonnull
	private ModuleTargetAttribute readModuleTarget() throws IOException {
		CpUtf8 platformName = (CpUtf8) cp.get(is.readUnsignedShort());
		return new ModuleTargetAttribute(name, platformName);
	}

	/**
	 * @return Module hashes attribute.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	@Nonnull
	private ModuleHashesAttribute readModuleHashes() throws IOException {
		CpUtf8 algorithm = (CpUtf8) cp.get(is.readUnsignedShort());
		int moduleCount = is.readUnsignedShort();
		Map<CpUtf8, byte[]> moduleHashes = new LinkedHashMap<>();
		for (int i = 0; i < moduleCount; i++) {
			int moduleNameIndex = is.readUnsignedShort();
			int moduleHashLength = is.readUnsignedShort();
			CpUtf8 moduleName = (CpUtf8) cp.get(moduleNameIndex);
			byte[] moduleHash = new byte[moduleHashLength];
			is.read(moduleHash);
			moduleHashes.put(moduleName, moduleHash);
		}
		return new ModuleHashesAttribute(name, algorithm, moduleHashes);
	}

	/**
	 * @return Signature attribute.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	@Nonnull
	private SignatureAttribute readSignature() throws IOException {
		CpUtf8 signature = (CpUtf8) cp.get(is.readUnsignedShort());
		return new SignatureAttribute(name, signature);
	}

	/**
	 * @return Source file name attribute.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	@Nonnull
	private SourceFileAttribute readSourceFile() throws IOException {
		CpUtf8 sourceFile = (CpUtf8) cp.get(is.readUnsignedShort());
		return new SourceFileAttribute(name, sourceFile);
	}

	/**
	 * @return Enclosing method attribute.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	@Nonnull
	private EnclosingMethodAttribute readEnclosingMethod() throws IOException {
		CpClass enclosingClass = (CpClass) cp.get(is.readUnsignedShort());
		CpNameType enclosingMethod = orNullInCp(is.readUnsignedShort());
		return new EnclosingMethodAttribute(name, enclosingClass, enclosingMethod);
	}

	/**
	 * @return Exceptions attribute.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	@Nonnull
	private ExceptionsAttribute readExceptions() throws IOException {
		int numberOfExceptionIndices = is.readUnsignedShort();
		List<CpClass> exceptions = new ArrayList<>(numberOfExceptionIndices);
		for (int i = 0; i < numberOfExceptionIndices; i++) {
			exceptions.add((CpClass) cp.get(is.readUnsignedShort()));
		}
		return new ExceptionsAttribute(name, exceptions);
	}

	/**
	 * @return Inner classes attribute.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	@Nonnull
	private InnerClassesAttribute readInnerClasses() throws IOException {
		int numberOfInnerClasses = is.readUnsignedShort();
		List<InnerClass> innerClasses = new ArrayList<>();
		for (int i = 0; i < numberOfInnerClasses; i++) {
			CpClass innerClass = (CpClass) cp.get(is.readUnsignedShort());
			CpClass outerClass = orNullInCp(is.readUnsignedShort());
			CpUtf8 innerName = orNullInCp(is.readUnsignedShort());
			int innerClassAccessFlags = is.readUnsignedShort();
			innerClasses.add(new InnerClass(innerClass, outerClass, innerName, innerClassAccessFlags));
		}
		return new InnerClassesAttribute(name, innerClasses);
	}

	/**
	 * @return Nest host attribute.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	@Nullable
	private NestHostAttribute readNestHost() throws IOException {
		if (expectedContentLength != 2) {
			logger.debug("Found NestHost with illegal content length: {} != 2", expectedContentLength);
			return null;
		}
		CpClass nestHost = (CpClass) cp.get(is.readUnsignedShort());
		return new NestHostAttribute(name, nestHost);
	}

	/**
	 * @return Nest members attribute.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	@Nonnull
	private NestMembersAttribute readNestMembers() throws IOException {
		int count = is.readUnsignedShort();
		List<CpClass> memberClassIndices = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			memberClassIndices.add((CpClass) cp.get(is.readUnsignedShort()));
		}
		return new NestMembersAttribute(name, memberClassIndices);
	}

	/**
	 * @return Source debug attribute.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	@Nullable
	private SourceDebugExtensionAttribute readSourceDebugExtension() throws IOException {
		byte[] debugExtension = new byte[expectedContentLength];
		is.readFully(debugExtension);
		// Validate data represents UTF text
		try {
			new DataInputStream(new ByteArrayInputStream(debugExtension)).readUTF();
		} catch (Throwable t) {
			logger.debug("Invalid SourceDebugExtension, not a valid UTF");
			return null;
		}
		return new SourceDebugExtensionAttribute(name, debugExtension);
	}

	/**
	 * @param context
	 * 		Location the annotation is defined in.
	 *
	 * @return Annotations attribute.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	@Nullable
	private AnnotationsAttribute readAnnotations(AttributeContext context, boolean visible) throws IOException {
		return new AnnotationReader(reader, builder.getPool(), is, expectedContentLength, name, context, visible)
				.readAnnotations();
	}

	/**
	 * @param context
	 * 		Location the annotation is defined in.
	 *
	 * @return ParameterAnnotations attribute.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	@Nullable
	private ParameterAnnotationsAttribute readParameterAnnotations(AttributeContext context, boolean visible)
			throws IOException {
		return new AnnotationReader(reader, builder.getPool(), is, expectedContentLength, name, context, visible)
				.readParameterAnnotations();
	}

	/**
	 * @param context
	 * 		Location the annotation is defined in.
	 *
	 * @return TypeAnnotation attribute.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	@Nullable
	private AnnotationsAttribute readTypeAnnotations(AttributeContext context, boolean visible) throws IOException {
		return new AnnotationReader(reader, builder.getPool(), is, expectedContentLength, name, context, visible)
				.readTypeAnnotations();
	}

	/**
	 * @param context
	 * 		Location the annotation is defined in.
	 *
	 * @return AnnotationDefault attribute.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	@Nullable
	private AnnotationDefaultAttribute readAnnotationDefault(AttributeContext context) throws IOException {
		return new AnnotationReader(reader, builder.getPool(), is, expectedContentLength, name, context, true)
				.readAnnotationDefault();
	}

	/**
	 * @return Synthetic attribute.
	 */
	@Nonnull
	private SyntheticAttribute readSynthetic() {
		return new SyntheticAttribute(name);
	}

	/**
	 * @return Bootstrap methods attribute.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	@Nonnull
	private BootstrapMethodsAttribute readBoostrapMethods() throws IOException {
		List<BootstrapMethod> bootstrapMethods = new ArrayList<>();
		int bsmCount = is.readUnsignedShort();
		for (int i = 0; i < bsmCount; i++) {
			CpMethodHandle methodRef = (CpMethodHandle) cp.get(is.readUnsignedShort());
			int argCount = is.readUnsignedShort();
			List<CpEntry> args = new ArrayList<>();
			for (int j = 0; j < argCount; j++) {
				args.add(cp.get(is.readUnsignedShort()));
			}
			bootstrapMethods.add(new BootstrapMethod(methodRef, args));
		}
		return new BootstrapMethodsAttribute(name, bootstrapMethods);
	}

	/**
	 * @return Code attribute.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	@Nonnull
	private CodeAttribute readCode() throws IOException {
		int maxStack = -1;
		int maxLocals = -1;
		int codeLength = -1;
		List<ExceptionTableEntry> exceptions = new ArrayList<>();
		List<Attribute> attributes = new ArrayList<>();

		// Parse depending on class format version
		if (builder.isOakVersion()) {
			// Pre-java oak parsing (half-size data types)
			maxStack = is.readUnsignedByte();
			maxLocals = is.readUnsignedByte();
			codeLength = is.readUnsignedShort();
		} else {
			// Modern parsing
			maxStack = is.readUnsignedShort();
			maxLocals = is.readUnsignedShort();
			codeLength = is.readInt();
		}

		// The JVMLS states method code shall not be longer than an unsigned short.
		if (reader.doCheckCodeLength() && codeLength > 65536)
			throw new IOException("Method code_length > 65536: " + codeLength);

		// Read instructions
		InstructionReader reader = new InstructionReader(this.reader.fallbackReaderSupplier.get());
		List<Instruction> instructions = reader.read(is, cp, codeLength);

		// Read exceptions
		int numExceptions = is.readUnsignedShort();
		for (int i = 0; i < numExceptions; i++)
			exceptions.add(readCodeException());

		// Read attributes
		int numAttributes = is.readUnsignedShort();
		for (int i = 0; i < numAttributes; i++) {
			Attribute attr = new AttributeReader(this.reader, builder, is).readAttribute(AttributeContext.ATTRIBUTE);
			if (attr != null)
				attributes.add(attr);
		}

		return new CodeAttribute(name, maxStack, maxLocals, instructions, exceptions, attributes);
	}

	/**
	 * @return Exception table entry for code attribute.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	@Nonnull
	private CodeAttribute.ExceptionTableEntry readCodeException() throws IOException {
		return new CodeAttribute.ExceptionTableEntry(
				is.readUnsignedShort(),
				is.readUnsignedShort(),
				is.readUnsignedShort(),
				orNullInCp(is.readUnsignedShort())
		);
	}

	/**
	 * @return Constant value attribute.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	@Nonnull
	private ConstantValueAttribute readConstantValue() throws IOException {
		CpEntry value = cp.get(is.readUnsignedShort());
		return new ConstantValueAttribute(name, value);
	}

	@Nonnull
	private StackMapTableAttribute readStackMapTable() throws IOException {
		int numEntries = is.readUnsignedShort();
		List<StackMapTableAttribute.StackMapFrame> frames = new ArrayList<>(numEntries);
		for (int i = 0; i < numEntries; i++) {
			// u1: frame_type
			int frameType = is.readUnsignedByte();
			if (frameType <= StackMapTableConstants.SAME_FRAME_MAX) {
				// same_frame
				// The offset_delta is the frame_type
				frames.add(new StackMapTableAttribute.SameFrame(frameType));
			} else if (frameType <= StackMapTableConstants.SAME_LOCALS_ONE_STACK_ITEM_MAX) {
				// same_locals_1_stack_item_frame
				// The offset_delta is frame_type - 64
				// verification_type_info stack
				StackMapTableAttribute.TypeInfo stack = readVerificationTypeInfo();
				frames.add(new StackMapTableAttribute.SameLocalsOneStackItem(
						frameType - 64,
						stack
				));
			} else if (frameType < StackMapTableConstants.SAME_LOCALS_ONE_STACK_ITEM_EXTENDED_MIN) {
				// Tags in the range [128-246] are reserved for future use.
				throw new IllegalArgumentException("Unknown stackframe tag " + frameType);
			} else if (frameType <= StackMapTableConstants.SAME_LOCALS_ONE_STACK_ITEM_EXTENDED_MAX) {
				// same_locals_1_stack_item_frame_extended
				// u2: offset_delta
				int offsetDelta = is.readUnsignedShort();
				// verification_type_info stack
				StackMapTableAttribute.TypeInfo stack = readVerificationTypeInfo();
				frames.add(
						new StackMapTableAttribute.SameLocalsOneStackItemExtended(
								offsetDelta,
								stack
						)
				);
			} else if (frameType <= StackMapTableConstants.CHOP_FRAME_MAX) {
				// chop_frame
				// This frame type indicates that the frame has the same local
				// variables as the previous frame except that the last k local
				// variables are absent, and that the operand stack is empty. The
				// value of k is given by the formula 251 - frame_type.
				int k = 251 - frameType;
				// u2: offset_delta
				int offsetDelta = is.readUnsignedShort();
				frames.add(new StackMapTableAttribute.ChopFrame(offsetDelta, k));
			} else if (frameType < 252) {
				// same_frame_extended
				// u2: offset_delta
				int offsetDelta = is.readUnsignedShort();
				frames.add(new StackMapTableAttribute.SameFrameExtended(
						offsetDelta
				));
			} else if (frameType <= StackMapTableConstants.APPEND_FRAME_MAX) {
				// append_frame
				// u2: offset_delta
				int offsetDelta = is.readUnsignedShort();
				// verification_type_info locals[frame_type - 251]
				int numLocals = frameType - 251;
				List<StackMapTableAttribute.TypeInfo> locals = new ArrayList<>(numLocals);
				for (int j = 0; j < numLocals; j++) {
					locals.add(readVerificationTypeInfo());
				}
				frames.add(new StackMapTableAttribute.AppendFrame(
						offsetDelta, locals
				));
			} else if (frameType <= StackMapTableConstants.FULL_FRAME_MAX) {
				// full_frame
				// u2: offset_delta
				int offsetDelta = is.readUnsignedShort();
				// verification_type_info locals[u2 number_of_locals]
				int numLocals = is.readUnsignedShort();
				List<StackMapTableAttribute.TypeInfo> locals = new ArrayList<>(numLocals);
				for (int j = 0; j < numLocals; j++) {
					locals.add(readVerificationTypeInfo());
				}
				// verification_type_info stack[u2 number_of_stack_items]
				int numStackItems = is.readUnsignedShort();
				List<StackMapTableAttribute.TypeInfo> stack = new ArrayList<>(numStackItems);
				for (int j = 0; j < numStackItems; j++) {
					stack.add(readVerificationTypeInfo());
				}
				frames.add(new StackMapTableAttribute.FullFrame(
						offsetDelta, locals, stack
				));
			} else {
				throw new IllegalArgumentException("Unknown frame type " + frameType);
			}
		}
		return new StackMapTableAttribute(name, frames);
	}

	@Nonnull
	private StackMapTableAttribute.TypeInfo readVerificationTypeInfo() throws IOException {
		// u1 tag
		int tag = is.readUnsignedByte();
		switch (tag) {
			case StackMapTableConstants.ITEM_TOP:
				return new StackMapTableAttribute.TopVariableInfo();
			case StackMapTableConstants.ITEM_INTEGER:
				return new StackMapTableAttribute.IntegerVariableInfo();
			case StackMapTableConstants.ITEM_FLOAT:
				return new StackMapTableAttribute.FloatVariableInfo();
			case StackMapTableConstants.ITEM_DOUBLE:
				return new StackMapTableAttribute.DoubleVariableInfo();
			case StackMapTableConstants.ITEM_LONG:
				return new StackMapTableAttribute.LongVariableInfo();
			case StackMapTableConstants.ITEM_NULL:
				return new StackMapTableAttribute.NullVariableInfo();
			case StackMapTableConstants.ITEM_UNINITIALIZED_THIS:
				return new StackMapTableAttribute.UninitializedThisVariableInfo();
			case StackMapTableConstants.ITEM_OBJECT:
				// u2 cpool_index
				CpClass classEntry = (CpClass) cp.get(is.readUnsignedShort());
				return new StackMapTableAttribute.ObjectVariableInfo(classEntry);
			case StackMapTableConstants.ITEM_UNINITIALIZED:
				// u2 offset
				int offset = is.readUnsignedShort();
				return new StackMapTableAttribute.UninitializedVariableInfo(offset);
			default:
				throw new IllegalArgumentException("Unknown verification type tag " + tag);
		}
	}

	@Nullable
	@SuppressWarnings("unchecked")
	private <T extends CpEntry> T orNullInCp(int index) {
		// If the index is 0, that's an edge case where we want to use 'null'
		return index == 0 ? null : (T) cp.get(index);
	}
}
