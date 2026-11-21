
package service;

import model.Player;
import queue.PlayerQueue;
import java.util.ArrayList;
import java.util.List;

public class QueueManager {
<<<<<<< HEAD

    private int maxWaitingPlayers = 15;

    public static final int MAX_WAITING = 15;
    public static final int PLAYERS_PER_GAME = 4;

    public enum RegisterResult {
        SUCCESS, DUPLICATE, QUEUE_FULL
    }

    private final PlayerQueue playerQueue = new PlayerQueue();
    private final List<Player> players = new ArrayList<>(); // Waiting + active

    // ---------- Registration ----------

    public RegisterResult registerPlayer(Player player) {

        if (isPlayerRegistered(player.getId())) {
            return RegisterResult.DUPLICATE;
        }

        if (isQueueFull()) {
            return RegisterResult.QUEUE_FULL;
        }

        players.add(player);
        playerQueue.enqueue(player);

=======
    public static final int PLAYERS_PER_GAME = 4;
    public enum RegisterResult { SUCCESS, DUPLICATE, QUEUE_FULL }
    private int maxWaitingPlayers = 15;

    private final PlayerQueue waitingQueue = new PlayerQueue(); 
    private final List<Player> players = new ArrayList<>();    

    public RegisterResult registerPlayer(Player player) {
        if (findPlayer(player.getId()) != null) { return RegisterResult.DUPLICATE;}
        if (isQueueFull()) { return RegisterResult.QUEUE_FULL;}
        
        players.add(player);
        waitingQueue.enqueue(player);
>>>>>>> 6a49074ee3a6bc2b71725774bd3ac655d9132bfd
        return RegisterResult.SUCCESS;
    }

    public List<Player> peekNextEligible(int n) {
        List<Player> result = new ArrayList<>();
        for (Player p : waitingQueue.toList()) {
            if (result.size() == n) { break; }
            if (p.isEligible()) {result.add(p);}
        }
        return result;
    }

    public int countEligible() {return peekNextEligible(Integer.MAX_VALUE).size();}

    public List<Player> selectPlayersForGame() {
        if (countEligible() < PLAYERS_PER_GAME) { return null; }
        List<Player> chosen = new ArrayList<>();
        int waiting = waitingQueue.size();
        for (int i = 0; i < waiting; i++) {
            Player front = waitingQueue.peek();  
            waitingQueue.dequeue();              
            boolean needMore = chosen.size() < PLAYERS_PER_GAME;
            if (needMore && front.isEligible()) {
                chosen.add(front);              
            } else {
                if (needMore) {
                    front.consumeSkip();         
                }
                waitingQueue.enqueue(front);      
            }
        } return chosen;
    }

    public List<Player> completeGame(List<Player> gamePlayers, List<Player> continuing) {
        List<Player> returning = new ArrayList<>();
        for (Player p : gamePlayers) {
            boolean stays = continuing.contains(p) && !p.isLeaveAfterGame();
            if (stays) {
                returning.add(p);
            } else {
                players.remove(p);        
            }
        }
        return rotatePlayers(returning);
    }

    public List<Player> rotatePlayers(List<Player> returning) {
        List<Player> noRoom = new ArrayList<>();
        for (Player p : returning) {
            p.recordCompletedGame();            
            if (isQueueFull()) {
                players.remove(p);
                noRoom.add(p);
            } else {
                waitingQueue.enqueue(p);       
            }
        }
        return noRoom;
    }

    public Player leaveQueue(String playerId) {
        Player removed = waitingQueue.remove(playerId);
        if (removed != null) {
            players.remove(removed);
        }
        return removed;
    }
    
    public int getAvailableSlotsAfterGame(List<Player> gamePlayers) {
    return Math.max(0, maxWaitingPlayers - waitingQueue.size());
    }

    public boolean isQueueFull() {return waitingQueue.size() >= maxWaitingPlayers;}

    public void setMaxWaitingPlayers(int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("Waiting limit cannot be negative.");
        }
        maxWaitingPlayers = limit;
    }

    public int getMaxWaitingPlayers() { return maxWaitingPlayers;}

    public PlayerQueue getPlayerQueue() { return waitingQueue;}

    private Player findPlayer(String playerId) {
        for (Player player : players) {
            if (player.getId().equalsIgnoreCase(playerId)) {
                return player;
            }
        }
        return null;
    }
<<<<<<< HEAD

    // Checks TOTAL registered players (waiting + active)
    public boolean isQueueFull() {
        return players.size() >= maxWaitingPlayers;
    }

    public int getTotalRegisteredPlayers() {
        return players.size();
    }

    // ---------- Eligibility / court assignment ----------

    public List<Player> peekNextEligible(int n) {

        List<Player> result = new ArrayList<>();

        for (Player p : playerQueue.toList()) {
            if (result.size() == n) {
                break;
            }

            if (p.isEligible()) {
                result.add(p);
            }
        }

        return result;
    }

    public int countEligible() {
        return peekNextEligible(Integer.MAX_VALUE).size();
    }

    // ---------- Capacity ----------

    public void setMaxWaitingPlayers(int limit) {

        if (limit < 0) {
            throw new IllegalArgumentException(
                "Waiting limit cannot be negative."
            );
        }

        maxWaitingPlayers = limit;
    }

    public int getMaxWaitingPlayers() {
        return maxWaitingPlayers;
    }

    // ---------- Select players for a game ----------

    public List<Player> selectPlayersForGame() {

        List<Player> chosen = peekNextEligible(PLAYERS_PER_GAME);

        if (chosen.size() < PLAYERS_PER_GAME) {
            return null;
        }

        Player last = chosen.get(chosen.size() - 1);

        // Players passed over before the last selected player
        // consume their skip
        for (Player p : playerQueue.toList()) {

            if (p == last) {
                break;
            }

            if (!p.isEligible()) {
                p.consumeSkip();
            }
        }

        // Remove selected players from waiting queue.
        // They remain in the registered players list.
        for (Player p : chosen) {
            playerQueue.remove(p.getId());
        }

        return chosen;
    }

    // ---------- Leaving ----------

    public Player leaveQueue(String playerId) {

        Player removed = playerQueue.remove(playerId);

        if (removed != null) {
            players.remove(removed);
        }

        return removed;
    }

    // ---------- Completing a game ----------

    public List<Player> completeGame(
            List<Player> gamePlayers,
            List<Player> continuing) {

        List<Player> noRoom = new ArrayList<>();

        for (Player p : gamePlayers) {

            boolean stays =
                continuing.contains(p) && !p.isLeaveAfterGame();

            if (!stays) {

                // Player leaves the system and frees capacity
                players.remove(p);

                continue;
            }

            // Player is already registered.
            // Do NOT check capacity again.
            p.recordCompletedGame();

            // Return to the back of the waiting queue
            playerQueue.enqueue(p);
        }

        return noRoom;
    }

    // ---------- Getters ----------

    public PlayerQueue getPlayerQueue() {
        return playerQueue;
    }

    public List<Player> getPlayers() {
        return players;
    }
=======
>>>>>>> 6a49074ee3a6bc2b71725774bd3ac655d9132bfd
}
