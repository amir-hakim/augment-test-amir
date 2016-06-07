package common.lib.controller.backend;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;

import org.apache.http.HttpEntity;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;

import common.lib.controller.CTOperationResponse;
import common.lib.controller.SyncManager;

// All operations that needs to run on separated thread can use this operation
// it extend async task
// Most of cases, we using it in server connections
public abstract class BaseOperation<T> extends AsyncTask<Void, Object, CTOperationResponse> {

    public static final String SERIALIZE_CLASS_NAME = "serializeClassName";
    public static final String SERIALIZE_REQUEST_ID = "serializeRequestID";
    public static final String SERIALIZE_OPERATION_ID = "serializeOperationID";
    public static final int UNHANDLED_EXCEPTION_STATUS_CODE = 1001;
    private static HashMap<String, BaseOperation<?>> activeOperations = new HashMap<String, BaseOperation<?>>();
    private static HashMap<Object, BaseOperation<?>> activeOperationsMapByRequstId = new HashMap<Object, BaseOperation<?>>();
    protected ServerConnection serverConnection;
    protected boolean isShowLoadingDialog = true;
    protected Context context;
    protected Dialog dialog;
    protected Object requestID = 0;
    protected long operationUniqueID = 0;
    protected boolean isRunningFromOffline = false;
    public boolean isAllowingCache = false;
    protected boolean isOperationUnique = false; // to be called once, and don't allow multiple calls at same time for same operation/req
    public boolean isOperationRuning = false;
    ArrayList<RequestObserver> observersList;

    public BaseOperation() {
        operationUniqueID = System.currentTimeMillis();
    }

    public BaseOperation(Object requestID, boolean isShowLoadingDialog, Context activity) {
        this.isShowLoadingDialog = isShowLoadingDialog;
        this.context = activity;
        this.requestID = requestID;

        serverConnection = new ServerConnection();
        observersList = new ArrayList<RequestObserver>();
    }

    public static BaseOperation<?> getActiveOperation(Class<? extends BaseOperation<?>> operationClass) {
        return activeOperations.get(operationClass.getName());
    }

    public static BaseOperation<?> getActiveOperationByRequestId(Object requestId) {

        if (requestId != null)
            return activeOperationsMapByRequstId.get(requestId);
        return null;
    }

    /*
     * Check if the JSON response is succeeded
     */
    protected static void ensureRequestSucceeded(JSONObject responseJSON) {
//		if (responseJSON != null)
//		{
//			String statusCode = responseJSON.optString(ServerKeys.STATUS_CODE);
//			String statusMessage = responseJSON.optString(ServerKeys.STATUS_MESSAGE);
//
//			if (!Utilities.isNullString(statusCode))
//			{
//				double status = Double.valueOf(statusCode);
//				if (status != HttpURLConnection.HTTP_OK && status != HttpURLConnection.HTTP_CREATED
//				  && status != HttpURLConnection.HTTP_ACCEPTED)
//					throw new CTHttpError(statusMessage, Double.valueOf(statusCode));
//			}
//		}
    }

    public static BaseOperation desrialize(JSONObject serializedJson) throws InstantiationException, IllegalAccessException, ClassNotFoundException, JSONException {
        String className = serializedJson.getString(SERIALIZE_CLASS_NAME);
        BaseOperation operation = (BaseOperation) Class.forName(className).newInstance();
        operation.requestID = serializedJson.optInt(SERIALIZE_REQUEST_ID);
        operation.operationUniqueID = serializedJson.optLong(SERIALIZE_OPERATION_ID);

        operation.initFromSerialze(serializedJson);
        return operation;
    }

    /**
     * Do/Execute the operation itself
     *
     * @return the object
     * @throws Exception
     */
    public abstract T doMain() throws Throwable;

    protected void showWaitingDialog() {

    }

    @Override
    protected void onPreExecute() {
        isOperationRuning = true;
        activeOperations.put(this.getClass().getName(), this);
        if (requestID != null)
            activeOperationsMapByRequstId.put(requestID, this);
        super.onPreExecute();

        if (isShowLoadingDialog) {
            showWaitingDialog();
        }

    }

    @Override
    protected CTOperationResponse doInBackground(Void... params) {
        CTOperationResponse response = new CTOperationResponse();
        try {
            response.response = doMain();
        } catch (Throwable t) {
            if (!(t instanceof CTHttpError)) t.printStackTrace();


            response.error = t;
        }


        return response;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        if (isShowLoadingDialog && dialog.isShowing()) dialog.dismiss();
    }

    @Override
    protected void onPostExecute(CTOperationResponse result) {
        isOperationRuning = false;
        activeOperations.remove(this.getClass().getName());
        if (requestID != null)
            activeOperationsMapByRequstId.remove(requestID);

        super.onPostExecute(result);
        try {
            if (isShowLoadingDialog && dialog.isShowing())
                dialog.dismiss();
        } catch (Exception ex) {
            //ignore exception, as this happens sometimes
            ex.printStackTrace();
        }

        // Found an error
        if (result != null && result.error instanceof CTHttpError) {
            double statusCode = ((CTHttpError) result.error).statusCode;
            if (isAllowingCache && statusCode != 400 && statusCode != 401) {
                try {
                    SyncManager.getInstance().addOperationToQueue(serializeOperation(), isOperationUnique);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } // if status code not 400 or 401
        }
        // Success contacting server
        else if (isRunningFromOffline && (result == null || ((result.error instanceof Throwable) == false))) {
            SyncManager.getInstance().removeOperationFromQueueByID(operationUniqueID);
        }
        if (isRunningFromOffline)
            SyncManager.getInstance().callNextOperation();


        doOnPostExecute(result);
        // Wake observers with the result
        for (RequestObserver observer : observersList) {

            observer.handleRequestFinished(requestID, result.error, result.response);
        }
    }

    protected void doOnPostExecute(CTOperationResponse result) {
    }

    /**
     * Execute http request
     *
     * @param requestUrl        URL for the request
     * @param methodType        GET/POST/PUT etc..
     * @param contentType       Content Type
     * @param additionalHeaders any headers to be applied for the request
     * @param bodyEntity        if it was a Post request and need a body
     * @param responseType      Byte/String
     * @return the CTHttpResponse object
     */
    public CTHttpResponse doRequest(String requestUrl, String methodType, final String contentType,
                                    final HashMap<String, String> additionalHeaders, final HttpEntity bodyEntity, final ServerConnection.ResponseType responseType) {
        CTHttpResponse response = serverConnection.sendRequestToServer(requestUrl, methodType, contentType,
                additionalHeaders, null, bodyEntity, responseType);

        ensureHTTPRequestSucceeded(response);
        return response;
    }

    public CTHttpResponse doRequest(String requestUrl, String methodType, final String contentType,
                                    final HashMap<String, String> additionalHeaders, HttpParams params, final HttpEntity bodyEntity, final ServerConnection.ResponseType responseType) {
        CTHttpResponse response = serverConnection.sendRequestToServer(requestUrl, methodType, contentType,
                additionalHeaders, params, bodyEntity, responseType);

        if (params == null || response.statusCode != 302) ensureHTTPRequestSucceeded(response);
        return response;
    }

    // //////////////// End of observers handling /////////////////////

    /*
     * ******************************************************************
     * ********************** Observers Handling ************************
     * ******************************************************************
     */
    /*
	 * Add Request Observer to List
	 */
    public BaseOperation<T> addRequsetObserver(RequestObserver requestObserver) {
        // remove the observer if it was already added here
        removeRequestObserver(requestObserver);
        // add to observers List
        observersList.add(requestObserver);

        return this;
    }

    /*
     * Remove Request Observer from the list
     */
    public void removeRequestObserver(RequestObserver requestObserver) {
        observersList.remove(requestObserver);
    }

    /*
     * Check if the response is Valid HTTP Response
     */
    protected void ensureHTTPRequestSucceeded(CTHttpResponse response) {
        if (response == null) {
            throw new RuntimeException("Invalid Response Object while processing operation ["
                    + this.getClass().getName() + "]");
        }

        if (response.statusCode != HttpURLConnection.HTTP_OK && response.statusCode != HttpURLConnection.HTTP_CREATED
                && response.statusCode != HttpURLConnection.HTTP_ACCEPTED) {
            throw new CTHttpError(response.statusMessage, response.statusCode);
        }
    }

    public boolean isShowLoadingDialog() {
        return isShowLoadingDialog;
    }

    /*
     * Setters & Getters
     */
    public void setShowLoadingDialog(boolean isShowLoadingDialog) {
        this.isShowLoadingDialog = isShowLoadingDialog;
    }

    public JSONObject serializeOperation() throws JSONException {
        throw new RuntimeException("serializable() method not implemented " + getClass().getSimpleName());
    }

    public void initFromSerialze(JSONObject serializedJson) throws JSONException {
        throw new RuntimeException("desrialize() method not implemented ");
    }
}
