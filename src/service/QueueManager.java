package service;

import model.Player;
import java.util.*;

public class QueueManager {
    public static final int PLAYERS_PER_GAME = 4;
    private int maxWaitingPlayers = 15;
    private final String playerIdPrefix;
    private int lastPlayerNumber = 0;

    private final Deque<Player> waitingQueue = new ArrayDeque<>();
    private final List<Player> players = new ArrayList<>();
    private final List<Player> allPlayers = new ArrayList<>();

    public QueueManager() {
        this(0);
    }

    public QueueManager(int lastPlayerNumber) {
        this("P", lastPlayerNumber);
    }

    private QueueManager(String playerIdPrefix, int lastPlayerNumber) {
        if (lastPlayerNumber < 0) {
            throw new IllegalArgumentException("Player ID sequence cannot be negative.");
        }
        this.playerIdPrefix = playerIdPrefix;
        this.lastPlayerNumber = lastPlayerNumber;
    }

    private String generatePlayerId() {
        lastPlayerNumber++;
        return playerIdPrefix + String.format("%03d", lastPlayerNumber);
    }

    // Registers a new player by name with an auto-made ID; returns null if the registered limit is reached.
    public Player registerPlayer(String name) {
        if (isQueueFull()) { return null; }

        Player player = new Player(generatePlayerId(), name);
        players.add(player);
        allPlayers.add(player);
        waitingQueue.addLast(player);
        return player;
    }

    public void restoreRegisteredPlayer(Player player) {
        allPlayers.add(player);
        String id = player.getId();
        if (!id.startsWith(playerIdPrefix)
                || !id.substring(playerIdPrefix.length()).matches("\\d+")) {
            return;
        }
        lastPlayerNumber = Math.max(lastPlayerNumber,
            Integer.parseInt(id.substring(playerIdPrefix.length())));
    }

    public void restoreWaitingPlayer(Player player) {
        players.add(player);
        waitingQueue.addLast(player);
    }

    public void restorePlayingPlayer(Player player) {
        players.add(player);
    }

    // Returns the players who would play next WITHOUT changing the queue (used by "next 4" display).
    public List<Player> peekNextGamePlayers() {
        List<Player> queue = new ArrayList<>(waitingQueue);
        List<Player> eligible = new ArrayList<>();
        for (Player p : queue) {
            if (p.isEligible()) { eligible.add(p); }
        }

        Set<Player> chosen = new HashSet<>();
        if (eligible.size() >= PLAYERS_PER_GAME) {
            // Enough normal players: take the first 4 eligible, skip players keep waiting
            chosen.addAll(eligible.subList(0, PLAYERS_PER_GAME));
        } else if (queue.size() >= PLAYERS_PER_GAME) {
            // Not enough normal players: take all of them, then fill the rest with skip players in queue order
            chosen.addAll(eligible);
            for (Player p : queue) {
                if (chosen.size() == PLAYERS_PER_GAME) { break; }
                if (!p.isEligible()) { chosen.add(p); }
            }
        }

        List<Player> result = new ArrayList<>();
        for (Player p : queue) {          // keep the queue order so teams are fair
            if (chosen.contains(p)) { result.add(p); }
        }
        return result;
    }

    // Returns true when the next game must use skip players (fewer than 4 normal players waiting).
    public boolean needsSkipPlayers() {
        int eligible = 0;
        for (Player p : waitingQueue) {
            if (p.isEligible()) { eligible++; }
        }
        return eligible < PLAYERS_PER_GAME;
    }

    // Returns true when a game can start (4 players waiting, skip players count if needed).
    public boolean canStartGame() {
        return peekNextGamePlayers().size() == PLAYERS_PER_GAME;
    }

    // Takes the 4 players out of the queue for a game; returns null if there are not enough players.
    public List<Player> selectPlayersForGame() {
        List<Player> chosen = peekNextGamePlayers();
        if (chosen.size() < PLAYERS_PER_GAME) { return null; }

        // Position of the last chosen player: skip players before it were "passed over"
        List<Player> queue = new ArrayList<>(waitingQueue);
        int lastChosenIndex = 0;
        for (int i = 0; i < queue.size(); i++) {
            if (chosen.contains(queue.get(i))) { lastChosenIndex = i; }
        }

        int waiting = waitingQueue.size();
        for (int i = 0; i < waiting; i++) {
            Player front = waitingQueue.removeFirst();
            if (chosen.contains(front)) {
                if (!front.isEligible()) {
                    front.consumeSkip();      // a skip player used to fill the game: skip is used up
                }
            } else {
                if (!front.isEligible() && i < lastChosenIndex) {
                    front.consumeSkip();      // skip player was passed over: skip is used up, plays next time
                }
                waitingQueue.addLast(front);  // not chosen: goes back, keeping the same order
            }
        }
        return chosen;
    }

    public boolean selectPlayersForGame(List<Player> selectedPlayers, int teamSize) {
        if (teamSize < 1 || teamSize > 3 || selectedPlayers.size() != teamSize * 2) {
            return false;
        }

        Set<String> selectedIds = new HashSet<>();
        for (Player player : selectedPlayers) {
            if (!selectedIds.add(player.getId()) || !waitingQueue.contains(player)) {
                return false;
            }
        }

        for (Iterator<Player> iterator = waitingQueue.iterator(); iterator.hasNext();) {
            Player waiting = iterator.next();
            if (selectedIds.contains(waiting.getId())) {
                iterator.remove();
                if (!waiting.isEligible()) {
                    waiting.consumeSkip();
                }
            }
        }
        return true;
    }

    // Finishes a game: players who continue go back to the queue, the others leave the system.
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
        for (Player p : returning) {
            p.recordCompletedGame();
        }
        for (Player p : returning) {
            waitingQueue.addLast(p);
        }
        return new ArrayList<>();
    }

    // Removes a waiting player from the queue by ID; returns null if not found.
    public Player leaveQueue(String playerId) {
        Player removed = null;
        for (Iterator<Player> iterator = waitingQueue.iterator(); iterator.hasNext();) {
            Player player = iterator.next();
            if (player.getId().equalsIgnoreCase(playerId)) {
                removed = player;
                iterator.remove();
                break;
            }
        }
        if (removed != null) {
            players.remove(removed);
        }
        return removed;
    }

    // Returns how many players of a finished game may continue (all of them: they are already counted).
    public int getAvailableSlotsAfterGame(List<Player> gamePlayers) {
        return gamePlayers.size();
    }

    // Returns true when waiting + playing players reached the limit (no new registration allowed).
    public boolean isQueueFull() {return players.size() >= maxWaitingPlayers;}

    // Returns how many players are registered in total (waiting + on court).
    public int getTotalRegisteredPlayers() {return players.size();}

    // Sets the registered-players limit (waiting + on court; changes when courts are added/deleted).
    public void setMaxWaitingPlayers(int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("Waiting limit cannot be negative.");
        }
        maxWaitingPlayers = limit;
    }

    // Returns the maximum number of waiting players.
    public int getMaxWaitingPlayers() { return maxWaitingPlayers;}

    public List<Player> getWaitingPlayers() { return new ArrayList<>(waitingQueue); }

    public List<Player> getRegisteredPlayers() { return new ArrayList<>(players); }

    public List<Player> getAllPlayers() { return new ArrayList<>(allPlayers); }

    public String displayWaitingPlayers() {
        StringBuilder text = new StringBuilder();
        for (Player player : waitingQueue) {
            text.append("  ").append(player.getName()).append(" - ").append(player.getId());
            if (!player.isEligible()) {
                text.append("  (skips next game)");
            }
            text.append('\n');
        }
        return text.toString();
    }
}
