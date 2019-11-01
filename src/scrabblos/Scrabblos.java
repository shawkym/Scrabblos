package scrabblos;


import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;

public class Scrabblos {

	public static String byteArrayToHex(byte[] a) {
		StringBuilder sb = new StringBuilder(a.length * 2);
		for(byte b: a)
			sb.append(String.format("%02x", b));
		return sb.toString();
	}
	public static void main(String[] args) {

	}
	public static void makeMessage (String cmd, Socket s)
	{
		ByteBuffer ba = ByteBuffer.allocate(cmd.length()+8);
		ba.putLong(cmd.chars().count());
		//ba.flip();
		ba.put(cmd.getBytes());
		try {
			s.getOutputStream().write(ba.array());
			//s.getOutputStream().flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
