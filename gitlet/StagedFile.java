package gitlet;

import java.io.Serializable;

public class StagedFile implements Serializable {
    private final Blob blob;
    private final String path;

    public StagedFile(Blob blob, String path) {
        this.blob = blob;
        this.path = path;
    }

    public Blob getBlob() {
        return this.blob;
    }

    public String getPath() {
        return this.path;
    }

    @Override
    public String toString() {
        return this.path + " " + this.blob.getHash();
    }
}
