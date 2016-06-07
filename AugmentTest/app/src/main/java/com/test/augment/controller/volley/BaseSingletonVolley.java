package com.test.augment.controller.volley;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.test.augment.R;
import com.test.augment.view.AugmentApplication;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import common.lib.helper.Logger;

public class BaseSingletonVolley
{
    private static final String TAG = "BaseSingletonVolley";
    private static BaseSingletonVolley instance;
    private RequestQueue requestQueue;

    private ImageLoader imageLoader;


//    private boolean isShowLoadingDialog;
//    private BaseActivity activity;


    private BaseSingletonVolley()
    {
        requestQueue = Volley.newRequestQueue(AugmentApplication.getContext());

        //image loading & caching
        imageLoader = new ImageLoader(requestQueue, new ImageLoader.ImageCache()
        {
            public final int maxCahceSize = (int) (Runtime.getRuntime().maxMemory()/1024)/8;
            LruCache<String,Bitmap>cache = new LruCache<>(maxCahceSize);

            @Override
            public Bitmap getBitmap(String url) {
                return cache.get(url);
            }

            @Override
            public void putBitmap(String url, Bitmap bitmap) {
                cache.put(url,bitmap);
            }
        });

    }

//get the data cached by Volley
    public String getVolleyCachedData(String url){
        Cache cache = requestQueue.getCache();
        Cache.Entry entry = cache.get(url);
        String cached=null;
        if(entry!=null){
            try {
                 cached = new String(entry.data,"UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return cached;
    }

    public static BaseSingletonVolley getInstance(){
        if(instance==null)
            instance= new BaseSingletonVolley();
        return instance;
    }

    public RequestQueue getRequestQueue() {
        return requestQueue;
    }

    public ImageLoader getImageLoader() {
        return imageLoader;
    }


    /**general types of requests*/

    //Image Request
    /**
     * @param url : image url
     * @param imageView : imageView used to display the loaded image
     */
    public void startImageRequest(String url, final ImageView imageView,int placeHolderImageResId)
    {
        if(placeHolderImageResId>0){
            imageLoader.get(url,ImageLoader.getImageListener(imageView,placeHolderImageResId,-1));

        }
        //don't use place holder
        else{
            imageLoader.get(url, new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    imageView.setImageBitmap(response.getBitmap());
                }

                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
        }
    }


    //String Request
    /**
     * @param methodType : ex Request.Method.GET ,Request.Method.POST
     * @param url: request url
     * @param responseListener : interface to be invoked on success
     * @param errorListener : interface to be invoked on error
     */
    public void startStringRequest(int methodType,String url,
                                   final Map<String, String>  headers,
                                   final Map<String, String>  bodyEntity,
                                   final Response.Listener responseListener,
                                   final Response.ErrorListener errorListener)
    {
        StringRequest request = new StringRequest(methodType, url, new Response.Listener() {
            @Override
            public void onResponse(Object response) {
                Logger.instance().v(TAG, "Request Finished response =  " + (String) response);
                if (responseListener != null)
                    responseListener.onResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Logger.instance().v(TAG, "Request Finished VolleyError error = " + error.getMessage());
                if(error.networkResponse!=null){
                    //get status code here
                    int errorCode = error.networkResponse.statusCode;
                    //get response body and parse with appropriate encoding
                    String errorMessage="";
                    if(error.networkResponse.data!=null) {
                        try {
                            errorMessage = new String(error.networkResponse.data,"UTF-8");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Logger.instance().v(TAG, "Request Finished error errorCode = " +errorCode);
                    Logger.instance().v(TAG, "Request Finished error errorMessage = " +errorMessage);
                }

                if (errorListener != null)
                    errorListener.onErrorResponse( error) ;
            }
        })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                if(headers!=null||headers.size()>0)
                    return headers;
                return super.getHeaders();
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                if(bodyEntity!=null||bodyEntity.size()>0)
                    return bodyEntity;
                return super.getParams();
            }

            @Override
            protected String getParamsEncoding() {
                return super.getParamsEncoding();
            }
        };

        Logger.instance().v(TAG , "starting request : URL = "+url);
        requestQueue.add(request);
    }

    //same as the above but with requestId
    public void startStringRequest(int methodType, final String url, final int requestId,
                                   final Map<String, String> headers,
                                   final Map<String, String> bodyEntity,
                                   final VolleyResponseListener responseListener, final boolean showDialog, Activity _activity)
    {
        final StringRequest request = new StringRequest(methodType, url, new Response.Listener() {
            @Override
            public void onResponse(Object response) {
                Logger.instance().v(TAG, "Request Finished \nUrl =  "+url+" \nresponse =  " + (String) response);
                dismiss();
                if (responseListener != null)
                    responseListener.onResponse(requestId,response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Logger.instance().v(TAG, "Request Finished \n" +"Url =  "+url+" \nVolleyError error = " + error.getMessage());
                dismiss();
                if(error.networkResponse!=null){
                    //get status code here
                    int errorCode = error.networkResponse.statusCode;
                    //get response body and parse with appropriate encoding
                    String errorMessage="";
                    if(error.networkResponse.data!=null) {
                        try {
                            errorMessage = new String(error.networkResponse.data,"UTF-8");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Logger.instance().v(TAG, "Request Finished error errorCode = " +errorCode);
                    Logger.instance().v(TAG, "Request Finished error errorMessage = " +errorMessage);
                }

                if (responseListener != null)
                    responseListener.onErrorResponse( requestId , error) ;
            }
        })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                if(headers!=null && headers.size()>0)
                    return headers;
                return super.getHeaders();
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                if(bodyEntity!=null && bodyEntity.size()>0)
                    return bodyEntity;
                return super.getParams();
            }

            @Override
            protected String getParamsEncoding() {
                return super.getParamsEncoding();
            }
        };

        Logger.instance().v(TAG , "starting request : URL = "+url);

        if(showDialog){
            showWaitingDialog(_activity,(String)request.getTag());
        }
        requestQueue.add(request);
    }




    //JsonObject Request
    /**
     * @param methodType : ex Request.Method.GET ,Request.Method.POST
     * @param url : request url
     * @param postBody: An Object {JSONObject or String} to post with the request. Null is allowed and
     * @param responseListener
     * @param errorListener
     */
    public void startJsonObjectRequest(int methodType,String url,
                                       final JSONObject postBody,
                                       final Map<String, String>  headers,
                                       final Map<String, String>  bodyEntity,
                                       Response.Listener responseListener,
                                       Response.ErrorListener errorListener)
    {
        JsonObjectRequest request = null;//postBody=((JSONObject) postBody).toString()
        if (postBody instanceof JSONObject)
        {
            request= new JsonObjectRequest(methodType,url, postBody,responseListener,errorListener){

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError
                {
                    if(headers!=null||headers.size()>0)
                    {
                        headers.put("Content-Type", "application/json; charset=utf-8");
                        return headers;
                    }
                    return super.getHeaders();
                }

                @Override
                public String getBodyContentType() {
                    return "application/json charset=utf-8";
                }
            };
        }

        if(request!=null){
            requestQueue.add(request);
        }
    }



//same the above but with request Id
    public void startJsonObjectRequest(int methodType, final String url, final int requestId,
                                       final Map<String, String> headers,
                                       final JSONObject bodyEntity,
                                       final VolleyResponseListener responseListener, boolean showDialog, Activity activity)
    {
        JsonObjectRequest request = null;//postBody=((JSONObject) postBody).toString()
        request = new JsonObjectRequest(methodType, url, (JSONObject) bodyEntity, new Response.Listener() {
            @Override
            public void onResponse(Object response) {
                Logger.instance().v(TAG, "Request Finished \nUrl =  "+url+" \nresponse =  " + response.toString());
                dismiss();
                if (responseListener != null)
                    responseListener.onResponse(requestId,response);
            }
        },new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Logger.instance().v(TAG, "Request Finished \n" +"Url =  "+url+" \nVolleyError error = " + error.getMessage());
                        dismiss();
                        if(error.networkResponse!=null){
                            //get status code here
                            int errorCode = error.networkResponse.statusCode;
                            //get response body and parse with appropriate encoding
                            String errorMessage="";
                            if(error.networkResponse.data!=null) {
                                try {
                                    errorMessage = new String(error.networkResponse.data,"UTF-8");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            Logger.instance().v(TAG, "Request Finished error errorCode = " +errorCode);
                            Logger.instance().v(TAG, "Request Finished error errorMessage = " +errorMessage);
                        }

                        if (responseListener != null)
                            responseListener.onErrorResponse( requestId , error) ;
                    }
                })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                if (headers != null || headers.size() > 0) {
                    headers.put("Content-Type", "application/json; charset=utf-8");
                    return headers;
                }
                return super.getHeaders();
            }

            @Override
            public String getBodyContentType() {
                return "application/json charset=utf-8";
            }
        };


        if(showDialog){
            showWaitingDialog(activity,(String)request.getTag());
        }
        Logger.instance().v(TAG , "starting request : URL = "+url);
        if(request!=null){
            requestQueue.add(request);
        }
    }


    //cancel request which has the tag = RequestTag
    //if the RequestTag == null then cancelAll requests
    public void cancelRequest(String RequestTag)
    {
        if (requestQueue != null)
        {
            if(RequestTag!=null)
                requestQueue.cancelAll(RequestTag);
            else
                requestQueue.cancelAll(null);

        }

    }



    private ProgressDialog dialog;
    protected void showWaitingDialog(Activity activity , final String RequestTag)
    {
        if(activity==null)
            return;

        if (dialog == null)
            dialog =new ProgressDialog(activity);


        dialog.setMessage(activity.getString(R.string.loading));
        dialog.setIndeterminate(true);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);

        if (!dialog.isShowing())
            dialog.show();

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Log.v("Dialog", "Canceled ");
                cancelRequest(RequestTag);
            }
        });

        try {
            if (!dialog.isShowing())
                dialog.show();
        } catch (Exception e) {
        } // Show dialog on activity killed
    }


    private void dismiss() {
        try {
            if ( dialog.isShowing()) dialog.dismiss();
        } catch (Exception e) {
        } // Dismiss on activity killed
    }
}
