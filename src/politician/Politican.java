package politician;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;

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

import auteur.Auteur;
import scrabble.Dictionary;
import scrabble.Scrabble;
import scrabble.Tile;
import scrabble.TileBag;
import scrabblos.Block;
import scrabblos.Letter;
import scrabblos.Utils;

public class Politican implements Runnable, IPolitician {

	// Network
	private final static String server = "localhost";
	private final static int port = 12345;
	private final int turn_limit = 100;
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
	public Set<String> dictionary;
	//public Trie trie;
	private long period;
	public int id;
	private Block block;
	private TileBag tileBag;
	private static int next_Politician_id = 0;
	private Scrabble scrbl;
	/**
	 * Creates a new client and generates random keys
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 */
	public Politican() throws UnknownHostException, IOException, NoSuchAlgorithmException, NoSuchProviderException {
		socket = new Socket(server, port);
		id = next_Politician_id++;
		period = 0;		
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
		dictionary = new HashSet<String>();
		//trie = new Trie(); 
		scrbl =  new Scrabble(new Dictionary(),this);
		tileBag = new TileBag(scrbl);
		scrbl.setTileBag(tileBag);
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
		JSONObject data = new JSONObject();
		JSONObject letter = getLetter();
		data.put("inject_letter", letter);
		String msg = data.toString();
		System.out.println("Politician " + id + " "+ msg);
		writer.writeLong(msg.length());
		writer.write(msg.getBytes("UTF-8"),0,msg.length());
		block = new Block(letter, block);
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
		letter.put("head",  "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
		letter.put("author", Utils.bytesToHex(publicKey.getEncoded()));
		ByteBuffer bb = ByteBuffer.allocate(8096);
		bb.putChar(c);
		bb.putLong(period);
		bb.put(("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855").getBytes("UTF-8"));
		bb.put(publicKey.getEncoded());
		bb.order(ByteOrder.BIG_ENDIAN);
		//String s = Utils.hash(Utils.StringToBinairy(c.toString())+Utils.StringToBinairy(new String(bb.array())+Utils.StringToBinairy(Utils.hash(""))+asymmetricCipherKeyPair.getPublic());
		MessageDigest md = MessageDigest.getInstance("SHA-256","BC");
		String f = new String (md.digest(bb.array()),"UTF-8");
		byte[] sig = signMessage(f);
		letter.put("signature", Utils.bytesToHex(sig));
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
		//String actualSignature = Base64.getEncoder().encodeToString(signature);
		//return actualSignature;
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
		long taille_ans = reader.readLong();
		byte [] cbuf = new byte[(int)taille_ans];
		reader.read(cbuf, 0, (int)taille_ans);
		String s = new String(cbuf,"UTF-8");
		System.out.println("Politician "+id+" Got "+s);
		JSONObject o = new JSONObject(s);
		//JsonObject myo =  (JsonObject) new JsonParser().parse(s);
		if (s.contains("full_letterpool"))
			parseLetterPool(o);
		if (s.contains("next_turn"))
			nextTurn(o);
		if (s.contains("inject_letter"))
			parseInjectedLetterMessage(o);
		if (s.contains("full_wordpool"))
			parseFullWordPool(o);
	}

	/**
	 * Parse full word pool
	 * @param word pool as json
	 */
	private void parseFullWordPool(JSONObject o) {
		JSONObject j = (JSONObject) o.get("full_wordpool");
		Integer fperiod = (Integer) j.get("current_period");
		period = Long.parseLong(fperiod.toString());
		JsonObject lettersj =  new JsonParser().parse(o.toString()).getAsJsonObject();
		lettersj = (JsonObject) lettersj.get("full_wordpool");
		JsonElement letters = lettersj.get("words");
		for (JsonElement l : letters.getAsJsonArray())
		{
			JsonObject word = (JsonObject) ((JsonArray)l).get(1);
			letterPool.add(word.get("letter").getAsCharacter());
		}
	}


	/**
	 * Parse injected_letter message 
	 * @param injected_letter JSONObject contatining message  
	 */
	private void parseInjectedLetterMessage(JSONObject o) {
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
			int s = Letter.getScore(o.get("letter").getAsCharacter());
			tileBag.AddTile((o.get("letter").getAsCharacter()), s);
		}
	}

	/**
	 * Get Full Letter Pool from Server
	 * @return true when successful 
	 * @throws IOException
	 * @throws JSONException
	 */
	@Override
	public boolean getFullLetterPool() throws IOException, JSONException {
		JSONObject obj = new JSONObject();
		obj.put("get_full_letterpool",JSONObject.NULL);
		String msg = obj.toString();
		long taille = msg.length();
		writer.writeLong(taille);
		writer.write(msg.getBytes("UTF-8"),0,(int)taille);
		return true;
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
		if (letterBag.isEmpty() || period > turn_limit)
			return;
	}

	/**
	 * Main Loop of Politician
	 */
	@Override
	public void run() {
		System.out.println("Starting Scrabblos Politician Node " + id);
		try {

			//	registerOnServer();
			listen();
			getFullLetterPool();
			getFullWordPool();
			new Thread(scrbl).start();
			injectWord();
			while(true) {
				read();
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get full word pool from server
	 * @throws IOException
	 */
	private void getFullWordPool() throws IOException {
		JSONObject obj = new JSONObject();
		obj.put("get_full_wordpool",JSONObject.NULL);
		String msg = obj.toString();
		long taille = msg.length();
		writer.writeLong(taille);
		writer.write(msg.getBytes("UTF-8"),0,(int)taille);
	}

	/**
	 * Validate a word (block of letters)
	 * @throws NoSuchProviderException 
	 * @throws NoSuchAlgorithmException 
	 */
	private void injectWord() throws NoSuchAlgorithmException, NoSuchProviderException {
		block = new Block();
		List<List<String>> results = new ArrayList<List<String>>();
		char[] priority = "KWXYZJQFHVBCPDGMAEILNORSTU".toLowerCase().toCharArray();
		
		//trie.findWords(trie.root,"f");
		searchDictionary("fh",dictionary, new Stack<String>(), results);
		for (List<String> result : results) {
			for (String word : result) {
				System.out.print(word + " ");
			}
			System.out.println("(" + result.size() + " words)");
		}
	}

	/**
	 * Search similar words in Set Dict
	 * @param input
	 * @param dictionary
	 * @param words
	 * @param results
	 */
	public static void searchDictionary(String input, Set<String> dictionary,
			Stack<String> words, List<List<String>> results) {

		for (int i = 0; i < input.length(); i++) {
			// take the first i characters of the input and see if it is a word
			String substring = input.substring(0, i + 1);

			if (dictionary.contains(substring)) {
				// the beginning of the input matches a word, store on stack
				words.push(substring);

				if (i == input.length() - 1) {
					// there's no input left, copy the words stack to results
					results.add(new ArrayList<String>(words));
				} else {
					// there's more input left, search the remaining part
					searchDictionary(input.substring(i + 1), dictionary, words, results);
				}

				// pop the matched word back off so we can move onto the next i
				words.pop();
			}
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
		new Thread(new Auteur()).start();
		new Thread(new Auteur()).start();
		Politican p = new Politican();
		new Thread(p).start();
		
		//new Thread(p).start();
	//	new Thread(new Politican()).start();
	
	}


	@Override
	public void registerOnServer() throws IOException {
		// TODO Auto-generated method stub
		
	}

	public void findnew()
	{
		scrbl.resetInstance();
		scrbl.setTileBag(tileBag);
		scrbl.mine();
	}
	public boolean injectWordbyAI(String word) {
		
		if(true)
		{
			return true;
		}
	return false;
	}
}