package Translate;

import Temp.*;
import Tree.*;

public class ForExp extends Exp {
	Level currentL; // ��
	Access var; // ѭ������
	Exp low, high, body; // ��ʼֵ\��ֵֹ\ѭ����
	Label out; // ����

	ForExp(Level home, Access var, Exp low, Exp high, Exp body, Label out) {
		this.currentL = home;
		this.var = var;
		this.low = low;
		this.high = high;
		this.body = body;
		this.out = out;
	}

	// for���û�з���ֵ,����ת��ΪEx
	Tree.Exp unEx() {
		System.err.println("WhileExp.unCx()");
		return null;
	}

	// ѭ��������ѭ�����ޱ�������֡�ռ���
	// ����Ϊ������ʽ�Ĵ���:
	// MOVE VAR, LOW
	// MOVE LIMIT, HIGH
	// if (VAR<=LIMIT) goto BEGIN else goto DONE
	// LABEL BEGIN:
	// body
	// if (VAR<LIMIT) goto GOON else goto DONE
	// LABEL GOON:
	// VAR=VAR+1
	// GOTO BEGIN:
	// LABEL DONE:
	Tree.Stm unNx() {
		Access hbound = currentL.allocLocal(true);
		Label begin = new Label();
		Label goon = new Label();

		return new SEQ(
				new MOVE(
						var.acc.exp(new TEMP(currentL.frame.FP())), low
								.unEx()),
				new SEQ(new MOVE(
						hbound.acc
								.exp(new TEMP(currentL.frame.FP())),
						high.unEx()),
						new SEQ(
								new CJUMP(CJUMP.LE, var.acc.exp(new TEMP(currentL.frame.FP())),
										hbound.acc.exp(new TEMP(currentL.frame.FP())), begin, out),
								new SEQ(new LABEL(begin), new SEQ(body.unNx(),
										new SEQ(new CJUMP(CJUMP.LT, var.acc.exp(new TEMP(currentL.frame.FP())),
												hbound.acc
														.exp(new TEMP(currentL.frame.FP())),
												goon, out),
												new SEQ(
														new LABEL(
																goon),
														new SEQ(new MOVE(var.acc.exp(new TEMP(currentL.frame.FP())),
																new BINOP(BINOP.PLUS,
																		var.acc.exp(new TEMP(currentL.frame.FP())),
																		new CONST(1))),
																new SEQ(new JUMP(begin), new LABEL(out))))))))));
	}

	// forѭ��ֻ��һ������,����ת��
	Tree.Stm unCx(Label t, Label f) {
		System.err.println("ForExp.unEx() should not be called.");
		return null;
	}
}
