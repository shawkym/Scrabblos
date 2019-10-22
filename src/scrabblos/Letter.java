package scrabblos;

public class Letter {
	private final char letter;
	private final int period;
	private final byte[] head;
	private final byte[] author;
	private final byte[] signature;
	
	public Letter(char letter, int period, byte[] head, byte[] author, byte[] signature) {
		this.letter = letter;
		this.period = period;
		this.head = head;
		this.author = author;
		// signature a chiffre avec ED255519
		this.signature = signature;
		
	}
	
}
