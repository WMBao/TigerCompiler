package MIPS;

import Tree.*;

public class InFrame extends Frame.Access {
	private MipsFrame frame; // 帧
	public int offset;// 帧中偏移量

	public InFrame(MipsFrame frame, int offset) {
		this.frame = frame;
		this.offset = offset;
	}

	public Tree.Exp exp(Tree.Exp framePtr) {
		// 以 fp 为起始地址返回变量的 IR 树结点
		return new MEM(new BINOP(BINOP.PLUS, framePtr, new CONST(offset)));
	}

	public Tree.Exp expFromStack(Tree.Exp stackPtr) {
		// 以 sp 为起始地址返回变量的 IR 树结点
		// fp=sp+帧帧空间+4bytes
		return new MEM(
				new BINOP(BINOP.PLUS, stackPtr, new CONST(offset - frame.allocDown - Translate.Library.WORDSIZE)));
	}

}