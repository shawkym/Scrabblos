package auteur;

public interface IClient extends Runnable {

	public char[] getLettres();
	public int getGlobalScore();
	public int getScore();
	public int calcScore();
	public void listen(int port);
	public void process();
	
}
