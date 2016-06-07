package com.test.augment.controller.backend;

import java.util.HashMap;
import java.util.Map;

public class ServerKeys {

    public static final boolean IS_PRODUCTION = true;

    // Server
    private static final String REQUEST_URL_STAGING = "https://api.github.com";
    private static final String REQUEST_URL_PRODUCTION = "https://api.github.com";
    public static final String REQUEST_URL = IS_PRODUCTION ? REQUEST_URL_PRODUCTION : REQUEST_URL_STAGING;

    // Services
    public static final String SERVICE_REPO = "/repositories";

    public static HashMap<String, String> getDefaultHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "Android");
        return headers;
    }
}
