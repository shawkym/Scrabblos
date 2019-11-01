package politician;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.StringTokenizer;

public class ScrabblosPolitician implements Runnable {

	private final Socket socket;
	private boolean isAuthorityDelegate = false;
	
	public ScrabblosPolitician(Socket s, boolean isAuthorityDelegate) {
		this.socket = s;
		this.isAuthorityDelegate = isAuthorityDelegate;
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
			if (isAuthorityDelegate)
			{
				// Should Client Register ??
				String cmd2 =   "{\"register\":\"c2e39e952f96d51bd7caeec9441ed7c978c22724e0f0b1f1234182c1bdda36db\"}\n";
				ByteBuffer ba = ByteBuffer.allocate(cmd2.length()+8);
				ba.putLong(cmd2.chars().count());
				//ba.flip();
				ba.put(cmd2.getBytes());
				getSocket().getOutputStream().write(ba.array());
				//out.write(ba.array().toString());
				out.flush();
			}
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
		System.out.println("Searching for Authority...");
		new MultiThreadListener(12346, ScrabblosPolitician.class).searchAuthority();
		System.out.println("Listening...");
		//new MultiThreadListener(12346, ScrabblosPolitician.class).startListening(); 
	} 
}
