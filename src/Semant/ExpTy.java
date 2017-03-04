package Semant;

import Translate.*;
import Types.*;

//带返回值类型的表达式类
public class ExpTy {
	Exp exp;// 表达式体
	Type ty;// 表达式返回值类型

	ExpTy(Exp e, Type t) {
		exp = e;
		ty = t;
	}
}
