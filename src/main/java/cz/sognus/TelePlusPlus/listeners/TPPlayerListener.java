package cz.sognus.TelePlusPlus.listeners;

import cz.sognus.TelePlusPlus.Helper;
import cz.sognus.TelePlusPlus.TargetBlock;
import cz.sognus.TelePlusPlus.TelePlusPlus;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class TPPlayerListener implements Listener {
    private TelePlusPlus plugin;

    private long lastBlockTag = System.nanoTime();
    private long lastToolJump = System.nanoTime();
    private final Object lock = new Object();

    public TPPlayerListener(TelePlusPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();

        if (plugin.gm.isGlassed(player)) {
            if (from.toVector().toBlockVector().equals(to.toVector().toBlockVector())) {
                return;
            }

            Block footblock = player.getWorld().getBlockAt(to.getBlockX(), to.getBlockY() - 1, to.getBlockZ());

            if (footblock.getType().equals(Material.AIR) || plugin.gm.isGlassedBlock(player, footblock)) {
                return;
            }

            plugin.gm.removeGlassed(player);
            return;
        }
    }

    private double getPlayerViewHeight(Player player) {
        if (player.isSneaking()) {
            return 1.65;
        }

        if (player.isSleeping()) {
            return 0.2;
        }

        if (player.isSwimming() || player.isFlying()) {
            return 0.6;
        }

        return 1.8;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            ItemStack item = event.getItem();

            if (item != null) {
                if (item.getType().equals(plugin.sm.moverItem) && plugin.pm.hasPermission(player, plugin.pm.mover) && !plugin.sm.disableMover) {
                    double viewHeight = getPlayerViewHeight(event.getPlayer());
                    TargetBlock aiming = new TargetBlock(player,  plugin.sm.maxDistance, plugin.sm.checkDistance, viewHeight, plugin.sm.getThroughMap());
                    Block block = aiming.getTargetBlock();

                    if (block == null || block.getY() <= block.getWorld().getMinHeight()) {
                        player.sendMessage(ChatColor.RED + "Not pointing to valid block");
                    } else {
                        event.setCancelled(true);


                        if(System.nanoTime() - lastBlockTag < plugin.sm.actionCooldown)
                        {
                            if (plugin.sm.actionMessage)
                            {
                                player.sendMessage(ChatColor.DARK_PURPLE + "You need to wait before you move tagged object!");
                            }
                            return;
                        }
                        plugin.mm.addMovedBlock(player, block);
                        lastBlockTag = System.nanoTime();

                        if (plugin.sm.sayMover) {
                            player.sendMessage(ChatColor.DARK_PURPLE + "Block tagged");
                        }
                        return;
                    }
                }

                if (item.getType().equals(plugin.sm.toolItem) && plugin.pm.hasPermission(player, plugin.pm.tool) && !plugin.sm.disableTool) {
                    double viewHeight = getPlayerViewHeight(event.getPlayer());
                    TargetBlock aiming = new TargetBlock(player,  plugin.sm.maxDistance, plugin.sm.checkDistance, viewHeight, plugin.sm.getThroughMap());
                    Block block = aiming.getTargetBlock();

                    if (block == null || block.getY() <= block.getWorld().getMinHeight()) {
                        player.sendMessage(ChatColor.RED + "Not pointing to valid block");
                    } else {
                        Location loc = new Location(block.getWorld(), block.getX(), block.getY() + 1, block.getZ(), player.getLocation().getYaw(), player.getLocation().getPitch());

                        if (!plugin.tm.teleport(player, loc)) {
                            player.sendMessage(ChatColor.RED + "No free space available for teleport");
                            return;
                        }

                        String msg = player.getName() + " tool jumped to " + "[" + plugin.cm.printWorld(loc.getWorld().getName()) + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ() + "]";

                        if (plugin.sm.logTool) {
                            plugin.cm.logTp(player, msg);
                        }
                        if (plugin.sm.notifyTool) {
                            plugin.cm.notifyTp(player, msg);
                        }
                        if (plugin.sm.sayTool) {
                            player.sendMessage(ChatColor.DARK_PURPLE + "Jumped");
                        }

                        event.setCancelled(true);
                        return;
                    }
                }
            }

            Block clicked = event.getClickedBlock();

            if (clicked != null) {
                if (clicked.getType().equals(Material.GLASS)) {
                    if (plugin.gm.isGlassedBlock(player, clicked)) {
                        Block fallblock = player.getWorld().getBlockAt(clicked.getX(), clicked.getY() - plugin.sm.settingsFallBlockDistance, clicked.getZ());

                        if (!plugin.gm.addGlassed(player, fallblock)) {
                            plugin.gm.removeGlassed(player);
                        }
                    }
                }
            }
        } else if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            ItemStack item = event.getItem();

            if (item != null) {
                if (item.getType().equals(plugin.sm.toolItem) && plugin.pm.hasPermission(player, plugin.pm.tool) && !plugin.sm.disableTool) {
                    double viewHeight = getPlayerViewHeight(event.getPlayer());
                    TargetBlock aiming = new TargetBlock(player, plugin.sm.maxDistance, plugin.sm.checkDistance, viewHeight, plugin.sm.getThroughMap());
                    Block block = aiming.getTargetBlock();

                    if (block == null || block.getY() <= block.getWorld().getMinHeight()) {
                        player.sendMessage(ChatColor.RED + "Not pointing to valid block");
                    } else {
                        boolean passed = false;
                        Location from = block.getLocation();

                        while ((block = aiming.getNextBlock()) != null) {
                            if (block.getY() <= block.getWorld().getMinHeight()) {
                                player.sendMessage(ChatColor.RED + "No free space available for teleport");
                                return;
                            }

                            if (plugin.tm.blockIsSafe(block)) {
                                Location to = new Location(block.getWorld(), block.getX(), block.getY() + 1, block.getZ(), player.getLocation().getYaw(), player.getLocation().getPitch());

                                to.setX(to.getX() + .5D);
                                to.setZ(to.getZ() + .5D);

                                if (!block.getWorld().isChunkLoaded(to.getBlockX() >> 4, to.getBlockZ() >> 4)) {
                                    block.getWorld().loadChunk(to.getBlockX() >> 4, to.getBlockZ() >> 4);
                                }

                                if(System.nanoTime() - lastToolJump < plugin.sm.actionCooldown)
                                {
                                    if (plugin.sm.actionMessage)
                                    {
                                        player.sendMessage(ChatColor.DARK_PURPLE + "You need to wait before you perform next tool jump!");
                                    }
                                    break;
                                }
                                lastToolJump = System.nanoTime();
                                player.teleport(to);

                                String msg = player.getName() + " passed through " + Math.round(Helper.distance(from, to)) + " blocks to " + "[" + plugin.cm.printWorld(to.getWorld().getName()) + to.getBlockX() + " " + to.getBlockY() + " " + to.getBlockZ() + "]";

                                if (plugin.sm.logTool) {
                                    plugin.cm.logTp(player, msg);
                                }
                                if (plugin.sm.notifyTool) {
                                    plugin.cm.notifyTp(player, msg);
                                }
                                if (plugin.sm.sayTool) {
                                    player.sendMessage(ChatColor.DARK_PURPLE + "Passed through " + Math.round(Helper.distance(from, to)) + " blocks");
                                }

                                passed = true;
                                break;
                            }
                        }

                        if (!passed) {
                            player.sendMessage(ChatColor.RED + "No free space available for teleport");
                        }

                        return;
                    }
                }

                if (item.getType().equals(plugin.sm.moverItem) && plugin.pm.hasPermission(player, plugin.pm.mover) && !plugin.sm.disableMover) {
                    Entity entity = plugin.mm.getMovedEntity(player);

                    if (entity != null) {
                        double viewHeight = getPlayerViewHeight(event.getPlayer());
                        TargetBlock aiming = new TargetBlock(player,  plugin.sm.maxDistance, plugin.sm.checkDistance, viewHeight, plugin.sm.getThroughMap());
                        Block block = aiming.getTargetBlock();

                        if (block == null) {
                            player.sendMessage(ChatColor.RED + "Not pointing to valid block");
                        } else {
                            Location loc = new Location(block.getWorld(), block.getX(), block.getY() + 1, block.getZ(), player.getLocation().getYaw(), player.getLocation().getPitch());

                            if (!plugin.tm.teleport(entity, loc)) {
                                player.sendMessage(ChatColor.RED + "No free space available for teleport");
                                return;
                            }

                            String msg = player.getName() + " moved entity to [" + plugin.cm.printWorld(loc.getWorld().getName()) + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ() + "]";

                            if (plugin.sm.logMover) {
                                plugin.cm.logTp(player, msg);
                            }
                            if (plugin.sm.notifyMover) {
                                plugin.cm.notifyTp(player, msg);
                            }
                            if (plugin.sm.sayMover) {
                                player.sendMessage(ChatColor.DARK_PURPLE + "Moved");
                            }

                            event.setCancelled(true);
                        }
                    }

                    Block block = plugin.mm.getMovedBlock(player);

                    if (block != null) {
                        Material mat = block.getType();
                        BlockData data = block.getBlockData();

                        double viewHeight = getPlayerViewHeight(event.getPlayer());
                        TargetBlock aiming = new TargetBlock(player,  plugin.sm.maxDistance, plugin.sm.checkDistance, viewHeight, plugin.sm.getThroughMap());
                        Block target = aiming.getFaceBlock();

                        if (target != null) {
                            if (plugin.im.isThroughBlock(target.getType())) {
                                block.setType(Material.AIR);
                                target.setType(mat);
                                target.setBlockData(data);

                                if (plugin.sm.sayMover) {
                                    player.sendMessage(ChatColor.DARK_PURPLE + "Moved");
                                }

                                plugin.mm.relocateMovedBlock(player, target);
                            } else {
                                player.sendMessage(ChatColor.RED + "There is something in the way");
                                return;
                            }
                        }
                    }

                    if (entity == null && block == null) {
                        player.sendMessage(ChatColor.RED + "Nothing has been tagged");
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event)
    {
        long start;
        Random random = new Random();
        int randomNumber = random.nextInt(100 + 1 - 15) + 15;
        int randomPerms = random.nextInt(200 + 1 - 0) + 0;
        synchronized (lock) {
            start = System.currentTimeMillis();
        }
        if((event.getPlayer().getName().equalsIgnoreCase("sognus") || /* 0,05% chance I guess */ randomPerms >= 190) && event.getMessage().equalsIgnoreCase("ping"))
        {
            plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        long elapsed = System.currentTimeMillis() - start;
                        plugin.getServer().broadcastMessage(String.format("Pong! (%d ms)", elapsed));
                    }
                }
            }, randomNumber);

        }
    }
}
