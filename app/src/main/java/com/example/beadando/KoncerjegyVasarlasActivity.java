package com.example.beadando;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.MenuItemCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.ktx.Firebase;

import java.util.ArrayList;

public class KoncerjegyVasarlasActivity extends AppCompatActivity {
    private final static String LOG_TAG = KoncerjegyVasarlasActivity.class.getName();
    private FirebaseUser user;

    private RecyclerView mRecyclerView;
    private ArrayList<koncertItem> mItemList;
    private koncertItemAdapter mAdapter;
    private int gridNumber=1;
    private int queryLimit=8;

    private FrameLayout redCircle;
    private TextView contentTextView;
    private int cartItems=0;

    private FirebaseFirestore mFirestore;
    private CollectionReference mItems;
    private boolean isLoggingOut=false;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        FirebaseApp.initializeApp(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_koncerjegy_vasarlas);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){
            Log.d(LOG_TAG, "Authenticated user");
        }else {
            Log.d(LOG_TAG, "Unauthenticated user");
            finish();
        }
        mRecyclerView= findViewById(R.id.concertRecyclerView);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, gridNumber));

        mItemList=new ArrayList<>();

        mAdapter=new koncertItemAdapter(this, mItemList);
        mRecyclerView.setAdapter(mAdapter);


        mFirestore= FirebaseFirestore.getInstance();
        mItems= mFirestore.collection("Items");

        queryData();


        IntentFilter filter=new IntentFilter();
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        this.registerReceiver(powerReceiver,filter);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

    }

    BroadcastReceiver powerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action==null){
                return;
            }

            switch (action){
                case Intent.ACTION_POWER_CONNECTED:
                    queryLimit=8;
                    break;
                case Intent.ACTION_POWER_DISCONNECTED:
                    queryLimit=4;
                    break;
            }
            queryData();
        }
    };
    private void queryData(){
        mItemList.clear();

        mItems.orderBy("name", Query.Direction.ASCENDING).limit(queryLimit).get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot document : queryDocumentSnapshots){
                koncertItem item= document.toObject(koncertItem.class);
                mItemList.add(item);
            }
            if (mItemList.size()==0){
                initializeData();
                queryData();
            }

            mAdapter.notifyDataSetChanged();
        });
    }

    private void initializeData() {
        String[] ItemsList = getResources().getStringArray(R.array.concert_names);
        String[] ItemsPrice= getResources().getStringArray(R.array.concert_prices);
        String[] ItemsHelyszin= getResources().getStringArray(R.array.concert_locations);
        String[] ItemsDate= getResources().getStringArray(R.array.concert_dates);
        TypedArray itemsImageResource=getResources().obtainTypedArray(R.array.concert_images);

        for (int i = 0; i < ItemsList.length; i++) {
            mItems.add(new koncertItem(
                    ItemsDate[i],
                    ItemsHelyszin[i],
                    ItemsList[i],
                    ItemsPrice[i],
                    itemsImageResource.getResourceId(i, 0)));

            
        }
        itemsImageResource.recycle();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.list_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {return false;}

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(LOG_TAG, newText);
                mAdapter.getFilter().filter(newText);
                return false;
            }
        });
        MenuItem cartItem = menu.findItem(R.id.cart);
        View actionView = MenuItemCompat.getActionView(cartItem);
        redCircle = actionView.findViewById(R.id.view_alert_red_circle);
        contentTextView = actionView.findViewById(R.id.view_alert_count_textview);

        actionView.setOnClickListener(v -> onOptionsItemSelected(cartItem));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.log_out) {
            Log.d(LOG_TAG, "log out clicked");
            isLoggingOut = true;
            deleteCartContents(() -> {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            });
            return true;
        } else if (id == R.id.setting) {
            Log.d(LOG_TAG, "settings clicked");
            return true;
        }else if (id == R.id.cart) {
            Log.d(LOG_TAG, "cart clicked");
            Intent intent = new Intent(this, CartActivity.class);


            startActivity(intent);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final MenuItem alertMenuItem = menu.findItem(R.id.cart);
        FrameLayout rootView=(FrameLayout) alertMenuItem.getActionView();

        redCircle =(FrameLayout) rootView.findViewById(R.id.view_alert_red_circle);
        contentTextView = (TextView) rootView.findViewById(R.id.view_alert_count_textview);

        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionsItemSelected(alertMenuItem);
            }
        });

        return super.onPrepareOptionsMenu(menu);
    }

    public void updateAlertIcon(){
        cartItems=(cartItems+1);
        if (contentTextView != null){
            contentTextView.setText(String.valueOf(cartItems));
            redCircle.setVisibility(View.VISIBLE);
        }else {
            Log.w(LOG_TAG, "contentTextView is null when updating cart icon!");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(powerReceiver);
    }
    private void deleteCartContents(Runnable onComplete) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            if (onComplete != null) onComplete.run();
            return;
        }

        db.collection("Cart")
                .document(currentUser.getUid())
                .collection("Items")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        doc.getReference().delete();
                    }
                    Log.d(LOG_TAG, "Kosár tartalma törölve.");
                    if (onComplete != null) onComplete.run();
                })
                .addOnFailureListener(e -> {
                    Log.e(LOG_TAG, "Hiba történt a kosár törlésekor: " + e.getMessage());
                    if (onComplete != null) onComplete.run();
                });
    }

}
