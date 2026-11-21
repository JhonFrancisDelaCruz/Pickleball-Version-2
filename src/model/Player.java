package model;

public class Player {
    private final String id;
    private final String name;
    private int consecutiveGames = 0;        
    private boolean skipNextSelection = false; 
    private boolean leaveAfterGame = false;  
    private int wins = 0;

    public Player(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getConsecutiveGames() {
        return consecutiveGames;
    }

    public boolean isEligible() {
        return !skipNextSelection;
    }

    public boolean isLeaveAfterGame() {
        return leaveAfterGame;
    }

    public void setLeaveAfterGame(boolean leaveAfterGame) {
        this.leaveAfterGame = leaveAfterGame;
    }

    public int getWins() {
        return wins;
    }

    public void addWin() {
        wins++;
    }

    public void recordCompletedGame() {
        consecutiveGames++;
        if (consecutiveGames >= 2) {
            skipNextSelection = true; 
        }
    }

    public void consumeSkip() {
        skipNextSelection = false;
        consecutiveGames = 0;
    }

    @Override
    public String toString() {
        return id + " - " + name;
    }
}
