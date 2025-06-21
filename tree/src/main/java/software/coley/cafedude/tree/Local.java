package software.coley.cafedude.tree;

import software.coley.cafedude.classfile.Descriptor;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * Local variable object.
 *
 * @author Justus Garbe
 */
public class Local {
	private final int index;
	private String name;
	private Descriptor desc;
	private String signature;
	private Label start;
	private Label end;


	/**
	 * @param index
	 * 		Index of local variable.
	 * @param name
	 * 		Name of local variable.
	 * @param desc
	 * 		Descriptor of local variable.
	 * @param signature
	 * 		Signature of local variable.
	 * @param start
	 * 		Label of start of local variable.
	 * @param end
	 * 		Label of end of local variable.
	 */
	public Local(int index, @Nonnull String name, @Nonnull Descriptor desc, @Nullable String signature,
				 @Nonnull Label start, @Nonnull Label end) {
		this.index = index;
		this.name = name;
		this.desc = desc;
		this.signature = signature;
		this.start = start;
		this.end = end;
	}

	/**
	 * @return Name of local variable.
	 */
	@Nonnull
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 * 		Name of local variable.
	 */
	public void setName(@Nonnull String name) {
		this.name = name;
	}

	/**
	 * @return Descriptor of local variable.
	 */
	@Nonnull
	public Descriptor getDesc() {
		return desc;
	}

	/**
	 * @param desc
	 * 		Descriptor of local variable.
	 */
	public void setDesc(@Nonnull Descriptor desc) {
		this.desc = desc;
	}

	/**
	 * @return Signature of local variable.
	 */
	@Nullable
	public String getSignature() {
		return signature;
	}

	/**
	 * @param signature
	 * 		Signature of local variable.
	 */
	public void setSignature(@Nullable String signature) {
		this.signature = signature;
	}

	/**
	 * @return Label of start of local variable.
	 */
	@Nonnull
	public Label getStart() {
		return start;
	}

	/**
	 * @param start
	 * 		Label of start of local variable.
	 */
	public void setStart(@Nonnull Label start) {
		this.start = start;
	}

	/**
	 * @return Label of end of local variable.
	 */
	@Nonnull
	public Label getEnd() {
		return end;
	}

	/**
	 * @param end
	 * 		Label of end of local variable.
	 */
	public void setEnd(@Nonnull Label end) {
		this.end = end;
	}

	/**
	 * @return Local variable index.
	 */
	public int getIndex() {
		return index;
	}

}
