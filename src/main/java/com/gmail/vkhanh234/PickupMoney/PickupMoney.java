package com.gmail.vkhanh234.PickupMoney;

import com.gmail.vkhanh234.PickupMoney.ActionBar.ActionBar;
import com.gmail.vkhanh234.PickupMoney.Config.Blocks;
import com.gmail.vkhanh234.PickupMoney.Config.Entities;
import com.gmail.vkhanh234.PickupMoney.Config.Language;
import com.gmail.vkhanh234.PickupMoney.Config.Limit;
import com.gmail.vkhanh234.PickupMoney.Listener.MainListener;
import com.gmail.vkhanh234.PickupMoney.Listener.MultiplierListener;
import com.gmail.vkhanh234.PickupMoney.Listener.MythicMobsListener;
import com.gmail.vkhanh234.PickupMoney.Listener.PickupListener;
import com.darkblade12.particleeffect.ParticleEffect;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class PickupMoney extends JavaPlugin {

	public FileConfiguration fc;
	public static Economy economy = null;
	public Entities entities;
	public Language language;
	public HashMap<UUID, Integer> dropMulti = new HashMap<UUID, Integer>();
	public HashMap<UUID, Integer> pickupMulti = new HashMap<UUID, Integer>();
	public Blocks blocks;
	public Limit limit;
	public int svVer;
	String version = getDescription().getVersion();
	ConsoleCommandSender console;
	private String prefix = "[PickupMoney] ";
	public List<UUID> spawners = new ArrayList<UUID>();
	public ActionBar actionBar;
	private String svVersion;

	public void onEnable()
	{
		this.console = getServer().getConsoleSender();
		loadConfiguration();
		initConfig();
		if (this.fc.getBoolean("notiUpdate"))
		{
			sendConsole(ChatColor.GREEN + "Current version: " + ChatColor.AQUA + this.version);
			String vers = getNewestVersion();
			if (vers != null)
			{
				sendConsole(ChatColor.GREEN + "Latest version: " + ChatColor.RED + vers);
				if (!vers.equals(this.version))
				{
					sendConsole(ChatColor.RED + "There is a new version on Spigot!");
					sendConsole(ChatColor.RED + "https://www.spigotmc.org/resources/11334/");
				}
			}
		}
		if (!setupEconomy())
		{
			getLogger().info(String.format("[%s] - Disabled due to no Vault dependency found!", new Object[] { getDescription().getName() }));
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		getServer().getPluginManager().registerEvents(new MainListener(this), this);
		getServer().getPluginManager().registerEvents(new MultiplierListener(this), this);
		if (this.fc.getBoolean("scheduleMode.enable"))
		{
			PickupRunnable runnable = new PickupRunnable(this);
			runnable.runTaskTimer(this, this.fc.getInt("scheduleMode.interval"), this.fc.getInt("scheduleMode.interval"));
		}
		getServer().getPluginManager().registerEvents(new PickupListener(this), this);
		loadMultipliers();
		try
		{
			Class.forName("net.elseland.xikage.MythicMobs.API.Bukkit.Events.MythicMobDeathEvent");
			getServer().getPluginManager().registerEvents(new MythicMobsListener(this), this);
		}
		catch (ClassNotFoundException localClassNotFoundException) {}
		String packageName = getServer().getClass().getPackage().getName();
		this.svVersion = packageName.substring(packageName.lastIndexOf('.') + 1);
		try
		{
			Class<?> clazz = Class.forName("com.gmail.vkhanh234.PickupMoney.ActionBar.ActionBar_" + this.svVersion);
			if (ActionBar.class.isAssignableFrom(clazz)) {
				this.actionBar = ((ActionBar)clazz.getConstructor(new Class[0]).newInstance(new Object[0]));
			}
		}
		catch (Exception e)
		{
			getLogger().info("Not support ActionBar in this Spigot version. Contact me if you want to add.");
		}
	}

	private void loadMultipliers()
	{
		for (Player p : getServer().getOnlinePlayers()) {
			loadMultiplier(p);
		}
	}

	public void onDisable() {}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (!sender.hasPermission("PickupMoney.command"))
		{
			sendMessage(sender, this.language.get("noPermission"));
			return true;
		}
		if (args.length >= 1) {
			try
			{
				if ((args[0].equals("reload")) && (sender.hasPermission("PickupMoney.admincmd")))
				{
					reloadConfig();
					initConfig();
					sendMessage(sender, this.language.get("reload"));
				}
				else if ((args[0].equals("drop")) && ((sender instanceof Player)) && (args.length == 2))
				{
					Player p = (Player)sender;
					float money = getRandom(args[1]);
					if (money < this.fc.getInt("minimumCmdDrop"))
					{
						sendMessage(p, this.language.get("miniumCmdDrop").replace("{money}", String.valueOf(this.fc.getInt("minimumCmdDrop"))));
						return true;
					}
					Set<Material> set = null;
					Block b = p.getTargetBlock(set, 6);
					if (costMoney(money, p)) {
						spawnMoney(p, money, b.getLocation());
					} else {
						sendMessage(p, this.language.get("noMoney"));
					}
				}
				else if ((args[0].equals("clearlimit")) && (args.length == 2) && (sender.hasPermission("PickupMoney.admincmd")))
				{
					if (args[1].equals("all")) {
						this.limit.clear();
					} else {
						this.limit.clearPlayer(args[1]);
					}
					sendMessage(sender, this.language.get("clearLimit"));
				}
				else
				{
					showHelp(sender);
				}
			}
			catch (Exception e)
			{
				showHelp(sender);
			}
		}
		showHelp(sender);

		return true;
	}

	public void loadMultiplier(Player p)
	{
		int id = 1;int ip = 1;
		for (PermissionAttachmentInfo perms : p.getEffectivePermissions())
		{
			String perm = perms.getPermission();
			if (perm.toLowerCase().startsWith("pickupmoney.multiply."))
			{
				String[] spl = perm.split("\\.");
				if (spl[3].matches("\\d+"))
				{
					int num = Integer.parseInt(spl[3]);
					if ((spl[2].equals("drop")) && (id < num)) {
						id = num;
					} else if ((spl[2].equals("pickup")) && (ip < num)) {
						ip = num;
					}
				}
			}
		}
		this.dropMulti.put(p.getUniqueId(), Integer.valueOf(id));
		this.pickupMulti.put(p.getUniqueId(), Integer.valueOf(ip));
	}

	public void sendConsole(String s)
	{
		this.console.sendMessage(this.prefix + s);
	}

	private void showHelp(CommandSender sender)
	{
		sender.sendMessage(ChatColor.RED + "PickupMoney version " + this.version);
		if (sender.hasPermission("PickupMoney.admincmd"))
		{
			sender.sendMessage(ChatColor.GREEN + "Reload - " + ChatColor.AQUA + "/pickupmoney reload");
			sender.sendMessage(ChatColor.GREEN + "Clear limit - " + ChatColor.AQUA + "/pickupmoney clearlimit <all/player_name>");
		}
		sender.sendMessage(ChatColor.GREEN + "Drop Money - " + ChatColor.AQUA + "/pickupmoney drop <amount>");
	}

	public float getMoneyOfPlayer(Player p, String val)
	{
		if (val.contains("%"))
		{
			String s = val.replace("%", "");
			int percent = KUtils.getRandomInt(s);
			return Double.valueOf(economy.getBalance(p)).floatValue() * percent / 100.0F;
		}
		return getRandom(val);
	}

	public String getMoney(String name)
	{
		String[] spl = ChatColor.stripColor(this.language.get("nameSyntax")).split("\\{money\\}");
		String t = name;
		for (String s : spl) {
			t = t.replace(s, "");
		}
		return t;
	}

	public void giveMoney(String money, Player p)
	{
		float amount = Float.parseFloat(money);
		if (this.pickupMulti.containsKey(p.getUniqueId())) {
			amount *= ((Integer)this.pickupMulti.get(p.getUniqueId())).intValue();
		}
		if ((this.fc.getBoolean("limit.enable")) && 
				(!this.limit.add(p.getName(), amount)))
		{
			sendMessage(p, this.language.get("limit"));
			return;
		}
		economy.depositPlayer(p, amount);
		sendMessage(p, this.language.get("pickup").replace("{money}", money));
	}

	public float moneyToSteal(float amount, Player p)
	{
		double bal = economy.getBalance(p);
		if (bal >= amount)
		{
			economy.withdrawPlayer(p, amount);
			return amount;
		}
		if (bal == 0.0D) {
			return 0.0F;
		}
		economy.withdrawPlayer(p, bal);
		return (float)bal;
	}

	public boolean costMoney(float amount, Player p)
	{
		if (economy.getBalance(p) >= amount)
		{
			economy.withdrawPlayer(p, amount);
			return true;
		}
		return false;
	}

	public void spawnMoney(Entity p, float money, Location l)
	{
		if ((p != null) && ((p instanceof Player)) && (this.dropMulti.containsKey(p.getUniqueId()))) {
			money *= ((Integer)this.dropMulti.get(p.getUniqueId())).intValue();
		}
		for(Entity e : l.getWorld().getNearbyEntities(l, 5, 5, 5))
			if(e instanceof Item) {
				Item i = (Item) e;
				if(i.hasMetadata("droppedMoney")){
					Float m = Float.valueOf(i.getName().replaceAll("[^0-9.]", ""));
					money = money + m;
					e.remove();
				}
			}
		Item item = l.getWorld().dropItemNaturally(l, getItem(Float.valueOf(money).floatValue()));
		String m = getStringOfMoney(money);

		item.setCustomName(this.language.get("nameSyntax").replace("{money}", m));
		item.setCustomNameVisible(true);
		item.setMetadata("droppedMoney", new FixedMetadataValue(this, true));
	}

	private String getStringOfMoney(float money)
	{
		if (this.fc.getInt("decimalPlace") == 0) {
			return String.valueOf(Math.round(money));
		}
		return String.valueOf(money);
	}

	public void spawnParticle(Location l)
	{
		if (this.fc.getBoolean("particle.enable")) {
			if ((this.svVersion.equals("v1_10_R1")) || (this.svVersion.equals("v1_11_R1"))) {
				l.getWorld().spigot().playEffect(l, Effect.getByName(this.fc.getString("particle.type")), 0, 0, 0.5F, 0.5F, 0.5F, 1.0F, this.fc.getInt("particle.amount"), 20);
			} else {
				ParticleEffect.fromName(this.fc.getString("particle.type")).display(0.5F, 0.5F, 0.5F, 1.0F, this.fc.getInt("particle.amount"), l, 20.0D);
			}
		}
	}

	public boolean checkWorld(Location location)
	{
		if (this.fc.getList("disableWorld").contains(location.getWorld().getName())) {
			return false;
		}
		return true;
	}

	public ItemStack getItem(float money)
	{
		ItemStack item;
		if (money < this.fc.getDouble("item.small.amount"))
		{
			item = new ItemStack(Material.getMaterial(this.fc.getString("item.small.type")), 1);
		}
		else
		{
			if (money < this.fc.getDouble("item.normal.amount")) {
				item = new ItemStack(Material.getMaterial(this.fc.getString("item.normal.type")), 1);
			} else {
				item = new ItemStack(Material.getMaterial(this.fc.getString("item.big.type")), 1);
			}
		}
		ItemMeta meta = item.getItemMeta();
		List<String> lore = new ArrayList<String>();
		lore.add("PickupMoney");
		lore.add("Money " + money);
		lore.add(String.valueOf(KUtils.getRandomInt(1, 100000000)));
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}

	public void loadConfiguration()
	{
		getConfig().options().copyDefaults(true);
		saveConfig();
		getConfig().options().copyDefaults(false);
	}

	private void initConfig()
	{
		this.fc = getConfig();
		this.language = new Language(this);
		this.entities = new Entities(this);
		this.blocks = new Blocks(this);
		if (this.fc.getBoolean("limit.enable")) {
			this.limit = new Limit(this);
		}
	}

	private boolean setupEconomy()
	{
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}
		return (economy != null);
	}

	public String getMessage(String type)
	{
		return KUtils.convertColor(this.fc.getString("Message." + type));
	}

	private String getNewestVersion()
	{
		try
		{
			URL url = new URL("https://dl.dropboxusercontent.com/s/a890l19kn0fv32l/PickupMoney.txt");
			URLConnection con = url.openConnection();
			con.setConnectTimeout(2000);
			con.setReadTimeout(1000);
			InputStream in = con.getInputStream();
			return getStringFromInputStream(in);
		}
		catch (IOException ex)
		{
			sendConsole(ChatColor.RED + "Failed to check for update!");
		}
		return null;
	}

	private static String getStringFromInputStream(InputStream is){
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();
		try{
			br = new BufferedReader(new InputStreamReader(is));
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			return sb.toString();
		}
		catch (IOException e){
			e.printStackTrace();
			return null;
		}finally{
			if (br != null) {
				try
				{
					br.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	public void sendMessage(CommandSender p, String s)
	{
		if ((s == null) || (s.equals(""))) {
			return;
		}
		if (this.fc.getBoolean("chatMessage")) {
			p.sendMessage(s);
		}
		if (((p instanceof Player)) && (this.actionBar != null) && (this.fc.getBoolean("actionBarMessage"))) {
			this.actionBar.send((Player)p, s);
		}
	}

	public float round(float d)
	{
		int v = this.fc.getInt("decimalPlace");
		if (v == 0) {
			return Math.round(d);
		}
		BigDecimal bd = new BigDecimal(Float.toString(d));
		bd = bd.setScale(v, 4);
		return bd.floatValue();
	}

	public float getMoneyBonus(String val, float bonus, int fortune)
	{
		float money = getRandom(val);
		if (bonus > 0.0F) {
			money += money * bonus * fortune;
		}
		return round(money);
	}

	public float getRandom(String level)
	{
		if (level.contains("-"))
		{
			String[] spl = level.split("-");

			return round(getRandomFloat(Float.parseFloat(spl[0]), Float.parseFloat(spl[1])));
		}
		return Float.parseFloat(level);
	}

	public float getRandomFloat(float minX, float maxX)
	{
		Random random = new Random();
		return (maxX - minX) * random.nextFloat() + minX;
	}
}
