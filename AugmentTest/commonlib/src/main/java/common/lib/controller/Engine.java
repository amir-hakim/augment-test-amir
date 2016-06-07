package common.lib.controller;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.logging.Logger;

import common.lib.helper.Utilities;
import common.lib.model.AppConfiguration;


public class Engine {

	private static final String TAG = "Engine";
	private static AppConfiguration appConfig;

	public final static Logger LOGGER = Logger.getLogger("SECB_LOGGER");

	public static boolean isLanguangeFromApp = true;  // this to modify if the language from application or the settings

	public static final SimpleDateFormat SDF_DIR = new SimpleDateFormat("ddMMyyyy", Locale.ENGLISH);

	public static void deleteFileRecursive(File fileOrDirectory) {
		if (fileOrDirectory.isDirectory())
			for (File child : fileOrDirectory.listFiles())
				deleteFileRecursive(child);
		fileOrDirectory.delete();
	}

	public static void validateCachedData(Context context) {
		//clear cache in any of both cases
		//User installed new version
		//User switched operator

		AppConfiguration appConfig = CachingManager.getInstance().loadAppConfiguration();

		TelephonyManager tel = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		String networkOperator = tel.getNetworkOperator();

		String mcc = "", mnc = "";
		int versionCode = 0;

		try {
			versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		if (networkOperator != null && networkOperator.length() > 4) {
			mcc = networkOperator.substring(0, 3);
			mnc = networkOperator.substring(3);
		}

		String preferedAppLanguage = Utilities.getLanguage();
		if (appConfig == null) {
			appConfig = new AppConfiguration();
			preferedAppLanguage = Utilities.getLanguage();
		} else {
			preferedAppLanguage = appConfig.getLanguage();
		}

		boolean deleteCachedData = false;

		deleteCachedData = appConfig.getAppVersion() != versionCode;
		deleteCachedData = deleteCachedData || !mcc.equalsIgnoreCase(appConfig.getLastKnownMCC());
		deleteCachedData = deleteCachedData || !mnc.equalsIgnoreCase(appConfig.getLastKnownMNC());

		if (deleteCachedData) {
			Engine.LOGGER.info("Delete application cached info");

			Engine.deleteFileRecursive(DataFolder.APP_DATA);

			initialize(context);

			appConfig.setLanguage(preferedAppLanguage);
			appConfig.setAppVersion(versionCode);
			appConfig.setLastKnownMCC(mcc);
			appConfig.setLastKnownMNC(mnc);
			CachingManager.getInstance().saveAppConfigration(appConfig);
		}

		Engine.appConfig = appConfig;

		if (Engine.appConfig == null) {
			Engine.appConfig = new AppConfiguration();
			Engine.appConfig.setLanguage(preferedAppLanguage);
			Engine.appConfig.setAppVersion(versionCode);
			Engine.appConfig.setLastKnownMCC(mcc);
			Engine.appConfig.setLastKnownMNC(mnc);
		}
	}

	public static AppConfiguration getAppConfiguration() {

		if (appConfig == null) {
			common.lib.helper.Logger.instance().e(TAG, "app configuration is null, expected crash", false);
		}
		return appConfig;
	}

	public static boolean isCurrentLanguageArabic(Context context) {
		return Engine.getAppConfiguration().getLanguage().equals("ar");
	}

	/**
	 * Check if external storage is built-in or removable.
	 *
	 * @return True if external storage is removable (like an SD card), false otherwise.
	 */
	@TargetApi(VERSION_CODES.GINGERBREAD)
	public static boolean isExternalStorageRemovable() {
		if (hasGingerbread()) {
			return Environment.isExternalStorageRemovable();
		}
		return true;
	}

	/**
	 * Get the external app cache directory.
	 *
	 * @param context The context to use
	 * @return The external cache dir
	 */
	@TargetApi(VERSION_CODES.FROYO)
	public static File getExternalCacheDir(Context context) {
		if (hasFroyo()) {
			//Update by samehg to fix NullPointerException that happens when there is no external storage
			//context.getExternalCacheDir();
			File externalFile = context.getExternalCacheDir();
			return externalFile != null ? externalFile : context.getCacheDir();
		}

		// Before Froyo we need to construct the external cache dir ourselves
		final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache/";
		return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
	}

	/**
	 * Get a usable cache directory (external if available, internal otherwise).
	 *
	 * @param context The context to use
	 * @return The cache dir
	 */
	private static String getDiskCacheDir(Context context) {
		// Check if media is mounted or storage is built-in, if so, try and use external cache dir
		// otherwise use internal cache dir
		String cachePath = context.getCacheDir().getPath();
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !isExternalStorageRemovable())
			cachePath = getExternalCacheDir(context).getPath();


		return cachePath;
	}


	public static boolean hasGingerbread() {
		return Build.VERSION.SDK_INT >= VERSION_CODES.GINGERBREAD;
	}

	public static boolean hasFroyo() {
		// Can use static final constants like FROYO, declared in later versions
		// of the OS since they are inlined at compile time. This is guaranteed behavior.
		return Build.VERSION.SDK_INT >= VERSION_CODES.FROYO;
	}

	public static void initialize(Context appContext) {

		initializeDataFolders(appContext);

//		cacheParams = new ImageCacheParams();
//		cacheParams.setMemCacheSizePercent(0.1f); // Set memory cache to 25% of app memory
//		cacheParams.setDiskCacheDir(DataFolder.IMAGE_CACHE);
	}

	public static void initializeDataFolders(Context appContext) {

		DataFolder.APP_DATA = appContext.getDir("app_data", Context.MODE_PRIVATE);
		DataFolder.USER_DATA = appContext.getDir("user_data", Context.MODE_PRIVATE);

		/**NEWS*/
		DataFolder.NEWS =getCacheDir(appContext,"ews");

		/**Location_Directions*/
		DataFolder.FOLDER_LOCATION_DIRECTIONS =getCacheDir(appContext,"location_directions");


	}

	public static void switchAppLanguage(ContextWrapper context, String newLang) {

		String switchLanguage = "en";
		if(Utilities.isNullString(newLang)) {
			String currentLanguage = appConfig.getLanguage();
			if (currentLanguage.equals("ar")) {
				switchLanguage = "en";
			} else if (currentLanguage.equals("en")) {
				switchLanguage = "ar";
			}
		} else switchLanguage = newLang;

		appConfig.setLanguage(switchLanguage);

//		Engine.deleteFileRecursive(Engine.DataFolder.COLLECTION_CACH);
//		Engine.deleteFileRecursive(Engine.DataFolder.CONTENT_CACH);
		Engine.deleteFileRecursive(DataFolder.APP_DATA);


		Engine.initialize(context);

		CachingManager.getInstance().saveAppConfigration(appConfig);

		setApplicationLanguage(context, switchLanguage);
	}

	public static void setApplicationLanguage(ContextWrapper context, String language) {


		if (isLanguangeFromApp) {
			String currentLanguage = language != null ? language
					: context.getResources().getConfiguration().locale.getLanguage();

			Resources res = context.getBaseContext().getResources();
			DisplayMetrics dm = res.getDisplayMetrics();
			android.content.res.Configuration conf = res.getConfiguration();
			conf.locale = new Locale(currentLanguage);
			appConfig.setLocale(conf.locale);
			res.updateConfiguration(conf, dm);
		}
	}


	/*
	 * ********************************************************************************
	 * ************************* Dir **************************************************
	 * ********************************************************************************



	/**************************************************/
	//get the "Cache" folder
	public static File getCacheRootDir(Context appContext)
	{
		File file = new File(getDiskCacheDir(appContext));
		if (!file.exists())
			file.mkdirs();
		return file;
	}
	//create folder named "DirName" inside the "Cache" folder
	public static File getCacheDir(Context appContext ,String DirName)
	{
		File file = new File(getDiskCacheDir(appContext) + File.separator + DirName);
		if (!file.exists())
			file.mkdirs();
		return file;
	}

	//create file called "fileName" inside the "parent" File
	public static File getCacheFile(File parent ,String fileName, Context appContext) {
	/*	File file = new File(parent + File.separator + fileName + "");
		return file;*/

		if(!parent.exists()){
			try {
//				parent.createNewFile();
				getCacheDir(appContext,parent.getName());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		File file = new File(parent + File.separator + fileName + "");
		if(!file.exists())
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		return file;
	}

	//get folders inside that folder
	public static File[] getSubFolders( File root)
	{
		File[] subFolders = null;
		if(root!=null)
		{
			File[] subs=(root.listFiles());
			ArrayList<File> filesList = new ArrayList<File>(java.util.Arrays.asList(subs));
			Iterator<File> iterator = filesList.iterator();
			while(iterator.hasNext()){
				File currentFile = iterator.next();
				if(!currentFile.isDirectory()){
					iterator.remove();
				}
				// other operations
			}
			subFolders = filesList.toArray(new File[filesList.size()]);
			return subFolders;
		}
		return subFolders;
	}

	//return true if the specified file exists in the specified folder.
	public static boolean isExistingFile(File containerFolder, String fileName)
	{
		File file = new File(containerFolder + File.separator + fileName + "");
		return file.exists();
	}


	// ////////////////////////////////////////////////////////////////////////////////

	public static class FileName{
		public final static String APP_CONFIGURATION = "app_config.dat";
		public final static String APP_USER = "app_user.dat";
		public final static String OPERATIONS_QUEUE = "operations_queue.dat";


		//file Extension
		public final static String APP_FILES_EXT=".dat";

		/**News*/
		//news_categories
		public static final String NEWS_CATEGORIES_LIST = "news_categories";
		//news_list
		public static final String NEWS_LIST = "news_list";

		/**Map Location Directions*/
		public static final String FILE_LOCATION_DIRECTIONS = "app_location_directions";

	}

	public static class DataFolder{

		public static File APP_DATA;
		public static File USER_DATA;

		/**News*/
		public static File NEWS ;

		/**Map Location Directions*/
		public static File FOLDER_LOCATION_DIRECTIONS;

	}

	/**
	 * Expiry for each object types in hours
	 */
	public static class ExpiryInfo {

		final public static int NO_EXIPREY =0;
		final public static int EXPIRING_APP =48;
		final public static int EXPIRING_LOCATION_DIRECTIONS = 1;

	}
}
