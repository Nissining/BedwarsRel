package io.github.bedwarsrel.commands;

import com.google.common.collect.ImmutableMap;
import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.game.Game;
import io.github.bedwarsrel.game.GameState;
import io.github.bedwarsrel.utils.ChatWriter;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetLobbyCommand extends BaseCommand implements ICommand {

    public SetLobbyCommand(BedwarsRel plugin) {
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

        if (game.getState() != GameState.STOPPED) {
            sender.sendMessage(
                    ChatWriter.pluginMessage(ChatColor.RED + BedwarsRel
                            ._l(sender, "errors.notwhilegamerunning")));
            return false;
        }

        game.setLobby(player);
        return true;
    }

    @Override
    public String[] getArguments() {
        return new String[0];
    }

    @Override
    public String getCommand() {
        return "setlobby";
    }

    @Override
    public String getDescription() {
        return BedwarsRel._l("commands.setlobby.desc");
    }

    @Override
    public String getName() {
        return BedwarsRel._l("commands.setlobby.name");
    }

    @Override
    public String getPermission() {
        return "setup";
    }

}
