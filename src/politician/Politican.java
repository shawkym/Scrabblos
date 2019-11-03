package politician;

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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

import MerkleTree.MerkleHash;
import MerkleTree.MerkleProofHash;
import MerkleTree.MerkleTree;
import auteur.Auteur;
import scrabble.Dictionary;
import scrabble.Scrabble;
import scrabble.TileBag;
import scrabblos.Block;
import scrabblos.Letter;
import scrabblos.Word;

public class Politican implements Runnable {

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
	public ArrayList<Character> tileBagLetters;
	public ArrayList<Letter> letterBag;
	public ArrayList<Character> letterPool;
	public Set<String> dictionary;
	//public Trie trie;
	
	public MerkleTree blockchain;
	MerkleHash rootHash;
	
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
		tileBagLetters = new ArrayList<Character>();
		letterPool = new ArrayList<Character>();
		letterBag = new ArrayList<Letter>();
		dictionary = new HashSet<String>();
		//trie = new Trie(); 
		scrbl =  new Scrabble(new Dictionary(),this);
		tileBag = new TileBag(scrbl);
		scrbl.setTileBag(tileBag);
		
		blockchain = new MerkleTree();
		rootHash = new MerkleHash();
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

	public byte[] signMessage(String message) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, UnsupportedEncodingException, DataLengthException, CryptoException {
		Signer signer = new Ed25519Signer();
		signer.init(true, privateKey);
		signer.update(message.getBytes(), 0, message.length());
		byte[] signature = signer.generateSignature();
		return signature;
	}

	/**
	 * Inform the server authority we're reading update regularly 
	 * @throws IOException
	 */

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

	public void read() throws IOException, JSONException, InvalidKeyException, DataLengthException, NoSuchAlgorithmException, SignatureException, NoSuchProviderException, CryptoException {
		long taille_ans = reader.readLong();
		byte [] cbuf = new byte[(int)taille_ans];
		reader.read(cbuf, 0, (int)taille_ans);
		String s = new String(cbuf,"UTF-8");
		System.out.println("Politician "+id+" Got "+s);
		JSONObject o = new JSONObject(s);
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

	public void nextTurn(JSONObject o) throws InvalidKeyException, JSONException, NoSuchAlgorithmException, SignatureException, IOException, DataLengthException, CryptoException, NoSuchProviderException {
		period = o.getInt("next_turn");
		if (tileBagLetters.isEmpty() || period > turn_limit)
			return;
	}

	/**
	 * Main Loop of Politician
	 */
	@Override
	public void run() {
		System.out.println("Starting Scrabblos Politician Node " + id);
		try {
			listen();
			getFullLetterPool();
			getFullWordPool();
			new Thread(scrbl).start();
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
	 * @throws IOException 
	 */
	private void injectWord(Word word) throws NoSuchAlgorithmException, NoSuchProviderException, IOException {
		Block b = block;
		block = new Block(b);
		block.setWord(word);
		block.generate(privateKey,publicKey);
		
		JSONObject obj = new JSONObject();
		obj.put("get_full_wordpool",block.getData());
		String msg = obj.toString();
		long taille = msg.length();
		writer.writeLong(taille);
		writer.write(msg.getBytes("UTF-8"),0,(int)taille);
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

		new Thread(new Auteur()).start();
		new Thread(new Auteur()).start();
		Politican p = new Politican();
		new Thread(p).start();
	
		
	}

	/**
	 * Remine with current letterpool
	 */
	public void findnew()
	{
		scrbl.resetInstance();
		scrbl.setTileBag(tileBag);
		scrbl.mine();
	}

	/**
	 * New Word discovered by Miner
	 * Called by Miner to start word injection
	 * @param word
	 * @return true if word became official
	 */
	public boolean injectWordbyAI(String word) {

		ArrayList<Letter> mbag = new ArrayList<Letter>();
		ArrayList<byte[]> authors_added = new ArrayList<byte[]>();

		for (char c : word.toCharArray())
		{
			for (Letter b : letterBag)
			{
				if (authors_added.contains(b.getAuthor()))
				{
					continue;
				}
				if (b.getLetter() == c)
				{
					mbag.add(b);
					authors_added.add(b.getAuthor());
				}
			}
		}
		Word w = new Word(mbag);

		try {
			injectWord(w);
			if(last_block_now_official())
			return true;
		} catch (NoSuchAlgorithmException | NoSuchProviderException | IOException e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * See if injected word is taken by all servers
	 * @return
	 */
	private boolean last_block_now_official() {
		// TODO Wu consensus politicians
		
		String s = "";
		for (Letter c : block.getWord().mot) {
			s+= c.getLetter();
		}
		//cree le merckle tree avec un hash
		MerkleTree tmp = new MerkleTree();
		MerkleHash l1 = MerkleHash.create(s);
		tmp.appendLeaf(l1);
		tmp.buildTree();
		//merge avec b2 qui doit contient toutes les precedents hash
		//afin obtenir un roothash
		rootHash = blockchain.addTree(tmp);
		//test si roothash verifier l1 
		List<MerkleProofHash> auditTrail = tmp.auditProof(l1);
        boolean is_verif =  MerkleTree.verifyAudit(rootHash, l1, auditTrail);  
        return true;
	}
}