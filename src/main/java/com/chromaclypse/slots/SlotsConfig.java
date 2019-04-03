package com.chromaclypse.slots;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Color;
import org.bukkit.inventory.ItemStack;

import com.chromaclypse.api.Defaults;
import com.chromaclypse.api.config.ConfigObject;

public class SlotsConfig extends ConfigObject {
	public Map<String, SlotData> slot_tables = new HashMap<>();
	
	public static class SlotData {
		public double cost = 0.0;
		public List<Potential> potentials = new ArrayList<>();

		public static class Potential {
			public String display = "STONE";
			public double weight = 1.0;
			public double reward = 0.0;
			public ItemStack rewardItem = null;
			public int announceRadius = 0;
			public String rewardMessage = "Winner!";
			public String announceMessage = "<player> won!";
			public List<Color> colors = Defaults.list(Color.WHITE);
		}
	}

	public int victory_delay_ticks = 15;
}
