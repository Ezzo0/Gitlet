package gitlet;

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
            // TODO: Remove this statement to put it in its place
            System.out.print("Incorrect operands.");
            return false;
        }
        return true;
    }

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args.length == 0)
        {
            System.out.print("Please enter a command.");
            return;
        }

        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                /** Description:
                 *  Creates a new Gitlet version-control system in the current directory.
                 *  This system will automatically start with one commit: a commit that contains no files and has the commit message initial commit.
                 *  It will have a single branch: master, which initially points to this initial commit, and master will be the current branch.
                 *  The timestamp for this initial commit will be 00:00:00 UTC, Thursday, 1 January 1970 in whatever format you choose for dates.
                 *  Since the initial commit in all repositories created by Gitlet will have exactly the same content,
                 *  it follows that all repositories will automatically share this commit (they will all have the same UID)
                 *  and all commits in all repositories will trace back to it.
                 *
                 *
                 *  Failure cases: If there is already a Gitlet version-control system in the current directory, it should abort.
                 *  It should NOT overwrite the existing system with a new one.
                 *  Should print the error message A Gitlet version-control system already exists in the current directory.
                 * */

                Repository.init();
                break;
            case "add":
                // TODO: handle the `add [filename]` command

                break;
            // TODO: FILL THE REST IN
            default:
                System.out.print("No command with that name exists.");
        }
    }
}
