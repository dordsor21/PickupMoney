package com.gmail.vkhanh234.PickupMoney.Listener;

import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent;

import com.gmail.vkhanh234.PickupMoney.KUtils;
import com.gmail.vkhanh234.PickupMoney.PickupMoney;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MythicMobsListener implements Listener {

	private final PickupMoney plugin;

	public MythicMobsListener(PickupMoney plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onMobsDeath(MythicMobDeathEvent e) {
		if (plugin.fc.getBoolean("enableEntitiesDrop")) {
			Entity entity = e.getEntity();
			String name = e.getMobType().getInternalName();
			if (plugin.entities.isOnlyKill(name) && e.getKiller() == null || !(e.getKiller() instanceof Player) || !plugin.checkWorld(entity.getLocation()))
				return;
			if ((plugin.entities.contain(name)) && (plugin.entities.getEnable(name)) && (KUtils.getSuccess(plugin.entities.getChance(name)))) {
				float bonus = plugin.entities.getLootingBonus(name);
				int looting = 0;
				if ((e.getKiller() instanceof Player))
					looting = KUtils.getPlayerLooting((Player)e.getKiller());
				for (int i = 0; i < KUtils.getRandomInt(plugin.entities.getAmount(name)); i++) {
					@SuppressWarnings("unused")
					float money = plugin.getMoneyBonus(plugin.entities.getMoney(name), bonus, looting);
					plugin.spawnMoney(e.getKiller(), plugin.getRandom(plugin.entities.getMoney(name)), entity.getLocation());
				}
				plugin.spawnParticle(entity.getLocation());
			}
		}
	}
}
