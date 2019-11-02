package scrabblos;

import java.util.ArrayList;

public class Word {
	private final ArrayList<Letter> mot;
	private final byte[] head;
	private final byte[] politician;
	private final byte[] signature;
	
	public Word(ArrayList<Letter> mot, byte[] head, byte[] politician, byte[] signature) {
		this.mot = mot;
		this.head = head;
		this.politician = politician;
		// signature a chiffre avec ED255519
		this.signature = signature;
	}
	
	/**
	 * Calculate score for word
	 */
	public static int getScore(String word)
	{
		int score = 0;
		for (char c : word.toCharArray())
		{
			if (c == '\0' || c == '\n' || c==' ')
				break;
			score += Letter.getScore(c);
		}
		return score;
	}
}
