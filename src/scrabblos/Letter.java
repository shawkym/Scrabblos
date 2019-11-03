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
	/**
	 * 	Calculate Scrabble French Char Score
	 *     
	 *     A,E,I,L,N,O,R,S,T,U : 1 point
	 *     D,G,M : 2 points
	 *     B,C,P : 3 points
	 *     F,H,V : 4 points
	 *     J,Q : 8 points
	 *     K,W,X,Y,Z : 10 points
	 *     Joker : 0 point
	 * 
	 * @return
	 */
	public static int getScore(char b)
	{
		Character c = Character.toUpperCase(b);
		if (c == 'A' || c == 'I' || c== 'E'
				|| c== 'L' || c== 'N' || c=='O'
				|| c== 'R' || c== 'S' || c=='T')
			return 1;
		if (c == 'D' || c=='G' || c=='M')
			return 2;
		if (c == 'B' || c=='C' || c=='P')
			return 3;
		if (c == 'F' || c=='H' || c=='V')
			return 4;
		if (c == 'J' || c=='Q')
			return 8;
		if (c == 'K' || c=='W' || c=='X' || c=='Y' || c=='Z')
			return 10;
		return 0;
	}

}
