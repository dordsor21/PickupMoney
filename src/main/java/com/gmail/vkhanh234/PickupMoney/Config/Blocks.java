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
	private File configFile = new File("plugins/PickupMoney/blocks.yml");
	HashMap<String, BlockDat> map = new HashMap<String, BlockDat>();
	private final PickupMoney plugin;

	public Blocks(PickupMoney plugin)
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
			this.config.load(this.plugin.getResource("blocks.yml"));
			this.config.save(this.configFile);
		}
	}

	public void load()
	{
		for (String k : this.config.getKeys(false))
		{
			BlockDat e = new BlockDat();
			e.enable = this.config.getBoolean(k + ".enable");
			e.chance = this.config.getInt(k + ".chance");
			e.money = this.config.getString(k + ".money");
			e.amount = this.config.getString(k + ".amount");
			e.dropBlock = this.config.getBoolean(k + ".dropBlock");
			if (this.config.contains(k + ".fortuneBonus")) {
				e.fortuneBonus = Float.valueOf(this.config.getString(k + ".fortuneBonus")).floatValue();
			} else {
				e.fortuneBonus = Float.valueOf(this.plugin.fc.getString("defaultFortuneBonus")).floatValue();
			}
			this.map.put(k, e);
		}
	}

	public boolean contain(String name)
	{
		return this.map.containsKey(name);
	}

	public boolean getDropBlock(String name)
	{
		return ((BlockDat)this.map.get(name)).dropBlock;
	}

	public boolean getEnable(String name)
	{
		return ((BlockDat)this.map.get(name)).enable;
	}

	public int getChance(String name)
	{
		return ((BlockDat)this.map.get(name)).chance;
	}

	public String getMoney(String name)
	{
		return ((BlockDat)this.map.get(name)).money;
	}

	public String getAmount(String name)
	{
		return ((BlockDat)this.map.get(name)).amount;
	}

	public BlockDat getBlockDat(String name)
	{
		return (BlockDat)this.map.get(name);
	}

	public float getFortuneBonus(String name)
	{
		return ((BlockDat)this.map.get(name)).fortuneBonus;
	}

	class BlockDat
	{
		boolean enable;
		boolean dropBlock;
		int chance;
		float fortuneBonus;
		String money;
		String amount;

		BlockDat() {}
	}
}
