package scrabblos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class Word {
	public final ArrayList<Letter> mot;
	
	public Word(ArrayList<Letter> mot) {
		this.mot = mot;
	}
	
	public Word(JsonObject data) throws JsonParseException, JsonMappingException, IOException {
		HashMap<String, Object> map = new Gson().fromJson(data.toString(), HashMap.class);
		this.mot = new ArrayList<Letter>();
		for(Object l : ((ArrayList) map.get("word"))) {
			this.mot.add(new Letter((JsonObject) l));
		}
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
