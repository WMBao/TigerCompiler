package Translate;

import Temp.*;

public class Ex extends Exp {
	Tree.Exp exp;

	public Ex(Tree.Exp e) {
		exp = e;
	}

	Tree.Exp unEx() {
		return exp;
	}// Tree.Exp ��������з���ֵ���ʽ,����ת��ֱ�ӷ���

	Tree.Stm unNx() {
		return new Tree.Exp(exp);
	} // Tree.Exp �޷���ֵ���ʽ

	Tree.Stm unCx(Label t, Label f) {
		return new Tree.CJUMP(Tree.CJUMP.NE, exp, new Tree.CONST(0), t, f);
	}
	// �����ʽ�� 0 ת�� t,����ת�� f
}
