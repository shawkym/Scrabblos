package RadixTree;

public class RadixTreeEdge {
	
	private String cle;
	private RadixTreeNode next;
	
	public RadixTreeEdge(String cle, RadixTreeNode next) {
		this.cle = cle;
		this.next = next;
	}

	public String getCle() {
		return cle;
	}

	public void setCle(String cle) {
		this.cle = cle;
	}

	public RadixTreeNode getNext() {
		return next;
	}

	public void setNext(RadixTreeNode next) {
		this.next = next;
	}
	
	public int prefixe(String s) {
		int pos = 0;
		for (int i = 0; i < s.length(); i++) {
			if(s.charAt(i) == cle.charAt(i)) {
				pos++;
			}
		}
		return pos;
	}
	public int sizeOfPrefix (String s) {
		
		int i = 0;
		while (i < cle.length() && s.charAt(i) == cle.charAt(i)) {
			i++;
		}
		return i;
	}
	
	
	
}
