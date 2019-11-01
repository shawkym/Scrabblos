package scrabblos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.fasterxml.jackson.databind.ObjectMapper;

public class LetterPool {
	private int current_period;
	private int next_period;
	private ArrayList<Lettres> lettres;
	
	
	public LetterPool(String data) {
		try {
			HashMap<String, Object> result = new ObjectMapper().readValue(data, HashMap.class);
			this.current_period = (int) result.get("current_period");
			this.next_period = (int) result.get("next_period");
			lettres = new ArrayList<Lettres>();
			for(Object l : ((ArrayList) result.get("letters"))) {
				this.lettres.add(new Lettres(l.toString()));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
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


	@Override
	public String toString() {
		return "LetterPool [current_period=" + current_period + ", next_period=" + next_period + ", letters=" + lettres
				+ "]";
	}


	/**
	 * @return the letters
	 */
	public ArrayList<Lettres> getLetters() {
		return lettres;
	}
}
