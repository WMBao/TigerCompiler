package Semant;

import Types.*;

//�������,�˴�Ϊ��ͨ�û����庯��
public class FuncEntry extends Entry {
	RECORD paramlist;// ������
	Type returnTy;// ����ֵ����
	public Translate.Level level;// �����Ĳ�
	public Temp.Label label;// �����ı������

	public FuncEntry(Translate.Level level, Temp.Label label, RECORD p, Type rt) {
		paramlist = p;
		returnTy = rt;
		this.level = level;
		this.label = label;
	}
}
