package Tree;

public class MEM extends Exp {
	public Exp exp;

	public MEM(Exp e) {
		super(null);
		exp = e;
	}

	public ExpList kids() {
		return new ExpList(exp, null);
	}

	public Exp build(ExpList kids) {
		return new MEM(kids.head);
	}
}
