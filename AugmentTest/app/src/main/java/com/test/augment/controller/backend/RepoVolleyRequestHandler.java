package com.test.augment.controller.backend;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.test.augment.controller.volley.BaseSingletonVolley;
import com.test.augment.controller.volley.VolleyResponseListener;
import com.test.augment.model.Repo;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class RepoVolleyRequestHandler implements VolleyResponseListener {
    int page;
    int per_page;
    int requestID;
    boolean isShowDialog;
    VolleyResponseListener volleyResponseListener;
    Gson gson;

    public RepoVolleyRequestHandler(int page, int per_page, int requestID, boolean isShowDialog, VolleyResponseListener volleyResponseListener) {
        this.page = page;
        this.per_page = per_page;
        this.requestID = requestID;
        this.isShowDialog = isShowDialog;
        this.volleyResponseListener = volleyResponseListener;

        gson = new Gson();
    }

    public void execute() {
        String requestURL = ServerKeys.REQUEST_URL + ServerKeys.SERVICE_REPO+"?page="+page+"&per_page"+per_page;
        BaseSingletonVolley.getInstance().startStringRequest(Request.Method.GET, requestURL, requestID, null, null, this, false, null);
    }

    @Override
    public void onResponse(int requestId, Object response) {
        Type listType = new TypeToken<List<Repo>>() {}.getType();
        ArrayList<Repo> reposList = gson.fromJson((String)response, listType);
        if(volleyResponseListener != null)
            volleyResponseListener.onResponse(requestId, reposList);
    }

    @Override
    public void onErrorResponse(int requestId, VolleyError error) {
        if(volleyResponseListener != null)
            volleyResponseListener.onErrorResponse(requestId, error);
    }
}
