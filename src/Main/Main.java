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
		// ����������
		PrintStream out = new PrintStream(new FileOutputStream(filename.substring(0, filename.length() - 4) + ".s"));
		// �����������ļ�
		Yylex lexer = new Yylex(inp, errorMsg);
		java_cup.runtime.Symbol tok;
		Yylex lexer2 = new Yylex(inp2, errorMsg);
		System.out.println("# �ʷ�������");
		try {
			do {
				tok = lexer2.next_token();
				System.out.println(symnames[tok.sym] + " " + tok.left);
			} while (tok.sym != sym.EOF);
			inp2.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// ����Ϊ�ʷ�����
		System.out.println("\n# �﷨������");
		Exp result = null;
		parser p = new parser(lexer);
		// �﷨����
		// ��ӡ�����﷨��
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
		// �������
		Frag.Frag frags = smt.transProg((Exp) result);

		if (ErrorMsg.anyErrors == false)
			System.out.println("�������﷨����");
		else
			return;
		// �����﷨�����õ��Ķ�,��Ϊ���ݶκͳ����
		irOut = new PrintStream(new FileOutputStream(filename.substring(0, filename.length() - 4) + ".ir"));
		// �����м��ʾ��������ļ�
		out.println(".globl main");
		// ����mainΪȫ�ֱ��
		for (Frag.Frag f = frags; f != null; f = f.next)// �������еĶ�
			if (f instanceof Frag.ProcFrag)
				emitProc(out, (Frag.ProcFrag) f);// ��Ϊ���������Ϊ��.textΪ��ͷ�Ļ�����
			else if (f instanceof Frag.DataFrag)
				out.print("\n.data\n" + ((Frag.DataFrag) f).data);// ��Ϊ���ݶ�����Ϊ��.data��ͷ�����ݻ��ַ���
		BufferedReader runtime = new BufferedReader(new FileReader("runtime.s"));
		while (runtime.ready())
			out.println(runtime.readLine());
		// ��runtime.s�еĿ⺯��������ӵ��ļ�ĩβ
		out.close();
		System.out.println("������������");
		System.exit(0);

	}

	static void emitProc(java.io.PrintStream out, Frag.ProcFrag f) {
		Tree.Print print = new Tree.Print(irOut);
		// ���IR��
		Tree.StmList stms = Canon.Canon.linearize(f.body);
		// IR ���淶����ȥSEQ��ESEQ ��������
		Canon.BasicBlocks b = new Canon.BasicBlocks(stms);
		// ���ݸñ��ֻ�����,ÿ���������в������ڲ���ת�ͱ��
		Tree.StmList traced = (new Canon.TraceSchedule(b)).stms;
		// ������˳�����
		prStmList(print, traced);
		// ��ӡ�淶�����IR��
		Assem.InstrList instrs = codegen(f.frame, traced);
		// ���������
		instrs = f.frame.procEntryExit2(instrs);
		RegAlloc regAlloc = new RegAlloc(f.frame, instrs);
		// �Ĵ�������,���̴���Ϊ:��ָ���б������ͼ,����ͼ�Ļ��Է���,����ͼ���������Ϣ�����ͻͼ,�Գ�ͻͼ������ɫ������Ĵ���
		instrs = f.frame.procEntryExit3(instrs);
		Temp.TempMap tempmap = new Temp.CombineMap(f.frame, regAlloc);
		// ��������MIPSָ��
		out.println("\n.text");
		for (Assem.InstrList p = instrs; p != null; p = p.tail)
			out.println(p.head.format(tempmap));
	}

	// ��ӡ���ʽ�б�
	static void prStmList(Tree.Print print, Tree.StmList stms) {
		for (Tree.StmList l = stms; l != null; l = l.tail)
			print.prStm(l.head);
	}

	// ���м��ʾ���ı��ʽ�б����ָ���б�,��������ε��ø���CodeGen
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
