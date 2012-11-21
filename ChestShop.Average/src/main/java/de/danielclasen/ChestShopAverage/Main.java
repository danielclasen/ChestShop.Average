package de.danielclasen.ChestShopAverage;

import java.io.File;
import java.text.DecimalFormat;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.Acrobot.Breeze.Utils.MaterialUtil;
import com.Acrobot.Breeze.Utils.MessageUtil;
import com.Acrobot.Breeze.Utils.StringUtil;
import com.Acrobot.ChestShop.ChestShop;
import com.Acrobot.ChestShop.Config.Language;
import com.Acrobot.ChestShop.DB.Transaction;

import com.Acrobot.ChestShop.Events.ShopCreatedEvent;
import com.Acrobot.ChestShop.Events.TransactionEvent;

//I Change!

/**
 * @author Daniel Clasen
 *
 */
public final class Main extends JavaPlugin implements Listener {
	public static File dataFolder = new File("plugins/ChestShopStats");

	
	public void onEnable(){
		getLogger().info("onEnable has been invoked!");
		getServer().getPluginManager().registerEvents(this, this);
	}
 
	public void onDisable(){
		getLogger().info("onDisable has been invoked!");
	}
	
	
    @EventHandler
    public void ChestShopPostTransaction(TransactionEvent event) {
        // Some code here
    	event.getClient().sendMessage("ChestShopPostTransaction fired");
    	
    	if (getAveragePrice(event.getItem(),"buy")>0){
    		String plural = (event.getItemAmount()>1) ? "s" : "";
    		ItemStack item = event.getItem();
    		float averagePrice = getAveragePrice(item,"buy");
    		ChatColor priceColor = (averagePrice<(event.getPrice()/event.getItemAmount())) ? ChatColor.RED : ChatColor.GREEN ;
	    	event.getClient().sendMessage("The average Price for "+event.getItemAmount()+ChatColor.GRAY+" "+MaterialUtil.getName(event.getItem())+plural+ChatColor.WHITE+" is "+priceColor+String.valueOf(averagePrice*event.getItemAmount())+ChatColor.GOLD+" Dollar");
    	}
    	else{
    		event.getClient().sendMessage("No average Price Records found for "+ChatColor.GRAY+MaterialUtil.getName(event.getItem()));
    	}
    }

    @EventHandler
    public void ChestShopPostCreate(ShopCreatedEvent event) {
        // Some code here
    	event.getPlayer().sendMessage("ChestShopPostTransaction fired");
    	ItemStack item = MaterialUtil.getItem(event.getSignLines()[3]);
    	
    	event.getPlayer().sendMessage("Line 1: "+event.getSignLines()[0]);
    	event.getPlayer().sendMessage("Line 2: "+event.getSignLines()[1]);
    	event.getPlayer().sendMessage("Line 3: "+event.getSignLines()[2]);
    	event.getPlayer().sendMessage("Line 4: "+event.getSignLines()[3]);
    	if (getAveragePrice(item,"buy")>0){
    		int itemAmount = Integer.parseInt(event.getSignLines()[1]);
    		String plural = (itemAmount>1) ? "s" : "";
    		float averagePrice = getAveragePrice(item,"buy");
    		float price = Float.parseFloat(event.getSignLines()[2].replaceAll(" ", "").replaceAll("(?i)B", "").replaceAll("(?i)S", "").split(":")[0]);
    		ChatColor priceColor = (averagePrice<(price/itemAmount)) ? ChatColor.RED : ChatColor.GREEN ;
	    	event.getPlayer().sendMessage("The average Price for "+itemAmount+ChatColor.GRAY+" "+MaterialUtil.getName(item)+plural+ChatColor.WHITE+" is "+priceColor+String.valueOf(averagePrice*itemAmount)+ChatColor.GOLD+" Dollar");
    	}
    	else{
    		event.getPlayer().sendMessage("No average Price Records found for "+ChatColor.GRAY+MaterialUtil.getName(item));
    	}
    } 
   
	
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
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
        sender.sendMessage(getNameAndID(item) + ChatColor.WHITE);
        
        try {

        	// String average = String.valueOf(getAveragePrice(item));
            sender.sendMessage("Average Price:   B "+String.valueOf(getAveragePrice(item,"buy")) +"   :   S "+String.valueOf(getAveragePrice(item,"sell")) +ChatColor.GOLD+"   Dollar "+ChatColor.WHITE+"per Piece " + ChatColor.WHITE);
            sender.sendMessage("Average Price:   B "+String.valueOf(getAveragePrice(item,"buy")*item.getMaxStackSize()) +"   :   S "+String.valueOf(getAveragePrice(item,"sell")*item.getMaxStackSize()) +ChatColor.GOLD+"   Dollar "+ChatColor.WHITE+"per Stack ("+item.getMaxStackSize()+") " + ChatColor.WHITE);
		} catch (Exception e) {
			// TODO: handle exception
			sender.sendMessage("No average Price Records for "+MaterialUtil.getName(item)+" found");
			//sender.sendMessage(""+e);
		}

//        ItemInfoEvent event = new ItemInfoEvent(sender, item);
//        ChestShop.callEvent(event);

        return true;
    }

	private static float getAveragePrice(ItemStack item, String transactionType) {
		boolean boolBuy = (transactionType=="buy")? true : false;
        float price = 0;
        int itemID = item.getType().getId();

        	List<Transaction> prices = ChestShop.getDB().find(Transaction.class).where().eq("itemID", itemID).eq("buy", boolBuy).findList();
        	
            for (Transaction t : prices) {
                price += t.getAveragePricePerItem();
            }
            
            float toReturn = price / prices.size();
            
            DecimalFormat df2 = new DecimalFormat( "#,###,###,##0.00" );
            return (!Float.isNaN(toReturn) ? Float.valueOf(df2.format(toReturn)) : 0);
            
    }

	private static String getNameAndID(ItemStack item) {
        String itemName = MaterialUtil.getName(item);

        return ChatColor.GRAY + itemName + ChatColor.WHITE + "           :" + item.getTypeId(); 
	}
	
	




}
