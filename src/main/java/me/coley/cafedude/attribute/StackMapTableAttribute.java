package me.coley.cafedude.attribute;

public class StackMapTableAttribute extends Attribute {
	public final StackMapFrame[] frames;
	
	public StackMapTableAttribute(StackMapFrame[] frames) {
		this.frames = frames;
	}
	
	@Override
	public int computeInternalLength() {
		
	}
	
	public static abstract class TypeInfo {
		public int computeInternalLength() {
			// By default no contents
			return 0;
		}
		
		public int computeCompleteLength() {
			// u1 tag + internal
			return 1 + computeInternalLength();
		}
	}
	
	public static class TopVariableInfo extends TypeInfo {
	}
	
	public static class IntegerVariableInfo extends TypeInfo {
	}
	
	public static class FloatVariableInfo extends TypeInfo {
	}
	
	public static class NullVariableInfo extends TypeInfo {
	}
	
	public static class UninitializedThisVariableInfo extends TypeInfo {
	}
	
	public static class ObjectVariableInfo extends TypeInfo {
		// Index of the ClassConstant representing type of this variable
		public int classIndex;
		
		public ObjectVariableInfo(int classIndex) {
			this.classIndex = classIndex;
		}
		
		@Override
		public int computeInternalLength() {
			// u2 cpool_index
			return 2;
		}
	}
	
	public static class UninitializedVariableInfo extends TypeInfo {
		// Offset of code where the NEW instruction resides
		public int offset;
		
		public UninitializedVariableInfo(int offset) {
			this.offset = offset;
		}
		
		@Override
		public int computeInternalLength() {
			// u2 offset
			return 2;
		}
	}
	
	public static class LongVariableInfo extends TypeInfo {
	}
	
	public static class DoubleVariableInfo extends TypeInfo {
	}
	
	public static class StackMapFrame {
		
	}
}
