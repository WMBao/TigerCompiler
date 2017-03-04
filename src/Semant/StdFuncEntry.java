package Semant;

import Types.*;

//继承自普通函数入口类,此为标准库函数入口类
public class StdFuncEntry extends FuncEntry {

	public StdFuncEntry(Translate.Level l, Temp.Label lab, RECORD params, Type rt) {
		super(l, lab, params, rt);
	}
}
