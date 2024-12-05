package gitlet;

import java.io.Serializable;

public class TreeEntry implements Serializable {
    private String path;
    private String hash;

    public TreeEntry()
    {
        this.path = null;
        this.hash = null;
    }

    public TreeEntry(String path, String hash) {
        this.path = path;
        this.hash = hash;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return this.path;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getHash() {
        return this.hash;
    }

    public long getSize(){
        return this.path.length() + this.hash.length();
    }
}
