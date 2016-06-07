package common.lib.controller;

import android.content.Context;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import common.lib.helper.Logger;
import common.lib.helper.Utilities;
import common.lib.model.AppConfiguration;

public class CachingManager {

    private static final String TAG = "CachingManager";



    public enum ImageType {
        SMALL_IMAGE, LARGE_IMAGE
    }

    protected static CachingManager self;

    public static CachingManager getInstance() {
        if (self == null) {
            self = new CachingManager();
        }
        return self;
    }


    private CachingManager() {

    }

    protected boolean isObjectCachedAndNotExpired(long expireInHours, File objectFile) {
        boolean exist, expired = false;
        exist = objectFile.exists();

        if (exist) {
            if ((objectFile.lastModified() + expireInHours * 60 * 60 * 1000) < new Date().getTime()) {
                expired = true;
            }
        }
        return exist && !expired;
    }

    protected boolean isObjectCachedAndExpired(long expireInHours, File objectFile) {
        boolean exist, expired = false;
        exist = objectFile.exists();

        if (expireInHours <= 0)
            expired = true;
        else if (exist) {
            if ((objectFile.lastModified() + expireInHours * 60 * 60 * 1000) < new Date().getTime()) {
                expired = true;
            }
        }
        return exist && expired;
    }


    public static void saveObject(Serializable object, File objectFile) throws IOException {
        if (!objectFile.exists()) {
            objectFile.createNewFile();
        }
        ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(objectFile));
        outputStream.writeObject(object);
        objectFile.setLastModified(new Date().getTime());
        outputStream.close();

        objectFile.setLastModified(new Date().getTime());
    }

    public static Serializable loadObject(File objectFile) throws Exception {
        Object cachedObject = null;
        if (objectFile.exists()) {
            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(objectFile));
            cachedObject = inputStream.readObject();
            inputStream.close();

            objectFile.setLastModified(new Date().getTime());
        }

        return (Serializable) cachedObject;
    }


    public void saveAppConfigration(AppConfiguration appConfig) {
        File saveToFile = new File(Engine.DataFolder.APP_DATA, Engine.FileName.APP_CONFIGURATION);

        try {
            saveObject(appConfig, saveToFile);

        } catch (IOException ex) {
            ex.printStackTrace();
            // else ignore exception
        }
    }

    public AppConfiguration loadAppConfiguration() {
        AppConfiguration appConfig = null;

        try {
            appConfig = (AppConfiguration) loadObject(new File(Engine.DataFolder.APP_DATA, Engine.FileName.APP_CONFIGURATION));
        } catch (Throwable t) {
            Logger.instance().v("cachingmanager", "loadAppConfiguration - failed to load cached app configuration" + t.getClass().getSimpleName(), false);
        }

        return appConfig;
    }


    //delete contents of cache folder
    public void clearCachingFolder(Context context) {
        try {
            //get sub-folders inside cache folder
            File cacheFolder = Engine.getCacheRootDir(context);
            File[] subFolders = Engine.getSubFolders(cacheFolder);
            if (subFolders != null && subFolders.length > 0) {
                for (File folder : subFolders) {
                    try {
                        Engine.deleteFileRecursive(folder);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


/*******************************************************************
* **************************  News part  **************************
* *****************************************************************
*/
//Categories






/*******************************************************************
* ******************  Location Directions onMap ********************
* *****************************************************************
*/


    public void setLocationDirections(Context context, int itemId, List<List<HashMap<String, String>>> routes) {
        if (routes != null) {
            String itemFileName = Engine.FileName.FILE_LOCATION_DIRECTIONS + "_" + itemId;
            File containerFolder = Engine.DataFolder.FOLDER_LOCATION_DIRECTIONS;
            Serializable serializableRoutes = (Serializable) routes;
            saveObject(itemFileName, containerFolder, serializableRoutes, context);
        }
    }

    public List<List<HashMap<String, String>>> getLocationDirections(Context context, int itemId) {
        List<List<HashMap<String, String>>> routes = null;
        if (itemId > 0) {
            String itemFileName = Engine.FileName.FILE_LOCATION_DIRECTIONS + "_" + itemId;
            File containerFolder = Engine.DataFolder.FOLDER_LOCATION_DIRECTIONS;
            Serializable object = loadObject(itemFileName, containerFolder, context, Engine.ExpiryInfo.EXPIRING_LOCATION_DIRECTIONS);
            routes = (List<List<HashMap<String, String>>>) object;
        }
        return routes;
    }

    /**
     * **********************************************************************
     * **************************  SAVE && LOAD  ****************************
     * **********************************************************************
     */

    private void saveObject(String listFileName, File containerFolder, Serializable object, Context context) {
        //1-get language to cache the list in both languages
        //2-create file containing the list (if not exists)
        //3-create folder containing lists files (if not exists)
        //4-save list file in the folder

        String language = "en";

        if (!Utilities.isNullString(language))
            listFileName = listFileName + "_" + language.toUpperCase() + Engine.FileName.APP_FILES_EXT;

        File folder = Engine.getCacheFile(containerFolder, listFileName, context);

        try {
            saveObject(object, folder);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private Serializable loadObject(String listFileName, File containerFolder, Context context, int expireHours) {
        //1-get language to get cached list of the current language
        //2-get the file containing the list
        //3-get the folder containing lists files
        //4-check if the file isn't expired yet
        //5-load list from  list file which is in the folder

        String language = "en";

        if (language != null)
            listFileName = listFileName + "_" + language.toUpperCase() + Engine.FileName.APP_FILES_EXT;

        //check that the file is already existing
        //if it's not exiting  don't continue
        if (!Engine.isExistingFile(containerFolder, listFileName)) {
            return null;
        }
        File folder = Engine.getCacheFile(containerFolder, listFileName, context);
        Serializable object = null;
        //if expire hours == 0 this means don't care expiry
        try {
            boolean isExpired;
            if (expireHours == Engine.ExpiryInfo.NO_EXIPREY) {
                isExpired = false;
            } else {
                isExpired = isObjectCachedAndNotExpired(expireHours, folder);
            }

            if (!isExpired) {
                object = loadObject(folder);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return object;
    }
    /* ***********************************************************
	 * *****************  Operations queue CACHING ***************
	 * ***********************************************************
	 */
    public void saveOperationsQueue(ArrayList<JSONObject> queueOperations){
        ArrayList<String> queueList = new ArrayList<String>();
        if(queueOperations != null && queueOperations.size() > 0) {
            for(JSONObject jsonObject : queueOperations)
                queueList.add(jsonObject.toString());
        }
        File saveToFile = new File(Engine.DataFolder.APP_DATA, Engine.FileName.OPERATIONS_QUEUE);
        try{
            saveObject(queueList, saveToFile);
        }catch (IOException ex){
            ex.printStackTrace();
            // else ignore exception
        }
        queueList.clear();
        queueList = null;
    }

    public ArrayList<JSONObject> loadOperationsQueue(){
        ArrayList<JSONObject> queueOperations = new ArrayList<JSONObject>();

        try{
            ArrayList<String> operationsList = (ArrayList<String>) loadObject(new File(Engine.DataFolder.APP_DATA, Engine.FileName.OPERATIONS_QUEUE));
            if(operationsList != null && operationsList.size() > 0) {
                for(String operation : operationsList)
                    queueOperations.add(new JSONObject(operation));
                operationsList.clear();
            }
            operationsList = null;
        }catch (Throwable t){
            Logger.instance().v("cachingmanager", "loadUserData - failed to load User Data" + t.getClass().getSimpleName(), false);
        }

        return queueOperations;
    }
    public void deleteOperationsQueue()
    {
        File saveToFile = new File(Engine.DataFolder.APP_DATA, Engine.FileName.OPERATIONS_QUEUE);
        Engine.deleteFileRecursive(saveToFile);
    }
}
