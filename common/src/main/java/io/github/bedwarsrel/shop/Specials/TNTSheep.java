package io.github.bedwarsrel.shop.Specials;

import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.events.BedwarsUseTNTSheepEvent;
import io.github.bedwarsrel.game.Game;
import io.github.bedwarsrel.game.GameState;
import io.github.bedwarsrel.game.Team;
import io.github.bedwarsrel.utils.ChatWriter;
import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.SpawnEgg;
import org.bukkit.scheduler.BukkitRunnable;

public class TNTSheep extends SpecialItem {

  private Game game = null;
  private Player player = null;
  private ITNTSheep sheep = null;

  private Player findTargetPlayer(Player player) {
    Player foundPlayer = null;

    if (game.getPlayers().size() == 1) {
      foundPlayer = player;
    } else {
      double distance = Double.MAX_VALUE;

      Team playerTeam = this.game.getPlayerTeam(player);

      ArrayList<Player> possibleTargets = new ArrayList<Player>();
      possibleTargets.addAll(this.game.getTeamPlayers());
      possibleTargets.removeAll(playerTeam.getPlayers());

      for (Player p : possibleTargets) {
        if (player.getWorld() != p.getWorld()) {
          continue;
        }
        double dist = player.getLocation().distance(p.getLocation());
        if (dist < distance) {
          foundPlayer = p;
          distance = dist;
        }
      }
    }
    return foundPlayer;
  }

  @Override
  public Material getActivatedMaterial() {
    return null;
  }

  public int getEntityTypeId() {
    return 91;
  }

  public Game getGame() {
    return this.game;
  }

  public void setGame(Game game) {
    this.game = game;
  }

  @Override
  public Material getItemMaterial() {
    return Material.MONSTER_EGG;
  }

  public Player getPlayer() {
    return this.player;
  }

  public void setPlayer(Player player) {
    this.player = player;
  }

  public ITNTSheep getSheep() {
    return this.sheep;
  }

  @SuppressWarnings("deprecation")
  public void run(Location startLocation) {

    ItemStack usedStack = null;

    if (BedwarsRel.getInstance().getCurrentVersion().startsWith("v1_8")) {
      usedStack = player.getInventory().getItemInHand();
      if (((SpawnEgg) usedStack.getData()).getSpawnedType() != EntityType.SHEEP) {
        return;
      }
      usedStack.setAmount(usedStack.getAmount() - 1);
      player.getInventory().setItem(player.getInventory().getHeldItemSlot(), usedStack);
    } else {
      if (player.getInventory().getItemInOffHand().getType() == this.getItemMaterial()) {
        usedStack = player.getInventory().getItemInOffHand();
        usedStack.setAmount(usedStack.getAmount() - 1);
        player.getInventory().setItemInOffHand(usedStack);
      } else if (player.getInventory().getItemInMainHand().getType() == this.getItemMaterial()) {
        usedStack = player.getInventory().getItemInMainHand();
        usedStack.setAmount(usedStack.getAmount() - 1);
        player.getInventory().setItemInMainHand(usedStack);
      }
    }
    player.updateInventory();

    final Team playerTeam = this.game.getPlayerTeam(this.player);
    Player targetPlayer = this.findTargetPlayer(this.player);
    if (targetPlayer == null) {
      this.player.sendMessage(ChatWriter
          .pluginMessage(
              ChatColor.RED + BedwarsRel
                  ._l(this.player, "ingame.specials.tntsheep.no-target-found")));
      return;
    }

    BedwarsUseTNTSheepEvent event =
        new BedwarsUseTNTSheepEvent(this.game, this.player, targetPlayer, startLocation);
    BedwarsRel.getInstance().getServer().getPluginManager().callEvent(event);

    if (event.isCancelled()) {
      return;
    }

    final Player target = event.getTargetPlayer();
    final Location start = event.getStartLocation();

    // as task
    new BukkitRunnable() {

      @Override
      public void run() {
        final TNTSheep that = TNTSheep.this;

        try {
          // register entity
          Class<?> tntRegisterClass = BedwarsRel.getInstance()
              .getVersionRelatedClass("TNTSheepRegister");
          ITNTSheepRegister register = (ITNTSheepRegister) tntRegisterClass.newInstance();
          TNTSheep.this.sheep = register.spawnCreature(that, start, TNTSheep.this.player, target,
              playerTeam.getColor().getDyeColor());

          new BukkitRunnable() {

            @Override
            public void run() {
              that.getGame().getRegion()
                  .removeRemovingEntity(that.getSheep().getTNT().getVehicle());
              that.getGame().getRegion().removeRemovingEntity(that.getSheep().getTNT());
            }
          }.runTaskLater(BedwarsRel.getInstance(),
              (long) ((
                  BedwarsRel.getInstance().getConfig().getDouble("specials.tntsheep.fuse-time", 8.0)
                      * 20) - 5));

          new BukkitRunnable() {

            @Override
            public void run() {
              that.getSheep().getTNT().remove();
              that.getSheep().remove();
              that.getGame().removeSpecialItem(that);
            }
          }.runTaskLater(BedwarsRel.getInstance(),
              (long) ((
                  BedwarsRel.getInstance().getConfig().getDouble("specials.tntsheep.fuse-time", 8.0)
                      * 20) + 13));

          TNTSheep.this.game.addSpecialItem(that);
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    }.runTask(BedwarsRel.getInstance());
  }

  public void updateTNT() {
    new BukkitRunnable() {

      @Override
      public void run() {
        final TNTSheep that = TNTSheep.this;

        if (that.game.isStopping() || that.game.getState() != GameState.RUNNING) {
          return;
        }

        if (that.sheep == null) {
          return;
        }

        if (that.sheep.getTNT() == null) {
          return;
        }

        TNTPrimed old = that.sheep.getTNT();
        final int fuse = old.getFuseTicks();

        if (fuse <= 0) {
          return;
        }

        final Entity source = old.getSource();
        final Location oldLoc = old.getLocation();
        final float yield = old.getYield();
        old.leaveVehicle();
        old.remove();

        new BukkitRunnable() {

          @Override
          public void run() {
            TNTPrimed primed = (TNTPrimed) that.game.getRegion().getWorld().spawnEntity(oldLoc,
                EntityType.PRIMED_TNT);
            primed.setFuseTicks(fuse);
            primed.setYield(yield);
            primed.setIsIncendiary(false);
            that.sheep.setPassenger(primed);
            that.sheep.setTNT(primed);
            that.sheep.setTNTSource(source);

            if (primed.getFuseTicks() >= 60) {
              that.updateTNT();
            }
          }
        }.runTaskLater(BedwarsRel.getInstance(), 3L);
      }

    }.runTaskLater(BedwarsRel.getInstance(), 60L);
  }

}
