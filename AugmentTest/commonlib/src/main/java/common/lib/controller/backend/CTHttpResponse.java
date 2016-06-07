package common.lib.controller.backend;

import java.util.HashMap;

public class CTHttpResponse{
	public int statusCode;
	public String statusMessage;
	
	public HashMap<String, String> responseHeaders;
	public Object response;
	public String errorMessage;
	public Throwable error;
	
	public CTHttpResponse(){
		responseHeaders = new HashMap<String, String>();
	}
}
