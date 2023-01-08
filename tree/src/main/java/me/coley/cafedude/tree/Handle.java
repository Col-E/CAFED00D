package me.coley.cafedude.tree;

import me.coley.cafedude.classfile.Descriptor;

/**
 * Handle to a method or field.
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
	public Handle(Tag tag, String owner, String name, Descriptor descriptor) {
		this.tag = tag;
		this.owner = owner;
		this.name = name;
		this.descriptor = descriptor;
	}

	/**
	 * @return Tag of the handle.
	 */
	public Tag getTag() {
		return tag;
	}

	/**
	 * @param tag
	 * 		Tag of the handle.
	 */
	public void setTag(Tag tag) {
		this.tag = tag;
	}

	/**
	 * @return Owner of the handle.
	 */
	public String getOwner() {
		return owner;
	}

	/**
	 * @param owner
	 * 		Owner of the handle.
	 */
	public void setOwner(String owner) {
		this.owner = owner;
	}

	/**
	 * @return Name of the handle.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 * 		Name of the handle.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return Descriptor of the handle.
	 */
	public Descriptor getDescriptor() {
		return descriptor;
	}

	/**
	 * @param descriptor
	 * 		Descriptor of the handle.
	 */
	public void setDescriptor(Descriptor descriptor) {
		this.descriptor = descriptor;
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
		public static Tag fromKind(int kind) {
			return values()[kind-1];
		}
	}

}
