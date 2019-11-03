package scrabble;

import java.util.ArrayList;

public class WordsOnBoard implements Constants {

	private Scrabble game;
	private BonusChecker bc;
	
	WordsOnBoard(Scrabble game){
		this.game = game;
		this.bc = new BonusChecker(this.game.board.tileArr);
	}
	
	public  ArrayList<PlayedWord> getNewWords(){
		ArrayList<PlayedWord> old = game.wordList;
		ArrayList<PlayedWord> current = getWordList();
		for (PlayedWord oldWord : old){
			for (PlayedWord curWord : current){
				if (oldWord.word.equals(curWord.word)){
					current.remove(curWord);
					break;
				}
			}	
		}
		
		for (PlayedWord playedWord : current) {
			System.err.println(playedWord.toString());
		}
		System.out.println("----");
		return current;
	}
	
	public  	ArrayList<PlayedWord> getWordList(){
		ArrayList<PlayedWord> words = new ArrayList<>();
		words.addAll(getVerticalWords());
		words.addAll(getHorizontalWords());
		return words;
	}
	
	
	
	private  ArrayList<PlayedWord> getVerticalWords(){
		ArrayList<PlayedWord> vertical = new ArrayList<>();
		Tile[][] TileArr = game.board.tileArr;
		StringBuilder curWord = new StringBuilder();
		int multiplier = 1;
		int score = 0;
		for (int col = 0 ; col < BOARD_DIMENSIONS ; col++){
			for (int row = 0 ; row < BOARD_DIMENSIONS ; row++){
				if (TileArr[row][col].letter == ' '){
					if (curWord.length() > 1){
						vertical.add(new PlayedWord(curWord.toString(), score * multiplier));
					}
					curWord = new StringBuilder();
					multiplier = 1;
					score = 0;
				} else {
					score += TileArr[row][col].points * bc.letterMultiplier(row, col);
					multiplier *= bc.wordMultiplier(row, col);
					curWord.append(TileArr[row][col].letter);
				}
			}
			if (curWord.length() > 1){
				vertical.add(new PlayedWord(curWord.toString(), score * multiplier));
			}
			curWord = new StringBuilder();
			multiplier = 1;
			score = 0;
		}
		return vertical;
	}
	
	private  ArrayList<PlayedWord> getHorizontalWords(){
		ArrayList<PlayedWord> horizontal = new ArrayList<>();
		Tile[][] TileArr = game.board.tileArr;
		StringBuilder curWord = new StringBuilder();
		int multiplier = 1;
		int score = 0;
		for (int row = 0 ; row < BOARD_DIMENSIONS ; row++){
			for (int col = 0 ; col < BOARD_DIMENSIONS ; col++){
				if (TileArr[row][col].letter == ' '){
					if (curWord.length() > 1){
						horizontal.add(new PlayedWord(curWord.toString(), score * multiplier));
					}
					curWord = new StringBuilder();
					multiplier = 1;
					score = 0;
				} else {
					score += TileArr[row][col].points * bc.letterMultiplier(row, col);
					multiplier *= bc.wordMultiplier(row, col);
					curWord.append(TileArr[row][col].letter);
				}
			}
			if (curWord.length() > 1){
				horizontal.add(new PlayedWord(curWord.toString(), score * multiplier));
			}
			curWord = new StringBuilder();
			multiplier = 1;
			score = 0;
		}
		return horizontal;
	}
	
}
