package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

public class StagingArea implements Serializable {
    private HashMap<String, StagedFile> stage;

    public StagingArea() {
        this.stage = new HashMap<>();
    }

    public void addFile(String filePath) throws IllegalArgumentException {
        File f = new File(filePath);
        Blob blob = new Blob(f);

        // If file is not staged, push it to staging area
        if (!this.stage.containsKey(filePath))
        {
            StagedFile sf = new StagedFile(blob, f.toString());
            this.stage.put(filePath, sf);
            return;
        }

        // If file was staged, push it if there are differences
        StagedFile sf = this.stage.get(filePath);
        Blob stagedBlob = sf.getBlob();
        if (!stagedBlob.getHash().equals(blob.getHash()))
        {
            sf = new StagedFile(blob, f.toString());
            this.stage.put(filePath, sf);
        }
    }

    public void stageRemovedFile(String filePath) throws IllegalArgumentException {
        File f = new File(filePath);
        this.stage.put(filePath, null);
    }

    public void removeFile(String filePath) {
        this.stage.remove(filePath);
    }

    public HashMap<String, StagedFile> getStagedFiles() {
        return this.stage;
    }

    public void clearStage()
    {
        this.stage = null;
        Utils.writeObject(Repository.INDEX, this);
    }

    public boolean iscleared()
    {
        return this.stage == null || this.stage.isEmpty();
    }

    public void displayStagedFiles() {
        for (HashMap.Entry<String, StagedFile> entry : this.stage.entrySet()) {
            if (entry.getValue() != null)
                Utils.message(entry.getKey());
        }
    }

    public void displayRemovedFiles() {
        for (HashMap.Entry<String, StagedFile> entry : this.stage.entrySet()) {
            if (entry.getValue() == null)
                Utils.message(entry.getKey());
        }
    }
}
