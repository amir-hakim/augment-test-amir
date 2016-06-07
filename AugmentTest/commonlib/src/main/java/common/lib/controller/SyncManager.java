package common.lib.controller;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import common.lib.controller.backend.BaseOperation;
import common.lib.helper.Logger;

// This class used to sync offline operations
public class SyncManager {
    private static final String TAG = "SyncManager";
    private static SyncManager _instance = null;
    boolean isSyncInProgress = false;
    private ArrayList<JSONObject> operationsQueue;
    private Queue<BaseOperation> operationsExectutingQueue;

    /*
     * Constructors
     */
    public SyncManager() {
        loadOperationsQueue();
    }

    public static SyncManager getInstance() {
        if (_instance == null)
            _instance = new SyncManager();
        return _instance;
    }

    public void loadOperationsQueue() {
        operationsExectutingQueue = new LinkedList<BaseOperation>();
        operationsQueue = CachingManager.getInstance().loadOperationsQueue();
        if (operationsQueue == null)
            operationsQueue = new ArrayList<JSONObject>();
    }

    public void saveOperationsQueue() {
        CachingManager.getInstance().saveOperationsQueue(operationsQueue);
    }

    public void addOperationToQueue(JSONObject jsonObject, boolean isRemoveAddedOper) {
        if (isRemoveAddedOper)
            removeOperationFromQueueByClassName(jsonObject.optString(BaseOperation.SERIALIZE_CLASS_NAME));
        operationsQueue.add(jsonObject);
        saveOperationsQueue();
    }

    public void removeOperationFromQueueByID(long operationUniqueID) {
        synchronized (operationsQueue) {
            for (JSONObject object : operationsQueue) {
                if (object.optLong(BaseOperation.SERIALIZE_OPERATION_ID) == operationUniqueID) {
                    operationsQueue.remove(object);
                    break;
                }
            }
        }
        saveOperationsQueue();
    }

    public void removeOperationFromQueueByClassName(String className) {
        synchronized (operationsQueue) {
            for (JSONObject object : operationsQueue) {
                if (object.optString(BaseOperation.SERIALIZE_CLASS_NAME).compareTo(className) == 0) {
                    operationsQueue.remove(object);
                    break;
                }
            }
        }
        saveOperationsQueue();
    }

    public void startSync() {
        if (!isSyncInProgress && operationsQueue != null && operationsQueue.size() > 0) {
            for (JSONObject object : operationsQueue) {
                BaseOperation operation;
                try {
                    operation = BaseOperation.desrialize(object);
                    operationsExectutingQueue.add(operation);
                } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | JSONException e) {
                    e.printStackTrace();
                }
            } // End for loop
            callNextOperation();
        } // End checking on the queue
        else
            Logger.instance().v(TAG, "isInProgress " + isSyncInProgress + " Queue: " + operationsQueue);
    }

    public void callNextOperation() {
        isSyncInProgress = true;
        if (!operationsExectutingQueue.isEmpty()) {
            operationsExectutingQueue.poll().execute();
        } else isSyncInProgress = false;
    }
}
