package Translate;

public abstract class Exp {
	// 抽象类,总括其他表达式类的实现
	abstract Tree.Exp unEx();

	abstract Tree.Stm unNx();

	abstract Tree.Stm unCx(Temp.Label t, Temp.Label f);
}
