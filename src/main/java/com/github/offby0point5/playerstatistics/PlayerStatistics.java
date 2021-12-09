package com.github.offby0point5.playerstatistics;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import org.slf4j.Logger;

@Plugin(
        id = "player-statistics",
        name = "Player Statistics",
        version = BuildConstants.VERSION,
        description = "See who was online on what server for how long",
        url = "https://github.com/off-by-0point5",
        authors = {"offby0point5"}
)
public class PlayerStatistics {

    @Inject
    private Logger logger;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
    }
}
