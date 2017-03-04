package Semant;

import Types.*;

//变量入口类
public class VarEntry extends Entry {
	Type Ty;// 变量类型
	Translate.Access acc;// 为变量分配的存储空间
	boolean isFor;// 标记是否为循环变量

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
