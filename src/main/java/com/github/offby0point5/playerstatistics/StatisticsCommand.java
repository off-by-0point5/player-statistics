package com.github.offby0point5.playerstatistics;

import com.velocitypowered.api.command.SimpleCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StatisticsCommand implements SimpleCommand {
    private final Statistics statistics;

    public StatisticsCommand(Statistics statistics) {
        this.statistics = statistics;
    }

    @Override
    public void execute(Invocation invocation) {
        statistics.generateStatistics();
        Component message = Component.text("=====> Statistics", NamedTextColor.GOLD);

        if (invocation.arguments().length > 0) {
            switch (invocation.arguments()[0]) {
                case "player":
                    for (Database.StatisticsPlayer player : statistics.getUniquePlayers()) {
                        if (!(invocation.arguments().length == 1 ||
                                player.getUsername().equals(invocation.arguments()[1]))) continue;
                        message = message.append(Component.newline());
                        message = message.append(Component.text(player.getUsername()+": "));
                        for (Map.Entry<String, Long> serverPlaytime :
                                statistics.getPerServerPlaytime(player.getUuid()).entrySet()) {
                            message = message.append(Component.newline());
                            message = message.append(Component.text(" > "+getShortServerName(serverPlaytime.getKey())
                                    +": "+ formatMillisDuration(serverPlaytime.getValue())));
                        }
                    }
                    break;
                case "server":
                    for (Database.StatisticsPlayer player : statistics.getUniquePlayers()) {
                        for (Map.Entry<String, Long> serverPlaytime :
                                statistics.getPerServerPlaytime(player.getUuid()).entrySet()) {
                            if (!(invocation.arguments().length == 1 ||
                                    serverPlaytime.getKey().equals(invocation.arguments()[1]))) continue;
                            message = message.append(Component.newline());
                            message = message.append(Component.text(getShortServerName(serverPlaytime.getKey())+": "));
                            message = message.append(Component.newline());
                            message = message.append(Component.text(" > "+player.getUsername()
                                    +": "+ formatMillisDuration(serverPlaytime.getValue())));
                        }
                    }
                    break;
                case "proxy":
                    for (Database.StatisticsPlayer player : statistics.getUniquePlayers()) {
                        if (!(invocation.arguments().length == 1 ||
                                player.getUsername().equals(invocation.arguments()[1]))) continue;
                        message = message.append(Component.newline());
                        message = message.append(Component.text(player.getUsername() +
                                ": "+ formatMillisDuration(statistics.getProxyPlaytime(player.getUuid()))));
                    }
            }
        }

        invocation.source().sendMessage(message);
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        statistics.generateStatistics();
        return switch (invocation.arguments().length) {
            case 0 -> List.of("player", "server", "proxy");
            case 1 -> Stream.of("player", "server", "proxy")
                    .filter(s -> s.startsWith(invocation.arguments()[0])).collect(Collectors.toList());
            case 2 -> switch (invocation.arguments()[0]) {
                case "player" -> statistics.getUniquePlayerNames().stream()
                        .filter(s -> s.startsWith(invocation.arguments()[1])).collect(Collectors.toList());
                case "server" -> statistics.getUniqueServers().stream()
                        .filter(s -> s.startsWith(invocation.arguments()[1])).collect(Collectors.toList());
                default -> List.of();
            };
            default -> List.of();
        };
    }

    private String formatMillisDuration(long timeDuration) {
        Duration duration = Duration.of(timeDuration, ChronoUnit.MILLIS);
        return String.format("%d days %02d:%02d:%02d",
                duration.toDays(), duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart());
    }

    private String getShortServerName(String server) {
        int minus = server.indexOf("-");
        if (minus == -1) return server;
        else return server.substring(0, minus + 5) + "...";
    }
}
