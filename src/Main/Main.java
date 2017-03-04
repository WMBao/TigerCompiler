package Main;

import java.io.*;
import Absyn.*;
import Frame.Frame;
import MIPS.MipsFrame;
import RegAlloc.RegAlloc;
import Semant.*;
import Translate.Translate;
import Tree.StmList;
import tiger.errormsg.ErrorMsg;
import tiger.parse.*;

public class Main {
	static java.io.PrintStream irOut;

	public static void main(String[] argv) throws java.io.IOException {
//		String filename = "Testcases/queens.tig";
		String filename = "Testcases/Official/Bad/test14.tig";
		ErrorMsg errorMsg = new ErrorMsg(filename);
		InputStream inp = new FileInputStream(filename);
		InputStream inp2 = new FileInputStream(filename);
		// 定义输入流
		PrintStream out = new PrintStream(new FileOutputStream(filename.substring(0, filename.length() - 4) + ".s"));
		// 定义输出汇编文件
		Yylex lexer = new Yylex(inp, errorMsg);
		java_cup.runtime.Symbol tok;
		Yylex lexer2 = new Yylex(inp2, errorMsg);
		System.out.println("# 词法分析：");
		try {
			do {
				tok = lexer2.next_token();
				System.out.println(symnames[tok.sym] + " " + tok.left);
			} while (tok.sym != sym.EOF);
			inp2.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 以上为词法分析
		System.out.println("\n# 语法分析：");
		Exp result = null;
		parser p = new parser(lexer);
		// 语法分析
		// 打印抽象语法树
		try {
			result = (Exp) p.parse().value;
		} catch (Exception e) {
			e.printStackTrace();
		}
		Print pr = new Print(System.out);
		pr.prExp((Exp) result, 0);
		System.out.println("\n");

		Frame frame = new MipsFrame();
		Translate translator = new Translate(frame);
		Semant smt = new Semant(translator, errorMsg);
		// 语义分析
		Frag.Frag frags = smt.transProg((Exp) result);

		if (ErrorMsg.anyErrors == false)
			System.out.println("无语义语法错误");
		else
			return;
		// 返回语法分析得到的段,分为数据段和程序段
		irOut = new PrintStream(new FileOutputStream(filename.substring(0, filename.length() - 4) + ".ir"));
		// 定义中间表示树的输出文件
		out.println(".globl main");
		// 定义main为全局标记
		for (Frag.Frag f = frags; f != null; f = f.next)// 遍历所有的段
			if (f instanceof Frag.ProcFrag)
				emitProc(out, (Frag.ProcFrag) f);// 若为程序段则翻译为以.text为开头的汇编代码
			else if (f instanceof Frag.DataFrag)
				out.print("\n.data\n" + ((Frag.DataFrag) f).data);// 若为数据段则翻译为以.data开头的数据或字符串
		BufferedReader runtime = new BufferedReader(new FileReader("runtime.s"));
		while (runtime.ready())
			out.println(runtime.readLine());
		// 将runtime.s中的库函数汇编代码接到文件末尾
		out.close();
		System.out.println("汇编代码已生成");
		System.exit(0);

	}

	static void emitProc(java.io.PrintStream out, Frag.ProcFrag f) {
		Tree.Print print = new Tree.Print(irOut);
		// 输出IR树
		Tree.StmList stms = Canon.Canon.linearize(f.body);
		// IR 树规范化，去SEQ和ESEQ 结点的树表
		Canon.BasicBlocks b = new Canon.BasicBlocks(stms);
		// 根据该表划分基本块,每个基本块中不包含内部跳转和标号
		Tree.StmList traced = (new Canon.TraceSchedule(b)).stms;
		// 基本块顺序放置
		prStmList(print, traced);
		// 打印规范化后的IR树
		Assem.InstrList instrs = codegen(f.frame, traced);
		// 输出汇编代码
		instrs = f.frame.procEntryExit2(instrs);
		RegAlloc regAlloc = new RegAlloc(f.frame, instrs);
		// 寄存器分配,过程大致为:由指令列表产生流图,做流图的活性分析,由流图及其活性信息构造冲突图,对冲突图进行着色法分配寄存器
		instrs = f.frame.procEntryExit3(instrs);
		Temp.TempMap tempmap = new Temp.CombineMap(f.frame, regAlloc);
		// 以下生成MIPS指令
		out.println("\n.text");
		for (Assem.InstrList p = instrs; p != null; p = p.tail)
			out.println(p.head.format(tempmap));
	}

	// 打印表达式列表
	static void prStmList(Tree.Print print, Tree.StmList stms) {
		for (Tree.StmList l = stms; l != null; l = l.tail)
			print.prStm(l.head);
	}

	// 由中间表示树的表达式列表产生指令列表,按层次依次调用各级CodeGen
	static Assem.InstrList codegen(Frame f, StmList stms) {
		Assem.InstrList first = null, last = null;
		for (Tree.StmList s = stms; s != null; s = s.tail) {
			Assem.InstrList i = f.codegen(s.head);
			if (last == null) {
				first = last = i;
			} else {
				while (last.tail != null)
					last = last.tail;
				last = last.tail = i;
			}
		}
		return first;
	}

	static String symnames[] = new String[100];
	static {

		symnames[sym.FUNCTION] = "FUNCTION";
		symnames[sym.EOF] = "EOF";
		symnames[sym.INT] = "INT";
		symnames[sym.GT] = "GT";
		symnames[sym.DIVIDE] = "DIVIDE";
		symnames[sym.COLON] = "COLON";
		symnames[sym.ELSE] = "ELSE";
		symnames[sym.OR] = "OR";
		symnames[sym.NIL] = "NIL";
		symnames[sym.DO] = "DO";
		symnames[sym.GE] = "GE";
		symnames[sym.error] = "error";
		symnames[sym.LT] = "LT";
		symnames[sym.OF] = "OF";
		symnames[sym.MINUS] = "MINUS";
		symnames[sym.ARRAY] = "ARRAY";
		symnames[sym.TYPE] = "TYPE";
		symnames[sym.FOR] = "FOR";
		symnames[sym.TO] = "TO";
		symnames[sym.TIMES] = "TIMES";
		symnames[sym.COMMA] = "COMMA";
		symnames[sym.LE] = "LE";
		symnames[sym.IN] = "IN";
		symnames[sym.END] = "END";
		symnames[sym.ASSIGN] = "ASSIGN";
		symnames[sym.STRING] = "STRING";
		symnames[sym.DOT] = "DOT";
		symnames[sym.LPAREN] = "LPAREN";
		symnames[sym.RPAREN] = "RPAREN";
		symnames[sym.IF] = "IF";
		symnames[sym.SEMICOLON] = "SEMICOLON";
		symnames[sym.ID] = "ID";
		symnames[sym.WHILE] = "WHILE";
		symnames[sym.LBRACK] = "LBRACK";
		symnames[sym.RBRACK] = "RBRACK";
		symnames[sym.NEQ] = "NEQ";
		symnames[sym.VAR] = "VAR";
		symnames[sym.BREAK] = "BREAK";
		symnames[sym.AND] = "AND";
		symnames[sym.PLUS] = "PLUS";
		symnames[sym.LBRACE] = "LBRACE";
		symnames[sym.RBRACE] = "RBRACE";
		symnames[sym.LET] = "LET";
		symnames[sym.THEN] = "THEN";
		symnames[sym.EQ] = "EQ";
	}
}
