package com.test.augment.controller.manager;


import com.test.augment.model.Repo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import common.lib.controller.CachingManager;
import common.lib.controller.Engine;
import common.lib.helper.Logger;

/*
 * AugmentCaching  for wrap the caching of Augment apps, it use the base caching manager of common library
 */
public class AugmentCaching {

    public final static String REPOS_LIST = "repos_list.dat";

    /*
     * Save Repositories list
     */
    public static void saveReposList(ArrayList<Repo> eventsList){

        File saveToFile = new File(Engine.DataFolder.APP_DATA, REPOS_LIST);
        try{
            CachingManager.getInstance().saveObject(eventsList, saveToFile);
        }catch (IOException ex){
            ex.printStackTrace();
            // else ignore exception
        }
    }

    /*
     * Load repositories list
     */
    public static ArrayList<Repo> loadReposList(){
        ArrayList<Repo> reposList = new ArrayList<Repo>();

        try{
            reposList = (ArrayList<Repo>) CachingManager.getInstance().loadObject(new File(Engine.DataFolder.APP_DATA, REPOS_LIST));
        }catch (Throwable t){
            Logger.instance().v("cachingmanager", "loadRepost list - failed to load " + t.getClass().getSimpleName(), false);
        }

        return reposList;
    }
    /*
     * Delete saved repositories list
     */
    public void deleteReposList()
    {
        File saveToFile = new File(Engine.DataFolder.APP_DATA, REPOS_LIST);
        Engine.deleteFileRecursive(saveToFile);
    }
}
