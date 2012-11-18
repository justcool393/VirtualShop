package org.blockface.virtualshop;

import java.io.IOException;

import net.milkbowl.vault.economy.Economy;

import org.blockface.virtualshop.commands.Buy;
import org.blockface.virtualshop.commands.Cancel;
import org.blockface.virtualshop.commands.Find;
import org.blockface.virtualshop.commands.Help;
import org.blockface.virtualshop.commands.Sales;
import org.blockface.virtualshop.commands.Sell;
import org.blockface.virtualshop.commands.Stock;
import org.blockface.virtualshop.managers.ConfigManager;
import org.blockface.virtualshop.managers.DatabaseManager;
import org.blockface.virtualshop.util.ItemDb;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class VirtualShop extends JavaPlugin {
    
    public static Economy econ = null;
    
    public void onDisable() {
        DatabaseManager.Close();
    }

    public void onEnable() {
        if (this.setupEconomy()){
            
        } else {
            this.getLogger().severe("You must have Vault to use. Disabling...");
            this.getServer().getPluginManager().disablePlugin(this);
        }
		Chatty.Initialize(this);
        ConfigManager.Initialize(this);
        DatabaseManager.Initialize();
        try {
            ItemDb.load(this.getDataFolder(),"items.csv");
        } catch (IOException e) {
	    this.getLogger().sever("Could not load items.csv. Is it in the folder?");
            this.getPluginLoader().disablePlugin(this);
            return;
        }
    }
    
    public boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
    
    public boolean hasEnough(String playerName, double money) {
        double balance = econ.getBalance(playerName) - money;
        if (balance > 0){
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if(label.equalsIgnoreCase("sell")) Sell.Execute(sender, args, this);
        if(label.equalsIgnoreCase("buy")) Buy.Execute(sender, args, this);
        if(label.equalsIgnoreCase("cancel")) Cancel.Execute(sender, args, this);
        if(label.equalsIgnoreCase("stock")) Stock.Execute(sender, args, this);
        if(label.equalsIgnoreCase("sales")) Sales.Execute(sender, args, this);
        if(label.equalsIgnoreCase("find")) Find.Execute(sender, args, this);
        if(label.equalsIgnoreCase("vs")) Help.Execute(sender, this);
        if(label.equalsIgnoreCase("virtualshop)) Help.Execute(sender, this);
        return true;
    }
}
