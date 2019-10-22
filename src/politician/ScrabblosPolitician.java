package politician;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.StringTokenizer;

public class ScrabblosPolitician implements Runnable {

	private final Socket socket;

	public ScrabblosPolitician(Socket s) {
		this.socket = s;
	}

	public Socket getSocket() {
		return socket;
	}
	
	// Multi-Threaded Listening
	@Override
	public void run() {
		// we manage our particular client connection
		BufferedReader in = null;
		PrintWriter out = null;
		String input = null;
		StringTokenizer request = null;
		try {
			// we read characters from the client via input stream on the socket
			in = new BufferedReader(new InputStreamReader(getSocket().getInputStream()));
			// we get character output stream to client (for reply)
			out = new PrintWriter(getSocket().getOutputStream(),true);
			while(true) {
			// get first line of the request from the client
			 input = in.readLine();
			// we parse the request with a string tokenizer
			if(input != null)
			 request = new StringTokenizer(input);
		
			}
		} catch (IOException e) {
			try {
				getSocket().close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main (String args[]) throws Exception { 
		System.out.println("Scrabblos Politician Node Starting...");
		System.out.println("Listening...");
		new MultiThreadListener(12345, ScrabblosPolitician.class).startListening(); 
	} 
}
