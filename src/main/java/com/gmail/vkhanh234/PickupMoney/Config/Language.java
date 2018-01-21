package com.gmail.vkhanh234.PickupMoney.Config;

import com.gmail.vkhanh234.PickupMoney.PickupMoney;
import java.io.File;
import java.io.IOException;

import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Language {

	private FileConfiguration config;
	private File configFile;

	public Language(PickupMoney plugin) {

		try {
			configFile = new File(plugin.getDataFolder(), "language.yml");

			if(!configFile.exists()) {
				configFile.getParentFile().mkdirs();
				plugin.saveResource("language.yml", false);
			}

			config = new YamlConfiguration();
			config.load(configFile);
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}

	public String get(String name) {
		return ChatColor.translateAlternateColorCodes('&', config.getString(name));
	}
}
