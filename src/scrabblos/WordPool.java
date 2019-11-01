package scrabblos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.fasterxml.jackson.databind.ObjectMapper;

public class WordPool {

	private int current_period;
	private int next_period;
	private ArrayList<Mots> words;
	
	
	public WordPool(String data) {
		try {
			HashMap<String, Object> result = new ObjectMapper().readValue(data, HashMap.class);
			this.current_period = (int) result.get("current_period");
			this.next_period = (int) result.get("next_period");
			words = new ArrayList<Mots>();
			for(Object l : ((ArrayList) result.get("words"))) {
				this.words.add(new Mots(l.toString()));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	@Override
	public String toString() {
		return "WordPool [current_period=" + current_period + ", next_period=" + next_period + ", words=" + words + "]";
	}


	/**
	 * @return the current_period
	 */
	public int getCurrent_period() {
		return current_period;
	}


	/**
	 * @return the next_period
	 */
	public int getNext_period() {
		return next_period;
	}


	/**
	 * @return the words
	 */
	public ArrayList<Mots> getWords() {
		return words;
	}
}
