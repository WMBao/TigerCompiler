package Frame;

import Temp.*;
import Util.BoolList;

public abstract class Frame implements TempMap {
	// ������֡(���ơ�����������Ϣ)
	public abstract Frame newFrame(Label name, BoolList formals);

	public Label name; // ����
	public AccessList formals = null; // ���ر���(�ֲ���������)�б�

	public abstract Access allocLocal(boolean escape); // �����±��ر���(�Ƿ�����)

	public abstract Tree.Exp externalCall(String func, Tree.ExpList args); // �ⲿ����

	public abstract Temp FP(); // ָ֡��

	public abstract Temp SP(); // ջָ��

	public abstract Temp RA(); // ���ص�ַ

	public abstract Temp RV(); // ����ֵ

	public abstract java.util.HashSet<Temp> registers(); // �Ĵ����б�

	public abstract Tree.Stm procEntryExit1(Tree.Stm body); // ��Ӷ��⺯������ָ��,�� 5.4

	public abstract Assem.InstrList procEntryExit2(Assem.InstrList body); // ͬ��

	public abstract Assem.InstrList procEntryExit3(Assem.InstrList body); // ͬ��

	public abstract String string(Label label, String value);

	public abstract Assem.InstrList codegen(Tree.Stm s); // ���� MIPS ָ����
	// public abstract int wordSize(); //����һ���ֳ�(����Ϊ 4bytes)
	// public abstract TempList colors();
}
