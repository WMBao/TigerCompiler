package Translate;

import Temp.*;

public class Nx extends Exp {
	Tree.Stm stm;

	Nx(Tree.Stm s) {
		stm = s;
	}

	Tree.Exp unEx() {
		// �޷���ֵ���ʽ�����ܱ�����Ϊ�з���ֵ���ʽ,���޲���
		return null;
	}

	Tree.Stm unNx() {
		return stm;
	}// ����Ϊ�޷���ֵ���ʽ,����ת��

	Tree.Stm unCx(Label t, Label f) {
		// �޷�ת��,���޲���
		return null;
	}
}
