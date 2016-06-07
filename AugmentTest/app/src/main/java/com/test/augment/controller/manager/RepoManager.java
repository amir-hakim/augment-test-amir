package com.test.augment.controller.manager;


import com.test.augment.model.Repo;

import java.util.ArrayList;

public class RepoManager {
    private static final int MAX_REPO_PAGES_CACH = 4; // Maximum number of pages to cache
    ArrayList<Repo> reposList;
    public int page; // Used for paging
    public int since = -1; // Used for Paging

    private static RepoManager _instance = null;
    public static RepoManager getInstance() {
        if(_instance == null)
            _instance = new RepoManager();
        return _instance;
    }

    /*
     * Constructor
     */
    public RepoManager() {
    }

    /*
     * Append new list to repositories list
     */
    public void appendNewReposList(ArrayList<Repo> newRepositories, int repoPage, int since) {
        if(newRepositories == null)
            return;
        this.page = page;
        this.since = since;
        if(reposList == null)
            reposList = new ArrayList<>();

        reposList.addAll(newRepositories);
        if(repoPage <= MAX_REPO_PAGES_CACH)
            AugmentCaching.saveReposList(reposList);
    }

    public ArrayList<Repo> getSavedRepoList() {
        return AugmentCaching.loadReposList();
    }
}
