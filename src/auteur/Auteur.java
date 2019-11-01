package auteur;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator;
import org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import scrabblos.Scrabblos;

public class Auteur implements IAuteur {

	private final static String server = "localhost";
	private static int port = 12345;
	private Socket connexion = null;
	private boolean is_central = false;
	AsymmetricCipherKeyPair asymmetricCipherKeyPair = null;
	Ed25519PrivateKeyParameters privateKey = null;
	Ed25519PublicKeyParameters publicKey = null;
	private List<Character> letters = new ArrayList<Character>();

	public Auteur (Socket s, boolean b)
	{
		this();
		this.connexion = s;
		this.is_central = b;
	}

	public Auteur ()
	{
		try {
			Security.addProvider(new BouncyCastleProvider());
			SecureRandom RANDOM = new SecureRandom();
			Ed25519KeyPairGenerator keyPairGenerator = new Ed25519KeyPairGenerator();
			keyPairGenerator.init(new Ed25519KeyGenerationParameters(RANDOM));
			asymmetricCipherKeyPair = keyPairGenerator.generateKeyPair();
			privateKey = (Ed25519PrivateKeyParameters) asymmetricCipherKeyPair.getPrivate();
			publicKey = (Ed25519PublicKeyParameters) asymmetricCipherKeyPair.getPublic();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
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
		System.out.println("Scrabblos Auteur Node");
		try {
			if (connexion == null)
				connexion = new Socket(server, port);
			if (is_central)
				sync_server();
			else
			{

			}
		} catch (IOException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

	}

	private void sync_server() throws NoSuchAlgorithmException {
		try {
			// Register with public key in authority server
			String keymsg =  Scrabblos.byteArrayToHex(publicKey.getEncoded());
			Scrabblos.makeMessage("{\"register\":\""+ keymsg +"\"}\n",connexion);
			// Expect Initial Letters bag				
			DataInputStream dIn = new DataInputStream(connexion.getInputStream());
			int length = connexion.getInputStream().available();
			System.out.println("Got Buffer Size " + length);
			byte[] message = null;
			if(length>0) {
				message = new byte[length];
				dIn.readFully(message, 0, message.length); // read the message
			}
			byte[] encodedBytes = Base64.getEncoder().encode(message);
			byte[] decodedBytes = Base64.getDecoder().decode(encodedBytes);
			String ds = new String(decodedBytes,"UTF-8");
			ds = ds.substring(8);
			JsonObject request = new JsonParser().parse(ds).getAsJsonObject();
			System.out.println("Got \n" + request.toString() + "\n");
			for (JsonElement j : request.get("letters_bag").getAsJsonArray())
			{
				System.out.println("Adding Letter: "+j.getAsCharacter());
				letters.add(j.getAsCharacter());
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
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

	}

	@Override
	public void process() {
		// TODO Auto-generated method stub

	}

	@Override
	public void stop_listen() throws IOException {
		// TODO Auto-generated method stub

	}

	public static void main (String args[]) throws Exception {
		if (args.length > 0)
			port = Integer.parseInt(args[0]);
		else
			port = 12345;
		System.out.println("Scrabblos Auteur Node Starting...");
		System.out.println("Searching for Authority... On Port " + port);
		new MultiThreadListener(port, Auteur.class).searchAuthority();
		System.out.println("Listening...");
		//new MultiThreadListener(12346, ScrabblosPolitician.class).startListening(); 
	} 
}