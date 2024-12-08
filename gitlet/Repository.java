package gitlet;

import java.io.File;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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

        // Updating head and branch files
        File branchFile = Utils.join(BRANCH, commit.getBranch());
        Utils.writeContents(HEAD, commit.getBranch());
        Utils.writeContents(branchFile, SHA1);
        System.out.println("Commit Hash: " + SHA1);

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
                    File treeSHA = Utils.join(TREES, commitTree);
                    Tree tree = Utils.readObject(treeSHA, Tree.class);
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
            String commitID = args[1];
            String fileName = args[3];
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
                File treeSHA = Utils.join(TREES, commitTree);
                Tree tree = Utils.readObject(treeSHA, Tree.class);
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
            /* java gitlet.Main checkout [branch name] */
            String branch = args[1];
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
            File treeSHA = Utils.join(TREES, commitTree);
            Tree tree = Utils.readObject(treeSHA, Tree.class);

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
    }
}
