package auteur;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Random;

import javax.crypto.Cipher;
import javax.rmi.CORBA.Util;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator;
import org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.signers.Ed25519Signer;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
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
	AsymmetricCipherKeyPair asymmetricCipherKeyPair = null;
	Ed25519PrivateKeyParameters privateKey = null;
	Ed25519PublicKeyParameters publicKey = null;
//	private KeyPair keyPair;
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
		Security.addProvider(new BouncyCastleProvider());
		SecureRandom random = new SecureRandom();
		Ed25519KeyPairGenerator keyPairGenerator = new Ed25519KeyPairGenerator();
		keyPairGenerator.init(new Ed25519KeyGenerationParameters(random));
		asymmetricCipherKeyPair = keyPairGenerator.generateKeyPair();
		privateKey = (Ed25519PrivateKeyParameters) asymmetricCipherKeyPair.getPrivate();
		publicKey = (Ed25519PublicKeyParameters) asymmetricCipherKeyPair.getPublic();
		keyPublic = Utils.getHexKey(asymmetricCipherKeyPair.getPublic());
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
	
	public void inject_Letter() throws IOException, NoSuchAlgorithmException, InvalidKeyException, JSONException, SignatureException {
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
		Signature s = Signature.getInstance("SHA256");
//		s.initSign(privateKey);
//		s.update(message.getBytes());
//		String signature = Utils.bytesToHex(s.sign());
		return s.toString();
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
	
	public boolean getFullLetterPool() throws IOException, JSONException {
		JSONObject obj = new JSONObject();
		obj.put("get_full_letterpool",JSONObject.NULL);
		String msg = obj.toString();
		long taille = msg.length();
		writer.writeLong(taille);
		writer.write(msg.getBytes("UTF-8"),0,(int)taille);
		
		read();
		return true;
	}
	
	
	public boolean getLetterPoolSince(int p) throws IOException, JSONException {
		JSONObject obj = new JSONObject();
		obj.put("get_letterpool_since",p);
		String msg = obj.toString();
		long taille = msg.length();
		writer.writeLong(taille);
		writer.write(msg.getBytes("UTF-8"),0,(int)taille);
		read();

		return true;
	}
	
	public void next_turn(JSONObject o) throws InvalidKeyException, JSONException, NoSuchAlgorithmException, SignatureException, IOException {
		period = o.getInt("period");
		inject_Letter();
	}

	@Override
	public void run() {
		System.out.print("Scrabblos Auteur Node");
		try {
			
			register();
			listen();
			inject_Letter();
			inject_Letter();
			while(true) {
				read();
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) throws SignatureException, InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException, NoSuchProviderException {
//
		Security.addProvider(new BouncyCastleProvider());
		SecureRandom random = new SecureRandom();
		Ed25519KeyPairGenerator keyPairGenerator = new Ed25519KeyPairGenerator();
		keyPairGenerator.init(new Ed25519KeyGenerationParameters(random));
		AsymmetricCipherKeyPair asymmetricCipherKeyPair = keyPairGenerator.generateKeyPair();
		CipherParameters privateKey =  asymmetricCipherKeyPair.getPrivate();
		Ed25519PublicKeyParameters publicKey = (Ed25519PublicKeyParameters) asymmetricCipherKeyPair.getPublic();
		String ss = Utils.getHexKey(publicKey);
		ss.toString();
		
		String s1 = Utils.hash(Utils.StringToBinairy("a")+Long.toBinaryString(0)+Utils.hash("")+"b7b597e0d64accdb6d8271328c75ad301c29829619f4865d31cc0c550046a08f");
//		System.out.println(s1);
//		Ed25519Signer signer = null;
//		signer.init(true, publicKey);
//		Signature signer = Signature.getInstance("SHA256",BouncyCastleProvider.PROVIDER_NAME);
//		signer.initVerify((PublicKey) asymmetricCipherKeyPair.getPublic());
//		signer.update(data);
//		return signer.verify(sig);
//		String s = Utils.getHexKey(asymmetricCipherKeyPair.getPublic());
		
		// algorithm is pure Ed25519
		Signature sig = Signature.getInstance("SHA256WithDSA");
		sig.initSign((PrivateKey) asymmetricCipherKeyPair.getPrivate());
		sig.update(s1.getBytes());
		byte[] s = sig.sign();
		String signature = Utils.bytesToHex(s);
		System.out.println(s.toString());
		
		
		
	}
}