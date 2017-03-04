package MIPS;

public class InReg extends Frame.Access {
	private Temp.Temp reg;

	public InReg() {
		reg = new Temp.Temp();
	}

	// 寄存器中无所谓$fp和$sp,总是返回那个寄存器
	public Tree.Exp exp(Tree.Exp framePtr) {
		return new Tree.TEMP(reg);
	}

	public Tree.Exp expFromStack(Tree.Exp stackPtr) {
		return new Tree.TEMP(reg);
	}
}