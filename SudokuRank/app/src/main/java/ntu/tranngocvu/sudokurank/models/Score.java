package ntu.tranngocvu.sudokurank.models;

import com.google.firebase.Timestamp;

public class Score {
    private String userId;
    private String username;
    private long score;
    private long time;
    private long mistakes;
    private String difficulty;
    private String mode;
    private Timestamp createdAt;

    public Score() {}

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public long getScore() { return score; }
    public void setScore(long score) { this.score = score; }

    public long getTime() { return time; }
    public void setTime(long time) { this.time = time; }

    public long getMistakes() { return mistakes; }
    public void setMistakes(long mistakes) { this.mistakes = mistakes; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
