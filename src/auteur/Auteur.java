package auteur;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;

public class Auteur implements IAuteyr {

	private final static String server = "localhost";
	private final static int port = 12345;
	private Socket connexion = null;
	private BufferedInputStream reader = null;
	private byte[] public_key;
	private byte[] private_key;
	
//	Client(String host, int port) {
//		try {
//			connexion = new Socket(host, port);
//		}catch (UnknownHostException e) {
//			e.printStackTrace();
//		}catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
	
//	public static void main (String[] args)
//	{
//		new Thread(new Client()).start();
//	}

	@Override
	public void run() {
		System.out.print("Scrabblos Auteur Node");
		try {
			connexion = new Socket(server, port);
			reader = new BufferedInputStream(connexion.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
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
	public void listen() throws IOException {
		String response = "";
		int stream;
		byte[] b = new byte[4096];
		stream = reader.read(b);
		response = new String(b, 0, stream);
	}

	@Override
	public void process() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop_listen() throws IOException {
		// TODO Auto-generated method stub
		
	}
}