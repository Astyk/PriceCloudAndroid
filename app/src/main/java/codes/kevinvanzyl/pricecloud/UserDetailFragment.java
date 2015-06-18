package codes.kevinvanzyl.pricecloud;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.google.gson.Gson;

import codes.kevinvanzyl.pricecloud.dummy.DummyContent;

/**
 * A fragment representing a single User detail screen.
 * This fragment is either contained in a {@link UserListActivity}
 * in two-pane mode (on tablets) or a {@link UserDetailActivity}
 * on handsets.
 */
public class UserDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private UserItem mItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public UserDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            Context context = getActivity();
            SharedPreferences sharedPref = context.getSharedPreferences(getString(R.string.userList), Context.MODE_PRIVATE);

            String json = sharedPref.getString(getString(R.string.userList), "");
            Gson gson = new Gson();
            UserItem[] items = gson.fromJson(json, UserItem[].class);

            Log.d("PriceCloud", "UserDetailFragment: ARG_ITEM_ID is null? "+(getArguments().getString(ARG_ITEM_ID) == null));
            Log.d("PriceCloud", "UserDetailFragment: userList length is "+items.length);
            Log.d("PriceCloud", "UserDetailFragment: ARG_ITEM_ID is "+getArguments().getString(ARG_ITEM_ID));

            int id = 1;
            if (getArguments().getString(ARG_ITEM_ID) != null) {
                try {
                    id = Integer.parseInt(getArguments().getString(ARG_ITEM_ID));
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }

            mItem = items[id-1];
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (mItem != null) {
            ((TextView) rootView.findViewById(R.id.user_firstname)).setText(mItem.firstname);
            ((TextView) rootView.findViewById(R.id.user_lastname)).setText(mItem.lastname);
            ((TextView) rootView.findViewById(R.id.user_username)).setText(mItem.username);
            ((TextView) rootView.findViewById(R.id.user_email)).setText(mItem.email);
            ((TextView) rootView.findViewById(R.id.user_phone)).setText(mItem.phone);
        }

        return rootView;
    }
}
