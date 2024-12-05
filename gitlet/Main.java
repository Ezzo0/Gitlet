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
            File f = Utils.join(Repository.OBJs, "6932eba879e350ca14059bde20a249d5c89be204");
            Commit c = Utils.readObject(f, Commit.class);
            System.out.println("************** Commit details **************");
            System.out.println("Message: " + c.getMessage());
            System.out.println("Tree: " + c.getTree());
            System.out.println("Parent: " + c.getParent());
            f = Utils.join(Repository.OBJs, "1c51c9a3b2256071bb8ce97819777d8492350a4f");
            Tree t = Utils.readObject(f, Tree.class);
            System.out.println("************** Tree details **************");
            for (TreeEntry e: t.getTree().values()) {
                System.out.println("Path: " + e.getPath());
                System.out.println("Hash: " + e.getHash());
            }
            System.out.println("************** Blob details **************");
            f = Utils.join(Repository.OBJs, "303ec5505566359202d6042059e8eaa92f1b2783");
            Blob b = Utils.readObject(f, Blob.class);
            System.out.println("Content: " + b.getContent());


            f = Utils.join(Repository.OBJs, "5a9c3c2e79b184a5b0f313aa7df578704e687af6");
            c = Utils.readObject(f, Commit.class);
            System.out.println("************** Commit details **************");
            System.out.println("Message: " + c.getMessage());
            System.out.println("Tree: " + c.getTree());
            System.out.println("Parent: " + c.getParent());
            f = Utils.join(Repository.OBJs, "d263dfdc0da07694768f4fcf6e272d8aecdd9e84");
            t = Utils.readObject(f, Tree.class);
            System.out.println("************** Tree details **************");
            for (TreeEntry e: t.getTree().values()) {
                System.out.println("Path: " + e.getPath());
                System.out.println("Hash: " + e.getHash());
            }
            System.out.println("************** Blob details **************");
            f = Utils.join(Repository.OBJs, "c5fb1a437d2a9389b6acc2e482a224195b4b603e");
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
            File f = Utils.join(Repository.OBJs, "eb20ac3528dbc904afc225af23e2fdfd9bc67bc4");
            Commit c = Utils.readObject(f, Commit.class);
            System.out.println("************** Commit details **************");
            System.out.println("Message: " + c.getMessage());
            System.out.println("Tree: " + c.getTree());
            System.out.println("Parent: " + c.getParent());
            f = Utils.join(Repository.OBJs, "a3b15a1ad4e0c5e7c62c570b06915e4ff62b37e6");
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

            default:
                Utils.message("No command with that name exists.");
                checker(3);
        }
    }
}
