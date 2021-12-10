package com.github.offby0point5.playerstatistics;

import com.velocitypowered.api.proxy.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public interface Database {
    void addConnectProxy(Player player, long timestamp);
    void addDisconnectProxy(Player player, long timestamp);
    void addConnectServer(Player player, String server, long timestamp);
    void addDisconnectServer(Player player, String server, long timestamp);

    List<TimelineEvent> getPlayerTimeline(StatisticsPlayer player, long oldest, long newest);
    List<TimelineEvent> getPlayerTimeline(String player, long oldest, long newest);
    List<TimelineEvent> getPlayerTimeline(UUID player, long oldest, long newest);

    List<TimelineEvent> getServerTimeline(String server, long oldest, long newest);
    List<TimelineEvent> getProxyTimeline(long oldest, long newest);
    List<TimelineEvent> getTimeline(long oldest, long newest);

    class TimelineEvent {
        private final long timestamp;
        private final String server;  // null if connection to proxy
        private final StatisticsPlayer player;
        private final boolean connect;

        public TimelineEvent(long timestamp, String server, StatisticsPlayer player, boolean connect) {
            this.timestamp = timestamp;
            this.server = server;
            this.player = player;
            this.connect = connect;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public @Nullable String getServer() {
            return server;
        }

        public @NotNull StatisticsPlayer getPlayer() {
            return player;
        }

        public boolean isConnect() {
            return connect;
        }
    }

    class StatisticsPlayer {
        private final UUID uuid;
        private final String username;

        public StatisticsPlayer(UUID uuid, String username) {
            this.uuid = uuid;
            this.username = username;
        }

        public UUID getUuid() {
            return uuid;
        }

        public String getUsername() {
            return username;
        }

        public static StatisticsPlayer of(Player player) {
            return new StatisticsPlayer(player.getUniqueId(), player.getUsername());
        }
    }
}
