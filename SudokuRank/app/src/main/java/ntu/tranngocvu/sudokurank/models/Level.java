package ntu.tranngocvu.sudokurank.models;

public class Level {
    private long level;
    private String difficulty;
    private String puzzle;
    private String solution;

    public Level() {}

    public long getLevel() { return level; }
    public void setLevel(long level) { this.level = level; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getPuzzle() { return puzzle; }
    public void setPuzzle(String puzzle) { this.puzzle = puzzle; }

    public String getSolution() { return solution; }
    public void setSolution(String solution) { this.solution = solution; }
}
