package scrabblos;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import org.json.JSONObject;

public class Block {

	private String head;
	private Block previous;
	private JSONObject data;
	
	
	public Block() throws NoSuchAlgorithmException, NoSuchProviderException {
		this.head = Utils.hash("");
		this.previous = null;
		this.data = null;
	}
	
	public Block(JSONObject data, Block previous) throws NoSuchAlgorithmException, NoSuchProviderException {
		if (previous !=null && previous.getData() !=null) {
			head = Utils.hash(previous.getData().toString());
		}

		this.data = data;
		this.previous = previous;	
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
}
