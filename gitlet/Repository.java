package gitlet;

import java.io.File;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

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
    public static final File GITLET_DIR = Utils.join(CWD, ".gitlet");
    /** The .gitlet/objects directory. In which commits, trees and blobs are stored */
    public static final File OBJs = Utils.join(GITLET_DIR, "objects");
    /** The .gitlet/objects/commits directory. In which commits are stored */
    public static final File COMMITS = Utils.join(OBJs, "commits");
    /** The .gitlet/objects/trees directory. In which trees are stored */
    public static final File TREES = Utils.join(OBJs, "trees");
    /** The .gitlet/objects/blobs directory. In which blobs are stored */
    public static final File BLOBS = Utils.join(OBJs, "blobs");
    /** The head pointer file */
    public static final File HEAD = Utils.join(GITLET_DIR, "HEAD");
    public static final File INDEX = Utils.join(GITLET_DIR, "index");
    private static StagingArea st;


    public Repository() {}

    public static boolean isInitialized()
    {
        if (GITLET_DIR.exists())
        {
            Utils.message("A Gitlet version-control system already exists in the current directory.");
            return true;
        }
        return false;
    }

    // Check the existence of .gitlet Directory
    private static boolean initializedGitlet()
    {
        if (!GITLET_DIR.exists())
        {
            Utils.message("Not in an initialized Gitlet directory.");
            return false;
        }
        return true;
    }

    public static void init()
    {
        if (isInitialized())
            return;

        // Initialize Gitlet Directories
        GITLET_DIR.mkdir();
        OBJs.mkdir();
        COMMITS.mkdir();
        TREES.mkdir();
        BLOBS.mkdir();

        // Define an init commit and save it to hard disk
        Commit initialCommit = new Commit("initial commit");
        try {
            String SHA1 = initialCommit.hashCommitObject();
            File SHA1Commit = Utils.join(COMMITS, SHA1);
            Utils.writeObject(SHA1Commit, initialCommit);
            Utils.writeContents(HEAD, SHA1);
        } catch (IllegalArgumentException e){
            Utils.message("An error occurred, please DELETE .gitlet directory and try again!");
        }
    }

    public static void add(String filePath) {
        // Check the existence of .gitlet Directory
        if (!initializedGitlet())
            return;

        // Read Staging area file if it is existed
        if (INDEX.exists())
        {
            try {
                st = Utils.readObject(INDEX, StagingArea.class);
                // Cleared Stage
                if (st.iscleared())
                    st = new StagingArea();
            } catch (IllegalArgumentException e){
                System.out.println(e.getMessage());
            }
        }
        else
            st = new StagingArea();

        // Check the existence of the file, push it to Staging Area, and save Staging Area to hard disk
        if (!Utils.join(Repository.CWD, filePath).exists())
        {
            Utils.message("File does not exist.");
            return;
        }

        try {
            st.addFile(filePath);
            if (st != null)
                st.displayStagedFiles();
            Utils.writeObject(INDEX, st);
        } catch (Exception e) {
            System.out.println("Exception err: " + e);
        }
    }

    public static void commit(String message) {
        // Check the existence of .gitlet Directory
        if (!initializedGitlet())
            return;

        if (message == null || message.isEmpty())
        {
            Utils.message("Please enter a commit message.");
            return;
        }

        Commit commit = new Commit(message);

        // Fetch the previous commit and update the current commit parent
        String previousCommit = Utils.readContentsAsString(HEAD);
        commit.setParent(previousCommit);

        // Get current date and time in UTC and Define the format
        ZonedDateTime now = ZonedDateTime.now(java.time.ZoneOffset.UTC);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
                "HH:mm:ss 'UTC', EEEE, d MMMM yyyy", Locale.ENGLISH
        );
        // Format the current date and time and update Commit Timestamp
        commit.setTimeStamp(now.format(formatter));

        // Nothing to be committed if Staging Area was cleared
        StagingArea stage;
        try {
            stage = Utils.readObject(Repository.INDEX, StagingArea.class);
        } catch (IllegalArgumentException e) {
            Utils.message("No changes added to the commit.");
            return;
        }
        if (stage.iscleared())
        {

            Utils.message("No changes added to the commit.");
            return;
        }

        HashMap<String, StagedFile> stagedFiles = stage.getStagedFiles();
        Tree commitTree = new Tree();
        TreeEntry entry = new TreeEntry();

        // Getting previous commit tree and storing it in the current commit tree
        File f = Utils.join(COMMITS, previousCommit);
        Commit parent = Utils.readObject(f, Commit.class);
        if (parent.getParent() != null)
        {
            File t = Utils.join(TREES, parent.getTree());
            Tree parentTree = Utils.readObject(t, Tree.class);
            for (TreeEntry element : parentTree.getTree().values())
                commitTree.addBlob(element.getPath(), element);
        }

        // Storing Staged files in commit tree
        for (HashMap.Entry<String, StagedFile> e : stagedFiles.entrySet()) {
            // Staged Removed File (File that was in the directory in HEAD Commit and was deleted by user)
            if (e.getValue() == null)
            {
                if (commitTree.getTree().containsKey(e.getKey()))
                    commitTree.removeBlob(e.getKey());
                continue;
            }

            entry.setPath(e.getKey());
            entry.setHash(e.getValue().getBlob().getHash());
            commitTree.addBlob(entry.getPath(), entry);

            // Writing blobs files to hard disk
            String SHA1 = e.getValue().getBlob().getHash();
            File SHA1Blob = Utils.join(BLOBS, SHA1);
            Utils.writeObject(SHA1Blob, e.getValue().getBlob());
            System.out.println("Blob Hash: " + SHA1);
        }

        // Writing Tree as a file
        String treeSHA1 = commitTree.hashTreeObject();
        File treeFile = Utils.join(TREES, treeSHA1);
        Utils.writeObject(treeFile, commitTree);
        commit.setTree(treeSHA1);
        System.out.println("Tree Hash: " + treeSHA1);

        // Writing Commit as a file
        String SHA1 = commit.hashCommitObject();
        File SHA1Commit = Utils.join(COMMITS, SHA1);
        Utils.writeObject(SHA1Commit, commit);
        Utils.writeContents(HEAD, SHA1);
        System.out.println("Commit Hash: " + SHA1);

        // Clear Staging Area
        stage.clearStage();
    }

    public static void rm(String filePath)
    {
        // Check the existence of .gitlet Directory
        if (!initializedGitlet())
            return;

        // Is file staged
        StagingArea stage;
        try {
            stage = Utils.readObject(Repository.INDEX, StagingArea.class);
        } catch (IllegalArgumentException e) {
            // INDEX file is not existed, which means there is no commits except initial commit
            Utils.message("No reason to remove the file.");
            return;
        }
        // INDEX file exist and there are staged files
        if (!stage.iscleared())
        {
            if (stage.getStagedFiles().containsKey(filePath))
            {
                // Check if file was staged as a removed one (Was in the HEAD commit and removed from Working Dir)
                StagedFile sFile = stage.getStagedFiles().get(filePath);
                if (sFile == null) return;

                stage.removeFile(filePath);
                Utils.writeObject(Repository.INDEX, stage);
            }
            return;
        }
        else if (stage.iscleared()){
            stage = new StagingArea();
        }

        try {
            // Get Tree
            File head = Utils.join(COMMITS, Utils.readContentsAsString(HEAD));
            Commit c = Utils.readObject(head, Commit.class);
            File treePath = Utils.join(TREES, c.getTree());
            Tree tree = Utils.readObject(treePath, Tree.class);
            HashMap<String, TreeEntry> treeMap = tree.getTree();

            // Check if HEAD commit contains the file
            if (treeMap.containsKey(filePath))
            {
                // Stage the removed file
                stage.stageRemovedFile(filePath);
                try {
                    Utils.writeObject(INDEX, stage);
                } catch (IllegalArgumentException e) {
                    System.out.println("INDEX file error: " + e);
                }

                try {
                    File f = new File(filePath);
                    if (f.exists())
                    {
                        // Delete file from hard Disk
                        boolean flag = Utils.restrictedDelete(filePath);
                        if (!flag)
                            System.out.println("Unknown Error!!!");
                    }
                } catch (IllegalArgumentException e) {
                    System.out.println("Deletion error: " + e);
                }

                return;
            }
        } catch (IllegalArgumentException e) {
            Utils.message("No reason to remove the file.");
        }

        // File is neither Staged nor Tracked by the HEAD Commit
        Utils.message("No reason to remove the file.");
    }

    /** TODO:For merge commits (those that have two parent commits), add a line just below the first, as in
     * ===
     * commit 3e8bf1d794ca2e9ef8a4007275acf3751c7170ff
     * Merge: 4975af1 2c1ead1
     * Date: Sat Nov 11 12:30:00 2017 -0800
     * Merged development into master.
     *
     * where the two hexadecimal numerals following “Merge:” consist of the first seven digits of the first and second parents’ commit ids, in that order.
     * The first parent is the branch you were on when you did the merge; the second is that of the merged-in branch.
     * */

    public static void log()
    {
        if (HEAD.exists())
        {
            String SHA = Utils.readContentsAsString(HEAD);
            File commitSHA1 = Utils.join(COMMITS, SHA);
            try {
                Commit currentCommit = Utils.readObject(commitSHA1, Commit.class);
                while (true)
                {
                    System.out.println("Commit " + SHA);
                    System.out.println("Date: " + currentCommit.getTimeStamp());
                    Utils.message(currentCommit.getMessage());
                    System.out.println();
                    Utils.message("===");
                    SHA = currentCommit.getParent();
                    if (SHA == null) break;
                    commitSHA1 = Utils.join(COMMITS, SHA);
                    currentCommit = Utils.readObject(commitSHA1, Commit.class);
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Log Error: " + e.getMessage());
            }
        }
    }

    public static void global_Log()
    {
        if (COMMITS.exists())
        {
            List<String> l = Utils.plainFilenamesIn(COMMITS);
            if (l != null) {
                for (String SHA: l)
                {
                    File commitSHA1 = Utils.join(COMMITS, SHA);
                    Commit currentCommit = Utils.readObject(commitSHA1, Commit.class);
                    System.out.println("Commit " + SHA);
                    System.out.println("Date: " + currentCommit.getTimeStamp());
                    Utils.message(currentCommit.getMessage());
                    System.out.println();
                    Utils.message("===");
                }
            }
        }
    }
}
