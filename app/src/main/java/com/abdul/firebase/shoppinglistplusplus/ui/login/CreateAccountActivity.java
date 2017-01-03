package com.abdul.firebase.shoppinglistplusplus.ui.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.abdul.firebase.shoppinglistplusplus.R;
import com.abdul.firebase.shoppinglistplusplus.model.User;
import com.abdul.firebase.shoppinglistplusplus.ui.BaseActivity;
import com.abdul.firebase.shoppinglistplusplus.utils.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;

/**
 * Represents Sign up screen and functionality of the app
 */
public class CreateAccountActivity extends BaseActivity {
    private static final String LOG_TAG = CreateAccountActivity.class.getSimpleName();
    private ProgressDialog mAuthProgressDialog;
    private EditText mEditTextUsernameCreate, mEditTextEmailCreate, mEditTextPasswordCreate;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(LOG_TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                }
                else {
                    // User is signed out
                    Log.d(LOG_TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
        /**
         * Link layout elements from XML and setup the progress dialog
         */
        initializeScreen();
    }

    /**
     * Override onCreateOptionsMenu to inflate nothing
     *
     * @param menu The menu with which nothing will happen
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
    /**
     * Link layout elements from XML and setup the progress dialog
     */
    public void initializeScreen() {
        mEditTextUsernameCreate = (EditText) findViewById(R.id.edit_text_username_create);
        mEditTextEmailCreate = (EditText) findViewById(R.id.edit_text_email_create);
        mEditTextPasswordCreate = (EditText) findViewById(R.id.edit_text_password_create);
        LinearLayout linearLayoutCreateAccountActivity = (LinearLayout) findViewById(R.id.linear_layout_create_account_activity);
        initializeBackground(linearLayoutCreateAccountActivity);

        /* Setup the progress dialog that is displayed later when authenticating with Firebase */
        mAuthProgressDialog = new ProgressDialog(this);
        mAuthProgressDialog.setTitle(getResources().getString(R.string.progress_dialog_loading));
        mAuthProgressDialog.setMessage(getResources().getString(R.string.progress_dialog_creating_user_with_firebase));
        mAuthProgressDialog.setCancelable(false);
    }

    /**
     * Open LoginActivity when user taps on "Sign in" textView
     */
    public void onSignInPressed(View view) {
        Intent intent = new Intent(CreateAccountActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Create new account using Firebase email/password provider
     */
    public void onCreateAccountPressed(View view) {
        final String email = mEditTextEmailCreate.getText().toString();
        String password = mEditTextPasswordCreate.getText().toString();
        String user = mEditTextUsernameCreate.getText().toString();
        if (!isEmailValid(email)) {
            mEditTextEmailCreate.setError("Incorrect email");
            return;
        }
        if (!isUserNameValid(user)) {
            mEditTextUsernameCreate.setError("Incorrect username");
            return;
        }
        if (!isPasswordValid(password)) {
            mEditTextPasswordCreate.setError("Incorrect password");
            return;
        }
        mAuthProgressDialog.show();
        SecureRandom r = new SecureRandom();
        String throwAwayPassword = new BigInteger(130, r).toString(32);
        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(LOG_TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(CreateAccountActivity.this, "Authentication failed",
                                    Toast.LENGTH_SHORT).show();
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthUserCollisionException e) {
                                mEditTextEmailCreate.setError(getString(R.string.error_email_taken));
                            }
                            catch (Exception e) {
                                Log.e(LOG_TAG, e.getMessage());
                            }
                            mAuthProgressDialog.dismiss();
                            return;
                        }
                        createUserInFirebaseHelper(task.getResult().getUser().getEmail());
                    }
                });
    }

    /**
     * Creates a new user in Firebase from the Java POJO
     */
    private void createUserInFirebaseHelper(final String email) {
        Log.d(LOG_TAG, "Email authenticated with " + email);
        String encodedEmail = email.replace(".",",");
        final User user = new User(encodedEmail,email);
        DatabaseReference userRootRef = FirebaseDatabase.getInstance().getReference()
                .child(Constants.FIREBASE_LOCATION_USERS);
        final DatabaseReference userInstance = userRootRef.child(encodedEmail);
        //check if child with encodedEmail exists
        userInstance.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    userInstance.setValue(user);
                    Intent intent = new Intent(CreateAccountActivity.this, LoginActivity.class);
                    intent.putExtra(getString(R.string.pref_email),email);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                    //verifyEmail();
                }
                else {
                    Log.d(LOG_TAG, "user already exists in Firebase");
                    mAuthProgressDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(LOG_TAG, "onCancelled with message: " + databaseError.getMessage());
                mAuthProgressDialog.dismiss();
            }
        });
    }
    private void verifyEmail() {
        Log.d(LOG_TAG, "verifyEmail");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        //sending verification email and allowing user to verify it
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.d(LOG_TAG, "onComplete Email Verfication");
                            if (!task.isSuccessful()) {
                                Log.d(LOG_TAG, "onComplete not successful");
                                mAuthProgressDialog.dismiss();
                                return;
                            }
                            Intent emailIntent = new Intent(Intent.ACTION_MAIN);
                            emailIntent.addCategory(Intent.CATEGORY_APP_EMAIL);
                            //startActivity(intent);
                            PackageManager pm = getPackageManager();
                            List<ResolveInfo> activities = pm.queryIntentActivities(emailIntent,0);
                            if (activities.size() > 0)
                                startActivityForResult(emailIntent,Constants.FINISH_VERIFICATION);
                            else {
                                Toast.makeText(CreateAccountActivity.this,
                                        "No app for opening email", Toast.LENGTH_SHORT).show();
                                mAuthProgressDialog.dismiss();
                            }
                        }
                    });
        }
    }

    private boolean isEmailValid(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isUserNameValid(String userName) {
        return true;
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 5;
    }

    /**
     * Show error toast to users
     */
    private void showErrorToast(String message) {
        Toast.makeText(CreateAccountActivity.this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(LOG_TAG, "onActivityResult");
        if (requestCode == Constants.FINISH_VERIFICATION) {
            Log.d(LOG_TAG,"finished verification");
            final String email = mEditTextEmailCreate.getText().toString();
            mAuthProgressDialog.dismiss();
            Intent intent = new Intent(CreateAccountActivity.this, LoginActivity.class);
            intent.putExtra(getString(R.string.pref_email),email);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }
}