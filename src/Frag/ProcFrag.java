package Frag;

public class ProcFrag extends Frag {
	public Frame.Frame frame;// ����ε�֡
	public Tree.Stm body;// ������ڵĳ�����
	// �����,��.text��ͷ

	public ProcFrag(Tree.Stm body, Frame.Frame f) {
		this.body = body;
		frame = f;
	}
}
