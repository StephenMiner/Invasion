package me.stephenminer.invasion.listener;

import me.stephenminer.invasion.Invasion;
import me.stephenminer.invasion.entity.InvasionMob;
import me.stephenminer.invasion.entity.MobType;
import me.stephenminer.invasion.nexus.Nexus;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Mob;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

public class NexusListener implements Listener {
    private final Invasion plugin;
    public final Map<BlockKey, Nexus> nexusMap;

    public NexusListener(){
        this.plugin = JavaPlugin.getPlugin(Invasion.class);
        this.nexusMap = new HashMap<>();
    }

    @EventHandler
    public void placeNexus(BlockPlaceEvent event){
        ItemStack item = event.getItemInHand();
        if (!item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (!container.has(Nexus.ITEM_KEY, PersistentDataType.STRING)) return;
        Nexus nexus = new Nexus(event.getBlock().getLocation(), 200);
        BlockKey key = new BlockKey(event.getBlock().getLocation());
        nexusMap.put(key, nexus);
        writeAdditionalPos(event.getBlock().getChunk(), event.getBlock().getLocation(), nexus);
    }

    @EventHandler
    public void loadNexus(ChunkLoadEvent event){
         readChunkData(event.getChunk());
    }

    @EventHandler
    public void chatTest(AsyncPlayerChatEvent event){
        for (Nexus nexus : nexusMap.values()){
            Bukkit.getScheduler().runTaskLater(plugin, ()->{
                            nexus.testSpawn(event.getPlayer().getLocation());
                        }, 1);
        }
    }


    public void writeAdditionalPos(Chunk chunk, Location loc, Nexus nexus){
        PersistentDataContainer container = chunk.getPersistentDataContainer();
        byte[] posArr = null;
        if (container.has(Nexus.POS_KEY, PersistentDataType.BYTE_ARRAY))
            posArr = container.get(Nexus.POS_KEY, PersistentDataType.BYTE_ARRAY);
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        try {
            if (posArr != null)
                outStream.write(posArr);
            int pos = packPosition(loc);
            writePos(outStream, pos);
        }catch (IOException e){
            e.printStackTrace();
        }
        if (nexus != null)
            outStream.write(nexus.catalyst().encoding());
        ByteBuffer buff = ByteBuffer.wrap(new byte[20]);
        buff.putInt(nexus.health());
        UUID uuid = nexus.uuid();
        buff.putLong(uuid.getMostSignificantBits());
        buff.putLong(uuid.getLeastSignificantBits());
        try {
            outStream.write(buff.array());
        }catch (IOException e){ e.printStackTrace(); }
        container.set(Nexus.POS_KEY, PersistentDataType.BYTE_ARRAY, outStream.toByteArray());
    }

    public void readChunkData(Chunk chunk){
        PersistentDataContainer container = chunk.getPersistentDataContainer();
        if (!container.has(Nexus.POS_KEY, PersistentDataType.BYTE_ARRAY)) return;
        byte[] posArr = container.get(Nexus.POS_KEY, PersistentDataType.BYTE_ARRAY);
        ByteArrayInputStream inStream = new ByteArrayInputStream(posArr);
        byte[] uuidPortion = Arrays.copyOfRange(posArr,4, 19);
        while (inStream.available() > 23){ // 3 for pos, 1 for cat, 4 for hp, 16 for UUID 24 total
            int d1 = inStream.read();
            int d2 = inStream.read();
            int d3 = inStream.read();
            int catType = inStream.read();
            int data = (d1 << 16) | (d2 << 8) | d3;
            Block b = unpackPosition(chunk, data);
            BlockKey key = new BlockKey(b.getX(), b.getY(), b.getZ());
            if (!nexusMap.containsKey(key)) {
                byte[] complexBytes = new byte[20];
                for (int i = 0; i < 16; i++){
                    // since we are within #isAvailable check, this should be a safe cast
                    complexBytes[i] = (byte) inStream.read();
                }
                ByteBuffer buff = ByteBuffer.wrap(complexBytes);
                int hp = buff.getInt();
                long high = buff.getLong();
                long low = buff.getLong();
                UUID uuid = new UUID(high, low);
                Nexus nexus = new Nexus(b.getLocation(), 200, hp, uuid);
                if (catType != 0){
                    Nexus.Catalyst cat = Nexus.Catalyst.fromByte((byte) catType);
                    if (cat != null)
                        nexus.setCatalyst(cat);
                }
                nexusMap.put(key, nexus);
            }

        }

    }


    private void writePos(ByteArrayOutputStream outStream, int pos){
        outStream.write(pos >>> 16 & 0xFF);
        outStream.write(pos >>> 8 & 0xFF);
        outStream.write(pos & 0xFF);
    }

    private int packPosition(Location loc){
        int x = loc.getBlockX() & 0xF;
        int z = loc.getBlockZ() & 0xF;
        return loc.getBlockY() << 8 | x << 4 | z;
    }

    private Block unpackPosition(Chunk chunk, int pos) {
        int y = pos >> 8;
        int x = (pos >> 4) & 0xF;
        int z = pos & 0xF;
        return chunk.getBlock(x, y, z);
    }


    public class BlockKey{
        private final int x, y, z;

        public BlockKey(Location loc){
            this(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        }
        public BlockKey(int x, int y, int z){
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public int x(){ return x; }
        public int y(){ return y; }
        public int z(){ return z; }

        @Override
        public int hashCode(){
            return Objects.hash(x, y, z);
        }

        @Override
        public boolean equals(Object other){
            if (other instanceof BlockKey){
                BlockKey key = (BlockKey) other;
                return this.x == key.x && this.y == key.y && this.z == key.z;
            }
            return false;
        }


        @EventHandler
        public void restoreInvasionEntities(EntitiesLoadEvent event){
            List<Entity> entities = event.getEntities();
            for (Entity entity : entities){
                if (entity instanceof Mob){
                    Mob mob = (Mob) entity;
                    PersistentDataContainer container = mob.getPersistentDataContainer();
                    if (!container.has(InvasionMob.NEXUS_KEY, PersistentDataType.BYTE_ARRAY)) continue;
                    byte[] uuidBytes = container.get(InvasionMob.NEXUS_KEY, PersistentDataType.BYTE_ARRAY);
                    ByteBuffer buff = ByteBuffer.wrap(uuidBytes);
                    UUID uuid = new UUID(buff.getLong(), buff.getLong());
                    InvasionMob invasionMob = MobType.copy(mob, uuid);
                }
                    
            }
        }
    }

}
