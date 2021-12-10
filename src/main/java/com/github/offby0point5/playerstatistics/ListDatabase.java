package com.github.offby0point5.playerstatistics;

import com.velocitypowered.api.proxy.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ListDatabase implements Database {
    private final List<Database.TimelineEvent> timeline;

    public ListDatabase() {
        this.timeline = new ArrayList<>();
    }

    @Override
    public void addConnectProxy(Player player, long timestamp) {
        timeline.add(new TimelineEvent(timestamp,null, StatisticsPlayer.of(player), true));
    }

    @Override
    public void addDisconnectProxy(Player player, long timestamp) {
        timeline.add(new TimelineEvent(timestamp,null, StatisticsPlayer.of(player), false));
    }

    @Override
    public void addConnectServer(Player player, String server, long timestamp) {
        timeline.add(new TimelineEvent(timestamp,server, StatisticsPlayer.of(player), true));
    }

    @Override
    public void addDisconnectServer(Player player, String server, long timestamp) {
        timeline.add(new TimelineEvent(timestamp,server, StatisticsPlayer.of(player), false));
    }

    @Override
    public List<TimelineEvent> getPlayerTimeline(StatisticsPlayer player, long oldest, long newest) {
        return getTimelineStream(oldest, newest).filter(t ->
                t.getPlayer().equals(player)).collect(Collectors.toList());
    }

    @Override
    public List<TimelineEvent> getPlayerTimeline(String player, long oldest, long newest) {
        return getTimelineStream(oldest, newest).filter(t ->
                t.getPlayer().getUsername().equals(player)).collect(Collectors.toList());
    }

    @Override
    public List<TimelineEvent> getPlayerTimeline(UUID player, long oldest, long newest) {
        return getTimelineStream(oldest, newest).filter(t ->
                t.getPlayer().getUuid().equals(player)).collect(Collectors.toList());
    }

    @Override
    public List<TimelineEvent> getServerTimeline(@NotNull String server, long oldest, long newest) {
        return getTimelineStream(oldest, newest).filter(t ->
                server.equals(t.getServer())).collect(Collectors.toList());
    }

    @Override
    public List<TimelineEvent> getProxyTimeline(long oldest, long newest) {
        return getTimelineStream(oldest, newest).filter(t ->
                t.getServer() == null).collect(Collectors.toList());
    }

    @Override
    public List<TimelineEvent> getTimeline(long oldest, long newest) {
        return getTimelineStream(oldest, newest).collect(Collectors.toList());
    }

    private Stream<TimelineEvent> getTimelineStream(long oldest, long newest) {
        return timeline.stream().filter(t -> t.getTimestamp() > oldest && t.getTimestamp() < newest);
    }
}
