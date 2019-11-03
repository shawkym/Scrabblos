package MerkleTree;

import java.security.InvalidParameterException;

public class MerkleNode {
	private MerkleHash hash;
	private MerkleNode left;
	private MerkleNode right;
	private MerkleNode parent;
	
	public MerkleNode() {};
	

	public MerkleNode(MerkleHash hash) {
		this.hash = hash;
	}
	
	public MerkleNode(MerkleNode left, MerkleNode right) {
		this.left = left;
		this.right = right;
		this.left.parent = this;
        if (this.right != null) {
        	this.right.parent = this;
        }
        this.computeHash();
	}
	
	private void computeHash() {
		if(this.right == null) {
			this.hash = this.left.hash;
		} else {
	        this.hash = MerkleHash.create(MerkleHash.concatenate(this.left.hash.getValue(), this.right.hash.getValue()));
	    }
        if (this.parent != null) {
            this.parent.computeHash();
        }    
		
	}
	
	public boolean isleaf() {
		return this.left == null && this.right == null;
	}
	
   public boolean verifyHash() {
        if (this.left == null && this.right == null) return true;
        if (this.right == null) return hash.equals(left.hash);

        if (this.left == null) {
            throw new InvalidParameterException("Left branch must be a node if right branch is a node!");
        }

        MerkleHash leftRightHash = MerkleHash.create(this.left.hash, this.right.hash);
        return hash.equals(leftRightHash);
    }
	
   
	public boolean equals(MerkleNode o) {
		return this.hash.equals(o.hash);
	}

	public MerkleHash getHash() {
		return hash;
	}

	public void setHash(MerkleHash hash) {
		this.hash = hash;
	}

	public MerkleNode getLeft() {
		return left;
	}

	public void setLeft(MerkleNode node) {
		if (node.hash == null) {
            throw new InvalidParameterException("Node hash null");
        }

        this.left = node;
        this.left.parent = this;

        this.computeHash();
	}

	public MerkleNode getRight() {
		return right;
	}

	public void setRight(MerkleNode node) {
		if(node.hash == null) {
			throw new InvalidParameterException("Node hash null");
		}
		
		this.right = node;
		this.right.parent = this;
		this.computeHash();
	}

	public MerkleNode getParent() {
		return parent;
	}
}
