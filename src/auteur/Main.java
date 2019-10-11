package auteur;

public class Main implements IClient {

	public final static String server = "localhost";
	public final static int port = 5555;
	
	public static void main (String[] args)
	{
		new Thread(new Main()).start();
	}

	@Override
	public void run() {
		System.out.print("Scrabblos Auteur Node");
		
	}

	@Override
	public char[] getLettres() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getGlobalScore() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getScore() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int calcScore() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void listen(int port) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void process() {
		// TODO Auto-generated method stub
		
	}
}