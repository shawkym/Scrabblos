package scrabblos;

import java.io.IOException;
import java.util.HashMap;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Letter {
	private final char letter;
	private final int period;
	private final String head;
	private final String author;
	private final String signature;
	
	public Letter(char letter, int period, String head, String author, String signature) {
		this.letter = letter;
		this.period = period;
		this.head = head;
		this.author = author;
		// signature a chiffre avec ED255519
		this.signature = signature;
		
	}
	
	public Letter(String data) throws JsonParseException, JsonMappingException, IOException {
		HashMap<String,Object> map =
		        new ObjectMapper().readValue(data, HashMap.class);
		this.letter = (char) map.get("letter");
		this.period = (int) map.get("period");
		this.head = (String) map.get("head");
		this.author = (String) map.get("author");
		this.signature = (String) map.get("signature");
	}
	
	
	public char getLetter() {
		return letter;
	}


	public int getPeriod() {
		return period;
	}


	public String getHead() {
		return head;
	}


	public String getAuthor() {
		return author;
	}


	public String getSignature() {
		return signature;
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
