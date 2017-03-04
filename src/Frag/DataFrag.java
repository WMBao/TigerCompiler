package Frag;

public class DataFrag extends Frag {
	Temp.Label label = null;// 段名称
	public String data = null;// 数据段所含的字符串常量
	// 数据段,用来表示一个字符串常量,以.data开头

	public DataFrag(Temp.Label label, String data) {
		this.label = label;
		this.data = data;
	}
}
