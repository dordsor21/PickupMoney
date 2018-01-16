package com.gmail.vkhanh234.PickupMoney.Listener;

import com.gmail.vkhanh234.PickupMoney.KUtils;
import com.gmail.vkhanh234.PickupMoney.PickupMoney;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
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

	public MainListener(PickupMoney plugin)
	{
		this.plugin = plugin;
	}

	@EventHandler
	public void onDeath(EntityDeathEvent e)
	{
		if (this.plugin.fc.getBoolean("enableEntitiesDrop"))
		{
			Entity entity = e.getEntity();
			String name = entity.getType().toString();
			if ((this.plugin.entities.isOnlyKill(name)) && ((e.getEntity().getKiller() == null) || (!(e.getEntity().getKiller() instanceof Player)))) {
				return;
			}
			if (!this.plugin.checkWorld(entity.getLocation())) {
				return;
			}
			if ((this.plugin.entities.contain(name)) && (this.plugin.entities.getEnable(name)) && (KUtils.getSuccess(this.plugin.entities.getChance(name))))
			{
				if ((entity instanceof Player))
				{
					Player p = (Player)entity;
					for (int i = 0; i < KUtils.getRandomInt(this.plugin.entities.getAmount(name)); i++)
					{
						float money = this.plugin.getMoneyOfPlayer((Player)entity, this.plugin.entities.getMoney(name));
						if (this.plugin.entities.getCost(name))
						{
							money = this.plugin.moneyToSteal(money, p);
							this.plugin.sendMessage(p, this.plugin.language.get("dropOut").replace("{money}", String.valueOf(money)));
						}
						if (money > 0.0F) {
							this.plugin.spawnMoney(e.getEntity().getKiller(), money, entity.getLocation());
						}
					}
				}
				else
				{
					int perc = 100;
					if (this.plugin.spawners.contains(entity.getUniqueId())) {
						perc = this.plugin.fc.getInt("spawnerPercent");
					}
					if (perc == 0) {
						return;
					}
					float bonus = this.plugin.entities.getLootingBonus(name);
					int looting = 0;
					if ((e.getEntity().getKiller() instanceof Player)) {
						looting = KUtils.getPlayerLooting(e.getEntity().getKiller());
					}
					for (int i = 0; i < KUtils.getRandomInt(this.plugin.entities.getAmount(name)); i++)
					{
						float money = this.plugin.getMoneyBonus(this.plugin.entities.getMoney(name), bonus, looting);
						this.plugin.spawnMoney(e.getEntity().getKiller(), money * perc / 100.0F, entity.getLocation());
					}
				}
				this.plugin.spawnParticle(entity.getLocation());
			}
		}
		if (e.getEntity().getType().equals(EntityType.ZOMBIE))
		{
			Zombie zombie = (Zombie)e.getEntity();
			float money = KUtils.getMoneyFromItem(zombie.getEquipment().getItemInHand());
			if (money != 0.0F)
			{
				this.plugin.spawnMoney(e.getEntity().getKiller(), money, e.getEntity().getLocation());
				e.getDrops().remove(zombie.getEquipment().getItemInHand());
				zombie.getEquipment().setItemInHand(new ItemStack(Material.AIR));
			}
		}
	}

	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
	public void onBreak(BlockBreakEvent e)
	{
		if (this.plugin.fc.getBoolean("enableBlocksDrop"))
		{
			Block block = e.getBlock();
			if (!this.plugin.checkWorld(block.getLocation())) {
				return;
			}
			String name = block.getType().toString();
			if ((this.plugin.blocks.contain(name)) && (this.plugin.blocks.getEnable(name)) && (KUtils.getSuccess(this.plugin.blocks.getChance(name))))
			{
				if ((!this.plugin.blocks.getDropBlock(name)) || 
						((e.getPlayer().getGameMode().equals(GameMode.CREATIVE)) && (!this.plugin.fc.getBoolean("dropBlock.creative"))) || (
								(!this.plugin.fc.getBoolean("dropBlock.creative")) && (hasSilkTorch(e.getPlayer()))))
				{
					e.setCancelled(true);
					e.getBlock().setType(Material.AIR);
				}
				float bonus = this.plugin.blocks.getFortuneBonus(name);
				int fortune = KUtils.getPlayerFortune(e.getPlayer());
				for (int i = 0; i < KUtils.getRandomInt(this.plugin.blocks.getAmount(name)); i++)
				{
					float money = this.plugin.getMoneyBonus(this.plugin.blocks.getMoney(name), bonus, fortune);
					this.plugin.spawnMoney(e.getPlayer(), money, block.getLocation());
				}
				this.plugin.spawnParticle(block.getLocation());
			}
		}
	}

	private boolean hasSilkTorch(Player player)
	{
		return player.getItemInHand().getEnchantmentLevel(Enchantment.SILK_TOUCH) > 0;
	}

	@EventHandler
	public void onSpawner(CreatureSpawnEvent e)
	{
		if (e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER) {
			this.plugin.spawners.add(e.getEntity().getUniqueId());
		}
	}

	@EventHandler
	public void onHopper(InventoryPickupItemEvent e)
	{
		if ((e.getInventory().getType().toString().equalsIgnoreCase("hopper")) && (e.getItem().getCustomName() != null)) {
			e.setCancelled(true);
		}
	}
}
