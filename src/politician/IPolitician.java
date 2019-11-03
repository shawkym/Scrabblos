package politician;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;

import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.DataLengthException;
import org.json.JSONException;
import org.json.JSONObject;

public interface IPolitician {

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

	byte[] signMessage(String message) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException,
			UnsupportedEncodingException, DataLengthException, CryptoException;

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
	 */

	void read() throws IOException, JSONException, InvalidKeyException, DataLengthException, NoSuchAlgorithmException,
			SignatureException, NoSuchProviderException, CryptoException;

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
	 */

	void nextTurn(JSONObject o) throws InvalidKeyException, JSONException, NoSuchAlgorithmException, SignatureException,
			IOException, DataLengthException, CryptoException, NoSuchProviderException;

	/**
	 * New Word discovered by Miner
	 * Called by Miner to start word injection
	 * @param word
	 * @return true if word became offical
	 */
	boolean injectWordbyAI(String word);

}