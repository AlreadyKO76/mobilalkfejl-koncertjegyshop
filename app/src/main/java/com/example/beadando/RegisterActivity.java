package com.example.beadando;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

@RequiresApi(api = Build.VERSION_CODES.S)
public class RegisterActivity extends AppCompatActivity {
    private static final String LOG_TAG= RegisterActivity.class.getName();
    private static final String PREF_KEY= RegisterActivity.class.getPackageName().toString();
    private static final int SECRET_KEY= 88;
    private SharedPreferences preferences;

    private FirebaseAuth mAuth;


    EditText userNameEditText;
    EditText userEmailEditText;
    EditText userPasswordEditText;
    EditText userPasswordAgainEditText;
    EditText phoneEditText;
    EditText addressEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Log.i(LOG_TAG, "onCreate");

        int secret_key= getIntent().getIntExtra("SECRET_KEY", 0);

        if (secret_key != 88){
            finish();
        }



        userNameEditText = findViewById(R.id.userNameEditText);
        userEmailEditText = findViewById(R.id.userEmailEditText);
        userPasswordEditText = findViewById(R.id.userPasswordEditText);
        userPasswordAgainEditText = findViewById(R.id.userPasswordAgainEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        addressEditText = findViewById(R.id.addressEditText);


        preferences = getSharedPreferences(PREF_KEY, MODE_PRIVATE);

        String userName = preferences.getString("userName", "");
        String password = preferences.getString("password", "");

        userNameEditText.setText(userName);
        userPasswordEditText.setText(password);


        mAuth = FirebaseAuth.getInstance();
    }

    public void register(View view) {
        String userName= userNameEditText.getText().toString();
        String email= userEmailEditText.getText().toString();
        String password= userPasswordEditText.getText().toString();
        String passwordAgain= userPasswordAgainEditText.getText().toString();

        if (!password.equals(passwordAgain)){
            Log.e(LOG_TAG, "Nem egyenlő a jelszó és a megerősítése.");
            return;
        }

        String phoneNumber= phoneEditText.getText().toString();
        String address = addressEditText.getText().toString();

        Log.i(LOG_TAG, "Regisztrált: " + userName + ", e-mail: "+ email);


        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    Log.d(LOG_TAG, "User Created Succesfully");
                    startShop();
                } else {
                    Log.d(LOG_TAG, "User wasn't created successfully");
                    Toast.makeText(RegisterActivity.this, "User wasn't created successfully: " + task.getException().getMessage(),Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    public void cancel(View view) {
        finish();
    }

    private void startShop(){
        Intent intent = new Intent(this, KoncerjegyVasarlasActivity.class);

        startActivity(intent);


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "onDestroy");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(LOG_TAG, "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(LOG_TAG, "onStop");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(LOG_TAG, "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(LOG_TAG, "onResume");
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(LOG_TAG, "onRestart");
    }

}