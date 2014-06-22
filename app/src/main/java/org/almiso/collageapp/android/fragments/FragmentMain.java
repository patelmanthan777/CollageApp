package org.almiso.collageapp.android.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import org.almiso.collageapp.android.R;
import org.almiso.collageapp.android.base.CollageFragment;
import org.almiso.collageapp.android.core.model.InstaUserDependence;
import org.almiso.collageapp.android.preview.InstaPreviewView;
import org.almiso.collageapp.android.preview.user.dependence.UserDependenceReceiver;

/**
 * Created by almiso on 13.06.2014.
 */
public class FragmentMain extends CollageFragment implements View.OnClickListener, UserDependenceReceiver {

    private InstaUserDependence mDependence;

    //Controls
    private TextView mediaCount;
    private TextView followsCount;
    private TextView followedByCount;


    @Override
    public void onResume() {
        super.onResume();
        if (mDependence == null) {
            loadDependence();
        }
        updateDataLayout();
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, null);
        setUpView(view);
        return view;
    }

    private void setUpView(View view) {
        mediaCount = (TextView) view.findViewById(R.id.mediaCount);
        followsCount = (TextView) view.findViewById(R.id.followsCount);
        followedByCount = (TextView) view.findViewById(R.id.followedByCount);

        view.findViewById(R.id.buttonMyPhotos).setOnClickListener(this);
        view.findViewById(R.id.buttonMyFollows).setOnClickListener(this);
        view.findViewById(R.id.buttonMyFollowedBy).setOnClickListener(this);


        view.findViewById(R.id.buttonMyLikedPhotos).setOnClickListener(this);
        view.findViewById(R.id.buttonSearchFeed).setOnClickListener(this);

        view.findViewById(R.id.avatarTouchLayer).setOnClickListener(this);


        initFields(view);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonMyPhotos:
                getRootController().openFragmentSearch(FragmentPhotoGrid.ACTION_SEARCH_MY_PHOTOS,
                        application.getAccount().getMe(), false);
                break;
            case R.id.buttonMyLikedPhotos:
                getRootController().openFragmentSearch(FragmentPhotoGrid.ACTION_SEARCH_MY_LIKED_PHOTOS,
                        application.getAccount().getMe(), false);
                break;
            case R.id.buttonMyFollows:
                getRootController().openFragmentFriendList(FragmentUserList.ACTION_FOLLOWS,
                        application.getAccount().getMe());
                break;
            case R.id.buttonMyFollowedBy:
                getRootController().openFragmentFriendList(FragmentUserList.ACTION_FOLLOWED_BY,
                        application.getAccount().getMe());
                break;
            case R.id.buttonSearchFeed:
                getRootController().openFragmentSearch(FragmentPhotoGrid.ACTION_SEARCH_FEED,
                        application.getAccount().getMe(), false);
                break;

        }

    }

    private void initFields(View view) {
        ((TextView) view.findViewById(R.id.name)).setText(application.getAccount().getUsername().toUpperCase());
        InstaPreviewView previewView = (InstaPreviewView) view.findViewById(R.id.avatar);
        previewView.setEmptyDrawable(R.drawable.ic_action_person);
        previewView.requestAvatar();
    }

    private void updateDataLayout() {
        if (mDependence == null) {
            return;
        }
        mediaCount.setText(String.valueOf(mDependence.getMediaCount()));
        followsCount.setText(String.valueOf(mDependence.getFollowsCount()));
        followedByCount.setText(String.valueOf(mDependence.getFollowedByCount()));
    }

    private void loadDependence() {
        InstaUserDependence dep = new InstaUserDependence(application.getMyId(), 0, 0, 0, true);
        application.getUiKernel().getInstaUserDependenceLoader().requestSearchUser(dep, this);
    }

    @Override
    public void onUserDependenceReceived(InstaUserDependence dependence) {
        mDependence = dependence;
        updateDataLayout();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_frag_main, menu);

        getSherlockActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSherlockActivity().getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSherlockActivity().getSupportActionBar().setHomeButtonEnabled(false);
        getSherlockActivity().getSupportActionBar().setTitle(R.string.st_app_name);
        getSherlockActivity().getSupportActionBar().setSubtitle(null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ic_settings:
                getRootController().openFragmentSettings();
                return true;
            case R.id.ic_search_user:
                getRootController().openFragmentSearchUserByNick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
