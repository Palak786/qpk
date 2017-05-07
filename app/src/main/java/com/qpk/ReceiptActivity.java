package com.qpk;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class ReceiptActivity extends AppCompatActivity {
    ImageView image;
    private FragmentManager fragmentManager;
    String text2Qr;
    Button navigate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);
        image = (ImageView) findViewById(R.id.image);
        navigate = (Button) findViewById(R.id.navigate);

        Bundle b = getIntent().getExtras();

        text2Qr= (String) b.getCharSequence("text1");

        generateQr();

        navigate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentRoute fragmentRoute = new FragmentRoute();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.frame_maps, fragmentRoute);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();

            }});


            }
    public void generateQr() {

        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(text2Qr, BarcodeFormat.QR_CODE, 200, 200);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            image.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }
}
