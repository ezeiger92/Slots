package com.chromaclypse.slots;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.chromaclypse.api.messages.Text;
import com.chromaclypse.slots.MachineData.MachineInfo;
import com.chromaclypse.slots.MachineData.MachineInfo.Stats;

public class SlotsCommand implements TabExecutor {
	private static final List<String> subcommands = Arrays.asList("create", "gstats", "info", "reload", "remove",
			"save");
	private static final List<String> emptyList = Arrays.asList();

	private Slots handle;

	public SlotsCommand(Slots handle) {
		this.handle = handle;
	}

	private static String printDouble(double d) {
		long whole = (long) Math.floor(d);
		double d1 = d - whole;

		if (d < 0) {
			d1 = 1 - d1;
		}

		long frac = (long) Math.round(d1 * 100);

		StringBuilder sb = new StringBuilder();
		sb.append(whole);

		if (frac > 0) {
			sb.append('.');

			if (frac < 100) {
				sb.append('0');

				if (frac < 10) {
					sb.append('0');
				}
			}

			if (frac % 10 == 0) {
				if (frac % 100 == 0) {
					frac /= 10;
				}

				frac /= 10;
			}

			sb.append(frac);
		}

		return sb.toString();
	}

	private void printStats(CommandSender sender, Stats stats) {
		double balance = stats.revenue - stats.expenses;

		sender.sendMessage(Text.colorize("  Balance: " + printDouble(balance) + " &a(+" + printDouble(stats.revenue)
				+ ") &c[-" + printDouble(stats.expenses) + "]"));
		sender.sendMessage("  Uses: " + stats.uses + ", Payouts: " + stats.payouts);

		if (stats.payouts > 0) {
			sender.sendMessage("  Average payout: " + printDouble(stats.expenses / stats.payouts));
		}

		if (stats.uses > 0) {
			sender.sendMessage("  Average profit: " + printDouble(balance / stats.uses));
		}
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		switch (args.length) {
			case 0:
				return subcommands;

			case 1:
				return subcommands.stream().filter(s -> s.startsWith(args[0])).collect(Collectors.toList());

			default:
				if ((args[0].equals("create") || args[0].equals("gstats")) && args.length == 2) {
					return handle.getConfig().slot_tables.keySet().stream().filter(s -> s.startsWith(args[1]))
							.collect(Collectors.toList());
				}

				return emptyList;
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length > 0) {
			switch (args[0].toLowerCase(Locale.ENGLISH)) {
				case "create":
					if (sender instanceof Player) {

						if (args.length < 2) {
							sender.sendMessage(Text.colorize("&c[Slots] Missing machine type! /slots create <type>"));
							break;
						}

						if (handle.dataOf(args[1]) == null) {
							sender.sendMessage(Text.colorize("&c[Slots] Unknown machine type! Try tab completing"));
							break;
						}

						Player player = (Player) sender;
						Block target = player.getTargetBlock(null, 8);
						float yaw = player.getLocation().getYaw();
						BlockFace lookingAt;

						yaw -= Math.floor(yaw / 360) * 360;

						if (yaw >= 315 || yaw < 45) {
							lookingAt = BlockFace.NORTH;
						}
						else if (yaw < 135) {
							lookingAt = BlockFace.EAST;
						}
						else if (yaw < 225) {
							lookingAt = BlockFace.SOUTH;
						}
						else {
							lookingAt = BlockFace.WEST;
						}

						if (target.getType() != Material.AIR) {
							handle.createMachine(args[1], target.getLocation(), lookingAt);
							sender.sendMessage(Text.colorize("&a[Slots] Machine created"));
						}
					}
					break;

				case "gstats":
					String arg;
					Stats total = new Stats();
					int machines = 0;

					if (args.length > 1) {
						arg = args[1];
						if (handle.dataOf(args[1]) == null) {
							sender.sendMessage(Text.colorize("&c[Slots] Unknown machine type! Try tab completing"));
							break;
						}

						for (MachineInfo machine : handle.getData().machines.values()) {
							if (machine.type.equals(args[1])) {
								++machines;
								total.expenses += machine.stats.expenses;
								total.payouts += machine.stats.payouts;
								total.revenue += machine.stats.revenue;
								total.uses += machine.stats.uses;
							}
						}
					}
					else {
						arg = "Global";
						for (Map.Entry<String, MachineInfo> entry : handle.getData().machines.entrySet()) {
							++machines;
							total.expenses += entry.getValue().stats.expenses;
							total.payouts += entry.getValue().stats.payouts;
							total.revenue += entry.getValue().stats.revenue;
							total.uses += entry.getValue().stats.uses;
						}
					}

					sender.sendMessage("[Slots] Machine info: [" + arg + "]");
					printStats(sender, total);

					double avgRevenue = total.revenue / machines;
					double avgExpenses = total.expenses / machines;
					double avgPayouts = total.payouts / (double) machines;
					double avgUses = total.uses / (double) machines;

					double avgBalance = avgRevenue - avgExpenses;

					sender.sendMessage("  Total machines: " + machines);
					sender.sendMessage(Text.colorize("  Avg balance: " + printDouble(avgBalance) + " &a(+"
							+ printDouble(avgRevenue) + ") &c[-" + printDouble(avgExpenses) + "]"));
					sender.sendMessage(
							"  Avg uses: " + printDouble(avgUses) + ", Avg payouts: " + printDouble(avgPayouts));

					break;

				case "info":
					if (sender instanceof Player) {
						Player player = (Player) sender;
						Block target = player.getTargetBlock(null, 8);

						if (target != null) {
							MachineInfo machine = handle.getMachine(target.getLocation());

							if (machine != null) {
								sender.sendMessage("[Slots] Machine info: " + target.getLocation());
								printStats(sender, machine.stats);
							}
							else {
								sender.sendMessage(Text.colorize("&c[Slots] That is not a machine"));
							}
						}
					}
					break;

				case "reload":
					handle.getConfig().init(handle.getPlugin());
					sender.sendMessage(Text.colorize("&a[Slots] Config reloaded"));
					break;

				case "remove":
					if (sender instanceof Player) {
						Player player = (Player) sender;
						Block target = player.getTargetBlock(null, 8);

						if (target != null) {
							handle.removeMachine(target.getLocation());
							sender.sendMessage(Text.colorize("&a[Slots] Machine removed"));
						}
						else {
							sender.sendMessage(Text.colorize("&c[Slots] That is not a machine"));
						}
					}
					break;

				case "save":
					handle.save();
					sender.sendMessage(Text.colorize("&a[Slots] Saved to disk"));
					break;
				default:
			}
		}
		else {

		}

		return true;
	}

}
