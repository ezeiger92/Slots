package com.chromaclypse.slots;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		}
	}

	public int victory_delay_ticks = 15;
}
