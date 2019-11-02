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

public class Auteur implements Runnable, IAuteur {

	// Network
	private final static String server = "localhost";
	private final static int port = 12345;
	private Socket socket;
	private DataInputStream reader;
	private DataOutputStream writer;
	// Crypto
	AsymmetricCipherKeyPair asymmetricCipherKeyPair = null;
	Ed25519PrivateKeyParameters privateKey = null;
	Ed25519PublicKeyParameters publicKey = null;
	// Logic;
	public ArrayList<Character> letterBag;
	public ArrayList<Character> letterPool;
	private long period;
	private int id;
	private Block block;
	private boolean is_nextTurn;
	private static int next_auteur_id = 0;

	/**
	 * Creates a new client and generates random keys
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 */
	public Auteur() throws UnknownHostException, IOException, NoSuchAlgorithmException, NoSuchProviderException {
		socket = new Socket(server, port);
		id = next_auteur_id++;
		period = 0;		
		is_nextTurn = true;
		Security.addProvider(new BouncyCastleProvider());
		SecureRandom random = new SecureRandom();
		Ed25519KeyPairGenerator keyPairGenerator = new Ed25519KeyPairGenerator();
		keyPairGenerator.init(new Ed25519KeyGenerationParameters(random));
		asymmetricCipherKeyPair = keyPairGenerator.generateKeyPair();
		privateKey = (Ed25519PrivateKeyParameters) asymmetricCipherKeyPair.getPrivate();
		publicKey = (Ed25519PublicKeyParameters) asymmetricCipherKeyPair.getPublic();
		block = new Block();
		reader = new DataInputStream(socket.getInputStream());
		writer = new DataOutputStream(socket.getOutputStream());
		letterBag = new ArrayList<Character>();
		letterPool = new ArrayList<Character>();
	}


	/** 
	 * Register this client on server authority 
	 * 
	 * @throws IOException
	 * @throws CryptoException 
	 * @throws NoSuchProviderException 
	 * @throws SignatureException 
	 * @throws NoSuchAlgorithmException 
	 * @throws JSONException 
	 * @throws DataLengthException 
	 * @throws InvalidKeyException 
	 */
	@Override
	public void registerOnServer() throws IOException, InvalidKeyException, DataLengthException, JSONException, NoSuchAlgorithmException, SignatureException, NoSuchProviderException, CryptoException {
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
		System.out.println("Author "+id+" "+s);

		JsonObject x =  new JsonParser().parse(s).getAsJsonObject();
		JsonArray array  =  (JsonArray) x.get("letters_bag");
		System.out.println(array.toString());
		for(JsonElement e : array) {
			letterBag.add(e.getAsCharacter());
		}
	}

	/** 
	 * Pseudo mine next block 
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws JSONException
	 * @throws SignatureException
	 * @throws DataLengthException
	 * @throws CryptoException
	 * @throws NoSuchProviderException
	 */
	@Override
	public void injectLetter() throws IOException, NoSuchAlgorithmException, InvalidKeyException, JSONException, SignatureException, DataLengthException, CryptoException, NoSuchProviderException {
		getFullLetterPool();
		if(is_nextTurn) {
			JSONObject data = new JSONObject();
			JSONObject letter = getLetter();
			data.put("inject_letter", letter);
			String msg = data.toString();
			System.out.println("Author " + id + " "+ msg);
			writer.writeLong(msg.length());
			writer.write(msg.getBytes("UTF-8"),0,msg.length());
			block = new Block(letter, block);
			is_nextTurn = false;
		}else {
			System.out.println("Wait for next turn.");
		}
	}

	/**
	 * Get a letter from letter_bag and prepare it for injection
	 * and adds it to the current letter_pool
	 * @return JSONObject containing a letter
	 * @throws JSONException
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException
	 * @throws InvalidKeyException
	 * @throws SignatureException
	 * @throws DataLengthException
	 * @throws CryptoException
	 * @throws NoSuchProviderException
	 */
	@Override
	public JSONObject getLetter() throws JSONException, NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException, SignatureException, DataLengthException, CryptoException, NoSuchProviderException {
		Random rand = new Random();
		int alea = rand.nextInt(letterBag.size());
		Character c = letterBag.remove(alea);
		letterPool.add(c);
		JSONObject letter = new JSONObject();
		letter.put("letter", c);
		letter.put("period", period);
		letter.put("head",  block.getHash());
		letter.put("author", Utils.getHexKey(publicKey));
		String f = Utils.hash(Utils.StringToBinairy(c.toString())+Long.toBinaryString(period)+Utils.hash("")+Utils.getHexKey(publicKey));
		byte[] sig = signMessage(f);
		letter.put("signature", Utils.bytesToHex(sig));
//		System.out.println("verif \n"+verify(publicKey, f.getBytes(), sig));
		return letter;
	}

	/**
	 * Signs a message using Ed25519Signer
	 * @param  a message to sign
	 * @return array of bytes containing digital signature
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws SignatureException
	 * @throws UnsupportedEncodingException
	 * @throws DataLengthException
	 * @throws CryptoException
	 */
	@Override
	public byte[] signMessage(String message) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, UnsupportedEncodingException, DataLengthException, CryptoException {
		Signer signer = new Ed25519Signer();
		signer.init(true, privateKey);
		signer.update(message.getBytes(), 0, message.length());
		byte[] signature = signer.generateSignature();
		return signature;
	}
	
	public static boolean verify(Ed25519PublicKeyParameters publicKey, byte[] message, byte[] signature)
	{	
		Signer verifier = new Ed25519Signer();
        verifier.init(false, publicKey);
        verifier.update(message, 0, message.length);
        return verifier.verifySignature(signature);
	}

	/**
	 * Inform the server authority we're reading update regularly 
	 * @throws IOException
	 */
	@Override
	public void listen() throws IOException {
		JSONObject data = new JSONObject();
		data.put("listen",JSONObject.NULL);
		String msg = data.toString();
		long taille = msg.length();
		writer.writeLong(taille);
		writer.write(msg.getBytes("UTF-8"),0,(int)taille);
	}

	/**
	 * Read Incoming messages from authority server
	 * @throws IOException
	 * @throws JSONException
	 * @throws InvalidKeyException
	 * @throws DataLengthException
	 * @throws NoSuchAlgorithmException
	 * @throws SignatureException
	 * @throws NoSuchProviderException
	 * @throws CryptoException
	 */
	@Override
	public void read() throws IOException, JSONException, InvalidKeyException, DataLengthException, NoSuchAlgorithmException, SignatureException, NoSuchProviderException, CryptoException {
		long taille = reader.readLong();
		byte [] cbuf = new byte[(int)taille];
		reader.read(cbuf, 0, (int)taille);
		String s = new String(cbuf,"UTF-8");
		System.out.println("Author "+id+" "+s);
		JSONObject o = new JSONObject(s);
		
		//JsonObject myo =  (JsonObject) new JsonParser().parse(s);
		
		if (s.contains("full_letterpool"))
			parseLetterPool(o);
		if (s.contains("next_turn"))
			nextTurn(o);
		if (s.contains("inject_letter"))
			injected_letter(o);
	}

	/**
	 * Parse injected_letter message 
	 * @param injected_letter JSONObject contatining message  
	 */
	private void injected_letter(JSONObject o) {
		char c = ((JSONObject) o.get("inject_letter")).getString("letter").charAt(0);
		letterPool.add(c);
	}

	/**
	 * Parse letter_pool message 
	 * @param letterPool JSONObject contatining message 
	 */
	private void parseLetterPool(JSONObject x) {
		JSONObject j = (JSONObject) x.get("full_letterpool");
		Integer fperiod = (Integer) j.get("current_period");
		period = Long.parseLong(fperiod.toString());
		JsonObject lettersj =  new JsonParser().parse(x.toString()).getAsJsonObject();
		lettersj = (JsonObject) lettersj.get("full_letterpool");
		JsonElement letters = lettersj.get("letters");
		for (JsonElement l : letters.getAsJsonArray())
		{
			JsonObject o = (JsonObject) ((JsonArray)l).get(1);
			letterPool.add(o.get("letter").getAsCharacter());
		}
	}

	/**
	 * Get Full Letter Pool from Server
	 * @return true when successful 
	 * @throws IOException
	 * @throws JSONException
	 */
	@Override
	public void getFullLetterPool() throws IOException, JSONException {
		JSONObject obj = new JSONObject();
		obj.put("get_full_letterpool",JSONObject.NULL);
		String msg = obj.toString();
		long taille = msg.length();
		writer.writeLong(taille);
		writer.write(msg.getBytes("UTF-8"),0,(int)taille);
	}

	/**
	 * Get letter pool since period p
	 * @param p : last known period 
	 * @return true when successful 
	 * @throws IOException
	 * @throws JSONException
	 */
	@Override
	public boolean getLetterPoolSince(int p) throws IOException, JSONException {
		JSONObject obj = new JSONObject();
		obj.put("get_letterpool_since",p);
		String msg = obj.toString();
		long taille = msg.length();
		writer.writeLong(taille);
		writer.write(msg.getBytes("UTF-8"),0,(int)taille);
		return true;
	}

	/**
	 * Parse next_turn message and start mining
	 * @param o JSONObject of next_turn message
	 * @throws InvalidKeyException
	 * @throws JSONException
	 * @throws NoSuchAlgorithmException
	 * @throws SignatureException
	 * @throws IOException
	 * @throws DataLengthException
	 * @throws CryptoException
	 * @throws NoSuchProviderException
	 */
	@Override
	public void nextTurn(JSONObject o) throws InvalidKeyException, JSONException, NoSuchAlgorithmException, SignatureException, IOException, DataLengthException, CryptoException, NoSuchProviderException {
		period = o.getInt("next_turn");
		if (letterBag.isEmpty())
			return;
		is_nextTurn = true;
		injectLetter();
	}

	/**
	 * Main Loop of Auteur
	 */
	@Override
	public void run() {
		System.out.println("Starting Scrabblos Auteur Node " + id);
		try {

			registerOnServer();
			listen();
			getFullLetterPool();
			injectLetter();
			while(true) {
				read();
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Static Main for testing 
	 * @param args
	 * @throws SignatureException
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 * @throws DataLengthException
	 * @throws CryptoException
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public static void main(String[] args) throws SignatureException, InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, DataLengthException, CryptoException, UnknownHostException, IOException {

//		Security.addProvider(new BouncyCastleProvider());
//		SecureRandom random = new SecureRandom();
//		Ed25519KeyPairGenerator keyPairGenerator = new Ed25519KeyPairGenerator();
//		keyPairGenerator.init(new Ed25519KeyGenerationParameters(random));
//		AsymmetricCipherKeyPair asymmetricCipherKeyPair = keyPairGenerator.generateKeyPair();
//		Ed25519PrivateKeyParameters privateKey = (Ed25519PrivateKeyParameters) asymmetricCipherKeyPair.getPrivate();
//		Ed25519PublicKeyParameters publicKey = (Ed25519PublicKeyParameters) asymmetricCipherKeyPair.getPublic();
//		System.out.println(Utils.bytesToHex(publicKey.getEncoded()));
//		System.out.println(Utils.getHexKey(publicKey));


//		Security.addProvider(new BouncyCastleProvider());
//		SecureRandom random = new SecureRandom();
//		Ed25519KeyPairGenerator keyPairGenerator = new Ed25519KeyPairGenerator();
//		keyPairGenerator.init(new Ed25519KeyGenerationParameters(random));
//		AsymmetricCipherKeyPair asymmetricCipherKeyPair = keyPairGenerator.generateKeyPair();
//		Ed25519PrivateKeyParameters privateKey = (Ed25519PrivateKeyParameters) asymmetricCipherKeyPair.getPrivate();
//		Ed25519PublicKeyParameters publicKey = (Ed25519PublicKeyParameters) asymmetricCipherKeyPair.getPublic();
		
//		String message = Utils.hash(Utils.StringToBinairy("a")+Long.toBinaryString(0)+Utils.hash("")+"b7b597e0d64accdb6d8271328c75ad301c29829619f4865d31cc0c550046a08f");
//		Signer signer = new Ed25519Signer();
//		signer.init(true, privateKey);
//		signer.update(message.getBytes(), 0, message.length());
//		byte[] signature = signer.generateSignature();
//		String sss = Utils.bytesToHex(signature);
//		System.out.println("bytestoHex:\n"+sss);
//		System.out.println("verif\n"+verify(publicKey, message.getBytes(), signature));
//		
//		ByteBuffer bb = ByteBuffer.allocate(8096);
//		bb.putChar('a');
//		bb.putLong(0);
//		bb.put(Utils.getSHA(""));
//		bb.put(("b7b597e0d64accdb6d8271328c75ad301c29829619f4865d31cc0c550046a08f").getBytes("UTF-8"));
//		bb.order(ByteOrder.BIG_ENDIAN);
//		//String s = Utils.hash(Utils.StringToBinairy(c.toString())+Utils.StringToBinairy(new String(bb.array())+Utils.StringToBinairy(Utils.hash(""))+asymmetricCipherKeyPair.getPublic());
//		MessageDigest md = MessageDigest.getInstance("SHA-256","BC");
//		String f = new String (md.digest(bb.array()),"UTF-8");
//		signer.update(f.getBytes(), 0, f.length());
//		byte[] signature1 = signer.generateSignature();
//		String ssss = Utils.bytesToHex(signature1);
//		System.out.println("with BC SHA\n"+ssss);
//		System.out.println(verify(publicKey, md.digest(bb.array()), signature1));
		new Thread(new Auteur()).start();
//		new Thread(new Auteur()).start();
	}
}