package com.marshteq.hlcb.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.iamhabib.easy_preference.EasyPreference;
import com.marshteq.hlcb.Helpers.Credentials;
import com.marshteq.hlcb.Models.Product;
import com.marshteq.hlcb.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AssignedStockAdapter extends RecyclerView.Adapter<AssignedStockAdapter.ViewHolder> {

    private final List<Product> products;
    Context context;
    String Base_URL;

    public AssignedStockAdapter(List<Product> products, Context context){
        this.products = products;
        this.context = context;
        Credentials credentials = EasyPreference.with(context).getObject("server_details", Credentials.class);
        Base_URL = credentials.server_url;
    }

    @Override
    public AssignedStockAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.assigned_products_layout,parent,false);
        return new ViewHolder(itemView);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView product_name,  product_category, price, quantity, barcode,total_value;
        ImageView thumbnail;

        public ViewHolder(View itemView) {
            super(itemView);
            product_name= itemView.findViewById(R.id.product_name);
            product_category = itemView.findViewById(R.id.product_category);
            price = itemView.findViewById(R.id.price);
            quantity = itemView.findViewById(R.id.quantity);
            barcode = itemView.findViewById(R.id.barcode);
            total_value = itemView.findViewById(R.id.total_value);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

        }
    }

        @Override
        public void onBindViewHolder(AssignedStockAdapter.ViewHolder holder, int position) {
            Product product = products.get(position);
            if(product.id!="empty"){
                holder.product_name.setText(product.product_name);
                holder.product_category.setText("Category: "+product.product_category.category_name);
                holder.price.setText("Total Value: "+product.total_value);
                holder.barcode.setText("Barcode: "+product.barcode);
                holder.price.setText("Unit Price: R"+product.price);
                holder.quantity.setText("Quantity: "+ product.quantity);
                holder.total_value.setText("Total Value: R "+product.total_value);
                Picasso.get().load(Base_URL+"storage/"+product.product_image_url).into(holder.thumbnail);

            }else{
                holder.product_name.setText(product.description);
            }
        }



    }
