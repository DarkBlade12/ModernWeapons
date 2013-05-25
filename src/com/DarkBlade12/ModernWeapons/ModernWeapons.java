package com.DarkBlade12.ModernWeapons;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.Configuration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.DarkBlade12.ModernWeapons.Commands.ModernWeaponsCE_mw;
import com.DarkBlade12.ModernWeapons.Config.GrenadesLoader;
import com.DarkBlade12.ModernWeapons.Config.GunsLoader;
import com.DarkBlade12.ModernWeapons.Listener.WeaponListener;
import com.DarkBlade12.ModernWeapons.Util.WeaponUtil;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class ModernWeapons extends JavaPlugin {
	Logger log = Logger.getLogger("Minecraft");
	public WeaponUtil wu;
	public WeaponListener wl;
	public ModernWeaponsCE_mw mwCE = new ModernWeaponsCE_mw(this);
	public GunsLoader gl;
	public GrenadesLoader grl;
	public boolean messagesEnabled;
	public boolean headshotMessage;
	public boolean fullmagStart;
	public boolean worldLimit;
	public boolean customDeath;
	public boolean noPvpDisabled;
	public boolean disabledMessage;
	public boolean hasWorldGuard;
	public boolean blockDamage;
	public boolean autoReload;
	public boolean creativeUnlimited;
	public boolean headshotEffect;
	public String noMagazine;
	public String weaponReloaded;
	public String headshotShooter;
	public String headshotVictim;
	public String death;
	public String disabled;
	public String prefix = "§8§l[§r§3§oModern§4§oWeapons§r§8§l] §r";
	public List<String> worlds;
	public boolean knifeEnabled;
	public int knifeDamage;
	public int knifeBackstabDamage;
	public ItemStack knifeIte;

	/**
	 * Plugin Start
	 */
	public void onEnable() {
		/**
		 * Load configs
		 */

		if (!manageConfigs()) {
			return;
		}
		/**
		 * Initialize Stuff
		 */
		initializeStuff();
		/**
		 * Register stuff
		 */
		registerStuff();
		/**
		 * Output message
		 */
		log.log(Level.INFO, "[ModernWeapons] Plugin Version " + this.getDescription().getVersion() + " activated!");
	}

	/**
	 * Plugin Stop
	 */
	public void onDisable() {
		/**
		 * Output message
		 */
		log.log(Level.INFO, "[ModernWeapons] Plugin deactivated!");
	}

	/**
	 * Load default config
	 */
	public void loadConfig() {
		if (new File("plugins/ModernWeapons/config.yml").exists()) {
			log.log(Level.INFO, "[ModernWeapons] config.yml successfully loaded.");
		} else {
			saveDefaultConfig();
			log.log(Level.INFO, "[ModernWeapons] New config.yml has been created.");
		}
	}

	/**
	 * Manage configs
	 */
	public boolean manageConfigs() {
		loadConfig();
		try {
			gl = new GunsLoader(this);
		} catch (Exception e) {
			log.log(Level.WARNING, "[ModernWeapons] Error occurred while loading guns.yml! Plugin will disable!");
			e.printStackTrace();
			this.getServer().getPluginManager().disablePlugin(this);
			return false;
		}
		log.log(Level.INFO, "[ModernWeapons] guns.yml successfully loaded.");
		try {
			grl = new GrenadesLoader(this);
		} catch (Exception e) {
			log.log(Level.WARNING, "[ModernWeapons] Error occurred while loading grenades.yml! Plugin will disable!");
			this.getServer().getPluginManager().disablePlugin(this);
			return false;
		}
		log.log(Level.INFO, "[ModernWeapons] grenades.yml successfully loaded.");
		return true;
	}

	/**
	 * Initialize Stuff
	 */
	public void initializeStuff() {
		Configuration config = this.getConfig();
		// Message stuff
		this.messagesEnabled = config.getBoolean("MessageOptions.Enabled");
		this.headshotMessage = config.getBoolean("MessageOptions.HeadshotMessage");
		this.customDeath = config.getBoolean("MessageOptions.CustomDeathMessage");
		this.disabledMessage = config.getBoolean("MessageOptions.DisabledMessage");
		this.noMagazine = config.getString("Messages.NoMagazine").replace("&", "§");
		this.weaponReloaded = config.getString("Messages.WeaponReloaded").replace("&", "§");
		this.headshotShooter = config.getString("Messages.HeadshotShooter").replace("&", "§");
		this.headshotVictim = config.getString("Messages.HeadshotVictim").replace("&", "§");
		this.death = config.getString("Messages.Death").replace("&", "§");
		this.disabled = config.getString("Messages.Disabled").replace("&", "§");
		// General stuff
		this.fullmagStart = config.getBoolean("General.FullMagazineStart");
		this.worldLimit = config.getBoolean("General.WorldLimit.Enabled");
		this.noPvpDisabled = config.getBoolean("General.DisableInNonPvpAreas");
		this.blockDamage = config.getBoolean("General.BlockDamage");
		this.autoReload = config.getBoolean("General.AutoReload");
		this.creativeUnlimited = config.getBoolean("General.CreativeUnlimitedAmmo");
		this.headshotEffect = config.getBoolean("General.HeadshotEffect");
		this.worlds = config.getStringList("General.WorldLimit.Worlds");
		this.hasWorldGuard = worldGuardInstalled();
		if (this.hasWorldGuard) {
			log.log(Level.INFO, "[ModernWeapons] WorldGuard and WorldEdit have been detected!");
		}
		// Knife
		this.knifeEnabled = config.getBoolean("Knife.Enabled");
		this.knifeDamage = config.getInt("Knife.Damage");
		this.knifeBackstabDamage = config.getInt("Knife.BackstabDamage");
		this.knifeIte = new WeaponUtil(this).getItem(config.getString("Knife.Item"));
	}

	/**
	 * Check if WorldGuard is installed
	 */
	public boolean worldGuardInstalled() {
		return getWorldGuard() != null && getWorldEdit() != null;
	}

	/**
	 * Gets the WorldGuard plugin
	 */
	public WorldGuardPlugin getWorldGuard() {
		Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
		if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
			return null;
		}
		return (WorldGuardPlugin) plugin;
	}

	/**
	 * Gets the WorldEdit plugin
	 */
	public WorldEditPlugin getWorldEdit() {
		Plugin plugin = getServer().getPluginManager().getPlugin("WorldEdit");
		if (plugin == null || !(plugin instanceof WorldEditPlugin)) {
			return null;
		}
		return (WorldEditPlugin) plugin;
	}

	/**
	 * Some config methods
	 */
	public Configuration getGuns() {
		gl.load();
		return gl.getConfig();
	}

	public Configuration getGrenades() {
		grl.load();
		return grl.getConfig();
	}

	/**
	 * Register various stuff
	 */
	public void registerStuff() {
		wu = new WeaponUtil(this);
		wl = new WeaponListener(this);
		this.getCommand("mw").setExecutor(mwCE);
	}
}
