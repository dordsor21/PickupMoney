package com.gmail.vkhanh234.PickupMoney.Config;

import com.gmail.vkhanh234.PickupMoney.KUtils;
import com.gmail.vkhanh234.PickupMoney.PickupMoney;
import java.io.File;
import java.io.IOException;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Language {

	private FileConfiguration config;
	private File configFile = new File("plugins/PickupMoney/language.yml");
	private final PickupMoney plugin;

	public Language(PickupMoney plugin)
	{
		this.plugin = plugin;
		this.config = YamlConfiguration.loadConfiguration(this.configFile);
		try
		{
			update();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (InvalidConfigurationException e)
		{
			e.printStackTrace();
		}
	}

	@SuppressWarnings("deprecation")
	public void update()
			throws IOException, InvalidConfigurationException
	{
		if (!this.configFile.exists())
		{
			this.config.load(this.plugin.getResource("language.yml"));
			this.config.save(this.configFile);
		}
		else
		{
			FileConfiguration c = YamlConfiguration.loadConfiguration(this.plugin.getResource("language.yml"));
			for (String k : c.getKeys(true)) {
				if (!this.config.contains(k)) {
					this.config.set(k, c.get(k));
				}
			}
			this.config.save(this.configFile);
		}
	}

	public String get(String name)
	{
		return KUtils.convertColor(this.config.getString(name));
	}
}
