package com.gmail.vkhanh234.PickupMoney.Config;

import com.gmail.vkhanh234.PickupMoney.PickupMoney;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Limit {

	private FileConfiguration config;
	private File configFile;
	HashMap<String, Float> map = new HashMap<String, Float>();
	private final PickupMoney plugin;
	private long time = 0L;
	private long limitTime = 0L;
	private int limitAmount = 0;

	public Limit(PickupMoney plugin) {
		this.plugin = plugin;
		limitTime = plugin.fc.getLong("limit.time");
		limitAmount = plugin.fc.getInt("limit.amount");

		try {
			configFile = new File(plugin.getDataFolder(), "language.yml");

			if(!configFile.exists()) {
				configFile.getParentFile().mkdirs();
				plugin.saveResource("language.yml", false);
			}

			config = new YamlConfiguration();
			config.load(configFile);

			time = config.getLong("time");
			ConfigurationSection cs = config.getConfigurationSection("list");
			if (cs == null)
				return;
			for (String k : cs.getKeys(false))
				map.put(k, Float.valueOf(cs.getString(k)));

		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}

	}

	public boolean add(final String name, float amount) {
		checkTime();
		if (!map.containsKey(name))
			map.put(name, Float.valueOf(0.0F));
		final float newamount = ((Float)map.get(name)).floatValue() + amount;
		if (newamount > limitAmount)
			return false;
		map.put(name, Float.valueOf(newamount));
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			public void run() {
				config.set("list." + name, Float.valueOf(newamount));
				try {
					config.save(configFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		return true;
	}

	private void checkTime() {
		if (time + limitTime < System.currentTimeMillis())
			clear();
	}

	public void clear() {
		map.clear();
		config.createSection("list");
		updateTime();
	}

	public void clearPlayer(final String name) {
		map.put(name, Float.valueOf(0.0F));
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			public void run() {
				config.set("list." + name, Float.valueOf(0.0F));
				try {
					config.save(configFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void updateTime() {
		long t = (System.currentTimeMillis() - time) / limitTime;
		time += (t + 1L) * limitTime;
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			public void run() {
				config.set("time", Long.valueOf(time));
				try {
					config.save(configFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}
}
