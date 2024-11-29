package gitlet;

import java.io.File;
import static gitlet.Utils.*;

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Abdelrahman Ezz
 */
public class Repository {

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /** The .gitlet/objects directory. In which commits and blobs are stored */
    public static final File OBJs = join(GITLET_DIR, "objects");
    /** The head pointer file */
    public static final File HEAD = join(GITLET_DIR, "HEAD");


    public Repository() {}

    public static boolean isInitialized()
    {
        if (GITLET_DIR.exists())
        {
            // TODO: Remove this statement to put it in its place
            System.out.print("A Gitlet version-control system already exists in the current directory.");
            return true;
        }
        return false;
    }



    public static void init()
    {
        if (GITLET_DIR.exists())
        {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            return;
        }
        GITLET_DIR.mkdir();
        OBJs.mkdir();

        Commit initialCommit = new Commit("initial commit");
        String SHA1 = Utils.sha1("commit " + initialCommit.getMessageLength() + "\0" + initialCommit.getMessage());
        File SHA1Commit = join(OBJs, SHA1);
        Utils.writeObject(SHA1Commit, initialCommit);
        Utils.writeContents(HEAD, SHA1);
    }


    /* TODO: fill in the rest of this class. */
}
