package gitlet;

import java.util.HashMap;
import java.util.Map;

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
            case "rm-branch":
                if (!validateNumArgs(args, 2)) {
                    Utils.message("Incorrect operands.");
                    break;
                }
                Repository.rmBranch(args[1]);
                break;
            case "reset":
                if (!validateNumArgs(args, 2)) {
                    Utils.message("Incorrect operands.");
                    break;
                }
                Repository.reset(args[1]);
                break;
            case "merge":
                if (!validateNumArgs(args, 2)) {
                    Utils.message("Incorrect operands.");
                    break;
                }
                Repository.merge(args[1]);
                break;

            default:
                Utils.message("No command with that name exists.");

        }
    }
}
