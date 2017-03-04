package Translate;

import Symbol.Symbol;
import Util.*;

public class Level {
	public Level parent;// ��һ��level
	Frame.Frame frame;// ���ж�Ӧ��֡
	public AccessList formals = null;// ������

	public Level(Level parent, Symbol name, BoolList fmls) {
		// ���캯��,������һ��
		this.parent = parent;
		BoolList bl = new BoolList(true, fmls);
		this.frame = parent.frame.newFrame(new Temp.Label(name), bl);
		for (Frame.AccessList f = frame.formals; f != null; f = f.next)
			this.formals = new AccessList(new Access(this, f.head), this.formals);
	}

	public Level(Frame.Frame frm) {
		// �½�һ��û����һ��Ĳ�
		this.frame = frm;
		this.parent = null;
	}

	public Access staticLink() {
		// ������һ��ľ�̬��,��Ϊ������˵���ǼĴ���$a0
		return formals.head;
	}

	public Access allocLocal(boolean escape) {
		// ����洢�ռ�
		return new Access(this, frame.allocLocal(escape));
	}
}
