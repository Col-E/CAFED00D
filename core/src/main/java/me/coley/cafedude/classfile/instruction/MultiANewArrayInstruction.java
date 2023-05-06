package me.coley.cafedude.classfile.instruction;

import me.coley.cafedude.classfile.behavior.CpAccessor;
import me.coley.cafedude.classfile.constant.CpClass;
import me.coley.cafedude.classfile.constant.CpEntry;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Set;

public class MultiANewArrayInstruction extends BasicInstruction implements CpAccessor {

	private CpClass descriptor;
	private int dimensions;

	public MultiANewArrayInstruction(CpClass descriptor, int dimensions) {
		super(Opcodes.MULTIANEWARRAY);
		this.descriptor = descriptor;
		this.dimensions = dimensions;
	}

	public int getDimensions() {
		return dimensions;
	}

	public void setDimensions(int dimensions) {
		this.dimensions = dimensions;
	}

	public CpClass getDescriptor() {
		return descriptor;
	}

	public void setDescriptor(CpClass descriptor) {
		this.descriptor = descriptor;
	}

	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof MultiANewArrayInstruction)) return false;
		if (!super.equals(o)) return false;

		MultiANewArrayInstruction that = (MultiANewArrayInstruction) o;

		if (dimensions != that.dimensions) return false;
		return descriptor.equals(that.descriptor);
	}

	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + dimensions;
		result = 31 * result + descriptor.hashCode();
		return result;
	}

	@Nonnull
	@Override
	public Set<CpEntry> cpAccesses() {
		return Collections.singleton(descriptor);
	}
}
