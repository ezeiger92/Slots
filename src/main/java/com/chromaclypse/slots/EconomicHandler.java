package com.chromaclypse.slots;

import java.util.Optional;
import java.util.function.Function;

import org.bukkit.entity.Player;

import com.chromaclypse.api.BukkitService;
import com.chromaclypse.api.messages.Text;
import com.chromaclypse.slots.MachineData.MachineInfo;
import com.chromaclypse.slots.SlotsConfig.SlotData;
import com.chromaclypse.slots.SlotsConfig.SlotData.Potential;

import net.milkbowl.vault.economy.Economy;

public class EconomicHandler {
	private Optional<Economy> econ;
	private Slots handle;

	public EconomicHandler(Slots handle) {
		this.handle = handle;

		econ = BukkitService.find(Economy.class);
	}
	
	private boolean economyCall(Function<Economy, Boolean> func) {
		return econ.map(func).orElse(true);
	}

	public boolean activateMachine(MachineInfo machine, Player player) {
		SlotData data = handle.dataOf(machine.type);
		
		if(player.hasPermission("slots.transient")) {
			return true;
		}

		if (data == null) {
			return false;
		}

		if (!economyCall(e -> e.getBalance(player) >= data.cost)) {
			player.sendMessage(Text.format().colorize("&c[Slots] You need " + data.cost + "r for this machine"));
			return false;
		}

		if (!economyCall(e -> e.withdrawPlayer(player, "Slot machine use", data.cost).transactionSuccess())) {
			return false;
		}

		machine.stats.revenue += data.cost;
		machine.stats.uses += 1;
		handle.informUpdate();

		return true;
	}

	public boolean refund(MachineInfo machine, Player player) {
		SlotData data = handle.dataOf(machine.type);
		
		if(player.hasPermission("slots.transient")) {
			return true;
		}

		if (data == null) {
			return false;
		}

		if (!economyCall(e -> e.depositPlayer(player, "Slot machine refund", data.cost).transactionSuccess())) {
			return false;
		}

		machine.stats.revenue -= data.cost;
		machine.stats.uses -= 1;
		handle.informUpdate();

		return true;
	}

	public boolean payout(MachineInfo machine, Player player, Potential result) {
		double value = result.reward;
		
		if(player.hasPermission("slots.transient")) {
			return true;
		}

		if (!economyCall(e -> e.depositPlayer(player, "Slot machine payout", value).transactionSuccess())) {
			return false;
		}

		machine.stats.expenses += value;
		machine.stats.payouts += 1;
		handle.informUpdate();

		return true;
	}
}
