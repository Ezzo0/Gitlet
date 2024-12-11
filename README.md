Gitlet is a version control system inspired from [git](https://git-scm.com/) that was implemented in java.
## Internal Structure
Real Git distinguishes several different kinds of objects. For our purposes, the important ones are

- **blobs**: The saved contents of files. Since Gitlet saves many versions of files, a single file might correspond to multiple blobs: each being tracked in a different commit.
- **trees**: Directory structures mapping names to references to blobs and other trees (subdirectories).
- **commits**: Combinations of log messages, other metadata (commit date, author, etc.), a reference to a tree, and references to parent commits. The repository also maintains a mapping from branch heads to references to commits, so that certain important commits have symbolic names. 

Gitlet simplifies from Git still further by

- Incorporating trees into commits and not dealing with subdirectories (so there will be one “flat” directory of plain files for each repository).
- Limiting ourselves to merges that reference two parents (in real Git, there can be any number of parents.)
- Having our metadata consist only of a timestamp and log message. A commit, therefore, will consist of a log message, timestamp, a mapping of file names to blob references, a parent reference, and (for merges) a second parent reference.

## The Commands
1. **_init_** 
	- **Usage**: `java gitlet.Main init`.
	- **Description**: Creates a new Gitlet version-control system in the current directory. This system will automatically start with one commit: a commit that contains no files and has the commit message initial commit (just like that, with no punctuation). It will have a single branch: master, which initially points to this initial commit, and master will be the current branch. The timestamp for this initial commit will be 00:00:00 UTC, Thursday, 1 January 1970 in your timezone you choose for dates.
2. **_add_**
	- **Usage**: `java gitlet.Main add [file name]`.
	- **Description**: Adds a copy of the file as it currently exists to the staging area. For this reason, adding a file is also called staging the file for addition. Staging an already-staged file overwrites the previous entry in the staging area with the new contents. The staging area is in .gitlet directory. If the current working version of the file is identical to the version in the current commit, it will not be staged, and will be removed from the staging area if it is already there (as can happen when a file is changed, added, and then changed back to it’s original version). The file will no longer be staged for removal (see gitlet `rm`), if it was at the time of the command.
3. **_commit_**
	- **Usage**: `java gitlet.Main commit [message]`.
	- **Description**: Saves a snapshot of tracked files in the current commit and staging area so they can be restored at a later time, creating a new commit. The commit is said to be _tracking_ the saved files. By default, each commit’s snapshot of files will be exactly the same as its parent commit’s snapshot of files; it will keep versions of files exactly as they are, and not update them. A commit will only update the contents of files it is tracking that have been staged for addition at the time of commit, in which case the commit will now include the version of the file that was staged instead of the version it got from its parent. A commit will save and start tracking any files that were staged for addition but weren’t tracked by its parent. Finally, files tracked in the current commit may be untracked in the new commit as a result being _staged for removal_ by the `rm` command (below).
4. **_rm_**
	- **Usage**: `java gitlet.Main rm [file name]`.
	- **Description**: Unstage the file if it is currently staged for addition. If the file is tracked in the current commit, stage it for removal and remove the file from the working directory if the user has not already done so.
5. **_log_**
	- **Usage**: `java gitlet.Main log`.
	- **Description**: Starting at the current head commit, display information about each commit backwards along the commit tree until the initial commit, following the first parent commit links, ignoring any second parents found in merge commits. This set of commit nodes is called the commit’s _history_. For every node in this history, the information displayed is the commit id, the time the commit was made, and the commit message. For merge commits (those that have two parent commits), the line like: `Merge: [First parent ID] [Second parent ID]` is added.
6. **_global-log_**
	- **Usage**: `java gitlet.Main global-log`.
	- **Description**: Like `log`, except displays information about all commits ever made. The commits are not in order.
7. **_find_**
	- **Usage**: `java gitlet.Main find [commit message]`.
	- **Description**: Prints out the ids of all commits that have the given commit message, one per line. If there are multiple such commits, it prints the ids out on separate lines. The commit message is a single operand; to indicate a multiword message, put the operand in quotation marks, as for the `commit` command.
8. **_status_**
	- **Usage**: `java gitlet.Main status`.
	- **Description**: Displays what branches currently exist, and marks the current branch with a `*`. Also displays what files have been staged for addition or removal. An example of the _exact_ format it should follow is as follows:
```
=== Branches ===
*master
other-branch
  
=== Staged Files ===
wug.txt
wug2.txt
  
=== Removed Files ===
goodbye.txt
  
=== Modifications Not Staged For Commit ===
junk.txt (deleted)
wug3.txt (modified)
  
=== Untracked Files ===
random.stuff
```

9. **_checkout_**
	- **Usages**:
		1. `java gitlet.Main checkout -- [file name]`.
        2. `java gitlet.Main checkout [commit id] -- [file name]`.
        3. `java gitlet.Main checkout [branch name]`.
    - **Descriptions**:
	    1. Takes the version of the file as it exists in the head commit and puts it in the working directory, overwriting the version of the file that’s already there if there is one. The new version of the file is not staged.
	    2. Takes the version of the file as it exists in the commit with the given id, and puts it in the working directory, overwriting the version of the file that’s already there if there is one. The new version of the file is not staged.
	    3. Takes all files in the commit at the head of the given branch, and puts them in the working directory, overwriting the versions of the files that are already there if they exist. Also, at the end of this command, the given branch will now be considered the current branch. Any files that are tracked in the current branch but are not present in the checked-out branch are deleted. The staging area is cleared, unless the checked-out branch is the current branch.
10. **_branch_**
	- **Usage**: `java gitlet.Main branch [branch name]`. 
	- **Description**: Creates a new branch with the given name, and points it at the current head commit. This command does NOT immediately switch to the newly created branch. Before you ever call branch, your code should be running with a default branch called `master`.
11. **_rm-branch_**
	- **Usage**: `java gitlet.Main rm-branch [branch name]`.
	- **Description**: Deletes the branch with the given name. This only means to delete the pointer associated with the branch; it does not mean to delete all commits that were created under the branch, or anything like that.
12. **_reset_**
	- **Usage**: `java gitlet.Main reset [commit id]`.
	- **Description**: Checks out all the files tracked by the given commit. Removes tracked files that are not present in that commit. Also moves the current branch’s head to that commit node. The staging area is cleared.
13. **_merge_**
	- **Usage**: `java gitlet.Main merge [branch name]`.
	- **Description**: Merges files from the given branch into the current branch.