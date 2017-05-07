package com.qpk;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class ReviweBooking extends AppCompatActivity {
    private Button makepayment;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviwe_booking);

        makepayment = (Button)findViewById(R.id.b_makepayment);
        makepayment.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent gIntent = new Intent(ReviweBooking.this, GeneratorActivity.class);
                startActivity(gIntent);

            }
        });
    }}