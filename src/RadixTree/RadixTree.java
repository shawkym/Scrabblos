package RadixTree;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;


/**
 * @author Shao
 *
 */
public class RadixTree {
	private RadixTreeNode root;
	
	public RadixTree() {
		this.root = new RadixTreeNode();
	}
	
	
	/**
	 * @param m
	 * @param p
	 */
	public RadixTreeNode insert(String m, ArrayList<Position> p) {
		RadixTreeNode node = new RadixTreeNode();
		node.setPositons(p);
		RadixTreeNode res = insertAuxi(root, m, node);
		return res;
	}
	
	/**
	 * @param node
	 * @param mots
	 * @param aInsert
	 * @return construire sous sous arbre avec le mot 
	 */
	public RadixTreeNode insertAuxi(RadixTreeNode node, String m, RadixTreeNode aInsert) {
		if(m.isEmpty()) {
			return aInsert;
		}else {
			RadixTreeEdge dir = node.getEdge(m);
			String c = dir.getCle();
			//plus grande prefixe
			int i = 0;
			int min = Math.min(c.length(), m.length());
			for (int j = 0; j < min; j++) {
				if(m.charAt(i) == c.charAt(i)) {
					i++;
				}
			}
			//new node
			if(i == 0) {
				dir.setCle(m);
				RadixTreeNode res = insertAuxi(dir.getNext(), "", aInsert);
				dir.setNext(res);
				return res;
			}	
			
			String suffixe_cle = c.substring(i);
			String suffixe_m = m.substring(i);
			if(suffixe_cle.isEmpty()) {
				return insertAuxi(dir.getNext(), suffixe_m, aInsert);
			}
			
			RadixTreeNode move = dir.getNext();
			RadixTreeNode newNode;
			
			if(suffixe_m.isEmpty()) {
				newNode = aInsert;
			}else {
				newNode = new RadixTreeNode();
			}
			
			dir.setNext(newNode);
			dir.setCle(c.substring(0,i));
			insertAuxi(newNode, suffixe_cle, move);
			
			return insertAuxi(newNode, suffixe_m, aInsert);	
		}
	}
	
	
	/**
	 * @param node
	 * @return tous les positions
	 */
	public ArrayList<Position> getPositions(RadixTreeNode node){
		ArrayList<Position> res = node.getPositons();
		for(RadixTreeEdge e : node.edges) {
			if(e.getNext() != null) {
				res.addAll(getPositions(e.getNext()));
			}
		}
		return res;
	}
	
	

	/**
	 * @param m
	 * @param node
	 * @return position de mots dans sous arbre
	 */
	public ArrayList<Position> chercheR(String m, RadixTreeNode node){
		if (m.isEmpty()) {
			return getPositions(node);
		}
		if(node == null) {
			return new ArrayList<>();
		}
		RadixTreeEdge e = node.getEdge(m);
		int p = e.sizeOfPrefix(m);
		if(p == e.getCle().length()) {
			return chercheR(m.substring(e.getCle().length()), e.getNext());
		}
		
		if(p <= e.getCle().length() && m.substring(p).isEmpty()) {
			return getPositions(e.getNext());
		}
		return new ArrayList<>();
	}
	
	/**
	 * @param m
	 * @return position de mots dans radix tree
	 */
	public ArrayList<Position> cherche(String m){
		return chercheR(m, root);
	}
	
	public String toString() {
		return root.toString();
	}
	
	public void saveRadixTree(String filename) throws IOException {
		FileOutputStream fileOut = new FileOutputStream(filename);
		ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(this);
        out.close();
        fileOut.close();
        System.out.println("Serialized data is saved in " + filename);
	}
	
	public static RadixTree readRadixTree(String filename) throws IOException, ClassNotFoundException {
        FileInputStream fileIn = new FileInputStream(filename);
        ObjectInputStream in = new ObjectInputStream(fileIn);
        RadixTree rd = (RadixTree) in.readObject();
        in.close();
        fileIn.close();
        return rd;
	}
	
	
}
