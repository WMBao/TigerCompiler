package FindEscape;

import Absyn.*;
import Symbol.*;

public class FindEscape {
	Table escEnv = new Table();

	void traverseVar(int depth, Var v) {
		if (v == null)
			return;
		else if (v instanceof SimpleVar) {
			Escape escape = (Escape) (escEnv.get(((SimpleVar) v).name));
			if (escape == null)
				System.out.println(((SimpleVar) v).name);
			if (escape.depth < depth)
				escape.setEscape();
		} else if (v instanceof FieldVar) {
			traverseVar(depth, ((FieldVar) v).var);
		} else if (v instanceof SubscriptVar) {
			traverseVar(depth, ((SubscriptVar) v).var);
			traverseExp(depth, ((SubscriptVar) v).index);
		}
	}

	void traverseExp(int depth, Exp e) {
		if (e == null)
			return;
		else if (e instanceof VarExp)
			traverseVar(depth, ((VarExp) e).var);
		else if (e instanceof CallExp) {
			for (ExpList el = ((CallExp) e).args; el != null; el = el.tail)
				traverseExp(depth, el.head);
		} else if (e instanceof OpExp) {
			traverseExp(depth, ((OpExp) e).left);
			traverseExp(depth, ((OpExp) e).right);
		} else if (e instanceof RecordExp)
			for (FieldExpList el = ((RecordExp) e).fields; el != null; el = el.tail)
				traverseExp(depth, el.init);
		else if (e instanceof SeqExp)
			for (ExpList el = ((SeqExp) e).list; el != null; el = el.tail)
				traverseExp(depth, el.head);
		else if (e instanceof AssignExp) {
			traverseVar(depth, ((AssignExp) e).var);
			traverseExp(depth, ((AssignExp) e).exp);
		} else if (e instanceof IfExp) {
			traverseExp(depth, ((IfExp) e).test);
			traverseExp(depth, ((IfExp) e).thenclause);
			traverseExp(depth, ((IfExp) e).elseclause);
		} else if (e instanceof WhileExp) {
			traverseExp(depth, ((WhileExp) e).test);
			traverseExp(depth, ((WhileExp) e).body);
		} else if (e instanceof ForExp) {
			traverseDec(depth, ((ForExp) e).var);
			traverseExp(depth, ((ForExp) e).hi);
			traverseExp(depth, ((ForExp) e).body);
		} else if (e instanceof LetExp) {
			for (DecList dl = ((LetExp) e).decs; dl != null; dl = dl.tail)
				traverseDec(depth, dl.head);
			traverseExp(depth, ((LetExp) e).body);
		} else if (e instanceof ArrayExp) {
			traverseExp(depth, ((ArrayExp) e).size);
			traverseExp(depth, ((ArrayExp) e).init);
		}
	}

	void traverseDec(int depth, Dec d) {
		if (d == null)
			return;
		else if (d instanceof VarDec) {
			traverseExp(depth, ((VarDec) d).init);
			escEnv.put(((VarDec) d).name, new VarEscape(depth, (VarDec) d));
		} else if (d instanceof FunctionDec) {
			for (FunctionDec fd = (FunctionDec) d; fd != null; fd = fd.next) {
				if (fd.inline)
					continue;
				for (FieldList fl = (fd).params; fl != null; fl = fl.tail)
					escEnv.put((fl).name, new FormalEscape(depth + 1, fl));
				traverseExp(depth + 1, (fd).body);
			}
		}
	}

	public void findEscape(Exp e) {
		traverseExp(0, e);
	}
}
