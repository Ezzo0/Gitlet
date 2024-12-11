package gitlet;

import jdk.jshell.execution.Util;

import java.io.File;
import java.rmi.server.UID;
import java.util.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

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
    /** Staging Area file */
    public static final File INDEX = Utils.join(GITLET_DIR, "index");
    /** The head pointer file */
    public static final File BRANCH = Utils.join(GITLET_DIR, "branches");
    private static StagingArea st;
    private static boolean conflict = false;


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
        BRANCH.mkdir();

        // Define an init commit and save it to hard disk
        Commit initialCommit = new Commit("initial commit");
        try {
            // Writing Commit as object file
            String SHA1 = initialCommit.hashCommitObject();
            File SHA1Commit = Utils.join(COMMITS, SHA1);
            Utils.writeObject(SHA1Commit, initialCommit);

            // Writing head and branch files
            File branchFile = Utils.join(BRANCH, initialCommit.getBranch());
            Utils.writeContents(HEAD, initialCommit.getBranch());
            Utils.writeContents(branchFile, SHA1);
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

        // Fetch the previous commit and update the current commit parent and branch
        String branch = Utils.readContentsAsString(HEAD);
        File head = Utils.join(BRANCH, branch);
        String previousCommit = Utils.readContentsAsString(head);
        commit.setParent(previousCommit);
        commit.setBranch(branch);

        // Get the current date and time
        Date now = new Date();
        // Create a formatter for the desired format
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");

        // Set the formatter to use the system's default timezone
        sdf.setTimeZone(TimeZone.getDefault());
        // Format the current date and time and update Commit Timestamp
        commit.setTimeStamp(sdf.format(now));

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
        }

        // Writing Tree as a file
        String treeSHA1 = commitTree.hashTreeObject();
        File treeFile = Utils.join(TREES, treeSHA1);
        Utils.writeObject(treeFile, commitTree);
        commit.setTree(treeSHA1);

        // Writing Commit as a file
        String SHA1 = commit.hashCommitObject();
        File SHA1Commit = Utils.join(COMMITS, SHA1);
        Utils.writeObject(SHA1Commit, commit);

        // Updating head and branch files
        File branchFile = Utils.join(BRANCH, commit.getBranch());
        Utils.writeContents(HEAD, commit.getBranch());
        Utils.writeContents(branchFile, SHA1);

        // Clear Staging Area
        stage.clearStage();
    }

    public static void rm(String filePath) {
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
            String currentBranch = Utils.readContentsAsString(HEAD);
            File branch = Utils.join(BRANCH, currentBranch);
            String commitHash = Utils.readContentsAsString(branch);

            File head = Utils.join(COMMITS, commitHash);
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

    /** TODO: For merge commits (those that have two parent commits), add a line just below the first, as in
     * ===
     * commit 3e8bf1d794ca2e9ef8a4007275acf3751c7170ff
     * Merge: 4975af1 2c1ead1
     * Date: Sat Nov 11 12:30:00 2017 -0800
     * Merged development into master.
     *
     * where the two hexadecimal numerals following “Merge:” consist of the first seven digits of the first and second parents’ commit ids, in that order.
     * The first parent is the branch you were on when you did the merge; the second is that of the merged-in branch.
     * */

    public static void log() {
        // Check the existence of .gitlet Directory
        if (!initializedGitlet())
            return;

        if (HEAD.exists())
        {
            String currentBranch = Utils.readContentsAsString(HEAD);
            File branch = Utils.join(BRANCH, currentBranch);
            String SHA = Utils.readContentsAsString(branch);
            File commitSHA1 = Utils.join(COMMITS, SHA);
            try {
                Commit currentCommit = Utils.readObject(commitSHA1, Commit.class);
                while (true)
                {
                    System.out.println("Commit " + SHA);
                    if (currentCommit.getSecParent() != null)
                        System.out.println("Merge: " + currentCommit.getParent().substring(0,7) + " " + currentCommit.getSecParent().substring(0,7));

                    System.out.println("Date: " + currentCommit.getTimeStamp());
                    Utils.message(currentCommit.getMessage());
                    System.out.println();
                    if (!currentCommit.getMessage().equals("initial commit"))
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

    public static void global_Log() {
        // Check the existence of .gitlet Directory
        if (!initializedGitlet())
            return;

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

    public static void find(String message) {
        // Check the existence of .gitlet Directory
        if (!initializedGitlet())
            return;

        if (COMMITS.exists())
        {
            List<String> l = Utils.plainFilenamesIn(COMMITS);
            if (l != null) {
                for (String SHA: l)
                {
                    File commitSHA1 = Utils.join(COMMITS, SHA);
                    Commit currentCommit = Utils.readObject(commitSHA1, Commit.class);
                    String commitMessage = currentCommit.getMessage();
                    if (message.equals(commitMessage))
                        Utils.message(SHA);
                }
            }
        }
    }

    private static Map<String, String> Tracked() {
        Map<String, String> map = new TreeMap<>();
        // Fetching the current commit
        String branch = Utils.readContentsAsString(HEAD);
        File head = Utils.join(BRANCH, branch);
        String commitHash = Utils.readContentsAsString(head);
        head = Utils.join(COMMITS, commitHash);
        Commit currentCommit = Utils.readObject(head, Commit.class);

        // Fetching the tree of the current commit
        String t = currentCommit.getTree();
        File treeHash = null;
        Tree tree = null;
        if (t != null) {
            treeHash = Utils.join(TREES, t);
            tree = Utils.readObject(treeHash, Tree.class);
        }

        // If current commit is the init commit, we need to create index object(as index object is created with add method)
        try {
            st = Utils.readObject(INDEX, StagingArea.class);
        } catch (IllegalArgumentException e) {
            st = new StagingArea();
        }
        HashMap<String, StagedFile> stagedFiles = st.getStagedFiles();

        List<String> l = Utils.plainFilenamesIn(CWD);
        Map<String, String> workingFiles = new HashMap<>();
        if (l != null) {
            // Modified Files
            for (String s: l)
            {
                File f = new File(s);
                Blob currentBlob = new Blob(f);
                workingFiles.put(s, currentBlob.getHash());
                TreeEntry entry = null;
                if (tree != null)
                    entry = tree.getTree().get(s);

                // Tracked in the current commit?
                if (tree != null && tree.getTree().containsKey(s)) {
                    // Changed in the working directory?
                    if ( !entry.getHash().equals( currentBlob.getHash() ) ) {
                        // Staged?
                        if (!st.iscleared() && stagedFiles.containsKey(s)) {
                            // Different content?
                            if ( !stagedFiles.get(s).getBlob().getHash().equals( currentBlob.getHash() ) )
                                map.put(s, "(modified)");
                        }
                        else
                            map.put(s, "(modified)");
                    }
                    else {
                        // Staged?
                        if (!st.iscleared() && stagedFiles.containsKey(s)) {
                            // Removed with rm command and exist in WD without gitlet knowledge.
                            if ( stagedFiles.get(s) == null )
                                map.put(s, "Untracked");
                        }
                    }
                }
                else {
                    if (st.iscleared()) {
                        map.put(s, "Untracked");
                    }
                    else {
                        // Staged?
                        if (stagedFiles.containsKey(s)) {
                            // Staged for removal, but then re-created without Gitlet’s knowledge.
                            if (stagedFiles.get(s).getBlob() == null) {
                                map.put(s, "Untracked");
                            }
                            else {
                                // Different content?
                                if ( !stagedFiles.get(s).getBlob().getHash().equals( currentBlob.getHash() ) )
                                    map.put(s, "(modified)");
                            }
                        }
                        else
                            map.put(s, "Untracked");
                    }
                }
            }

            if (!st.iscleared()) {
                // Staged for addition, but deleted in the working directory.
                for (HashMap.Entry<String, StagedFile> entry : stagedFiles.entrySet()) {
                    if (entry.getValue() != null)
                        if ( !workingFiles.containsKey( entry.getKey() ) )
                            map.put(entry.getKey(), "(deleted)");
                }
            }

            if (tree != null) {
                // Not staged for removal, but tracked in the current commit and deleted from the working directory.
                for (HashMap.Entry<String, TreeEntry> entry : tree.getTree().entrySet()) {
                    if ( !workingFiles.containsKey( entry.getKey() ) && ( st.iscleared() || !stagedFiles.containsKey( entry.getKey() ) ) )
                        map.put(entry.getKey(), "(deleted)");
                }
            }
        }
        return map;
    }


    public static void status() {
        // Check the existence of .gitlet Directory
        if (!initializedGitlet())
            return;

        // Fetching branches' names
        if (BRANCH.exists() && HEAD.exists())
        {
            List<String> l = Utils.plainFilenamesIn(BRANCH);
            String currentBranch = Utils.readContentsAsString(HEAD);
            if (l != null) {
                Utils.message("=== Branches ===");
                for (String branch: l) {
                    if (branch.equals(currentBranch)) System.out.print("*");
                    Utils.message(branch);
                }
                System.out.println();
            }
        }
        else {
            Utils.message("Unknown Error!!!");
            return;
        }

        Map<String, String> stage = new TreeMap<>();
        // Fetching staging area
        if (INDEX.exists())
        {
            st = Utils.readObject(Repository.INDEX, StagingArea.class);
            if (!st.iscleared()) {
                for (HashMap.Entry<String, StagedFile> entry : st.getStagedFiles().entrySet()) {
                    if (entry.getValue() != null)
                        stage.put(entry.getKey(), "Staged");
                    else
                        stage.put(entry.getKey(), "Removed");
                }
            }
        }

        Map <String, String> m = Tracked();
        Iterator<Map.Entry<String, String>> iterator = stage.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            if (m.containsKey(entry.getKey())) {
                iterator.remove(); // Safe removal
            }
        }

        Utils.message("=== Staged Files ===");
        for (Map.Entry<String, String> entry : stage.entrySet())
            if (entry.getValue().equals("Staged"))
                Utils.message(entry.getKey());
        System.out.println();

        Utils.message("=== Removed Files ===");
        for (Map.Entry<String, String> entry : stage.entrySet())
            if (entry.getValue().equals("Removed"))
                Utils.message(entry.getKey());
        System.out.println();

        /// Modified but not staged for commit
        Utils.message("=== Modifications Not Staged For Commit ===");
        /*
          Tracked in the current commit, changed in the working directory, but not staged; or (done)
          Staged for addition, but with different contents than in the working directory; or (done)
          Staged for addition, but deleted in the working directory; or (done)
          Not staged for removal, but tracked in the current commit and deleted from the working directory. (done)
          */
        for (Map.Entry<String, String> entry : m.entrySet())
            if (!entry.getValue().equals("Untracked"))
                Utils.message(entry.getKey() + " " + entry.getValue());
        System.out.println();


        Utils.message("=== Untracked Files ===");
        for (Map.Entry<String, String> entry : m.entrySet())
            if (entry.getValue().equals("Untracked"))
                Utils.message(entry.getKey());
        System.out.println();
    }

    private static void checkoutCommitID(String id, String name) {
        String commitID = id;
        String fileName = name;
        try {
            File commitSHA = Utils.join(COMMITS, commitID);
            Commit c;
            try {
                c = Utils.readObject(commitSHA, Commit.class);
            } catch (IllegalArgumentException e) {
                Utils.message("No commit with that id exists");
                return;
            }
            String commitTree = c.getTree();
            Tree tree = null;
            if (commitTree != null) {
                File treeSHA = Utils.join(TREES, commitTree);
                tree = Utils.readObject(treeSHA, Tree.class);
            } else {
                tree = new Tree();
            }
            if (tree.getTree().containsKey(fileName)) {
                // Remove the file from staging area if it is staged
                st = Utils.readObject(INDEX, StagingArea.class);
                if (!st.iscleared())
                    st.removeFile(fileName);

                // Fetching the content
                String blobHash = tree.getTree().get(fileName).getHash();
                File f = Utils.join(BLOBS, blobHash);
                Blob blob = Utils.readObject(f, Blob.class);
                String content = blob.getContent();

                // Overwriting content
                try {
                    File path = Utils.join(CWD, fileName);
                    Utils.writeContents(path, content);
                } catch (IllegalArgumentException e) {
                    Utils.message("Unknown Error!!!");
                }

            } else {
                Utils.message("File does not exist in that commit.");
            }
        } catch (Exception e) {
            Utils.message("File does not exist in that commit.");
        }
    }

    private static void checkoutBranch(String b) {
        String branch = b;
        String currentBranch = Utils.readContentsAsString(HEAD);
        if (branch.equals(currentBranch)) {
            Utils.message("No need to checkout the current branch.");
            return;
        }
        File head = Utils.join(BRANCH, branch);
        String previousCommit = null;
        try {
            previousCommit = Utils.readContentsAsString(head);
        } catch (IllegalArgumentException e) {
            Utils.message("No such branch exists.");
            return;
        }
        File commitSHA = Utils.join(COMMITS, previousCommit);
        Commit c = Utils.readObject(commitSHA, Commit.class);
        String commitTree = c.getTree();
        Tree tree = null;
        if (commitTree != null) {
            File treeSHA = Utils.join(TREES, commitTree);
            tree = Utils.readObject(treeSHA, Tree.class);
        } else {
            tree = new Tree();
        }

        List<String> l = Utils.plainFilenamesIn(CWD);

        Map<String, String> untracked = Tracked();
        for (String file: l) {
            // Checking the existence of untracked files
            if (untracked.containsKey(file) && untracked.get(file).equals("Untracked") ) {
                Utils.message("There is an untracked file in the way; delete it, or add and commit it first.");
                return;
            }
        }


        // Deleting tracked files that are not in checked-out branch tree
        for (String file: l) {
            if (!tree.getTree().containsKey(file)) {
                Utils.restrictedDelete(file);
            }
        }

        for (HashMap.Entry<String, TreeEntry> entry : tree.getTree().entrySet()) {
            if (entry.getValue() != null) {
                try {
                    // Fetching the content
                    String blobHash = entry.getValue().getHash();
                    File f = Utils.join(BLOBS, blobHash);
                    Blob blob = Utils.readObject(f, Blob.class);
                    String content = blob.getContent();

                    // Overwriting the content
                    f = Utils.join(CWD, entry.getKey());
                    Utils.writeContents(f, content);

                } catch (IllegalArgumentException e) {
                    Utils.message("Unknown Error !!!");
                }
            }
        }

        // Changing the current branch (HEAD) to the given branch.
        Utils.writeContents(HEAD, branch);

        // Clearing Staging Area
        st = Utils.readObject(INDEX, StagingArea.class);
        st.clearStage();
        Utils.writeObject(INDEX, st);
    }

    public static void checkout(String[] args) {
        // Check the existence of .gitlet Directory
        if (!initializedGitlet())
            return;

        if (args.length == 3) {
            /* java gitlet.Main checkout -- [file name] */
            String fileName = args[2];

            // Fetch the previous commit
            if (HEAD.exists() && BRANCH.exists()) {
                try {
                    String branch = Utils.readContentsAsString(HEAD);
                    File head = Utils.join(BRANCH, branch);
                    String previousCommit = Utils.readContentsAsString(head);

                    File commitSHA = Utils.join(COMMITS, previousCommit);
                    Commit c = Utils.readObject(commitSHA, Commit.class);
                    String commitTree = c.getTree();
                    Tree tree = null;
                    if (commitTree != null) {
                        File treeSHA = Utils.join(TREES, commitTree);
                        tree = Utils.readObject(treeSHA, Tree.class);
                    } else {
                        tree = new Tree();
                    }
                    if (tree.getTree().containsKey(fileName)) {
                        // Remove the file from staging area if it is staged
                        st = Utils.readObject(INDEX, StagingArea.class);
                        if (!st.iscleared())
                            st.removeFile(fileName);

                        // Fetching the content
                        String blobHash = tree.getTree().get(fileName).getHash();
                        File f = Utils.join(BLOBS, blobHash);
                        Blob blob = Utils.readObject(f, Blob.class);
                        String content = blob.getContent();

                        // Overwriting content
                        try {
                            File path = Utils.join(CWD, fileName);
                            Utils.writeContents(path, content);
                        } catch (IllegalArgumentException e) {
                            Utils.message("Unknown Error!!!");
                        }
                    } else {
                        Utils.message("File does not exist in that commit.");
                    }
                } catch (Exception e) {
                    Utils.message("File does not exist in that commit.");
                }
            } else {
                Utils.message("File does not exist in that commit.");
                return;
            }
        } else if (args.length == 4) {
            /* java gitlet.Main checkout [commit id] -- [file name] */
            checkoutCommitID(args[1], args[3]);

        } else {
            /* java gitlet.Main checkout [branch name] */
            checkoutBranch(args[1]);
        }
    }

    public static void branch(String newBranch) {
        // Check the existence of .gitlet Directory
        if (!initializedGitlet())
            return;

        // Check the existance of the branch
        if (Utils.join(BRANCH, newBranch).exists()) {
            Utils.message("A branch with that name already exists.");
            return;
        }

        // Creating new branch
        if (HEAD.exists()) {
            String currentBranch = Utils.readContentsAsString(HEAD);
            File f = Utils.join(BRANCH, currentBranch);
            String currentCommit = Utils.readContentsAsString(f);

            f = Utils.join(BRANCH, newBranch);
            Utils.writeContents(f, currentCommit);
        } else
            Utils.message("Unknown Error !!!");
    }

    public static void rmBranch(String branch) {
        // Check the existence of .gitlet Directory
        if (!initializedGitlet())
            return;

        if (HEAD.exists()) {
            String currentBranch = Utils.readContentsAsString(HEAD);
            if (currentBranch.equals(branch)) {
                Utils.message("Cannot remove the current branch.");
                return;
            }

            File path = Utils.join(BRANCH, branch);
            if (path.exists()) {
                if (!path.delete())
                    Utils.message("Unknown Error !!!");
            }
            else
                Utils.message("A branch with that name does not exist.");
        } else
            Utils.message("Unknown Error !!!");
    }

    public static void reset(String commitID) {
        // Check the existence of .gitlet Directory
        if (!initializedGitlet())
            return;

        File path = Utils.join(COMMITS, commitID);
        if (!path.exists()) {
            Utils.message("No commit with that id exists.");
            return;
        }
        Commit commit = Utils.readObject(path, Commit.class);
        String commitTree = commit.getTree();
        Tree tree = null;
        if (commitTree != null) {
            File treeSHA = Utils.join(TREES, commitTree);
            tree = Utils.readObject(treeSHA, Tree.class);
        } else {
            tree = new Tree();
        }

        List<String> l = Utils.plainFilenamesIn(CWD);

        Map<String, String> untracked = Tracked();
        for (String file: l) {
            // Checking the existence of untracked files
            if (untracked.containsKey(file) && untracked.get(file).equals("Untracked") ) {
                Utils.message("There is an untracked file in the way; delete it, or add and commit it first.");
                return;
            }
        }

        // Deleting tracked files that are not in checked-out commit tree
        for (String file: l) {
            if (!tree.getTree().containsKey(file)) {
                Utils.restrictedDelete(file);
            }
        }

        for (HashMap.Entry<String, TreeEntry> entry : tree.getTree().entrySet()) {
            if (entry.getValue() != null) {
                try {
                    // Fetching the content
                    String blobHash = entry.getValue().getHash();
                    File f = Utils.join(BLOBS, blobHash);
                    Blob blob = Utils.readObject(f, Blob.class);
                    String content = blob.getContent();

                    // Overwriting the content
                    f = Utils.join(CWD, entry.getKey());
                    Utils.writeContents(f, content);

                } catch (IllegalArgumentException e) {
                    Utils.message("Unknown Error !!!");
                }
            }
        }

        // Changing the current branch (HEAD) to the given branch.
        Utils.writeContents(HEAD, commit.getBranch());

        // Clearing Staging Area
        st = Utils.readObject(INDEX, StagingArea.class);
        st.clearStage();
        Utils.writeObject(INDEX, st);
    }

    public static Commit findMergeBase(Commit inComingBranch, Commit currentBranch) {
        // Visited sets for both branches
        Set<String> visitedBranch1 = new HashSet<>();
        Set<String> visitedBranch2 = new HashSet<>();

        // Queues for BFS
        Queue<Commit> queue1 = new LinkedList<>();
        Queue<Commit> queue2 = new LinkedList<>();

        queue1.add(inComingBranch);
        queue2.add(currentBranch);

        File path;
        Commit parent;
        // Perform BFS for both branches
        while (!queue1.isEmpty() || !queue2.isEmpty()) {
            // Process inComingBranch
            if (!queue1.isEmpty()) {
                Commit current1 = queue1.poll();
                if (visitedBranch2.contains(current1.getSHA()))
                    return current1; // Found common ancestor

                visitedBranch1.add(current1.getSHA());
                if (current1.getParent() != null) {
                    path = Utils.join(COMMITS, current1.getParent());
                    parent = Utils.readObject(path, Commit.class);
                    queue1.add(parent);
                }
                if (current1.getSecParent() != null) {
                    path = Utils.join(COMMITS, current1.getSecParent());
                    parent = Utils.readObject(path, Commit.class);
                    queue1.add(parent);
                }
            }

            // Process currentBranch
            if (!queue2.isEmpty()) {
                Commit current2 = queue2.poll();
                if (visitedBranch1.contains(current2.getSHA()))
                    return current2; // Found common ancestor

                visitedBranch2.add(current2.getSHA());
                if (current2.getParent() != null) {
                    path = Utils.join(COMMITS, current2.getParent());
                    parent = Utils.readObject(path, Commit.class);
                    queue2.add(parent);
                }
                if (current2.getSecParent() != null) {
                    path = Utils.join(COMMITS, current2.getSecParent());
                    parent = Utils.readObject(path, Commit.class);
                    queue2.add(parent);
                }
            }
        }

        return null; // No common ancestor found
    }

    private static String handleConflict(String name, TreeEntry currentEntry, TreeEntry inComingEntry) {
        File path;
        Blob current, inComing;
        if (currentEntry != null) {
            path = Utils.join(BLOBS, currentEntry.getHash());
            current = Utils.readObject(path, Blob.class);
        } else {
            current = new Blob();
        }

        if (inComingEntry != null) {
            path = Utils.join(BLOBS, inComingEntry.getHash());
            inComing = Utils.readObject(path, Blob.class);
        } else {
            inComing = new Blob();
        }

        String currentContents = current.getContent();
        String givenContents = inComing.getContent();

        conflict = true;

        // Construct conflict content
        return "<<<<<<< HEAD\n" + currentContents + "=======\n" + givenContents + ">>>>>>>\n";
    }

    // Perform a three-way diff between trees
    public static Tree diffTreesWithBase(Commit splitPoint, Commit inComingBranch, Commit currentBranch) {
        File path;
        Tree baseTree;
        if (splitPoint.getTree() != null) {
            path = Utils.join(TREES, splitPoint.getTree());
            baseTree = Utils.readObject(path, Tree.class);
        } else
            baseTree = new Tree();

        Tree currentTree;
        if (currentBranch.getTree() != null) {
            path = Utils.join(TREES, currentBranch.getTree());
            currentTree = Utils.readObject(path, Tree.class);
        } else
            currentTree = new Tree();

        Tree inComingTree;
        if (inComingBranch.getTree() != null) {
            path = Utils.join(TREES, inComingBranch.getTree());
            inComingTree = Utils.readObject(path, Tree.class);
        } else
            inComingTree = new Tree();


        Tree t = new Tree();
        // Union of all entries across the three trees
        Set<String> allEntries = new HashSet<>();
        allEntries.addAll(baseTree.getEntryNames());
        allEntries.addAll(inComingTree.getEntryNames());
        allEntries.addAll(currentTree.getEntryNames());

        for (String name : allEntries) {
            TreeEntry baseEntry = baseTree.getTree().get(name);
            TreeEntry inComingEntry = inComingTree.getTree().get(name);
            TreeEntry currentEntry = currentTree.getTree().get(name);

            if (baseEntry != null) {
                // File present in split point
                if (currentEntry != null && inComingEntry != null) {
                    if ( baseEntry.getHash().equals(currentEntry.getHash()) && !baseEntry.getHash().equals(inComingEntry.getHash()) ) {
                        // Case 1: Modified in the incoming branch since the split point, but not modified in the current branch
                        checkoutCommitID(inComingBranch.getSHA(), name);
                        t.addBlob(name, inComingEntry);
                        try {
                            st = Utils.readObject(INDEX, StagingArea.class);
                        } catch (IllegalArgumentException e) {
                            st = new StagingArea();
                        }
                        if (st.iscleared()) {
                            st = new StagingArea();
                        }
                        st.addFile(name);
                        Utils.writeObject(INDEX, st);
                    } else if ( !baseEntry.getHash().equals(currentEntry.getHash()) && baseEntry.getHash().equals(inComingEntry.getHash()) ) {
                        // Case 2: Modified in the current branch but not in the given branch since the split point
                        // Do nothing
                        t.addBlob(name, currentEntry);
                    } else if ( currentEntry.getHash().equals(inComingEntry.getHash()) ) {
                        // Case 3: Modified in both the current and given branch in the same way (have the same content)
                        // Do nothing
                        t.addBlob(name, currentEntry);
                    } else {
                        // Case 8: Modified in different ways in the current and given branches (contents of both are changed and different from other)
                        String conf = handleConflict(name, currentEntry, inComingEntry);
                        path = Utils.join(CWD, name);
                        Utils.writeContents(path, conf);
                        Blob b = new Blob(path);
                        path = Utils.join(COMMITS, b.getHash());
                        Utils.writeObject(path, b);
                        TreeEntry e = new TreeEntry(name, b.getHash());
                        t.addBlob(name, e);
                    }
                } else if (currentEntry != null && inComingEntry == null) {
                    if (baseEntry.getHash().equals(currentEntry.getHash())) {
                        // Case 6: Unmodified in the current branch, and absent in the incoming branch
                        Repository.rm(name);
                    } else {
                        // Case 8: The contents of one are changed and the other file is deleted
                        String conf = handleConflict(name, currentEntry, inComingEntry);
                        path = Utils.join(CWD, name);
                        Utils.writeContents(path, conf);
                        Blob b = new Blob(path);
                        path = Utils.join(COMMITS, b.getHash());
                        Utils.writeObject(path, b);
                        TreeEntry e = new TreeEntry(name, b.getHash());
                        t.addBlob(name, e);
                    }
                } else if (currentEntry == null && inComingEntry != null) {
                    if (baseEntry.getHash().equals(inComingEntry.getHash())) {
                        // Case 7: Unmodified in the incoming branch, and absent in the current branch
                        // Do nothing
                    } else {
                        // Case 8: The contents of one are changed and the other file is deleted
                        String conf = handleConflict(name, currentEntry, inComingEntry);
                        path = Utils.join(CWD, name);
                        Utils.writeContents(path, conf);
                        Blob b = new Blob(path);
                        path = Utils.join(COMMITS, b.getHash());
                        Utils.writeObject(path, b);
                        TreeEntry e = new TreeEntry(name, b.getHash());
                        t.addBlob(name, e);
                    }
                } else {
                    // Case 3: Modified in both the current and given branch in the same way (both removed)
                    // Do nothing
                }
            } else {
                // File not present in split point
                if (currentEntry != null && inComingEntry != null) {
                    // Case 8: The contents of one are changed and the other file is deleted
                    String conf = handleConflict(name, currentEntry, inComingEntry);
                    path = Utils.join(CWD, name);
                    Utils.writeContents(path, conf);
                    Blob b = new Blob(path);
                    path = Utils.join(COMMITS, b.getHash());
                    Utils.writeObject(path, b);
                    TreeEntry e = new TreeEntry(name, b.getHash());
                    t.addBlob(name, e);
                } else if (currentEntry != null && inComingEntry == null) {
                    // Case 4: Present only in the current branch
                    // Do nothing
                    t.addBlob(name, currentEntry);
                } else if (currentEntry == null && inComingEntry != null) {
                    // Case 5: Present only in the incoming branch
                    checkoutCommitID(inComingBranch.getSHA(), name);
                    t.addBlob(name, inComingEntry);
                    try {
                        st = Utils.readObject(INDEX, StagingArea.class);
                    } catch (IllegalArgumentException e) {
                        st = new StagingArea();
                    }
                    if (st.iscleared()) {
                        st = new StagingArea();
                    }
                    st.addFile(name);
                    Utils.writeObject(INDEX, st);
                }
            }
        }
        return t;
    }

    public static void merge(String branch) {
        // Check the existence of .gitlet Directory
        if (!initializedGitlet())
            return;

        String currentHead = Utils.readContentsAsString(HEAD);
        if (currentHead.equals(branch)) {
            Utils.message("Cannot merge a branch with itself.");
            return;
        }

        List<String> l = Utils.plainFilenamesIn(CWD);
        try {
            st = Utils.readObject(INDEX, StagingArea.class);
        } catch (IllegalArgumentException e) {
            st = new StagingArea();
        }

        Map<String, String> untracked = Tracked();
        for (String file: l) {
            // Checking the existence of untracked files
            if (untracked.containsKey(file) && untracked.get(file).equals("Untracked") ) {
                Utils.message("There is an untracked file in the way; delete it, or add and commit it first.");
                return;
            }
            // Checking the existence of staged additions or removals
            if ( ( !st.iscleared() && st.getStagedFiles().containsKey(file) )
                    || (untracked.containsKey(file) ) ) {
                Utils.message("You have uncommitted changes.");
                return;
            }
        }

        File path = Utils.join(BRANCH, branch);
        String commitSHA;
        Commit inComingBranch;

        // Fetching the incoming branch
        try {
            commitSHA = Utils.readContentsAsString(path);
            path = Utils.join(COMMITS, commitSHA);
            inComingBranch = Utils.readObject(path, Commit.class);
        } catch (IllegalArgumentException e) {
            Utils.message("A branch with that name does not exist.");
            return;
        }

        // Fetching the current branch
        path = Utils.join(BRANCH, currentHead);
        commitSHA = Utils.readContentsAsString(path);
        path = Utils.join(COMMITS, commitSHA);
        Commit currentBranch = Utils.readObject(path, Commit.class);

        // If the merge results in no new changes (because the branches are already identical or equivalent)
        if (currentBranch.getSHA().equals(inComingBranch.getSHA())) {
            Utils.message("No changes added to the commit.");
            return;
        }

        Commit splitPoint = findMergeBase(inComingBranch, currentBranch);
        // If the split point is the same commit as the incoming branch, then we do nothing; the merge is completed.
        if (splitPoint.getSHA().equals(inComingBranch.getSHA())) {
            Utils.message("Given branch is an ancestor of the current branch.");
            return;
        }
        // If the split point is the current branch
        if (splitPoint.getSHA().equals(currentBranch.getSHA())) {
            checkoutBranch(branch);
            Utils.message("Current branch fast-forwarded.");
            return;
        }

        // Getting new tree
        Tree commitTree = diffTreesWithBase(splitPoint, inComingBranch, currentBranch);
        if (conflict) {
            Utils.message("Encountered a merge conflict.");
            conflict = false;
        }
        String SHA = commitTree.hashTreeObject();

        // Writing new tree object
        path = Utils.join(TREES, SHA);
        Utils.writeObject(path, commitTree);

        // Commiting Merge
        String message = "Merged " + inComingBranch.getBranch() + "into "  + currentBranch.getBranch() + ".";
        Commit commit = new Commit(message);
        commit.setParent(currentBranch.getSHA());
        commit.setSecParent(inComingBranch.getSHA());
        commit.setBranch(currentBranch.getBranch());
        commit.setTree(SHA);

        // Get the current date and time
        Date now = new Date();
        // Create a formatter for the desired format
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");

        // Set the formatter to use the system's default timezone
        sdf.setTimeZone(TimeZone.getDefault());
        // Format the current date and time and update Commit Timestamp
        commit.setTimeStamp(sdf.format(now));

        SHA = commit.hashCommitObject();
        path = Utils.join(COMMITS, SHA);
        Utils.writeObject(path, commit);

        // Updating head and branch files
        File branchFile = Utils.join(BRANCH, commit.getBranch());
        Utils.writeContents(branchFile, SHA);

        // Clear Staging Area
        st.clearStage();

    }
}
