package io.github.bedwarsrel.commands;

import com.google.common.collect.ImmutableMap;
import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.game.Game;
import io.github.bedwarsrel.game.GameState;
import io.github.bedwarsrel.game.Team;
import io.github.bedwarsrel.utils.ChatWriter;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetSpawnCommand extends BaseCommand implements ICommand {

    public SetSpawnCommand(BedwarsRel plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, ArrayList<String> args) {
        if (!super.hasPermission(sender)) {
            return false;
        }

        Player player = (Player) sender;

        if (!this.getPlugin().setupGameName.containsKey(sender.getName())) {
            sender.sendMessage("请输入 /bw addgame 添加房间后再试!");
            return false;
        }
        String gameName = this.getPlugin().setupGameName.get(sender.getName());
        Game game = this.getPlugin().getGameManager().getGame(gameName);
        if (game == null) {
            player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED
                    + BedwarsRel
                    ._l(player, "errors.gamenotfound", ImmutableMap.of("game", gameName))));
            return false;
        }

        if (game.getState() == GameState.RUNNING) {
            sender.sendMessage(
                    ChatWriter.pluginMessage(ChatColor.RED + BedwarsRel
                            ._l(sender, "errors.notwhilegamerunning")));
            return false;
        }

        Team team = game.getTeam(args.get(0));
        if (team == null) {
            player.sendMessage(
                    ChatWriter.pluginMessage(ChatColor.RED + BedwarsRel._l(player, "errors.teamnotfound")));
            return false;
        }

        team.setSpawnLocation(player.getLocation());
        player
                .sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + BedwarsRel
                        ._l(player, "success.spawnset",
                                ImmutableMap
                                        .of("team", team.getChatColor() + team.getDisplayName() + ChatColor.GREEN))));
        return true;
    }

    @Override
    public String[] getArguments() {
        return new String[]{"team"};
    }

    @Override
    public String getCommand() {
        return "setspawn";
    }

    @Override
    public String getDescription() {
        return BedwarsRel._l("commands.setspawn.desc");
    }

    @Override
    public String getName() {
        return BedwarsRel._l("commands.setspawn.name");
    }

    @Override
    public String getPermission() {
        return "setup";
    }

}
