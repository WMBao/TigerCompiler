package MIPS;

public class InReg extends Frame.Access {
	private Temp.Temp reg;

	public InReg() {
		reg = new Temp.Temp();
	}

	// �Ĵ���������ν$fp��$sp,���Ƿ����Ǹ��Ĵ���
	public Tree.Exp exp(Tree.Exp framePtr) {
		return new Tree.TEMP(reg);
	}

	public Tree.Exp expFromStack(Tree.Exp stackPtr) {
		return new Tree.TEMP(reg);
	}
}