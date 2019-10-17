package wespad;

public class EPair <T1, T2> {
	public T1 Key;
	public T2 Value;
	
	public EPair(T1 key, T2 value) {
		Key = key;
		Value = value;
	}

	@Override
	public String toString() {
		return Key + " > " + Value;
	}
	
}
