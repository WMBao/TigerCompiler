package Semant;

import Translate.*;
import Types.*;

//������ֵ���͵ı��ʽ��
public class ExpTy {
	Exp exp;// ���ʽ��
	Type ty;// ���ʽ����ֵ����

	ExpTy(Exp e, Type t) {
		exp = e;
		ty = t;
	}
}
