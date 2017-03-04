package Semant;

import Types.*;
import Symbol.*;
import Translate.Level;
import Util.BoolList;
import tiger.errormsg.ErrorMsg;

public class Env {
	Table vEnv = null; //入口符号表,记录了变量和函数的入口
	Table tEnv = null; //类型符号表,记录了变量的类型
	Level root = null;
	ErrorMsg errorMsg = null;//传入 main 函数的层，添加库函数时使用
	java.util.HashSet<Symbol> stdFuncSet = new java.util.HashSet<Symbol>(); //记录标准库函数名的哈希表
	
	Env(ErrorMsg err, Level l)
	{
		errorMsg = err;
		root = l;
		initTEnv();
		initVEnv();//初始化两个符号表
	}
	public void initTEnv() 
	{
		//初始化类型符号表,加入int和string这两个默认类型
		tEnv = new Table();
		tEnv.put(Symbol.symbol("int"), new INT());
		tEnv.put(Symbol.symbol("string"), new STRING());
	}
	public void initVEnv() 
	{
		//初始化入口符号表,添加标准库函数,库函数的实现为runtime.s中的汇编代码
		vEnv = new Table();
		
		Symbol sym = null; 
		RECORD formals = null; 
		Type result = null; 
		Level level = null;
		
		sym = Symbol.symbol("allocRecord");
		formals = new RECORD(Symbol.symbol("size"), new INT(), null);
		result = new INT();
		level = new Level(root, sym, new BoolList(true, null));
		vEnv.put(sym, new StdFuncEntry(level, new Temp.Label(sym), formals, result));
		stdFuncSet.add(sym);
		
		sym = Symbol.symbol("initArray");
		formals = new RECORD(Symbol.symbol("size"), new INT(), new RECORD(Symbol.symbol("init"), new INT(), null));
		result = new INT();
		level = new Level(root, sym, new BoolList(true, new BoolList(true, null)));
		vEnv.put(sym, new StdFuncEntry(level, new Temp.Label(sym), formals, result));
		stdFuncSet.add(sym);
		
		sym = Symbol.symbol("print");
		formals = new RECORD(Symbol.symbol("str"), new STRING(), null);
		result = new VOID();
		level = new Level(root, sym, new BoolList(true, null));
		vEnv.put(sym, new StdFuncEntry(level, new Temp.Label(sym), formals, result));
		stdFuncSet.add(sym);
		
		sym = Symbol.symbol("flush");
		formals = null;
		result = new VOID();
		level = new Level(root, sym, null);
		vEnv.put(sym, new StdFuncEntry(level, new Temp.Label(sym), formals, result));
		stdFuncSet.add(sym);
		
		sym = Symbol.symbol("getchar");
		formals = null;
		result = new STRING();
		level = new Level(root, sym, null);
		vEnv.put(sym, new StdFuncEntry(level, new Temp.Label(sym), formals, result));
		stdFuncSet.add(sym);
		
		sym = Symbol.symbol("ord");
		formals = new RECORD(Symbol.symbol("str"), new STRING(), null);
		result = new INT();
		level = new Level(root, sym, new BoolList(true, null));
		vEnv.put(sym, new StdFuncEntry(level, new Temp.Label(sym), formals, result));
		stdFuncSet.add(sym);
		
		sym = Symbol.symbol("chr");
		formals = new RECORD(Symbol.symbol("i"), new INT(), null);
		result = new STRING();
		level = new Level(root, sym, new BoolList(true, null));
		vEnv.put(sym, new StdFuncEntry(level, new Temp.Label(sym), formals, result));
		stdFuncSet.add(sym);
		
		sym = Symbol.symbol("size");
		formals = new RECORD(Symbol.symbol("str"), new STRING(), null);
		result = new INT();
		level = new Level(root, sym, new BoolList(true, null));
		vEnv.put(sym, new StdFuncEntry(level, new Temp.Label(sym), formals, result));
		stdFuncSet.add(sym);
		
		sym = Symbol.symbol("substring");
		formals = new RECORD(Symbol.symbol("n"), new INT(), null);
		formals = new RECORD(Symbol.symbol("first"), new INT(), formals);
		formals = new RECORD(Symbol.symbol("str"), new STRING(), formals);
		result = new STRING();
		level = new Level(root, sym, new BoolList(true, new BoolList(true, new BoolList(true, null))));
		vEnv.put(sym, new StdFuncEntry(level, new Temp.Label(sym), formals, result));
		stdFuncSet.add(sym);
		
		sym = Symbol.symbol("concat");
		formals = new RECORD(Symbol.symbol("str2"), new STRING(), null);
		formals = new RECORD(Symbol.symbol("str1"), new STRING(), formals);
		result = new STRING();
		level = new Level(root, sym, new BoolList(true, new BoolList(true, null)));
		vEnv.put(sym, new StdFuncEntry(level, new Temp.Label(sym), formals, result));
		stdFuncSet.add(sym);
		
		sym = Symbol.symbol("not");
		formals = new RECORD(Symbol.symbol("j"), new INT(), null);
		result = new INT();
		level = new Level(root, sym, new BoolList(true, null));
		vEnv.put(sym, new StdFuncEntry(level, new Temp.Label(sym), formals, result));
		stdFuncSet.add(sym);
		
		sym = Symbol.symbol("exit");
		formals = new RECORD(Symbol.symbol("k"), new INT(), null);
		result = new VOID();
		level = new Level(root, sym, new BoolList(true, null));
		vEnv.put(sym, new StdFuncEntry(level, new Temp.Label(sym), formals, result));
		stdFuncSet.add(sym);
		
		sym = Symbol.symbol("printi");
		formals = new RECORD(Symbol.symbol("i"), new INT(), null);
		result = new VOID();
		level = new Level(root, sym, new BoolList(true, null));
		vEnv.put(sym, new StdFuncEntry(level, new Temp.Label(sym), formals, result));
		stdFuncSet.add(sym);
		
		
		sym = Symbol.symbol("arr");
		Translate.Access acc = root.allocLocal(true);
		vEnv.put(sym, new VarEntry(new ARRAY(new INT()), acc));
		
	}
}
