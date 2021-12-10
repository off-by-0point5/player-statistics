package com.github.offby0point5.playerstatistics;

import com.velocitypowered.api.command.SimpleCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class LogCommand implements SimpleCommand {
    private final Statistics statistics;

    public LogCommand(Statistics statistics) {
        this.statistics = statistics;
    }

    @Override
    public void execute(Invocation invocation) {
        List<Database.TimelineEvent> events;
        boolean printServer;
        boolean printPlayer;
        if (invocation.arguments().length == 0) {
            printPlayer = true;
            printServer = true;
            events = statistics.getDatabase().getTimeline(0, System.currentTimeMillis());
        } else {
            switch (invocation.arguments()[0]) {
                case "player" -> {
                    printPlayer = false;
                    printServer = true;
                    events = statistics.getDatabase().getPlayerTimeline(invocation.arguments()[1],
                            0, System.currentTimeMillis());
                }
                case "server" -> {
                    printPlayer = true;
                    printServer = false;
                    events = statistics.getDatabase().getServerTimeline(invocation.arguments()[1],
                            0, System.currentTimeMillis());
                }
                case "proxy" -> {
                    printPlayer = true;
                    printServer = false;
                    events = statistics.getDatabase().getProxyTimeline(0, System.currentTimeMillis());
                }
                default -> {
                    events = new ArrayList<>();
                    printPlayer = true;
                    printServer = true;
                }
            }
        }

        Component message = Component.text("=====> Log", NamedTextColor.GOLD);
        for (Database.TimelineEvent event : events) {
            message = message.append(Component.newline().append(getEventLine(event, printPlayer, printServer)));
        }
        invocation.source().sendMessage(message);
    }

    private Component getEventLine(Database.TimelineEvent event, boolean printPlayer, boolean printServer) {
        String formattedDateTime = DateTimeFormatter.ISO_DATE_TIME.format(
                LocalDateTime.ofEpochSecond(event.getTimestamp()/1000, 0, ZoneOffset.UTC));
        Component message = Component.text(formattedDateTime, NamedTextColor.YELLOW)
                .append(Component.text(": ", NamedTextColor.WHITE));

        if (event.isConnect()) message = message.append(Component.text("CONNECT", NamedTextColor.GREEN));
        else message = message.append(Component.text("DISCONNECT", NamedTextColor.RED));

        if (printPlayer) {
            message = message.append(Component.newline()).append(Component.text(" > ", NamedTextColor.DARK_AQUA));
            message = message.append(Component.text(event.getPlayer().getUsername() + " (..." +
                    event.getPlayer().getUuid().toString().substring(32) + ") ", NamedTextColor.AQUA));
        }

        if (printServer) {
            message = message.append(Component.newline()).append(Component.text(" > ", NamedTextColor.DARK_AQUA));
            String server = event.getServer();
            if (server != null) {
                int minus = server.indexOf("-");
                if (minus == -1) message = message.append(Component.text(server, NamedTextColor.AQUA));
                else message = message.append(Component.text(server.substring(0, minus + 5) + "...",
                        NamedTextColor.AQUA));
            } else message = message.append(Component.text("PROXY", NamedTextColor.BLUE));
        }

        return message;
    }
}
