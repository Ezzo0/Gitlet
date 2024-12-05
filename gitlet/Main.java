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
            Utils.message("Incorrect operands.");
            return false;
        }
        return true;
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
            File f = Utils.join(Repository.COMMITS, "0500610a590ca43ba6f78cacc6a7dc88d9543a61");
            Commit c = Utils.readObject(f, Commit.class);
            System.out.println("************** Commit details **************");
            System.out.println("Message: " + c.getMessage());
            System.out.println("Tree: " + c.getTree());
            System.out.println("Parent: " + c.getParent());
            f = Utils.join(Repository.TREES, "e0a5c367d247a6b6abed72b36ee59cf55ba9fe6f");
            Tree t = Utils.readObject(f, Tree.class);
            System.out.println("************** Tree details **************");
            for (TreeEntry e: t.getTree().values()) {
                System.out.println("Path: " + e.getPath());
                System.out.println("Hash: " + e.getHash());
            }
            System.out.println("************** Blob details **************");
            f = Utils.join(Repository.BLOBS, "4d049b8e5d6a72c315854c2b29528f205d894de4");
            Blob b = Utils.readObject(f, Blob.class);
            System.out.println("Content: " + b.getContent());


            f = Utils.join(Repository.COMMITS, "c7bda6817dc253125dd087af21a85eafebf991b0");
            c = Utils.readObject(f, Commit.class);
            System.out.println("************** Commit details **************");
            System.out.println("Message: " + c.getMessage());
            System.out.println("Tree: " + c.getTree());
            System.out.println("Parent: " + c.getParent());
            f = Utils.join(Repository.TREES, "8783b38508018b391a99e0e2f19efb47bad35762");
            t = Utils.readObject(f, Tree.class);
            System.out.println("************** Tree details **************");
            for (TreeEntry e: t.getTree().values()) {
                System.out.println("Path: " + e.getPath());
                System.out.println("Hash: " + e.getHash());
            }
            System.out.println("************** Blob details **************");
            f = Utils.join(Repository.BLOBS, "79f4ca438c8fe12f1b498e5c6aaf95d91bf9a20e");
            b = Utils.readObject(f, Blob.class);
            System.out.println("Content: " + b.getContent());

            System.out.println("************** Staging Area details **************");
            StagingArea sa = Utils.readObject(Repository.INDEX, StagingArea.class);
            if (!sa.iscleared())
                sa.displayStagedFiles();
            else
                Utils.message("Staging Area is cleared");

        }
        /**************************************** Testing rm method ****************************************/
        else if (inpt == 3) {
            File f = Utils.join(Repository.COMMITS, "89a157e08b6b4a95e6d38a01cba9dba87bf137f6");
            Commit c = Utils.readObject(f, Commit.class);
            System.out.println("************** Commit details **************");
            System.out.println("Message: " + c.getMessage());
            System.out.println("Tree: " + c.getTree());
            System.out.println("Parent: " + c.getParent());
            f = Utils.join(Repository.TREES, "2101afb583fc140e2bd5ab658b4427bb574ead01");
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
                if (!validateNumArgs(args, 1))  break;
                Repository.init();
                break;

            case "add":
                if (!validateNumArgs(args, 2))  break;
                Repository.add(args[1]);
                break;

            case "commit":
                if (!validateNumArgs(args, 2))  break;
                Repository.commit(args[1]);
                break;
            case "rm":
                if (!validateNumArgs(args, 2)) break;
                Repository.rm(args[1]);
                break;

            case "log":
                if (!validateNumArgs(args, 1)) break;
                Repository.log();
                break;
            case "global-log":
                if (!validateNumArgs(args, 1)) break;
                Repository.global_Log();
                break;

            default:
                Utils.message("No command with that name exists.");
                checker(3);
        }
    }
}
