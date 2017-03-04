package Temp;

public class Temp {
	private static int count = 40;
	public int num;

	public String toString() {
		return "t" + num;
	}

	public Temp() {
		num = count++;
	}

	public Temp(int i) {
		num = i;
	}
}
