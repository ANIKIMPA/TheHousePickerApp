package com.design2net.the_house.models;

import android.annotation.SuppressLint;

import java.util.ArrayList;
import java.util.Objects;

public class Bag {

    public static Bag selected = null;
    private static int count = 1;

    private String id;
    private ArrayList<Producto> productos = new ArrayList<>();
    private int checkQty;
    private int currentQty;
    private int binNumber;
    private String type;

    @SuppressLint("DefaultLocale")
    public Bag(int binNumber) {
        this.id = String.format("%03d", count++);
        this.binNumber = binNumber;
    }

    public Bag(String id, int checkQty, int binNumber) {
        this.id = id;
        count = Integer.parseInt(id) + 1;
        this.checkQty = checkQty;
        this.currentQty = checkQty;
        this.binNumber = binNumber;
    }

    // Reset the bag binNumber count.
    public static void restartCount() {
        count = 1;
    }

    public void addItem(Producto producto) {
        productos.add(producto);
        checkQty++;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getBinNumber() {
        return binNumber;
    }

    public void setBinNumber(int binNumber) {
        this.binNumber = binNumber;
    }

    public void setCurrentQty(int currentQty) {
        this.currentQty = currentQty;
    }

    public int getCurrentQty() {
        return currentQty;
    }

    public int getCheckQty() {
        return checkQty;
    }
    public String getId() {
        return this.id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bag bag = (Bag) o;
        return id.equals(bag.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}