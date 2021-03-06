package com.example.mars.views;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.example.mars.R;
import com.example.mars.entities.User;
import com.example.mars.utils.Constants;
import com.example.mars.viewmodels.AuthViewModel;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Arrays;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import static com.example.mars.utils.Constants.RC_SIGN_IN;
import static com.example.mars.utils.Constants.USER;
import static com.example.mars.utils.Helpers.logErrorMessage;

public class AuthActivity extends AppCompatActivity {
    private AuthViewModel authViewModel;
    private GoogleSignInClient googleSignInClient;
    private CallbackManager mCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        initSignInButtons();
        initAuthViewModel();
        initGoogleSignInClient();
        initFacebookSdk();
    }

    private void initSignInButtons() {
        SignInButton googleSignInButton = findViewById(R.id.google_sign_in_button);
        googleSignInButton.setOnClickListener(v -> signInGoogle());

        Button facebookSignInButton = findViewById(R.id.facebook_sign_in_button);
        facebookSignInButton.setOnClickListener(v -> signInFacebook());
    }

    private FacebookCallback<LoginResult> signInFb() {
        return new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(Constants.TAG, "facebook:onSuccess:" + loginResult);
                getFacebookAuthCredential(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException e) {
                logErrorMessage(e.getMessage());
                errorToastMessage();
            }
        };
    }

    private void initAuthViewModel() {
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
    }

    private void initGoogleSignInClient() {
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);
    }

    private void initFacebookSdk() {
        FacebookSdk.sdkInitialize(AuthActivity.this);
    }

    private void signInGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signInFacebook() {
        LoginManager.getInstance().logInWithReadPermissions(AuthActivity.this, Arrays.asList("email", "public_profile"));

        mCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(mCallbackManager, signInFb());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount googleSignInAccount = task.getResult(ApiException.class);
                if (googleSignInAccount != null) {
                    getGoogleAuthCredential(googleSignInAccount);
                }
            } catch (ApiException e) {
                logErrorMessage(e.getMessage());
                errorToastMessage();
            }
        } else {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void getFacebookAuthCredential(AccessToken token) {
        Log.d(Constants.TAG, "handleFacebookAccessToken:" + token);

        AuthCredential fbAuthCredential = FacebookAuthProvider.getCredential(token.getToken());
        signInWithFacebookAuthCredential(fbAuthCredential);
    }

    private void getGoogleAuthCredential(GoogleSignInAccount googleSignInAccount) {
        String googleTokenId = googleSignInAccount.getIdToken();
        AuthCredential googleAuthCredential = GoogleAuthProvider.getCredential(googleTokenId, null);
        signInWithGoogleAuthCredential(googleAuthCredential);
    }

    private void signInWithGoogleAuthCredential(AuthCredential googleAuthCredential) {
        authViewModel.signInWithGoogle(googleAuthCredential);
        authViewModel.authenticatedUserLiveData.observe(this, authenticatedUser -> {
            if (authenticatedUser.isNew) {
                createNewUser(authenticatedUser);
            } else {
                goToMainActivity(authenticatedUser);
            }
        });
    }

    private void signInWithFacebookAuthCredential(AuthCredential facebookAuthCredential) {
        authViewModel.signInWithFacebook(facebookAuthCredential);
        authViewModel.authenticatedUserLiveData.observe(this, authenticatedUser -> {
            if (authenticatedUser.isNew) {
                createNewUser(authenticatedUser);
            } else {
                goToMainActivity(authenticatedUser);
            }
        });
    }

    private void createNewUser(User authenticatedUser) {
        authViewModel.createUser(authenticatedUser);
        authViewModel.createdUserLiveData.observe(this, user -> {
            if (user.isCreated) {
                successToastMessage(user.name);
            }
            goToMainActivity(user);
        });
    }

    private void successToastMessage(String name) {
        Toast.makeText(this,
                getString(R.string.hello) + " " + name + "!\n"
                + getString(R.string.success_create_account),
                Toast.LENGTH_LONG).show();
    }

    private void errorToastMessage() {
        Toast.makeText(this,
                getString(R.string.error_create_account),
                Toast.LENGTH_LONG).show();
    }

    private void goToMainActivity(User user) {
        Intent intent = new Intent(AuthActivity.this, PostsActivity.class);
        intent.putExtra(USER, user);
        startActivity(intent);
        finish();
    }
}