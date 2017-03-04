package Tree;

import Temp.Temp;
import Temp.Label;

public class NAME extends Exp {
	public Label label;

	public NAME(Label l) {
		super(null);
		label = l;
	}

	public ExpList kids() {
		return null;
	}

	public Exp build(ExpList kids) {
		return this;
	}
}
