package auteur;

import java.io.IOException;

public interface IAuteur extends Runnable {

	public char[] getLettres();
	public int getGlobalScore();
	public int getScore();
	public int calcScore();
	public void listen() throws IOException;
	public void stop_listen() throws IOException;
	public void process();
	
}
