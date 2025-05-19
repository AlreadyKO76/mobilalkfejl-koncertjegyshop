package com.example.beadando;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class koncertItemAdapter extends RecyclerView.Adapter<koncertItemAdapter.ViewHolder> implements Filterable {
    private ArrayList<koncertItem> mKoncertItemData;
    private ArrayList<koncertItem> mKoncertItemDataAll;
    private Context mContext;
    private int lastPosition=-1;
    koncertItemAdapter(Context context, ArrayList<koncertItem> itemData){
        this.mKoncertItemData=itemData;
        this.mKoncertItemDataAll=itemData;
        this.mContext=context;
    }

    @Override
    public koncertItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.koncert_item, parent, false));
    }

    @Override
    public void onBindViewHolder(koncertItemAdapter.ViewHolder holder, int position) {
        koncertItem currentItem= mKoncertItemData.get(position);

        holder.bindTo(currentItem);

        if (holder.getAdapterPosition() > lastPosition){
            Animation animation= AnimationUtils.loadAnimation(mContext, R.anim.slide_in_row);
            holder.itemView.startAnimation(animation);
            lastPosition=holder.getAdapterPosition();
        }

    }

    @Override
    public int getItemCount() {
        return mKoncertItemData.size();
    }

    @Override
    public Filter getFilter() {
        return koncertFilter;
    }
    private Filter koncertFilter= new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            ArrayList<koncertItem> filteredList=new ArrayList<>();
            FilterResults results= new FilterResults();

            if (charSequence == null || charSequence.length() == 0){
                results.count=mKoncertItemDataAll.size();
                results.values= mKoncertItemDataAll;
            }else{
                String filterPatern= charSequence.toString().toLowerCase().trim();

                for (koncertItem item : mKoncertItemDataAll){
                    if (item.getName().toLowerCase().contains(filterPatern)){
                        filteredList.add(item);
                    }
                }
                results.count=filteredList.size();
                results.values= filteredList;
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults results) {
            mKoncertItemData= (ArrayList) results.values;
            notifyDataSetChanged();

        }
    };
    class ViewHolder extends RecyclerView.ViewHolder{

        private TextView mTitleText;
        private TextView mPriceText;
        private TextView mHelyszin;
        private TextView mDate;
        private ImageView mImg;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mTitleText= itemView.findViewById(R.id.concertTitleTextView);
            mPriceText= itemView.findViewById(R.id.concertPriceTextView);
            mHelyszin= itemView.findViewById(R.id.concertLocationTextView);
            mDate= itemView.findViewById(R.id.concertDateTextView);
            mImg= itemView.findViewById(R.id.concertImageView);

            itemView.findViewById(R.id.buyButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("Activity", "buy button clicked");
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        koncertItem selectedItem = mKoncertItemData.get(position);

                        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                        if (currentUser != null) {
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            String koncertAzonosito = selectedItem.getName(); // vagy name + date, ha több azonos nevű koncert van
                            DocumentReference itemRef = db.collection("Cart")
                                    .document(currentUser.getUid())
                                    .collection("Items")
                                    .document(koncertAzonosito);

                            itemRef.get().addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    // Már benne van: növeljük a mennyiséget
                                    Long currentQty = documentSnapshot.getLong("quantity");
                                    if (currentQty == null) currentQty = 1L;
                                    itemRef.update("quantity", currentQty + 1)
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(mContext, "Mennyiség növelve!", Toast.LENGTH_SHORT).show();
                                                if (mContext instanceof KoncerjegyVasarlasActivity) {
                                                    ((KoncerjegyVasarlasActivity) mContext).updateAlertIcon();
                                                }
                                            });
                                } else {
                                    // Újként tesszük be quantity = 1
                                    Map<String, Object> cartItem = new HashMap<>();
                                    cartItem.put("name", selectedItem.getName());
                                    cartItem.put("price", selectedItem.getPrice());
                                    cartItem.put("helyszin", selectedItem.getHelyszin());
                                    cartItem.put("date", selectedItem.getDate());
                                    cartItem.put("quantity", 1);

                                    itemRef.set(cartItem)
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(mContext, "Hozzáadva a kosárhoz!", Toast.LENGTH_SHORT).show();
                                                if (mContext instanceof KoncerjegyVasarlasActivity) {
                                                    ((KoncerjegyVasarlasActivity) mContext).updateAlertIcon();
                                                }
                                            });
                                }
                            }).addOnFailureListener(e -> {
                                Toast.makeText(mContext, "Hiba történt!", Toast.LENGTH_SHORT).show();
                            });

                        } else {
                            Toast.makeText(mContext, "Nem vagy bejelentkezve!", Toast.LENGTH_SHORT).show();
                        }
                    }

                }
            });
        }

        public void bindTo(koncertItem currentItem) {
            mTitleText.setText(currentItem.getName());
            mPriceText.setText(currentItem.getPrice());
            mHelyszin.setText(currentItem.getHelyszin());
            mDate.setText(currentItem.getDate());

            Glide.with(mContext).load(currentItem.getImgResource()).into(mImg);
        }
    }

}
