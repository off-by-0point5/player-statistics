package com.github.offby0point5.playerstatistics;

import com.google.inject.Inject;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.slf4j.Logger;

import java.util.Optional;

@Plugin(
        id = "player-statistics",
        name = "Player Statistics",
        version = BuildConstants.VERSION,
        description = "See who was online on what server for how long",
        url = "https://github.com/off-by-0point5",
        authors = {"offby0point5"}
)
public class PlayerStatistics {

    @Inject private Logger logger;
    @Inject private ProxyServer proxy;

    private Database database;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        Statistics statistics = new Statistics(new ListDatabase());
        database = statistics.getDatabase();

        proxy.getCommandManager().register("onlineLog", new LogCommand(statistics));
        proxy.getCommandManager().register("onlineStatistics", new StatisticsCommand(statistics));
    }

    @Subscribe public void onConnectProxy(LoginEvent event) {
        database.addConnectProxy(event.getPlayer(), System.currentTimeMillis());
    }

    @Subscribe public void onDisconnectProxy(DisconnectEvent event) {
        Optional<ServerConnection> optionalServerConnection = event.getPlayer().getCurrentServer();
        optionalServerConnection.ifPresent(serverConnection -> database.addDisconnectServer(event.getPlayer(),
                serverConnection.getServerInfo().getName(), System.currentTimeMillis()));
        database.addDisconnectProxy(event.getPlayer(), System.currentTimeMillis());
    }

    @Subscribe public void onConnectServer(ServerConnectedEvent event) {
        Optional<RegisteredServer> optionalRegisteredServer = event.getPreviousServer();
        optionalRegisteredServer.ifPresent(registeredServer -> database.addDisconnectServer(event.getPlayer(),
                registeredServer.getServerInfo().getName(), System.currentTimeMillis()));
        database.addConnectServer(event.getPlayer(),
                event.getServer().getServerInfo().getName(), System.currentTimeMillis());
    }

    @Subscribe public void onDisconnectServer(KickedFromServerEvent event) {
        database.addDisconnectServer(event.getPlayer(),
                event.getServer().getServerInfo().getName(), System.currentTimeMillis());
    }
}
