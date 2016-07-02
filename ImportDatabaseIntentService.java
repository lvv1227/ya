package com.astro.dsoplanner;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.ClipboardManager;
import android.util.Log;

import com.astro.dsoplanner.DbListItem.FieldTypes;
import com.astro.dsoplanner.misc.NSOG;

//do not use toasts as the handle intent is run in non-UI thread
public class ImportDatabaseIntentService extends IntentService {
	
	
	@ObfuscateOPEN
	private static final String NSOG_IMPORT = "NSOG import";
	private static final String IMPORT_DATABASE = "0";
	private static final String TEMP = "temp";	
	private static final String CLASS_CAST_ERROR = "class cast error";
	@ObfuscateCLOSE
	
	private static final String TAG="IDIS";@MarkTAG
    private static int NOTIFICATION_ID=1337;
    
    private NotificationHelper nh;
    private static Set<String> dbset=new HashSet<String>();//set of databases to be imported to
    
	public ImportDatabaseIntentService(){
		super(IMPORT_DATABASE);
	}
	/*@Override
    public void onCreate() {
		
	}*/
//	boolean onstartnotification=false;
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	            
	    Log.d(TAG,"onStart, intent="+intent);
	    nh=new NotificationHelper(this);
	    startForeground(NOTIFICATION_ID, nh.createNotification(getString(R.string.importing_database), getString(R.string.importing_database)));
	   
	    super.onStartCommand(intent, flags, startId);	
	    return START_STICKY;
	}
	
	
	
	/**
	 * 
	 * @param name - database to be imported into
	 */
	public static synchronized void registerImportToService(String dbname){
		dbset.add(dbname);
	}
	public static synchronized boolean isBeingImported(String dbname){
		return dbset.contains(dbname);
	}
	
	private static synchronized void unregisterImportFromService(String dbname){
		dbset.remove(dbname);
	}
	/**
	 * 
	 * @param result true for completion without errors, false otherwise
	 */
	private void workOver(boolean result,ErrorHandler eh,String dbname){
	//	Settings.putSharedPreferences(Constants.IDIS_WORKOVER, true, this);
	//	Global.idisWorkoverForVDA=true;
		//Intent intent =new Intent(Constants.IDIS_WORKOVER);	   
	   // LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	    if(dbname!=null){
	    	unregisterImportFromService(dbname);
	    }
		
		if(!result){
	    	nh.error(getString(R.string.error_importing_database));
	    }
	    else{
	    	nh.completed();
	    }
	//    Global.setIdisRunningStatus(false);
	    Log.d(TAG,"import over");
	    if(eh!=null&&eh.hasError()){
	    	Intent intent =new Intent(Constants.IDIS_ERROR_BROADCAST);	
	    	intent.putExtra(Constants.IDIS_ERROR_STRING, eh.getErrorString());
	 	    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	    }
	}
	//WakeLock cpulock;
	@Override
	protected void onHandleIntent(Intent intent) {
		/*if(cpulock==null){
			PowerManager pm=(PowerManager)getSystemService(Context.POWER_SERVICE);
			cpulock=pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "lock");
			cpulock.acquire();
		}*/
		
		nh.clearContent();//for more than 1 download
		Log.d(TAG,"onHandleIntent, intent="+intent);
		Bundle b=intent.getExtras();
		for(String key:b.keySet()){
			Log.d(TAG,"key="+key+" value="+b.get(key));
		}
		
		
		//NSOG
		boolean nsog=intent.getBooleanExtra(Constants.IDIS_NSOG, false);
		if(nsog){
			nh.setFileName(NSOG_IMPORT);
			try{
				new NSOG(this).make();
			}
			catch(Exception e){
				Log.d(TAG,"ex="+e);
			}
			workOver(true, null, null);
			return;
		}
		
		
		
		boolean pasting=intent.getBooleanExtra(Constants.IDIS_PASTING, false);
		String content="";	
		if(pasting){
			ClipboardManager clipboard=(ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);    			

			if(clipboard.hasText()){
				content= clipboard.getText().toString();
			}
		}
		
		boolean cmo_update=intent.getBooleanExtra(Constants.IDIS_CMO_UPDATE, false);
		Log.d(TAG,"import started");

		//initialisation
		
		String filename=intent.getStringExtra(Constants.IDIS_FILENAME);
		String shortname="";
		if(filename!=null){
			File f=new File(filename);
			shortname=f.getName();
			
		}
		
		String dbname=intent.getStringExtra(Constants.IDIS_DBNAME);
		if(dbname==null){
			Log.d(TAG,"dbname=null");
			workOver(false,null,null);
			return;
		}
		
		
		if(!pasting&&filename==null){
			Log.d(TAG,"filename=null");
			workOver(false,null,dbname);
			return;
		}

		nh.setFileName((pasting?getString(R.string.clipboard):shortname));
	//	Global.setIdisRunningStatus(true);
	//	Global.setIdisDbName(dbname);
		boolean notedatabase=intent.getBooleanExtra(Constants.IDIS_NOTES, false);
		
		int catalog=intent.getIntExtra(Constants.IDIS_CATALOG, -1);
		if(catalog==-1&&!notedatabase){
			Log.d(TAG,"catalog=-1");
			workOver(false,null,dbname);
			return;
		}

		boolean ignoreNgcicRefs=intent.getBooleanExtra(Constants.IDIS_IGNORE_NGCIC_REF, false);
		boolean ignoreCustomDbRefs=intent.getBooleanExtra(Constants.IDIS_IGNORE_CUSTOMDB_REF,false);
		
		
		byte[]ftypesArr=intent.getByteArrayExtra(Constants.IDIS_FTYPES);
		//boolean cdl=false;//custom database large
		FieldTypes ftypes=null;
		if(ftypesArr!=null){
			ByteArrayInputStream bin=new ByteArrayInputStream(ftypesArr);						
			try{
				DataInputStream din=new DataInputStream(bin);
				ftypes=new FieldTypes(din);
			}
			catch(Exception e){}
			
		}
		if (ftypes==null)
			ftypes=new FieldTypes();
		
		
		AstroCatalog cat=null;
		NoteDatabase ndb=null;
		if(!notedatabase){
			switch(catalog){
			case AstroCatalog.COMET_CATALOG:
				cat=new CometsDatabase(this,catalog);
				break;
			case AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG:
				cat=new CometsDatabase(this,catalog);
				break;
			default:
				if(ftypes.isEmpty())//there are no restrictions imposed
					cat=new CustomDatabase(this,dbname,catalog);
				else
					cat=new CustomDatabaseLarge(this,dbname,catalog,ftypes);
			}
			
			
		}
		else{
			ndb=new NoteDatabase(this);
		}

		InputStream in=null;
		ErrorHandler eh=new ErrorHandler();
		unregisterImportFromService(dbname);//so that db could be opened
		if(!notedatabase){
			cat.open(eh);
		}
		else{
			ndb.open(eh);
		}
		
		registerImportToService(dbname);//block for all others
		if(eh.hasError()){
		//	eh.showErrorInToast(this);
			Log.d(TAG,"error opening catalog");
			workOver(false,eh,dbname);
			return;
		}

		try{
			
			InfoListImpl iL;
			if(!notedatabase){
				if((catalog==AstroCatalog.COMET_CATALOG||catalog==AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG)&&cmo_update)
					iL=new SqlDbFillerInfoListImpl(TEMP,CustomObject.class,cat,nh,true);
				else				
					iL=new SqlDbFillerInfoListImpl(TEMP,CustomObject.class,cat,nh);//when loading the list all non-custom objects are discarded
				iL.allowObjExtraction();   
			}
			else{
				iL=new NoteDbFillerInfoListImpl(TEMP,ndb,nh);
			}
			
			InfoListLoader loader;
			if(!pasting){
				in=new FileInputStream(new File(filename));
				if((catalog==AstroCatalog.COMET_CATALOG)&&cmo_update)
					loader=new CometListLoader(in);
				else if(catalog==AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG&&cmo_update)
					loader=new MinorPlanetListLoader(in);
				else
					loader=new InfoListStringLoaderImp(in,ftypes);
			}
			else{
				loader=new InfoListStringLoaderImp(content,ftypes);
			}
			
			if(ignoreCustomDbRefs&&loader instanceof InfoListStringLoaderImp){
				InfoListStringLoaderImp loaderimp=(InfoListStringLoaderImp)loader;
				loaderimp.setIgnoreCustomDbRefsFlag();
			}
			if(ignoreNgcicRefs&&loader instanceof InfoListStringLoaderImp){
				InfoListStringLoaderImp loaderimp=(InfoListStringLoaderImp)loader;
				loaderimp.setIgnoreNgcicRefsFlag();
			}
			
			ErrorHandler eh1=iL.load(loader);
			eh.addError(eh1);
			Log.d(TAG,"error in try catch="+eh.getErrorString());
		}
		catch (Exception e){
			Log.d(TAG,"exception="+AstroTools.getStackTrace(e));
			if(!eh.hasError()){
				eh.addError(new ErrorHandler.ErrorRec(ErrorHandler.IO_ERROR,""));
			}    			
			workOver(false,eh,dbname);
			return;
		}
		finally{
			Log.d(TAG,"finally");
			try{
				if(!pasting)
					in.close();
			}
			catch(Exception e){}
			if(eh.hasError()){
				Log.d(TAG,"error="+eh.getErrorString());
			//	Toast.makeText(this, eh.getErrorString(), Toast.LENGTH_LONG).show();
				//eh.showErrorInToast(this);
			/*	try{
					FileOutputStream out=new FileOutputStream(new File(Global.dsoPath,"idis.txt"));
					PrintStream ps=new PrintStream(out);
					ps.println(eh.getErrorString());
					ps.close();
				}
				catch(Exception e){}*/
				
			}
			
			if(!notedatabase)
				cat.close();
			else
				ndb.close();
		}
	//	if(cpulock!=null)cpulock.release();
		workOver(true,eh,dbname);





	}
	
	private static class  NotificationHelper  {
		static protected int COMPLETE_NOTIFICATION_ID = 1;
		protected NotificationManager mNotificationManager;
		Context context;
		public NotificationHelper(Context context){
			 
		 //   COMPLETE_NOTIFICATION_ID++;
			this.context=context;
			mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			mBuilder=new NotificationCompat.Builder(context);
		}
		
		
		NotificationCompat.Builder mBuilder;
		String title="";
		
		String pullDownText;
		String pullDownTitle;
		
		public Notification createNotification(String statusBarTitle,String pullDownTitle){//,String filename) {
			Intent notificationIntent = new Intent();//,NotificationDisplay.class);
			PendingIntent intent= PendingIntent.getActivity(context, 0, notificationIntent, 0);
	    //   this.pullDownText=pullDownText;	       
			this.pullDownTitle=pullDownTitle;
		//	pullDownText=filename;
			mBuilder.setTicker(statusBarTitle).setContentTitle(pullDownTitle).setContentText(pullDownText).
			setSmallIcon(android.R.drawable.stat_notify_sync).setOngoing(true).setContentIntent(intent);
			mNotificationManager.notify(NOTIFICATION_ID,mBuilder.build());
			return mBuilder.build();

		}
		public void setFileName(String name){
			pullDownText=name;
			
		}
		public void progressUpdate(int items) {
			mBuilder.setContentText(pullDownText+":"+"\n"+items+context.getString(R.string._processed));
			mNotificationManager.notify(NOTIFICATION_ID,mBuilder.build());
		}
		public void clearContent(){
			mBuilder.setContentText("");
			mNotificationManager.notify(NOTIFICATION_ID,mBuilder.build());
		}
		
		 public void completed()   {
			Intent notificationIntent = new Intent();//,NotificationDisplay.class);
			PendingIntent intent= PendingIntent.getActivity(context, 0, notificationIntent, 0);

			NotificationCompat.Builder mBuilderCompleted=new NotificationCompat.Builder(context);
			mBuilderCompleted.setContentText(context.getString(R.string.import_complete)).setSmallIcon(android.R.drawable.stat_notify_sync_noanim).
			setOngoing(false).setContentTitle(pullDownTitle).setContentIntent(intent);
			mNotificationManager.notify(COMPLETE_NOTIFICATION_ID++,mBuilderCompleted.build());
		 }
	
		
		
		
		public void error(String pullDownText){
			Intent notificationIntent = new Intent();//,NotificationDisplay.class);
			PendingIntent intent= PendingIntent.getActivity(context, 0, notificationIntent, 0);

			NotificationCompat.Builder mBuilderError=new NotificationCompat.Builder(context);
			mBuilderError.setContentText(pullDownText).setSmallIcon(android.R.drawable.stat_notify_error).//stat_sys_download_done).
			setOngoing(false).setContentTitle(pullDownTitle).setContentIntent(intent);
			//mBuilder.setProgress(0,0,false).setContentText(pullDownText).setSmallIcon(android.R.drawable.stat_notify_error).setOngoing(false);
			mNotificationManager.notify(COMPLETE_NOTIFICATION_ID++,mBuilderError.build());
		}
	}
	private class NoteDbFillerInfoListImpl extends InfoListImpl {
		
		
		private static final String TAG="SqlDbFillerInfoListImpl";@MarkTAG
		NoteDatabase catalog;
		NotificationHelper nh;
		public NoteDbFillerInfoListImpl(String name,NoteDatabase catalog,NotificationHelper nh){
			super(name,NoteRecord.class);
			this.catalog=catalog;
			this.nh=nh;
			
		}
		@Override
		public synchronized ErrorHandler load(InfoListLoader listLoader){
			
			int line=1;
			try{
				name=listLoader.getName();
			}
			catch(IOException e){
				
				return new ErrorHandler(ErrorHandler.IO_ERROR,"");
			}
			Object obj=null;
			ErrorHandler eh=new ErrorHandler();
			while(true){
				try{			
					ErrorHandler.ErrorRec erec=new ErrorHandler.ErrorRec();
					erec.lineNum=line;
					obj=listLoader.next(erec);
				//	Log.d(TAG,"obj="+obj);
					if(obj==null){
						eh.addError(erec);//error from listLoader processing
						continue;
					}
					if(objClass!=null){
						try{
						//	Object o=objClass.cast(obj);
							Object o=objClass.cast(obj);
							if(o!=null)
								catalog.add((NoteRecord)o,eh); 
						
						}
						catch(ClassCastException e){
							//Log.d(TAG,"Incompatible import format. "+e); 
							eh.addError(ErrorHandler.WRONG_TYPE, CLASS_CAST_ERROR,"",line);
						}
					}
					
				}			
				catch(IOException e){
					Log.d(TAG,"IOException="+AstroTools.getStackTrace(e));
					if(!(e instanceof EOFException)){
						eh.addError(ErrorHandler.IO_ERROR,"","",line);
					}
					try{
						listLoader.close();
					}
					catch(IOException e1){}
					
					return eh;
				}
				if(line%100==0){
					nh.progressUpdate(line);
				}
				line++;
			}
			
			
		}
	}
	
	private class SqlDbFillerInfoListImpl extends InfoListImpl {
		
		
		private static final String TAG="SqlDbFillerInfoListImpl";@MarkTAG
		AstroCatalog catalog;
		NotificationHelper nh;
		private boolean update=false;
		/**
		 * filling the db, not checking if the object exists in db already
		 * @param name
		 * @param objClass
		 * @param catalog
		 * @param nh
		 */
		public SqlDbFillerInfoListImpl(String name,Class objClass,AstroCatalog catalog,NotificationHelper nh){
			super(name,objClass);
			this.catalog=catalog;
			this.nh=nh;
			
		}
		/**
		 * 
		 * @param name
		 * @param objClass
		 * @param catalog
		 * @param nh
		 * @param update - update object if the object with the same name exists
		 */
		public SqlDbFillerInfoListImpl(String name,Class objClass,AstroCatalog catalog,NotificationHelper nh,boolean update){
			super(name,objClass);
			this.catalog=catalog;
			this.nh=nh;
			this.update=update;
			
		}
		@Override
		public synchronized ErrorHandler load(InfoListLoader listLoader){
			
			int line=1;
			try{
				name=listLoader.getName();
			}
			catch(IOException e){
				
				return new ErrorHandler(ErrorHandler.IO_ERROR,"");
			}
			Object obj=null;
			ErrorHandler eh=new ErrorHandler();
			catalog.beginTransaction();
			while(true){
				try{			
					ErrorHandler.ErrorRec erec=new ErrorHandler.ErrorRec();
					erec.lineNum=line;
					obj=listLoader.next(erec);
				//	Log.d(TAG,"obj="+obj);
					if(obj==null){
						eh.addError(erec);//error from listLoader processing
						continue;
					}
					if(objClass!=null){
						try{
						//	Object o=objClass.cast(obj);
							Object o=castObject(obj);
							if(o!=null){
								if(!update)
									catalog.add((CustomObject)o,eh);
								else{
									CustomObject ao=(CustomObject)o;
									List<AstroObject>list=catalog.searchName(ao.getShortName());
								//	String name=ao.getShortName();
								//	List<AstroObject>list=catalog.search(CustomDatabase.NAME1+"="+name);
									if(list.size()==0){
										catalog.add(ao,eh);
										Log.d(TAG,"empty db at obj="+o);
									}
									else{
										if(catalog instanceof CustomDatabaseLarge){
											CustomDatabaseLarge cat=(CustomDatabaseLarge)catalog;
											//cat.edit((CustomObject)list.get(0)); - error. old object was put again!!!
											for(AstroObject ob:list){
												ao.id=ob.id;
												cat.edit(ao);
											}
											
										}
									}
								}
									
							}
						
						}
						catch(ClassCastException e){
							//Log.d(TAG,"Incompatible import format. "+e); 
							eh.addError(ErrorHandler.WRONG_TYPE, CLASS_CAST_ERROR,"",line);
						}
					}
					
				}			
				catch(IOException e){
					Log.d(TAG,"IOException="+AstroTools.getStackTrace(e));
					if(!(e instanceof EOFException)){
						eh.addError(ErrorHandler.IO_ERROR,"","",line);
					}
					try{
						listLoader.close();
					}
					catch(IOException e1){}
					
					try {
						catalog.setTransactionSuccessful();
					} 
					catch(Exception e2){

					}
					finally {
						catalog.endTransaction();
					}
					
					
					return eh;
				}
				
				if(line%100==0){
					nh.progressUpdate(line);
				}
				line++;
			}
			
			
		}
	}
}
