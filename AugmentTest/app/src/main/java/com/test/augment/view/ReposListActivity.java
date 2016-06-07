package com.test.augment.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.test.augment.R;
import com.test.augment.controller.backend.RepoOperation;
import com.test.augment.model.Repo;
import com.test.augment.view.adapter.RecyclerViewEvents;
import com.test.augment.view.adapter.RepoAdapter;

import java.util.ArrayList;

import common.lib.controller.backend.RequestObserver;
import common.lib.helper.Logger;

public class ReposListActivity extends AugmentBaseActivity implements RequestObserver {

    private int REQUEST_ID_REPO = 1;

    int page = 0; // Current page called
    int per_page = 100; // Per page items count

    ArrayList<Repo> reposList = new ArrayList<>(); // Whole repositories List
    RecyclerView mRecyclerView; // Repositories recycler view
    RepoAdapter adapter; // Adapter for the repositories adapter
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repos_list);

        mRecyclerView = (RecyclerView) findViewById(R.id.home_recycler_view);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener(mLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                // do something...
                Logger.instance().v("Load More..", "Page: "+current_page);
                loadItems(current_page);
            }
        });
        setAdapter(reposList);

        loadItems(page);
    }

    /*
     * On Load repositries finished
     * called after each page get called
     */
    public void onLoadFinished(ArrayList<Repo> newReposList){
        reposList.remove(reposList.size()-1); // Remove null item that added for loading
        if(newReposList != null)
            reposList.addAll(newReposList);
        adapter.setReposList(reposList);
        adapter.notifyDataSetChanged();
    }

    /*
     * Load new page of repositories
     */
    private void loadItems(int page) {
        reposList.add(null); // add extra item for loading
        adapter.notifyItemInserted(reposList.size() - 1);

        this.page = page; // store last page called

        // Call the operation
        RepoOperation operation = new RepoOperation(page, per_page, REQUEST_ID_REPO, false, this);
        operation.addRequsetObserver(this);
        operation.execute();
    }

    /*
     * Set Adapter
     */
    public void setAdapter(ArrayList<Repo> reposList) {
        this.reposList = reposList;
        adapter = new RepoAdapter(reposList);
        mRecyclerView.setAdapter(adapter);
        adapter.setRecyclerViewEvents(recyclerViewEvents);
    }

    /*
     * Recyclerview events, clicked and long clicked
     */
    RecyclerViewEvents recyclerViewEvents = new RecyclerViewEvents() {
        @Override
        public void onRecyclerItemClick(int position) {
            Repo repository = reposList.get(position);

            Intent intent = new Intent(ReposListActivity.this, RepoDetailsActivity.class);
            intent.putExtra(RepoDetailsActivity.BUNDLE_KEY_REPO, repository);
            startActivity(intent);
        }
        @Override
        public void onRecyclerItemLongClick(int position) {
        }
    };

    /*
     * ********************************************************
     * *************** Request Observer ***********************
     * ********************************************************
     */
    @Override
    public void handleRequestFinished(Object requestId, Throwable error, Object resulObject) {
        if(requestId == REQUEST_ID_REPO) {
            if(error == null) {
                Logger.instance().v("onResponse", resulObject);
                if(resulObject instanceof ArrayList) {
                    ArrayList<Repo> newRepoList = (ArrayList<Repo>) resulObject;
                    onLoadFinished(newRepoList);
                }
            } else {
                onLoadFinished(null);
                // TODO get and load from cache
//                RepoManager.getInstance().getSavedRepoList(); // list cached
            }
        }
    }

    @Override
    public void requestCanceled(Integer requestId, Throwable error) {
    }

    @Override
    public void updateStatus(Integer requestId, String statusMsg) {
    }
    // End of 'Request Observer' ////////////////////////////
}
