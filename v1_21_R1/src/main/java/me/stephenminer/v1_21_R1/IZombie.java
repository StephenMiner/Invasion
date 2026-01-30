package me.stephenminer.v1_21_R1;

import me.stephenminer.invasion.Invasion;
import me.stephenminer.invasion.entity.InvasionMob;
import me.stephenminer.invasion.entity.MobType;
import me.stephenminer.invasion.nexus.Nexus;
import me.stephenminer.v1_21_R1.pathfinder.InvasionGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_21_R1.CraftWorld;
import org.bukkit.entity.Mob;

import java.util.UUID;

public class IZombie extends Zombie implements InvasionMob {
    private InvasionGoal goal;
    private UUID nexusUUID;
    private Nexus nexus;

    public IZombie(Location loc, float health, UUID nexusUUID){
        super(EntityType.ZOMBIE, ((CraftWorld) loc.getWorld()).getHandle());
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(health);
        this.setHealth(health);
        this.setPos(loc.getX(), loc.getY(), loc.getZ());
        this.nexusUUID = nexusUUID;
        this.level().addFreshEntity(this);
        implantMobType();
        implantNexusData();
    }

    public IZombie(Location loc, float health) {
        super(EntityType.ZOMBIE, ((CraftWorld) loc.getWorld()).getHandle());
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(health);
        this.setHealth(health);
        this.setPos(loc.getX(), loc.getY(), loc.getZ());

        this.level().addFreshEntity(this);
    }


    @Override
    public void tick(){
        super.tick();
        if (nexus == null && Invasion.nexusMap.containsKey(nexusUUID)){
            System.out.println(33333);
            this.nexus = Invasion.nexusMap.get(nexusUUID);
            Location loc = nexus.loc();
            this.setTargetPos(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0,new FloatGoal(this));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(2, new ZombieAttackGoal(this, 1.0, false));
        this.goal = new InvasionGoal(this);
        this.goalSelector.addGoal(3, goal);

        this.targetSelector.addGoal(1, (new HurtByTargetGoal(this, new Class[0])).setAlertOthers(new Class[]{ZombifiedPiglin.class}));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        /*
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true, (entity) -> {
            if (entity instanceof Player) {
                Player p = (Player) entity;
                org.bukkit.entity.Player player = Bukkit.getPlayer(p.getUUID());
                return goal == null || (player != null && !team.hasPlayer(player));
            }
            return true;
        }));

         */
    }


    @Override
    public void setTargetPos(int x, int y, int z){
        goal.setTargetPos(new BlockPos(x, y, z));
    }

    @Override
    public double x() {
        return this.getX();
    }

    @Override
    public double y() {
        return this.getY();
    }

    @Override
    public double z() {
        return this.getZ();
    }

    @Override
    public Location loc() {
        return new Location((World) this.level().getWorld(), this.getX(), this.getY(), this.getZ());
    }

    @Override
    public MobType mobType(){ return MobType.IZOMBIE; }

    @Override
    public UUID nexusUUID() {
        return nexusUUID;
    }

    @Override
    public Mob bukkitMob() {
        return (Mob) this.getBukkitEntity();
    }
}
