package me.coley.cafedude.tree.frame;

/**
 * Contains a string which represents the type of the value.
 *
 * @author Justus Garbe
 */
public class ObjectValue extends Value {
	private String type;

	/**
	 * @param type
	 * 		Type of the value.
	 */
	public ObjectValue(String type) {
		this.type = type;
	}

	/**
	 * @return Type of the value.
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type
	 * 		Type of the value.
	 */
	public void setType(String type) {
		this.type = type;
	}

}
