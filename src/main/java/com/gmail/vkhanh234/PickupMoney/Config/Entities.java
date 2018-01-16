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
	private File configFile = new File("plugins/PickupMoney/entities.yml");
	HashMap<String, EntityDat> map = new HashMap<String, EntityDat>();
	private final PickupMoney plugin;

	public Entities(PickupMoney plugin)
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
		load();
	}

	@SuppressWarnings("deprecation")
	public void update()
			throws IOException, InvalidConfigurationException
	{
		if (!this.configFile.exists())
		{
			this.config.load(this.plugin.getResource("entities.yml"));
			this.config.save(this.configFile);
		}
	}

	public void load()
	{
		for (String k : this.config.getKeys(false))
		{
			EntityDat e = new EntityDat();
			e.enable = this.config.getBoolean(k + ".enable");
			e.chance = this.config.getInt(k + ".chance");
			e.money = this.config.getString(k + ".money");
			e.amount = this.config.getString(k + ".amount");
			e.cost = (this.config.contains(k + ".cost") ? this.config.getBoolean(k + ".cost") : false);
			e.onlyKill = (this.config.contains(k + ".onlyKill") ? this.config.getBoolean(k + ".onlyKill") : true);
			if (this.config.contains(k + ".lootingBonus")) {
				e.lootingBonus = Float.valueOf(this.config.getString(k + ".lootingBonus")).floatValue();
			} else {
				e.lootingBonus = Float.valueOf(this.plugin.fc.getString("defaultLootingBonus")).floatValue();
			}
			this.map.put(k, e);
		}
	}

	public boolean contain(String name)
	{
		if (this.map.containsKey(name)) {
			return true;
		}
		return false;
	}

	public boolean getEnable(String name)
	{
		return ((EntityDat)this.map.get(name)).enable;
	}

	public int getChance(String name)
	{
		return ((EntityDat)this.map.get(name)).chance;
	}

	public String getMoney(String name)
	{
		return ((EntityDat)this.map.get(name)).money;
	}

	public String getAmount(String name)
	{
		return ((EntityDat)this.map.get(name)).amount;
	}

	public boolean getCost(String name)
	{
		return ((EntityDat)this.map.get(name)).cost;
	}

	public float getLootingBonus(String name)
	{
		return ((EntityDat)this.map.get(name)).lootingBonus;
	}

	public boolean isOnlyKill(String name)
	{
		if (!this.map.containsKey(name)) {
			return false;
		}
		return ((EntityDat)this.map.get(name)).onlyKill;
	}

	class EntityDat
	{
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
