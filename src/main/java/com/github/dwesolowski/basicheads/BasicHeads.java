package com.github.dwesolowski.basicheads;

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
        if (killer == null || !killer.hasPermission("basicheads.drops")) return false;
        return dropRate >= 1 || dropRate > 0 && ThreadLocalRandom.current().nextDouble() >= dropRate;
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
        key = getConfig().getString(key, "");
        return key.trim().isEmpty() ? key : ChatColor.translateAlternateColorCodes('&', key);
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

        msg(victim, LOST_HEAD, "{KILLER}", killer);
        msg(killer, OBTAINED_HEAD, "{VICTIM}", victim);
    }

    private void msg(Player receiver, String msg, String placeHolder, Player replace) {
        if (!msg.trim().isEmpty()) receiver.sendMessage(msg.replace(placeHolder, replace.getDisplayName()));
    }

    public boolean onCommand(CommandSender cs, Command cmd, String alias, String[] args) {
        cs.sendMessage("§7[§aBasicHeads§7] §eConfig Reloaded!");
        reloadConfig();
        return true;
    }
}