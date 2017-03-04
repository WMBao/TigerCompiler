package Translate;

import Tree.*;

public class IfExp extends Exp {
	private Exp test; // ��������
	private Exp e1; // then�Ӿ�
	private Exp e2; // else�Ӿ�

	IfExp(Exp test, Exp e1, Exp e2) {
		this.test = test;
		this.e1 = e1;
		this.e2 = e2;
	}

	// if-else ����������з���ֵ�ı��ʽ:
	// if (test) goto T else goto F
	// LABEL T: r=e1
	// goto JOIN
	// LABEL F: r=e2
	// LABEL JOIN: return r
	Tree.Exp unEx() {
		Temp.Temp r = new Temp.Temp();// ����ֵ
		Temp.Label join = new Temp.Label();// ͳһ�ĳ���
		Temp.Label t = new Temp.Label();// �����
		Temp.Label f = new Temp.Label();// �ٳ���
		Tree.Exp tmp = new ESEQ(
				new SEQ(test.unCx(t, f), new SEQ(new LABEL(t), new SEQ(new MOVE(new TEMP(r), e1.unEx()),
						new SEQ(new JUMP(join),
								new SEQ(new LABEL(f), new SEQ(new MOVE(new TEMP(r), e2.unEx()), new LABEL(join))))))),
				new TEMP(r));
		return tmp;
	}

	// ����������޷���ֵ�� if-then
	// ���û�� else �Ӿ�
	// if (test) goto T else goto JOIN
	// LABEL T: e1
	// LABEL JOIN:
	// ����� else �Ӿ�
	// if (test) goto T else goto F
	// LABEL T: e1
	// goto JOIN
	// LABEL F: e2
	// LABEL JOIN:
	Stm unNx() {
		Temp.Label join = new Temp.Label();// ͳһ�ĳ���
		Temp.Label t = new Temp.Label();// �����
		Temp.Label f = new Temp.Label();// �ٳ���
		// ��������else�Ӿ�,��ȥ���ٳ���
		if (e2 == null)
			return new SEQ(test.unCx(t, join), new SEQ(new LABEL(t), new SEQ(e1.unNx(), new LABEL(join))));
		else
			return new SEQ(test.unCx(t, f), new SEQ(new LABEL(t), new SEQ(e1.unNx(),
					new SEQ(new JUMP(join), new SEQ(new LABEL(f), new SEQ(e2.unNx(), new LABEL(join)))))));
	}

	// ֱ�ӱ�� Cx:
	// if test!=0 goto T else goto F
	Stm unCx(Temp.Label t, Temp.Label f) {
		return new CJUMP(CJUMP.NE, unEx(), new CONST(0), t, f);
	}
}
