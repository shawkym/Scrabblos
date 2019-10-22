package scrabblos;

import java.util.ArrayList;

public class Mots {
	private final ArrayList<Letter> mot;
	private final byte[] head;
	private final byte[] politician;
	private final byte[] signature;
	
	public Mots(ArrayList<Letter> mot, byte[] head, byte[] politician, byte[] signature) {
		this.mot = mot;
		this.head = head;
		this.politician = politician;
		// signature a chiffre avec ED255519
		this.signature = signature;
	}
}
