package de.danielclasen.ChestShop.Average;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.Acrobot.Breeze.Utils.MaterialUtil;
import com.Acrobot.Breeze.Utils.MessageUtil;
import com.Acrobot.Breeze.Utils.StringUtil;
import com.Acrobot.ChestShop.Config.Language;
import com.iCo6.Constants;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * @author Daniel Clasen
 * 
 */
public final class ChestShopAverage extends JavaPlugin implements Listener {
	public static File dataFolder;

	private static Server server;
	private static Logger logger;
	private static PluginManager pluginManager;
	private static List<String> currencyMajor;
	private static List<String> currencyMinor;
	private static LogReader internalDatabase;
	private static ChestShopAverage plugin;

	private static File configFile;
	private static FileConfiguration config;

	public void onEnable() {
		configFile = new File(getDataFolder(), "config.yml");

		try {
			firstRun();
		} catch (Exception e) {
			e.printStackTrace();
		}

		config = new YamlConfiguration();
		loadYamls();

		plugin = this;
		logger = getLogger();
		pluginManager = getServer().getPluginManager();
		dataFolder = getDataFolder();
		server = getServer();


		pluginManager.registerEvents(this, this);
		currencyMajor = Constants.Nodes.Major.getStringList();
		currencyMinor = Constants.Nodes.Minor.getStringList();

		try {
			internalDatabase = new LogReader("plugins/ChestShop/ChestShop.log");
			this.initInternalDB();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void onDisable() {
		saveYamls();
	}

	private void firstRun() throws Exception {
		if (!configFile.exists()) {
			configFile.getParentFile().mkdirs();
			copy(getResource("config.yml"), configFile);
		}
	}

	private void copy(InputStream in, File file) {
		try {
			OutputStream out = new FileOutputStream(file);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			out.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initInternalDB() {

		Bukkit.getScheduler().scheduleSyncRepeatingTask(
				plugin,
				new Runnable() {

					public void run() {

						internalDatabase.readLog();
						getLogger()
								.info("Internal Database Refresh finished!");
					}
				},
				Long.valueOf(config.getInt("INTERNAL_DATABASE_INITIALDELAY") * 20),
				Long.valueOf(config.getInt("INTERNAL_DATABASE_REFRESH") * 20));

	}

	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		// test();

		ItemStack item;

		if (args.length == 0) {
			if (!(sender instanceof HumanEntity)) {
				return false;
			}

			item = ((HumanEntity) sender).getItemInHand();
		} else {
			item = MaterialUtil.getItem(StringUtil.joinArray(args));
		}

		if (MaterialUtil.isEmpty(item)) {
			return false;
		}

		MessageUtil.sendMessage(sender, Language.iteminfo);
		sender.sendMessage(getNameAndID(item) + ChatColor.WHITE
				+ getDurability(item) + getEnchantment(item));

		try {
			float averageSell = getAveragePrice(item, "sell");
			float averageBuy = getAveragePrice(item, "buy");
			if (averageSell != 0 || averageBuy != 0) {
				sender.sendMessage("Average Price:   B "
						+ formatCurrency(averageBuy) + "   :   S "
						+ formatCurrency(averageSell) + "per Piece "
						+ ChatColor.WHITE);
				sender.sendMessage("Average Price:   B "
						+ formatCurrency(averageBuy * item.getMaxStackSize())
						+ "   :   S "
						+ formatCurrency(averageSell * item.getMaxStackSize())
						+ "per Stack (" + item.getMaxStackSize() + ") "
						+ ChatColor.WHITE);
			} else {
				printNoRecords(sender, item);
			}
		} catch (Exception e) {
			// TODO: handle exception
			printNoRecords(sender, item);
		}

		return true;
	}

	private void printNoRecords(CommandSender sender, ItemStack item) {
		sender.sendMessage("No average Price Records for "
				+ MaterialUtil.getName(item) + " found");

	}

	private static String formatCurrency(float price) {
		if (price > 1)
			return String.valueOf(price) + " " + ChatColor.GOLD
					+ currencyMajor.get(0) + " " + ChatColor.WHITE;
		else
			return String.valueOf(price) + " " + ChatColor.GOLD
					+ currencyMajor.get(1) + " " + ChatColor.WHITE;

	}

	private static float getAveragePrice(ItemStack item, String transactionType) {

		float toReturn;
		double toReturnDouble = 0;

		if (transactionType == "buy") {
			toReturnDouble = internalDatabase.getAverageBuyMap().get(
					MaterialUtil.getName(item, false).toUpperCase()
							+ getEnchantment(item).replaceAll("§3", ""));
			// TODO:
			// Add
			// enchantment
			// care of case
			// sensitive

		} else if (transactionType == "sell") {
			toReturnDouble = internalDatabase.getAverageSellMap().get(
					MaterialUtil.getName(item, false).toUpperCase()
							+ getEnchantment(item).replaceAll("§3", ""));
			// TODO:
			// Add
			// enchantment
			// care of case
			// sensitive

		}

		toReturn = (float) toReturnDouble;

		DecimalFormat df2 = new DecimalFormat("#,###,###,##0.00");
		return (!Float.isNaN(toReturn) ? Float.valueOf(df2.format(toReturn))
				: 0);

	}

	// private static float getAveragePrice(ItemStack item, String
	// transactionType) {
	// boolean boolBuy = (transactionType == "buy") ? true : false;
	// float price = 0;
	// int itemID = item.getType().getId();
	//
	// List<Transaction> prices = ChestShop.getDB().find(Transaction.class)
	// .where().eq("itemID", itemID).eq("buy", boolBuy).findList();
	//
	// for (Transaction t : prices) {
	// price += t.getAveragePricePerItem();
	// }
	//
	// float toReturn = price / prices.size();
	//
	// DecimalFormat df2 = new DecimalFormat("#,###,###,##0.00");
	// return (!Float.isNaN(toReturn) ? Float.valueOf(df2.format(toReturn))
	// : 0);
	//
	// }

	private static String getNameAndID(ItemStack item) {
		String itemName = MaterialUtil.getName(item);

		return ChatColor.GRAY + itemName + ChatColor.WHITE + "           :"
				+ item.getTypeId();
	}

	private static String getDurability(ItemStack item) {
		if (item.getDurability() != 0) {
			return ChatColor.DARK_GREEN + ":"
					+ Integer.toString(item.getDurability());
		} else {
			return "";
		}
	}

	private static String getEnchantment(ItemStack item) {
		String encodedEnchantments = MaterialUtil.Enchantment
				.encodeEnchantment(item);

		if (encodedEnchantments != null) {
			return ChatColor.DARK_AQUA + "-" + encodedEnchantments;
		} else {
			return "";
		}
	}

	public final static Logger getBukkitLogger() {
		return logger;
	}

	public final static FileConfiguration getPluginConfig() {
		return config;
	}

	public void saveYamls() {
		try {
			config.save(configFile);
			// Add more?
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void loadYamls() {
		try {
			config.load(configFile);
			// Add more?
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
