package RadixTree;

import java.util.ArrayList;


public class RadixTreeNode {
	public RadixTreeEdge[] edges;
	public ArrayList<Position> positons;
	
	public RadixTreeNode() {
		this.positons = new ArrayList<Position>();
		this.edges = new RadixTreeEdge[28];
		for(int i = 0; i < 28; i++) {
			edges[i] = new RadixTreeEdge("", null);
		}
	}
    
	public RadixTreeEdge getEdge(String s) {
		char c = s.charAt(0);
		int pos;
		
		if (c == '-') {
			pos = 26;
		}
		else {
			//in last pos
			if (c == '\'') {
				pos = 27;
			}
			else {
				pos = c - 'a';
			}
		}
		return edges[pos];
	}
	

	public void setEdges(RadixTreeEdge[] edges) {
		this.edges = edges;
	}

	public ArrayList<Position> getPositons() {
		return positons;
	}

	public void setPositons(ArrayList<Position> positons) {
		this.positons = positons;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append('(');
		for (RadixTreeEdge edge: edges) {
			sb.append(edge.toString());
			sb.append(',');
		}
		sb.append(')');
		return sb.toString();
	}
	
}
