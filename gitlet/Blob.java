package gitlet;

import java.io.File;
import java.nio.charset.StandardCharsets;

public class Blob {
    private String hash;
    private String content;

    public Blob(File path)
    {
        try {
            this.content = new String( Utils.readContents(path), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    public void computeSHA1()
    {
        this.hash = Utils.sha1("Blob " + this.content.length() + "\0" + this.content);
    }

    public String getHash() {
        return hash;
    }

    public String getContent() {
        return content;
    }



}
