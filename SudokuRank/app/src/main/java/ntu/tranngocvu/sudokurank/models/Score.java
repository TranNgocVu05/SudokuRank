package ntu.tranngocvu.sudokurank.models;

import com.google.firebase.Timestamp;

public class Score {
    public String userId, username, avatar, rankImage, difficulty, mode;
    public long score, time, mistakes;
    public Timestamp createdAt;

    public Score() {}
}
