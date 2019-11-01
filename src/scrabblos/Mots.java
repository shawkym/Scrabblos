package scrabblos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Mots {
	private ArrayList<Lettres> word;
	private String head;
	private String politician;
	private String signature;
	
	public Mots(String data) {
		try {
			HashMap<String, Object> result = new ObjectMapper().readValue(data, HashMap.class);
			word = new ArrayList<Lettres>();
			for(Object l : ((ArrayList) result.get("word"))) {
				this.word.add(new Lettres(l.toString()));
			}
			this.head = (String) result.get("head");
			this.politician = (String) result.get("politician");
			this.signature = (String) result.get("signature");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return "Mots [word=" + word + ", head=" + head + ", politician=" + politician + ", signature=" + signature
				+ "]";
	}

	/**
	 * @return the word
	 */
	public ArrayList<Lettres> getWord() {
		return word;
	}

	/**
	 * @return the head
	 */
	public String getHead() {
		return head;
	}

	/**
	 * @return the politician
	 */
	public String getPolitician() {
		return politician;
	}

	/**
	 * @return the signature
	 */
	public String getSignature() {
		return signature;
	}
}
