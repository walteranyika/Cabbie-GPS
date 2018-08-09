package io.cabie.cabbie;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
    }

    public void go_to_driver(View view) {
        startActivity(new Intent(this, DriverLoginRegisterActivity.class));
    }

    public void go_to_customer(View view) {
        startActivity(new Intent(this, CustomerLoginRegisterActivity.class));
    }
}
