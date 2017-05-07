package com.qpk;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class GeneratorActivity extends AppCompatActivity {
    EditText text;
    Button gen_btn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generator);
        text = (EditText) findViewById(R.id.editText);
        gen_btn = (Button) findViewById(R.id.gen_btn);

        gen_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent gIntent = new Intent(GeneratorActivity.this, ReceiptActivity.class);
                Bundle b = new Bundle();

                //Inserts a String value into the mapping of this Bundle
                b.putString("text1", text.getText().toString());

                gIntent.putExtras(b);

                //start the DisplayActivity
                startActivity(gIntent);
            }
        });
    }
}
