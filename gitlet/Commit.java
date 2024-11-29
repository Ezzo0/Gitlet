package gitlet;

import java.io.Serializable;
import java.time.Instant;

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
    /** The parent of this Commit. */
    private String parent;
    /** The timeStamp of this Commit. */
    private long timeStamp;
    /** The tree structure of this Commit. */
    private Tree tree;

    public Commit(String message)
    {
        this.message = message;
        this.branch = "master";
        this.parent = null;
        this.tree = null;

        // Representing 00:00:00 UTC, 1 January 1970
        Instant epoch = Instant.EPOCH;
        /// Convert the Instant to seconds since the epoch
        this.timeStamp = epoch.getEpochSecond();
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

    public void setTimeStamp(long timeStamp)
    {
        this.timeStamp = timeStamp;
    }

    public long getTimeStamp()
    {
        return this.timeStamp;
    }

    public void setTree(Tree tree)
    {
        this.tree = tree;
    }

    public Tree getTree()
    {
        return this.tree;
    }


}
