package me.coley.cafedude.classfile.attribute;

import me.coley.cafedude.classfile.behavior.AttributeHolder;
import me.coley.cafedude.classfile.behavior.CpAccessor;
import me.coley.cafedude.classfile.constant.CpEntry;
import me.coley.cafedude.classfile.constant.CpUtf8;
import me.coley.cafedude.io.AttributeContext;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Permitted classes attribute.
 *
 * @author Matt Coley
 */
public class RecordAttribute extends Attribute {
	private List<RecordComponent> components;

	/**
	 * @param name
	 * 		Name index in constant pool.
	 * @param components
	 * 		Record components <i>(fields)</i>.
	 */
	public RecordAttribute(CpUtf8 name, List<RecordComponent> components) {
		super(name);
		this.components = components;
	}

	@Override
	public Set<CpEntry> cpAccesses() {
		Set<CpEntry> set = super.cpAccesses();
		for (RecordComponent component : getComponents())
			set.addAll(component.cpAccesses());
		return set;
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
	 * @param components
	 * 		New record components <i>(fields)</i>.
	 */
	public void setComponents(List<RecordComponent> components) {
		this.components = components;
	}

	/**
	 * Component entry.
	 */
	public static class RecordComponent implements CpAccessor, AttributeHolder {
		private CpUtf8 name;
		private CpUtf8 desc;
		private List<Attribute> attributes;

		/**
		 * @param name
		 * 		Entry of name of component.
		 * @param desc
		 * 		Entry of field descriptor of component.
		 * @param attributes
		 * 		Attributes of the record field.
		 */
		public RecordComponent(CpUtf8 name, CpUtf8 desc, List<Attribute> attributes) {
			this.name = name;
			this.desc = desc;
			this.attributes = attributes;
		}

		/**
		 * @return Index of name of component.
		 */
		public CpUtf8 getName() {
			return name;
		}

		/**
		 * @param name
		 * 		New index of name of component.
		 */
		public void setName(CpUtf8 name) {
			this.name = name;
		}

		/**
		 * @return Index of field descriptor of component.
		 */
		public CpUtf8 getDesc() {
			return desc;
		}

		/**
		 * @param descIndex
		 * 		New index of field descriptor of component.
		 */
		public void setDesc(CpUtf8 desc) {
			this.desc = desc;
		}

		/**
		 * @return Attributes of the record field.
		 */
		public List<Attribute> getAttributes() {
			return attributes;
		}

		@Override
		public <T extends Attribute> @Nullable T getAttribute(Class<T> type) {
			return null;
		}

		/**
		 * @param attributes
		 * 		New attributes of the record field.
		 */
		public void setAttributes(List<Attribute> attributes) {
			this.attributes = attributes;
		}

		@Override
		public AttributeContext getHolderType() {
			return AttributeContext.RECORD_COMPONENT;
		}

		@Override
		public Set<CpEntry> cpAccesses() {
			Set<CpEntry> set = new HashSet<>();
			set.add(name);
			set.add(desc);
			return set;
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
