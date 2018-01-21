package com.gmail.vkhanh234.PickupMoney.Listener;

import com.gmail.vkhanh234.PickupMoney.PickupMoney;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class MultiplierListener implements Listener {

	private final PickupMoney plugin;

	public MultiplierListener(PickupMoney plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onLogin(PlayerJoinEvent e) {
		this.plugin.loadMultiplier(e.getPlayer());
	}
}
