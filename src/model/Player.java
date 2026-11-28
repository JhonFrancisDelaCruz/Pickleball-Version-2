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

    public Player(String id, String name, int wins, int consecutiveGames,
                  boolean skipNextSelection, boolean leaveAfterGame) {
        this.id = id;
        this.name = name;
        this.wins = wins;
        this.consecutiveGames = consecutiveGames;
        this.skipNextSelection = skipNextSelection;
        this.leaveAfterGame = leaveAfterGame;
    }

    // Returns the player ID (like P001).
    public String getId() {return id;}
    // Returns the player name.
    public String getName() { return name;}

    // Returns how many games in a row the player played.
    public int getConsecutiveGames() { return consecutiveGames;}

    // True if the player can be picked now (false = must skip the next game).
    public boolean isEligible() {return !skipNextSelection;}

    // True if the player asked to leave after the current game.
    public boolean isLeaveAfterGame() { return leaveAfterGame;}

    // Marks the player to leave after the current game.
    public void setLeaveAfterGame(boolean leaveAfterGame) { this.leaveAfterGame = leaveAfterGame;}

    // Returns how many games the player won.
    public int getWins() { return wins;}
    // Adds 1 win to the player.
    public void addWin() {wins++;}

    // Counts a finished game; after 2 in a row the player must skip the next game.
    public void recordCompletedGame() {
        consecutiveGames++;
        if (consecutiveGames >= 2) {
            skipNextSelection = true; 
        }
    }

    // Uses up the skip: the player is eligible again and the game count resets.
    public void consumeSkip() {
        skipNextSelection = false;
        consecutiveGames = 0;
    }

    @Override
    public String toString() {
        return id + " - " + name;
    }
}
