package Frag;

public class DataFrag extends Frag {
	Temp.Label label = null;// ������
	public String data = null;// ���ݶ��������ַ�������
	// ���ݶ�,������ʾһ���ַ�������,��.data��ͷ

	public DataFrag(Temp.Label label, String data) {
		this.label = label;
		this.data = data;
	}
}
