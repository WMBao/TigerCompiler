package Semant;

import Types.*;

//���������
public class VarEntry extends Entry {
	Type Ty;// ��������
	Translate.Access acc;// Ϊ��������Ĵ洢�ռ�
	boolean isFor;// ����Ƿ�Ϊѭ������

	public VarEntry(Type ty, Translate.Access acc) {
		Ty = ty;
		this.acc = acc;
		this.isFor = false;
	}

	public VarEntry(Type ty, Translate.Access acc, boolean isf) {
		Ty = ty;
		this.acc = acc;
		this.isFor = isf;
	}
}
