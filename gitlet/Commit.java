package gitlet;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Abdelrahman Ezz
 */


/** TODO
 * A commit, therefore, will consist of
 * a log message, timestamp, a mapping of file names to blob references,
 * a parent reference, and (for merges) a second parent reference.
 */



public class Commit implements Serializable {

    /** The message of this Commit. */
    private String message;
    /** The branch of this Commit. */
    private String branch;
    /** The first parent of this Commit. */
    private String parent;
    /** The second parent of this Commit. */
    private String secParent;
    /** The timeStamp of this Commit. */
    private String timeStamp;
    /** The tree structure of this Commit. */
    private String tree;

    private String setDefaultTimeStamp()
    {
        // Create a ZonedDateTime for the epoch time (1970-01-01T00:00:00Z)
        ZonedDateTime epochTime = ZonedDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC);

        // Define the desired format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
                "HH:mm:ss 'UTC', EEEE, d MMMM yyyy", Locale.ENGLISH
        );

        // Format the epoch time
        return epochTime.format(formatter);
    }

    public Commit(String message)
    {
        this.message = message;
        this.branch = "master";
        this.parent = null;
        this.secParent = null;
        this.tree = null;
        this.timeStamp = setDefaultTimeStamp();
    }

    public Commit(String message, String branch)
    {
        this.message = message;
        this.branch = branch;
        this.parent = null;
        this.secParent = null;
        this.tree = null;
        this.timeStamp = setDefaultTimeStamp();
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public String getMessage()
    {
        return this.message;
    }

    public long getMessageLength()
    {
        return this.message.length();
    }

    public void setBranch(String branch)
    {
        this.branch = branch;
    }

    public String getBranch()
    {
        return this.branch;
    }

    public void setParent(String parent)
    {
        this.parent = parent;
    }

    public String getParent()
    {
        return this.parent;
    }

    public void setTimeStamp(String timeStamp)
    {
        this.timeStamp = timeStamp;
    }

    public String getTimeStamp()
    {
        return this.timeStamp;
    }

    public void setTree(String tree)
    {
        this.tree = tree;
    }

    public String getTree()
    {
        return this.tree;
    }

    // Method to compute the SHA-1 hash of a tree object
    public String hashCommitObject() throws IllegalArgumentException
    {
        // Serialize the commit content
        StringBuilder commitContent = new StringBuilder();
        commitContent.append("Commit\0 ");
        if (this.tree != null) {
            commitContent.append("Tree ").append(this.tree).append("\n");
        }
        if (this.parent != null) {
            commitContent.append("Parent ").append(this.parent).append("\n");
        }
        if (this.secParent != null) {
            commitContent.append("Second parent ").append(this.secParent).append("\n");
        }
        commitContent.append("TimeStamp ").append(this.timeStamp).append(" +0000").append("\n");
        commitContent.append(this.message).append("\n");
        return Utils.sha1(commitContent.toString());
    }

}
