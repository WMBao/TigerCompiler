package Semant;

import Types.*;

public class LoopVarEntry extends VarEntry {

	public LoopVarEntry(Type ty, Translate.Access acc) {
		super(ty, acc);
	}

	public LoopVarEntry(Type ty, Translate.Access acc, boolean isf) {
		super(ty, acc, isf);
	}
}
