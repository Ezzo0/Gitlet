package gitlet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Tree implements Serializable {

    // TODO: Add dirs to tree
    List<TreeEntry> blobs;
    String hash;

    public Tree()
    {
        blobs = new ArrayList<>();
    }

    public void addBlob(TreeEntry blob)
    {
        blobs.add(blob);
    }

    // Method to compute the SHA-1 hash of a tree object
    public void hashTreeObject()
    {
        List<String> l = new ArrayList<>();
        for (TreeEntry e: this.blobs)
        {
            StringBuilder s = new StringBuilder();

            s.append("Tree ");
            s.append(e.getSize());
            s.append("\0");
            s.append(e.getHash());
            l.add(s.toString());
        }
        this.hash = Utils.sha1(l);
    }

    public String getHash()
    {
        return this.hash;
    }
}
