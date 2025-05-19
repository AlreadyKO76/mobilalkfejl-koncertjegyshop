package com.example.beadando;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.beadando.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.ArrayList;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
    private Context context;
    private ArrayList<CartItem> cartList;
    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public CartAdapter(Context context, ArrayList<CartItem> cartList) {
        this.context = context;
        this.cartList = cartList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cart_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem item = cartList.get(position);

        holder.title.setText(item.getName());
        holder.price.setText(item.getPrice());
        holder.location.setText(item.getHelyszin());
        holder.date.setText(item.getDate());
        holder.quantityTextView.setText("Mennyiség: " + item.getQuantity());

        holder.deleteButton.setOnClickListener(v -> {
            if (currentUser != null && item.getDocumentId() != null) {
                db.collection("Cart")
                        .document(currentUser.getUid())
                        .collection("Items")
                        .document(item.getDocumentId())
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            cartList.remove(position);
                            notifyItemRemoved(position);
                            Toast.makeText(context, "Törölve a kosárból", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(context, "Hiba történt!", Toast.LENGTH_SHORT).show()
                        );
            }
        });
        holder.plusButton.setOnClickListener(v -> {
            Animation bounce = AnimationUtils.loadAnimation(context, R.anim.bounce_card);
            holder.itemView.startAnimation(bounce);
            int newQuantity = item.getQuantity() + 1;
            updateQuantity(item, newQuantity, holder);
        });

        holder.minusButton.setOnClickListener(v -> {

            int newQuantity = item.getQuantity() - 1;
            if (newQuantity > 0) {
                Animation bounce = AnimationUtils.loadAnimation(context, R.anim.bounce_card);
                holder.itemView.startAnimation(bounce);
                updateQuantity(item, newQuantity, holder);
            } else {
                Toast.makeText(context, "A mennyiség nem lehet 0", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateQuantity(CartItem item, int newQuantity, ViewHolder holder) {
        if (currentUser != null && item.getDocumentId() != null) {
            db.collection("Cart")
                    .document(currentUser.getUid())
                    .collection("Items")
                    .document(item.getDocumentId())
                    .update("quantity", newQuantity)
                    .addOnSuccessListener(aVoid -> {
                        item.setQuantity(newQuantity);
                        holder.quantityTextView.setText("Mennyiség: " + newQuantity);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Nem sikerült frissíteni a mennyiséget", Toast.LENGTH_SHORT).show();
                    });
        }
    }


    @Override
    public int getItemCount() {
        return cartList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, price, location, date, quantityTextView;
        Button deleteButton,plusButton, minusButton;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.cartTitleTextView);
            price = itemView.findViewById(R.id.cartPriceTextView);
            location = itemView.findViewById(R.id.cartLocationTextView);
            date = itemView.findViewById(R.id.cartDateTextView);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            quantityTextView = itemView.findViewById(R.id.quantityTextView);
            plusButton = itemView.findViewById(R.id.plusButton);
            minusButton = itemView.findViewById(R.id.minusButton);

        }
    }
}
