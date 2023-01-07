package me.coley.cafedude.tree;

import me.coley.cafedude.classfile.Descriptor;

public class Handle {

	private Tag tag;
	private String owner;
	private String name;
	private Descriptor descriptor;

	public Handle(Tag tag, String owner, String name, Descriptor descriptor) {
		this.tag = tag;
		this.owner = owner;
		this.name = name;
		this.descriptor = descriptor;
	}

	public Tag getTag() {
		return tag;
	}

	public void setTag(Tag tag) {
		this.tag = tag;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Descriptor getDescriptor() {
		return descriptor;
	}

	public void setDescriptor(Descriptor descriptor) {
		this.descriptor = descriptor;
	}

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

		public static Tag fromKind(int kind) {
			return values()[kind];
		}
	}

}
