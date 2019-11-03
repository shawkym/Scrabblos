package scrabble;

import scrabble.Constants;
import scrabble.WordsOnBoard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.JOptionPane;

public class HumanMove implements Constants {
	
	private  ArrayList<HumanAction> actionList;
	Scrabble game;
	private WordsOnBoard wc;

	HumanMove(Scrabble game)
	{
		this.game = game;
		actionList = new ArrayList<HumanAction>();
		wc = new WordsOnBoard(this.game);
	}
	 
	
	public  ArrayList<HumanAction> getInstance(){
		return  actionList;
	}
	
	public  boolean isValid(){
		return hasMovedTiles() && (isRowOrCol()) && !hasGaps() && isJoinedUp() && isProperWord();
	}
	
	private  boolean isJoinedUp(){
		ArrayList<PlayedWord> newWords = new WordsOnBoard(game).getNewWords();
		int letterCount  = 0;
		for(PlayedWord word: newWords) letterCount += word.word.length();
		
		//System.err.println(letterCount + );
		if (letterCount > actionList.size()){
			return true;
		} else{
			JOptionPane.showMessageDialog(null, "Tiles must be joined up with existing tiles");
			return false;
		}
	}
	
	private  boolean isProperWord(){
		if (game.enforeDictionary.isSelected()){
			ArrayList<PlayedWord> newWords = wc.getNewWords();
			for (PlayedWord word: newWords){
				if (!Dictionary.bigTrie.searchWord(word.word)){
					if (JOptionPane.showConfirmDialog(null,"The word '" + word.word + "' does not appear in ScrabbleBot's Dictionary. \n would you like to play it anyway?"  ) == JOptionPane.YES_OPTION){
						
					} else {
						return false;
					}
					//JOptionPane.showOptionDialog(Board.getInstance(), message, title, optionType, messageType, icon, options, initialValue)//(null, "The word '" + word + "' does not appear in ScrabbleBot's Dictionary. Please play a different word or deselect 'enfore dictionary'");
				}
			}
		} 
		return true;
	}
	
	private  boolean hasMovedTiles(){
		if (actionList.size() > 0){
			return true;
		}
		JOptionPane.showMessageDialog(null, "Please move some tiles onto the board before pressing 'Play'");
		return false;
	}
	
	private  boolean isRowOrCol(){
		if (isRow() || isCol() ){
			return true;
		} else {
			JOptionPane.showMessageDialog(null, "Tiles must be all be in the same row or column");
			return false;
		}
	}
	
	private  boolean isRow(){
		int moveRow = actionList.get(0).row;
		for (HumanAction a : actionList){
			if (moveRow != a.row){
				return false;
			}
		}
		return true;
	}
	
	private  boolean isCol(){
		int moveCol = actionList.get(0).col;
		for (HumanAction a : actionList){
			if (moveCol != a.col){
				return false;
			}
		}
		return true;
	}

	private  void sortActions(){
		if (isRow()){
			Collections.sort(actionList, new Comparator<HumanAction>() {
				public int compare(HumanAction a1 , HumanAction a2){
					return a1.col - a2.col;
				}
			});			
		} else if (isCol()){
			Collections.sort(actionList, new Comparator<HumanAction>() {
				public int compare(HumanAction a1 , HumanAction a2){
					return a1.row - a2.row;
				}
			});
		}
	}
	
	private  boolean hasGaps(){
		
		if (actionList.size() <= 1) return false;
		sortActions();		
		if (isRow()){
			int row = actionList.get(0).row;
			for (int col = actionList.get(0).col ; col <= actionList.get(actionList.size()-1).col ; col++){
				if (game.board.tileArr[row][col].letter == ' '){
					JOptionPane.showMessageDialog(null, "Words must not have gaps");
					return true;
				}
			}
		} else if (isCol()){	
			int col = actionList.get(0).col;
			for (int row = actionList.get(0).row ; row <= actionList.get(actionList.size()-1).row ; row++){
				if (game.board.tileArr[row][col].letter == ' '){
					JOptionPane.showMessageDialog(null, "Words must not have gaps");
					return true;
				}
			}
		}
		return false;
	}	
	
	public  void execute(Player player){
		
		ArrayList<PlayedWord> newWords = wc.getNewWords();
		
		for (PlayedWord word : newWords){
			
			if (!Dictionary.bigTrie.searchWord(word.word)){
				game.log.append("??? '" + word.word + "' ??? !\n");
			}
			
			int score = word.score;//Dictionary.getWordScore(word.);
			
			game.log.append(player.name + " plays the word " + word.word + " for " + score + " points\n");
			
			player.awardPoints(score);
		}
		if (player.letterRack.tiles.size() == 0){
			game.log.append("***" + player.name + " scores a BINGO for 50 points! ***    \n");
			player.awardPoints(50);
		}
		
		if (!player.name.equalsIgnoreCase("scrabblebot")){
			for (HumanAction action : actionList){
				action.movedTile.setNormal();
			}
			actionList = new ArrayList<HumanAction>();
		}
		new BonusChecker(game.board.tileArr).RemovePlayedBonuses();
		try {
			player.letterRack.refill();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public  void reverse(){
		
		for (HumanAction action : actionList){
			action.movedTile.setNormal();
			game.user.letterRack.tiles.add(action.movedTile);
			game.board.tileArr[action.row][action.col] = new Tile(' ', 0);
		}
		actionList = new ArrayList<HumanAction>();
	}
}




