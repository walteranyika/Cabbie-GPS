package io.cabie.cabbie;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import mehdi.sakout.fancybuttons.FancyButton;

public class DriverLoginRegisterActivity extends AppCompatActivity {
    FancyButton btnLogin, btnSignUp;
    TextView txtTitle, txtCreateAccount;
    MaterialEditText inputEmail, inputPassword, inputNames,inputPhone;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    SpotsDialog progressDialog;
    DatabaseReference driversRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login_register);
        btnLogin = findViewById(R.id.buttonSignIn);
        btnSignUp = findViewById(R.id.buttonSignUp);
        txtTitle = findViewById(R.id.txt_title);
        txtCreateAccount = findViewById(R.id.txtRegister);
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        inputNames = findViewById(R.id.inputNames);
        inputPhone = findViewById(R.id.inputPhone);
        progressDialog = new SpotsDialog(this);
        driversRef= FirebaseDatabase.getInstance().getReference().child("Users/Drivers");
    }

    public void login(View view) {
        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Empty fields!!", Toast.LENGTH_SHORT).show();
            return;
        }
        progressDialog.show();
        mAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                // Toast.makeText(DriverLoginRegisterActivity.this, "Login success", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                startActivity(new Intent(DriverLoginRegisterActivity.this, DriverMapActivity.class));
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(DriverLoginRegisterActivity.this, "Wrong username or passord", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void sign_up(View view) {

        final String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();
        final String names = inputNames.getText().toString().trim();
        final String phone = inputPhone.getText().toString().trim();
        if (email.isEmpty() || password.isEmpty() || names.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Empty fields!!", Toast.LENGTH_SHORT).show();
            return;
        }
        progressDialog.show();
        mAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                progressDialog.dismiss();
                Map<String,String> map=new HashMap<>();
                map.put("names",names);
                map.put("email",email);
                map.put("phone",phone);
                driversRef.child(mAuth.getCurrentUser().getUid()).setValue(map);
                //Toast.makeText(DriverLoginRegisterActivity.this, "Success", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(DriverLoginRegisterActivity.this, DriverMapActivity.class));
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(DriverLoginRegisterActivity.this, "Failed To register", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void no_account(View view) {
        btnLogin.setVisibility(View.GONE);
        btnSignUp.setVisibility(View.VISIBLE);
        txtTitle.setText("Driver SignUp");
        txtCreateAccount.setVisibility(View.GONE);
        inputNames.setVisibility(View.VISIBLE);
        inputPhone.setVisibility(View.VISIBLE);
    }
}
