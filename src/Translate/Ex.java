package Translate;

import Temp.*;

public class Ex extends Exp {
	Tree.Exp exp;

	public Ex(Tree.Exp e) {
		exp = e;
	}

	Tree.Exp unEx() {
		return exp;
	}// Tree.Exp 本身就是有返回值表达式,无需转换直接返回

	Tree.Stm unNx() {
		return new Tree.Exp(exp);
	} // Tree.Exp 无返回值表达式

	Tree.Stm unCx(Label t, Label f) {
		return new Tree.CJUMP(Tree.CJUMP.NE, exp, new Tree.CONST(0), t, f);
	}
	// 若表达式非 0 转到 t,否则转到 f
}
