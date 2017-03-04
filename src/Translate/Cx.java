package Translate;

import Temp.*;
import Tree.*;

public abstract class Cx extends Exp {
	Tree.Exp unEx() {
		Temp r = new Temp();// ����ֵ
		Label t = new Label();// �����
		Label f = new Label();// �ٳ���
		// ����Ϊ��������ĳ����:
		// if (exp!=0) goto T else goto F (���ľ��崦�����������)
		// LABEL f:
		// r=0
		// LABEL t:
		// return r
		return new ESEQ(
				new SEQ(new MOVE(new TEMP(r), new CONST(1)),
						new SEQ(unCx(t, f), // ���������
								new SEQ(new LABEL(f), new SEQ(new MOVE(new TEMP(r), new CONST(0)), new LABEL(t))))),
				new TEMP(r));
	}

	abstract Stm unCx(Label t, Label f); // ����������崦��

	Stm unNx() {
		return new Tree.Exp(unEx());
	} // ����������崦��
}
