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

import com.example.courier.Commom.Common;
import com.example.courier.HomeActivity;
import com.example.courier.R;
import com.example.courier.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

public class DriverSigIn extends AppCompatActivity {

    FirebaseAuth mauth;
    EditText phone_code,phone_no;
    EditText Verificationcode;
    Button Send;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mcallback;
    String verification_code;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_sig_in);
        Send = findViewById(R.id.bt_send);
        phone_code = findViewById(R.id.et_verificationCode);
        phone_no = findViewById(R.id.et_phoneNo);
        mauth = FirebaseAuth.getInstance();

        mcallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                verification_code = s;
                Toast.makeText(getApplicationContext(),"Code Sent To Number",Toast.LENGTH_SHORT).show();
                viewVerificationDialog();

            }
        };

        Send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSms();
            }
        });
    }

    private void viewVerificationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Verification");
        builder.setMessage("Enter 6 digit code to Confirm");

        final LayoutInflater inflater =  LayoutInflater.from(this);
        View VerificatinLayout = inflater.inflate(R.layout.layout_verification,null);

        Verificationcode = VerificatinLayout.findViewById(R.id.et_verificationCode);
        builder.setView(VerificatinLayout);
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String input_code = Verificationcode.getText().toString();
                if(!verification_code.equals("")){

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verification_code,input_code);
                    mauth.signInWithCredential(credential)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()) {
                                        FirebaseDatabase.getInstance().getReference(Common.driver_table_info).child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        Common.currentUser = dataSnapshot.getValue(User.class);
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });
                                        //  Toast.makeText(getApplicationContext(),"Sccessfuly Signed In",Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(DriverSigIn.this, HomeActivity.class));
                                        // finish();

                                    }else if(task.getException() instanceof FirebaseAuthUserCollisionException){
                                        Toast.makeText(getApplicationContext(),"User Already Exist",Toast.LENGTH_SHORT).show();
                                    }
                                    else
                                        Toast.makeText(getApplicationContext(),"Please Re enter the Correct Code",Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });
        builder.show();
    }


    public void sendSms(){
        String no = phone_code.getText().toString() + phone_no.getText().toString();
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                no,30, TimeUnit.SECONDS,this,mcallback
        );
    }

}
