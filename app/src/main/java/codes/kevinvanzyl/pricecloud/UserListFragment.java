package codes.kevinvanzyl.pricecloud;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;


import com.google.gson.Gson;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import codes.kevinvanzyl.pricecloud.dummy.DummyContent;

/**
 * A list fragment representing a list of Users. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link UserDetailFragment}.
 * <p/>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class UserListFragment extends ListFragment {

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";
    private static final String USERS_ENDPOINT = "http://10.0.3.2:8000/users";

    public static List userList = new ArrayList<UserItem>();
    private FetchUsersTask mFetchUsersTask = null;

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = sDummyCallbacks;

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(String id);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(String id) {
        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public UserListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mFetchUsersTask != null) {
            return;
        }

        mFetchUsersTask = new FetchUsersTask();
        mFetchUsersTask.execute((Void) null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
        UserItem user = (UserItem) userList.get(position);
        mCallbacks.onItemSelected(user.id);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }

    public class FetchUsersTask extends AsyncTask<Void, Void, Boolean> {

        FetchUsersTask() {
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            String text = "";

            // Send data
            try {
                // Send POST data request
                HttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(USERS_ENDPOINT);

                try {
                    HttpResponse response = httpclient.execute(httpget);
                    StatusLine sl = response.getStatusLine();
                    int statusCode = sl.getStatusCode();

                    if ( statusCode == 200 ){
                        HttpEntity hp = response.getEntity();
                        InputStream content = hp.getContent();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(content));

                        StringBuilder builder = new StringBuilder();

                        String line;
                        while((line = reader.readLine()) != null){
                            builder.append(line);
                        }

                        String responseBody = builder.toString();

                        Context context = getActivity();
                        SharedPreferences sharedPref = context.getSharedPreferences(getString(R.string.userList), Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(getString(R.string.userList), responseBody);
                        editor.commit();

                        Gson gson = new Gson();
                        UserItem[] items = gson.fromJson(responseBody, UserItem[].class);
                        for (UserItem item: items) {
                            userList.add(item);
                        }

                        Log.d("PriceCloud", "UserListFragment: fetching successful");
                        return true;
                    } else {
                        Log.d("PriceCloud", "UserListFragment: error: "+ statusCode+" : "+sl.getReasonPhrase());
                        System.out.println(text);
                        return false;
                    }

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            catch(IOException e) {
                Log.d("PriceCloud", "UserListFragment: "+e.getClass());
                Log.d("PriceCloud", "UserListFragment: "+e.getMessage());
            }
            catch(Exception ex) {
                Log.d("PriceCloud", "UserListFragment Exception: "+ex.getMessage());
                Log.d("PriceCloud", "UserListFragment Exception: "+ex.getClass());
                ex.printStackTrace();
            }

            Log.d("PriceCloud", "UserListFragment: Response: "+text);

            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {

            mFetchUsersTask = null;

            if (success) {

                //show the user list
                setListAdapter(new ArrayAdapter<UserItem>(
                        getActivity(),
                        android.R.layout.simple_list_item_activated_1,
                        android.R.id.text1,
                        userList));

            } else {
                Log.d("PriceCloud", "UserList fetching FAILED");
            }
        }

        @Override
        protected void onCancelled() {
            mFetchUsersTask = null;
        }
    }
}
