package ntu.tranngocvu.sudokurank.models;

public class Level {
    private int level;
    private String difficulty;
    private String puzzle;
    private String solution;

    public Level() {}

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getPuzzle() { return puzzle; }
    public void setPuzzle(String puzzle) { this.puzzle = puzzle; }

    public String getSolution() { return solution; }
    public void setSolution(String solution) { this.solution = solution; }
}
