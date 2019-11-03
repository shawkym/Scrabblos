package scrabblos;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Collection;

import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.json.JSONObject;

public class Block {

	private String head;
	private Block previous;
	private	Word  word;
	private JSONObject data;


	public Block() throws NoSuchAlgorithmException, NoSuchProviderException {
		this.head = Utils.hash("");
		this.previous = null;
		this.data = new JSONObject();
	}

	public Block(JSONObject data, Block previous) throws NoSuchAlgorithmException, NoSuchProviderException {
		if (previous !=null && previous.getData() !=null) {
			head = Utils.hash(previous.getData().toString());
		}

		this.data = data;
		this.previous = previous;	
	}

	public Block(Block b) throws NoSuchAlgorithmException, NoSuchProviderException {
		this();
		this.previous = b;
	}

	public String getHash() throws NoSuchAlgorithmException, NoSuchProviderException {
		if(data == null) {
			return Utils.hash("");
		}
		return Utils.hash(data.toString());
	}

	public boolean is_valid() throws NoSuchAlgorithmException, NoSuchProviderException {
		if(previous == null) return true;

		if(previous.getHash().equals(head)) return previous.is_valid();
		else return false;


	}

	/**
	 * @return the head
	 */
	public String getHead() {
		return head;
	}

	/**
	 * @return the data
	 */
	public JSONObject getData() {
		return data;
	}

	/**
	 * @param head the head to set
	 */
	public void setHead(String head) {
		this.head = head;
	}

	public Word getWord() {
		return word;
	}

	public void setWord(Word word) {
		this.word = word;
	}

	public void generate() {
		for(Letter l : word.getMot())
		{
			JSONObject letter = new JSONObject();
			letter.put("letter", l.getLetter());
			letter.put("period", l.getPeriod());
			letter.put("head",  l.getHead());
			letter.put("author", l.getAuthor());
			letter.put("signature", l.getSignature());
			data.put("word",letter);
		}
	}

	public void sign(Ed25519PrivateKeyParameters privateKey, Ed25519PublicKeyParameters publicKey) throws InvalidKeyException, DataLengthException, NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, NoSuchProviderException, CryptoException, IOException {;
	String head = getNewHead(privateKey);
	data.put("head", head);
	data.put("author", Utils.bytesToHex(publicKey.getEncoded()));
	ByteBuffer bb = ByteBuffer.allocate(8096);
	bb.put(head.getBytes("UTF-8"));
	bb.put(publicKey.getEncoded());
	bb.order(ByteOrder.BIG_ENDIAN);
	MessageDigest md = MessageDigest.getInstance("SHA-256","BC");
	String f = new String (md.digest(bb.array()),"UTF-8");
	byte[] sig = Utils.signMessage(f,privateKey);
	data.put("signature", Utils.bytesToHex(sig));
	}

	private String getNewHead(Ed25519PrivateKeyParameters privateKey) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, DataLengthException, SignatureException, InvalidKeySpecException, CryptoException, IOException {
		if (previous == null)
			return head;
		MessageDigest md = MessageDigest.getInstance("SHA-256","BC");
		String f = new String (md.digest(data.toString().getBytes()));
		byte[] sig = Utils.signMessage(f,privateKey);
		return Utils.bytesToHex(sig);
	}
}
