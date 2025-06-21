package software.coley.cafedude.classfile.attribute;

import software.coley.cafedude.classfile.constant.CpEntry;
import software.coley.cafedude.classfile.constant.CpUtf8;

import jakarta.annotation.Nonnull;
import java.util.Set;

/**
 * Module target attribute, will hold values like <i>"linux-x86"</i>, <i>"win-x64"</i>, etc.
 *
 * @author Matt Coley
 */
public class ModuleTargetAttribute extends Attribute {
	private CpUtf8 platformName;

	/**
	 * @param name
	 * 		Constant pool entry holding the attribute name.
	 * @param platformName
	 * 		Constant pool entry holding the platform name.
	 */
	public ModuleTargetAttribute(@Nonnull CpUtf8 name, @Nonnull CpUtf8 platformName) {
		super(name);
		this.platformName = platformName;
	}

	/**
	 * @return Constant pool entry holding the platform name.
	 */
	@Nonnull
	public CpUtf8 getPlatformName() {
		return platformName;
	}

	/**
	 * @param platformName
	 * 		New constant pool entry holding the platform name.
	 */
	public void setPlatformName(@Nonnull CpUtf8 platformName) {
		this.platformName = platformName;
	}

	@Nonnull
	@Override
	public Set<CpEntry> cpAccesses() {
		Set<CpEntry> set = super.cpAccesses();
		set.add(getPlatformName());
		return set;
	}

	@Override
	public int computeInternalLength() {
		// U2: platformName
		return 2;
	}
}
