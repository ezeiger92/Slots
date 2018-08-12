package com.chromaclypse.slots;

import org.bukkit.entity.Player;

import com.chromaclypse.api.messages.Text;
import com.chromaclypse.slots.MachineData.MachineInfo;
import com.chromaclypse.slots.SlotsConfig.SlotData;
import com.chromaclypse.slots.SlotsConfig.SlotData.Potential;

public class Roller {
	private Slots handle;

	public Roller(Slots handle) {
		this.handle = handle;
	}

	public Result roll(Player player, MachineInfo machine) {
		SlotData data = handle.dataOf(machine.type);

		if (data == null || !handle.getTransactions().activateMachine(machine, player)) {
			player.sendMessage(Text.colorize(
					"&c[Slots] This machine ran into an issue, please contact an admin! You have not been chared."));
			return null;
		}

		double totalWeight = 0.0;
		for (Potential p : data.potentials) {
			totalWeight += p.weight;
		}

		double[] rolls = {
				handle.getRandom().nextDouble() * totalWeight,
				handle.getRandom().nextDouble() * totalWeight,
				handle.getRandom().nextDouble() * totalWeight,
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
			player.sendMessage("Winner! " + results[0].reward);
			// Attempt payout
			if (!handle.getTransactions().payout(machine, player, results[0])) {
				// Attempt refund
				if (!handle.getTransactions().refund(machine, player)) {
					player.sendMessage(Text.colorize("&c[Slots] Serious machine error, please contact an admin!"));
					throw new IllegalStateException("Failed to refund " + data.cost + " to \"" + player.getName()
							+ "\" (" + player.getUniqueId().toString() + ") after failing to pay out "
							+ results[0].reward);
				}

				player.sendMessage(Text.colorize(
						"&c[Slots] This machine ran into an issue, please contact an admin! You have not been chared."));
				return null;
			}
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
