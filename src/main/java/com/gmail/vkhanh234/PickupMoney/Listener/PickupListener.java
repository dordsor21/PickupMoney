package com.gmail.vkhanh234.PickupMoney.Listener;

import com.gmail.vkhanh234.PickupMoney.PickupMoney;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.meta.ItemMeta;

public class PickupListener implements Listener {

	private final PickupMoney plugin;

	public PickupListener(PickupMoney plugin)
	{
		this.plugin = plugin;
	}

	@EventHandler
	public void onPickup(PlayerPickupItemEvent e)
	{
		Item item = e.getItem();
		if (item.getCustomName() != null)
		{
			ItemMeta meta = item.getItemStack().getItemMeta();
			if (!meta.hasLore()) {
				return;
			}
			if (!meta.getLore().contains("PickupMoney")) {
				return;
			}
			Player p = e.getPlayer();
			e.setCancelled(true);
			if ((this.plugin.fc.getBoolean("shiftToPickUp")) && (!p.isSneaking())) {
				return;
			}
			if (this.plugin.fc.getBoolean("scheduleMode.enable")) {
				return;
			}
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
