package gitlet;

import java.io.File;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

public class Blob implements Serializable {
    private String hash;
    private String content;

    public Blob(File path) throws IllegalArgumentException
    {
        this.content = new String( Utils.readContents(path), StandardCharsets.UTF_8);
        String sha = "Blob " + this.content.length() + "\0" + this.content;
        this.hash = Utils.sha1(sha);
    }

    public String getHash() {
        return hash;
    }

    public String getContent() {
        return content;
    }

}
