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

import com.example.courier.Commom.Common;
import com.example.courier.HomeActivity;
import com.example.courier.R;
import com.example.courier.model.User;
import com.example.courier.model.Vehicle;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DriverRegister extends AppCompatActivity {

    EditText name,address,phone,email,nic,password;
    Button next;
    FirebaseDatabase database;
    DatabaseReference myRef;
    DatabaseReference myRef1;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_register);

        name= (EditText)findViewById(R.id.et_dname);
        address =  (EditText)findViewById(R.id.et_daddress);
        email =  (EditText)findViewById(R.id.et_demail);
        phone = (EditText)findViewById(R.id.et_dphone);
        nic = (EditText)findViewById(R.id.et_nicNo);
        next = (Button)findViewById(R.id.et_next);

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        myRef = database.getReference(Common.driver_table_info);
        myRef1 = database.getReference(Common.driver_vehicles_table);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Viewdialog();
            }
        });
    }

    private void Viewdialog() {

        User u1 = new User();
        u1.setName(name.getText().toString());
        u1.setAddress(name.getText().toString());
        u1.setPhoneNo(phone.getText().toString());
        u1.setEmai(email.getText().toString());
        u1.setNic(nic.getText().toString());

        myRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(u1);
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Vehicle Details");
        dialog.setMessage("Please Enter Proper Details");

        LayoutInflater inflater = LayoutInflater.from(this);
        View vehicleLayout = inflater.inflate(R.layout.layout_vehicle_details,null);

        final EditText vname =vehicleLayout.findViewById(R.id.et_vcle_name);
        final EditText vno = vehicleLayout.findViewById(R.id.et_vcle_no);
        final EditText rdate =vehicleLayout.findViewById(R.id.et_rgstr_dte);
        final EditText exdate =vehicleLayout.findViewById(R.id.et_exp_dte);
       // EditText vname =vehicleLayout.findViewById(R.id.et_vcle_name);


        dialog.setView(vehicleLayout);
        dialog.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
               // dialog.dismiss();
                if (vname.getText().toString().isEmpty()) {
                    vname.setError("Cannot be Empty");
                    vname.requestFocus();
                    return;
                }
                if (vno.getText().toString().isEmpty()) {
                    vno.setError("Cannot be Empty");
                    vno.requestFocus();
                    return;
                }
                if (rdate.getText().toString().isEmpty()) {
                    rdate.setError("Cannot be Empty");
                    rdate.requestFocus();
                    return;
                }
                if (exdate.getText().toString().isEmpty()) {
                    exdate.setError("Cannot be Empty");
                    exdate.requestFocus();
                    return;
                }

                Vehicle v1 = new Vehicle();
                v1.setVname(vname.getText().toString());
                v1.setVno(vno.getText().toString());
                v1.setDate(rdate.getText().toString());
                v1.setExdate(exdate.getText().toString());

                myRef1.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(v1)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        startActivity(new Intent(DriverRegister.this, HomeActivity.class));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        startActivity(new Intent(DriverRegister.this,DriverRegister.class));
                    }
                });



            }});
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startActivity(new Intent(DriverRegister.this,DriverRegister.class));
            }
        });
        dialog.show();
    }


}
