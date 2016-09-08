package turtler.voyageur.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.widget.LoginButton;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import turtler.voyageur.R;

public class LoginActivity extends AppCompatActivity {
    @BindView(R.id.login_button) LoginButton loginButton;
    String email;
    String name;
    JSONObject picture;
    String pictureUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        List<String> permissions = Arrays.asList("user_friends", "email", "public_profile");
        ParseFacebookUtils.logInWithReadPermissionsInBackground(this, permissions, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException err) {
                if (user == null) {
                    Log.d("MyApp", "Uh oh. The user cancelled the Facebook login.");
                } else if (user.isNew()) {
                    getUserInfoFromFb(user);
                    Log.d("MyApp", "User signed up and logged in through Facebook!");
                } else {
                    Log.d("MyApp", "User logged in through Facebook!");
                    getUserInfoFromParse();
                }
            }
        });
    }

    public void getUserInfoFromFb(ParseUser user) {
        final ParseUser currentUser = user;
        Bundle parameters = new Bundle();
        parameters.putString("fields", "email,name,picture");
        new GraphRequest(AccessToken.getCurrentAccessToken(),
                "/me",
                parameters,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        try {
                            email = response.getJSONObject().getString("email");
                            name = response.getJSONObject().getString("name");
                            picture = response.getJSONObject().getJSONObject("picture");
                            JSONObject data = picture.getJSONObject("data");
                            pictureUrl = data.getString("url");
                            currentUser.setEmail(email);
                            currentUser.put("name", name);
                            currentUser.put("pictureUrl", pictureUrl);
                            currentUser.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e != null) {
                                        Log.d("exception", e.toString());
                                    }
                                    else {
                                        Intent data = new Intent();
                                        data.putExtra("user_email", currentUser.getEmail());
                                        setResult(500, data);
                                        finish();
                                    }
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
        ).executeAsync();
    }

    public void getUserInfoFromParse() {
        ParseUser parseUser = ParseUser.getCurrentUser();
        Toast.makeText(LoginActivity.this, "Welcome back " + parseUser.getUsername().toString(), Toast.LENGTH_SHORT).show();
        Intent data = new Intent();
        data.putExtra("user_email", parseUser.getEmail());
        setResult(500, data);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }
}
