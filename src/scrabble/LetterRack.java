package scrabble;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class LetterRack implements Constants{
	
	ArrayList<Tile> tiles;
	Player owner;
	
	LetterRack(Player owner){
		this.owner = owner;
		tiles = new ArrayList<Tile>();
		//refill();
	}

	public String readTiles(){
		String s = "";
		for (Tile tile : tiles ){
			if (tile != null){
				s += tile.toString();
			
			}
		}
		//System.out.println(s);
		if (s.isEmpty())
			return "no tiles in bag";
		return s;
	}
	
	public void refill() throws InterruptedException{
		if(owner.game.getTileBag().TileSet == null
				|| owner.game.getTileBag().TileSet.size() == 0)
			{
			Thread.sleep(2000);
			return;
			}
		
		for (Tile t : owner.game.getTileBag().TileSet)
		{
			if(t != null)
			tiles.add(t);
		}
		/*
		while (tiles.size() < TILES_IN_RACK){
			Tile newTile = owner.tileBag().takeOutTile();
			if (newTile == null){
				return;
			}
			tiles.add(newTile);
		}
		//System.out.println("bot has tiles:" + tiles.toString());
		 */
	}
	
	public void SwapTiles(){
		for (int i = 0 ; i < tiles.size() ; i++){
			Tile tile =tiles.get(i) ; 
			if (tile != null){
				owner.tileBag().TileSet.add(tile);
				//tilePanel.remove(tiles.get(0).icon);
			}
		}
		tiles.clear();
		
		try {
			refill();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//readTiles();	
	}

	public void ShuffleTiles(){
		Collections.shuffle(tiles);
	}
	
	public String toString()
	{
		return readTiles();
	}
}
