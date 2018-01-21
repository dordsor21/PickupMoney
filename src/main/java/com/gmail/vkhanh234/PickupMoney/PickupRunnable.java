package com.gmail.vkhanh234.PickupMoney;

import java.util.Iterator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class PickupRunnable extends BukkitRunnable {

	private final PickupMoney plugin;
	private final double rad;

	public PickupRunnable(PickupMoney plugin) {
		this.plugin = plugin;
		this.rad = plugin.fc.getDouble("scheduleMode.radius");
	}

	public void run() {

		Player p;
		for (Iterator<? extends Player> localIterator1 = Bukkit.getOnlinePlayers().iterator(); localIterator1.hasNext();) {
			p = (Player)localIterator1.next();
			if ((!plugin.fc.getBoolean("shiftToPickUp")) || (p.isSneaking())) {
				for (Entity e : p.getNearbyEntities(rad, rad, rad)) {
					if ((e instanceof Item)) {
						Item item = (Item)e;
						if (item.getCustomName() != null) {
							String name = ChatColor.stripColor(item.getCustomName());
							String money = plugin.getMoney(name);
							if (p.hasPermission("PickupMoney.pickup")) {
								e.removeMetadata("droppedMoney", plugin);
								item.remove();
								String type = "player";
								if(item.hasMetadata("monster"))
									type = "monster";
								else if(item.hasMetadata("animal"))
									type = "animal";
								plugin.giveMoney(money, p, type);
								if (plugin.fc.getBoolean("sound.enable")) {
									p.getLocation().getWorld().playSound(p.getLocation(), Sound.valueOf(plugin.fc.getString("sound.type")), 
											(float)plugin.fc.getDouble("sound.volumn"), 
											(float)plugin.fc.getDouble("sound.pitch"));
								}
							}
						}
					}
				}
			}
		}
	}
}
