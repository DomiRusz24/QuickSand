package me.domirusz24.pk.quicksand.quicksand;

import com.google.gson.internal.$Gson$Preconditions;
import com.projectkorra.projectkorra.BendingPlayer;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;

import java.util.HashSet;

public class QuickSandListener implements Listener {

    public static HashSet<String> name = new HashSet<>();

    @EventHandler
    public void onHit(PlayerAnimationEvent event) {
        if (event.getAnimationType().equals(PlayerAnimationType.ARM_SWING)) {
            BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(event.getPlayer());
            if (event.isCancelled() || bPlayer == null) {
                return;
            }

            if (bPlayer.getBoundAbilityName().equalsIgnoreCase(null)) {
                return;
            }

            if (bPlayer.getBoundAbilityName().equalsIgnoreCase("QuickSand")) {
                new QuickSandAbility(bPlayer.getPlayer(), QuickSandAbility.QuickSandType.LeftClick);
            }
        }
    }

    @EventHandler
    public void onShift(PlayerToggleSneakEvent event) {
        if (!event.getPlayer().isSneaking()) {
            BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(event.getPlayer());
            if (event.isCancelled() || bPlayer == null) {
                return;
            }

            if (bPlayer.getBoundAbilityName().equalsIgnoreCase(null)) {
                return;
            }

            if (bPlayer.getBoundAbilityName().equalsIgnoreCase("QuickSand")) {
                new QuickSandAbility(bPlayer.getPlayer(), QuickSandAbility.QuickSandType.Shift);
            }
        }
    }

    @EventHandler(
            priority = EventPriority.NORMAL,
            ignoreCancelled = true
    )
    public void onFall(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            if (event.getDamage() < 10) return;
            BendingPlayer bPlayer = BendingPlayer.getBendingPlayer((Player) event.getEntity());
            if (bPlayer == null) {
                return;
            }

            if (bPlayer.getBoundAbilityName().equalsIgnoreCase(null)) {
                return;
            }

            if (bPlayer.getBoundAbilityName().equalsIgnoreCase("QuickSand")) {
                new QuickSandAbility(bPlayer.getPlayer(), QuickSandAbility.QuickSandType.ShockWave);
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (name.contains(event.getPlayer().getName())) {
            if (event.getFrom().getY() < event.getTo().getY())
            {
                event.setTo(event.getFrom());
            }
        }
    }
}
