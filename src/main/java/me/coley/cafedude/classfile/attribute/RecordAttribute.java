package me.coley.cafedude.classfile.attribute;

import java.util.List;

/**
 * Permitted classes attribute.
 *
 * @author Matt Coley
 */
public class RecordAttribute extends Attribute {
	private List<RecordComponent> components;

	/**
	 * @param nameIndex
	 * 		Name index in constant pool.
	 * @param components
	 * 		Record components <i>(fields)</i>.
	 */
	public RecordAttribute(int nameIndex, List<RecordComponent> components) {
		super(nameIndex);
		this.components = components;
	}

	@Override
	public int computeInternalLength() {
		// u2: count
		// u2: class_index * count
		return 2 + (components.stream().mapToInt(RecordComponent::length).sum());
	}

	/**
	 * @return Record components <i>(fields)</i>.
	 */
	public List<RecordComponent> getComponents() {
		return components;
	}

	/**
	 * @param components New record components <i>(fields)</i>.
	 */
	public void setComponents(List<RecordComponent> components) {
		this.components = components;
	}

	/**
	 * Component entry.
	 */
	public static class RecordComponent {
		private int nameIndex;
		private int descIndex;
		private List<Attribute> attributes;

		/**
		 * @param nameIndex
		 * 		Index of name of component.
		 * @param descIndex
		 * 		Index of field descriptor of component.
		 * @param attributes
		 * 		Attributes of the record field.
		 */
		public RecordComponent(int nameIndex, int descIndex, List<Attribute> attributes) {
			this.nameIndex = nameIndex;
			this.descIndex = descIndex;
			this.attributes = attributes;
		}

		/**
		 * @return Index of name of component.
		 */
		public int getNameIndex() {
			return nameIndex;
		}

		/**
		 * @param nameIndex
		 * 		New index of name of component.
		 */
		public void setNameIndex(int nameIndex) {
			this.nameIndex = nameIndex;
		}

		/**
		 * @return Index of field descriptor of component.
		 */
		public int getDescIndex() {
			return descIndex;
		}

		/**
		 * @param descIndex
		 * 		New index of field descriptor of component.
		 */
		public void setDescIndex(int descIndex) {
			this.descIndex = descIndex;
		}

		/**
		 * @return Attributes of the record field.
		 */
		public List<Attribute> getAttributes() {
			return attributes;
		}

		/**
		 * @param attributes
		 * 		New attributes of the record field.
		 */
		public void setAttributes(List<Attribute> attributes) {
			this.attributes = attributes;
		}

		/**
		 * @return Component bytecode size.
		 */
		public int length() {
			// u2: name_index
			// u2: desc_index
			// u2: attribute_count
			// ??  attributes[]
			int len = 6;
			for (Attribute attribute : attributes)
				len += attribute.computeCompleteLength();
			return len;
		}
	}
}
