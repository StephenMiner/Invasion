package me.stephenminer.invasion.entity;

import me.stephenminer.invasion.Invasion;
import me.stephenminer.invasion.nexus.Nexus;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Mob;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.nio.ByteBuffer;
import java.util.UUID;

public enum MobType {

    IZOMBIE("IZombie");


    private MobType(String id){
        this.id = id;
    }

    private final String id;
    public String id(){ return id; }


    public static InvasionMob copy(Mob mob, UUID uuid){
        PersistentDataContainer container = mob.getPersistentDataContainer();
        if (!container.has(InvasionMob.MOB_TYPE_KEY, PersistentDataType.STRING)) return null;
        String raw = container.get(InvasionMob.MOB_TYPE_KEY, PersistentDataType.STRING);
        try {
            MobType mobType = MobType.valueOf(raw);
            InvasionMob invasionMob = instance(mobType, mob.getLocation(), uuid);
            Mob entity = invasionMob.bukkitMob();
            EntityEquipment ogEquipment = mob.getEquipment();
            EntityEquipment equipment = entity.getEquipment();
            equipment.setArmorContents(ogEquipment.getArmorContents());
            equipment.setItemInMainHand(ogEquipment.getItemInMainHand());
            equipment.setItemInOffHand(ogEquipment.getItemInOffHand());
            entity.setHealth(mob.getHealth());
            entity.addPotionEffects(mob.getActivePotionEffects());
            entity.getPersistentDataContainer().set(InvasionMob.MOB_TYPE_KEY, PersistentDataType.STRING,invasionMob.mobType().name());
            ByteBuffer buff = ByteBuffer.wrap(new byte[16]);
            buff.putLong(uuid.getMostSignificantBits());
            buff.putLong(uuid.getLeastSignificantBits());
            entity.getPersistentDataContainer().set(InvasionMob.NEXUS_KEY, PersistentDataType.BYTE_ARRAY, buff.array());
            mob.remove();
            return invasionMob;
        }catch (Exception e){
            return null;
        }
    }

    public static InvasionMob instance(MobType type, Location loc, Nexus target){
        return MobType.instance(type, loc, target.uuid());
    }

    public static InvasionMob instance(MobType type, Location loc, UUID uuid){
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
        if (mob != null){
            if (Invasion.nexusMap.containsKey(uuid)) {
              //  Nexus target = Invasion.nexusMap.get(uuid);
               // mob.setTargetPos((int) target.loc().getX(), (int) target.loc().getY(), (int) target.loc().getZ());
            }
        }
        return mob;
    }
}
