package scrabblos;

import java.io.IOException;
import java.util.HashMap;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Lettres {
	private char letter;
	private int period;
	private String head;
	private String author;
	private String signature;
	
	
	public Lettres(char letter, int period, String head, String author, String signature) {
		this.letter = letter;
		this.period = period;
		this.head = head;
		this.author = author;
		this.signature = signature;
	}

	public Lettres(String data) {
		try {
			HashMap<String, Object> result = new ObjectMapper().readValue(data, HashMap.class);
		
			this.letter = (char) result.get("letter");
			this.period = (int) result.get("period");
			this.head = (String) result.get("head");
			this.author = (String) result.get("author");
			this.signature = (String) result.get("signature");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * @return the letter
	 */
	public char getLetter() {
		return letter;
	}

	/**
	 * @return the period
	 */
	public int getPeriod() {
		return period;
	}

	/**
	 * @return the head
	 */
	public String getHead() {
		return head;
	}

	/**
	 * @return the author
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * @return the signature
	 */
	public String getSignature() {
		return signature;
	}

	@Override
	public String toString() {
		return "Letter [letter=" + letter + ", period=" + period + ", head=" + head + ", author=" + author
				+ ", signature=" + signature + "]";
	}
	
	
	
}
