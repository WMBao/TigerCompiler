package Assem;

public class InstrList {
	public Instr head;
	public InstrList tail;

	public InstrList(Instr h, InstrList t) {
		head = h;
		tail = t;
	}

	static public InstrList append(InstrList i1, InstrList i2) {
		// ������ָ���������һ��,i2��i1֮��
		InstrList t = null;
		for (InstrList t1 = i1; t1 != null; t1 = t1.tail)
			t = new InstrList(t1.head, t);
		// �Ƚ�i1��ת
		InstrList ret = i2;
		for (InstrList t2 = t; t2 != null; t2 = t2.tail)
			ret = new InstrList(t2.head, ret);
		// �ٽ�i1����ӵ�i2������ͷ��
		return ret;
	}
}
