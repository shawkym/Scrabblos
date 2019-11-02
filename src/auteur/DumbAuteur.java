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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import scrabblos.Scrabblos;

public class DumbAuteur implements IAuteur {

	private final static String server = "localhost";
	private static int port = 12345;
	private Socket connexion = null;
	private boolean is_central = false;
	AsymmetricCipherKeyPair asymmetricCipherKeyPair = null;
	Ed25519PrivateKeyParameters privateKey = null;
	Ed25519PublicKeyParameters publicKey = null;
	private List<Character> letter_bag = new ArrayList<Character>();
	private List<Character> known_injected = new ArrayList<Character>();
	private int global_score = 0;
	private int score = 0;

	public DumbAuteur (Socket s, boolean b)
	{
		this();
		this.connexion = s;
		this.is_central = b;
	}

	public DumbAuteur ()
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
			e.printStackTrace();
		}	
	}

	@Override
	public void run() {
		System.out.println("Scrabblos Auteur Node Searching Authority");
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
			byte[] message = null;
			while(length <=0) {
				Thread.sleep(1000);
				length = connexion.getInputStream().available();
			}
			System.out.println("Got Buffer Size " + length);
			message = new byte[length];
			dIn.readFully(message, 0, message.length); // read the message

			byte[] encodedBytes = Base64.getEncoder().encode(message);
			byte[] decodedBytes = Base64.getDecoder().decode(encodedBytes);
			String ds = new String(decodedBytes,"UTF-8");
			ds = ds.substring(8);
			if (ds.contains("raw"))
			{
				raw_op_handle(ds);
				sync_server();
				return;
			}
			JsonObject request = new JsonParser().parse(ds).getAsJsonObject();
			System.out.println("Got \n" + request.toString() + "\n");
			for (JsonElement j : request.get("letters_bag").getAsJsonArray())
			{
				System.out.println("Adding Letter: "+j.getAsCharacter());
				letter_bag.add(j.getAsCharacter());
			}
			getLettres();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public synchronized char[] getLettres() {
		try {
			Scrabblos.makeMessage("{\"get_full_letterpool\":null}\n",connexion);
			DataInputStream dIn = new DataInputStream(connexion.getInputStream());
			int length = connexion.getInputStream().available();
			byte[] message = null;
			while(length <=0) {
				Thread.sleep(1000);
				length = connexion.getInputStream().available();
			}
			System.out.println("Got Buffer Size " + length);
			message = new byte[length];
			dIn.readFully(message, 0, message.length); // read the message
			byte[] encodedBytes = Base64.getEncoder().encode(message);
			byte[] decodedBytes = Base64.getDecoder().decode(encodedBytes);
			String ds = new String(decodedBytes,"UTF-8");
			ds = ds.substring(8);
			if (ds.contains("raw"))
			{
				raw_op_handle(ds);
				getLettres();
				return null;
			}
			if(ds != null) {
				if (new JsonParser().parse(ds).isJsonNull() == false)
				{
					JsonObject request = new JsonParser().parse(ds).getAsJsonObject();
					System.out.println("Got \n" + request.toString() + "\n");
					if(request != null) {
						JsonObject pool = request.getAsJsonObject("full_letterpool");
						if(pool != null) {
							JsonArray letters = (JsonArray) pool.get("letters");
							for (JsonElement j : letters.getAsJsonArray())
							{
								System.out.println("Adding Letter to pool: "+j.getAsCharacter());
								known_injected.add(j.getAsCharacter());
							}
						}}}}}
		catch (Exception e)
		{
			e.printStackTrace();
		} finally {}
		return null;
	}

	@Override
	public int getGlobalScore() {
		return 0;
	}

	@Override
	public int getScore() {
		return 0;
	}

	@Override
	public int calcScore() {
		return 0;
	}

	@Override
	public void listen() throws IOException {
		Scrabblos.makeMessage("{\"listen\":\"null\"}\n",connexion);
	}

	@Override
	public void process() {

	}

	@Override
	public void stop_listen() throws IOException {
		Scrabblos.makeMessage("{\"stop_listen\":\"null\"}\n",connexion);
	}

	public void raw_op_handle (String req)
	{
		System.out.println("Handling :" + req);
	}
	public static void main (String args[]) throws Exception {
		if (args.length > 0)
			port = Integer.parseInt(args[0]);
		else
			port = 12345;
		System.out.println("Scrabblos Auteur Node Starting...");
		System.out.println("Searching for Authority... On Port " + port);
		new MultiThreadListener(port, DumbAuteur.class).searchAuthority();
		System.out.println("Listening...");
		//new MultiThreadListener(12346, ScrabblosPolitician.class).startListening(); 
	} 
}