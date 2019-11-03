package scrabblos;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.Signer;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.signers.Ed25519Signer;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Encoder;

public class Utils {

	
	public static byte[] getSHA(String input) throws NoSuchAlgorithmException, NoSuchProviderException
	{
		MessageDigest md = MessageDigest.getInstance("SHA-256","BC");
		return md.digest(input.getBytes(StandardCharsets.UTF_8));
	}
	

	public static String bytesToHex(byte[] hashInBytes) {

        StringBuilder sb = new StringBuilder();
        for (byte b : hashInBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();

    }
	
	public static byte[] concatenate(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }
	
	 public static String hash(String input) throws NoSuchAlgorithmException, NoSuchProviderException {
	    	return bytesToHex(getSHA(input));
	    }
	
	public static String StringToBinairy(String s) throws UnsupportedEncodingException {
		byte[] infoBin = s.getBytes("UTF-8");
		String result = "";
        for (byte b : infoBin) {
          result += Integer.toBinaryString(b);
        }
		return result;
	}

	public static String getHexKey(AsymmetricKeyParameter asymmetricKeyParameter) {
		byte [] array = asymmetricKeyParameter.toString().getBytes();
		String s = Base64.toBase64String(array);
		byte[] decoded = Base64.decode(s);
		String res = String.format("%040x", new BigInteger(1, decoded));
		return res.substring(0, 63);
	}
	
	/**
	 * Signs a message using Ed25519Signer
	 * @param privateKey 
	 * @param  a message to sign
	 * @return array of bytes containing digital signature
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws SignatureException
	 * @throws DataLengthException
	 * @throws CryptoException
	 * @throws IOException 
	 * @throws InvalidKeySpecException 
	 * @throws NoSuchProviderException 
	 */
	static public byte[] signMessage(String message, CipherParameters privateKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, DataLengthException, CryptoException, IOException, InvalidKeySpecException, NoSuchProviderException {
		Signer signer = new Ed25519Signer();
		signer.init(true, privateKey);
		signer.update(message.getBytes(), 0, message.length());
		byte[] signature = signer.generateSignature();
		return signature;
		//String actualSignature = Base64.getEncoder().encodeToString(signature);
		//return actualSignature;
	}
}
