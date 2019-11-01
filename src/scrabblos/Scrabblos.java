package scrabblos;

import java.io.IOException;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import auteur.Auteur;

public class Scrabblos {
	public static void main(String[] args) throws UnknownHostException, NoSuchAlgorithmException, NoSuchProviderException, IOException {
		Auteur a = new Auteur();
		Thread t = new Thread(a);
		t.start();
	}
}
