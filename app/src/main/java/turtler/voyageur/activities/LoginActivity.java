package turtler.voyageur.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.widget.LoginButton;
import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import turtler.voyageur.R;
import turtler.voyageur.models.User;

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

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ParseUser.getCurrentUser() != null) {
                    ParseUser.getCurrentUser().logOut();
                }
                List<String> permissions = Arrays.asList("user_friends", "email", "public_profile");
                ParseFacebookUtils.logInWithReadPermissionsInBackground(LoginActivity.this, permissions, new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException err) {
                        if (user == null) {
                            Log.d("MyApp", "Uh oh. The user cancelled the Facebook login.");
                        } else if (user.isNew()) {
                            getUserInfoFromFb(user);
                            getFriendInfoFromFB(user);
                            Log.d("MyApp", "User signed up and logged in through Facebook!");
                        } else {
                            Log.d("MyApp", "User logged in through Facebook!");
                            getUserInfoFromParse();
                        }
                    }
                });
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
                            currentUser.put("fID", response.getJSONObject().getString("id"));
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
    public void getFriendInfoFromFB(final ParseUser user) {
        final ParseUser currentUser = user;
        new GraphRequest(AccessToken.getCurrentAccessToken(),
                "/me/friends",
                new Bundle(),
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        try {
                            final JSONArray friends = response.getJSONObject().getJSONArray("data");
                            final User u = (User) currentUser;
                            u.getFriends().getQuery().findInBackground(new FindCallback<User>() {
                                @Override
                                public void done(List<User> users, ParseException e) {
                                    if (friends.length() == users.size()) {
                                        Log.d("friends", "NO NEW FRIENDS!");
                                        return;
                                    }
                                    else {
                                        for (int i = 0; i < friends.length(); i++) {
                                            JSONObject f = null;
                                            try {
                                                f = friends.getJSONObject(i);
                                                String fID = f.getString("id");
                                                ParseQuery friendQuery = new ParseQuery("_User");
                                                friendQuery.whereEqualTo("fID", fID);
                                                friendQuery.findInBackground(new FindCallback<User>() {
                                                    public void done(List<User> users, ParseException e) {
                                                        if (e == null) {
                                                            for (int i = 0; i < users.size(); i++) {
                                                                u.addFriend(users.get(i));
                                                                Log.d("friends", "friend added!");
                                                                Log.d("friends", users.get(i).getName());
                                                            }
                                                        } else {
                                                            Log.e("friends", "Error Loading Friends" + e);
                                                        }
                                                    }
                                                });
                                            } catch (JSONException e1) {
                                                e1.printStackTrace();
                                            }
                                        }
                                    }
                                }
                            });

                            currentUser.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e != null) {
                                        Log.d("friends", "exception");
                                        Log.d("friends", e.toString());
                                    }
                                    else {
                                        setResult(500);
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
        Toast.makeText(LoginActivity.this, "Welcome back " + parseUser.get("name"), Toast.LENGTH_SHORT).show();
        getFriendInfoFromFB(parseUser);
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
