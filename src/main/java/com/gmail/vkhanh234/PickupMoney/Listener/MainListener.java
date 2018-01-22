package com.gmail.vkhanh234.PickupMoney.Listener;

import com.gmail.vkhanh234.PickupMoney.KUtils;
import com.gmail.vkhanh234.PickupMoney.PickupMoney;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.ItemStack;

public class MainListener implements Listener {

	private final PickupMoney plugin;

	public MainListener(PickupMoney plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onDeath(EntityDeathEvent e) {
		if (plugin.fc.getBoolean("enableEntitiesDrop")) {
			Entity entity = e.getEntity();
			String name = entity.getType().toString();
			if (plugin.entities.isOnlyKill(name) && (e.getEntity().getKiller() == null || !(e.getEntity().getKiller() instanceof Player)) || !plugin.checkWorld(entity.getLocation()))
				return;
			if ((plugin.entities.contain(name)) && (plugin.entities.getEnable(name)) && (KUtils.getSuccess(plugin.entities.getChance(name)))) {
				if ((entity instanceof Player)) {
					Player p = (Player)entity;
					for (int i = 0; i < KUtils.getRandomInt(plugin.entities.getAmount(name)); i++) {
						float money = plugin.getMoneyOfPlayer((Player)entity, plugin.entities.getMoney(name));
						if (plugin.entities.getCost(name)) {
							money = plugin.moneyToSteal(money, p);
							plugin.sendMessage(p, plugin.language.get("dropOut").replace("{money}", String.valueOf(money)));
						}
						if (money > 0.0F)
							plugin.spawnMoney(e.getEntity().getKiller(), money, entity.getLocation(), "player");
						if(plugin.fc.getBoolean("particleSpawn.player") && plugin.fc.getBoolean("particle"))
							plugin.spawnParticle(entity.getLocation());
					}
				} else {
					int perc = 100;
					if (plugin.spawners.contains(entity.getUniqueId()))
						perc = plugin.fc.getInt("spawnerPercent");
					if (perc == 0)
						return;
					float bonus = plugin.entities.getLootingBonus(name);
					int looting = 0;
					if ((e.getEntity().getKiller() instanceof Player))
						looting = KUtils.getPlayerLooting(e.getEntity().getKiller());
					for (int i = 0; i < KUtils.getRandomInt(plugin.entities.getAmount(name)); i++) {
						float money = plugin.getMoneyBonus(plugin.entities.getMoney(name), bonus, looting);
						if (e.getEntity().getType().equals(EntityType.ZOMBIE)){
							Zombie zombie = (Zombie)e.getEntity();
							Float zMoney = plugin.round(KUtils.getMoneyFromItem(zombie.getEquipment().getItemInMainHand()));
							if (zMoney != 0.0F){
								money += zMoney;
								e.getDrops().remove(zombie.getEquipment().getItemInMainHand());
								zombie.getEquipment().setItemInMainHand(new ItemStack(Material.AIR));
							}
						}
						String type = "animal";
						if(e.getEntity() instanceof Monster)
							type = "monster";
						plugin.spawnMoney(e.getEntity().getKiller(), money * perc / 100.0F, entity.getLocation(), type);
						if(plugin.fc.getBoolean("particleSpawn." + type) && plugin.fc.getBoolean("particle"))
							plugin.spawnParticle(entity.getLocation());
					}
				}
			}
		}
	}

	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
	public void onBreak(BlockBreakEvent e) {
		if (plugin.fc.getBoolean("enableBlocksDrop")) {
			Block block = e.getBlock();
			if (!plugin.checkWorld(block.getLocation()))
				return;
			String name = block.getType().toString();
			if ((plugin.blocks.contain(name)) && (plugin.blocks.getEnable(name)) && (KUtils.getSuccess(plugin.blocks.getChance(name)))) {
				if ((!plugin.blocks.getDropBlock(name)) || 
						((e.getPlayer().getGameMode().equals(GameMode.CREATIVE)) && (!plugin.fc.getBoolean("dropBlock.creative"))) || (
								(!plugin.fc.getBoolean("dropBlock.creative")) && (hasSilkTorch(e.getPlayer())))) {
					e.setCancelled(true);
					e.getBlock().setType(Material.AIR);
				}
				float bonus = plugin.blocks.getFortuneBonus(name);
				int fortune = KUtils.getPlayerFortune(e.getPlayer());
				for (int i = 0; i < KUtils.getRandomInt(plugin.blocks.getAmount(name)); i++) {
					float money = plugin.getMoneyBonus(plugin.blocks.getMoney(name), bonus, fortune);
					plugin.spawnMoney(e.getPlayer(), money, block.getLocation(), "block");
				}
				if(plugin.fc.getBoolean("particleSpawn.block") && plugin.fc.getBoolean("particle"))
					plugin.spawnParticle(block.getLocation());
			}
		}
	}

	private boolean hasSilkTorch(Player player) {
		return player.getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.SILK_TOUCH) > 0;
	}

	@EventHandler
	public void onSpawner(CreatureSpawnEvent e) {
		if (e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER)
			plugin.spawners.add(e.getEntity().getUniqueId());
	}

	@EventHandler
	public void onHopper(InventoryPickupItemEvent e) {
		if ((e.getInventory().getType().toString().equalsIgnoreCase("hopper")) && (e.getItem().getCustomName() != null))
			e.setCancelled(true);
	}
}
