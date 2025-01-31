package me.msuro.mGiveaway.utils;

        import me.msuro.mGiveaway.MGiveaway;
        import me.msuro.mGiveaway.classes.Giveaway;

        import java.io.File;
        import java.sql.*;
        import java.util.HashMap;
        import java.util.Map;

        public class DBUtils {
            private static MGiveaway instance;
            private static File dbFile;

            public DBUtils() {
                try {
                    instance = MGiveaway.getInstance();

                    dbFile = new File(instance.getDataFolder(), "mgiveaway.sqlite");
                    if (!dbFile.exists()) {
                        instance.saveResource("mgiveaway.sqlite", false);
                        instance.getLogger().info("Database file created!");
                    } else {
                        instance.getLogger().info("Database file loaded successfully!");
                    }
                    Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
                    Statement statement = connection.createStatement();
                    if(statement == null || statement.isClosed()) {
                        throw new RuntimeException("Failed to create database file!");
                    }

                } catch (SQLException e) {
                    throw new RuntimeException("Failed to load database file!", e);
                }

            }

            private static Connection getConnection() {
                try {
                    return DriverManager.getConnection("jdbc:sqlite:" + dbFile);
                } catch (SQLException e) {
                    throw new RuntimeException("Failed to get connection to database!", e);
                }
            }

            public void createGiveawayTable(String giveawayName) {
                try {
                    Connection conn = getConnection();
                    Statement statement = conn.createStatement();
                    statement.execute("CREATE TABLE IF NOT EXISTS `entries-" + giveawayName + "` (" +
                            "`discord_id` varchar(255) NOT NULL," +
                            "`minecraft_name` varchar(255) NOT NULL," +
                            "PRIMARY KEY (`discord_id`, `minecraft_name`));");

                    statement.close();
                    conn.close();

                } catch (SQLException e) {
                    throw new RuntimeException("Failed to create giveaway table!", e);
                }
            }

            public HashMap<String, String> refreshEntries(Giveaway giveaway) {
                HashMap<String, String> entries = new HashMap<>();
                try (Connection conn = getConnection()) {

                    Statement statement = conn.createStatement();
                    statement.execute("SELECT * FROM `entries-" + giveaway.getName() + "`;");
                    ResultSet resultSet = statement.getResultSet();
                    while (resultSet.next()) {
                        entries.put(resultSet.getString("discord_id"), resultSet.getString("minecraft_name"));
                    }

                    statement.close();
                    conn.close();

                } catch (SQLException e) {
                    throw new RuntimeException("Failed to refresh entries (giveaway: " + giveaway.getName() + ")!", e);
                }
                return entries;
            }

            public void saveEntries(Giveaway giveaway) {
                HashMap<String, String> entries = instance.getEntries().get(giveaway);
                if(entries == null || entries.isEmpty()) return;
                try {
                    instance.getLogger().info("Saving entries for giveaway: " + giveaway.getName() + " (" + entries.size() + ")");
                    Connection conn = getConnection();
                    String sql = "INSERT OR REPLACE INTO `entries-" + giveaway.getName() + "` (discord_id, minecraft_name) VALUES (?, ?)";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    for (Map.Entry<String, String> entry : entries.entrySet()) {
                        pstmt.setString(1, entry.getKey());
                        pstmt.setString(2, entry.getValue());
                        pstmt.addBatch();
                    }
                    pstmt.executeBatch();
                    pstmt.close();
                    conn.close();

                } catch (SQLException e) {
                    throw new RuntimeException("Failed to save entries (giveaway: " + giveaway.getName() + ")!", e);
                }
            }
        }