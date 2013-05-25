package com.DarkBlade12.ModernWeapons.Config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class GrenadesLoader {

	private YamlConfiguration config;
	private File configFile;
	Plugin plugin;

	/**
	 * Creates a new ConfigLoader, for handling default config.yml
	 * 
	 * @param main
	 *            The plugin's main class, for deloading.
	 */
	public GrenadesLoader(Plugin main) {
		plugin = main;
		load();
	}

	/**
	 * Gets the config.
	 * 
	 * @return The config.
	 */
	public YamlConfiguration getConfig() {
		return this.config;
	}

	/**
	 * Saves the config to disk
	 */
	public void save() {
		try {
			this.config.save(this.configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Loads the config from disk
	 */
	public void load() {
		this.configFile = new File("plugins/ModernWeapons/grenades.yml");
		// Create target and copy res-content
		if (!this.configFile.exists()) {
			try {
				new File("plugins/ModernWeapons/").mkdirs();
				this.configFile.createNewFile();
				copyResourceYAML(getClass().getResourceAsStream("grenades.yml"), this.configFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		this.config = new YamlConfiguration();
		try {
			this.config.load(this.configFile);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Copies the resource config.yml's content, to plugin folder config.yml
	 */
	public void copyResourceYAML(InputStream source, File target) {

		BufferedWriter writer = null;
		BufferedReader reader = new BufferedReader(new InputStreamReader(source));

		try {
			writer = new BufferedWriter(new FileWriter(target));
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			try {
				String buffer = "";

				while ((buffer = reader.readLine()) != null) {
					writer.write(buffer);
					writer.newLine();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} finally {
			try {
				if (writer != null)
					writer.close();
				if (reader != null)
					reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

