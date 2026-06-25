package com.example.afyagro.models;

/**
 * Représente une commande / livraison d'un produit.
 *
 * Le cycle de vie du statut est volontairement simple :
 *  - "pending"   : commande passée par l'acheteur, en attente de confirmation par l'admin.
 *  - "confirmed" : un admin a confirmé la date et l'heure de livraison.
 *  - "delayed"   : un admin a signalé un retard (voir {@link #getDelayNote()}).
 *  - "delivered" : la livraison a été effectuée.
 *
 * Seul un administrateur (account_type == 3) peut modifier la date, l'heure,
 * le statut et la note de retard.
 */
public class Delivery {

    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_CONFIRMED = "confirmed";
    public static final String STATUS_DELAYED = "delayed";
    public static final String STATUS_DELIVERED = "delivered";

    private String itemId;
    private String itemName;
    private String buyerId;
    private String buyerName;
    private String sellerId;
    private String sellerName;
    private String status;
    private String deliveryDate;
    private String deliveryTime;
    private String delayNote;
    private long createdAt;

    private String uid;

    public Delivery() {
    }

    public Delivery(String itemId, String itemName, String buyerId, String buyerName,
                    String sellerId, String sellerName, String status,
                    String deliveryDate, String deliveryTime, String delayNote, long createdAt) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.buyerId = buyerId;
        this.buyerName = buyerName;
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.status = status;
        this.deliveryDate = deliveryDate;
        this.deliveryTime = deliveryTime;
        this.delayNote = delayNote;
        this.createdAt = createdAt;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(String deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public String getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(String deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public String getDelayNote() {
        return delayNote;
    }

    public void setDelayNote(String delayNote) {
        this.delayNote = delayNote;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
