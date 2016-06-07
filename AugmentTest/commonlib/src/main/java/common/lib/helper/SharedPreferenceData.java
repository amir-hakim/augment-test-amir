package common.lib.helper;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceData
{
	public Context context = null;
	String PrefName = "PREF_FILE_NAME";
	public SharedPreferenceData(Context incontext)
	{
		this.context = incontext;
	}
	
	/*
	 * Save Object to Memory
	 */
	public void save(String key,Object value, String prefFileName)
	{
		if(Utilities.isNullString(prefFileName))
			prefFileName = PrefName;
		SharedPreferences settings = context.getSharedPreferences(prefFileName, Context.MODE_PRIVATE);
	  SharedPreferences.Editor editor = settings.edit();

	  if(value instanceof String)
	  	editor.putString(key, value.toString());
	  else if(value instanceof Integer)
	  	editor.putInt(key, Integer.valueOf(value.toString()));
	  else if(value instanceof Boolean)
	  	editor.putBoolean(key, Boolean.valueOf(value.toString()));
	  else if(value instanceof Long)
	  	editor.putLong(key, Long.valueOf(value.toString()));
	  editor.commit();
	}
	public void save(String key,Object value)
	{
		save(key, value, PrefName);
	}
	
	/*
	 * Get Object from Shared data
	 */
	public Object get(String key,Class objectClass, String prefFileName)
	{
		if(Utilities.isNullString(prefFileName))
			prefFileName = PrefName;
		Object value = null;
		
		synchronized (this)
    {
			try
	    {
				SharedPreferences settings = context.getSharedPreferences(prefFileName, Context.MODE_PRIVATE);
				if(objectClass == String.class)
					value = settings.getString(key, "null");
				else if(objectClass == Integer.class)
					value = settings.getInt(key, -1);
				else if(objectClass == Boolean.class)
					value = settings.getBoolean(key, false);
				else if(objectClass == Long.class)
					value = settings.getLong(key, 0);
				settings = null;
	    }
	    catch (Exception e)
	    {
	    	e.printStackTrace();
	    }
			catch (Error e) 
			{
				e.printStackTrace();
			}
    }
		
		System.gc();
		return value;
    
	}
	public Object get(String key,Class objectClass)
	{
		return get(key, objectClass, PrefName);    
	}
	
}
