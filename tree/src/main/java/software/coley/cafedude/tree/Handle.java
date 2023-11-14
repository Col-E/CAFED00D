package software.coley.cafedude.tree;

import software.coley.cafedude.classfile.Descriptor;

import javax.annotation.Nonnull;

/**
 * Handle to a method or field.
 *
 * @author Justus Garbe
 */
public class Handle {
	private Tag tag;
	private String owner;
	private String name;
	private Descriptor descriptor;

	/**
	 * @param tag
	 * 		Tag of the handle.
	 * @param owner
	 * 		Owner of the handle.
	 * @param name
	 * 		Name of the handle.
	 * @param descriptor
	 * 		Descriptor of the handle.
	 */
	public Handle(@Nonnull Tag tag, @Nonnull String owner, @Nonnull String name, @Nonnull Descriptor descriptor) {
		this.tag = tag;
		this.owner = owner;
		this.name = name;
		this.descriptor = descriptor;
	}

	/**
	 * @return Tag of the handle.
	 */
	@Nonnull
	public Tag getTag() {
		return tag;
	}

	/**
	 * @param tag
	 * 		Tag of the handle.
	 */
	public void setTag(@Nonnull Tag tag) {
		this.tag = tag;
	}

	/**
	 * @return Owner of the handle.
	 */
	@Nonnull
	public String getOwner() {
		return owner;
	}

	/**
	 * @param owner
	 * 		Owner of the handle.
	 */
	public void setOwner(@Nonnull String owner) {
		this.owner = owner;
	}

	/**
	 * @return Name of the handle.
	 */
	@Nonnull
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 * 		Name of the handle.
	 */
	public void setName(@Nonnull String name) {
		this.name = name;
	}

	/**
	 * @return Descriptor of the handle.
	 */
	@Nonnull
	public Descriptor getDescriptor() {
		return descriptor;
	}

	/**
	 * @param descriptor
	 * 		Descriptor of the handle.
	 */
	public void setDescriptor(@Nonnull Descriptor descriptor) {
		this.descriptor = descriptor;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Handle handle = (Handle) o;

		if (tag != handle.tag) return false;
		if (!owner.equals(handle.owner)) return false;
		if (!name.equals(handle.name)) return false;
		return descriptor.equals(handle.descriptor);
	}

	@Override
	public int hashCode() {
		int result = tag.hashCode();
		result = 31 * result + owner.hashCode();
		result = 31 * result + name.hashCode();
		result = 31 * result + descriptor.hashCode();
		return result;
	}

	/**
	 * Handle tag.
	 */
	public enum Tag {
		GETFIELD,
		GETSTATIC,
		PUTFIELD,
		PUTSTATIC,
		INVOKEVIRTUAL,
		INVOKESTATIC,
		INVOKESPECIAL,
		NEWINVOKESPECIAL,
		INVOKEINTERFACE;

		/**
		 * @param kind
		 * 		Tag ordinal.
		 *
		 * @return Tag for the given ordinal.
		 */
		@Nonnull
		public static Tag fromKind(int kind) {
			return values()[kind - 1];
		}
	}
}
