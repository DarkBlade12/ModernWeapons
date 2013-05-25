package com.DarkBlade12.ModernWeapons.Weapons;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.DarkBlade12.ModernWeapons.ModernWeapons;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;

public class Grenade {
	private ItemStack grenIte;
	private List<String> lore = new ArrayList<String>();
	private boolean smoke;
	private int damage;
	private List<String> effects;
	private int range;
	private long cooldown;
	private int expDelay;
	private boolean selfImmunity;
	private String name;
	private String index;
	private Player holder;
	private String hname;
	private ModernWeapons plugin;
	private Configuration config;
	private long cooldownMillis;
	private boolean sticky;

	public Grenade(String name, Player holder, ModernWeapons ModernWeapons) {
		this.name = name;
		this.index = name;
		this.holder = holder;
		this.hname = holder.getName();
		plugin = ModernWeapons;
		this.config = plugin.getGrenades();
		initialize();
	}

	private void initialize() {
		// General
		this.grenIte = getItem(config.getString(index + ".General.Item"));
		prepareGrenadeItem();
		this.selfImmunity = config.getBoolean(index + ".General.SelfImmunity");
		for (String l : config.getStringList(index + ".General.Lore")) {
			this.lore.add(l.replace("&", "§"));
		}
		this.cooldown = config.getLong(index + ".General.Cooldown");
		// Explosion
		this.expDelay = config.getInt(index + ".Explosion.Delay");
		this.damage = config.getInt(index + ".Explosion.Damage");
		this.smoke = config.getBoolean(index + ".Explosion.Smoke");
		this.range = config.getInt(index + ".Explosion.Range");
		// Abilities
		this.effects = config.getStringList(index + ".Ability.Effects");
		this.sticky = config.getBoolean(index + ".Ability.Sticky");
		// Cooldown
		this.cooldownMillis = getCooldownMillis();
	}

	private long getCooldownMillis() {
		if (this.holder.getMetadata("WeaponCooldown." + this.name).size() > 0) {
			return (long) this.holder.getMetadata("WeaponCooldown." + this.name).get(0).value();
		}
		return System.currentTimeMillis();
	}

	private boolean hasUnlimited() {
		return this.holder.getGameMode() == GameMode.CREATIVE && plugin.creativeUnlimited;
	}

	public void throwGrenade() {
		if (this.cooldownMillis > System.currentTimeMillis()) {
			return;
		}
		this.holder.setMetadata("WeaponCooldown." + this.name, new FixedMetadataValue(plugin, System.currentTimeMillis() + this.cooldown));
		Location loc = this.holder.getLocation();
		if (!hasUnlimited()) {
			removeGrenade();
		}
		this.holder.getWorld().playSound(this.holder.getLocation(), Sound.LAVA_POP, 1, 5);
		Item ite = loc.getWorld().dropItem(this.holder.getEyeLocation(), this.grenIte);
		ite.setVelocity(loc.getDirection().multiply(1.1D));
		if (this.sticky) {
			observeStickyGrenade(ite);
		}
		observeGrenade(ite, this.expDelay);
	}

	public void observeGrenade(final Item ite, final int remDelay) {
		if (ite.isDead()) {
			return;
		}
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			@Override
			public void run() {
				int newDelay = remDelay - 1;
				if (newDelay <= 0) {
					explode(ite);
				} else {
					if (remDelay == 1) {
						ite.getWorld().playSound(ite.getLocation(), Sound.FUSE, 1, 5);
					}
					observeGrenade(ite, newDelay);
				}
			}
		}, 20);
	}

	private void removeGrenade() {
		ItemStack hand = this.holder.getItemInHand();
		int amount = hand.getAmount();
		amount--;
		if (amount == 0) {
			this.holder.setItemInHand(new ItemStack(0));
			return;
		} else {
			hand.setAmount(amount);
			this.holder.setItemInHand(hand);
			return;
		}
	}

	public void observeStickyGrenade(final Item ite) {
		if (ite.isDead()) {
			return;
		}
		final String fHname = this.hname;
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			@Override
			public void run() {
				if (!ite.isDead()) {
					boolean gotTarget = false;
					List<Entity> elist = ite.getNearbyEntities(0.2D, 0.2D, 0.2D);
					if (elist.size() >= 1) {
						Entity target = elist.get(0);
						if (target instanceof Player) {
							if (!((Player) target).getName().equals(fHname)) {
								target.setPassenger(ite);
								gotTarget = true;
							}
						} else {
							if (plugin.wu.isValidEntity(target)) {
								target.setPassenger(ite);
								gotTarget = true;
							}
						}
					}
					if (!gotTarget) {
						observeStickyGrenade(ite);
					}
				}
			}
		}, 1);
	}

	private void explode(Item ite) {
		Location loc = ite.getLocation();
		ite.remove();
		loc.getWorld().createExplosion(loc, 0.0F);
		List<Entity> elist = ite.getNearbyEntities((double) this.range, (double) this.range, (double) this.range);
		for (int t = 0; t < elist.size(); t++) {
			Entity n = elist.get(t);
			boolean damage = true;
			if (n instanceof Player) {
				if (plugin.noPvpDisabled && plugin.hasWorldGuard) {
					RegionManager rm = plugin.getWorldGuard().getRegionManager(ite.getWorld());
					if (!rm.getApplicableRegions(n.getLocation()).allows(DefaultFlag.PVP)) {
						damage = false;
					}
				}
				if (((Player) n).getName().equalsIgnoreCase(this.hname) && this.selfImmunity) {
					damage = false;
				}
			}
			if (plugin.wu.isValidEntity(n) && damage) {
				((LivingEntity) n).setMetadata("DamagerWeaponName", new FixedMetadataValue(plugin, this.name));
				if (this.damage > 0) {
					((LivingEntity) n).damage(this.damage, this.holder);
				}
				if (this.effects.size() > 0) {
					addPotionEffects(n);
				}
			}
		}
		if (this.smoke) {
			loc.getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), 5.0F, false, plugin.blockDamage);
		}
	}

	private void addPotionEffects(Entity e) {
		for (String pstr : this.effects) {
			String[] split = pstr.split(",");
			String id = split[0];
			int duration = Integer.parseInt(split[1]);
			PotionEffectType type = PotionEffectType.getByName(id.toUpperCase());
			if (type != null) {
				((LivingEntity) e).addPotionEffect(new PotionEffect(type, duration * 20, 5));
			}
		}
	}

	@SuppressWarnings("deprecation")
	public void refreshItem() {
		String name = "§c§o" + this.name;
		for (int a : getGrenadeInstances()) {
			ItemStack i = this.holder.getInventory().getItem(a);
			this.holder.getInventory().setItem(a, plugin.wu.rename(plugin.wu.setLore(i, this.lore), name));
		}
		this.holder.updateInventory();
	}

	private List<Integer> getGrenadeInstances() {
		List<Integer> slots = new ArrayList<Integer>();
		for (int i = 0; i <= 35; i++) {
			ItemStack is = this.holder.getInventory().getItem(i);
			if (is != null) {
				if (is.getTypeId() == this.grenIte.getTypeId() && is.getData().getData() == this.grenIte.getData().getData()) {
					slots.add(i);
				}
			}
		}
		return slots;
	}

	private void prepareGrenadeItem() {
		this.grenIte.setAmount(64);
		ItemMeta im = this.grenIte.getItemMeta();
		im.setDisplayName(this.name);
		this.grenIte.setItemMeta(im);
	}

	private ItemStack getItem(String istr) {
		String[] split = istr.split(",");
		int id = Integer.parseInt(split[0]);
		byte data = 0;
		if (split.length == 2) {
			data = Byte.parseByte(split[1]);
		}
		return new ItemStack(id, 1, data);
	}

	public ItemStack getGrenadeItem() {
		return this.grenIte;
	}

	public boolean willSmoke() {
		return this.smoke;
	}

	public int getDamage() {
		return this.damage;
	}

	public List<String> getEffects() {
		return this.effects;
	}

	public int getRange() {
		return this.range;
	}

	public long getCooldown() {
		return this.cooldown;
	}

	public int getExplosionDelay() {
		return this.expDelay;
	}

	public String getName() {
		return this.name;
	}

	public Player getHolder() {
		return this.holder;
	}

	public String getHolderName() {
		return this.hname;
	}

	public boolean isSticky() {
		return this.sticky;
	}

	public boolean hasSelfImmunity() {
		return this.selfImmunity;
	}
}
