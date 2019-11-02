package scrabblos;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Base64.Encoder;

import org.bouncycastle.crypto.params.AsymmetricKeyParameter;

public class Utils {

	
	public static byte[] getSHA(String input) throws NoSuchAlgorithmException
	{
		MessageDigest md = MessageDigest.getInstance("SHA-256");
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
	
	 public static String hash(String input) throws NoSuchAlgorithmException {
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
		Encoder encoder = Base64.getEncoder();
		String s = encoder.encodeToString(array);
		byte[] decoded = Base64.getDecoder().decode(s);
		String res = String.format("%040x", new BigInteger(1, decoded));
		return res.substring(0, 63);
	}
	
	
}
