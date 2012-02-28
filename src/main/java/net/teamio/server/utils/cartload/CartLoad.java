package net.teamio.server.utils.cartload;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class CartLoad extends JavaPlugin {

	public static Properties config = new Properties();
	public static File configFile = new File("CartLoad.config");
	public static Properties oldconfig = new Properties();

	Logger log = Logger.getLogger("Minecraft");

	public void onEnable() {
		ConfigLoad();

		this.log.info("CartLoad v" + getDescription().getVersion()
				+ " has been loaded.");

		PluginManager pm = getServer().getPluginManager();

		pm.registerEvents(new CLlistener(), this);

		if (!config.getProperty("load_all_carts_on_startup").equalsIgnoreCase(
				"false")) {
			List<World> worlds = getServer().getWorlds();

			for (World world : worlds) {
				List<Entity> entities = world.getEntities();
				for (Entity entity : entities){
					if ((entity instanceof Minecart)) {
						int x = entity.getLocation().getBlockX();
						int z = entity.getLocation().getBlockZ();
						int radius = Math.abs(Integer.parseInt(config
								.getProperty("radius_of_loaded_chunks", "3")));

						for (int i = -radius; i <= radius; i++) {
							for (int j = -radius; j <= radius; j++) {
								if (world.isChunkLoaded(x + i, j + z))
									continue;
								world.loadChunk(x + i, z + j);
							}
						}
					}
				}
			}
		}
	}
	
	
	/* (non-Javadoc)
	 * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		if (command.getName().equalsIgnoreCase("countminecarts")){
			ArrayList<Minecart> mc = new ArrayList<Minecart>();
			for (World w : getServer().getWorlds()){
				mc.addAll(w.getEntitiesByClass(Minecart.class));
			}
			sender.sendMessage(ChatColor.YELLOW+"[CartLoad] There are "+mc.size()+" minecarts on the server.");
			return true;
		}
		return false;
	}

	private void ConfigLoad() {
		if (!configFile.exists()) {
			ConfigCreate(false);
			this.log.info("[CartLoad] Configuration file does not exist! Creating one now...");
		} else {
			try {
				FileInputStream in = new FileInputStream(configFile);
				oldconfig.load(in);
				in.close();
				if (!oldconfig.getProperty("version").equals(
						getDescription().getVersion())) {
					ConfigCreate(true);
					this.log.info("[CartLoad] Configuration file out of date! Updating...");
				} else {
					config = oldconfig;
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void ConfigCreate(boolean update) {
		if (!update) {
			try {
				configFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		config.put("version", getDescription().getVersion());
		ConfigPropertyStore("load_chunks_on_move", "true", update);
		ConfigPropertyStore("keep_stationary_carts_loaded", "true", update);
		ConfigPropertyStore("radius_of_loaded_chunks", "3", update);
		ConfigPropertyStore("load_all_carts_on_startup", "true", update);
		ConfigPropertyStore("minecarts_ignore_entities", "false", update);
		ConfigPropertyStore("minecarts_run_over_mobs", "false", update);
		ConfigPropertyStore("minecarts_ignore_players", "false", update);
		ConfigPropertyStore("minecarts_run_over_players", "false", update);
		ConfigPropertyStore("minecarts_dont_slow_down","false",update);
		try {
			FileOutputStream out = new FileOutputStream(configFile);
			config.store(out, null);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void ConfigPropertyStore(String key, String defaultvalue,
			boolean update) {
		if ((update) && (oldconfig.contains(key)))
			config.put(key, oldconfig.get(key));
		else
			config.put(key, defaultvalue);
	}

	public void onDisable() {
		this.log.info("CartLoad has been disabled - minecarts will now unload normally.");
	}
}