package gitlet;

import java.io.File;
import static gitlet.Utils.*;
import java.io.IOException;
import java.util.*;

/** Represents a gitlet repository.
 *  does at a high level.
 *
 *  @author Noah
 */
public class Repository {
    /**
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */
    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = Utils.join(CWD, ".gitlet");

    /**Allows for persistence*/
    public static boolean gitlet(String command) {
        if (command.equals("init")) {
            return true;
        } else {
            return checkIfGitletExists();
        }
    }

    public static boolean checkIfGitletExists() {
        File allFiles = Utils.join(GITLET_DIR);
        List<String> s = plainFilenamesIn(allFiles);
        return s != null;
    }

    public static void setUpPersistence() {
        File gitlet = new File(".gitlet");
        gitlet.mkdir();
        File stages = Utils.join(gitlet, "stages");
        stages.mkdir();
        File removal = Utils.join(gitlet, "stageRemoval");
        removal.mkdir();
        File commits = Utils.join(gitlet, "commits");
        commits.mkdir();
        File head = Utils.join(gitlet, "head");
        try {
            head.createNewFile();
        } catch (IOException error) {
            System.out.println("Error");
        }

        LinkedList<Commit> listCommits = new LinkedList<>();
        File allCommits = Utils.join(gitlet, "allCommits");
        try {
            allCommits.createNewFile();
        } catch (IOException error) {
            System.out.println("Error");
        }
        Utils.writeObject(allCommits, listCommits);
        File branches = Utils.join(GITLET_DIR, "branches");
        branches.mkdir();
        File nameOfHeadBranch = Utils.join(gitlet, "currentBranch");
        try {
            nameOfHeadBranch.createNewFile();
        } catch (IOException error) {
            System.out.println("Error");
        }
        Utils.writeObject(nameOfHeadBranch, "master");
        File allFileNamesEver = Utils.join(GITLET_DIR, "allFileNamesEver");
        try {
            allFileNamesEver.createNewFile();
        } catch (IOException error) {
            System.out.println("Error");
        }
        Utils.writeObject(allFileNamesEver, new LinkedList<String>());
    }

    public static void makeInit() {
        /*create and add commit to commit directory*/
        List<String> filesInCWD = Utils.plainFilenamesIn(CWD);
        ArrayList<String> cwdContents = new ArrayList<>();
        for (int i = 0; i < filesInCWD.size(); i += 1) {
            cwdContents.add(readContentsAsString(Utils.join(CWD, filesInCWD.get(i))));
        }
        Commit c = new Commit("initial commit", "*master",
                new ArrayList<>(), new ArrayList<>(), filesInCWD, cwdContents);
        c.setDate(0);
        c.setTime(c.getDate());
        c.setHash(sha1(Utils.serialize(c)));
        File commits = Utils.join(GITLET_DIR, "commits");
        File n = Utils.join(commits, c.getHash());
        Utils.writeObject(n, c);
        /*adds this commit to the linked list that is in its own folder*/
        File coms = Utils.join(GITLET_DIR, "allCommits");
        LinkedList<Commit> x = Utils.readObject(coms, LinkedList.class);
        x.addFirst(c);
        Utils.restrictedDelete("allCommits");
        File allCommits = Utils.join(GITLET_DIR, "allCommits");
        try {
            allCommits.createNewFile();
        } catch (IOException error) {
            System.out.println("Error");
        }
        Utils.writeObject(allCommits, x);
        /*changes head pointer*/
        File head = Utils.join(GITLET_DIR, "head");
        Utils.writeObject(head, x);
        /*Adds new linked list to current branch*/
        File branches = Utils.join(GITLET_DIR, "branches");
        File master = Utils.join(branches, "master");
        Utils.writeObject(master, x);
    }

    public static void stage(String s) {
        LinkedList<String> allFileNamesEver = readObject
                (Utils.join(GITLET_DIR, "allFileNamesEver"), LinkedList.class);
        List<String> allFilesCWD = plainFilenamesIn(Utils.join(CWD));
        if (!allFilesCWD.contains(s)) {
            System.out.println("File does not exist.");
            List<String> removals = plainFilenamesIn(Utils.join
                    (GITLET_DIR, "stageRemoval"));
            if (removals.contains(s)) {
                File removed = Utils.join(GITLET_DIR, "stageRemoval", s);
                String contents = readContentsAsString(removed);
                removed.delete();
                File newFile = Utils.join(CWD, s);
                try {
                    newFile.createNewFile();
                } catch (IOException error) {
                    System.out.println("Error");
                }
                Utils.writeContents(newFile, contents);
                stage(s);
            }
            System.exit(0);
        }
        File heads = Utils.join(GITLET_DIR, "branches", readObject
                (Utils.join(GITLET_DIR, "currentBranch"), String.class));
        LinkedList<Commit> headCommits = Utils.readObject(heads, LinkedList.class);
        ArrayList<File> currentCommitFiles = headCommits.getFirst().getFiLi();
        File cwdFiles = Utils.join(CWD, s);
        String x = readContentsAsString(cwdFiles);
        File stages = Utils.join(GITLET_DIR, "stages");
        File n = Utils.join(stages, s);
        Utils.writeContents(n, x);
        List<String> stagedFiles = Utils.plainFilenamesIn(stages);
        List<String> removalStageFiles = plainFilenamesIn
                (Utils.join(GITLET_DIR, "stageRemoval"));
        for (int i = 0; i < currentCommitFiles.size(); i += 1) {
            if (stagedFiles.contains(currentCommitFiles.get(i).getName())) {
                String ccfContents = headCommits.getFirst().getBlob().get(i);
                File fileInStage = Utils.join(stages, currentCommitFiles.get(i).getName());
                if (ccfContents.equals(readContentsAsString(fileInStage))) {
                    fileInStage.delete();
                }
                if (removalStageFiles.contains(currentCommitFiles.get(i).getName())) {
                    fileInStage = Utils.join(GITLET_DIR,
                            "stageRemoval", currentCommitFiles.get(i).getName());
                    if (ccfContents.equals(readContentsAsString(fileInStage))) {
                        fileInStage.delete();
                    }
                }
            }
        }
        if (!allFileNamesEver.contains(s)) {
            allFileNamesEver.add(s);
        }
        writeObject(Utils.join(GITLET_DIR, "allFileNamesEver"),
                allFileNamesEver);
    }

    public static void stageRemoval(String s) {
        List<String> allCWDFiles = plainFilenamesIn(Utils.join(CWD));
        if (!allCWDFiles.contains(s)) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        File cwdFiles = Utils.join(CWD, s);
        String x = readContentsAsString(cwdFiles);
        File stages = Utils.join(GITLET_DIR, "stageRemoval");
        File n = Utils.join(stages, s);
        Utils.writeContents(n, x);
    }

    public static void stageCommit(String s) {
        LinkedList<String> allFileNamesEver = readObject
                (Utils.join(GITLET_DIR, "allFileNamesEver"), LinkedList.class);
        List<String> allFilesCWD = plainFilenamesIn(Utils.join(CWD));
        if (!allFilesCWD.contains(s)) {
            List<String> removals = plainFilenamesIn(Utils.join
                    (GITLET_DIR, "stageRemoval"));
            if (removals.contains(s)) {
                File removed = Utils.join(GITLET_DIR, "stageRemoval", s);
                String contents = readContentsAsString(removed);
                removed.delete();
                File newFile = Utils.join(CWD, s);
                try {
                    newFile.createNewFile();
                } catch (IOException error) {
                    System.out.println("Error");
                }
                Utils.writeContents(newFile, contents);
                stageCommit(s);
            }
            return;
        }
        File heads = Utils.join(GITLET_DIR, "branches", readObject
                (Utils.join(GITLET_DIR, "currentBranch"), String.class));
        LinkedList<Commit> headCommits = Utils.readObject(heads, LinkedList.class);
        ArrayList<File> currentCommitFiles = headCommits.getFirst().getFiLi();
        File cwdFiles = Utils.join(CWD, s);
        String x = readContentsAsString(cwdFiles);
        File stages = Utils.join(GITLET_DIR, "stages");
        File n = Utils.join(stages, s);
        Utils.writeContents(n, x);
        List<String> stagedFiles = Utils.plainFilenamesIn(stages);
        List<String> removalStageFiles = plainFilenamesIn
                (Utils.join(GITLET_DIR, "stageRemoval"));
        for (int i = 0; i < currentCommitFiles.size(); i += 1) {
            if (stagedFiles.contains(currentCommitFiles.get(i).getName())) {
                String ccfContents = headCommits.getFirst().getBlob().get(i);
                File fileInStage = Utils.join(stages, currentCommitFiles.get(i).getName());
                if (ccfContents.equals(readContentsAsString(fileInStage))) {
                    fileInStage.delete();
                }
                if (removalStageFiles.contains(currentCommitFiles.get(i).getName())) {
                    fileInStage = Utils.join(GITLET_DIR,
                            "stageRemoval", currentCommitFiles.get(i).getName());
                    if (ccfContents.equals(readContentsAsString(fileInStage))) {
                        fileInStage.delete();
                    }
                }
            }
        }
        if (!allFileNamesEver.contains(s)) {
            allFileNamesEver.add(s);
        }
        writeObject(Utils.join(GITLET_DIR, "allFileNamesEver"),
                allFileNamesEver);
    }

    public static void stageRemovalCommit(String s) {
        List<String> allCWDFiles = plainFilenamesIn(Utils.join(CWD));
        if (!allCWDFiles.contains(s)) {
            return;
        }
        File cwdFiles = Utils.join(CWD, s);
        String x = readContentsAsString(cwdFiles);
        File stages = Utils.join(GITLET_DIR, "stageRemoval");
        File n = Utils.join(stages, s);
        Utils.writeContents(n, x);
    }

    public static void commit(String message) {
        File commits = Utils.join(GITLET_DIR, "commits");
        File staged = Utils.join(GITLET_DIR, "stages");
        File stageRemoval = Utils.join(GITLET_DIR, "stageRemoval");
        File head = Utils.join(GITLET_DIR, "head");
        LinkedList<Commit> h = Utils.readObject(head, LinkedList.class);
        Commit headCommit = h.getFirst();
        ArrayList<File> filesToCommit = new ArrayList<>();
        ArrayList<String> blobsToCommit = new ArrayList<>();
        List<String> l = plainFilenamesIn(staged);
        List<String> s = plainFilenamesIn(stageRemoval);
        if (l.size() == 0 && s.size() == 0) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        } else if (headCommit.getFiLi().size() > 0) {
            for (int i = 0; i < headCommit.getFiLi().size(); i += 1) {
                if (s.contains(headCommit.getFiLi().get(i).getName())) {
                    File stagedF = Utils.join(stageRemoval, headCommit.getFiLi().get(i).getName());
                    stagedF.delete();
                } else if (l.contains(headCommit.getFiLi().get(i).getName())) {
                    File stagedFile = Utils.join(staged, headCommit.getFiLi().get(i).getName());
                    filesToCommit.add(stagedFile);
                    blobsToCommit.add(readContentsAsString(stagedFile)); stagedFile.delete();
                } else {
                    filesToCommit.add(headCommit.getFiLi().get(i));
                    blobsToCommit.add(headCommit.getBlob().get(i));
                }
            }
        }
        l = plainFilenamesIn(Utils.join(GITLET_DIR, "stages"));
        if (l.size() > 0) {
            for (int i = 0; i < l.size(); i += 1) {
                File stagedFile = Utils.join(staged, l.get(i)); filesToCommit.add(stagedFile);
                blobsToCommit.add(readContentsAsString(stagedFile)); stagedFile.delete();
            }
        }
        stageRemoval.delete();
        File newRemove = Utils.join(GITLET_DIR, "stageRemoval");
        newRemove.mkdir();
        List<String> filesInCWD = Utils.plainFilenamesIn(CWD);
        ArrayList<String> cwdContents = new ArrayList<>();
        for (int i = 0; i < filesInCWD.size(); i += 1) {
            cwdContents.add(readContentsAsString(Utils.join(CWD, filesInCWD.get(i))));
        }
        Commit c = new Commit(message, "*master", filesToCommit,
                blobsToCommit, filesInCWD, cwdContents);
        c.setHash(sha1(Utils.serialize(c)));
        File m = Utils.join(commits, c.getHash());
        Utils.writeObject(m, c);
        File coms = Utils.join(GITLET_DIR, "allCommits");
        LinkedList<Commit> x = Utils.readObject(coms, LinkedList.class);
        x.addFirst(c);
        Utils.restrictedDelete("allCommits");
        File allCommits = Utils.join(GITLET_DIR, "allCommits");
        try {
            allCommits.createNewFile();
        } catch (IOException error) {
            System.out.println("Error");
        }
        Utils.writeObject(allCommits, x); h.addFirst(c); Utils.writeObject(head, h);
        File currentBranchName = Utils.join(GITLET_DIR, "currentBranch");
        File branches = Utils.join(GITLET_DIR, "branches");
        File branch = Utils.join(branches, readObject(currentBranchName, String.class));
        LinkedList<Commit> currcoms = readObject(branch, LinkedList.class);
        currcoms.addFirst(c);
        Utils.writeObject(branch, currcoms);
    }

    public static void checkoutFile(String s) {
        File currentVersion;
        File head = Utils.join(GITLET_DIR, "head");
        LinkedList<Commit> co = Utils.readObject(head, LinkedList.class);
        Commit c = co.getFirst();
        ArrayList<File> contentFiles = c.getFiLi();
        for (int i = 0; i < contentFiles.size(); i += 1) {
            if (contentFiles.get(i).getName().compareTo(s) == 0) {
                File allFilesInCWD = Utils.join(CWD);
                List<String> l = plainFilenamesIn(allFilesInCWD);
                if (!l.contains(s)) {
                    currentVersion = Utils.join(CWD, s);
                    try {
                        currentVersion.createNewFile();
                    } catch (IOException error) {
                        System.out.println("Error");
                    }
                }
                currentVersion = Utils.join(CWD, s);
                Utils.writeContents(currentVersion, c.getBlob().get(i));
                return;
            }
        }
        System.out.println("File does not exist in that commit.");
        System.exit(0);
    }

    public static void checkoutCommit(String s, String f) {
        File currentVersion;
        File commits = Utils.join(GITLET_DIR, "commits");
        List<String> ids = plainFilenamesIn(commits);
        for (int i = 0; i < ids.size(); i += 1) {
            if (s.length() < 7) {
                System.out.println("No commit with that id exists.");
                System.exit(0);
            }
            String shortID = readObject(join(commits,
                    ids.get(i)), Commit.class).getHash().substring(0, 6);
            if (ids.get(i).equals(s) || s.substring(0, 6).equals(shortID)) {
                s = readObject(join(commits, ids.get(i)), Commit.class).getHash();
                break;
            } else if (i == ids.size() - 1) {
                System.out.println("No commit with that id exists.");
                System.exit(0);
            }
        }
        File commitToCheck = Utils.join(commits, s);
        Commit c = Utils.readObject(commitToCheck, Commit.class);
        ArrayList<File> contentFiles = c.getFiLi();
        for (int i = 0; i < contentFiles.size(); i += 1) {
            if (contentFiles.get(i).getName().compareTo(f) == 0) {
                currentVersion = Utils.join(CWD, f);
                Utils.writeContents(currentVersion, c.getBlob().get(i));
                return;
            }
            if (!plainFilenamesIn(Utils.join(CWD)).contains(contentFiles.get(i).getName())) {
                File retrieve = Utils.join(CWD, f);
                try {
                    retrieve.createNewFile();
                } catch (IOException error) {
                    System.out.println("Error");
                }
                writeContents(retrieve, c.getBlob().get(i));
            }
        }
        System.out.println("File does not exist in that commit.");
    }

    public static void checkoutBranch(String name) {
        checkoutBranchCond(name);
        File branches = Utils.join(GITLET_DIR, "branches");
        List<String> cwd = plainFilenamesIn(CWD);
        File branch;
        File currentBranchName = Utils.join(GITLET_DIR, "currentBranch");
        File head = Utils.join(GITLET_DIR, "head");
        List<String> currentWD = plainFilenamesIn(CWD);
        /*If it is, move everything(overwrite) in the specified branch
        to CWD and change current branch name and head*/
        branch = Utils.join(branches, name);
        LinkedList<Commit> commitsInBranch = Utils.readObject(branch, LinkedList.class);
        for (int i = 0; i < commitsInBranch.get(0).getFiLi().size(); i += 1) {
            if (cwd.contains(commitsInBranch.get(0).getFiLi().get(i).getName())) {
                Utils.writeContents(Utils.join(CWD, commitsInBranch.get(0).
                        getFiLi().get(i).getName()), commitsInBranch.get(0).getBlob().get(i));
            } else {
                String oldFileNotInCWDName = commitsInBranch.get(0).getFiLi().get(i).getName();
                File oldFileNotInCWD = Utils.join(CWD, oldFileNotInCWDName);
                try {
                    oldFileNotInCWD.createNewFile();
                } catch (IOException error) {
                    System.out.println("Error");
                }
                writeContents(oldFileNotInCWD, commitsInBranch.get(0).getBlob().get(i));
            }
        }
        List<String> newWD = commitsInBranch.get(0).getCWD();
        for (int i = 0; i < currentWD.size(); i += 1) {
            if (!newWD.contains(currentWD.get(i))) {
                Utils.join(CWD, currentWD.get(i)).delete();
            }
        }
        Utils.join(GITLET_DIR, "stages").delete();
        Utils.join(GITLET_DIR, "stages").mkdir();
        Utils.join(GITLET_DIR, "stageRemoval").delete();
        Utils.join(GITLET_DIR, "stageRemoval").mkdir();
        Utils.writeObject(currentBranchName, name);
        Utils.writeObject(head, commitsInBranch);
    }

    public static void checkoutBranchCond(String name) {
        File branches = Utils.join(GITLET_DIR, "branches");
        String currentBranch = readObject(Utils.join(GITLET_DIR, "current"
                + "Branch"), String.class);
        List<String> l = plainFilenamesIn(branches);
        if (!l.contains(name)) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        File currentBranchName = Utils.join(GITLET_DIR, "currentBranch");
        String cbn = readObject(currentBranchName, String.class);
        if (name.equals(cbn)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        File heads = Utils.join(GITLET_DIR, "head");
        LinkedList<Commit> headCommit = readObject(heads, LinkedList.class);
        List<String> currentWD = plainFilenamesIn(CWD);
        if (currentWD.size() != headCommit.get(0).getCWD().size()) {
            System.out.println("There is an untracked file in the way; delete it, "
                    + "or add and commit it first.");
            System.exit(0);
        }
        for (int i = 0; i < currentWD.size(); i += 1) {
            if (!headCommit.get(0).getCWD().contains(currentWD.get(i))) {
                System.out.println("There is an untracked file in the way; delete it"
                        + ", or add and commit it first.");
                System.exit(0);
            }
        }
        LinkedList<String> headContents = new LinkedList<>();
        for (int i = 0; i < headCommit.get(0).getCWD().size(); i += 1) {
            headContents.add(headCommit.get(0).cwdCons().get(i));
        }
        LinkedList<String> allFileNamesEver = readObject(Utils.join
                (GITLET_DIR, "allFileNamesEver"), LinkedList.class);
        for (int i = 0; i < plainFilenamesIn(CWD).size(); i += 1) {
            if (!allFileNamesEver.contains(plainFilenamesIn(CWD).get(i))) {
                continue;
            }
            String currentWDContents = readContentsAsString(Utils.join
                    (CWD, plainFilenamesIn(CWD).get(i)));
            if (!headContents.contains(currentWDContents)) {
                System.out.println("There is an untracked file "
                        + "in the way; delete it, or add and commit it first.");
                System.exit(0);
            }
        }
        List<String> stage = plainFilenamesIn(Utils.join(GITLET_DIR, "stages"));
        if (stage.size() > 0) {
            System.out.println("There is an untracked file "
                    + "in the way; delete it, or add and commit it first.");
            System.exit(0);
        }
    }

    public static void log() {
        /*adds this commit to the linked list that is in its own folder*/
        File coms = Utils.join(GITLET_DIR, "head");
        LinkedList<Commit> listCommits = Utils.readObject(coms, LinkedList.class);
        /* Loops around the list of commits and print them out*/
        for (int i = 0; i < listCommits.size(); i += 1) {
            Commit c = listCommits.get(i);
            System.out.println("===");
            System.out.println("commit " + c.getHash());
            System.out.println("Date: " + c.getTime());
            System.out.println(c.getM() + "\n");
        }
    }

    public static void globalLog() {
        File coms = Utils.join(GITLET_DIR, "allCommits");
        LinkedList<Commit> listCommits = Utils.readObject(coms, LinkedList.class);
        /* Loops around the list of commits and print them out*/
        for (int i = 0; i < listCommits.size(); i += 1) {
            Commit c = listCommits.get(i);
            System.out.println("===");
            System.out.println("commit " + c.getHash());
            System.out.println("Date: " + c.getTime());
            System.out.println(c.getM() + "\n");
        }
    }

    public static void rm(String fileName) {
        boolean inHead = false;
        boolean delete = false;
        List<String> removalFiles = plainFilenamesIn(Utils.join(
                GITLET_DIR, "stageRemoval"));
        List<String> cwdFiles = plainFilenamesIn(CWD);
        File currentCommit = Utils.join(GITLET_DIR, "branches", readObject
                (Utils.join(GITLET_DIR, "currentBranch"), String.class));
        LinkedList<Commit> co = Utils.readObject(currentCommit, LinkedList.class);
        Commit c = co.getFirst();
        ArrayList<File> currentFiles = c.getFiLi();
        for (int i = 0; i < currentFiles.size(); i += 1) {
            if (currentFiles.get(i).getName().equals(fileName)) {
                inHead = true;
                if (!removalFiles.contains(currentFiles.get(i).getName())
                        && !cwdFiles.contains(currentFiles.get(i).getName())) {
                    File toAdd = Utils.join(Utils.join(GITLET_DIR,
                            "stageRemoval"), currentFiles.get(i).getName());
                    Utils.writeObject(toAdd, currentFiles.get(i));
                    return;
                }
            }
        }
        File staged = Utils.join(GITLET_DIR, "stages");
        List<String> l = plainFilenamesIn(staged);
        if (l.contains(fileName)) {
            File file = Utils.join(staged, fileName);
            delete = file.delete();
        }
        if (!delete && !inHead) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
        if (inHead) {
            stageRemoval(fileName);
            Utils.restrictedDelete(fileName);
        }
    }

    public static void find(String commitMsg) {
        int count = 0;
        File allCommitsFile = Utils.join(GITLET_DIR, "allCommits");
        LinkedList<Commit> allCommits = readObject(allCommitsFile, LinkedList.class);
        for (int i = 0; i < allCommits.size(); i += 1) {
            if (allCommits.get(i).getM().equals(commitMsg)) {
                System.out.println(allCommits.get(i).getHash());
                count += 1;
            }
        }
        if (count == 0) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
    }

    public static void status() {
        System.out.println("=== Branches ===");
        File branchesFile = Utils.join(GITLET_DIR, "branches");
        List<String> branches = plainFilenamesIn(branchesFile);
        File currentB = Utils.join(GITLET_DIR, "currentBranch");
        String currentBName = Utils.readObject(currentB, String.class);
        for (int i = 0; i < branches.size(); i += 1) {
            if (branches.get(i).equals(currentBName)) {
                System.out.println("*" + branches.get(i));
                continue;
            }
            System.out.println(branches.get(i));
        }
        System.out.print("\n");
        System.out.println("=== Staged Files ===");
        File stages = Utils.join(GITLET_DIR, "stages");
        List<String> staged = plainFilenamesIn(stages);
        for (int i = 0; i < staged.size(); i += 1) {
            System.out.println(staged.get(i));
        }
        System.out.print("\n");
        System.out.println("=== Removed Files ===");
        File stagesRemoval = Utils.join(GITLET_DIR, "stageRemoval");
        List<String> stagedRemoval = plainFilenamesIn(stagesRemoval);
        for (int i = 0; i < stagedRemoval.size(); i += 1) {
            System.out.println(stagedRemoval.get(i));
        }
        System.out.print("\n");
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.print("\n");
        System.out.println("=== Untracked Files ===");
        System.out.print("\n");
    }

    public static void branch(String name) {
        File branches = Utils.join(GITLET_DIR, "branches");
        List<String> l = plainFilenamesIn(branches);
        if (l.contains(name)) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        File newBranch = Utils.join(branches, name);
        /*Get current branch and the linked list of commits*/
        File currentBranchFile = Utils.join(GITLET_DIR, "currentBranch");
        String currentBranchName = Utils.readObject(currentBranchFile, String.class);
        File currentBranch = Utils.join(branches, currentBranchName);
        LinkedList<Commit> commits = Utils.readObject(currentBranch, LinkedList.class);
        Utils.writeObject(newBranch, commits);
    }

    public static void rmBranch(String name) {
        File branches = Utils.join(GITLET_DIR, "branches");
        List<String> l = plainFilenamesIn(branches);
        if (!l.contains(name)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        File branchToDelete = Utils.join(branches, name);
        File currentBranchFile = Utils.join(GITLET_DIR, "currentBranch");
        String currentBranchName = Utils.readObject(currentBranchFile, String.class);
        if (name.equals(currentBranchName)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        branchToDelete.delete();
    }

    public static void reset(String commitID) {
        File commits = Utils.join(GITLET_DIR, "commits");
        List<String> ids = plainFilenamesIn(commits);
        for (int i = 0; i < ids.size(); i += 1) {
            if (commitID.length() < 7) {
                System.out.println("No commit with that id exists.");
                System.exit(0);
            }
            String shortID = readObject(join(commits,
                    ids.get(i)), Commit.class).getHash().substring(0, 6);
            if (ids.get(i).equals(commitID) || commitID.substring(0, 6).equals(shortID)) {
                commitID = readObject(join(commits, ids.get(i)),
                        Commit.class).getHash();
                break;
            } else if (i == ids.size() - 1) {
                System.out.println("No commit with that id exists.");
                System.exit(0);
            }
        }
        Commit commit = readObject(Utils.join(GITLET_DIR,
                "commits", commitID), Commit.class);
        /*Check if there are any untracked files to be commited*/
        File heads = Utils.join(GITLET_DIR, "head");
        LinkedList<Commit> headCommit = readObject(heads, LinkedList.class);
        List<String> currentWD = plainFilenamesIn(CWD);
        if (currentWD.size() != headCommit.get(0).getCWD().size()) {
            System.out.println("There is an untracked file in the way; delete it, "
                    + "or add and commit it first.");
            System.exit(0);
        }
        for (int i = 0; i < currentWD.size(); i += 1) {
            if (!headCommit.get(0).getCWD().contains(currentWD.get(i))) {
                System.out.println("There is an untracked file in the way; delete it"
                        + ", or add and commit it first.");
                System.exit(0);
            }
        }
        LinkedList<String> headContents = new LinkedList<>();
        for (int i = 0; i < headCommit.get(0).getCWD().size(); i += 1) {
            headContents.add(headCommit.get(0).cwdCons().get(i));
        }
        LinkedList<String> allFileNamesEver = readObject(Utils.join
                (GITLET_DIR, "allFileNamesEver"), LinkedList.class);
        for (int i = 0; i < plainFilenamesIn(CWD).size(); i += 1) {
            if (!allFileNamesEver.contains(plainFilenamesIn(CWD).get(i))) {
                continue;
            }
            String currentWDContents = readContentsAsString(Utils.join
                    (CWD, plainFilenamesIn(CWD).get(i)));
            if (!headContents.contains(currentWDContents)) {
                System.out.println("There is an untracked file "
                        + "in the way; delete it, or add and commit it first.");
                System.exit(0);
            }
        }
        List<String> stage = plainFilenamesIn(Utils.join(GITLET_DIR, "stages"));
        if (stage.size() > 0) {
            System.out.println("There is an untracked file "
                    + "in the way; delete it, or add and commit it first.");
            System.exit(0);
        }
        for (int i = 0; i < currentWD.size(); i += 1) {
            if (commit.getCWD().contains(currentWD.get(i))) {
                checkoutCommit(commitID, currentWD.get(i));
            } else {
                Utils.join(CWD, currentWD.get(i)).delete();
            }
        }
        LinkedList<Commit> newHead = new LinkedList<>();
        for (int i = 0; i < headCommit.size(); i += 1) {
            if (headCommit.get(i).getHash().equals(commitID)) {
                newHead.addFirst(headCommit.get(i));
                break;
            }
            newHead.addFirst(headCommit.get(i));
        }
        Utils.writeObject(heads, newHead);
    }

    public static void checkConditions(String name) {
        File branches = Utils.join(GITLET_DIR, "branches");
        String currentBranch = readObject(Utils.join(GITLET_DIR, "current"
                + "Branch"), String.class);
        List<String> l = plainFilenamesIn(branches);
        if (!l.contains(name)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (name.equals(currentBranch)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
        List<String> stagedFiles = plainFilenamesIn(Utils.join(GITLET_DIR, "stages"));
        List<String> stageRemovalFiles = plainFilenamesIn(Utils.join(
                GITLET_DIR, "stageRemoval"));
        if (stagedFiles.size() > 0 || stageRemovalFiles.size() > 0) {
            System.out.println("You have uncommitted changes."); System.exit(0);
        }
        File heads = Utils.join(GITLET_DIR, "head");
        LinkedList<Commit> headCommit = readObject(heads, LinkedList.class);
        List<String> currentWD = plainFilenamesIn(CWD);
        if (currentWD.size() != headCommit.get(0).getCWD().size()) {
            System.out.println("There is an untracked file in the way; delete it, "
                    + "or add and commit it first.");
            System.exit(0);
        }
        for (int i = 0; i < currentWD.size(); i += 1) {
            if (!headCommit.get(0).getCWD().contains(currentWD.get(i))) {
                System.out.println("There is an untracked file in the way; delete it"
                        + ", or add and commit it first.");
                System.exit(0);
            }
        }
        LinkedList<String> headContents = new LinkedList<>();
        for (int i = 0; i < headCommit.get(0).getCWD().size(); i += 1) {
            headContents.add(headCommit.get(0).cwdCons().get(i));
        }
        LinkedList<String> allFileNamesEver = readObject(Utils.join
                (GITLET_DIR, "allFileNamesEver"), LinkedList.class);
        for (int i = 0; i < plainFilenamesIn(CWD).size(); i += 1) {
            if (!allFileNamesEver.contains(plainFilenamesIn(CWD).get(i))) {
                continue;
            }
            String currentWDContents = readContentsAsString(Utils.join
                    (CWD, plainFilenamesIn(CWD).get(i)));
            if (!headContents.contains(currentWDContents)) {
                System.out.println("There is an untracked file "
                        + "in the way; delete it, or add and commit it first.");
                System.exit(0);
            }
        }
        List<String> stage = plainFilenamesIn(Utils.join(GITLET_DIR, "stages"));
        if (stage.size() > 0) {
            System.out.println("There is an untracked file "
                    + "in the way; delete it, or add and commit it first.");
            System.exit(0);
        }
    }

    public static Commit checkMerge(String name) {
        String cb = readObject(join(GITLET_DIR, "currentBranch"), String.class);
        LinkedList<Commit> headCommits = readObject
                (Utils.join(GITLET_DIR, "branches", cb), LinkedList.class);
        LinkedList<Commit> targetBranchCommits = readObject
                (Utils.join(GITLET_DIR, "branches", name), LinkedList.class);
        Commit splitPoint = null;
        for (int i = 0; i < Math.max(headCommits.size(), targetBranchCommits.size()); i += 1) {
            if (headCommits.size() - i - 1 < 0 || targetBranchCommits.size() - i - 1 < 0) {
                break;
            }
            if (!headCommits.get(headCommits.size() - i - 1).getHash().equals
                    (targetBranchCommits.get(targetBranchCommits.size() - i - 1).getHash())) {
                break;
            }
            splitPoint = headCommits.get(headCommits.size() - i - 1);
        }
        Commit head = headCommits.getFirst(); Commit targetBranch = targetBranchCommits.getFirst();
        Set<String> allFileName = new HashSet<>(); ArrayList<String> headFiles = head.fNames();
        ArrayList<String> targetBranchFile = targetBranch.fNames();
        ArrayList<String> splitPointFile = splitPoint.fNames();
        allFileName.addAll(headFiles); allFileName.addAll(targetBranchFile);
        allFileName.addAll(splitPointFile); Commit end = null;
        if (targetBranchCommits.size() >= headCommits.size()) {
            end = targetBranch;
            for (int i = 0; i < targetBranchCommits.size(); i += 1) {
                if (head.getHash().equals(targetBranchCommits.get(i).getHash())) {
                    checkoutBranch(name);
                    System.out.println("Current branch fast-forwarded.");
                    System.exit(0);
                }
            }
        } else {
            end = head;
            for (int i = 0; i < headCommits.size(); i += 1) {
                if (targetBranch.getHash().equals(headCommits.get(i).getHash())) {
                    System.out.println("Given branch is an ancestor of the current branch.");
                    System.exit(0);
                }
            }
            if (splitPoint.getHash().equals(head.getHash())) {
                checkoutBranch(readObject(Utils.join
                        (GITLET_DIR, "currentBranch"), String.class));
                System.out.println("Current branch fast-forwarded.");
                System.exit(0);
            }
        }
        return splitPoint;
    }

    public static void merge(String n) {
        boolean c = false; checkConditions(n); Commit splitPoint = checkMerge(n);
        LinkedList<Commit> headCom = readObject(join(GITLET_DIR, "head"), LinkedList.class);
        LinkedList<Commit> tbc = readObject(join(GITLET_DIR, "branches", n), LinkedList.class);
        Commit head = headCom.getFirst(); Commit targetBranch = tbc.getFirst();
        Set<String> allfn = new HashSet<>(); ArrayList<String> hf = head.fNames();
        ArrayList<String> tbf = targetBranch.fNames(); ArrayList<String> spf = splitPoint.fNames();
        allfn.addAll(hf); allfn.addAll(tbf); allfn.addAll(spf); Object[] afn = allfn.toArray();
        for (int i = 0; i < afn.length; i += 1) {
            tbc = readObject(join(GITLET_DIR, "branches", n), LinkedList.class);
            targetBranch = tbc.getFirst(); tbf = targetBranch.fNames();
            String hcon = ""; String tcon = ""; String scon = ""; int hi = 0;
            if (hf.contains(afn[i]) && tbf.contains(afn[i]) && spf.contains(afn[i])) {
                for (int h = 0; h < hf.size(); h += 1) {
                    if (head.getFiLi().get(h).getName().equals(afn[i])) {
                        hcon = head.getBlob().get(h); hi = h; break;
                    }
                }
                for (int h = 0; h < tbf.size(); h += 1) {
                    if (targetBranch.getFiLi().get(h).getName().equals(afn[i])) {
                        tcon = targetBranch.getBlob().get(h); break;
                    }
                }
                for (int h = 0; h < spf.size(); h += 1) {
                    if (splitPoint.getFiLi().get(h).getName().equals(afn[i])) {
                        scon = splitPoint.getBlob().get(h); break;
                    }
                }
                if (hcon.equals(scon) && !scon.equals(tcon)) {
                    writeContents(Utils.join(CWD, head.getFiLi().get(hi).getName()), tcon);
                    stageCommit(head.getFiLi().get(hi).getName());
                } else if (tcon.equals(scon) && !scon.equals(hcon)) {
                    writeContents(Utils.join(CWD, head.getFiLi().get(hi).getName()), hcon);
                } else if (!tcon.equals(scon) && !hcon.equals(scon)) {
                    if (!tcon.equals(hcon)) {
                        mergeConflict(hcon, tcon, head.getFiLi().get(hi).getName()); c = true;
                    }
                }
            } else if (hf.contains(afn[i]) && !tbf.contains(afn[i]) && !spf.contains(afn[i])) {
                for (int h = 0; h < hf.size(); h += 1) {
                    if (head.getFiLi().get(h).getName().equals(afn[i])) {
                        hcon = head.getBlob().get(h); hi = h; break;
                    }
                }
                writeContents(Utils.join(CWD, head.getFiLi().get(hi).getName()), hcon);
            } else if (!hf.contains(afn[i]) && tbf.contains(afn[i]) && !spf.contains(afn[i])) {
                for (int h = 0; h < tbf.size(); h += 1) {
                    if (targetBranch.getFiLi().get(h).getName().equals(afn[i])) {
                        tcon = targetBranch.getBlob().get(h); hi = h;
                    }
                }
                writeContents(Utils.join(CWD, targetBranch.getFiLi().get(hi).getName()), tcon);
                stageCommit(targetBranch.getFiLi().get(hi).getName());
            } else if (hf.contains(afn[i]) && !tbf.contains(afn[i]) && spf.contains(afn[i])) {
                for (int h = 0; h < hf.size(); h += 1) {
                    if (head.getFiLi().get(h).getName().equals(afn[i])) {
                        hcon = head.getBlob().get(h); hi = h; break;
                    }
                }
                for (int h = 0; h < spf.size(); h += 1) {
                    if (splitPoint.getFiLi().get(h).getName().equals(afn[i])) {
                        scon = splitPoint.getBlob().get(h); hi = h; break;
                    }
                }
                if (hcon.equals(scon)) {
                    join(CWD, splitPoint.getFiLi().get(hi).getName()).delete();
                    stageRemovalCommit(splitPoint.getFiLi().get(hi).getName());
                } else {
                    mergeConflict(hcon, tcon, head.getFiLi().get(hi).getName()); c = true;
                }
            } else if (!hf.contains(afn[i]) && tbf.contains(afn[i]) && spf.contains(afn[i])) {
                for (int h = 0; h < tbf.size(); h += 1) {
                    if (targetBranch.getFiLi().get(h).getName().equals(afn[i])) {
                        targetBranch.removeF(h); break;
                    }
                }
            }
        }
        commitMerge(n, c);
    }

    public static void commitMerge(String n, boolean c) {
        File cs = Utils.join(GITLET_DIR, "commits"); File sa = Utils.join(GITLET_DIR, "stages");
        File stageRemoval = Utils.join(GITLET_DIR, "stageRemoval");
        String cb = readObject(join(GITLET_DIR, "currentBranch"), String.class);
        String message = "Merged " + n + " into " + cb + ".";
        ArrayList<File> ftc = new ArrayList<>(); ArrayList<String> btc = new ArrayList<>();
        File tb = join(GITLET_DIR, "branches", n); File head  = join(GITLET_DIR, "branches", cb);
        File branches = Utils.join(GITLET_DIR, "branches");
        File currentBranchName = Utils.join(GITLET_DIR, "currentBranch");
        File branch = Utils.join(branches, readObject(currentBranchName, String.class));
        LinkedList<Commit> t = Utils.readObject(tb, LinkedList.class);
        LinkedList<Commit> h = Utils.readObject(head, LinkedList.class);
        Commit targetCommit = t.getFirst(); Commit headCommit = h.getFirst();
        List<String> l = plainFilenamesIn(sa); List<String> s = plainFilenamesIn(stageRemoval);
        List<String> fileNamesToCommit = new LinkedList<>();
        if (headCommit.getFiLi().size() > 0) {
            for (int i = 0; i < headCommit.getFiLi().size(); i += 1) {
                if (s.contains(headCommit.getFiLi().get(i).getName())) {
                    File stagedF = Utils.join(stageRemoval, headCommit.getFiLi().get(i).getName());
                    fileNamesToCommit.add(headCommit.getFiLi().get(i).getName());
                    stagedF.delete();
                } else if (l.contains(headCommit.getFiLi().get(i).getName())) {
                    File stagedFile = Utils.join(sa, headCommit.getFiLi().get(i).getName());
                    ftc.add(stagedFile);
                    btc.add(readContentsAsString(stagedFile));
                    fileNamesToCommit.add(headCommit.getFiLi().get(i).getName());
                    stagedFile.delete();
                } else {
                    ftc.add(headCommit.getFiLi().get(i));
                    btc.add(headCommit.getBlob().get(i));
                    fileNamesToCommit.add(headCommit.getFiLi().get(i).getName());
                }
            }
            for (int i = 0; i < targetCommit.getFiLi().size(); i += 1) {
                if (fileNamesToCommit.contains(targetCommit.getFiLi().get(i).getName())) {
                    continue;
                }
                if (s.contains(targetCommit.getFiLi().get(i).getName())) {
                    File staF = Utils.join(stageRemoval, targetCommit.getFiLi().get(i).getName());
                    staF.delete();
                } else if (l.contains(targetCommit.getFiLi().get(i).getName())) {
                    File stagedFile = Utils.join(sa,
                            targetCommit.getFiLi().get(i).getName());
                    ftc.add(stagedFile);
                    btc.add(readContentsAsString(stagedFile)); stagedFile.delete();
                } else {
                    ftc.add(targetCommit.getFiLi().get(i));
                    btc.add(targetCommit.getBlob().get(i));
                }
            }
        }
        l = plainFilenamesIn(Utils.join(GITLET_DIR, "stages"));
        if (l.size() > 0) {
            for (int i = 0; i < l.size(); i += 1) {
                File stagedFile = Utils.join(sa, l.get(i)); ftc.add(stagedFile);
                btc.add(readContentsAsString(stagedFile)); stagedFile.delete();
            }
            stageRemoval.delete(); File newRemove = Utils.join(GITLET_DIR, "stageRemoval");
            newRemove.mkdir();
        }
        List<String> filesInCWD = Utils.plainFilenamesIn(CWD);
        ArrayList<String> cwdContents = new ArrayList<>();
        for (int i = 0; i < filesInCWD.size(); i += 1) {
            cwdContents.add(readContentsAsString(Utils.join(CWD, filesInCWD.get(i))));
        }
        Commit com = new Commit(message, "*master", ftc,
                btc, filesInCWD, cwdContents);
        com.setHash(sha1(Utils.serialize(c))); File m = Utils.join(cs, com.getHash());
        Utils.writeObject(m, com); File coms = Utils.join(GITLET_DIR, "allCommits");
        LinkedList<Commit> x = Utils.readObject(coms, LinkedList.class);
        x.addFirst(com); Utils.writeObject(coms, x); File tbranch = Utils.join(branches, n);
        LinkedList<Commit> currcoms = readObject(branch, LinkedList.class);
        LinkedList<Commit> currcomb = readObject(tbranch, LinkedList.class);
        currcomb.addFirst(com); currcoms.addFirst(com);
        Utils.writeObject(branch, currcoms); Utils.writeObject(tbranch, currcomb);
        Utils.writeObject(join(GITLET_DIR, "head"), currcoms);
        if (c) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    public static void mergeConflict(String hcontents,
                                     String tcontents, String nameOfFile) {
        File cwdFile = Utils.join(CWD, nameOfFile);
        writeContents(cwdFile, "<<<<<<< HEAD\n"
                + hcontents
                + "=======\n"
                + tcontents
                + ">>>>>>>\n");
    }
}
