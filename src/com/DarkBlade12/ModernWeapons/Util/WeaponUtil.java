package com.DarkBlade12.ModernWeapons.Util;

import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.DarkBlade12.ModernWeapons.ModernWeapons;

public class WeaponUtil {
	ModernWeapons plugin;

	public WeaponUtil(ModernWeapons ModernWeapons) {
		plugin = ModernWeapons;
	}

	public ItemStack getItem(String istr) {
		String[] split = istr.split(",");
		int id = Integer.parseInt(split[0]);
		byte data = 0;
		if (split.length == 2) {
			data = Byte.parseByte(split[1]);
		}
		return new ItemStack(id, 1, data);
	}

	public String getWeaponName(ItemStack i) {
		if (i == null) {
			return null;
		}
		int id = i.getTypeId();
		byte data = i.getData().getData();
		// Check for guns
		Configuration guns = plugin.getGuns();
		for (String gname : guns.getKeys(false)) {
			String gstr = guns.getString(gname + ".General.Item");
			String[] split = gstr.split(",");
			int gid = Integer.parseInt(split[0]);
			byte gdata = 0;
			if (split.length == 2) {
				gdata = Byte.parseByte(split[1]);
			}
			if (id == gid && data == gdata) {
				return gname;
			}
		}
		// Check for grenades
		Configuration grenades = plugin.getGrenades();
		for (String grname : grenades.getKeys(false)) {
			String grstr = grenades.getString(grname + ".General.Item");
			String[] split = grstr.split(",");
			int grid = Integer.parseInt(split[0]);
			byte grdata = 0;
			if (split.length == 2) {
				grdata = Byte.parseByte(split[1]);
			}
			if (id == grid && data == grdata) {
				return grname;
			}
		}
		if (id == plugin.knifeIte.getTypeId() && data == plugin.knifeIte.getData().getData()) {
			return "Knife";
		}
		return null;
	}

	public boolean isGun(String weapon) {
		Configuration guns = plugin.getGuns();
		for (String gname : guns.getKeys(false)) {
			if (gname.equalsIgnoreCase(weapon)) {
				return true;
			}
		}
		return false;
	}

	public boolean isGrenade(String weapon) {
		Configuration grenades = plugin.getGrenades();
		for (String grname : grenades.getKeys(false)) {
			if (grname.equalsIgnoreCase(weapon)) {
				return true;
			}
		}
		return false;
	}

	public ItemStack rename(ItemStack i, String name) {
		ItemMeta im = i.getItemMeta();
		im.setDisplayName(name);
		i.setItemMeta(im);
		return i;
	}

	public ItemStack setLore(ItemStack i, List<String> lore) {
		if (lore.size() == 0) {
			return i;
		}
		ItemMeta im = i.getItemMeta();
		im.setLore(lore);
		i.setItemMeta(im);
		return i;
	}

	public boolean isValidEntity(Entity e) {
		EntityType et = e.getType();
		if (et == EntityType.ITEM_FRAME || et == EntityType.BOAT || et == EntityType.ARROW || et == EntityType.ENDER_CRYSTAL || et == EntityType.COMPLEX_PART || et == EntityType.EGG || et == EntityType.DROPPED_ITEM || et == EntityType.ENDER_PEARL || et == EntityType.ENDER_SIGNAL || et == EntityType.EXPERIENCE_ORB || et == EntityType.FALLING_BLOCK || et == EntityType.FIREBALL || et == EntityType.LIGHTNING || et == EntityType.MINECART || et == EntityType.PAINTING || et == EntityType.PRIMED_TNT || et == EntityType.SMALL_FIREBALL || et == EntityType.SNOWBALL || et == EntityType.SPLASH_POTION || et == EntityType.THROWN_EXP_BOTTLE || et == EntityType.UNKNOWN || et == EntityType.WEATHER || et == EntityType.WITHER_SKULL) {
			return false;
		}
		return true;
	}

	public boolean isHeadshot(Projectile p, LivingEntity e) {
		double y = p.getLocation().getY();
		double y2 = e.getEyeLocation().getY();
		double distance = Math.abs(y - y2);
		return distance <= 0.3D;
	}

	public boolean isBackstab(Player p, LivingEntity e) {
		double angle = e.getLocation().getDirection().dot(p.getLocation().getDirection());
		return angle > 0.15D;
	}

	public String getGunList() {
		Configuration guns = plugin.getGuns();
		String gstr = "";
		if (guns.getKeys(false).size() != 0) {
			for (String gun : guns.getKeys(false)) {
				gstr += "\n   §b\u2022 §7§o" + gun;
			}
		} else {
			gstr = "\n  §b\u2022 §4§oNone";
		}
		return gstr;
	}

	public String getGrenadeList() {
		Configuration grenades = plugin.getGrenades();
		String grstr = "";
		if (grenades.getKeys(false).size() != 0) {
			for (String grenade : grenades.getKeys(false)) {
				grstr += "\n   §b\u2022 §7§o" + grenade;
			}
		} else {
			grstr = "\n  §b\u2022 §4§oNone";
		}
		return grstr;
	}

	public String getWeaponByName(String name) {
		// Check for guns
		Configuration guns = plugin.getGuns();
		for (String gname : guns.getKeys(false)) {
			if (gname.equalsIgnoreCase(name)) {
				return gname;
			}
		}
		// Check for grenades
		Configuration grenades = plugin.getGrenades();
		for (String grname : grenades.getKeys(false)) {
			if (grname.equalsIgnoreCase(name)) {
				return grname;
			}
		}
		return null;
	}

	public String getWeaponInformations(String weapon) {
		Configuration config = null;
		String info = "";
		boolean gun = isGun(weapon);
		if (!gun) {
			config = plugin.getGrenades();
			info += "\n §6\u2022 §aType: §7Grenade";
		} else {
			config = plugin.getGuns();
			info += "\n §6\u2022 §aType: §7Gun";
		}
		for (Entry<String, Object> e : config.getConfigurationSection(weapon).getValues(true).entrySet()) {
			String value = String.valueOf(e.getValue()).replace("[", "").replace("]", "");
			String property = e.getKey();
			if (!value.contains("MemorySectionpath")) {
				info += "\n §6\u2022 §a" + property + ": §7" + value;
			}
		}
		return info;
	}

	public boolean hasEnoughSpace(Player p) {
		int stacks = 0;
		for (ItemStack i : p.getInventory().getContents()) {
			if (i != null) {
				if (i.getType() != Material.AIR) {
					stacks++;
				}
			}
		}
		if (stacks < 36) {
			return true;
		}
		return false;
	}
}