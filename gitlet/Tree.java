package gitlet;

import java.io.Serializable;
import java.util.HashMap;

public class Tree implements Serializable {

    HashMap<String, TreeEntry> blobs;
    String hash;
    long treeSize;

    public Tree()
    {
        this.blobs = new HashMap<>();
        treeSize = 0;
    }

    public void addBlob(String path, TreeEntry blob)
    {
        if (this.blobs.containsKey(path)) {
            System.out.println("Previous size: " + this.treeSize);
            this.treeSize -= this.blobs.get(path).getSize();
        }

        blobs.put(path, new TreeEntry(blob.getPath(), blob.getHash()));
        this.treeSize += blob.getSize();
        System.out.println("Current size: " + this.treeSize);
    }

    public void removeBlob(String path)
    {
        this.blobs.remove(path);
    }

    public HashMap<String, TreeEntry> getTree()
    {
        return this.blobs;
    }

    // Method to compute the SHA-1 hash of a tree object
    public String hashTreeObject() throws IllegalArgumentException
    {
        StringBuilder s = new StringBuilder();
        s.append("Tree ");
        s.append(String.valueOf(this.treeSize));
        s.append("\0");

        for (TreeEntry e: this.blobs.values())
        {
            s.append(e.getPath());
            s.append("\0");
            s.append(e.getHash());
            s.append("\n");
        }
        this.hash = Utils.sha1(s.toString());
        return this.hash;
    }

    public String getHash()
    {
        return this.hash;
    }
}
