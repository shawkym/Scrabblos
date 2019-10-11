package politician;

public interface IPolitician extends Runnable{

	public char[] selectWord();
	public int getScore();
	public boolean isValid(char[] word);
	public int getDifficulty();
	public void listen();

}
