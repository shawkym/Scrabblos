package scrabble;

import scrabble.AI;
import scrabble.Player;
import scrabble.Tile;
import scrabble.TileBag;
import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import politician.Politican;

public class Scrabble implements Runnable {

	int turnCount;
	JTextArea log;
	JLabel lettersInBag;
	AI ai;
	Player user;
	Player bot;
	Board board;
	Tile blueTile;
	TileBag tileBag;
	ArrayList<PlayedWord> wordList;
	JCheckBox hardMode;
	JCheckBox enforeDictionary;
	Dictionary dico;
	public Politican politican;
	
	public Scrabble(Dictionary dico, Politican politican)
	{
		System.out.println("Init Scrabble UI Client");	
		this.dico =  dico;
		this.politican = politican;
		bot = new Player(this, "Scrabble Miner", true);
		user = new Player(this, "Politician " + politican.id , false);
	}

	public TileBag getTileBag() {
		return tileBag;
	}
	public void setTileBag(TileBag tileBag) {
		this.tileBag = tileBag;
	}

	public void resetInstance()
	{
		board = new Board(this);
		board.complete_board(this);
		
		user.letterRack = new LetterRack(user);
		bot.letterRack = new LetterRack(bot);
	
	}
	
	public void mine()
	{
		ai.makeFirstMove();
	}
	
	public void buildUI(){
		JPanel eastPanel = drawEastPanel();
		//PlayerTiles = new ArrayList<Tile>();
		board = new Board(this);
		board.complete_board(this);
		
		user.letterRack = new LetterRack(user);
		bot.letterRack = new LetterRack(bot);
		
		JFrame f = new JFrame("Scrabblos Politician " + politican.id + " Based on Tom Brennan's ScrabbleBot");

		drawMainFrame(bot, f, board, eastPanel);

	}

	public void beginGame() {

		buildUI();
		ai = new AI(bot);

		wordList = new ArrayList<>();

		boolean moved;
		do{
			moved = ai.makeFirstMove();
		} while (!moved);


		//Board.getInstance().print();

		wordList = new WordsOnBoard(this).getWordList();

		//wordList.forEach(System.outprintln);

		//Board.getInstance().print();
		//Board.getInstance().reDraw();

		//		for (Anchor anchor : ai.findAnchors()){
		//			System.out.println(anchor.toString());
		//		}

		//System.err.println(PlayerTiles.toString());

		//		boolean aiHasMoved = true;
		//		while (aiHasMoved){
		//			aiHasMoved = ai.makeSubsequentMove();
		//		}
	}

	private  JPanel drawEastPanel() {
		lettersInBag = new JLabel("Remaining Letters: ");
		lettersInBag.setHorizontalAlignment(SwingConstants.CENTER);
		lettersInBag.setFont(new Font("Calibri", 1, 30));
		JPanel controls = getControls();

		JPanel eastPanel =  new JPanel();
		log = new JTextArea();
		log.setPreferredSize(new Dimension(250, 700));
		eastPanel.setLayout(new BorderLayout());
		eastPanel.add(lettersInBag, BorderLayout.NORTH);
		eastPanel.add(log, BorderLayout.CENTER);
		eastPanel.add(controls, BorderLayout.SOUTH);
		return eastPanel;
	}

	private  void drawMainFrame(Player bot, JFrame f, Board board, JPanel eastPanel) {
		f.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.add(board, BorderLayout.CENTER);
		f.add(user.display, BorderLayout.SOUTH);
		f.add(bot.display, BorderLayout.NORTH);
		f.add(eastPanel, BorderLayout.EAST);
		f.setSize(1025,980);  
		f.setVisible(true);
		f.setLocation(100, 100);
		f.requestFocus();
	}

	private  JPanel getControls() {

		JPanel controls = new JPanel();
		controls.setLayout(new GridLayout(4, 1));

		JPanel one = new JPanel(new GridLayout(1, 1));


		JPanel two = new JPanel(new GridLayout(1, 2));

		JPanel three = new JPanel(new GridLayout(1, 2));


		JPanel four = new JPanel(new GridLayout(1, 2));
		//four.add(hardMode);
		//four.add(enforeDictionary);
		//enforeDictionary.setSelected(true);


		controls.add(one);
		controls.add(two);
		controls.add(three);
		controls.add(four);

		return controls;
	}

	@Override
	public void run() {
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		beginGame();	
	}
}
