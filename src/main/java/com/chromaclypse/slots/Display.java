package com.chromaclypse.slots;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import com.chromaclypse.slots.MachineData.MachineInfo;
import com.chromaclypse.slots.Roller.Result;
import com.chromaclypse.slots.SlotsConfig.SlotData;

public class Display {
	private Slots handle;
	private static final double depth = 0.775;
	private static final double range = 0.3;
	private static final double xoffset = 0.12;
	private static final double yoffset = -0.33;

	public Display(Slots handle) {
		this.handle = handle;
	}

	public void udpate(MachineInfo machine, Result result) {
		final double epsilon = Vector.getEpsilon();
		int i = 0;
		
		for (Location l : standLocations(machine)) {
			for (Entity e : l.getWorld().getNearbyEntities(l, epsilon, epsilon, epsilon)) {
				if (e instanceof ArmorStand && e.getLocation().getDirection().equals(l.getDirection())) {
					ArmorStand as = (ArmorStand) e;
					ItemStack icon = new ItemStack(Material.matchMaterial(result.get(i++).display));

					as.getEquipment().setItemInMainHand(icon);

					break;
				}
			}
		}
	}
	
	private static final int degreesFromFace(BlockFace face) {
		switch(face) {
			case NORTH:
				return 180;
			case EAST:
				return 270;
			case SOUTH:
				return 0;
			case WEST:
				return 90;
			default:
				throw new IllegalArgumentException("Only cardinal directions are supported");
		}
	}

	private Location[] standLocations(MachineInfo machine) {
		Location location = machine.location;
		BlockFace alongFace = BlockFace.valueOf(machine.facing);
		
		int x = alongFace.getModX();
		int z = alongFace.getModZ();

		Location center = location.clone().add(
				x * depth + z * xoffset + 0.5,
				yoffset,
				z * depth - x * xoffset + 0.5);
		
		center.setYaw(degreesFromFace(alongFace));

		double dx = x * range;
		double dz = z * range;

		Location left = center.clone().add(-dz, 0, dx);
		Location right = center.clone().add(dz, 0, -dx);

		return new Location[] { left, center, right };
	}

	public void initalize(MachineInfo machine) {
		SlotData data = handle.dataOf(machine.type);

		if (data != null) {
			ItemStack stack = new ItemStack(Material.matchMaterial(data.potentials.get(0).display));

			for (Location l : standLocations(machine)) {
				l.getWorld().spawn(l, ArmorStand.class, as -> {
					as.setSilent(true);
					as.setVisible(false);
					as.setMarker(true);
					as.setSmall(true);
					as.setGravity(false);
					as.setRightArmPose(new EulerAngle(Math.PI / 2, 0, Math.PI));
					as.getEquipment().setItemInMainHand(stack);
				});
			}
		}
	}

	public void destroy(MachineInfo machine) {
		for (Location l : standLocations(machine)) {
			for (Entity e : l.getWorld().getNearbyEntities(l, Vector.getEpsilon(), Vector.getEpsilon(),
					Vector.getEpsilon())) {

				if (!(e instanceof ArmorStand)) {
					continue;
				}

				if (e.getLocation().getDirection().equals(l.getDirection())) {
					e.remove();
					break;
				}
			}
		}
	}
}
