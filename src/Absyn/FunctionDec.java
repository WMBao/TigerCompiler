package Absyn;

import Symbol.Symbol;

public class FunctionDec extends Dec {
	public boolean inline = false;
	public Symbol name;
	public FieldList params;
	public NameTy result; /* optional */
	public Exp body;
	public FunctionDec next;

	public FunctionDec(int p, Symbol n, FieldList a, NameTy r, Exp b, FunctionDec x) {
		pos = p;
		name = n;
		params = a;
		result = r;
		body = b;
		next = x;
	}
}
