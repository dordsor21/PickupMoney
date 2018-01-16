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
	private File configFile = new File("plugins/PickupMoney/limit.yml");
	HashMap<String, Float> map = new HashMap<String, Float>();
	private final PickupMoney plugin;
	private long time = 0L;
	private long limitTime = 0L;
	private int limitAmount = 0;

	public Limit(PickupMoney plugin)
	{
		this.plugin = plugin;
		this.limitTime = plugin.fc.getLong("limit.time");
		this.limitAmount = plugin.fc.getInt("limit.amount");
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
			this.config.load(this.plugin.getResource("limit.yml"));
			this.config.save(this.configFile);
		}
	}

	public void load()
	{
		this.time = this.config.getLong("time");
		ConfigurationSection cs = this.config.getConfigurationSection("list");
		if (cs == null) {
			return;
		}
		for (String k : cs.getKeys(false)) {
			this.map.put(k, Float.valueOf(cs.getString(k)));
		}
	}

	public boolean add(final String name, float amount)
	{
		checkTime();
		if (!this.map.containsKey(name)) {
			this.map.put(name, Float.valueOf(0.0F));
		}
		final float newamount = ((Float)this.map.get(name)).floatValue() + amount;
		if (newamount > this.limitAmount) {
			return false;
		}
		this.map.put(name, Float.valueOf(newamount));
		Bukkit.getScheduler().runTaskAsynchronously(this.plugin, new Runnable()
		{
			public void run()
			{
				Limit.this.config.set("list." + name, Float.valueOf(newamount));
				try
				{
					Limit.this.config.save(Limit.this.configFile);
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		});
		return true;
	}

	private void checkTime()
	{
		if (this.time + this.limitTime < System.currentTimeMillis()) {
			clear();
		}
	}

	public void clear()
	{
		this.map.clear();
		this.config.createSection("list");
		updateTime();
	}

	public void clearPlayer(final String name)
	{
		this.map.put(name, Float.valueOf(0.0F));
		Bukkit.getScheduler().runTaskAsynchronously(this.plugin, new Runnable()
		{
			public void run()
			{
				Limit.this.config.set("list." + name, Float.valueOf(0.0F));
				try
				{
					Limit.this.config.save(Limit.this.configFile);
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	private void updateTime()
	{
		long t = (System.currentTimeMillis() - this.time) / this.limitTime;
		this.time += (t + 1L) * this.limitTime;
		Bukkit.getScheduler().runTaskAsynchronously(this.plugin, new Runnable()
		{
			public void run()
			{
				Limit.this.config.set("time", Long.valueOf(Limit.this.time));
				try
				{
					Limit.this.config.save(Limit.this.configFile);
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		});
	}
}
