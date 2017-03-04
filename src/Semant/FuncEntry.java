package Semant;

import Types.*;

//函数入口,此处为普通用户定义函数
public class FuncEntry extends Entry {
	RECORD paramlist;// 参数表
	Type returnTy;// 返回值类型
	public Translate.Level level;// 函数的层
	public Temp.Label label;// 函数的标记名称

	public FuncEntry(Translate.Level level, Temp.Label label, RECORD p, Type rt) {
		paramlist = p;
		returnTy = rt;
		this.level = level;
		this.label = label;
	}
}
