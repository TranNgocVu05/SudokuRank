package ntu.tranngocvu.sudokurank.models;

public class User {
    public String name, email, avatar, rank, rankImage, theme;
    public long bestScore, totalGames, winStreak, loseStreak;

    public User() {
        // Required for Firestore
    }

    public User(String name, String email, String avatar) {
        this.name = name;
        this.email = email;
        this.avatar = avatar;
        this.rank = "Bronze I";
        this.rankImage = "rank_bronze";
        this.theme = "default";
        this.bestScore = 0;
        this.totalGames = 0;
        this.winStreak = 0;
        this.loseStreak = 0;
    }
}
