package Types;

public class RECORD extends Type {
	public Symbol.Symbol fieldName;
	public Type fieldType;
	public RECORD tail;

	public RECORD(Symbol.Symbol n, Type t, RECORD x) {
		gen(n, t, x);
	}

	public RECORD() {
		gen(null, null, null);
	}

	public boolean coerceTo(Type t) {
		// return this==t.actual();
		Type a = t.actual();
		return (a instanceof RECORD) || (a instanceof NIL);
	}

	public void gen(Symbol.Symbol n, Type t, RECORD x) {
		fieldName = n;
		fieldType = t;
		tail = x;
	}

	static public boolean isNull(RECORD r) {
		if (r == null || (r.fieldName == null && r.fieldType == null && r.tail == null))
			return true;
		return false;
	}
}
