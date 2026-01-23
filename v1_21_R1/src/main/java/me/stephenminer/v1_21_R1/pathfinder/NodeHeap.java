package me.stephenminer.v1_21_R1.pathfinder;

import java.util.Arrays;

public class NodeHeap {
    private Node[] heap;
    private int size;

    public NodeHeap(int capacity){
        this.heap = new Node[capacity];
        this.size = 0;
    }

    public void push(Node node){
        if (size >= heap.length)
            heap = Arrays.copyOf(heap, heap.length << 1);
        heap[size] = node;
        siftUp(size++);
    }

    public Node pop(){
        Node result = heap[0];
        heap[0] = heap[--size];
        siftDown(0);
        return result;
    }

    private void siftUp(int index){
        while (index > 0){
            Node current = heap[index];
            int parent = (index - 1) >> 1;
            Node pNode = heap[parent];
            if (current.totalCost() >= pNode.totalCost()) break;
            heap[index] = pNode;
            heap[parent] = current;
            index = parent;
        }
    }

    private void siftDown(int index){
        while (true){
            int left = (index << 1) + 1;
            int right = left + 1;
            int smallest = index;
            if (left < size && heap[left].totalCost() < heap[smallest].totalCost())
                smallest = left;
            if (right < size && heap[right].totalCost() < heap[smallest].totalCost())
                smallest = right;
            if (smallest == index) break;
            Node current = heap[index];
            heap[index] = heap[smallest];
            heap[smallest] = current;
            index = smallest;

        }
    }

    public int size(){ return size; }
    public boolean empty(){ return size == 0; }
}
