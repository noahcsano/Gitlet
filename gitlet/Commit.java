package gitlet;
import java.io.File;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;


/** Represents a gitlet commit object.
 *  does at a high level.
 *
 *  @author Noah
 */
public class Commit implements Serializable {
    /**
     *
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */
    /** The message of this Commit. */
    private String message;

    /** Set up time*/
    private String time;

    private Date date;

    private SimpleDateFormat timeFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy -0800");

    /**Set up branch*/
    private String branch;

    /**List of Files*/
    private ArrayList<File> fileList;

    /**Hash Code*/
    private String hashCode;

    private ArrayList<String> blob;

    private List<String> listOfCWD;

    private ArrayList<String> cwdContents;

    private ArrayList<String> fileNames = new ArrayList<>();

    Commit(String msg, String b, ArrayList<File> files, ArrayList<String> bob,
           List<String> f, ArrayList<String> cwdCon) {
        timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        date = new Date();
        message = msg;
        branch = b;
        fileList = files;
        time = timeFormat.format(date.getTime());
        blob = bob;
        listOfCWD = f;
        cwdContents = cwdCon;
        for (File file : fileList) {
            this.fileNames.add(file.getName());
        }
    }

    public String getM() {
        return message;
    }

    public String getB() {
        return branch;
    }

    public ArrayList<File> getFiLi() {
        return fileList;
    }

    public String getHash() {
        return hashCode;
    }

    public void setHash(String h) {
        this.hashCode = h;
    }

    public ArrayList<String> getBlob() {
        return blob;
    }

    public List<String> getCWD() {
        return listOfCWD;
    }

    public ArrayList<String> cwdCons() {
        return cwdContents;
    }

    public ArrayList<String> fNames() {
        return fileNames;
    }

    public String getTime() {
        return time;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(int d) {
        date.setTime(d);
    }

    public void setTime(Date d) {
        time = timeFormat.format(d);
    }

    public void removeF(int index) {
        fileList.remove(index);
        blob.remove(index);
    }
}
