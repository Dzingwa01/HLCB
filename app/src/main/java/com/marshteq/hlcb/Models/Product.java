package com.marshteq.hlcb.Models;

public class Product {
    public String id,product_name;
    public String price,total_value;
    public int quantity;
    public String description,barcode,product_image_url;
    public ProductCategory product_category;

    public Product(String id, String barcode, String product_name, String description, String price, int quantity,String total_value,ProductCategory category,String product_image_url){
        this.id = id;
        this.product_name = product_name;
        this.price = price;
        this.quantity = quantity;
        this.description = description;
        this.barcode = barcode;
        this.product_category = category;
        this.product_image_url = product_image_url;
        this.total_value = total_value;

    }

}
