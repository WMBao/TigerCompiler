package FindEscape;

public class FormalEscape extends Escape {
	Absyn.FieldList fl;

	FormalEscape(int d, Absyn.FieldList f) {
		depth = d;
		fl = f;
		fl.escape = false;
	}

	void setEscape() {
		fl.escape = true;
	}
}
