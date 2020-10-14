package me.domirusz24.pk.quicksand.quicksand;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.*;
import com.projectkorra.projectkorra.util.TempBlock;
import org.apache.commons.lang.ObjectUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.UUID;

public class QuickSandAbility extends SandAbility implements AddonAbility {

    public static int smallRad = 6;
    public static int bigRad = 9;
    public static long duration = 5000;
    public static int streamlength = 10;
    public static int streamwidth = 5;
    private static long cooldown = 2000;

    private QuickSandType type;
    private Location loc;
    private int length;
    private int iterations = 0;

    private HashSet<TempBlock> blocks = new HashSet<>();
    private HashSet<TempBlock> sand = new HashSet<>();
    private HashSet<String> playersStuck = new HashSet<>();

    private Location right;

    public Block getBlock(Location loc, int height) {
        for (int i = height; i >= height * -1; i--) {
            Block l = loc.clone().add(0, i, 0).getBlock();
            if (isEarthbendable(l)) {
                return l;
            }
        }
        return null;
    }

    public Location getTeleport(Location loc) {
        while (GeneralMethods.isSolid(loc.getBlock())) {
            loc.add(0, 1, 0);
        }
        return loc;
    }


    public QuickSandAbility(Player player, QuickSandType type) {
        super(player);
        if (bPlayer.isOnCooldown(this)) return;
        loc = player.getLocation().add(0, -1, 0);
        loc.setDirection(loc.getDirection().setY(0).normalize());
        this.type = type;
        switch (type) {
            case Shift:
                length = smallRad - 3;
                break;
            case LeftClick:
                right = GeneralMethods.getRightSide(loc, ((float) streamwidth - 1) / 2);
                right.setDirection(GeneralMethods.rotateXZ(right.getDirection(), -90));
                right.add(loc.getDirection());
                length = streamlength;
                break;
            case ShockWave:
                length = bigRad - 3;
                break;
        }
        start();
        bPlayer.addCooldown(this);
    }

    public void removeSand() {
        for (TempBlock b : blocks) {
            b.revertBlock();
        }
        for (TempBlock b : sand) {
            b.revertBlock();
        }
        for (String e : playersStuck) {
            Entity en = Bukkit.getEntity(UUID.fromString(e));
            if (en != null) {
                en.teleport(getTeleport(en.getLocation()));
                if (en.getType().equals(EntityType.PLAYER)) QuickSandListener.name.remove(((Player) en).getName());
            }
        }
        blocks.clear();
    }

    public void addSand(Location l) {
        Block block = getBlock(l, 3);
        if (block == null) return;
        if (isEarthbendable(block.getLocation().add(0, -1, 0).getBlock())) {
            TempBlock tb = new TempBlock(block, Material.SAND, (byte) 0);
            TempBlock ss = new TempBlock(block.getLocation().add(0, -1, 0).getBlock(), Material.SANDSTONE, (byte) 0);
            tb.setRevertTime(QuickSandAbility.duration);
            ss.setRevertTime(QuickSandAbility.duration);
            blocks.add(tb);
            sand.add(ss);
        }
    }

    @Override
    public void progress() {
        if (iterations >= (duration / 50) || player.isDead() || !player.isOnline()) {
            removeSand();
            remove();
            return;
        }
        World w = loc.getWorld();
        if (iterations < length) {
            if (type.equals(QuickSandType.Shift) || type.equals(QuickSandType.ShockWave)) {
                for (Location l : GeneralMethods.getCircle(loc, iterations + 4, 1, true, false, 0)) {
                    addSand(l);
                }
            } else {
                right.add(loc.getDirection());
                for (Block b : GeneralMethods.getBlocksAlongLine(right, right.clone().add(right.clone().getDirection().multiply(streamwidth - 1)), w)) {
                    addSand(b.getLocation());
                }
            }
        }

        HashSet<String> tempNames = new HashSet<>(playersStuck);
        for (TempBlock t : blocks) {
            for (Entity e : loc.getWorld().getEntities()) {
                if (e.getLocation().getBlockX() == t.getLocation().getBlockX() && e.getLocation().getBlockZ() == t.getLocation().getBlockZ()) {
                    if (e.getLocation().getBlockY() <= t.getLocation().getBlockY() + 1 && e.getLocation().getBlockY() >= t.getLocation().getBlockY() - 1) {
                        /*
                        if (e.equals(player)) {
                            t.setType(Material.SANDSTONE);
                            continue;
                        }
                        */
                        if (playersStuck.contains(e.getUniqueId().toString())) {
                            if (!tempNames.contains(e.getUniqueId().toString())) continue;
                            if (!GeneralMethods.isSolid(e.getLocation().getBlock())) continue;
                            if (e.isOnGround()) {
                                e.sendMessage("Ziemia");
                            }
                            if (e.getLocation().getY() == Math.floor(e.getLocation().getY())) {
                                e.sendMessage("Ziemia Teoretycznie");
                                e.teleport(e.getLocation().add(0, -0.1, 0));
                            } else {
                                e.setVelocity(new Vector(0, -0.01, 0));
                            }
                            tempNames.remove(e.getUniqueId().toString());
                            if (e instanceof LivingEntity) {
                                ((LivingEntity) e).addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 20, 5));
                                ((LivingEntity) e).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20, 2));
                            }
                        } else if (e.isOnGround()) {
                            e.sendMessage("Utknales w QuickSand");
                            if (!e.getLocation().add(0, -1, 0).getBlock().getType().equals(Material.SAND) && !e.getLocation().add(0, -1, 0).getBlock().getType().equals(Material.SANDSTONE)) continue;
                            e.setVelocity(new Vector(e.getVelocity().getX(), 0, e.getVelocity().getZ()));
                            e.teleport(e.getLocation().add(0, -0.1, 0));
                            playersStuck.add(e.getUniqueId().toString());
                            if (e instanceof LivingEntity) {
                                ((LivingEntity) e).addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 20, 5));
                                ((LivingEntity) e).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20, 2));
                                if (e.getType().equals(EntityType.PLAYER)) QuickSandListener.name.add(((Player) e).getName());
                            }
                        }
                    }
                }
            }
        }
        for (String e : tempNames) {
            playersStuck.remove(e);
            Entity en = Bukkit.getEntity(UUID.fromString(e));
            if (en != null) {
                en.teleport(getTeleport(en.getLocation()));
                if (en.getType().equals(EntityType.PLAYER)) QuickSandListener.name.remove(((Player) en).getName());
                en.sendMessage("Uciekles z QuickSand");
            }
        }
        iterations++;
    }

    @Override
    public boolean isSneakAbility() {
        return true;
    }

    @Override
    public boolean isHarmlessAbility() {
        return false;
    }

    @Override
    public long getCooldown() {
        return cooldown;
    }

    @Override
    public String getName() {
        return "QuickSand";
    }

    @Override
    public Location getLocation() {
        return null;
    }

    @Override
    public void load() {
        ProjectKorra.plugin.getServer().getPluginManager().registerEvents(new QuickSandListener(), ProjectKorra.plugin);
        ProjectKorra.log.info("Successfully enabled " + getName() + " by " + getAuthor() + ".");
    }

    @Override
    public void stop() {
        for (QuickSandAbility e : CoreAbility.getAbilities(QuickSandAbility.class)) {
            e.removeSand();
            e.remove();
        }
        super.remove();
        ProjectKorra.log.info("Successfully disabled " + getName() + " by " + getAuthor() + ".");
    }

    @Override
    public String getAuthor() {
        return "DomiRusz24";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    public enum QuickSandType {
        Shift,
        LeftClick,
        ShockWave;
    }
}
