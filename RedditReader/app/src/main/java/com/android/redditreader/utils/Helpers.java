package com.android.redditreader.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.redditreader.R;
import com.android.redditreader.models.Subreddit;
import com.manas.asyncimageloader.AsyncImageLoader;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Manas on 29-12-2014.
 */
public class Helpers {

    private static final String TAG = Helpers.class.getSimpleName();

    public static HttpURLConnection getConnection(URL url, String requestMethod, boolean includeSessionCookie) {
        HttpURLConnection urlConnection = null;

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod(requestMethod);
            urlConnection.setRequestProperty("User-Agent", Globals.USER_AGENT);
            if (requestMethod.equals("POST")) {
                urlConnection.setDoOutput(true);
            }
            if (Globals.SESSION_COOKIE != null && includeSessionCookie) {
                urlConnection.setRequestProperty("Cookie", Globals.SESSION_COOKIE);
            }
            urlConnection.connect();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }

        return urlConnection;
    }

    public static void writeToConnection(HttpURLConnection conn, String postData) {
        OutputStreamWriter osWriter = null;
        try {
            osWriter = new OutputStreamWriter(conn.getOutputStream());
            osWriter.write(postData);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } finally {
            if (osWriter != null) {
                try {
                    osWriter.close();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }
    }

    public static String readStringFromConnection(HttpURLConnection urlConnection) {
        BufferedReader bufferedReader = null;
        String content = null;

        try {
            InputStream inputStream = urlConnection.getInputStream();
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            content = stringBuilder.toString();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }

        return content;
    }

    public static void viewURLInBrowser(Context context, String urlToView) {
        Intent viewIntent = new Intent(Intent.ACTION_VIEW);
        viewIntent.setData(Uri.parse(urlToView));

        // check if intent can be handled
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> infos = packageManager.queryIntentActivities(viewIntent, 0);
        if (infos.size() > 0) {
            context.startActivity(viewIntent);
        } else {
            Toast.makeText(context, R.string.error_intent_cannot_be_handled, Toast.LENGTH_LONG).show();
        }
    }

    public static void socialShareLink(Context context, String linkToShare) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, linkToShare);

        // check if intent can be handled
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> infos = packageManager.queryIntentActivities(shareIntent, 0);
        if (infos.size() > 0) {
            context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.social_share_dialog_title)));
        } else {
            Toast.makeText(context, R.string.error_intent_cannot_be_handled, Toast.LENGTH_LONG).show();
        }
    }

    public static void hideKeyboard(Context context, IBinder token) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(token, 0);
    }

    public static ArrayList<Subreddit> getDefaultSubredditsFromAsset(Context context) {
        final String defaultSubredditsAsset = "default_subreddits.txt";

        BufferedReader bufferedReader = null;
        ArrayList<Subreddit> subreddits = new ArrayList<>();

        try {
            bufferedReader = new BufferedReader(new InputStreamReader(context.getAssets().open(defaultSubredditsAsset)));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                Subreddit subreddit = new Subreddit();
                subreddit.setName(line);
                subreddits.add(subreddit);
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }

        return subreddits;
    }

    public static void displayPostThumbnail(String thumbnailURL, ImageView thumbnailImageView) {
        if (thumbnailURL.length() > 0) {
            thumbnailImageView.setVisibility(View.VISIBLE);

            switch (thumbnailURL) {
                case "self":
                    thumbnailImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    AsyncImageLoader.getInstance().displayImage(thumbnailImageView, String.valueOf(R.drawable.thumbnail_self), true);
                    break;

                case "nsfw":
                    thumbnailImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    AsyncImageLoader.getInstance().displayImage(thumbnailImageView, String.valueOf(R.drawable.thumbnail_nsfw), true);
                    break;

                case "default":
                    thumbnailImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    AsyncImageLoader.getInstance().displayImage(thumbnailImageView, String.valueOf(R.drawable.thumbnail_default), true);
                    break;

                default:
                    thumbnailImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    AsyncImageLoader.getInstance().displayImage(thumbnailImageView, thumbnailURL, false);
                    break;
            }
        } else {
            thumbnailImageView.setVisibility(View.GONE);
        }
    }

    public static String readFromPreferences(Context context, String fileName, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, null);
    }

    public static void writeToPreferences(Context context, String fileName, String key, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getUserPreferencesFileName(String username) {
        return username + "_prefs";
    }

    public static void addAccountToExistingAccounts(Context context, String username) {
        String existingAccountsJson = readFromPreferences(context, Globals.GLOBAL_PREFS, Globals.GLOBAl_PREFS_EXISTING_ACCOUNTS);
        JSONArray existingAccountsJsonArray = null;
        if (existingAccountsJson == null) {
            existingAccountsJsonArray = new JSONArray();
        } else {
            try {
                existingAccountsJsonArray = new JSONArray(existingAccountsJson);
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
            }
        }

        if (existingAccountsJsonArray != null) {
            // only add account if it does not already exist
            if (!existingAccountsJsonArray.toString().contains("\"" + username + "\"")) {
                existingAccountsJsonArray.put(username);
            }

            writeToPreferences(context, Globals.GLOBAL_PREFS, Globals.GLOBAl_PREFS_EXISTING_ACCOUNTS, existingAccountsJsonArray.toString());
        }
    }

    public static String[] getExistingAccounts(Context context) {
        String existingAccountsJson = readFromPreferences(context, Globals.GLOBAL_PREFS, Globals.GLOBAl_PREFS_EXISTING_ACCOUNTS);
        String[] existingAccountsArray = null;
        if (existingAccountsJson != null) {
            try {
                JSONArray existingAccountsJsonArray = new JSONArray(existingAccountsJson);
                existingAccountsArray = new String[existingAccountsJsonArray.length() + 1];  // leave one extra slot for 'Add Account' item
                existingAccountsArray[existingAccountsArray.length - 1] = "Add Account";

                for (int i = 0; i < existingAccountsJsonArray.length(); i++) {
                    existingAccountsArray[i] = existingAccountsJsonArray.getString(i);
                }
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
            }
        }
        else {
            existingAccountsArray  = new String[1];
            existingAccountsArray[0] = "Add Account";
        }

        return existingAccountsArray;
    }

    public static String getCurrentUsername(Context context) {
        String currentUsername = null;
        if (Globals.SESSION_COOKIE != null) {
            currentUsername = Helpers.readFromPreferences(
                    context,
                    Globals.GLOBAL_PREFS,
                    Globals.GLOBAL_PREFS_LAST_USERNAME_KEY);
        }

        return currentUsername;
    }

    public static void saveSubredditInfoForCurrentUser(Context context) {
        String currentUsername = getCurrentUsername(context);

        if (currentUsername != null) {
            writeToPreferences(
                    context,
                    currentUsername,
                    Globals.USER_PREFS_LAST_SUBREDDIT,
                    Globals.CURRENT_SUBREDDIT);

            writeToPreferences(
                    context,
                    currentUsername,
                    Globals.USER_PREFS_LAST_SORT,
                    Globals.CURRENT_SORT);

            writeToPreferences(
                    context,
                    currentUsername,
                    Globals.USER_PREFS_LAST_TIME,
                    Globals.CURRENT_TIME);
        }
    }

    public static void setSubredditInfoForCurrentUser(Context context) {
        String currentUsername = getCurrentUsername(context);

        if (currentUsername != null) {
            String lastSubreddit = readFromPreferences(
                    context,
                    currentUsername,
                    Globals.USER_PREFS_LAST_SUBREDDIT);
            Globals.CURRENT_SUBREDDIT = lastSubreddit != null ? lastSubreddit : Globals.DEFAULT_SUBREDDIT;

            String lastSort = readFromPreferences(
                    context,
                    currentUsername,
                    Globals.USER_PREFS_LAST_SORT);
            Globals.CURRENT_SORT = lastSort != null ? lastSort : Globals.DEFAULT_SORT;

            String lastTime = readFromPreferences(
                    context,
                    currentUsername,
                    Globals.USER_PREFS_LAST_TIME);
            Globals.CURRENT_TIME = lastTime != null ? lastTime : null;
        }
    }

    public static void setFavouriteSubredditsForCurrentUser(Context context, ArrayList<Subreddit> favouriteSubreddits) {
        JSONArray favouriteSubredditsJsonArray = new JSONArray();
        for (Subreddit subreddit: favouriteSubreddits) {
            favouriteSubredditsJsonArray.put(subreddit.getName());
        }

        writeToPreferences(
                context,
                getUserPreferencesFileName(getCurrentUsername(context)),
                Globals.USER_PREFS_FAVOURITE_SUBREDDITS,
                favouriteSubredditsJsonArray.toString());
    }

    public static String[] getFavouriteSubredditsForCurrentUser(Context context) {
        String[] favouriteSubredditsArray = null;

        String favouriteSubredditsJson = readFromPreferences(
                context,
                getUserPreferencesFileName(getCurrentUsername(context)),
                Globals.USER_PREFS_FAVOURITE_SUBREDDITS);

        if (favouriteSubredditsJson != null) {
            try {
                JSONArray favouriteSubredditsJsonArray = new JSONArray(favouriteSubredditsJson);
                favouriteSubredditsArray = new String[favouriteSubredditsJsonArray.length()];

                for (int i=0; i<favouriteSubredditsArray.length; i++) {
                    favouriteSubredditsArray[i] = favouriteSubredditsJsonArray.getString(i);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return favouriteSubredditsArray;
    }

}