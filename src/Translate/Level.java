package Translate;

import Symbol.Symbol;
import Util.*;

public class Level {
	public Level parent;// 上一层level
	Frame.Frame frame;// 层中对应的帧
	public AccessList formals = null;// 参数表

	public Level(Level parent, Symbol name, BoolList fmls) {
		// 构造函数,带入上一层
		this.parent = parent;
		BoolList bl = new BoolList(true, fmls);
		this.frame = parent.frame.newFrame(new Temp.Label(name), bl);
		for (Frame.AccessList f = frame.formals; f != null; f = f.next)
			this.formals = new AccessList(new Access(this, f.head), this.formals);
	}

	public Level(Frame.Frame frm) {
		// 新建一个没有上一层的层
		this.frame = frm;
		this.parent = null;
	}

	public Access staticLink() {
		// 返回这一层的静态链,作为函数来说就是寄存器$a0
		return formals.head;
	}

	public Access allocLocal(boolean escape) {
		// 分配存储空间
		return new Access(this, frame.allocLocal(escape));
	}
}
