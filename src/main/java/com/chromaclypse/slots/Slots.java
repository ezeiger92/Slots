package com.chromaclypse.slots;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.plugin.Plugin;

import com.chromaclypse.slots.MachineData.MachineInfo;
import com.chromaclypse.slots.SlotsConfig.SlotData;

public class Slots {
	private Plugin handle;
	private SlotsConfig config = new SlotsConfig();
	private MachineData data = new MachineData();
	private Roller roller;
	private Random random = new Random();
	private EconomicHandler econ;
	private Display display;
	public boolean needsUpdate = false;

	private HashMap<Location, Location> controlCache = new HashMap<>();

	private HashMap<Location, UUID> cache = new HashMap<>();

	public Slots(Plugin handle) {
		this.handle = handle;

		config.init(handle);
		data.init(handle);

		for (Map.Entry<String, MachineInfo> m : data.machines.entrySet()) {
			MachineInfo machine = m.getValue();
			SlotData type = config.slot_tables.get(machine.type);

			if (type != null) {
				Location location = machine.location;

				cache.put(location, UUID.fromString(m.getKey()));
				storeControls(location, BlockFace.valueOf(machine.facing));
			}
		}

		roller = new Roller(this);
		econ = new EconomicHandler(this);
		display = new Display(this);
	}

	private boolean storeControls(Location location, BlockFace alongFace) {
		BlockFace f;
		Location l1 = location.clone().add(alongFace.getModX(), 0, alongFace.getModZ());
		Location l2 = l1.clone();

		switch (alongFace) {
			case NORTH:
			case SOUTH:
				f = BlockFace.EAST;
				l1.add(f.getModX(), 0, f.getModZ());

				f = BlockFace.WEST;
				l2.add(f.getModX(), 0, f.getModZ());
				break;

			default:
				f = BlockFace.NORTH;
				l1.add(f.getModX(), 0, f.getModZ());

				f = BlockFace.SOUTH;
				l2.add(f.getModX(), 0, f.getModZ());
				break;
		}
		
		if(controlCache.containsKey(l1) || controlCache.containsKey(l2)) {
			return false;
		}

		controlCache.put(l1, location);
		controlCache.put(l2, location);
		return true;
	}

	public void save() {
		needsUpdate = false;
		data.save(handle);
	}
	
	public void queueSave() {
		if (needsUpdate) {
			save();
		}
	}

	public boolean createMachine(String type, Location location, BlockFace alongFace) {
		UUID tie = cache.get(location);
		SlotData d = config.slot_tables.get(type);

		if (tie == null && d != null) {
			UUID key = UUID.randomUUID();
			MachineInfo i = new MachineInfo();
			i.location = location;
			i.type = type;
			i.facing = alongFace.name();
			
			if(storeControls(location, alongFace)) {
				needsUpdate = true;
				
				data.machines.put(key.toString(), i);
				cache.put(location, key);

				display.initalize(i);

				return true;
			}
		}

		return false;
	}

	public MachineInfo getMachineFromInput(Location location) {
		Location actualMachine = controlCache.get(location);

		if (actualMachine != null) {
			MachineInfo result = getMachine(actualMachine);

			if (result != null) {
				return result;
			}

			controlCache.remove(location);
		}

		return null;
	}

	public MachineInfo getMachine(Location location) {
		UUID key = cache.get(location);

		if (key != null) {
			return data.machines.get(key.toString());
		}

		return null;
	}

	public SlotData dataOf(String type) {
		return config.slot_tables.get(type);
	}

	public boolean removeMachine(Location location) {
		UUID key = cache.remove(location);

		if (key != null) {
			MachineInfo machine = data.machines.remove(key.toString());

			if (machine != null) {

				needsUpdate = true;

				display.destroy(machine);
				return true;
			}
		}

		return false;
	}

	public SlotsConfig getConfig() {
		return config;
	}

	public MachineData getData() {
		return data;
	}

	public Plugin getPlugin() {
		return handle;
	}

	public Roller getRoller() {
		return roller;
	}

	public Random getRandom() {
		return random;
	}

	public EconomicHandler getTransactions() {
		return econ;
	}

	public Display getDisplay() {
		return display;
	}

	public void informUpdate() {
		needsUpdate = true;
	}
}
