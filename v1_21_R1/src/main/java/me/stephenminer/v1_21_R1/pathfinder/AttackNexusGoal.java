package me.stephenminer.v1_21_R1.pathfinder;

import me.stephenminer.invasion.Invasion;
import me.stephenminer.invasion.entity.InvasionMob;
import me.stephenminer.invasion.nexus.Nexus;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.Listener;

import java.util.UUID;

public class AttackNexusGoal extends Goal {
    protected final Mob mob;

    protected int attackTicks;
    protected UUID nexusUUID;
    protected Nexus nexus;

    protected static final int ATTACK_COOLDOWN = 5*20;

    public AttackNexusGoal(Mob mob){
        this.mob = mob;
    }

    @Override
    public boolean canUse(){
        if (nexus == null && Invasion.nexusMap.containsKey(nexusUUID)) {
            nexus = Invasion.nexusMap.get(nexusUUID);
            System.out.println(2000);
        }
        return canAttack();
    }

    @Override
    public void start(){
        attackTicks = 0;
    }

    @Override
    public void tick(){
        System.out.println(1);
        if (canAttack()){
            System.out.println(2);
            if (attackTicks > 0)
                attackTicks--;
            else {
                System.out.println(3);
                mob.swing(InteractionHand.MAIN_HAND);
                attack();
            }
        }
    }

    private boolean canAttack(){
        // System.out.println(10);
        if (nexus == null) return false;
        Location loc = nexus.loc();
        double dist = mob.distanceToSqr(loc.getX(), loc.getY(), loc.getZ());
        System.out.println(dist);
        return dist <= 2*2;
    }

    private void attack(){
        if (nexus == null) return;
        nexus.setHealth(nexus.health() - 1);
        attackTicks = ATTACK_COOLDOWN;
        Bukkit.broadcastMessage("DAMAGED NEXUS: HP = " + nexus.health());
    }

    public UUID nexusUUID(){ return nexusUUID; }
    public void setNexusUUID(UUID nexusUUID){ this.nexusUUID = nexusUUID; }
}
