package politician;

import java.io.IOException;

public interface IPolitician {

	public char[] selectWord();
	public int getScore();
	public boolean isValid(char[] word);
	public int getDifficulty();
	public void listen() throws IOException;

}
