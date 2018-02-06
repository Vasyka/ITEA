package com.productions.itea.motivatedev;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.firebase.ui.auth.IdpResponse;

public class SignedInActivity extends AppCompatActivity {

    private static final String EXTRA_IDP_RESPONSE = "extra_idp_response";

    public static Intent createIntent(
            Context context) { //IdpResponse idpResponse)


        /*Intent startIntent = new Intent();
        if (idpResponse != null) {
            startIntent.putExtra(EXTRA_IDP_RESPONSE, idpResponse);
        }*/

        return new Intent(context, SignedInActivity.class);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signed_in);
    }
}
