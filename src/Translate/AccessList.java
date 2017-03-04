package Translate;

public class AccessList {
	public Access head;
	public AccessList next;

	AccessList(Access head, AccessList accl) {
		this.head = head;
		next = accl;
	}
}
