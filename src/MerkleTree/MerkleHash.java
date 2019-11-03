package MerkleTree;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MerkleHash {

	private byte[] value;
	public MerkleHash() {
		
	}
	
	public static MerkleHash create(byte[] buffer) {
		MerkleHash hash = new MerkleHash();
		hash.computeHash(buffer);
		return hash;
	}
	
	 public static MerkleHash create(String buffer) {
	        return create(buffer.getBytes(StandardCharsets.UTF_8));
	 }
	
	private void computeHash(byte[] buffer) {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-256");
			digest.digest(buffer);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	public static byte[] concatenate(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }
	
	public static MerkleHash create(MerkleHash h1, MerkleHash h2) {
		return create(concatenate(h1.getValue(), h2.getValue()));
	}

	public byte[] getValue() {
		return value;
	}

	public void setValue(byte[] value) {
		this.value = value;
	}
	
	public String toString(	) {
		return value.toString();
	}
	
}
