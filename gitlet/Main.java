package gitlet;

import java.io.File;
import java.util.HashMap;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Abdelrahman Ezz
 */
public class Main {

    /**
     * Checks the number of arguments versus the expected number,
     * print the message to console if they do not match.
     *
     * @param args Argument array from command line
     * @param n Number of expected arguments
     */
    public static boolean validateNumArgs(String[] args, int n) {
        if (args.length != n) {
            return false;
        }
        return true;
    }

    private static void testingCommitMethod()
    {
        File f = Utils.join(Repository.COMMITS, "14412c3145b54ba0245f610603a410c8619317f8");
        Commit c = Utils.readObject(f, Commit.class);
        System.out.println("************** Commit details **************");
        System.out.println("Message: " + c.getMessage());
        System.out.println("Tree: " + c.getTree());
        System.out.println("Parent: " + c.getParent());
        f = Utils.join(Repository.TREES, "74f505bcc47628e9752ed95ad17d66b881445a65");
        Tree t = Utils.readObject(f, Tree.class);
        System.out.println("************** Tree details **************");
        for (TreeEntry e: t.getTree().values()) {
            System.out.println("Path: " + e.getPath());
            System.out.println("Hash: " + e.getHash());
        }
        System.out.println("************** Blob details **************");
        f = Utils.join(Repository.BLOBS, "6380979c8b7cc0f35e8d29dde473ed15c97fe8eb");
        Blob b = Utils.readObject(f, Blob.class);
        System.out.println("Content: " + b.getContent());

        System.out.println("************** Staging Area details **************");
        StagingArea sa = Utils.readObject(Repository.INDEX, StagingArea.class);
        if (!sa.iscleared()) {
            for (HashMap.Entry<String, StagedFile> entry : sa.getStagedFiles().entrySet()) {
                if (entry.getValue() != null)
                {
                    Utils.message("File: " + entry.getKey() + " -> Blob Hash: " + entry.getValue().getBlob().getHash());
                    Utils.message("Content: " + entry.getValue().getBlob().getContent());
                }
            }
        }
        else
            Utils.message("Staging Area is cleared");
    }

    private static void testing_rm_Method()
    {
        File f = Utils.join(Repository.COMMITS, "14412c3145b54ba0245f610603a410c8619317f8");
        Commit c = Utils.readObject(f, Commit.class);
        System.out.println("************** Commit details **************");
        System.out.println("Message: " + c.getMessage());
        System.out.println("Tree: " + c.getTree());
        System.out.println("Parent: " + c.getParent());
        f = Utils.join(Repository.TREES, "74f505bcc47628e9752ed95ad17d66b881445a65");
        Tree t = Utils.readObject(f, Tree.class);
        System.out.println("************** Tree details **************");
        for (TreeEntry e: t.getTree().values()) {
            System.out.println("Path: " + e.getPath());
            System.out.println("Hash: " + e.getHash());
        }

        System.out.println("************** Staging Area details **************");
        StagingArea sa = Utils.readObject(Repository.INDEX, StagingArea.class);
        if (!sa.iscleared())
        {
            for (HashMap.Entry<String, StagedFile> entry : sa.getStagedFiles().entrySet()) {
                Utils.message("File: " + entry.getKey() + " -> Blob Hash: " + entry.getValue());
            }
        }
        else
            Utils.message("Staging Area is cleared");
    }

    private static void checker(int inpt)
    {
        /**************************************** Testing add method and staging area ****************************************/
        if (inpt == 1)
        {
//                StagingArea sa = Utils.readObject(Repository.INDEX, StagingArea.class);
//                sa.displayStagedFiles();
        }
        /**************************************** Testing commit method ****************************************/
        else if (inpt == 2) {
            testingCommitMethod();
        }
        /**************************************** Testing rm method ****************************************/
        else if (inpt == 3) {
            testing_rm_Method();
        }
    }

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args.length == 0)
        {
            Utils.message("Please enter a command.");
            return;
        }

        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                if (!validateNumArgs(args, 1)) {
                    Utils.message("Incorrect operands.");
                    break;
                }
                Repository.init();
                break;

            case "add":
                if (!validateNumArgs(args, 2)) {
                    Utils.message("Incorrect operands.");
                    break;
                }
                Repository.add(args[1]);
                break;

            case "commit":
                if (!validateNumArgs(args, 2)) {
                    Utils.message("Incorrect operands.");
                    break;
                }
                Repository.commit(args[1]);
                break;
            case "rm":
                if (!validateNumArgs(args, 2)) {
                    Utils.message("Incorrect operands.");
                    break;
                }
                Repository.rm(args[1]);
                break;

            case "log":
                if (!validateNumArgs(args, 1)) {
                    Utils.message("Incorrect operands.");
                    break;
                }
                Repository.log();
                break;
            case "global-log":
                if (!validateNumArgs(args, 1)) {
                    Utils.message("Incorrect operands.");
                    break;
                }
                Repository.global_Log();
                break;
            case "find":
                if (!validateNumArgs(args, 2)) {
                    Utils.message("Incorrect operands.");
                    break;
                }
                Repository.find(args[1]);
                break;
            case "status":
                if (!validateNumArgs(args, 1)) {
                    Utils.message("Incorrect operands.");
                    break;
                }
                Repository.status();
                break;
            case "checkout":
                if (!validateNumArgs(args, 2) && !validateNumArgs(args, 3) && !validateNumArgs(args, 4)) {
                    Utils.message("Incorrect operands.");
                    break;
                }
                Repository.checkout(args);
                break;
            case "branch":
                if (!validateNumArgs(args, 2)) {
                    Utils.message("Incorrect operands.");
                    break;
                }
                Repository.branch(args[1]);
                break;

            default:
                Utils.message("No command with that name exists.");
                checker(2);
        }
    }
}
