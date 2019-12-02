package com.chromaclypse.slots;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.metadata.FixedMetadataValue;

import com.chromaclypse.api.messages.Text;
import com.chromaclypse.slots.MachineData.MachineInfo;
import com.chromaclypse.slots.SlotsConfig.SlotData;
import com.chromaclypse.slots.SlotsConfig.SlotData.Potential;

public class Roller {
	private final Slots handle;
	
	private final FixedMetadataValue metadata;

	public Roller(Slots handle) {
		this.handle = handle;
		metadata = new FixedMetadataValue(handle.getPlugin(), "dummy");
	}

	public Result roll(Player player, MachineInfo machine) {
		SlotData data = handle.dataOf(machine.type);

		if (data == null || !handle.getTransactions().activateMachine(machine, player)) {
			player.sendMessage(Text.format().colorize(
					"&c[Slots] This machine ran into an issue, please contact an admin! You have not been chared."));
			return null;
		}

		double totalWeight = 0.0;
		for (Potential p : data.potentials) {
			totalWeight += p.weight;
		}

		double[] rolls = {
				machine.getRandom().nextDouble() * totalWeight,
				machine.getRandom().nextDouble() * totalWeight,
				machine.getRandom().nextDouble() * totalWeight,
		};

		Potential[] results = { null, null, null, };
		int resultCount = 0;
		boolean match = false;

		for (Potential p : data.potentials) {
			int matches = 0;
			for (int i = 0; i < 3; ++i) {
				if (results[i] == null) {
					rolls[i] -= p.weight;

					if (rolls[i] <= 0) {
						results[i] = p;
						++resultCount;
						++matches;
					}
				}
			}

			if (matches == 3) {
				match = true;
			}

			if (resultCount == 3) {
				break;
			}
		}

		if (match) {
			if(results[0].reward > 0.0) {
				// Attempt payout
				if (!handle.getTransactions().payout(machine, player, results[0])) {
					// Attempt refund
					if (!handle.getTransactions().refund(machine, player)) {
						player.sendMessage(Text.format().colorize("&c[Slots] Serious machine error, please contact an admin!"));
						throw new IllegalStateException("Failed to refund " + data.cost + " to \"" + player.getName()
								+ "\" (" + player.getUniqueId().toString() + ") after failing to pay out "
								+ results[0].reward);
					}

					player.sendMessage(Text.format().colorize(
							"&c[Slots] This machine ran into an issue, please contact an admin! You have not been chared."));
					return null;
				}
			}
			
			// Item
			if(results[0].rewardItem != null) {
				if(!player.hasPermission("slots.transient")) {
					player.getInventory().addItem(results[0].rewardItem.clone());
				}
			}
			
			// Messages
			player.sendMessage(Text.format().colorize(results[0].rewardMessage));
			
			int rad = results[0].announceRadius * 2;
			
			if(rad > 0 || rad == -2) {
				String formatted = Text.format().colorize(results[0].announceMessage).replace("<player>", player.getName());
				
				if(rad == -2) {
					for(Player p : Bukkit.getOnlinePlayers()) {
						p.sendMessage(formatted);
					}
				}
				else {
					for(Entity e : player.getWorld().getNearbyEntities(machine.location, rad, rad, rad,
							entity -> entity instanceof Player && entity != player)) {
						Player p = (Player)e;
						
						if(p != player) {
							p.sendMessage(formatted);
						}
					}
				}
			}
			
			BlockFace facing = BlockFace.valueOf(machine.facing);
			Location launch = machine.location.clone().add(0.5 + facing.getModX() * 0.8, 0.0, 0.5 + facing.getModZ() * 0.8);
			
			launch.getWorld().spawn(launch, Firework.class, firework -> {
				FireworkMeta meta = firework.getFireworkMeta();
				
				meta.setPower(0);
				meta.addEffect(FireworkEffect.builder().withTrail().withFlicker().with(FireworkEffect.Type.BALL)
					.withColor(results[0].colors.toArray(new Color[0]))
					.withFade(Color.GREEN, Color.AQUA)
					.build());
				
				firework.setFireworkMeta(meta);
				firework.setMetadata("slots.rocket", metadata);
			});
		}

		return new Result(results, match);
	}

	public static class Result {
		private Potential[] results;
		private boolean winner;

		private Result(Potential[] results, boolean winner) {
			this.results = results;
			this.winner = winner;
		}

		public Potential first() {
			return results[0];
		}

		public Potential second() {
			return results[1];
		}

		public Potential third() {
			return results[2];
		}

		public Potential get(int index) {
			return results[index];
		}

		public boolean isWinner() {
			return winner;
		}
	}
}
