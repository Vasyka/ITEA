package com.firebase.ui.auth.viewmodel.email;

import android.app.Activity;
import android.app.Application;
import android.app.PendingIntent;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.util.Log;

import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.Resource;
import com.firebase.ui.auth.data.model.User;
import com.firebase.ui.auth.data.remote.ProfileMerger;
import com.firebase.ui.auth.ui.TaskFailureLogger;
import com.firebase.ui.auth.util.CredentialsUtil;
import com.firebase.ui.auth.viewmodel.AuthViewModelBase;
import com.firebase.ui.auth.viewmodel.PendingResolution;
import com.firebase.ui.auth.viewmodel.SingleLiveEvent;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseUser;

/**
 * Handles the logic for {@link com.firebase.ui.auth.ui.email.WelcomeBackPasswordPrompt} including
 * signing in with email and password, linking other credentials, and saving credentials to
 * SmartLock.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class WelcomeBackPasswordHandler extends AuthViewModelBase {

    private static final String TAG = "WBPasswordHandler";

    private static final int RC_SAVE = 100;

    private SingleLiveEvent<PendingResolution> mPendingResolutionLiveData = new SingleLiveEvent<>();
    private MutableLiveData<Resource<IdpResponse>> mSignInLiveData = new MutableLiveData<>();

    private IdpResponse mPendingIdpResponse;

    public WelcomeBackPasswordHandler(Application application) {
        super(application);
    }

    /**
     * Kick off the sign-in process.
     */
    public void startSignIn(@NonNull final String email,
                            @NonNull final String password,
                            @NonNull final IdpResponse inputResponse,
                            @Nullable final AuthCredential credential) {
        mSignInLiveData.setValue(new Resource<IdpResponse>());

        // Build appropriate IDP response based on inputs
        final IdpResponse outputResponse;
        if (credential == null) {
            outputResponse = new IdpResponse.Builder(
                    new User.Builder(EmailAuthProvider.PROVIDER_ID, email).build())
                    .build();
        } else {
            outputResponse = new IdpResponse.Builder(inputResponse.getUser())
                    .setToken(inputResponse.getIdpToken())
                    .setSecret(inputResponse.getIdpSecret())
                    .build();
        }

        // Kick off the flow including signing in, linking accounts, and saving with SmartLock
        getAuth().signInWithEmailAndPassword(email, password)
                .addOnFailureListener(new TaskFailureLogger(TAG, "signInWithEmailAndPassword failed."))
                .continueWithTask(new Continuation<AuthResult, Task<AuthResult>>() {
                    @Override
                    public Task<AuthResult> then(@NonNull Task<AuthResult> task) throws Exception {
                        // Forward task failure by asking for result
                        AuthResult result = task.getResult(Exception.class);

                        // Task succeeded, link user if necessary
                        if (credential == null) {
                            return Tasks.forResult(result);
                        } else {
                            return result.getUser()
                                    .linkWithCredential(credential)
                                    .continueWithTask(new ProfileMerger(outputResponse))
                                    .addOnFailureListener(new TaskFailureLogger(TAG, "linkWithCredential+merge failed."));
                        }
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            mSignInLiveData.setValue(new Resource<IdpResponse>(task.getException()));
                            return;
                        }

                        AuthResult authResult = task.getResult();
                        saveCredentialsOrFinish(authResult.getUser(), password, outputResponse);
                    }
                });
    }

    /**
     * Delegate activity result handling to the ViewModel. Returns {@code true} if the result was
     * handled.
     */
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SAVE) {
            if (resultCode != Activity.RESULT_OK) {
                Log.e(TAG, "SAVE: Canceled by user");
            }

            setSuccess(mPendingIdpResponse);

            return true;
        }

        return false;
    }

    /**
     * Get the observable state of the sign in operation.
     */
    public LiveData<Resource<IdpResponse>> getSignInResult() {
        return mSignInLiveData;
    }

    /**
     * Get an observable stream of {@link PendingIntent} resolutions requested by the ViewModel.
     *
     * Make sure to call {@link #onActivityResult(int, int, Intent)} for all activity results
     * after firing these pending intents.
     */
    public LiveData<PendingResolution> getPendingResolution() {
        return mPendingResolutionLiveData;
    }

    private void setSuccess(IdpResponse idpResponse) {
        mSignInLiveData.setValue(new Resource<>(idpResponse));
    }

    private void saveCredentialsOrFinish(FirebaseUser user,
                                         @Nullable String password,
                                         final IdpResponse idpResponse) {
        if (!getArguments().enableCredentials) {
            setSuccess(idpResponse);
            return;
        }

        Credential credential = CredentialsUtil.buildCredential(user, password, idpResponse);
        if (credential == null) {
            setSuccess(idpResponse);
            return;
        }

        getCredentialsClient().save(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            setSuccess(idpResponse);
                            return;
                        }

                        if (task.getException() instanceof ResolvableApiException) {
                            ResolvableApiException rae = (ResolvableApiException) task.getException();
                            PendingResolution pendingResolution = new PendingResolution(rae.getResolution(),
                                    RC_SAVE);

                            mPendingIdpResponse = idpResponse;
                            mPendingResolutionLiveData.setValue(pendingResolution);
                        } else {
                            // We don't consider SmartLock errors to be a problem, SmartLock is
                            // "best effort" and we will continue this sign in a success.
                            Log.w(TAG, "Unexpected smartlock exception.", task.getException());
                            setSuccess(idpResponse);
                        }
                    }
                });
    }
}
