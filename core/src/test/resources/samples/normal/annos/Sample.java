@Anno("Class Definition")
class Sample<@Anno("Type Parameter") T extends @Anno("Type Parameter Super") CharSequence> {
	private final T example;

	@Anno("Constructor")
	public Sample() {
		example = get(0);
	}

	@Anno("Method")
	@SuppressWarnings("unchecked")
	private T get(@Anno("Method Argument") int arg) {
		@Anno("Local type")
		String provided = provideString();
		return (@Anno("Local cast type") T) provided;
	}

	private String provideString() {
		return "Foo";
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof @Anno("Instanceof Type") Sample)
			return java.util.Objects.equals(example, ((@Anno("Sample cast type") Sample<?>) o).example);
		return false;
	}
}
