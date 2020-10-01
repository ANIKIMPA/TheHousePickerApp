package com.design2net.the_house.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.widget.NumberPicker;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.List;
import java.util.Objects;

public class Producto implements NumberPicker.OnValueChangeListener, Parcelable {
    private String sku;
    private String upcStr;
    private List<String> upcs;
    private String description;
    private String aisle;
    private int pickQty;
    private String imageUrl;
    private int checkQty;
    private int orderQty;
    private String pickerUser;
    private String adminComments;
    private int notAvailable;
    private String notAvailableReason;
    private String newAisle;
    private String comments;
    private String substitute;
    private int waitingSubstitute;
    private String dateTimePicked;

    public Producto(String sku) {
        this.sku = sku;
    }

    public Producto(String sku, String upcStr, List<String> upcs, String description, String imageUrl) {
        this.sku = sku;
        this.upcStr = upcStr;
        this.upcs = upcs;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public Producto(String description, int pickQty) {
        this.description = description;
        this.pickQty = pickQty;
    }

    public Producto(String sku, String upcStr, List<String> upcs, String description, int pickQty, String imageUrl, int checkQty, int orderQty) {
        this(description, pickQty);
        this.sku = sku;
        this.upcStr = upcStr;
        this.upcs = upcs;
        this.imageUrl = imageUrl;
        this.checkQty = checkQty;
        this.orderQty = orderQty;
    }

    public Producto(String sku, String upcStr, List<String> upcs, String description, String aisle, int pickQty, String imageUrl, int checkQty, int orderQty, String pickerUser, String adminComments, int notAvailable, String notAvailableReason, String newAisle, String comments, String substitute, int waitingSubstitute) {
        this(sku, upcStr, upcs, description, pickQty, imageUrl, checkQty, orderQty);
        this.aisle = aisle;
        this.pickerUser = pickerUser;
        this.adminComments = adminComments;
        this.notAvailable = notAvailable;
        this.notAvailableReason = notAvailableReason;
        this.newAisle = newAisle;
        this.comments = comments;
        this.substitute = substitute;
        this.waitingSubstitute = waitingSubstitute;
    }

    protected Producto(Parcel in) {
        sku = in.readString();
        upcStr = in.readString();
        upcs = in.createStringArrayList();
        description = in.readString();
        aisle = in.readString();
        pickQty = in.readInt();
        imageUrl = in.readString();
        checkQty = in.readInt();
        orderQty = in.readInt();
        pickerUser = in.readString();
        adminComments = in.readString();
        notAvailable = in.readInt();
        notAvailableReason = in.readString();
        newAisle = in.readString();
        comments = in.readString();
        substitute = in.readString();
        waitingSubstitute = in.readInt();
    }

    public int getOrderQty() {
        return orderQty;
    }

    public void setCheckQty(int checkQty) {
        this.checkQty = checkQty;
    }

    // Getters
    public String getSku() { return sku; }
    public List<String> getUpcs() { return upcs; }
    public String getDescription() { return description; }
    public int getPickQty() { return pickQty; }
    public String getImageUrl() { return imageUrl; }
    public int getCheckQty() { return checkQty; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Producto)) return false;
        Producto product = (Producto) o;
        return getSku().equals(product.getSku());
    }

    public void check() {
        this.checkQty++;
    }

    public boolean isVerified() {
        return checkQty >= pickQty;
    }
    public boolean isCorrect() { return checkQty == pickQty; }
    public boolean isChecked() { return checkQty >= 1; }
    public boolean isPicked() { return !pickerUser.isEmpty(); }
    public boolean isNotPicked() { return pickerUser.isEmpty(); }

    public boolean isPendiente() {
        return this.pickerUser.isEmpty() && this.notAvailable == 0;
    }

    public boolean isListo() {
        return this.isPicked() && this.notAvailable == 0;
    }

    public boolean isEnEspera() {
        return waitingSubstitute == 1;
    }

    public boolean isNoDisponible() {
        return this.isPicked() && this.notAvailable == 1;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSku());
    }

    @NotNull
    @Override
    public String toString() {
        return "'" + description + "'";
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

    }

    public String getPickerUser() {
        return pickerUser;
    }

    public void setPickerUser(String pickerUser) {
        this.pickerUser = pickerUser;
    }

    public String getAdminComments() {
        return adminComments;
    }

    public void setAdminComments(String adminComments) {
        this.adminComments = adminComments;
    }

    public String getAisle() {
        return aisle;
    }

    public void setPickQty(int pickQty) {
        this.pickQty = pickQty;
    }

    public String getUpcStr() {
        return upcStr;
    }

    public int getNotAvailable() {
        return notAvailable;
    }

    public void setNotAvailable(int notAvailable) {
        this.notAvailable = notAvailable;
    }

    public String getNewAisle() {
        return newAisle;
    }

    public void setNewAisle(String newAisle) {
        this.newAisle = newAisle;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public void setUpcStr(String upcStr) {
        this.upcStr = upcStr;
    }

    public void setUpcs(List<String> upcs) {
        this.upcs = upcs;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAisle(String aisle) {
        this.aisle = aisle;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setOrderQty(int orderQty) {
        this.orderQty = orderQty;
    }

    public String getNotAvailableReason() {
        return notAvailableReason;
    }

    public void setNotAvailableReason(String notAvailableReason) {
        this.notAvailableReason = notAvailableReason;
    }

    public void setSubstitute(String substitute) {
        this.substitute = substitute;
    }

    public int getWaitingSubstitute() {
        return waitingSubstitute;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getSubstitute() {
        return substitute;
    }

    public void setWaitingSubstitute(int waitingSubstitute) {
        this.waitingSubstitute = waitingSubstitute;
    }

    public String getDateTimePicked() {
        return dateTimePicked;
    }

    public void setDateTimePicked(String dateTimePicked) {
        this.dateTimePicked = dateTimePicked;
    }

    public static final Creator<Producto> CREATOR = new Creator<Producto>() {
        @Override
        public Producto createFromParcel(Parcel in) {
            return new Producto(in);
        }

        @Override
        public Producto[] newArray(int size) {
            return new Producto[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(sku);
        parcel.writeString(upcStr);
        parcel.writeStringList(upcs);
        parcel.writeString(description);
        parcel.writeString(aisle);
        parcel.writeInt(pickQty);
        parcel.writeString(imageUrl);
        parcel.writeInt(checkQty);
        parcel.writeInt(orderQty);
        parcel.writeString(pickerUser);
        parcel.writeString(adminComments);
        parcel.writeInt(notAvailable);
        parcel.writeString(notAvailableReason);
        parcel.writeString(newAisle);
        parcel.writeString(comments);
        parcel.writeString(substitute);
        parcel.writeInt(waitingSubstitute);
    }
}
