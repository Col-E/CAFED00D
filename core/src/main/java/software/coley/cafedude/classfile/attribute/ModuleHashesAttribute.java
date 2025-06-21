package software.coley.cafedude.classfile.attribute;

import software.coley.cafedude.classfile.constant.CpEntry;
import software.coley.cafedude.classfile.constant.CpUtf8;

import jakarta.annotation.Nonnull;
import java.util.Map;
import java.util.Set;

/**
 * Module hashes attribute.
 *
 * @author Matt Coley
 */
public class ModuleHashesAttribute extends Attribute {
	private Map<CpUtf8, byte[]> moduleHashes;
	private CpUtf8 algorithmName;

	/**
	 * @param name
	 * 		Constant pool entry holding the attribute name.
	 * @param algorithmName
	 * 		Constant pool entry holding the algorithm name.
	 * @param moduleHashes
	 * 		Map of constant pool entries of module names to their hashes.
	 */
	public ModuleHashesAttribute(@Nonnull CpUtf8 name, @Nonnull CpUtf8 algorithmName,
	                             @Nonnull Map<CpUtf8, byte[]> moduleHashes) {
		super(name);
		this.algorithmName = algorithmName;
		this.moduleHashes = moduleHashes;
	}


	/**
	 * @return Constant pool entry holding the algorithm name.
	 */
	@Nonnull
	public CpUtf8 getAlgorithmName() {
		return algorithmName;
	}

	/**
	 * @param algorithmName
	 * 		New constant pool entry holding the algorithm name.
	 */
	public void setAlgorithmName(@Nonnull CpUtf8 algorithmName) {
		this.algorithmName = algorithmName;
	}

	/**
	 * @return Map of constant pool entries of module names to their hashes.
	 */
	@Nonnull
	public Map<CpUtf8, byte[]> getModuleHashes() {
		return moduleHashes;
	}

	/**
	 * @param moduleHashes
	 * 		New map of constant pool entries of module names to their hashes.
	 */
	public void setModuleHashes(@Nonnull Map<CpUtf8, byte[]> moduleHashes) {
		this.moduleHashes = moduleHashes;
	}

	@Nonnull
	@Override
	public Set<CpEntry> cpAccesses() {
		Set<CpEntry> set = super.cpAccesses();
		set.add(getAlgorithmName());
		set.addAll(moduleHashes.keySet());
		return set;
	}

	@Override
	public int computeInternalLength() {
		// U2: platformName
		// U2: Entry count
		//  - U2: Name index
		//  - U2: Hash length
		//  -  N: Hash content
		return 4 + moduleHashes.values().stream()
				.mapToInt(bytes -> 4 + bytes.length)
				.sum();
	}
}
