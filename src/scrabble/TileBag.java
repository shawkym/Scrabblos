package scrabble;

import java.util.ArrayList;
import java.util.Random;

public class TileBag {
	ArrayList<Tile> TileSet;
	Scrabble game;
	public TileBag(Scrabble scrabble) {
		this.game = scrabble;
	}

	public Tile takeOutTile(){
		if (isEmpty()) return null;
		Random rnd = new Random();
		int idx = rnd.nextInt(TileSet.size());
		Tile tile = TileSet.get(idx);
		TileSet.remove(idx);
		this.game.lettersInBag.setText(TileSet.size() + " tiles left");
		return tile;
	}

	public void AddTile(char c, int s)
	{
		TileSet.add(new Tile(c, s));
	}
	void AddMultipleTiles(char letter, int points ,int quantity){
		for (int i = 0 ; i < quantity ; i++){
			TileSet.add(new Tile(letter, points));
		}
	}

	boolean isEmpty(){
		return (TileSet.size() == 0);
	}

}
