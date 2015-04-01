package com.DarkBlade12.ModernWeapons.Listener;

import java.util.List;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffectType;

import com.DarkBlade12.ModernWeapons.ModernWeapons;
import com.DarkBlade12.ModernWeapons.Weapons.Grenade;
import com.DarkBlade12.ModernWeapons.Weapons.Gun;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;

public class WeaponListener implements Listener {
	ModernWeapons plugin;

	public WeaponListener(ModernWeapons ModernWeapons) {
		this.plugin = ModernWeapons;
		ModernWeapons.getServer().getPluginManager().registerEvents(this, ModernWeapons);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerInteract(PlayerInteractEvent event) {
		Action action = event.getAction();
		if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK && action != Action.LEFT_CLICK_AIR && action != Action.LEFT_CLICK_BLOCK) {
			return;
		}
		Player p = event.getPlayer();
		if (this.plugin.worldLimit) {
			if (!this.plugin.worlds.contains(p.getWorld().getName())) {
				return;
			}
		}
		String weapon = plugin.wu.getWeaponName(p.getItemInHand());
		if (weapon == null) {
			return;
		}
		if (!p.hasPermission("ModernWeapons.use." + weapon) && !p.hasPermission("ModernWeapons.use.all")) {
			return;
		}
		if (plugin.noPvpDisabled) {
			if (!p.getWorld().getPVP()) {
				if (plugin.disabledMessage) {
					p.sendMessage(plugin.disabled);
				}
				return;
			} else if (plugin.hasWorldGuard) {
				RegionManager rm = plugin.getWorldGuard().getRegionManager(p.getWorld());
				ApplicableRegionSet set = rm.getApplicableRegions(p.getLocation());
				if (!set.testState(null, DefaultFlag.PVP)) {
					if (plugin.disabledMessage) {
						p.sendMessage(plugin.disabled);
					}
					return;
				}
			}
		}
		boolean aiming = false;
		if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
			aiming = true;
		}
		event.setCancelled(true);
		boolean gun = plugin.wu.isGun(weapon);
		if (aiming) {
			if (!gun) {
				return;
			}
			Gun g = new Gun(weapon, p, plugin, null);
			g.scope();
			return;
		} else {
			if (gun) {
				Gun g = new Gun(weapon, p, plugin, p.getItemInHand());
				if (p.isSneaking()) {
					g.startReloading();
					return;
				} else {
					g.shoot();
					return;
				}
			} else {
				Grenade gr = new Grenade(weapon, p, plugin);
				gr.throwGrenade();
				return;
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onProjectileHit(ProjectileHitEvent event) {
		Projectile pr = (Projectile) event.getEntity();
		Entity shooter = pr.getShooter();
		if (!(shooter instanceof Player)) {
			return;
		}
		Player p = (Player) shooter;
		if (pr.getMetadata("WeaponName").size() == 0) {
			return;
		}
		String weapon = (String) pr.getMetadata("WeaponName").get(0).value();
		Gun g = new Gun(weapon, p, plugin, null);
		if (pr.getType() != EntityType.fromName(g.getBullet())) {
			return;
		}
		if (g.willExplode()) {
			Location loc = pr.getLocation();
			loc.getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), 5.0F, false, plugin.blockDamage);
			List<Entity> elist = pr.getNearbyEntities((double) g.getExplosionRange(), (double) g.getExplosionRange(), (double) g.getExplosionRange());
			for (int t = 0; t < elist.size(); t++) {
				Entity n = elist.get(t);
				boolean damage = true;
				if (n instanceof Player) {
					if (plugin.noPvpDisabled && plugin.hasWorldGuard) {
						RegionManager rm = plugin.getWorldGuard().getRegionManager(p.getWorld());
						if (!rm.getApplicableRegions(n.getLocation()).allows(DefaultFlag.PVP)) {
							damage = false;
						}
					}
					if (((Player) n).getName().equalsIgnoreCase(p.getName()) && g.hasSelfImmunity()) {
						damage = false;
					}
				}
				if (plugin.wu.isValidEntity(n) && damage) {
					((LivingEntity) n).setMetadata("DamagerWeaponName", new FixedMetadataValue(plugin, g.getName()));
					((LivingEntity) n).damage(g.getExplosionDamage(), p);
				}
			}
		}
		g.playHitEffect(pr.getLocation());
		if (pr.getType() == EntityType.ARROW) {
			pr.remove();
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (!plugin.wu.isValidEntity(event.getEntity())) {
			return;
		}
		LivingEntity e = (LivingEntity) event.getEntity();
		if (event.getCause() != DamageCause.PROJECTILE) {
			if (plugin.knifeEnabled && event.getCause() == DamageCause.ENTITY_ATTACK && event.getDamager() instanceof Player) {
				Player d = (Player) event.getDamager();
				ItemStack h = d.getItemInHand();
				if (h.getTypeId() == plugin.knifeIte.getTypeId() && h.getData().getData() == plugin.knifeIte.getData().getData()) {
					int damage = plugin.knifeDamage;
					if (plugin.wu.isBackstab(d, e)) {
						damage = plugin.knifeBackstabDamage;
					}
					e.setMetadata("DamagerWeaponName", new FixedMetadataValue(plugin, "Knife"));
					event.setDamage(damage);
					return;
				}
			}
			return;
		}
		Projectile pr = (Projectile) event.getDamager();
		Entity shooter = pr.getShooter();
		if (!(shooter instanceof Player)) {
			return;
		}
		Player p = (Player) shooter;
		if (pr.getMetadata("WeaponName").size() == 0) {
			return;
		}
		String weapon = (String) pr.getMetadata("WeaponName").get(0).value();
		Gun g = new Gun(weapon, p, plugin, null);
		if (e instanceof Player) {
			if (plugin.noPvpDisabled && plugin.hasWorldGuard) {
				RegionManager rm = plugin.getWorldGuard().getRegionManager(p.getWorld());
				if (!rm.getApplicableRegions(e.getLocation()).allows(DefaultFlag.PVP)) {
					e.setMetadata("Headshot", new FixedMetadataValue(plugin, false));
					return;
				}
			}
			if (((Player) e).getName().equalsIgnoreCase(p.getName()) && g.hasSelfImmunity()) {
				return;
			}
		}
		int damage = g.getDamage();
		if (plugin.wu.isHeadshot(pr, e)) {
			if (e instanceof Player) {
				if (plugin.headshotMessage) {
					Player ep = (Player) e;
					p.sendMessage(plugin.headshotShooter.replace("%player%", ep.getName()));
					ep.sendMessage(plugin.headshotVictim.replace("%player%", p.getName()));
				}
				e.setMetadata("Headshot", new FixedMetadataValue(plugin, true));
			}
			if (plugin.headshotEffect) {
				e.getWorld().playEffect(e.getEyeLocation(), Effect.STEP_SOUND, 55);
			}
			damage += g.getHeadshotBonus();
		} else {
			if (e instanceof Player) {
				e.setMetadata("Headshot", new FixedMetadataValue(plugin, false));
			}
		}
		event.setDamage(damage);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		ItemStack i = event.getItem().getItemStack();
		if (!i.hasItemMeta()) {
			return;
		}
		ItemMeta im = i.getItemMeta();
		if (!im.hasDisplayName()) {
			return;
		}
		String name = im.getDisplayName();
		if (!plugin.wu.isGrenade(name)) {
			return;
		}
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getCause() != DamageCause.BLOCK_EXPLOSION) {
			return;
		}
		Entity e = event.getEntity();
		if (e.getMetadata("DamagerWeaponName").size() == 0) {
			return;
		}
		String weapon = (String) e.getMetadata("DamagerWeaponName").get(0).value();
		if (weapon.equalsIgnoreCase("None")) {
			return;
		}
		event.setCancelled(true);
		e.setMetadata("DamagerWeaponName", new FixedMetadataValue(plugin, "None"));
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerItemHeld(PlayerItemHeldEvent event) {
		Player p = event.getPlayer();
		if (p.getMetadata("Aiming").size() > 0 && (boolean) p.getMetadata("Aiming").get(0).value()) {
			p.removePotionEffect(PotionEffectType.SPEED);
			p.setMetadata("Aiming", new FixedMetadataValue(plugin, false));
		}
		int slot = event.getNewSlot();
		ItemStack i = p.getInventory().getItem(slot);
		if (i == null) {
			return;
		}
		String weapon = plugin.wu.getWeaponName(i);
		if (weapon == null) {
			return;
		}
		if (!p.hasPermission("ModernWeapons.use." + weapon) && !p.hasPermission("ModernWeapons.use.all")) {
			return;
		}
		boolean gun = plugin.wu.isGun(weapon);
		p.getWorld().playSound(p.getLocation(), Sound.BAT_TAKEOFF, 0.5F, 5);
		if (gun) {
			Gun g = new Gun(weapon, p, plugin, i);
			g.refreshItem(i);
			return;
		} else if (weapon.equalsIgnoreCase("Knife")) {
			p.getInventory().setItem(slot, plugin.wu.rename(i, "§b§oKnife"));
		} else {
			Grenade gr = new Grenade(weapon, p, plugin);
			gr.refreshItem();
			return;
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerDeath(PlayerDeathEvent event) {
		if (!plugin.customDeath) {
			return;
		}
		Player p = (Player) event.getEntity();
		if (!(p.getLastDamageCause() instanceof EntityDamageByEntityEvent)) {
			return;
		}
		EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) p.getLastDamageCause();
		if (e.getCause() == DamageCause.PROJECTILE) {
			Projectile pr = (Projectile) e.getDamager();
			if (!(pr.getShooter() instanceof Player)) {
				return;
			}
			Player k = (Player) pr.getShooter();
			if (pr.getMetadata("WeaponName").size() == 0) {
				return;
			}
			String weapon = (String) pr.getMetadata("WeaponName").get(0).value();
			String addition = "";
			if (p.getMetadata("Headshot").size() > 0) {
				boolean headshot = (boolean) p.getMetadata("Headshot").get(0).value();
				if (headshot) {
					addition = "§4\u271B";
				}
			}
			event.setDeathMessage(plugin.death.replace("%killer%", k.getName()).replace("%player%", p.getName() + addition).replace("%weapon%", weapon));
			pr.removeMetadata("WeaponName", plugin);
		} else {
			if (!(e.getDamager() instanceof Player)) {
				return;
			}
			Player k = (Player) e.getDamager();
			if (p.getMetadata("DamagerWeaponName").size() == 0) {
				return;
			}
			String weapon = (String) p.getMetadata("DamagerWeaponName").get(0).value();
			event.setDeathMessage(plugin.death.replace("%killer%", k.getName()).replace("%player%", p.getName()).replace("%weapon%", weapon));
			k.removeMetadata("WeaponName", plugin);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerEggThrow(PlayerEggThrowEvent event) {
		Projectile pr = (Projectile) event.getEgg();
		if (pr.getMetadata("WeaponName").size() == 0) {
			return;
		}
		event.setHatching(false);
	}
}
