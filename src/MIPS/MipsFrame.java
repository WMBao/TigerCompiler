package MIPS;

import Frame.*;
import Temp.*;
import Assem.*;
import java.util.*;

public class MipsFrame extends Frame {
	public int allocDown = 0;// 栈偏移量
	public ArrayList saveArgs = new ArrayList();// 用于保存参数
	private Temp fp = new Temp(0);
	private Temp sp = new Temp(1);
	private Temp ra = new Temp(2);
	private Temp rv = new Temp(3);
	private Temp zero = new Temp(4);
	public TempList argRegs = new TempList(new Temp(5),
			new TempList(new Temp(6), new TempList(new Temp(7), new TempList(new Temp(8), null))));// 寄存器$a0~$a3,为函数传入参数寄存器
	private TempList calleeSaves = null;// 寄存器$s0~$s7
	public TempList callerSaves = null;// 寄存器$t0~$t9
	private int numOfcalleeSaves = 8;// $s寄存器的数量,共8个

	public MipsFrame() {
		for (int i = 9; i <= 18; i++)
			callerSaves = new TempList(new Temp(i), callerSaves);
		for (int i = 19; i <= 26; i++)
			calleeSaves = new TempList(new Temp(i), calleeSaves);
		// 初始化寄存器,并为其统一编号
	}

	public Frame newFrame(Label name, Util.BoolList formals) {
		MipsFrame ret = new MipsFrame();
		ret.name = name; // 传入帧名称
		TempList argReg = argRegs; // 传入参数表
		for (Util.BoolList f = formals; f != null; f = f.tail, argReg = argReg.tail) {
			// 为每个参数分配存储空间
			Access a = ret.allocLocal(f.head);
			// 注意区分 Frame.formals 和本地的 formals, 前者是 AccessList 类型
			ret.formals = new AccessList(a, ret.formals);
			if (argReg != null) {
				ret.saveArgs.add(new Tree.MOVE(a.exp(new Tree.TEMP(fp)), new Tree.TEMP(argReg.head)));
				// 产生保存参数的汇编指令,把参数放入 frame 的 Access 中
			}
		}
		return ret;
	}

	public Access allocLocal(boolean escape) {
		if (escape) {
			// 若逃逸,则在帧中分配空间
			Access ret = new InFrame(this, allocDown);
			allocDown -= Translate.Library.WORDSIZE;
			// 增加一个机器字的存储空间,注意存储空间向下增长
			return ret;
		} else
			// 否则分配寄存器作为存储空间
			return new InReg();
	}

	public Tree.Stm procEntryExit1(Tree.Stm body) {
		// Callee 经过procEntryExit1 处理后增加了如下指令 保存原fp->计算新fp->保存
		// ra->保存Callee-save寄存器->保存参数->(函数体原指令)->恢复Callee-save寄存器->恢复返回地址->恢复fp
		// 在函数体原指令前加上保存参数的指令
		for (int i = 0; i < saveArgs.size(); ++i)
			body = new Tree.SEQ((Tree.MOVE) saveArgs.get(i), body);
		// 以下为加入保存CalleeSave的指令
		Access fpAcc = allocLocal(true);// 为$fp中的值分配空间
		Access raAcc = allocLocal(true);// 为$ra中的值分配空间
		Access[] calleeAcc = new Access[numOfcalleeSaves];// 为寄存器$s0~$s7分配空间
		TempList calleeTemp = calleeSaves;// 为寄存器寄存器$t0~$t9分配空间
		for (int i = 0; i < numOfcalleeSaves; ++i, calleeTemp = calleeTemp.tail) {
			// 将calleeSave寄存器存入帧空间中
			calleeAcc[i] = allocLocal(true);
			body = new Tree.SEQ(new Tree.MOVE(calleeAcc[i].exp(new Tree.TEMP(fp)), new Tree.TEMP(calleeTemp.head)),
					body);
		}
		body = new Tree.SEQ(new Tree.MOVE(raAcc.exp(new Tree.TEMP(fp)), new Tree.TEMP(ra)), body);
		// 在 body 前面加上保存返回地址 $ra 的指令
		body = new Tree.SEQ(new Tree.MOVE(new Tree.TEMP(fp), new Tree.BINOP(Tree.BINOP.PLUS, new Tree.TEMP(sp),
				new Tree.CONST(-allocDown - Translate.Library.WORDSIZE))), body);
		// 令$fp=$sp-帧空间+4 bytes
		body = new Tree.SEQ(new Tree.MOVE(fpAcc.expFromStack(new Tree.TEMP(sp)), new Tree.TEMP(fp)), body);
		// 在 body 前保存 fp
		calleeTemp = calleeSaves;
		// 在 body 后恢复 callee
		for (int i = 0; i < numOfcalleeSaves; ++i, calleeTemp = calleeTemp.tail)
			body = new Tree.SEQ(body,
					new Tree.MOVE(new Tree.TEMP(calleeTemp.head), calleeAcc[i].exp(new Tree.TEMP(fp))));
		body = new Tree.SEQ(body, new Tree.MOVE(new Tree.TEMP(ra), raAcc.exp(new Tree.TEMP(fp))));
		// body 后恢复返回地址
		body = new Tree.SEQ(body, new Tree.MOVE(new Tree.TEMP(fp), fpAcc.expFromStack(new Tree.TEMP(sp))));
		// body 后恢复 fp
		return body;
	}

	public Assem.InstrList procEntryExit2(Assem.InstrList body) {
		// 函数经procEntryExit2 处理后保持不变,仅增加一条空指令
		return Assem.InstrList.append(body, new Assem.InstrList(
				new Assem.OPER("", null, new TempList(zero, new TempList(sp, new TempList(ra, calleeSaves)))), null));
	}

	public InstrList procEntryExit3(InstrList body) {
		// 分配帧空间:将$sp 减去帧空间 (如32byes)
		body = new InstrList(new OPER("subu $sp, $sp, " + (-allocDown), new TempList(sp, null), new TempList(sp, null)),
				body);
		// 设置函数体标号
		body = new InstrList(new OPER(name.toString() + ":", null, null), body);
		// 跳转到返回地址
		InstrList epilogue = new InstrList(new OPER("jr $ra", null, new TempList(ra, null)), null);
		// 将$sp 加上相应的帧空间 (如32bytes)
		epilogue = new InstrList(
				new OPER("addu $sp, $sp, " + (-allocDown), new TempList(sp, null), new TempList(sp, null)), epilogue);
		body = InstrList.append(body, epilogue);
		return body;
	}

	public String string(Label label, String value) {
		// 产生字符串的数据段汇编代码
		String ret = label.toString() + ": " + System.getProperty("line.separator");
		if (value.equals("\n")) {
			ret = ret + ".word " + value.length() + System.getProperty("line.separator");
			ret = ret + ".asciiz \"" + System.getProperty("line.separator") + "\"";
			return ret;
		}
		ret = ret + ".word " + value.length() + System.getProperty("line.separator");
		ret = ret + ".asciiz \"" + value + "\"";
		return ret;
	}

	// 返回$fp\$sp\$ra\$rv寄存器
	public Temp FP() {
		return fp;
	}

	public Temp SP() {
		return sp;
	}

	public Temp RA() {
		return ra;
	}

	public Temp RV() {
		return rv;
	}

	public Tree.Exp externalCall(String func, Tree.ExpList args) {
		// 调用标准库函数
		return new Tree.CALL(new Tree.NAME(new Label(func)), args);
	}

	public String tempMap(Temp t) {
		// 将传入的寄存器转换为寄存器名称
		if (t.toString().equals("t0"))
			return "$fp";
		if (t.toString().equals("t1"))
			return "$sp";
		if (t.toString().equals("t2"))
			return "$ra";
		if (t.toString().equals("t3"))
			return "$v0";
		if (t.toString().equals("t4"))
			return "$zero";

		for (int i = 5; i <= 8; i++)
			if (t.toString().equals("t" + i)) {
				int r = i - 5;
				return "$a" + r;
			}
		for (int i = 9; i <= 18; i++)
			if (t.toString().equals("t" + i)) {
				int r = i - 9;
				return "$t" + r;
			}
		for (int i = 19; i <= 26; i++)
			if (t.toString().equals("t" + i)) {
				int r = i - 19;
				return "$s" + r;
			}

		return null;
	}

	public Assem.InstrList codegen(Tree.Stm s) {
		// 调用CodeGen类内的codegen函数,用于产生汇编指令
		return (new CodeGen(this)).codegen(s);
	}

	public java.util.HashSet registers() {
		// 返回寄存器表
		java.util.HashSet ret = new java.util.HashSet();
		for (TempList tl = this.calleeSaves; tl != null; tl = tl.tail)
			ret.add(tl.head);
		// 将calleeSave寄存器存入哈希表
		for (TempList tl = this.callerSaves; tl != null; tl = tl.tail)
			ret.add(tl.head);
		// 将callerSave寄存器存入哈希表
		return ret;
	}
}
