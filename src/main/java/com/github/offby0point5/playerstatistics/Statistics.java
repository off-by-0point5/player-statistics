package com.github.offby0point5.playerstatistics;

import java.util.*;

public class Statistics {
    private final Database database;


    private final Map<UUID, Map<String, Long>> serverPlaytime = new HashMap<>();
    private final Map<UUID, Long> proxyPlaytime = new HashMap<>();

    private final Map<UUID, Database.TimelineEvent> playerServerConnectEvent = new HashMap<>();
    private final Map<UUID, Database.TimelineEvent> playerProxyConnectEvent = new HashMap<>();

    private long lastUpdate = 0;

    public Statistics(Database database) {
        this.database = database;
    }

    public Database getDatabase() {
        return database;
    }
    
    public void generateStatistics() {
        if (System.currentTimeMillis() - lastUpdate < 5000) return;
        lastUpdate = System.currentTimeMillis();

        serverPlaytime.clear();
        proxyPlaytime.clear();
        playerServerConnectEvent.clear();
        playerProxyConnectEvent.clear();

        for (Database.TimelineEvent event : database.getTimeline(0, System.currentTimeMillis())) {
            if (event.getServer() == null) {
                if (event.isConnect()) playerProxyConnectEvent.put(event.getPlayer().getUuid(), event);
                else if (playerProxyConnectEvent.getOrDefault(event.getPlayer().getUuid(), null) != null) {
                    proxyPlaytime.putIfAbsent(event.getPlayer().getUuid(), 0L);
                    proxyPlaytime.computeIfPresent(event.getPlayer().getUuid(), (k, v) -> {
                        long playtime = event.getTimestamp() - playerProxyConnectEvent.get(k).getTimestamp();
                        return v+playtime;
                    });
                    playerProxyConnectEvent.remove(event.getPlayer().getUuid());
                }
            } else {
                if (event.isConnect()) playerServerConnectEvent.put(event.getPlayer().getUuid(), event);
                else if (playerServerConnectEvent.getOrDefault(event.getPlayer().getUuid(), null) != null) {
                    serverPlaytime.putIfAbsent(event.getPlayer().getUuid(), new HashMap<>());
                    serverPlaytime.get(event.getPlayer().getUuid()).putIfAbsent(event.getServer(), 0L);
                    serverPlaytime.get(event.getPlayer().getUuid()).computeIfPresent(event.getServer(), (k, v) -> {
                        long playtime = event.getTimestamp() - playerServerConnectEvent.get(event.getPlayer().getUuid()).getTimestamp();
                        return v+playtime;
                    });
                    playerServerConnectEvent.remove(event.getPlayer().getUuid());
                }
            }
        }
        for (Database.StatisticsPlayer player : getUniquePlayers()) {
            proxyPlaytime.putIfAbsent(player.getUuid(), 0L);
            proxyPlaytime.computeIfPresent(player.getUuid(), (k, v) -> {
                long playtime = System.currentTimeMillis() - playerProxyConnectEvent.get(k).getTimestamp();
                return v+playtime;
            });

            serverPlaytime.putIfAbsent(player.getUuid(), new HashMap<>());
            String server = playerServerConnectEvent.get(player.getUuid()).getServer();
            serverPlaytime.get(player.getUuid()).putIfAbsent(server, 0L);
            serverPlaytime.get(player.getUuid()).computeIfPresent(server, (k, v) -> {
                long playtime = System.currentTimeMillis() - playerServerConnectEvent.get(player.getUuid()).getTimestamp();
                return v+playtime;
            });
        }
    }

    public List<String> getUniquePlayerNames() {
        return List.copyOf(new HashSet<>(playerProxyConnectEvent.values().stream()
                .map(Database.TimelineEvent::getPlayer).map(Database.StatisticsPlayer::getUsername).toList()));
    }

    public List<Database.StatisticsPlayer> getUniquePlayers() {
        return List.copyOf(new HashSet<>(playerProxyConnectEvent.values().stream()
                .map(Database.TimelineEvent::getPlayer).toList()));
    }

    public List<String> getUniqueServers() {
        return List.copyOf(new HashSet<>(playerProxyConnectEvent.values().stream()
                .map(Database.TimelineEvent::getServer).toList()));
    }

    public Map<String, Long> getPerServerPlaytime(UUID player) {
        return serverPlaytime.getOrDefault(player, new HashMap<>());
    }

    public long getProxyPlaytime(UUID player) {
        return proxyPlaytime.getOrDefault(player, 0L);
    }
}
