package Translate;

import Tree.*;

public class Translate {
	private Frag.Frag frags = null;
	public Frame.Frame frame = null;

	// Translate输入一个Frame返回一个段链表
	public Translate(Frame.Frame f) {
		frame = f;
	}

	public Frag.Frag getResult() {
		return frags;
	}

	public void addFrag(Frag.Frag frag) {
		// 增长段列表
		frag.next = frags;
		frags = frag;
	}

	public void procEntryExit(Level level, Exp body, boolean returnValue) {
		Stm b = null;
		if (returnValue) {
			// 若有返回值,则将返回值存入$v0
			b = new MOVE(new TEMP(level.frame.RV()), body.unEx());
		} else
			// 若无返回值,则转换为Nx
			b = body.unNx();
		b = level.frame.procEntryExit1(b);
		// 加入函数的入口和出口代码
		addFrag(new Frag.ProcFrag(b, level.frame));
		// 增加程序段
	}

	public Exp transNoExp() {
		// 产生一条空语句,实际上和nil语句是同一个效果
		return new Ex(new CONST(0));
	}

	public Exp transIntExp(int value) {
		// 翻译整形常数
		return new Ex(new CONST(value));
	}

	public Exp transStringExp(String string) {
		// 翻译字符串常量,产生一个新的数据段
		Temp.Label lab = new Temp.Label();
		addFrag(new Frag.DataFrag(lab, frame.string(lab, string)));
		return new Ex(new NAME(lab));
	}

	public Exp transNilExp() {
		// 翻译nil语句
		return new Ex(new CONST(0));
	}

	public Exp transOpExp(int oper, Exp left, Exp right) {
		// 翻译二元运算
		if (oper >= BINOP.PLUS && oper <= BINOP.DIV)
			return new Ex(new BINOP(oper, left.unEx(), right.unEx()));
		// 加减乘除运算

		return new RelCx(oper, left, right);// 逻辑判断
	}

	public Exp transStringRelExp(Level currentL, int oper, Exp left, Exp right) {
		// 翻译字符串比较运算,调用标准库函数进行比较,然后进行逻辑判断
		Tree.Exp comp = currentL.frame.externalCall("stringEqual",
				new ExpList(left.unEx(), new ExpList(right.unEx(), null)));
		return new RelCx(oper, new Ex(comp), new Ex(new CONST(1)));
	}

	public Exp transAssignExp(Exp lvalue, Exp exp) {
		// 翻译赋值表达式,注意赋值表达式无返回值
		return new Nx(new MOVE(lvalue.unEx(), exp.unEx()));
	}

	public Exp transCallExp(Level currentL, Level dest, Temp.Label name, java.util.ArrayList<Exp> args_value) {
		// 翻译普通函数调用
		ExpList args = null;
		for (int i = args_value.size() - 1; i >= 0; --i) {
			args = new ExpList(((Exp) args_value.get(i)).unEx(), args);
		}
		// 产生实参参数表
		Level l = currentL;
		Tree.Exp currentFP = new TEMP(l.frame.FP());
		while (dest.parent != l) {
			currentFP = l.staticLink().acc.exp(currentFP);
			l = l.parent;
		}
		// 搜索逃逸信息找到静态链所指向的层
		args = new ExpList(currentFP, args);
		// 根据逃逸信息产生逃逸信息,并作为第一个参数即$a0传入函数
		return new Ex(new CALL(new NAME(name), args));
	}

	public Exp transStdCallExp(Level currentL, Temp.Label name, java.util.ArrayList<Exp> args_value) {
		// 翻译调用标准库函数
		ExpList args = null;
		for (int i = args_value.size() - 1; i >= 0; --i)
			args = new ExpList(((Exp) args_value.get(i)).unEx(), args);
		// 与普通函数调用的区别在于标准库函数不存在函数嵌套定义,即不存在静态链
		return new Ex(currentL.frame.externalCall(name.toString(), args));
	}

	public Exp stmcat(Exp e1, Exp e2) {
		// 连接两个表达式,连接后生成无返回值的表达式
		if (e1 == null) {
			if (e2 != null)
				return new Nx(e2.unNx());
			else
				return transNoExp();
		} else if (e2 == null)
			return new Nx(e1.unNx());
		else
			return new Nx(new SEQ(e1.unNx(), e2.unNx()));
	}

	public Exp exprcat(Exp e1, Exp e2) {
		// 连接两个表达式,连接后生成有返回值的表达式
		if (e1 == null) {
			return new Ex(e2.unEx());
		} else {
			return new Ex(new ESEQ(e1.unNx(), e2.unEx()));
		}
	}

	public Exp transRecordExp(Level currentL, java.util.ArrayList<Exp> field) {
		// 调用外部函数 _allocRecord 为记录在 frame 上分配空间,
		// 并得存储空间首地址
		// _allocRecord 执行如下的类 C 代码,注意它只负责分配空间
		// 初始化操作需要我们来完成
		// 以下是runtime.c中的代码
		// # int *allocRecord(int size)
		// # {int i;
		// # int *p, *a;
		// # p = a = (int *)malloc(size);
		// # for(i=0;i<size;i+=sizeof(int)) *p++ = 0;
		// # return a;
		// # }
		// 注意如果记录为空,也要用 1 个 机器字,否则每个域为一个机器字,按顺序存放
		Temp.Temp addr = new Temp.Temp();
		Tree.Exp rec_id = currentL.frame.externalCall("allocRecord",
				new ExpList(new CONST((field.size() == 0 ? 1 : field.size()) * Library.WORDSIZE), null));

		Stm stm = transNoExp().unNx();
		// 初始化指令
		for (int i = field.size() - 1; i >= 0; --i) {
			Tree.Exp offset = new BINOP(BINOP.PLUS, new TEMP(addr), new CONST(i * Library.WORDSIZE));
			Tree.Exp value = (field.get(i)).unEx();
			stm = new SEQ(new MOVE(new MEM(offset), value), stm);
			// 为记录中每个域生成 MOVE 指令,将值复制到帧中的相应区域
		}
		// 返回记录的首地址
		return new Ex(new ESEQ(new SEQ(new MOVE(new TEMP(addr), rec_id), stm), new TEMP(addr)));
	}

	public Exp transArrayExp(Level currentL, Exp init, Exp size) {
		// 调用外部函数 initArray 为数组在 frame 上分配存储空间,并得到
		// 存储空间首地址
		// initArray 执行如下的类 C 代码,需要提供数组大小与初始值
		// # int *initArray(int size, int init)
		// # {int i;
		// # int *a = (int *)malloc(size*sizeof(int));
		// # for(i=0;i<size;i++) a[i]=init;
		// # return a;
		// # }
		Tree.Exp alloc = currentL.frame.externalCall("initArray",
				new ExpList(size.unEx(), new ExpList(init.unEx(), null)));
		return new Ex(alloc);
	}

	public Exp transIfExp(Exp test, Exp e1, Exp e2) {
		// 将if语句翻译为IR树的节点
		return new IfExp(test, e1, e2);
	}

	public Exp transWhileExp(Exp test, Exp body, Temp.Label out) {
		// 将while语句翻译为IR树的节点,注意只可能是Nx
		return new WhileExp(test, body, out);
	}

	public Exp transForExp(Level currentL, Access var, Exp low, Exp high, Exp body, Temp.Label out) {
		// 将for语句翻译为IR树的节点,注意只可能是Nx
		return new ForExp(currentL, var, low, high, body, out);
	}

	public Exp transBreakExp(Temp.Label l) {
		// 翻译break语句为IR树的节点,l为loopstack的栈顶标记
		return new Nx(new JUMP(l));
	}

	public Exp transSimpleVar(Access acc, Level currentL) {
		// 翻译简单变量
		Tree.Exp e = new TEMP(currentL.frame.FP());
		Level l = currentL;
		// 由于可能为外层的变量,故沿着静态链接不断上溯, 直到变量的层与当前层相同
		while (l != acc.home) {
			e = l.staticLink().acc.exp(e);
			l = l.parent;
		}
		return new Ex(acc.acc.exp(e));
	}

	public Exp transSubscriptVar(Exp var, Exp index) {
		// 翻译数组元素
		Tree.Exp arr_addr = var.unEx();
		// 数组首地址
		Tree.Exp arr_offset = new BINOP(BINOP.MUL, index.unEx(), new CONST(Library.WORDSIZE));
		// 由下标得到偏移量
		return new Ex(new MEM(new BINOP(BINOP.PLUS, arr_addr, arr_offset)));
		// 产生指令使首地址加上偏移量为数组元素实际地址
	}

	public Exp transFieldVar(Exp var, int fig) {
		// 翻译域的成员变量
		Tree.Exp rec_addr = var.unEx();
		// 首地址
		Tree.Exp rec_offset = new CONST(fig * Library.WORDSIZE);
		// 偏移量,每个成员占一个机器字
		return new Ex(new MEM(new BINOP(BINOP.PLUS, rec_addr, rec_offset)));
	}
}
