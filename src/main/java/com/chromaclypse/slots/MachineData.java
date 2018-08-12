package com.chromaclypse.slots;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;

import com.chromaclypse.api.config.ConfigObject;
import com.chromaclypse.api.config.Section;

@Section(path = "slot_locations.yml")
public class MachineData extends ConfigObject {
	public Map<String, MachineInfo> machines = new HashMap<>();

	public static class MachineInfo {
		public Location location = null;
		public String facing = "NORTH";
		public String type = null;
		public Stats stats = new Stats();

		public static class Stats {
			public int payouts = 0;
			public int uses = 0;
			public double expenses = 0.0;
			public double revenue = 0.0;
		}
	}
}
