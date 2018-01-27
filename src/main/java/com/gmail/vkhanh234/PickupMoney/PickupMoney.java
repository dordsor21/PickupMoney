package com.gmail.vkhanh234.PickupMoney;

import com.gmail.vkhanh234.PickupMoney.Config.Blocks;
import com.gmail.vkhanh234.PickupMoney.Config.Entities;
import com.gmail.vkhanh234.PickupMoney.Config.Language;
import com.gmail.vkhanh234.PickupMoney.Config.Limit;
import com.gmail.vkhanh234.PickupMoney.Listener.MainListener;
import com.gmail.vkhanh234.PickupMoney.Listener.MultiplierListener;
import com.gmail.vkhanh234.PickupMoney.Listener.MythicMobsListener;
import com.gmail.vkhanh234.PickupMoney.Listener.PickupListener;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
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
import org.bukkit.material.MaterialData;
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
	ConsoleCommandSender console;
	private String prefix = "[PickupMoney] ";
	private String ver = "2.6.0";
	public List<UUID> spawners = new ArrayList<UUID>();

	public void onEnable() {
		console = getServer().getConsoleSender();
		loadConfiguration();
		initConfig();
		if (!setupEconomy()) {
			getLogger().info(String.format("[%s] - Disabled due to no Vault dependency found!", new Object[] { getDescription().getName() }));
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		getServer().getPluginManager().registerEvents(new MainListener(this), this);
		getServer().getPluginManager().registerEvents(new MultiplierListener(this), this);
		if (fc.getBoolean("scheduleMode.enable")) {
			PickupRunnable runnable = new PickupRunnable(this);
			runnable.runTaskTimer(this, fc.getInt("scheduleMode.interval"), fc.getInt("scheduleMode.interval"));
		}
		getServer().getPluginManager().registerEvents(new PickupListener(this), this);
		loadMultipliers();
		try {
			Class.forName("net.elseland.xikage.MythicMobs.API.Bukkit.Events.MythicMobDeathEvent");
			getServer().getPluginManager().registerEvents(new MythicMobsListener(this), this);
		} catch (ClassNotFoundException localClassNotFoundException) {}
		getLogger().info("Enabled Pickup Money v" + ver);
	}

	private void loadMultipliers() {
		for (Player p : getServer().getOnlinePlayers())
			loadMultiplier(p);
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!sender.hasPermission("PickupMoney.command")) {
			sendMessage(sender, language.get("noPermission"));
			return true;
		}
		if (args.length >= 1) {
			try {
				if ((args[0].equals("reload")) && (sender.hasPermission("PickupMoney.admincmd"))) {
					reloadConfig();
					initConfig();
					sendMessage(sender, language.get("reload"));
				} else if ((args[0].equals("drop")) && ((sender instanceof Player)) && (args.length == 2)) {
					Player p = (Player)sender;
					float money = getRandom(args[1]);
					if (money < fc.getInt("minimumCmdDrop")) {
						sendMessage(p, language.get("miniumCmdDrop").replace("{money}", String.valueOf(fc.getInt("minimumCmdDrop"))));
						return true;
					}
					Set<Material> set = null;
					Block b = p.getTargetBlock(set, 6);
					if (costMoney(money, p))
						spawnMoney(p, money, b.getLocation(), "commandDropped");else
							sendMessage(p, language.get("noMoney"));
				} else if ((args[0].equals("clearlimit")) && (args.length == 2) && (sender.hasPermission("PickupMoney.admincmd"))) {
					if (args[1].equals("all"))
						limit.clear();
					else
						limit.clearPlayer(args[1]);
					sendMessage(sender, language.get("clearLimit"));
				} else {
					showHelp(sender);
				}
			}
			catch (Exception e) {
				showHelp(sender);
			}
		}
		showHelp(sender);

		return true;
	}

	public void loadMultiplier(Player p) {
		int id = 1;int ip = 1;
		for (PermissionAttachmentInfo perms : p.getEffectivePermissions()) {
			String perm = perms.getPermission();
			if (perm.toLowerCase().startsWith("pickupmoney.multiply.")) {
				String[] spl = perm.split("\\.");
				if (spl[3].matches("\\d+")) {
					int num = Integer.parseInt(spl[3]);
					if ((spl[2].equals("drop")) && (id < num))
						id = num;
					else if ((spl[2].equals("pickup")) && (ip < num))
						ip = num;
				}
			}
		}
		dropMulti.put(p.getUniqueId(), Integer.valueOf(id));
		pickupMulti.put(p.getUniqueId(), Integer.valueOf(ip));
	}

	public void sendConsole(String s) {
		console.sendMessage(prefix + s);
	}

	private void showHelp(CommandSender sender) {
		sender.sendMessage(ChatColor.RED + "PickupMoney");
		if (sender.hasPermission("PickupMoney.admincmd")) {
			sender.sendMessage(ChatColor.GREEN + "Reload - " + ChatColor.AQUA + "/pickupmoney reload");
			sender.sendMessage(ChatColor.GREEN + "Clear limit - " + ChatColor.AQUA + "/pickupmoney clearlimit <all/player_name>");
		}
		sender.sendMessage(ChatColor.GREEN + "Drop Money - " + ChatColor.AQUA + "/pickupmoney drop <amount>");
	}

	public float getMoneyOfPlayer(Player p, String val) {
		if (val.contains("%")) {
			String s = val.replace("%", "");
			int percent = KUtils.getRandomInt(s);
			return Double.valueOf(economy.getBalance(p)).floatValue() * percent / 100.0F;
		}
		return getRandom(val);
	}

	public String getMoney(String name) {
		String[] spl = ChatColor.stripColor(language.get("nameSyntax")).split("\\{money\\}");
		String t = name;
		for (String s : spl)
			t = t.replace(s, "");
		return t;
	}

	public void giveMoney(String money, Player p, String type) {
		float amount = Float.parseFloat(money);
		if (pickupMulti.containsKey(p.getUniqueId()))
			amount *= ((Integer)pickupMulti.get(p.getUniqueId())).intValue();
		if ((fc.getBoolean("limit.enable")) && (!limit.add(p.getName(), amount))) {
			sendMessage(p, language.get("limit"));
			return;
		}
		economy.depositPlayer(p, amount);
		if(fc.getBoolean("chatAnnounce." + type) && fc.getBoolean("chatMessage"))
			sendMessage(p, language.get("pickup").replace("{money}", money));
	}

	public float moneyToSteal(float amount, Player p) {
		double bal = economy.getBalance(p);
		if (bal >= amount) {
			economy.withdrawPlayer(p, amount);
			return amount;
		}
		if (bal == 0.0D)
			return 0.0F;
		economy.withdrawPlayer(p, bal);
		return (float)bal;
	}

	public boolean costMoney(float amount, Player p) {
		if (economy.getBalance(p) >= amount) {
			economy.withdrawPlayer(p, amount);
			return true;
		}
		return false;
	}

	public void spawnMoney(Entity p, float money, Location l, String type) {
		if (p != null && p instanceof Player && dropMulti.containsKey(p.getUniqueId()))
			money *= ((Integer)dropMulti.get(p.getUniqueId())).intValue();
		int r = fc.getInt("collateRadius");
		for(Entity e : l.getWorld().getNearbyEntities(l, r, r, r))
			if(e instanceof Item) {
				Item i = (Item) e;
				if(i.hasMetadata("droppedMoney")){
					Float m = Float.valueOf(ChatColor.stripColor(e.getCustomName()).replaceAll("[^0-9.]", ""));
					money = Float.sum(money, m);
					e.removeMetadata("droppedMoney", this);
					e.remove();
				}
			}
		Item item = l.getWorld().dropItemNaturally(l, getItem(Float.valueOf(money).floatValue()));
		String m = getStringOfMoney(money);

		item.setMetadata(type, new FixedMetadataValue(this, true));
		item.setCustomName(language.get("nameSyntax").replace("{money}", m));
		item.setCustomNameVisible(true);
		item.setMetadata("droppedMoney", new FixedMetadataValue(this, true));
	}

	private String getStringOfMoney(float money) {
		if (fc.getInt("decimalPlace") == 0)
			return String.valueOf(Math.round(money));
		return String.valueOf(money);
	}

	public void spawnParticle(Location l) {
		Particle p = Particle.valueOf(fc.getString("particle.type"));
		Class<?> dataType = p.getDataType();
		if(dataType == Void.class)
			l.getWorld().spawnParticle(p, l, fc.getInt("particle.amount"));
		else if(dataType == MaterialData.class)
			l.getWorld().spawnParticle(p, l, fc.getInt("particle.amount"), new MaterialData(Material.valueOf(fc.getString("particle.data"))));
		else
			l.getWorld().spawnParticle(p, l, fc.getInt("particle.amount"), new ItemStack(Material.valueOf(fc.getString("particle.data"))));
	}

	public boolean checkWorld(Location location) {
		if (fc.getList("disableWorld").contains(location.getWorld().getName()))
			return false;
		return true;
	}

	public ItemStack getItem(float money) {
		ItemStack item;
		if (money < fc.getDouble("item.small.amount")) {
			item = new ItemStack(Material.getMaterial(fc.getString("item.small.type")), 1);
		} else {
			if (money < fc.getDouble("item.normal.amount"))
				item = new ItemStack(Material.getMaterial(fc.getString("item.normal.type")), 1);
			else
				item = new ItemStack(Material.getMaterial(fc.getString("item.big.type")), 1);
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

	public void loadConfiguration() {
		getConfig().options().copyDefaults(true);
		saveConfig();
		getConfig().options().copyDefaults(false);
	}

	private void initConfig() {
		fc = getConfig();
		language = new Language(this);
		entities = new Entities(this);
		blocks = new Blocks(this);
		if (fc.getBoolean("limit.enable"))
			limit = new Limit(this);
	}

	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null)
			economy = economyProvider.getProvider();
		return (economy != null);
	}

	public String getMessage(String type) {
		return ChatColor.translateAlternateColorCodes('&', fc.getString("Message." + type));
	}

	public void sendMessage(CommandSender p, String s) {
		if ((s == null) || (s.equals("")))
			return;
		p.sendMessage(s);
	}

	public float round(float d) {
		int v = fc.getInt("decimalPlace");
		if (v == 0)
			return Math.round(d);
		BigDecimal bd = new BigDecimal(Float.toString(d));
		bd = bd.setScale(v, 4);
		return bd.floatValue();
	}

	public float getMoneyBonus(String val, float bonus, int fortune) {
		float money = getRandom(val);
		if (bonus > 0.0F)
			money += money * bonus * fortune;
		return round(money);
	}

	public float getRandom(String level) {
		if (level.contains("-")) {
			String[] spl = level.split("-");
			return round(getRandomFloat(Float.parseFloat(spl[0]), Float.parseFloat(spl[1])));
		}
		return Float.parseFloat(level);
	}

	public float getRandomFloat(float minX, float maxX) {
		Random random = new Random();
		return ((maxX - minX) * random.nextFloat()) + minX;
	}

}
