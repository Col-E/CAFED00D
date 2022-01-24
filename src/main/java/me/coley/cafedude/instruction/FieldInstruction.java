package me.coley.cafedude.instruction;

/**
 * Field instruction.
 *
 * @author xDark
 */
public class FieldInstruction extends BasicInstruction {

	private String owner;
	private String name;
	private String desc;

	/**
	 * @param opcode
	 * 		Instruction opcode.
	 * @param owner
	 * 		Field owner.
	 * @param name
	 * 		Field name.
	 * @param desc
	 * 		Field desc.
	 */
	public FieldInstruction(int opcode, String owner, String name, String desc) {
		super(opcode);
		this.owner = owner;
		this.name = name;
		this.desc = desc;
	}

	/**
	 * @return field owner.
	 */
	public String getOwner() {
		return owner;
	}

	/**
	 * Sets field owner.
	 *
	 * @param owner
	 * 		New owner.
	 */
	public void setOwner(String owner) {
		this.owner = owner;
	}

	/**
	 * @return field name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets field name.
	 *
	 * @param name
	 * 		New name.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return field descriptor.
	 */
	public String getDesc() {
		return desc;
	}

	/**
	 * Sets field descriptor.
	 *
	 * @param desc
	 * 		New descriptor.
	 */
	public void setDesc(String desc) {
		this.desc = desc;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof FieldInstruction)) return false;
		if (!super.equals(o)) return false;

		FieldInstruction that = (FieldInstruction) o;

		if (!owner.equals(that.owner)) return false;
		if (!name.equals(that.name)) return false;
		return desc.equals(that.desc);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + owner.hashCode();
		result = 31 * result + name.hashCode();
		result = 31 * result + desc.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "field(" + getOpcode() + ", " + owner + ", " + name + ", " + desc + ')';
	}
}
