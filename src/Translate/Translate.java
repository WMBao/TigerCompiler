package Translate;

import Tree.*;

public class Translate {
	private Frag.Frag frags = null;
	public Frame.Frame frame = null;

	// Translate����һ��Frame����һ��������
	public Translate(Frame.Frame f) {
		frame = f;
	}

	public Frag.Frag getResult() {
		return frags;
	}

	public void addFrag(Frag.Frag frag) {
		// �������б�
		frag.next = frags;
		frags = frag;
	}

	public void procEntryExit(Level level, Exp body, boolean returnValue) {
		Stm b = null;
		if (returnValue) {
			// ���з���ֵ,�򽫷���ֵ����$v0
			b = new MOVE(new TEMP(level.frame.RV()), body.unEx());
		} else
			// ���޷���ֵ,��ת��ΪNx
			b = body.unNx();
		b = level.frame.procEntryExit1(b);
		// ���뺯������ںͳ��ڴ���
		addFrag(new Frag.ProcFrag(b, level.frame));
		// ���ӳ����
	}

	public Exp transNoExp() {
		// ����һ�������,ʵ���Ϻ�nil�����ͬһ��Ч��
		return new Ex(new CONST(0));
	}

	public Exp transIntExp(int value) {
		// �������γ���
		return new Ex(new CONST(value));
	}

	public Exp transStringExp(String string) {
		// �����ַ�������,����һ���µ����ݶ�
		Temp.Label lab = new Temp.Label();
		addFrag(new Frag.DataFrag(lab, frame.string(lab, string)));
		return new Ex(new NAME(lab));
	}

	public Exp transNilExp() {
		// ����nil���
		return new Ex(new CONST(0));
	}

	public Exp transOpExp(int oper, Exp left, Exp right) {
		// �����Ԫ����
		if (oper >= BINOP.PLUS && oper <= BINOP.DIV)
			return new Ex(new BINOP(oper, left.unEx(), right.unEx()));
		// �Ӽ��˳�����

		return new RelCx(oper, left, right);// �߼��ж�
	}

	public Exp transStringRelExp(Level currentL, int oper, Exp left, Exp right) {
		// �����ַ����Ƚ�����,���ñ�׼�⺯�����бȽ�,Ȼ������߼��ж�
		Tree.Exp comp = currentL.frame.externalCall("stringEqual",
				new ExpList(left.unEx(), new ExpList(right.unEx(), null)));
		return new RelCx(oper, new Ex(comp), new Ex(new CONST(1)));
	}

	public Exp transAssignExp(Exp lvalue, Exp exp) {
		// ���븳ֵ���ʽ,ע�⸳ֵ���ʽ�޷���ֵ
		return new Nx(new MOVE(lvalue.unEx(), exp.unEx()));
	}

	public Exp transCallExp(Level currentL, Level dest, Temp.Label name, java.util.ArrayList<Exp> args_value) {
		// ������ͨ��������
		ExpList args = null;
		for (int i = args_value.size() - 1; i >= 0; --i) {
			args = new ExpList(((Exp) args_value.get(i)).unEx(), args);
		}
		// ����ʵ�β�����
		Level l = currentL;
		Tree.Exp currentFP = new TEMP(l.frame.FP());
		while (dest.parent != l) {
			currentFP = l.staticLink().acc.exp(currentFP);
			l = l.parent;
		}
		// ����������Ϣ�ҵ���̬����ָ��Ĳ�
		args = new ExpList(currentFP, args);
		// ����������Ϣ����������Ϣ,����Ϊ��һ��������$a0���뺯��
		return new Ex(new CALL(new NAME(name), args));
	}

	public Exp transStdCallExp(Level currentL, Temp.Label name, java.util.ArrayList<Exp> args_value) {
		// ������ñ�׼�⺯��
		ExpList args = null;
		for (int i = args_value.size() - 1; i >= 0; --i)
			args = new ExpList(((Exp) args_value.get(i)).unEx(), args);
		// ����ͨ�������õ��������ڱ�׼�⺯�������ں���Ƕ�׶���,�������ھ�̬��
		return new Ex(currentL.frame.externalCall(name.toString(), args));
	}

	public Exp stmcat(Exp e1, Exp e2) {
		// �����������ʽ,���Ӻ������޷���ֵ�ı��ʽ
		if (e1 == null) {
			if (e2 != null)
				return new Nx(e2.unNx());
			else
				return transNoExp();
		} else if (e2 == null)
			return new Nx(e1.unNx());
		else
			return new Nx(new SEQ(e1.unNx(), e2.unNx()));
	}

	public Exp exprcat(Exp e1, Exp e2) {
		// �����������ʽ,���Ӻ������з���ֵ�ı��ʽ
		if (e1 == null) {
			return new Ex(e2.unEx());
		} else {
			return new Ex(new ESEQ(e1.unNx(), e2.unEx()));
		}
	}

	public Exp transRecordExp(Level currentL, java.util.ArrayList<Exp> field) {
		// �����ⲿ���� _allocRecord Ϊ��¼�� frame �Ϸ���ռ�,
		// ���ô洢�ռ��׵�ַ
		// _allocRecord ִ�����µ��� C ����,ע����ֻ�������ռ�
		// ��ʼ��������Ҫ���������
		// ������runtime.c�еĴ���
		// # int *allocRecord(int size)
		// # {int i;
		// # int *p, *a;
		// # p = a = (int *)malloc(size);
		// # for(i=0;i<size;i+=sizeof(int)) *p++ = 0;
		// # return a;
		// # }
		// ע�������¼Ϊ��,ҲҪ�� 1 �� ������,����ÿ����Ϊһ��������,��˳����
		Temp.Temp addr = new Temp.Temp();
		Tree.Exp rec_id = currentL.frame.externalCall("allocRecord",
				new ExpList(new CONST((field.size() == 0 ? 1 : field.size()) * Library.WORDSIZE), null));

		Stm stm = transNoExp().unNx();
		// ��ʼ��ָ��
		for (int i = field.size() - 1; i >= 0; --i) {
			Tree.Exp offset = new BINOP(BINOP.PLUS, new TEMP(addr), new CONST(i * Library.WORDSIZE));
			Tree.Exp value = (field.get(i)).unEx();
			stm = new SEQ(new MOVE(new MEM(offset), value), stm);
			// Ϊ��¼��ÿ�������� MOVE ָ��,��ֵ���Ƶ�֡�е���Ӧ����
		}
		// ���ؼ�¼���׵�ַ
		return new Ex(new ESEQ(new SEQ(new MOVE(new TEMP(addr), rec_id), stm), new TEMP(addr)));
	}

	public Exp transArrayExp(Level currentL, Exp init, Exp size) {
		// �����ⲿ���� initArray Ϊ������ frame �Ϸ���洢�ռ�,���õ�
		// �洢�ռ��׵�ַ
		// initArray ִ�����µ��� C ����,��Ҫ�ṩ�����С���ʼֵ
		// # int *initArray(int size, int init)
		// # {int i;
		// # int *a = (int *)malloc(size*sizeof(int));
		// # for(i=0;i<size;i++) a[i]=init;
		// # return a;
		// # }
		Tree.Exp alloc = currentL.frame.externalCall("initArray",
				new ExpList(size.unEx(), new ExpList(init.unEx(), null)));
		return new Ex(alloc);
	}

	public Exp transIfExp(Exp test, Exp e1, Exp e2) {
		// ��if��䷭��ΪIR���Ľڵ�
		return new IfExp(test, e1, e2);
	}

	public Exp transWhileExp(Exp test, Exp body, Temp.Label out) {
		// ��while��䷭��ΪIR���Ľڵ�,ע��ֻ������Nx
		return new WhileExp(test, body, out);
	}

	public Exp transForExp(Level currentL, Access var, Exp low, Exp high, Exp body, Temp.Label out) {
		// ��for��䷭��ΪIR���Ľڵ�,ע��ֻ������Nx
		return new ForExp(currentL, var, low, high, body, out);
	}

	public Exp transBreakExp(Temp.Label l) {
		// ����break���ΪIR���Ľڵ�,lΪloopstack��ջ�����
		return new Nx(new JUMP(l));
	}

	public Exp transSimpleVar(Access acc, Level currentL) {
		// ����򵥱���
		Tree.Exp e = new TEMP(currentL.frame.FP());
		Level l = currentL;
		// ���ڿ���Ϊ���ı���,�����ž�̬���Ӳ�������, ֱ�������Ĳ��뵱ǰ����ͬ
		while (l != acc.home) {
			e = l.staticLink().acc.exp(e);
			l = l.parent;
		}
		return new Ex(acc.acc.exp(e));
	}

	public Exp transSubscriptVar(Exp var, Exp index) {
		// ��������Ԫ��
		Tree.Exp arr_addr = var.unEx();
		// �����׵�ַ
		Tree.Exp arr_offset = new BINOP(BINOP.MUL, index.unEx(), new CONST(Library.WORDSIZE));
		// ���±�õ�ƫ����
		return new Ex(new MEM(new BINOP(BINOP.PLUS, arr_addr, arr_offset)));
		// ����ָ��ʹ�׵�ַ����ƫ����Ϊ����Ԫ��ʵ�ʵ�ַ
	}

	public Exp transFieldVar(Exp var, int fig) {
		// ������ĳ�Ա����
		Tree.Exp rec_addr = var.unEx();
		// �׵�ַ
		Tree.Exp rec_offset = new CONST(fig * Library.WORDSIZE);
		// ƫ����,ÿ����Առһ��������
		return new Ex(new MEM(new BINOP(BINOP.PLUS, rec_addr, rec_offset)));
	}
}
