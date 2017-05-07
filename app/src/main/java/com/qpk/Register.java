package com.qpk;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
public class Register extends AppCompatActivity implements View.OnClickListener{

    EditText name,email,mob,password,c_pass,vehicle;
    Button register;

    private static final String reg_url = "http://techiegirls6.esy.es/techiegirls/register.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        name = (EditText) findViewById(R.id.txt_name);
        email = (EditText) findViewById(R.id.txt_email);
        mob = (EditText) findViewById(R.id.txt_mobile);
        password = (EditText) findViewById(R.id.txt_password);
        c_pass = (EditText) findViewById(R.id.txt_confirm_pass);
        vehicle = (EditText) findViewById(R.id.txt_vehicle);

        register = (Button)findViewById(R.id.btn_register);
        register.setOnClickListener(this);
    }

    public void onClick(View v) {
        String Name = name.getText().toString().trim();
        String Email = email.getText().toString().trim();
        String Mobile = mob.getText().toString().trim();
        String Password = password.getText().toString().trim();
        String Confirm = c_pass.getText().toString().trim();
        String Vehicle = vehicle.getText().toString().trim();

        if (!Password.equals(Confirm))
        {
            Toast.makeText(getApplicationContext(), "Passwords do not match.", Toast.LENGTH_SHORT).show();
        } else
        {
            register(Name, Email, Mobile, Password, Vehicle);

        }
    }

    private void register(String name, String email, String mobile, String password, String vehicle)
    {
        final String urlSuffix = "?name=" + name + "&password=" + password + "&email=" + email + "&mobile=" + mobile + "&vehicle=" + vehicle;
        class RegisterUser extends AsyncTask<String, Void, String>
        {
            private ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(Register.this, "Please Wait", null, true, true);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                Toast.makeText(getApplicationContext(),s, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Register.this, Login.class));
            }

            @Override
            protected String doInBackground(String... params) {
                BufferedReader bufferReader;
                try {
                    URL url=new URL(reg_url+urlSuffix);
                    HttpURLConnection con=(HttpURLConnection)url.openConnection();
                    bufferReader=new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String result;
                    result=bufferReader.readLine();
                    return  result;
                }catch (Exception e){
                    return null;
                }
            }
        }
        RegisterUser ur = new RegisterUser();
        ur.execute(urlSuffix);
    }
}
