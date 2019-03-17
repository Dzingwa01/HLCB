package com.marshteq.hlcb.Models;

public class ProductCategory {
    public String id;
    public String category_name,description;

    public ProductCategory(String id, String category_name, String description){
        this.id = id;
        this.category_name = category_name;
        this.description = description;
    }
}
