package org.blockface.virtualshop.commands;

import org.blockface.virtualshop.Chatty;
import org.blockface.virtualshop.VirtualShop;
import org.blockface.virtualshop.managers.DatabaseManager;
import org.blockface.virtualshop.objects.Offer;
import org.blockface.virtualshop.objects.Transaction;
import org.blockface.virtualshop.util.InventoryManager;
import org.blockface.virtualshop.util.ItemDb;
import org.blockface.virtualshop.util.Numbers;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;


public class Buy {

    public static void Execute(CommandSender sender, String[] args, VirtualShop plugin)
    {
		if(!(sender instanceof Player))
		{
			Chatty.DenyConsole(sender);
			return;
		}
        if(!sender.hasPermission("virtualshop.buy"))
        {
            Chatty.NoPermissions(sender);
            return;
        }
		if(args.length < 2)
		{
			Chatty.SendError(sender, "Proper usage is /buy <amount> <item> <maxprice (optional)>");
			return;
		}
		int amount = Numbers.ParseInteger(args[0]);
		if(amount < 0)
		{
			Chatty.NumberFormat(sender);
			return;
		}

        float maxprice = 1000000000;
        if(args.length > 2)
        {
            maxprice = Numbers.ParseFloat(args[2]);
            if(maxprice < 0)
            {
                Chatty.NumberFormat(sender);
                return;
            }
        }

		ItemStack item = ItemDb.get(args[1], 0);
		if(item==null)
		{
			Chatty.WrongItem(sender, args[1]);
			return;
		}
		Player player = (Player)sender;
        int bought = 0;
        double spent = 0;
        InventoryManager im = new InventoryManager(player);
        List<Offer> offers = DatabaseManager.GetItemOffers(item);
        if(offers.size()==0) {
            Chatty.SendError(sender,"There is no " + Chatty.FormatItem(args[1])+ " for sale.");
            return;
        }
        for(Offer o: offers)
        {
            if(o.price > maxprice) continue;
            if(o.seller.equals(player.getName())) return;
            if((amount - bought) >= o.item.getAmount())
            {
                int canbuy = o.item.getAmount();
                double cost = o.price * canbuy;

                //Revise amounts if not enough money.
                if(!plugin.hasEnough(player.getName(), cost))
                {
                    canbuy = (int)(VirtualShop.econ.getBalance(player.getName()) / o.price);
                    cost = canbuy*o.price;
                    if(canbuy < 1)
                    {
							Chatty.SendError(player,"Ran out of money!");
							break;
                    }
                }
                bought += canbuy;
                spent += cost;
                VirtualShop.econ.withdrawPlayer(player.getName(), cost);
                VirtualShop.econ.depositPlayer(o.seller, cost);
                Chatty.SendSuccess(o.seller, Chatty.FormatSeller(player.getName()) + " just bought " + Chatty.FormatAmount(canbuy) + " " + Chatty.FormatItem(args[1]) + " for " + Chatty.FormatPrice(cost));
                int left = o.item.getAmount() - canbuy;
                if(left < 1) DatabaseManager.DeleteItem(o.id);
                else DatabaseManager.UpdateQuantity(o.id, left);
                Transaction t = new Transaction(o.seller, player.getName(), o.item.getTypeId(), o.item.getDurability(), canbuy, cost);
                DatabaseManager.LogTransaction(t);
            }
            else
            {
                int canbuy = amount - bought;
                double cost = canbuy * o.price;

                //Revise amounts if not enough money.
                if(!plugin.hasEnough(player.getName(), cost))
                {
                    canbuy = (int)(VirtualShop.econ.getBalance(player.getName()) / o.price);
                    cost = canbuy*o.price;
                    if(canbuy < 1)
                    {
							Chatty.SendError(player,"Ran out of money!");
							break;
                    }
                }
                bought += canbuy;
                spent += cost;
                VirtualShop.econ.withdrawPlayer(player.getName(), cost);
                VirtualShop.econ.depositPlayer(o.seller, cost);
                Chatty.SendSuccess(o.seller, Chatty.FormatSeller(player.getName()) + " just bought " + Chatty.FormatAmount(canbuy) + " " + Chatty.FormatItem(args[1]) + " for " + Chatty.FormatPrice(cost));
                int left = o.item.getAmount() - canbuy;
                DatabaseManager.UpdateQuantity(o.id, left);
                Transaction t = new Transaction(o.seller, player.getName(), o.item.getTypeId(), o.item.getDurability(), canbuy, cost);
                DatabaseManager.LogTransaction(t);
            }
            if(bought >= amount) break;

        }

        item.setAmount(bought);
        if(bought > 0) im.addItem(item);
        Chatty.SendSuccess(player,"Managed to buy " + Chatty.FormatAmount(bought) + " " + Chatty.FormatItem(args[1]) + " for " + Chatty.FormatPrice(spent));
    }
}
