package me.stephenminer.invasion.entity;

import me.stephenminer.invasion.nexus.Nexus;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public enum MobType {
    IZOMBIE("IZombie");


    private MobType(String id){
        this.id = id;
    }

    private final String id;
    public String id(){ return id; }

    public static InvasionMob instance(MobType type, Location loc, Nexus target){
        InvasionMob mob;
        String packageName = "me.stephenminer";
        String ver = Bukkit.getServer().getBukkitVersion();
        ver = ver.substring(0, ver.indexOf("-"));
        try{
            switch (ver){
                case "1.21":
                    mob =  (InvasionMob) Class.forName(packageName + ".v1_21_R1." + type.id).getConstructor(Location.class, float.class).newInstance(loc, 20);
                    break;
                default:
                    mob = null;
            }
        }catch (Exception e){
            e.printStackTrace();
            mob = null;
        }
        if (mob != null) mob.setTargetPos((int) target.loc().getX(), (int) target.loc().getY(), (int) target.loc().getZ());
        return mob;
    }
}
