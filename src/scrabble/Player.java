package scrabble;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.	swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class Player {
	String name;
	boolean isAI;
	LetterRack letterRack;
	int Score;
	JPanel display;
	JLabel scoreLabel;
	Scrabble game;
	
	public Player(Scrabble game, String name, boolean isAI) {
		super();
		this.name = name;
		this.isAI = isAI;
		this.game = game;
		
		this.Score = 0;
		display = new JPanel();
		//display.setBackground(new Color(0, 120, 0));
		display.setLayout(new BorderLayout());
		//display.add(letterRack.tilePanel, BorderLayout.CENTER);
		
		JLabel nameLabel = new JLabel(name);
		nameLabel.setFont(new Font("Calibri", 1, 30));
		nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
		display.add(nameLabel, BorderLayout.WEST);
		if(isAI == false)
			scoreLabel = new JLabel("Using Miner");
		else
		scoreLabel = new JLabel("Score: " + Score);
		scoreLabel.setFont(new Font("Calibri", 1, 30));
		display.add(scoreLabel, BorderLayout.EAST);

		
	}
	
	void awardPoints(int points){
		Score += points;
		scoreLabel.setText("Score: " + Score);
	}
	

	
	void swapTiles(){
		letterRack.SwapTiles();

		game.log.append(name + " swaps tiles with the bag\n");
	
	}

	public TileBag tileBag() {
		return game.getTileBag();
	}
	
}
