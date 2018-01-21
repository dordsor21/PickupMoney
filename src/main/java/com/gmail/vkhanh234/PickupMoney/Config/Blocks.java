package com.gmail.vkhanh234.PickupMoney.Config;

import com.gmail.vkhanh234.PickupMoney.PickupMoney;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Blocks {

	private FileConfiguration config;
	private File configFile;
	HashMap<String, BlockDat> map = new HashMap<String, BlockDat>();

	public Blocks(PickupMoney plugin) {

		try {
			configFile = new File(plugin.getDataFolder(), "blocks.yml");

			if(!configFile.exists()) {
				configFile.getParentFile().mkdirs();
				plugin.saveResource("blocks.yml", false);
			}

			config = new YamlConfiguration();
			config.load(configFile);

			for (String k : config.getKeys(false)) {
				BlockDat e = new BlockDat();
				e.enable = config.getBoolean(k + ".enable");
				e.chance = config.getInt(k + ".chance");
				e.money = config.getString(k + ".money");
				e.amount = config.getString(k + ".amount");
				e.dropBlock = config.getBoolean(k + ".dropBlock");
				if (config.contains(k + ".fortuneBonus"))
					e.fortuneBonus = Float.valueOf(config.getString(k + ".fortuneBonus")).floatValue();
				else
					e.fortuneBonus = Float.valueOf(plugin.fc.getString("defaultFortuneBonus")).floatValue();
				map.put(k, e);
			}

		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}

	}

	public boolean contain(String name) {
		return map.containsKey(name);
	}

	public boolean getDropBlock(String name) {
		return ((BlockDat)map.get(name)).dropBlock;
	}

	public boolean getEnable(String name) {
		return ((BlockDat)map.get(name)).enable;
	}

	public int getChance(String name) {
		return ((BlockDat)map.get(name)).chance;
	}

	public String getMoney(String name)
	{
		return ((BlockDat)map.get(name)).money;
	}

	public String getAmount(String name) {
		return ((BlockDat)map.get(name)).amount;
	}

	public BlockDat getBlockDat(String name) {
		return (BlockDat)map.get(name);
	}

	public float getFortuneBonus(String name) {
		return ((BlockDat)map.get(name)).fortuneBonus;
	}

	class BlockDat {
		boolean enable;
		boolean dropBlock;
		int chance;
		float fortuneBonus;
		String money;
		String amount;

		BlockDat() {}
	}
}
