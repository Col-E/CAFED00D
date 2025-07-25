package software.coley.cafedude.classfile.instruction;

import jakarta.annotation.Nonnull;
import software.coley.cafedude.classfile.behavior.CpAccessor;
import software.coley.cafedude.classfile.constant.CpClass;
import software.coley.cafedude.classfile.constant.CpEntry;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * Instruction for {@code multianewarray}.
 *
 * @author Justus Garbe
 */
public non-sealed class MultiANewArrayInstruction extends Instruction implements CpAccessor {
	private CpClass descriptor;
	private int dimensions;

	/**
	 * @param descriptor
	 * 		Constant pool entry holding the class type of the array.
	 * @param dimensions
	 * 		Number of dimensions for the array.
	 */
	public MultiANewArrayInstruction(@Nonnull CpClass descriptor, int dimensions) {
		super(Opcodes.MULTIANEWARRAY);
		this.descriptor = descriptor;
		this.dimensions = dimensions;
	}

	/**
	 * @return Number of dimensions for the array.
	 */
	public int getDimensions() {
		return dimensions;
	}

	/**
	 * @param dimensions
	 * 		New number of dimensions for the array.
	 */
	public void setDimensions(int dimensions) {
		this.dimensions = dimensions;
	}

	/**
	 * @return Constant pool entry holding the class type of the array.
	 */
	@Nonnull
	public CpClass getDescriptor() {
		return descriptor;
	}

	/**
	 * @param descriptor
	 * 		New constant pool entry holding the class type of the array.
	 */
	public void setDescriptor(@Nonnull CpClass descriptor) {
		this.descriptor = descriptor;
	}

	@Override
	public int computeSize() {
		// u1: opcode
		// u2: array class type index
		// u1: dims
		return 4;
	}

	@Nonnull
	@Override
	public Set<CpEntry> cpAccesses() {
		return Collections.singleton(descriptor);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof MultiANewArrayInstruction that)) return false;
		if (!super.equals(o)) return false;

		if (dimensions != that.dimensions) return false;
		return Objects.equals(descriptor, that.descriptor);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + dimensions;
		result = 31 * result + descriptor.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return super.toString() + " dimensions=" + dimensions + ", descriptor=" + descriptor.getName().getText();
	}
}
