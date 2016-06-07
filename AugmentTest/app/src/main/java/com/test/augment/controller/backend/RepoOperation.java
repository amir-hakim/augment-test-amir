package com.test.augment.controller.backend;

import android.content.Context;
import android.net.Uri;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.test.augment.controller.manager.RepoManager;
import com.test.augment.model.Repo;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import common.lib.controller.backend.BaseOperation;
import common.lib.controller.backend.CTHttpResponse;
import common.lib.controller.backend.ServerConnection;
import common.lib.helper.Logger;
import common.lib.helper.Utilities;

public class RepoOperation extends BaseOperation<Object> {
    int page;
    int per_page;
    Gson gson;

    public RepoOperation(int page, int per_page, Object requestID, boolean isShowLoadingDialog, Context activity) {
        super(requestID, isShowLoadingDialog, activity);
        this.page = page;
        this.per_page = per_page;
        gson = new Gson();
    }

    @Override
    public Object doMain() throws Throwable {
        String requestURL = ServerKeys.REQUEST_URL + ServerKeys.SERVICE_REPO + "?page=" + page + "&per_page=" + per_page + ((page > 0 && RepoManager.getInstance().since > 0) ? "&since="+RepoManager.getInstance().since: "");
//        BaseSingletonVolley.getInstance().startStringRequest(Request.Method.GET, requestURL, requestID, null, null, this, false, null);


        CTHttpResponse response = doRequest(requestURL, "GET", null,
                ServerKeys.getDefaultHeaders(), null, ServerConnection.ResponseType.RESP_TYPE_STRING);
        Logger.instance().v("NewEventsResp", response.response, false);

        ArrayList<Repo> reposList = getRepoListParsed((String) response.response);
        String linkHeader = (response.responseHeaders != null) ? response.responseHeaders.get("Link") : null;
        int since = getSince(linkHeader);
        RepoManager.getInstance().appendNewReposList(reposList, page, since);
        return reposList;
    }

    /*
     * Parse repositories list
     */
    private ArrayList<Repo> getRepoListParsed(String response) {
        Type listType = new TypeToken<List<Repo>>() {
        }.getType();
        ArrayList<Repo> reposList = gson.fromJson((String) response, listType);
        return reposList;
    }

    public int getSince(String link) {
        int since = -1;
        if(!Utilities.isNullString(link)) {
            String[] split = link.split(";");
            if(split.length > 0) {
                String fullURL = split[0];
                if(fullURL.startsWith("<"))
                    fullURL = fullURL.substring(1);
                if(fullURL.endsWith(">"))
                    fullURL = fullURL.substring(0, fullURL.length()-1);
                Uri uri=Uri.parse(fullURL);
                return Integer.valueOf(uri.getQueryParameter("since"));
            }
        }
        return since;
    }
}
