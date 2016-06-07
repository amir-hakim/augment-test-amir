package com.test.augment.controller.volley;

import com.android.volley.VolleyError;

public interface VolleyResponseListener
{
    void onResponse(int requestId, Object response);
    void onErrorResponse(int requestId, VolleyError error);
}
