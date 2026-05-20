package ntu.tranngocvu.sudokurank.models;

public class User {
    public String name;
    public String email;
    public String avatar;
    public String rank;
    public String theme;
    public long bestScore;
    public long totalGames;

    public User() {
        // Required for Firestore
    }

    public User(String name, String email) {
        this.name = name;
        this.email = email;
        this.avatar = "avatar_1"; // Default avatar name in drawable
        this.rank = "Newbie";
        this.theme = "Light";
        this.bestScore = 0;
        this.totalGames = 0;
    }
}
