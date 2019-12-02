package com.chromaclypse.slots;

import java.util.HashSet;

import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Firework;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.chromaclypse.slots.MachineData.MachineInfo;
import com.chromaclypse.slots.Roller.Result;

public class Activation implements Listener {
	private Slots handle;

	public Activation(Slots handle) {
		this.handle = handle;
	}

	private HashSet<MachineInfo> pause = new HashSet<>();

	@EventHandler
	public void onActivate(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			BlockData data = event.getClickedBlock().getBlockData();
			
			if(data instanceof Directional) {
				MachineInfo machine = handle.getMachineFromInput(
						event.getClickedBlock().getLocation(), ((Directional)data).getFacing());
	
				if (machine != null && !pause.contains(machine)) {
					Result result = handle.getRoller().roll(event.getPlayer(), machine);
	
					if (result != null) {
						handle.getDisplay().udpate(machine, result);
	
						if (result.isWinner()) {
							pause.add(machine);
	
							handle.getPlugin().getServer().getScheduler().runTaskLater(handle.getPlugin(), () -> {
								pause.remove(machine);
							}, handle.getConfig().victory_delay_ticks);
						}
					}
	
					event.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onDamage(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Firework && event.getDamager().hasMetadata("slots.rocket")) {
			event.setCancelled(true);
		}
	}
}
