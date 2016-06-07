package common.lib.controller.backend;


import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.HashMap;

import common.lib.helper.Logger;
import common.lib.helper.Utilities;

public class RequestHandler
{
	ServerConnection serverConnection;
	Thread connectionThread = null;

	/*
	 * Add new field to the entity
	 */
	public void addToEntity(MultipartEntity bodyEntity, String key, Object value)
	{
		if (bodyEntity == null) bodyEntity = new MultipartEntity();
		if (!Utilities.isNullString(key) && !Utilities.isNullString(value.toString()))
		{
			try
			{
				bodyEntity.addPart(key, new StringBody(value.toString()));
			}
			catch (UnsupportedEncodingException e)
			{
				e.printStackTrace();
			}

		}
	}
	
	/*
	 * Get default headers map
	 */
	public static HashMap<String, String> getDefaultHeadersMap()
	{
		HashMap<String, String> additionalHeaders = new HashMap<String, String>();
		return additionalHeaders;
	}

	
	/*
	 * Get Specific Value for a key in Json Object
	 */
	public static Object getFromJsonObject(JSONObject jsonObject, String key, Class typeClass)
	{
		Object object = jsonObject.opt(key);

		if (object == null)
		{
			Logger.instance().v("getStringFromJsonObject", key + " = null", false);
			return getObjectFromNull(typeClass);
		}

		return object;
	}

	public static Object getObjectFromNull(Class typeClass)
	{
		if (typeClass == String.class)
			return "";
		else if (typeClass == Boolean.class)
			return false;
		else if (typeClass == Integer.class) return 0;
		return null;
	}

	/*
	 * ******************************************************************************************
	 * ******************************** Check Request Status ************************************
	 * ******************************************************************************************
	 */
	
	/*
	 * check Request status
	 */
	public static boolean checkStatus(int response, int statusCode)
	{
		if (response != -1 && response == statusCode) return true;
		return false;
	}

	/*
	 * Check if the response is Valid Response
	 */
	public static boolean isRequestSucceed(int response)
	{
		return (checkStatus(response, HttpURLConnection.HTTP_OK) || checkStatus(response, HttpURLConnection.HTTP_CREATED)
		  || checkStatus(response, HttpURLConnection.HTTP_ACCEPTED));
	}
	
	/*
	 * Check if the response is TimedOut
	 */
	public static boolean isRequestTimedOut(int response)
	{
		return (checkStatus(response, HttpURLConnection.HTTP_CLIENT_TIMEOUT));
			
	}

	/*
	 * Check if the response is UnAuhtorized
	 */
	public static boolean isRequestUnAuthorized(int response)
	{
		return checkStatus(response, HttpURLConnection.HTTP_UNAUTHORIZED);
	}

	/*
	 * Check if the response is BadRequest
	 */
	public static boolean isRequestBadRequest(int response)
	{
		return checkStatus(response, HttpURLConnection.HTTP_BAD_REQUEST);
	}

	/*
	 * Check if the response is Foridden
	 */
	public static boolean isRequestForbidden(int response)
	{
		return checkStatus(response, HttpURLConnection.HTTP_FORBIDDEN);
	}

	/*
	 * Check if the response is Conflict
	 */
	public static boolean isRequestConflict(int response)
	{
		return checkStatus(response, HttpURLConnection.HTTP_CONFLICT);
	}

	/*
	 * Check if the response is Not Accepted - 406
	 */
	public static boolean isRequestNotAccepted(int response)
	{
		return checkStatus(response, HttpURLConnection.HTTP_NOT_ACCEPTABLE);
	}
	// ////////////////////////////////////////////////////////////
}
