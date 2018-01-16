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

	public PickupRunnable(PickupMoney plugin)
	{
		this.plugin = plugin;
		this.rad = plugin.fc.getDouble("scheduleMode.radius");
	}

	public void run()
	{

		Player p;
		for (Iterator<? extends Player> localIterator1 = Bukkit.getOnlinePlayers().iterator(); localIterator1.hasNext();)
		{
			p = (Player)localIterator1.next();
			if ((!this.plugin.fc.getBoolean("shiftToPickUp")) || (p.isSneaking())) {
				for (Entity e : p.getNearbyEntities(this.rad, this.rad, this.rad)) {
					if ((e instanceof Item))
					{
						Item item = (Item)e;
						if (item.getCustomName() != null)
						{
							String name = ChatColor.stripColor(item.getCustomName());
							String money = this.plugin.getMoney(name);
							if (p.hasPermission("PickupMoney.pickup"))
							{
								item.remove();
								this.plugin.giveMoney(money, p);
								if (this.plugin.fc.getBoolean("sound.enable")) {
									p.getLocation().getWorld().playSound(p.getLocation(), Sound.valueOf(this.plugin.fc.getString("sound.type")), 
											(float)this.plugin.fc.getDouble("sound.volumn"), 
											(float)this.plugin.fc.getDouble("sound.pitch"));
								}
							}
						}
					}
				}
			}
		}
	}
}
