package auteur;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Random;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.Signer;
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator;
import org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.signers.Ed25519Signer;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
	public ArrayList<Character> letters;
	private int period;
	private int id;
	private Block block;
	private static int cpt = 0;


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
		block = new Block();
		reader = new DataInputStream(connexion.getInputStream());
		writer = new DataOutputStream(connexion.getOutputStream());
		letters = new ArrayList<Character>();
	}

	public void register() throws IOException {
		JSONObject data = new JSONObject();
		String key = Utils.bytesToHex(publicKey.getEncoded());
		data.put("register", key);
		String msg = data.toString();
		writer.writeLong(msg.length());
		writer.write(msg.getBytes("UTF-8"),0,msg.length());
		long taille = reader.readLong();
		byte [] cbuf = new byte[(int)taille];
		reader.read(cbuf, 0, (int)taille);
		String s = new String(cbuf,"UTF-8");
		System.out.println("Author "+id+" receive "+s);

		JsonObject x =  new JsonParser().parse(s).getAsJsonObject();
		JsonArray array  =  (JsonArray) x.get("letters_bag");
		for(JsonElement e : array) {
			letters.add(e.getAsCharacter());
		}
	}

	public void inject_Letter() throws IOException, NoSuchAlgorithmException, InvalidKeyException, JSONException, SignatureException, DataLengthException, CryptoException {
		JSONObject data = new JSONObject();
		JSONObject letter = getLetter();
		data.put("inject_letter", letter);
		String msg = data.toString();
		System.out.println(msg);
		writer.writeLong(msg.length());
		writer.write(msg.getBytes("UTF-8"),0,msg.length());
		block = new Block(letter, block);
	}

	public JSONObject getLetter() throws JSONException, NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException, SignatureException, DataLengthException, CryptoException {
		Random rand = new Random();
		int alea = rand.nextInt(letters.size());
		Character c = letters.remove(alea);
		JSONObject letter = new JSONObject();
		letter.put("letter", c);
		letter.put("period", period);
		letter.put("head",  Utils.hash(""));
		letter.put("author", Utils.bytesToHex(publicKey.getEncoded()));
		ByteBuffer bb = ByteBuffer.allocate(8096);
		bb.putChar(c);
		bb.putLong(period);
		bb.put(Utils.hash("").getBytes());
		bb.put(Utils.bytesToHex(publicKey.getEncoded()).getBytes());
		//String s = Utils.hash(Utils.StringToBinairy(c.toString())+Utils.StringToBinairy(new String(bb.array())+Utils.StringToBinairy(Utils.hash(""))+asymmetricCipherKeyPair.getPublic());
		MessageDigest md = MessageDigest.getInstance("SHA256");
		String f = new String (md.digest(bb.array()));
		byte[] sig = signMessage(f);
		letter.put("signature", Utils.bytesToHex(sig));
		return letter;
	}


	public byte[] signMessage(String message) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, UnsupportedEncodingException, DataLengthException, CryptoException {
		Signer signer = new Ed25519Signer();
		signer.init(true, privateKey);
		signer.update(message.getBytes(), 0, message.length());
		byte[] signature = signer.generateSignature();
		return signature;
		//String actualSignature = Base64.getEncoder().encodeToString(signature);
		//return actualSignature;
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

	public void next_turn(JSONObject o) throws InvalidKeyException, JSONException, NoSuchAlgorithmException, SignatureException, IOException, DataLengthException, CryptoException {
		period = o.getInt("period");
		inject_Letter();
	}

	@Override
	public void run() {
		System.out.println("Starting Scrabblos Auteur Node " + id);
		try {

			register();
			listen();
			inject_Letter();
			while(true) {
				read();
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}


	public static void main(String[] args) throws SignatureException, InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, DataLengthException, CryptoException, UnknownHostException, IOException {
		/*
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
		/*
		Signature sig = Signature.getInstance("SHA256WithDSA");
		sig.initSign(asymmetricCipherKeyPair.getPrivate());
		sig.update(s1.getBytes());
		byte[] s = sig.sign();
		String signature = Utils.bytesToHex(s);
		System.out.println(s.toString());
		
		// Generate new signature
		Signer signer = new Ed25519Signer();
		signer.init(true, privateKey);
		signer.update(s1.getBytes(), 0, s1.length());
		byte[] signature = signer.generateSignature();
		var actualSignature = Base64.getUrlEncoder().encodeToString(signature).replace("=", "");

		//System.out.println("Expected signature: {}", expectedSig);
		System.out.println("Actual signature  : {}"+actualSignature);

		//assertEquals(expectedSig, actualSignature);	
		*/
		//new Thread(new Auteur()).start();
		new Thread(new Auteur()).start();
	}
}