package com.gmail.vkhanh234.PickupMoney.Config;

import com.gmail.vkhanh234.PickupMoney.PickupMoney;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Entities {

	private FileConfiguration config;
	private File configFile;
	HashMap<String, EntityDat> map = new HashMap<String, EntityDat>();

	public Entities(PickupMoney plugin) {
		try {
			configFile = new File(plugin.getDataFolder(), "entities.yml");

			if(!configFile.exists()) {
				configFile.getParentFile().mkdirs();
				plugin.saveResource("entities.yml", false);
			}

			config = new YamlConfiguration();
			config.load(configFile);

			for (String k : config.getKeys(false)) {
				EntityDat e = new EntityDat();
				e.enable = config.getBoolean(k + ".enable");
				e.chance = config.getInt(k + ".chance");
				e.money = config.getString(k + ".money");
				e.amount = config.getString(k + ".amount");
				e.cost = (config.contains(k + ".cost") ? config.getBoolean(k + ".cost") : false);
				e.onlyKill = (config.contains(k + ".onlyKill") ? config.getBoolean(k + ".onlyKill") : true);
				if (config.contains(k + ".lootingBonus"))
					e.lootingBonus = Float.valueOf(config.getString(k + ".lootingBonus")).floatValue();
				else
					e.lootingBonus = Float.valueOf(plugin.fc.getString("defaultLootingBonus")).floatValue();
				map.put(k, e);
			}

		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}

	public boolean contain(String name) {
		if (map.containsKey(name)) {
			return true;
		}
		return false;
	}

	public boolean getEnable(String name) {
		return ((EntityDat)map.get(name)).enable;
	}

	public int getChance(String name) {
		return ((EntityDat)map.get(name)).chance;
	}

	public String getMoney(String name) {
		return ((EntityDat)map.get(name)).money;
	}

	public String getAmount(String name) {
		return ((EntityDat)map.get(name)).amount;
	}

	public boolean getCost(String name) {
		return ((EntityDat)map.get(name)).cost;
	}

	public float getLootingBonus(String name) {
		return ((EntityDat)map.get(name)).lootingBonus;
	}

	public boolean isOnlyKill(String name) {
		if (!map.containsKey(name))
			return false;
		return ((EntityDat)map.get(name)).onlyKill;
	}

	class EntityDat {
		boolean enable;
		boolean cost;
		boolean onlyKill;
		int chance;
		float lootingBonus;
		String money;
		String amount;

		EntityDat() {}
	}
}
