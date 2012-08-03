package net.teamio.server.utils.cartload;

import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.util.Vector;

public class CLlistener implements Listener {
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onWorldLoad(WorldLoadEvent e) {
		Collection<Minecart> entities = e.getWorld().getEntitiesByClass(Minecart.class);
		for (Minecart minecart : entities) {
			World world = minecart.getWorld();
			Location pos = minecart.getLocation();
			
			int posX = pos.getBlockX();
			int posZ = pos.getBlockZ();
			
			int crossPosX = posX % 16; // Chunk positions (0-15)
			int crossPosZ = posZ % 16; // ^
			
			if (crossPosX <= 3) {
				world.getChunkAt(posX - 5, posZ).load();
				
				if (crossPosZ <= 3) {
					world.getChunkAt(posX - 5, posZ - 5).load();
				} else if (crossPosZ >= 12) { // 15 - 3 = 12
					world.getChunkAt(posX - 5, posZ + 5).load();
				}
			} else if (crossPosX >= 12) { // 15 - 3 = 12
				world.getChunkAt(posX + 5, posZ).load();
				
				if (crossPosZ <= 3) {
					world.getChunkAt(posX + 5, posZ - 5).load();
				} else if (crossPosZ >= 12) { // 15 - 3 = 12
					world.getChunkAt(posX + 5, posZ + 5).load();
				}
			}
			
			if (crossPosZ <= 3) {
				world.getChunkAt(posX, posZ - 5).load();
			} else if (crossPosZ >= 12) { // 15 - 3 = 12
				world.getChunkAt(posX, posZ + 5).load();
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onVehicleCreate(VehicleCreateEvent e) {
		Vehicle v = e.getVehicle();
		if (v instanceof Minecart) {
			if (CartLoad.config.getProperty("minecarts_dont_slow_down", "false").equalsIgnoreCase("true"))
				((Minecart) v).setSlowWhenEmpty(false);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onVehicleMove(VehicleMoveEvent event) {
		if (!CartLoad.config.getProperty("load_chunks_on_move", "true").equalsIgnoreCase("false")) {
			Vehicle vehicle = event.getVehicle();
			if (!(vehicle instanceof Minecart))
				return;
			Minecart minecart = (Minecart) vehicle;
			World world = minecart.getWorld();
			Location pos = minecart.getLocation();
			
			int posX = pos.getBlockX();
			int posZ = pos.getBlockZ();
			
			int crossPosX = posX % 16; // Chunk positions (0-15)
			int crossPosZ = posZ % 16; // ^
			
			if (crossPosX <= 3) {
				world.getChunkAt(posX - 5, posZ).load();
				
				if (crossPosZ <= 3) {
					world.getChunkAt(posX - 5, posZ - 5).load();
				} else if (crossPosZ >= 12) { // 15 - 3 = 12
					world.getChunkAt(posX - 5, posZ + 5).load();
				}
			} else if (crossPosX >= 12) { // 15 - 3 = 12
				world.getChunkAt(posX + 5, posZ).load();
				
				if (crossPosZ <= 3) {
					world.getChunkAt(posX + 5, posZ - 5).load();
				} else if (crossPosZ >= 12) { // 15 - 3 = 12
					world.getChunkAt(posX + 5, posZ + 5).load();
				}
			}
			
			if (crossPosZ <= 3) {
				world.getChunkAt(posX, posZ - 5).load();
			} else if (crossPosZ >= 12) { // 15 - 3 = 12
				world.getChunkAt(posX, posZ + 5).load();
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onVehicleEntityCollision(VehicleEntityCollisionEvent event) {
		boolean instanceofplayer = event.getEntity() instanceof Player;
		boolean ignoreplayers = Boolean.parseBoolean(CartLoad.config.getProperty("minecarts_ignore_players"));
		boolean ignoreentities = Boolean.parseBoolean(CartLoad.config.getProperty("minecarts_ignore_entities"));
		
		if ((instanceofplayer) && (ignoreplayers)) {
			event.setCancelled(true);
			event.setCollisionCancelled(true);
		} else if ((ignoreentities) && (!instanceofplayer)) {
			event.setCancelled(true);
			event.setCollisionCancelled(true);
		}
		
		if (((event.getEntity() instanceof Player)) && (CartLoad.config.getProperty("minecarts_run_over_players", "false").equalsIgnoreCase("true"))) {
			if (event.getVehicle().getVelocity().length() > 0.08D) {
				Vector velocity = event.getVehicle().getVelocity();
				Location location = event.getVehicle().getLocation();
				Location loc2 = event.getEntity().getLocation();
				double dx = loc2.getX() - location.getX();
				double dz = loc2.getZ() - location.getZ();
				velocity.setX(dx);
				velocity.setZ(dz);
				velocity.setY(1);
				
				event.getEntity().setVelocity(velocity);
			}
		} else if (((event.getEntity() instanceof LivingEntity)) && (!(event.getEntity() instanceof Player)) && (CartLoad.config.getProperty("minecarts_run_over_mobs", "false").equalsIgnoreCase("true")) && (event.getVehicle().getVelocity().length() > 0.08D)) {
			Vector velocity = event.getVehicle().getVelocity();
			Location location = event.getVehicle().getLocation();
			Location loc2 = event.getEntity().getLocation();
			double dx = loc2.getX() - location.getX();
			double dz = loc2.getZ() - location.getZ();
			velocity.setX(dx);
			velocity.setZ(dz);
			velocity.setY(1);
			velocity.multiply(2);
			event.getEntity().setVelocity(velocity);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onChunkUnload(ChunkUnloadEvent event) {
		if (CartLoad.config.getProperty("keep_stationary_carts_loaded", "true").equalsIgnoreCase("true")) {
			Collection<Minecart> entities = event.getWorld().getEntitiesByClass(Minecart.class);
			for (Minecart minecart : entities) {
				World world = minecart.getWorld();
				Location pos = minecart.getLocation();
				
				int posX = pos.getBlockX();
				int posZ = pos.getBlockZ();
				
				int crossPosX = posX % 16; // Chunk positions (0-15)
				int crossPosZ = posZ % 16; // ^
				
				if (crossPosX <= 3) {
					if (world.getChunkAt(posX - 5, posZ).equals(event.getChunk())) {
						event.setCancelled(true);
					}
					
					if (crossPosZ <= 3) {
						if (world.getChunkAt(posX - 5, posZ - 5).equals(event.getChunk())) {
							event.setCancelled(true);
						}
					} else if (crossPosZ >= 12) { // 15 - 3 = 12
						if (world.getChunkAt(posX - 5, posZ + 5).equals(event.getChunk())) {
							event.setCancelled(true);
						}
					}
				} else if (crossPosX >= 12) { // 15 - 3 = 12
					if (world.getChunkAt(posX + 5, posZ).equals(event.getChunk())) {
						event.setCancelled(true);
					}
					
					if (crossPosZ <= 3) {
						if (world.getChunkAt(posX + 5, posZ - 5).equals(event.getChunk())) {
							event.setCancelled(true);
						}
					} else if (crossPosZ >= 12) { // 15 - 3 = 12
						if (world.getChunkAt(posX + 5, posZ + 5).equals(event.getChunk())) {
							event.setCancelled(true);
						}
					}
				}
				
				if (crossPosZ <= 3) {
					if (world.getChunkAt(posX, posZ - 5).equals(event.getChunk())) {
						event.setCancelled(true);
					}
				} else if (crossPosZ >= 12) { // 15 - 3 = 12
					if (world.getChunkAt(posX, posZ + 5).equals(event.getChunk())) {
						event.setCancelled(true);
					}
				}
			}
		}
	}
	
}
