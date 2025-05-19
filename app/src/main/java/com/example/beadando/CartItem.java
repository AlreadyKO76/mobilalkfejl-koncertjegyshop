package com.example.beadando;

public class CartItem {
    private String name, price, helyszin, date;
    private String documentId;
    private int quantity;

    public CartItem() {}

    public CartItem(String name, String price, String helyszin, String date, String documentId, int quantity) {
        this.name = name;
        this.price = price;
        this.helyszin = helyszin;
        this.date = date;
        this.documentId = documentId;
        this.quantity=quantity;
    }

    public String getName() { return name; }
    public String getPrice() { return price; }
    public String getHelyszin() { return helyszin; }
    public String getDate() { return date; }
    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public int getQuantity() { return quantity; }

    public void setQuantity(int newQuantity) { this.quantity=newQuantity; }
}
