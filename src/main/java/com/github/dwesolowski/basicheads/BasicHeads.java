package com.github.dwesolowski.basicheads;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ThreadLocalRandom;

public class BasicHeads extends JavaPlugin implements Listener {
    private String LOST_HEAD, OBTAINED_HEAD;

    public static boolean shouldDrop(Player killer, double dropRate) {
        return killer != null && killer.hasPermission("basicHeads.drops") &&
               (dropRate >= 1 || (dropRate > 0 && ThreadLocalRandom.current().nextDouble() >= dropRate));
    }

    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        LOST_HEAD = interpret("Messages.LostYourHead");
        OBTAINED_HEAD = interpret("Messages.ObtainedHead");
    }

    private String interpret(String key) {
        return ChatColor.translateAlternateColorCodes('&', getConfig().getString(key, ""));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onDeath(PlayerDeathEvent evt) {
        final Player victim = evt.getEntity(), killer = victim.getKiller();
        if (!shouldDrop(killer, getConfig().getDouble("DropRate", 0))) return;

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = head.getItemMeta();

        if (meta instanceof SkullMeta)
            ((SkullMeta) meta).setOwningPlayer(victim);
        head.setItemMeta(meta);
        victim.getWorld().dropItemNaturally(victim.getLocation(), head);

        if (!StringUtils.isBlank(LOST_HEAD))
            victim.sendMessage(LOST_HEAD.replace("{KILLER}", killer.getDisplayName()));
        if (!StringUtils.isBlank(OBTAINED_HEAD))
            killer.sendMessage(OBTAINED_HEAD.replace("{VICTIM}", victim.getDisplayName()));
    }

    public boolean onCommand(CommandSender cs, Command cmd, String alias, String[] args) {
        cs.sendMessage("§7[§aBasicHeads§7] §eConfig Reloaded!");
        reloadConfig();
        return true;
    }
}