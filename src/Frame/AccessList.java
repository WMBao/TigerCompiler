package Frame;

public class AccessList {
	public Access head;
	public AccessList next;

	public AccessList(Access h, AccessList n) {
		head = h;
		next = n;
	}
}
