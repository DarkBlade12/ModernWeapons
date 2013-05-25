package com.DarkBlade12.ModernWeapons.Commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.DarkBlade12.ModernWeapons.ModernWeapons;
import com.DarkBlade12.ModernWeapons.Weapons.Grenade;
import com.DarkBlade12.ModernWeapons.Weapons.Gun;

public class ModernWeaponsCE_mw implements CommandExecutor {
	ModernWeapons plugin;

	public ModernWeaponsCE_mw(ModernWeapons ModernWeapons) {
		plugin = ModernWeapons;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("mw")) {
			if (args.length == 0) {
				sender.sendMessage(ChatColor.RED + "Invalid usage!" + "\n" + ChatColor.GOLD + "/mw help");
				return true;
			}
			if (args[0].equalsIgnoreCase("reload")) {
				if (args.length != 1) {
					sender.sendMessage(ChatColor.RED + "Invalid usage!" + "\n" + ChatColor.GOLD + "/mw reload");
					return true;
				}
				if (!sender.hasPermission("ModernWeapons.reload")) {
					sender.sendMessage(ChatColor.RED + "You don't have permission for this command!");
					return true;
				}
				plugin.reloadConfig();
				plugin.initializeStuff();
				if (sender instanceof Player) {
					sender.sendMessage("§6§o[ModernWeapons] Config has been reloaded.");
					return true;
				} else {
					sender.sendMessage("CONSOLE: §6ModernWeapons config reloaded.");
					for (Player p : Bukkit.getOnlinePlayers()) {
						if (p.hasPermission("McWeapons.reload")) {
							p.sendMessage("§7§o[CONSOLE: §6§oModernWeapons config has been reloaded.§7§o]");
						}
					}
					return true;
				}
			} else if (args[0].equalsIgnoreCase("list")) {
				if (args.length != 1) {
					sender.sendMessage(ChatColor.RED + "Invalid usage!" + "\n" + ChatColor.GOLD + "/mw list");
					return true;
				}
				if (!sender.hasPermission("ModernWeapons.list")) {
					sender.sendMessage(ChatColor.RED + "You don't have permission for this command!");
					return true;
				}
				sender.sendMessage(plugin.prefix + "§9List of weapons: \n §4\u2022 §c§lGuns:§r" + plugin.wu.getGunList() + "\n §4\u2022 §6§lGrenades:§r" + plugin.wu.getGrenadeList());
				return true;
			} else if (args[0].equalsIgnoreCase("info")) {
				if (args.length != 2) {
					sender.sendMessage(ChatColor.RED + "Invalid usage!" + "\n" + ChatColor.GOLD + "/mw info <weapon>");
					return true;
				}
				if (!sender.hasPermission("ModernWeapons.info")) {
					sender.sendMessage(ChatColor.RED + "You don't have permission for this command!");
					return true;
				}
				String weapon = plugin.wu.getWeaponByName(args[1]);
				if (weapon == null) {
					sender.sendMessage(plugin.prefix + "§cThat weapon doesn't exist!");
					return true;
				}
				sender.sendMessage(plugin.prefix + "§9Detailed information about §b" + weapon + "§9:" + plugin.wu.getWeaponInformations(weapon));
				return true;
			} else if (args[0].equalsIgnoreCase("give")) {
				if (!(sender instanceof Player)) {
					sender.sendMessage(ChatColor.RED + "Command can't be run as console!");
					return true;
				}
				Player p = (Player) sender;
				if (args.length < 2) {
					p.sendMessage(ChatColor.RED + "Invalid usage!" + "\n" + ChatColor.GOLD + "/mw give <weapon>");
					return true;
				}
				if (!sender.hasPermission("ModernWeapons.give")) {
					p.sendMessage(ChatColor.RED + "You don't have permission for this command!");
					return true;
				}
				String wstr = "";
				for (int i = 1; i <= args.length - 1; i++) {
					if (wstr.length() == 0) {
						wstr += args[i];
					} else {
						wstr += " " + args[i];
					}
				}
				String weapon = plugin.wu.getWeaponByName(wstr);
				if (weapon == null) {
					if (wstr.equalsIgnoreCase("Knife")) {
						if (!plugin.wu.hasEnoughSpace(p)) {
							p.sendMessage(plugin.prefix + "§cYou don't have enough space!");
							return true;
						}
						p.getInventory().addItem(plugin.wu.rename(plugin.knifeIte, "§b§oKnife"));
						p.sendMessage(plugin.prefix + "§eHere's your knife!");
						return true;
					}
					p.sendMessage(plugin.prefix + "§cThat weapon doesn't exist!");
					return true;
				}
				if (!plugin.wu.hasEnoughSpace(p)) {
					p.sendMessage(plugin.prefix + "§cYou don't have enough space!");
					return true;
				}
				if (plugin.wu.isGun(weapon)) {
					Gun g = new Gun(weapon, p, plugin, null);
					p.getInventory().addItem(g.getGunItem());
					g.refreshItem(g.getGunItem());
				} else {
					Grenade gr = new Grenade(weapon, p, plugin);
					p.getInventory().addItem(gr.getGrenadeItem());
					gr.refreshItem();
				}
				p.sendMessage(plugin.prefix + "§eHere's your weapon supply!");
				return true;
			} else if (args[0].equalsIgnoreCase("ammo")) {
				if (!(sender instanceof Player)) {
					sender.sendMessage(ChatColor.RED + "Command can't be run as console!");
					return true;
				}
				Player p = (Player) sender;
				if (args.length < 2) {
					p.sendMessage(ChatColor.RED + "Invalid usage!" + "\n" + ChatColor.GOLD + "/mw ammo <weapon>");
					return true;
				}
				if (!sender.hasPermission("ModernWeapons.ammo")) {
					p.sendMessage(ChatColor.RED + "You don't have permission for this command!");
					return true;
				}
				String wstr = "";
				for (int i = 1; i <= args.length - 1; i++) {
					if (wstr.length() == 0) {
						wstr += args[i];
					} else {
						wstr += " " + args[i];
					}
				}
				String weapon = plugin.wu.getWeaponByName(wstr);
				if (weapon == null) {
					p.sendMessage(plugin.prefix + "§cThat weapon doesn't exist!");
					return true;
				}
				if (!plugin.wu.isGun(weapon)) {
					p.sendMessage(plugin.prefix + "§cGrenades don't have ammo!");
					return true;
				}
				if (!plugin.wu.hasEnoughSpace(p)) {
					p.sendMessage(plugin.prefix + "§cYou don't have enough space!");
					return true;
				}
				Gun g = new Gun(weapon, p, plugin, null);
				ItemStack ammo = g.getAmmoItem();
				ammo.setAmount(64);
				p.getInventory().addItem(ammo);
				p.sendMessage(plugin.prefix + "§eHere's your ammo supply!");
				return true;
			}
		}
		return false;
	}
}
