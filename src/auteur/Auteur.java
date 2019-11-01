package auteur;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import scrabblos.Block;
import scrabblos.Utils;

public class Auteur implements Runnable {

	private final static String server = "localhost";
	private final static int port = 12345;
	private Socket connexion;
	private DataInputStream reader;
	private DataOutputStream writer;
	private KeyPair keyPair;
	public ArrayList<String> letters;
	private int period;
	private int id;
	private Block block;
	private static int cpt = 0;
	private String keyPublic;
	
	
	public Auteur() throws UnknownHostException, IOException, NoSuchAlgorithmException, NoSuchProviderException {
		connexion = new Socket(server, port);
		id = cpt++;
		period = 0;
		letters = new ArrayList<String>();
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
		keyPairGenerator.initialize(2048,random);
		keyPair = keyPairGenerator.generateKeyPair();
		keyPublic = Utils.getHexKey(keyPair.getPublic());
		block = new Block();
		reader = new DataInputStream(connexion.getInputStream());
		writer = new DataOutputStream(connexion.getOutputStream());
		
	}
	
	public void register() throws IOException {
		JSONObject data = new JSONObject();
		data.put("register", keyPublic);
		String msg = data.toString();
		writer.writeLong(msg.length());
		writer.write(msg.getBytes("UTF-8"),0,msg.length());
		long taille = reader.readLong();
		byte [] cbuf = new byte[(int)taille];
		reader.read(cbuf, 0, (int)taille);
		String s = new String(cbuf,"UTF-8");
		System.out.println("Author "+id+" receive "+s);
		
		JSONObject object = new JSONObject(s);
		JSONArray array  =  (JSONArray) object.get("letters_bag");
		for(int i=0;i<array.length();i++) {
			String x = (String)array.get(i);
			letters.add(x);
		}
	}
	
	public void injectLetter() throws IOException, NoSuchAlgorithmException, InvalidKeyException, JSONException, SignatureException {
		JSONObject data = new JSONObject();
		JSONObject letter = getLetter();
		data.put("inject_letter", letter);
		String msg = data.toString();
		writer.writeLong(msg.length());
		writer.write(msg.getBytes("UTF-8"),0,msg.length());
		block = new Block(letter, block);
	}
	
	public JSONObject getLetter() throws JSONException, NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException, SignatureException {
		Random rand = new Random();
		int alea = rand.nextInt(letters.size());
		String c = letters.remove(alea);
		JSONObject letter = new JSONObject();
		letter.put("letter", c);
		letter.put("period", period);
		letter.put("head", block.getHash());
		letter.put("author", keyPublic);
		String s = Utils.hash(Utils.StringToBinairy(c)+Long.toBinaryString(period)+Utils.hash("")+keyPublic);
		letter.put("signature", signMessage(s));
		return letter;
	}
	

	public String signMessage(String message) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, UnsupportedEncodingException {
		Signature s = Signature.getInstance("SHA256withDSA");
		s.initSign(keyPair.getPrivate());
		s.update(message.getBytes());
		String signature = Utils.bytesToHex(s.sign());
		return signature;
	}
	
	public void listen() throws IOException {
		JSONObject data = new JSONObject();
		data.put("listen",JSONObject.NULL);
		String msg = data.toString();
		long taille = msg.length();
		writer.writeLong(taille);
		writer.write(msg.getBytes("UTF-8"),0,(int)taille);
		
	}
	
	public void read() throws IOException, JSONException {
		long taille_ans = reader.readLong();
		byte [] cbuf = new byte[(int)taille_ans];
		reader.read(cbuf, 0, (int)taille_ans);
		String s = new String(cbuf,"UTF-8");
		System.out.println("Author "+id+" receive "+s);
	}

	@Override
	public void run() {
		System.out.print("Scrabblos Auteur Node");
		try {
			
			register();
			listen();
			injectLetter();
			while(true) {
				read();
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}