package me.coley.cafedude.tree.frame;

/**
 * Contains a string which represents the type of the value.
 */
public class ObjectValue extends Value {

	private String type;

	public ObjectValue(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
