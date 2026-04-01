package me.stephenminer.v1_21_R1;

import me.stephenminer.invasion.Invasion;
import me.stephenminer.invasion.entity.InvasionMob;
import me.stephenminer.invasion.entity.MobType;
import me.stephenminer.invasion.nexus.Nexus;
import me.stephenminer.v1_21_R1.pathfinder.AttackNexusGoal;
import me.stephenminer.v1_21_R1.pathfinder.BuilderPathfinder;
import me.stephenminer.v1_21_R1.pathfinder.InvasionGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.monster.Stray;
import net.minecraft.world.entity.player.Player;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_21_R1.CraftWorld;
import org.bukkit.entity.Mob;

import java.util.Set;
import java.util.UUID;

public class BuilderStray extends Stray implements InvasionMob {
    private InvasionGoal invasionGoal;
    private AttackNexusGoal attackNexusGoal;
    private UUID nexusUUID;
    private Nexus nexus;


    public BuilderStray(Location loc, float health, UUID nexusUUID) {
        super(EntityType.STRAY, ((CraftWorld) loc.getWorld()).getHandle());
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(health);
        this.setHealth(health);
        this.setPos(loc.getX(), loc.getY(), loc.getZ());
        this.nexusUUID = nexusUUID;
        this.level().addFreshEntity(this);
        implantMobType();
        implantNexusData();
    }

    @Override
    public void tick(){
        super.tick();
        if (this.nexus == null && Invasion.nexusMap.containsKey(nexusUUID)) {
            this.nexus = Invasion.nexusMap.get(nexusUUID);
            Location loc = this.nexus.loc();
            this.setTargetPos(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        }
    }

    @Override
    public void registerGoals(){
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        attackNexusGoal = new AttackNexusGoal(this);
        this.goalSelector.addGoal(2, attackNexusGoal);
        BuilderPathfinder builderPathing = new BuilderPathfinder(this.level(),2, Set.of());
        this.invasionGoal = new InvasionGoal(this, builderPathing);
        this.goalSelector.addGoal(3, invasionGoal);
    }


    @Override
    public void setTargetPos(int x, int y, int z){
        this.invasionGoal.setTargetPos(new BlockPos(x,y, z));
    }

    @Override
    public double x(){ return this.getX(); }

    @Override
    public double y(){ return this.getY(); }

    @Override
    public double z(){ return this.getZ(); }

    @Override
    public Location loc(){
        return new Location((World) this.level().getWorld(),this.getX(), this.getY(), this.getZ());
    }

    @Override
    public MobType mobType(){ return MobType.BUILDER_STRAY; }

    @Override
    public UUID nexusUUID(){ return this.nexusUUID; }

    @Override
    public Mob bukkitMob(){ return (Mob) this.getBukkitEntity(); }


}
