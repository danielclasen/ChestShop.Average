package de.danielclasen.ChestShop.Average;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;

import com.Acrobot.ChestShop.ChestShop;

public class LogReader {

	private String chestShopLogFile = "";

	private static Logger bukkitLogger = ChestShopAverage.getBukkitLogger();
	
	private static FileConfiguration config = ChestShopAverage.getPluginConfig();

	private HashMap<String, Double> buyMap;

	private HashMap<String, Double> sellMap;

	public LogReader(String chestShopLogFile) throws IOException {
		if (chestShopLogFile == null) {
			throw new IllegalArgumentException(
					"ChestShop Log File cannot be null");
		}
		this.chestShopLogFile = chestShopLogFile;

	}

	public final void readLog() {
		try {
			bukkitLogger.info("Internal Database Refresh started!");
			// Open the file that is the first
			// command line parameter
			FileInputStream fstream = new FileInputStream(this.chestShopLogFile);
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			// Read File Line By Line

			HashMap<String, Double> buy = new HashMap<String, Double>();
			HashMap<String, Double> sell = new HashMap<String, Double>();

			HashMap<String, Integer> buyIterations = new HashMap<String, Integer>();
			HashMap<String, Integer> sellIterations = new HashMap<String, Integer>();

			Date now = new Date();

			while ((strLine = br.readLine()) != null) {
				// Print the content on the console

				String transaction[] = strLine.split(" ");

				String entryDateText = transaction[0] + " " + transaction[1];

				Date entryDate;

				SimpleDateFormat sdfToDate = new SimpleDateFormat(
						"yyyy/MM/dd HH:mm:ss");
				entryDate = sdfToDate.parse(entryDateText);
			
				long duration = config.getInt("INTERNAL_DATABASE_CALCULATE_FOR_SECONDS") * 1000;

				if ((now.getTime() - entryDate.getTime()) < duration) { //Only if entry is included in calculated duration					

					String transactionType = transaction[4];

					if (transactionType.equalsIgnoreCase("bought")) {

						Integer transactionAmount = Integer
								.valueOf(transaction[5]);
						String transactionItem = transaction[6].replaceAll("_",
								" ");
						Double transactionValue = Double
								.valueOf(transaction[8]) / transactionAmount;

						buy.put(transactionItem,
								(buy.get(transactionItem) != null) ? buy
										.get(transactionItem)
										+ transactionValue : transactionValue);
						buyIterations
								.put(transactionItem,
										(int) ((buyIterations
												.get(transactionItem) != null) ? buyIterations
												.get(transactionItem) + 1 : 1));

					}

					if (transactionType.equalsIgnoreCase("sold")) {

						Integer transactionAmount = Integer
								.valueOf(transaction[5]);
						String transactionItem = transaction[6].replaceAll("_",
								" ");
						Double transactionValue = Double
								.valueOf(transaction[8]) / transactionAmount;

						sell.put(
								transactionItem,
								(sell.get(transactionItem) != null) ? sell
										.get(transactionItem)
										+ transactionValue : transactionValue);
						sellIterations
								.put(transactionItem,
										(int) ((sellIterations
												.get(transactionItem) != null) ? sellIterations
												.get(transactionItem) + 1 : 1));
					}
				}
			}
			// Close the input stream
			in.close();

			// Get a set of the entries
			Set<Entry<String, Double>> buySet = buy.entrySet();
			// Get an iterator
			Iterator<Entry<String, Double>> iBuy = buySet.iterator();
			// Display elements
			while (iBuy.hasNext()) {
				Map.Entry<String, Double> me = (Entry<String, Double>) iBuy
						.next();
				buy.put(me.getKey(),
						me.getValue() / buyIterations.get(me.getKey()));

			}
			this.buyMap = buy;

			// Get a set of the entries
			Set<Entry<String, Double>> sellSet = sell.entrySet();
			// Get an iterator
			Iterator<Entry<String, Double>> iSell = sellSet.iterator();
			// Display elements
			while (iSell.hasNext()) {
				Map.Entry<String, Double> me = (Entry<String, Double>) iSell
						.next();
				sell.put(me.getKey(),
						me.getValue() / sellIterations.get(me.getKey()));

			}
			this.sellMap = sell;

		} catch (Exception e) {// Catch exception if any
			bukkitLogger.info("Error: " + e.getMessage());
		}
	}

	public HashMap<String, Double> getAverageBuyMap() {
		return buyMap;
	}

	public HashMap<String, Double> getAverageSellMap() {
		return sellMap;
	}
}
