package auteur;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.DataLengthException;
import org.json.JSONException;
import org.json.JSONObject;

public interface IAuteur {

	/** 
	 * Register this client on server authority 
	 * 
	 * @throws IOException
	 */
	void registerOnServer() throws IOException;

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
	 * @throws InvalidKeySpecException 
	 */
	void injectLetter() throws IOException, NoSuchAlgorithmException, InvalidKeyException, JSONException,
			SignatureException, DataLengthException, CryptoException, NoSuchProviderException, InvalidKeySpecException;

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
	 * @throws IOException 
	 * @throws InvalidKeySpecException 
	 */
	JSONObject getLetter() throws JSONException, NoSuchAlgorithmException, UnsupportedEncodingException,
			InvalidKeyException, SignatureException, DataLengthException, CryptoException, NoSuchProviderException, IOException, InvalidKeySpecException;

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
	 * @throws IOException 
	 * @throws InvalidKeySpecException 
	 * @throws NoSuchProviderException 
	 */
	byte[] signMessage(String message) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException,
			UnsupportedEncodingException, DataLengthException, CryptoException, IOException, InvalidKeySpecException, NoSuchProviderException;

	/**
	 * Inform the server authority we're reading update regularly 
	 * @throws IOException
	 */
	void listen() throws IOException;

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
	 * @throws InvalidKeySpecException 
	 */
	void read() throws IOException, JSONException, InvalidKeyException, DataLengthException, NoSuchAlgorithmException,
			SignatureException, NoSuchProviderException, CryptoException, InvalidKeySpecException;

	/**
	 * Get Full Letter Pool from Server
	 * @return true when successful 
	 * @throws IOException
	 * @throws JSONException
	 */
	boolean getFullLetterPool() throws IOException, JSONException;

	/**
	 * Get letter pool since period p
	 * @param p : last known period 
	 * @return true when successful 
	 * @throws IOException
	 * @throws JSONException
	 */
	boolean getLetterPoolSince(int p) throws IOException, JSONException;

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
	 * @throws InvalidKeySpecException 
	 */
	void nextTurn(JSONObject o) throws InvalidKeyException, JSONException, NoSuchAlgorithmException, SignatureException,
			IOException, DataLengthException, CryptoException, NoSuchProviderException, InvalidKeySpecException;

}