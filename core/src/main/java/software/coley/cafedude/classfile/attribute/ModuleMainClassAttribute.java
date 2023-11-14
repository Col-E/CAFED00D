package software.coley.cafedude.classfile.attribute;

import software.coley.cafedude.classfile.behavior.CpAccessor;
import software.coley.cafedude.classfile.constant.CpClass;
import software.coley.cafedude.classfile.constant.CpEntry;
import software.coley.cafedude.classfile.constant.CpUtf8;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * Module main class attribute.
 *
 * @author Justus Garbe
 */
public class ModuleMainClassAttribute extends Attribute implements CpAccessor {
	private CpClass mainClass;

	/**
	 * @param name
	 * 		Constant pool entry holding the attribute name.
	 * @param mainClass
	 * 		Constant pool entry holding the main class type.
	 */
	public ModuleMainClassAttribute(@Nonnull CpUtf8 name, @Nonnull CpClass mainClass) {
		super(name);
		this.mainClass = mainClass;
	}

	/**
	 * @return Constant pool entry holding the main class type.
	 */
	@Nonnull
	public CpClass getMainClass() {
		return mainClass;
	}

	/**
	 * @param mainClass
	 * 		New constant pool entry holding the main class type
	 */
	public void setMainClass(@Nonnull CpClass mainClass) {
		this.mainClass = mainClass;
	}

	@Nonnull
	@Override
	public Set<CpEntry> cpAccesses() {
		Set<CpEntry> set = super.cpAccesses();
		set.add(mainClass);
		return set;
	}

	@Override
	public int computeInternalLength() {
		// U2: mainClassIndex
		return 2;
	}
}
