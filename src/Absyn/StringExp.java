package Absyn;

public class StringExp extends Exp {
	public String value;

	public StringExp(int p, String v) {
		pos = p;
		value = v;
	}
}
