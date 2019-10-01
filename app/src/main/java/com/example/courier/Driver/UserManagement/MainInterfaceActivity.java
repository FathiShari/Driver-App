package com.example.courier.Driver.UserManagement;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.courier.HomeActivity;
import com.example.courier.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainInterfaceActivity extends AppCompatActivity  {

    Button signIn, signUp;
    EditText email,password;
    FirebaseAuth auth;
    FirebaseDatabase database;
    DatabaseReference users;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_interface);
        signIn = (Button)findViewById(R.id.btn_dsignIn);
        signUp = (Button)findViewById(R.id.btn_dregister);
        auth = FirebaseAuth.getInstance();


        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainInterfaceActivity.this,MainActivity.class));
                finish();
            }
        });

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(MainInterfaceActivity.this, HomeActivity.class));
                        finish();
                    }
                });
            }
        });
    }

    private void SignInDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Sign In");
        dialog.setMessage("Please Login to continue");

        LayoutInflater inflater = LayoutInflater.from(this);
        View layoutSignIn = inflater.inflate(R.layout.layout_sign_in,null);

        email = layoutSignIn.findViewById(R.id.et_demail);
       // password = layoutSignIn.findViewById(R.id.et_password);


        dialog.setView(layoutSignIn);
        dialog.setPositiveButton("Sign In", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (email.getText().toString().isEmpty()) {
                    email.setError("Cannot be Empty");
                    email.requestFocus();
                    return;
                }
                if (password.getText().toString().isEmpty()) {
                    password.setError("Cannot be Empty");
                    password.requestFocus();
                    return;
                }

                auth.signInWithEmailAndPassword(email.getText().toString(),password.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                startActivity(new Intent(MainInterfaceActivity.this, HomeActivity.class));
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Toast.makeText(getApplicationContext(),"Failure to Sign in",Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(MainInterfaceActivity.this,MainInterfaceActivity.class));
                                finish();
                            }
                        });

            }});
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
