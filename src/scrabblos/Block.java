package scrabblos;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

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
		this.data = null;
		this.word = null;
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

	public void generate(Ed25519PrivateKeyParameters privateKey, Ed25519PublicKeyParameters publicKey) {
		// TODO Wu generate les json de tous les lettre dans le mots
		// et faire signer
		
	}
}
