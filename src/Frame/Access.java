package Frame;

public abstract class Access {
	public abstract Tree.Exp exp(Tree.Exp framePtr);// �� fp Ϊ��ʼ��ַ���ر���

	public abstract Tree.Exp expFromStack(Tree.Exp stackPtr); // �� sp Ϊ��ʼ��ַ���ر���

}
