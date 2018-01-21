package com.gmail.vkhanh234.PickupMoney;

import java.util.Random;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class KUtils {

	public static String addSpace(String s)
	{
		return s.replace("-", " ");
	}

	public static float getMoneyFromItem(ItemStack item)
	{
		if ((item == null) || (item.getType().equals(Material.AIR)) || (!item.hasItemMeta()) || (!item.getItemMeta().hasLore())) {
			return 0.0F;
		}
		ItemMeta meta = item.getItemMeta();
		String lore = (String)meta.getLore().get(1);
		if (lore.startsWith("Money ")) {
			return Float.valueOf(lore.substring(5)).floatValue();
		}
		return 0.0F;
	}

	public static int getPlayerFortune(Player p)
	{
		return p.getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
	}

	public static int getPlayerLooting(Player p)
	{
		return p.getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS);
	}

	public static int getRandomInt(String level)
	{
		if (level.contains("-"))
		{
			String[] spl = level.split("-");
			return getRandomInt(Integer.parseInt(spl[0]), Integer.parseInt(spl[1]));
		}
		return Integer.parseInt(level);
	}

	public static float randomNumber(int min, int max)
	{
		Random random = new Random();
		@SuppressWarnings("unused")
		float number = random.nextFloat() * (max - min) + min;
		return random.nextFloat() * (max - min) + min;
	}

	public static int getRandomInt(int min, int max)
	{
		Random random = new Random();
		return random.nextInt(max - min + 1) + min;
	}

	public static boolean getSuccess(int percent)
	{
		int i = getRandomInt(1, 100);
		return i <= percent;
	}

	public static boolean hasPermmision(Player p, String perm)
	{
		if (p.hasPermission(perm)) {
			return true;
		}
		return p.isOp();
	}
}
