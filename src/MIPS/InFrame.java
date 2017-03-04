package MIPS;

import Tree.*;

public class InFrame extends Frame.Access {
	private MipsFrame frame; // ֡
	public int offset;// ֡��ƫ����

	public InFrame(MipsFrame frame, int offset) {
		this.frame = frame;
		this.offset = offset;
	}

	public Tree.Exp exp(Tree.Exp framePtr) {
		// �� fp Ϊ��ʼ��ַ���ر����� IR �����
		return new MEM(new BINOP(BINOP.PLUS, framePtr, new CONST(offset)));
	}

	public Tree.Exp expFromStack(Tree.Exp stackPtr) {
		// �� sp Ϊ��ʼ��ַ���ر����� IR �����
		// fp=sp+֡֡�ռ�+4bytes
		return new MEM(
				new BINOP(BINOP.PLUS, stackPtr, new CONST(offset - frame.allocDown - Translate.Library.WORDSIZE)));
	}

}