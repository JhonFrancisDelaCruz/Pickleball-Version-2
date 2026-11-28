package service;

import gui.Theme;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.sql.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import model.Player;

public final class SqlStore {
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/pickleball_db";
    private static final String DEFAULT_USER = "root";
    private static final int PASSWORD_ITERATIONS = 120_000;

    public Connection openConnection() throws SQLException {
        String url = System.getProperty("pickleball.db.url",
            System.getenv().getOrDefault("PICKLEBALL_DB_URL", DEFAULT_URL));
        String user = System.getProperty("pickleball.db.user",
            System.getenv().getOrDefault("PICKLEBALL_DB_USER", DEFAULT_USER));
        String password = System.getProperty("pickleball.db.password",
            System.getenv().getOrDefault("PICKLEBALL_DB_PASSWORD", ""));
        return DriverManager.getConnection(url, user, password);
    }

    public boolean hasUsers() throws SQLException {
        try (Connection connection = openConnection();
             Statement statement = connection.createStatement();
             ResultSet results = statement.executeQuery("SELECT 1 FROM users LIMIT 1")) {
            return results.next();
        }
    }

    public void createUser(String username, String fullName, char[] password)
            throws SQLException, GeneralSecurityException {
        try (Connection connection = openConnection()) {
            String passwordColumn = getUserPasswordColumn(connection);
            boolean hasEmail = hasUserColumn(connection, "email");
            String columns = "username, " + (hasEmail ? "email, " : "")
                + "`" + passwordColumn + "`, full_name";
            String values = hasEmail ? "?, ?, ?, ?" : "?, ?, ?";
            String sql = "INSERT INTO users (" + columns + ") VALUES (" + values + ")";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, username);
                int parameter = 2;
                if (hasEmail) {
                    statement.setString(parameter++, username + "@offline.local");
                }
                statement.setString(parameter++, hashPassword(password));
                statement.setString(parameter, fullName);
                statement.executeUpdate();
            }
        }
    }

    public int authenticate(String username, char[] password)
            throws SQLException, GeneralSecurityException {
        int userId;
        String storedPassword;
        try (Connection connection = openConnection()) {
            String passwordColumn = getUserPasswordColumn(connection);
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT user_id, `" + passwordColumn + "` FROM users WHERE username = ?")) {
                statement.setString(1, username);
                try (ResultSet results = statement.executeQuery()) {
                    if (!results.next()) {
                        return -1;
                    }
                    userId = results.getInt("user_id");
                    storedPassword = results.getString(passwordColumn);
                }
            }
        }

        if (storedPassword == null) {
            return -1;
        }
        if (storedPassword.startsWith("pbkdf2$")) {
            return verifyPassword(password, storedPassword) ? userId : -1;
        }
        boolean matches = MessageDigest.isEqual(
            new String(password).getBytes(StandardCharsets.UTF_8),
            storedPassword.getBytes(StandardCharsets.UTF_8));
        if (!matches) {
            return -1;
        }
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(
                 "UPDATE users SET `" + getUserPasswordColumn(connection)
                     + "` = ? WHERE user_id = ?")) {
            statement.setString(1, hashPassword(password));
            statement.setInt(2, userId);
            statement.executeUpdate();
        }
        return userId;
    }

    private static String getUserPasswordColumn(Connection connection) throws SQLException {
        boolean hasPassword = false;
        try (ResultSet columns = connection.getMetaData().getColumns(
                connection.getCatalog(), null, "users", null)) {
            while (columns.next()) {
                String name = columns.getString("COLUMN_NAME");
                if ("password_hash".equalsIgnoreCase(name)) {
                    return "password_hash";
                }
                if ("password".equalsIgnoreCase(name)) {
                    hasPassword = true;
                }
            }
        }
        if (hasPassword) {
            return "password";
        }
        throw new SQLException("The users table needs a password_hash or password column.");
    }

    private static boolean hasUserColumn(Connection connection, String columnName)
            throws SQLException {
        try (ResultSet columns = connection.getMetaData().getColumns(
                connection.getCatalog(), null, "users", null)) {
            while (columns.next()) {
                if (columnName.equalsIgnoreCase(columns.getString("COLUMN_NAME"))) {
                    return true;
                }
            }
        }
        return false;
    }

    public DashboardData loadDashboardData(int userId) throws SQLException {
        DashboardData data = new DashboardData();
        try (Connection connection = openConnection()) {
            data.courtCount = getIntSetting(connection, userId, "court_count", 3);
            data.maxCourts = getIntSetting(connection, userId, "max_courts", 10);
            data.waitingLimit = getIntSetting(connection, userId, "waiting_limit", 15);
            data.autoWaiting = getBooleanSetting(connection, userId, "auto_waiting", true);
            data.lastPlayerNumber = loadLastPlayerNumber(connection);
            String theme = getStringSetting(connection, userId, "theme", "LIGHT");
            if ("DARK".equalsIgnoreCase(theme)) {
                data.themeMode = Theme.Mode.DARK;
            } else if ("LIGHT".equalsIgnoreCase(theme)) {
                data.themeMode = Theme.Mode.LIGHT;
            } else {
                throw new SQLException("Invalid saved theme setting: " + theme);
            }

            String playerSql = "SELECT player_id, name, wins, consecutive_games, "
                + "skip_next_selection, leave_after_game, player_status, queue_position, "
                + "court_number, court_slot FROM players WHERE user_id = ? "
                + "ORDER BY CASE WHEN player_status = 'WAITING' THEN 0 "
                + "WHEN player_status = 'PLAYING' THEN 1 ELSE 2 END, queue_position, player_id";
            try (PreparedStatement statement = connection.prepareStatement(playerSql)) {
                statement.setInt(1, userId);
                try (ResultSet results = statement.executeQuery()) {
                    while (results.next()) {
                        Player player = new Player(
                            results.getString("player_id"),
                            results.getString("name"),
                            results.getInt("wins"),
                            results.getInt("consecutive_games"),
                            results.getBoolean("skip_next_selection"),
                            results.getBoolean("leave_after_game"));
                        int courtNumber = results.getInt("court_number");
                        if (results.wasNull()) {
                            courtNumber = 0;
                        }
                        int courtSlot = results.getInt("court_slot");
                        if (results.wasNull()) {
                            courtSlot = 0;
                        }
                        int queuePosition = results.getInt("queue_position");
                        if (results.wasNull()) {
                            queuePosition = -1;
                        }
                        data.players.add(new PlayerState(player,
                            results.getString("player_status"), queuePosition,
                            courtNumber, courtSlot));
                    }
                }
            }
            loadCompletedGames(connection, userId, data);
        }
        return data;
    }

    private static int loadLastPlayerNumber(Connection connection) throws SQLException {
        int lastNumber = 0;
        try (Statement statement = connection.createStatement();
             ResultSet results = statement.executeQuery(
                 "SELECT player_id FROM players WHERE player_id LIKE 'P%'")) {
            while (results.next()) {
                String playerId = results.getString(1);
                if (playerId.matches("P\\d+")) {
                    try {
                        lastNumber = Math.max(lastNumber,
                            Integer.parseInt(playerId.substring(1)));
                    } catch (NumberFormatException exception) {
                        throw new SQLException("Player ID number is too large: " + playerId,
                            exception);
                    }
                }
            }
        }
        return lastNumber;
    }

    public void saveSettings(int userId, int courtCount, int maxCourts, int waitingLimit,
                             boolean autoWaiting, Theme.Mode themeMode) throws SQLException {
        try (Connection connection = openConnection()) {
            connection.setAutoCommit(false);
            try {
                putSetting(connection, userId, "court_count", String.valueOf(courtCount));
                putSetting(connection, userId, "max_courts", String.valueOf(maxCourts));
                putSetting(connection, userId, "waiting_limit", String.valueOf(waitingLimit));
                putSetting(connection, userId, "auto_waiting", String.valueOf(autoWaiting));
                putSetting(connection, userId, "theme", themeMode.name());
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    public void saveDashboardState(int userId, List<Player> allPlayers, List<Player> waitingPlayers,
                                   Map<Integer, List<Player>> activeGames) throws SQLException {
        Map<String, Integer> waitingOrder = new LinkedHashMap<>();
        for (int i = 0; i < waitingPlayers.size(); i++) {
            waitingOrder.put(waitingPlayers.get(i).getId(), i);
        }
        Map<String, int[]> playingOrder = new LinkedHashMap<>();
        for (Map.Entry<Integer, List<Player>> game : activeGames.entrySet()) {
            for (int i = 0; i < game.getValue().size(); i++) {
                playingOrder.put(game.getValue().get(i).getId(), new int[]{game.getKey(), i});
            }
        }

        String sql = "INSERT INTO players (user_id, player_id, name, wins, consecutive_games, "
            + "skip_next_selection, leave_after_game, player_status, queue_position, "
            + "court_number, court_slot) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
            + "ON DUPLICATE KEY UPDATE name=VALUES(name), wins=VALUES(wins), "
            + "consecutive_games=VALUES(consecutive_games), "
            + "skip_next_selection=VALUES(skip_next_selection), "
            + "leave_after_game=VALUES(leave_after_game), player_status=VALUES(player_status), "
            + "queue_position=VALUES(queue_position), court_number=VALUES(court_number), "
            + "court_slot=VALUES(court_slot)";
        try (Connection connection = openConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement update = connection.prepareStatement(
                     "UPDATE players SET player_status='LEFT', queue_position=NULL, "
                         + "court_number=NULL, court_slot=NULL WHERE user_id = ?");
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                update.setInt(1, userId);
                update.executeUpdate();
                for (Player player : allPlayers) {
                    Integer position = waitingOrder.get(player.getId());
                    int[] court = playingOrder.get(player.getId());
                    String status = position != null ? "WAITING" : court != null ? "PLAYING" : "LEFT";
                    statement.setInt(1, userId);
                    statement.setString(2, player.getId());
                    statement.setString(3, player.getName());
                    statement.setInt(4, player.getWins());
                    statement.setInt(5, player.getConsecutiveGames());
                    statement.setBoolean(6, !player.isEligible());
                    statement.setBoolean(7, player.isLeaveAfterGame());
                    statement.setString(8, status);
                    if (position == null) statement.setNull(9, java.sql.Types.INTEGER);
                    else statement.setInt(9, position);
                    if (court == null) {
                        statement.setNull(10, java.sql.Types.INTEGER);
                        statement.setNull(11, java.sql.Types.INTEGER);
                    } else {
                        statement.setInt(10, court[0]);
                        statement.setInt(11, court[1]);
                    }
                    statement.addBatch();
                }
                statement.executeBatch();
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    public void saveCompletedGame(int userId, int courtNumber, char winningTeam,
                                  List<Player> players)
            throws SQLException {
        if (players.size() < 2 || players.size() % 2 != 0) {
            throw new IllegalArgumentException("A match requires equal teams with at least one player each.");
        }
        int teamSize = players.size() / 2;
        String gameSql = "INSERT INTO games (user_id, court_number, winning_team) VALUES (?, ?, ?)";
        String playerSql = "INSERT INTO game_players "
            + "(game_id, player_id, player_name, team, won) VALUES (?, ?, ?, ?, ?)";
        List<Player> winners = new ArrayList<>();
        try (Connection connection = openConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement gameStatement = connection.prepareStatement(
                    gameSql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement playerStatement = connection.prepareStatement(playerSql);
                 PreparedStatement updateWins = connection.prepareStatement(
                     "UPDATE players SET wins = wins + 1 WHERE user_id = ? AND player_id = ?")) {
                gameStatement.setInt(1, userId);
                gameStatement.setInt(2, courtNumber);
                gameStatement.setString(3, String.valueOf(winningTeam));
                gameStatement.executeUpdate();
                try (ResultSet keys = gameStatement.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("The database did not return the saved game ID.");
                    }
                    long gameId = keys.getLong(1);
                    for (int i = 0; i < players.size(); i++) {
                        Player player = players.get(i);
                        char team = i < teamSize ? 'A' : 'B';
                        boolean won = team == winningTeam;
                        if (won) {
                            winners.add(player);
                            updateWins.setInt(1, userId);
                            updateWins.setString(2, player.getId());
                            updateWins.addBatch();
                        }
                        playerStatement.setLong(1, gameId);
                        playerStatement.setString(2, player.getId());
                        playerStatement.setString(3, player.getName());
                        playerStatement.setString(4, String.valueOf(team));
                        playerStatement.setBoolean(5, won);
                        playerStatement.addBatch();
                    }
                    playerStatement.executeBatch();
                    updateWins.executeBatch();
                }
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }
        }
        for (Player winner : winners) {
            winner.addWin();
        }
    }

    public String createPlayerReport(int userId) throws SQLException {
        StringBuilder report = new StringBuilder();
        report.append("PICKLEBALL OPEN PLAY\nPLAYER AND GAME REPORT\n")
            .append("Generated: ").append(new Timestamp(System.currentTimeMillis())).append("\n")
            .append("=".repeat(86)).append("\n\n")
            .append(String.format("%-12s %-28s %8s %-12s%n",
                "PLAYER ID", "PLAYER NAME", "WINS", "STATUS"))
            .append("-".repeat(68)).append('\n');

        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(
                 "SELECT player_id, name, wins, player_status FROM players "
                 + "WHERE user_id = ? ORDER BY wins DESC, name, player_id")) {
            statement.setInt(1, userId);
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    report.append(String.format("%-12s %-28s %8d %-12s%n",
                        results.getString("player_id"),
                        fit(results.getString("name"), 28),
                        results.getInt("wins"),
                        results.getString("player_status")));
                }
            }
        }

        report.append("\n\nCOMPLETED GAMES\n")
            .append("=".repeat(86)).append('\n');
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(
                 "SELECT g.game_id, g.court_number, g.winning_team, g.completed_at, "
                 + "(SELECT COUNT(*) / 2 FROM game_players all_players "
                 + "WHERE all_players.game_id = g.game_id) AS team_size, "
                 + "GROUP_CONCAT(CASE WHEN gp.won THEN gp.player_name END "
                 + "ORDER BY gp.team, gp.player_id SEPARATOR ', ') AS winners "
                 + "FROM games g LEFT JOIN game_players gp ON gp.game_id = g.game_id "
                 + "WHERE g.user_id = ? "
                 + "GROUP BY g.game_id, g.court_number, g.winning_team, g.completed_at "
                 + "ORDER BY g.game_id")) {
            statement.setInt(1, userId);
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    report.append(String.format(
                        "Game %d | Court %d | %d vs %d | Team %s | Winners: %s | %s%n",
                        results.getLong("game_id"),
                        results.getInt("court_number"),
                        results.getInt("team_size"),
                        results.getInt("team_size"),
                        results.getString("winning_team"),
                        results.getString("winners"),
                        results.getTimestamp("completed_at")));
                }
            }
        }
        return report.toString();
    }

    private static String fit(String value, int width) {
        if (value == null) {
            return "";
        }
        return value.length() <= width ? value : value.substring(0, width - 3) + "...";
    }

    private static void loadCompletedGames(Connection connection, int userId, DashboardData data)
            throws SQLException {
        String sql = "SELECT g.game_id, g.court_number, g.winning_team, "
            + "(SELECT COUNT(*) / 2 FROM game_players all_players "
            + "WHERE all_players.game_id = g.game_id) AS team_size, "
            + "gp.player_id, gp.player_name FROM games g "
            + "JOIN game_players gp ON gp.game_id = g.game_id AND gp.won = TRUE "
            + "WHERE g.user_id = ? "
            + "ORDER BY g.game_id, gp.player_id";
        Map<Long, GameRecord> records = new LinkedHashMap<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    long id = results.getLong("game_id");
                    GameRecord record = records.get(id);
                    if (record == null) {
                        record = new GameRecord(id, results.getInt("court_number"),
                            results.getString("winning_team").charAt(0),
                            results.getInt("team_size"));
                        records.put(id, record);
                    }
                    record.winnerIds.add(results.getString("player_id"));
                    record.winnerNames.add(results.getString("player_name"));
                }
            }
        }
        data.games.addAll(records.values());
    }

    private static int getIntSetting(Connection connection, int userId, String key, int fallback)
            throws SQLException {
        String value = getStringSetting(connection, userId, key, null);
        if (value == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new SQLException("Invalid saved integer setting for " + key + ": " + value,
                exception);
        }
    }

    private static boolean getBooleanSetting(Connection connection, int userId, String key,
                                             boolean fallback)
            throws SQLException {
        String value = getStringSetting(connection, userId, key, null);
        if (value == null) {
            return fallback;
        }
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return Boolean.parseBoolean(value);
        }
        throw new SQLException("Invalid saved boolean setting for " + key + ": " + value);
    }

    private static String getStringSetting(Connection connection, int userId, String key,
                                           String fallback)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT setting_value FROM account_settings "
                    + "WHERE user_id = ? AND setting_key = ?")) {
            statement.setInt(1, userId);
            statement.setString(2, key);
            try (ResultSet results = statement.executeQuery()) {
                return results.next() ? results.getString(1) : fallback;
            }
        }
    }

    private static void putSetting(Connection connection, int userId, String key, String value)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO account_settings (user_id, setting_key, setting_value) "
                    + "VALUES (?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE setting_value=VALUES(setting_value)")) {
            statement.setInt(1, userId);
            statement.setString(2, key);
            statement.setString(3, value);
            statement.executeUpdate();
        }
    }

    private static String hashPassword(char[] password) throws GeneralSecurityException {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        byte[] hash = derivePassword(password, salt, PASSWORD_ITERATIONS);
        return "pbkdf2$" + PASSWORD_ITERATIONS + "$"
            + Base64.getEncoder().encodeToString(salt) + "$"
            + Base64.getEncoder().encodeToString(hash);
    }

    private static boolean verifyPassword(char[] password, String stored)
            throws GeneralSecurityException {
        String[] parts = stored.split("\\$", -1);
        if (parts.length != 4) {
            return false;
        }
        try {
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            return MessageDigest.isEqual(expected, derivePassword(password, salt, iterations));
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private static byte[] derivePassword(char[] password, byte[] salt, int iterations)
            throws GeneralSecurityException {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, 256);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec).getEncoded();
        } finally {
            spec.clearPassword();
        }
    }

    public static final class DashboardData {
        public int courtCount;
        public int maxCourts;
        public int waitingLimit;
        public int lastPlayerNumber;
        public boolean autoWaiting;
        public Theme.Mode themeMode;
        public final List<PlayerState> players = new ArrayList<>();
        public final List<GameRecord> games = new ArrayList<>();
    }

    public static final class PlayerState {
        public final Player player;
        public final String status;
        public final int queuePosition;
        public final int courtNumber;
        public final int courtSlot;

        private PlayerState(Player player, String status, int queuePosition,
                            int courtNumber, int courtSlot) {
            this.player = player;
            this.status = status;
            this.queuePosition = queuePosition;
            this.courtNumber = courtNumber;
            this.courtSlot = courtSlot;
        }
    }

    public static final class GameRecord {
        public final long gameId;
        public final int courtNumber;
        public final int teamSize;
        public final char winningTeam;
        public final List<String> winnerIds = new ArrayList<>();
        public final List<String> winnerNames = new ArrayList<>();

        private GameRecord(long gameId, int courtNumber, char winningTeam, int teamSize) {
            this.gameId = gameId;
            this.courtNumber = courtNumber;
            this.winningTeam = winningTeam;
            this.teamSize = teamSize;
        }
    }
}
