package gitlet;

public class TreeEntry {
    private final String mode;
    private final String name;
    private final String type;
    private final String hash;

    public TreeEntry(String mode, String name, String type, String hash) {
        this.mode = mode;
        this.name = name;
        this.type = type;
        this.hash = hash;
    }

    public String getMode() {
        return this.mode;
    }

    public String getName() {
        return this.name;
    }

    public String getType() {
        return this.type;
    }

    public String getHash() {
        return this.hash;
    }

    public String getSize()
    {
        return String.valueOf(this.mode.length() + this.name.length() + this.type.length() + this.hash.length());
    }
}
