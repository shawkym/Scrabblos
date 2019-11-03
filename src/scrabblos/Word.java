package scrabblos;

import java.util.ArrayList;

public class Word {
	public final ArrayList<Letter> mot;
	
	public Word(ArrayList<Letter> mot) {
		this.mot = mot;
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

	public ArrayList<Letter> getMot() {
		return mot;
	}
}
