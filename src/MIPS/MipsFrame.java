package MIPS;

import Frame.*;
import Temp.*;
import Assem.*;
import java.util.*;

public class MipsFrame extends Frame {
	public int allocDown = 0;// ջƫ����
	public ArrayList saveArgs = new ArrayList();// ���ڱ������
	private Temp fp = new Temp(0);
	private Temp sp = new Temp(1);
	private Temp ra = new Temp(2);
	private Temp rv = new Temp(3);
	private Temp zero = new Temp(4);
	public TempList argRegs = new TempList(new Temp(5),
			new TempList(new Temp(6), new TempList(new Temp(7), new TempList(new Temp(8), null))));// �Ĵ���$a0~$a3,Ϊ������������Ĵ���
	private TempList calleeSaves = null;// �Ĵ���$s0~$s7
	public TempList callerSaves = null;// �Ĵ���$t0~$t9
	private int numOfcalleeSaves = 8;// $s�Ĵ���������,��8��

	public MipsFrame() {
		for (int i = 9; i <= 18; i++)
			callerSaves = new TempList(new Temp(i), callerSaves);
		for (int i = 19; i <= 26; i++)
			calleeSaves = new TempList(new Temp(i), calleeSaves);
		// ��ʼ���Ĵ���,��Ϊ��ͳһ���
	}

	public Frame newFrame(Label name, Util.BoolList formals) {
		MipsFrame ret = new MipsFrame();
		ret.name = name; // ����֡����
		TempList argReg = argRegs; // ���������
		for (Util.BoolList f = formals; f != null; f = f.tail, argReg = argReg.tail) {
			// Ϊÿ����������洢�ռ�
			Access a = ret.allocLocal(f.head);
			// ע������ Frame.formals �ͱ��ص� formals, ǰ���� AccessList ����
			ret.formals = new AccessList(a, ret.formals);
			if (argReg != null) {
				ret.saveArgs.add(new Tree.MOVE(a.exp(new Tree.TEMP(fp)), new Tree.TEMP(argReg.head)));
				// ������������Ļ��ָ��,�Ѳ������� frame �� Access ��
			}
		}
		return ret;
	}

	public Access allocLocal(boolean escape) {
		if (escape) {
			// ������,����֡�з���ռ�
			Access ret = new InFrame(this, allocDown);
			allocDown -= Translate.Library.WORDSIZE;
			// ����һ�������ֵĴ洢�ռ�,ע��洢�ռ���������
			return ret;
		} else
			// �������Ĵ�����Ϊ�洢�ռ�
			return new InReg();
	}

	public Tree.Stm procEntryExit1(Tree.Stm body) {
		// Callee ����procEntryExit1 ���������������ָ�� ����ԭfp->������fp->����
		// ra->����Callee-save�Ĵ���->�������->(������ԭָ��)->�ָ�Callee-save�Ĵ���->�ָ����ص�ַ->�ָ�fp
		// �ں�����ԭָ��ǰ���ϱ��������ָ��
		for (int i = 0; i < saveArgs.size(); ++i)
			body = new Tree.SEQ((Tree.MOVE) saveArgs.get(i), body);
		// ����Ϊ���뱣��CalleeSave��ָ��
		Access fpAcc = allocLocal(true);// Ϊ$fp�е�ֵ����ռ�
		Access raAcc = allocLocal(true);// Ϊ$ra�е�ֵ����ռ�
		Access[] calleeAcc = new Access[numOfcalleeSaves];// Ϊ�Ĵ���$s0~$s7����ռ�
		TempList calleeTemp = calleeSaves;// Ϊ�Ĵ����Ĵ���$t0~$t9����ռ�
		for (int i = 0; i < numOfcalleeSaves; ++i, calleeTemp = calleeTemp.tail) {
			// ��calleeSave�Ĵ�������֡�ռ���
			calleeAcc[i] = allocLocal(true);
			body = new Tree.SEQ(new Tree.MOVE(calleeAcc[i].exp(new Tree.TEMP(fp)), new Tree.TEMP(calleeTemp.head)),
					body);
		}
		body = new Tree.SEQ(new Tree.MOVE(raAcc.exp(new Tree.TEMP(fp)), new Tree.TEMP(ra)), body);
		// �� body ǰ����ϱ��淵�ص�ַ $ra ��ָ��
		body = new Tree.SEQ(new Tree.MOVE(new Tree.TEMP(fp), new Tree.BINOP(Tree.BINOP.PLUS, new Tree.TEMP(sp),
				new Tree.CONST(-allocDown - Translate.Library.WORDSIZE))), body);
		// ��$fp=$sp-֡�ռ�+4 bytes
		body = new Tree.SEQ(new Tree.MOVE(fpAcc.expFromStack(new Tree.TEMP(sp)), new Tree.TEMP(fp)), body);
		// �� body ǰ���� fp
		calleeTemp = calleeSaves;
		// �� body ��ָ� callee
		for (int i = 0; i < numOfcalleeSaves; ++i, calleeTemp = calleeTemp.tail)
			body = new Tree.SEQ(body,
					new Tree.MOVE(new Tree.TEMP(calleeTemp.head), calleeAcc[i].exp(new Tree.TEMP(fp))));
		body = new Tree.SEQ(body, new Tree.MOVE(new Tree.TEMP(ra), raAcc.exp(new Tree.TEMP(fp))));
		// body ��ָ����ص�ַ
		body = new Tree.SEQ(body, new Tree.MOVE(new Tree.TEMP(fp), fpAcc.expFromStack(new Tree.TEMP(sp))));
		// body ��ָ� fp
		return body;
	}

	public Assem.InstrList procEntryExit2(Assem.InstrList body) {
		// ������procEntryExit2 ����󱣳ֲ���,������һ����ָ��
		return Assem.InstrList.append(body, new Assem.InstrList(
				new Assem.OPER("", null, new TempList(zero, new TempList(sp, new TempList(ra, calleeSaves)))), null));
	}

	public InstrList procEntryExit3(InstrList body) {
		// ����֡�ռ�:��$sp ��ȥ֡�ռ� (��32byes)
		body = new InstrList(new OPER("subu $sp, $sp, " + (-allocDown), new TempList(sp, null), new TempList(sp, null)),
				body);
		// ���ú�������
		body = new InstrList(new OPER(name.toString() + ":", null, null), body);
		// ��ת�����ص�ַ
		InstrList epilogue = new InstrList(new OPER("jr $ra", null, new TempList(ra, null)), null);
		// ��$sp ������Ӧ��֡�ռ� (��32bytes)
		epilogue = new InstrList(
				new OPER("addu $sp, $sp, " + (-allocDown), new TempList(sp, null), new TempList(sp, null)), epilogue);
		body = InstrList.append(body, epilogue);
		return body;
	}

	public String string(Label label, String value) {
		// �����ַ��������ݶλ�����
		String ret = label.toString() + ": " + System.getProperty("line.separator");
		if (value.equals("\n")) {
			ret = ret + ".word " + value.length() + System.getProperty("line.separator");
			ret = ret + ".asciiz \"" + System.getProperty("line.separator") + "\"";
			return ret;
		}
		ret = ret + ".word " + value.length() + System.getProperty("line.separator");
		ret = ret + ".asciiz \"" + value + "\"";
		return ret;
	}

	// ����$fp\$sp\$ra\$rv�Ĵ���
	public Temp FP() {
		return fp;
	}

	public Temp SP() {
		return sp;
	}

	public Temp RA() {
		return ra;
	}

	public Temp RV() {
		return rv;
	}

	public Tree.Exp externalCall(String func, Tree.ExpList args) {
		// ���ñ�׼�⺯��
		return new Tree.CALL(new Tree.NAME(new Label(func)), args);
	}

	public String tempMap(Temp t) {
		// ������ļĴ���ת��Ϊ�Ĵ�������
		if (t.toString().equals("t0"))
			return "$fp";
		if (t.toString().equals("t1"))
			return "$sp";
		if (t.toString().equals("t2"))
			return "$ra";
		if (t.toString().equals("t3"))
			return "$v0";
		if (t.toString().equals("t4"))
			return "$zero";

		for (int i = 5; i <= 8; i++)
			if (t.toString().equals("t" + i)) {
				int r = i - 5;
				return "$a" + r;
			}
		for (int i = 9; i <= 18; i++)
			if (t.toString().equals("t" + i)) {
				int r = i - 9;
				return "$t" + r;
			}
		for (int i = 19; i <= 26; i++)
			if (t.toString().equals("t" + i)) {
				int r = i - 19;
				return "$s" + r;
			}

		return null;
	}

	public Assem.InstrList codegen(Tree.Stm s) {
		// ����CodeGen���ڵ�codegen����,���ڲ������ָ��
		return (new CodeGen(this)).codegen(s);
	}

	public java.util.HashSet registers() {
		// ���ؼĴ�����
		java.util.HashSet ret = new java.util.HashSet();
		for (TempList tl = this.calleeSaves; tl != null; tl = tl.tail)
			ret.add(tl.head);
		// ��calleeSave�Ĵ��������ϣ��
		for (TempList tl = this.callerSaves; tl != null; tl = tl.tail)
			ret.add(tl.head);
		// ��callerSave�Ĵ��������ϣ��
		return ret;
	}
}
