package io.github.bedwarsrel.commands;

import com.google.common.collect.ImmutableMap;
import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.game.Game;
import io.github.bedwarsrel.game.GameState;
import io.github.bedwarsrel.utils.ChatWriter;
import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class SaveGameCommand extends BaseCommand implements ICommand {

  public SaveGameCommand(BedwarsRel plugin) {
    super(plugin);
  }

  @Override
  public boolean execute(CommandSender sender, ArrayList<String> args) {
    if (!sender.hasPermission("bw." + this.getPermission())) {
      return false;
    }

    if (!this.getPlugin().setupGameName.containsKey(sender.getName())) {
      sender.sendMessage("请输入 /bw addgame 添加房间后再试!");
      return false;
    }
    String gameName = this.getPlugin().setupGameName.get(sender.getName());
    Game game = this.getPlugin().getGameManager().getGame(gameName);

    if (game == null) {
      sender.sendMessage(ChatWriter.pluginMessage(ChatColor.RED
          + BedwarsRel
          ._l(sender, "errors.gamenotfound", ImmutableMap.of("game", gameName))));
      return false;
    }

    if (game.getState() == GameState.RUNNING) {
      sender.sendMessage(
          ChatWriter.pluginMessage(ChatColor.RED + BedwarsRel
              ._l(sender, "errors.notwhilegamerunning")));
      return false;
    }

    if (!game.saveGame(sender, true)) {
      return false;
    }

    sender
        .sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + BedwarsRel
            ._l(sender, "success.saved")));
    return true;
  }

  @Override
  public String[] getArguments() {
    return new String[]{"game"};
  }

  @Override
  public String getCommand() {
    return "save";
  }

  @Override
  public String getDescription() {
    return BedwarsRel._l("commands.save.desc");
  }

  @Override
  public String getName() {
    return BedwarsRel._l("commands.save.name");
  }

  @Override
  public String getPermission() {
    return "setup";
  }

}
