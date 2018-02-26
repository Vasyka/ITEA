package com.productions.itea.motivatedev;

import com.google.firebase.database.FirebaseDatabase;

// Offline

public class MotivateDev extends android.app.Application{
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
