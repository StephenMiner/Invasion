package me.stephenminer.invasion.entity;

import me.stephenminer.invasion.nexus.Nexus;
import org.bukkit.Location;
import org.bukkit.entity.Mob;

public interface InvasionMob {

    public double x();
    public double y();
    public double z();
    public Location loc();
    public Mob bukkitMob();

    public void setTargetPos(int x, int y, int z);
}
