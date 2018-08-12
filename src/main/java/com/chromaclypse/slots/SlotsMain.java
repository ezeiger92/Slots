package com.chromaclypse.slots;

import org.bukkit.command.PluginCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class SlotsMain extends JavaPlugin {
	private Slots impl;
	private int autosave;

	@Override
	public void onEnable() {
		impl = new Slots(this);

		SlotsCommand onCommand = new SlotsCommand(impl);

		PluginCommand command = getServer().getPluginCommand("slots");
		command.setExecutor(onCommand);
		command.setTabCompleter(onCommand);

		getServer().getPluginManager().registerEvents(new Activation(impl), this);

		autosave = getServer().getScheduler().scheduleSyncRepeatingTask(this, impl::queueSave, 20 * 10, 20 * 20);
	}

	@Override
	public void onDisable() {
		impl = null;
		HandlerList.unregisterAll(this);
		getServer().getScheduler().cancelTask(autosave);
	}
}
