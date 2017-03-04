package Translate;

import Temp.*;

public class Nx extends Exp {
	Tree.Stm stm;

	Nx(Tree.Stm s) {
		stm = s;
	}

	Tree.Exp unEx() {
		// 无返回值表达式不可能被翻译为有返回值表达式,故无操作
		return null;
	}

	Tree.Stm unNx() {
		return stm;
	}// 本身即为无返回值表达式,无需转换

	Tree.Stm unCx(Label t, Label f) {
		// 无法转换,故无操作
		return null;
	}
}
