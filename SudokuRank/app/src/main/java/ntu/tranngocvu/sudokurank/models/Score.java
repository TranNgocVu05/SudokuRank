package ntu.tranngocvu.sudokurank.models;

import com.google.firebase.Timestamp;

public class Score {

    public String userId;
    public String username;
    public String avatar;
    public String rank;
    public String rankImage;
    public String difficulty;
    public String mode;

    public long score;
    public long time;
    public long mistakes;

    public Timestamp createdAt;

    public Score() {
    }
}