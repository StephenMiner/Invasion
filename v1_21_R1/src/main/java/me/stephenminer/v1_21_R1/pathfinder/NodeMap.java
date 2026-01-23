package me.stephenminer.v1_21_R1.pathfinder;

import org.bukkit.block.Block;
import org.checkerframework.checker.units.qual.N;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class NodeMap {
    private long[] keys;
    private Node[] vals;

    private int capacity, size;

    public NodeMap(int capacity){
        this.capacity = capacity;
        keys = new long[capacity];
        vals = new Node[capacity];
        Arrays.fill(keys, Long.MIN_VALUE);
        this.size = 0;
    }

    private int hash(long key){
        return Long.hashCode(key) & (capacity - 1);
    }

    public Node get(long key){
        int index = hash(key);
        while (this.keys[index] != Long.MIN_VALUE){
            if (keys[index] == key) return vals[index];
            index = (index + 1) & (capacity - 1);
        }
        return null;
    }

    public void put(long key, Node value){
        int index = hash(key);
        if (size > capacity * 0.6)
            resize();
        while (keys[index] != Long.MIN_VALUE){
            if (keys[index] == key) {
                vals[index] = value;
                return;
            }
            index = (index + 1) & (capacity - 1);
        }
        keys[index] = key;
        vals[index] = value;
        size++;
    }

    private void resize(){
        int capacity = this.capacity * 2;
        long[] keys = new long[capacity];
        Node[] vals = new Node[capacity];
        Arrays.fill(keys, Long.MIN_VALUE);
        for (int i = 0; i < this.capacity; i++){
            if (this.keys[i] != Long.MIN_VALUE){
                long key = this.keys[i];
                Node node = this.vals[i];
                int index = Long.hashCode(key) & (capacity - 1);
                while (keys[index] != Long.MIN_VALUE){
                    index = (index + 1) & (capacity - 1);
                }
                keys[index] = key;
                vals[index] = node;
            }
        }
        this.vals = vals;
        this.keys = keys;
        this.capacity = capacity;
    }

    public int size(){ return size; }
    public Collection<Node> values(){
        List<Node> values = new ArrayList<>();
        for (int i = 0; i < this.keys.length; i++){
            if (keys[i] != Long.MIN_VALUE) values.add(vals[i]);
        }
        return values;
    }
}
