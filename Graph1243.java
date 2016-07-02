package com.astro.dsoplanner;

import static com.astro.dsoplanner.Global.ALEX_MENU_FLAG;
import static java.lang.Math.PI;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.astro.dsoplanner.CuV.TopMessage;
import com.astro.dsoplanner.CuV.UploadRec;
import com.astro.dsoplanner.InputDialog.OnButtonListener;
import com.astro.dsoplanner.alexmenu.alexMenu;
import com.astro.dsoplanner.alexmenu.alexMenu.OnMenuItemSelectedListener;
import com.astro.dsoplanner.alexmenu.alexMenuItem;
import com.astro.dsoplanner.d.DSSdownloadable;
import com.astro.dsoplanner.d.DownloadService;
import com.astro.dsoplanner.d.DownloadService.LocalBinder;


public class Graph1243 extends ParentActivity implements OnMenuItemSelectedListener, OnGestureListener {
	
	
	
	
	@ObfuscateOPEN
	private static final String UGC = "UGC";
	//private static final String SEARCH = R.string.search;
	//private static final String RANGE = R.string.layers;
	private static final String OFF = "Off";
	private static final String LEVEL2 = "Level";
	private static final String COMPASS_LEVEL = "Compass+Level";
	//private static final String COMPASS2 = R.string.compass;
	private static final int NBY_DIALOG = 15;	
	private static final String _0_06 = "0.06";
	private static final String _0_12 = "0.12";
	private static final String _0_25 = "0.25";
	private static final String _0_5 = "0.5";
	private static final String _1 = "1";
	private static final String _2 = "2";
	private static final String _5 = "5";
	private static final String _10 = "10";
	private static final String _20 = "20";
	private static final String _30 = "30";
	private static final String _45 = "45";
	private static final String _60 = "60";
	private static final String _90 = "90";	
	public static final String CALIBR_ORIENT = "CalibrOrient";
	private static final String _120 = "120";
	//private static final String SETTINGS = R.string.settings;
	//private static final String BOLDNESS = R.string.boldness;
	//private static final String RA_DEC = R.string.ra_dec;
	//private static final String ALIGN_STAR = R.string.align_star;
	//private static final String COMPASS = COMPASS2;
	//private static final String SCOPE_GO = R.string.scope_go;
	/*private static final String DSS = R.string.dss;
	private static final String NEARBY = R.string.nearby;
	private static final String EP_FOV = "EP FOV";
	private static final String TELRAD = R.string.telrad;
	private static final String ROTATE_180 = R.string.rotate_180_;
	private static final String MIRROR = R.string.mirror;
	private static final String _1_HOUR2 = R.string._1_hour2;
	private static final String _1_HOUR = R.string._1_hour;
	private static final String CENTER = R.string.center;*/
	private static final String NOW = "NOW ";
	private static final String NO_DATA = "No Data";
	private static final String R_D_02D_02_0F_S_02D_02_0F = "R/D:%02d %02.0f%s%02d %02.0f";
	private static final String RA = "ra ";
	private static final String FILENAME2 = "filename ";
	@ObfuscateCLOSE
	
	public static boolean redrawRequired=false;
	
	
	static class DssImageRec{
		double ra;
		double dec;
		String name;
		public DssImageRec(double ra, double dec, String name) {
			super();
			this.ra = ra;
			this.dec = dec;
			this.name = name;
		}
	}
	
	static interface DssImage{
		DssImageRec getInfo();
		void select();
		void delete();
		boolean isSelected();
		boolean isPotential();//rectangle depicting potential dss image
	}

	private CuV cView;//here we draw the sky 
	private RelativeLayout layout;
	private BSp mSpinner;//here we set FOV
	public static  String[] spinArr=new String[]{_90,_60,_45,_30,_20,_10,_5,_2,_1,_0_5,_0_25,_0_12,_0_06};
	private  String[] spinArr2=new String[]{_90,_60,_45,_30,_20,_10,_5,_2,_1,"30'","15'","7.2'","3.6'"};

	private static final String TAG="Graph";@MarkTAG
	private MyDateDialog dd;//used for running set date and set time requests
	private static final int raDecDialog=3;
	private GraphRec prefs;
	private Artv mTextRT;//Time
	private Artv mTextRB;//here we show RA and DEC
	private Artv mTextLT;//here we show obj name
	private Artv mTextLB;//here we show az and alt
	private final int GET_CODE=2;//for running dateTimePicker activity
	private final int SR_CODE=3;//for returning from search
//	private SensorManager myManager = null;
	private SensorEventListener mySensorListener=null;
	private SensorProcessor sensorProcessor=null;//new SensorProcessor();
	//private GyroSensorProcessor gyroSensorProcessor=null;//new GyroSensorProcessor();
	private int mPrevPos=0;//previous position of spinner
	private volatile Bitmap dssBitmap;
	private Thread dssThread=null;
	private StarBoldness boldnessDialog = null;
	private StarMags starmagsDialog=null;
	private alexMenu aMenu;
	private View bottomBar;

	boolean realtimeMode = false;
//	boolean nightMode = false;
	boolean mBound = false;
	DownloadService mService;
	InputDialog mZoomDialog;
    List<AstroObject> listNearby=new ArrayList<AstroObject>();
    AstroObject raDecCenter=null;//used by raDecDialog and cView.initDsoObjects
	volatile List<AstroObject> threadListNearby=new ArrayList<AstroObject>();
	
    /**
     * connection to download service
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            Log.d(TAG,"onServiceConnected");
        	// We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
	
    BroadcastReceiver geoReceiver=new BroadcastReceiver(){	
		@Override
		public void onReceive(Context context, Intent intent) {
			if(cView!=null)//to try avoiding crash
				cView.sgrChanged(false);
		}
	};
	
	private void registerReceivers(){
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.GEO_BROADCAST);
		LocalBroadcastManager.getInstance(this).registerReceiver(geoReceiver,filter);
		
		filter = new IntentFilter();
		filter.addAction(Constants.BTCOMM_RADEC_BROADCAST);
		LocalBroadcastManager.getInstance(this).registerReceiver(btcommReceiver,filter);
		
		filter = new IntentFilter();
		filter.addAction(Constants.DOWNLOAD_BROADCAST);
		LocalBroadcastManager.getInstance(this).registerReceiver(downloadReceiver,filter);
		
	}
	
	private void unregisterReceivers(){
		LocalBroadcastManager.getInstance(this).unregisterReceiver(geoReceiver);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(btcommReceiver);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(downloadReceiver);
		cView.finishServiceConnection();
	}
	
	BroadcastReceiver downloadReceiver=new BroadcastReceiver(){	
		@Override
		public void onReceive(Context context, Intent intent) {
			int value=intent.getIntExtra(Constants.GRAPH_DOWNLOAD_STATUS, -1);
			switch(value){
			case DSSdownloadable.REMOVE_DOWNLOAD_TEXT://remove download message
				cView.topMessage.set("", TopMessage.DSS_UPLOAD_PRIORITY);
				break;
			case DSSdownloadable.ADD_DOWNLOAD_TEXT:
				cView.topMessage.set(context.getString(R.string.dss_image_downloading), TopMessage.DSS_UPLOAD_PRIORITY);
				break;
				//cView.setMessage();
			case DSSdownloadable.UPDATE_SKY:
				 UploadRec u=CuV.makeUploadRec();
				 if(Point.getFOV()<=Settings1243.getDSSZoom() && Settings1243.isDSSon()) {
					 Global.dss.uploadDSS(u, cView, handler);
				 }
				break;
				
			}
		}
    };
    
    BroadcastReceiver btcommReceiver=new BroadcastReceiver(){	
		@Override
		public void onReceive(Context context, Intent intent) {
			
			double ra=intent.getDoubleExtra(Constants.BTCOMM_RA, -1);
			double dec=intent.getDoubleExtra(Constants.BTCOMM_DEC, -1);
			if(ra==-1&&dec==-1)return;
		//	Log.d(TAG,"scope handler, rec received="+rec);
			Point p=new Point(ra,dec);
			p.setXY();	
			p.setDisplayXY();
			Point.setCenterAz(p.getAz(), p.getAlt());
			setCenterLoc();
			UploadRec u=cView.makeUploadRec();
			cView.upload(u,false,-1);
			if(Point.getFOV()<=5.1&&Settings1243.isDSSon()) {
				//Global.context = Graph.this;
				Global.dss.uploadDSS(u, cView, handler);
			}	
		}
    };
    
    
	
/*	private Handler scopeHandler=new Handler(){
		@Override
		public void handleMessage(Message msg){
			if(msg.obj==null)
				return;
			RaDecRec rec=(RaDecRec)msg.obj;
		//	Log.d(TAG,"scope handler, rec received="+rec);
			Point p=new Point(rec.ra,rec.dec);
			p.setXY();	
			p.setDisplayXY();
			Point.setCenterAz(p.getAz(), p.getAlt());
			setCenterLoc();
			UploadRec u=cView.makeUploadRec();
			cView.upload(u);
			if(Point.getFOV()<=5.1&&PreferenceManager.getDefaultSharedPreferences(Global.getAppContext()).getBoolean("DSSOn", true)) {
				//Global.context = Graph.this;
				Global.dss.uploadDSS(u, cView, handler);
			}			
		}
	};*/
	
	private Handler handler=new Handler(){
		@Override 
		public void handleMessage(Message msg){

			switch(msg.arg1){
			case 1:
				listNearby=threadListNearby;
			//	cView.initDsoList();
				//for(Point p:listNearby)
				//	cView.starList.add(p);
				//	Log.d(TAG,"list Nearby array size="+listNearby.size());
				if(cView!=null)
					cView.invalidate();
				break;
			case 2:

				UploadRec u=CuV.makeUploadRec();
				if(Point.getFOV()<=Settings1243.getDSSZoom() && Settings1243.isDSSon()) {
				//	Global.context = Graph.this;
					Global.dss.uploadDSS(u, cView, handler);
				}
			//	Log.d(TAG,"handler reached");
				break;
			case 4://message from nearby 
				String message=getString(R.string.nearby_search);
				if(cView!=null)
					cView.topMessage.set(message,cView.topMessage.NEARBY_SEARCH);
				break;
			case 5:
				if(cView!=null)
					cView.topMessage.set("",cView.topMessage.NEARBY_SEARCH);
				break;
			case CuV.INVALIDATE:
				if(cView!=null)
					cView.invalidate();
				break;
				
				
			}
		}
	};
	
	private Handler initHandler=new Handler(){
		@Override 
		public void handleMessage(Message msg){
			sensorProcessor = new SensorProcessor();
			//	gyroSensorProcessor=new GyroSensorProcessor();

			prefs = new GraphRec(Graph1243.this);//loading prefs from shared preferences
			
			handler.removeCallbacks(mUpdateTimeTask);
			
			//GraphRec gr=Global.graphStack.removeFirst();

			dd = new MyDateDialog(Graph1243.this,AstroTools.getDefaultTime(Graph1243.this),new MyDateDialog.Updater(){//after setting date or time need to update the sky
				public void update(){
					Graph1243.this.update();
				}
			});
			//Date-time picker
		//	DateTimePicker.setCal(dd);

		//	Log.d(TAG,"dd="+dd.getDateTime());

			//MAIN SCREEN LAYOUT DEFINITION
			layout = new RelativeLayout(Graph1243.this);
			//layout.setOrientation(LinearLayout.VERTICAL);
			//setContentView(R.layout.topbar);

			//BOTTOM BAR GADGET
			//=========================
			LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
			bottomBar = vi.inflate(R.layout.topbar, null, false); 

			mTextLT  = (Artv) bottomBar.findViewById(R.id.tbL1);
			mTextRT  = (Artv) bottomBar.findViewById(R.id.tbR1);
			mTextLB  = (Artv) bottomBar.findViewById(R.id.tbL2);
			mTextRB  = (Artv) bottomBar.findViewById(R.id.tbR2);
			
			//spinner replacement
			mSpinner = (BSp)bottomBar.findViewById(R.id.zSpinner);
			mSpinner.init(Graph1243.this);
			
			mZoomDialog = new InputDialog(Graph1243.this);
			mZoomDialog.setType(InputDialog.DType.INPUT_DROPDOWN);
			mZoomDialog.setTitle("");
			mZoomDialog.setListItems(spinArr2, new InputDialog.OnButtonListener() {
				public void onClick(String value) {
					int pos=-1;
					for(int i=0;i<spinArr2.length;i++){
						if(spinArr2[i].equals(value)){
							pos=i;
							break;
						}
					}
					if(pos!=-1)
					mSpinner.stSelection(spinArr[pos]);
				}
			});
			
			mSpinner.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					if(Settings1243.isFOVcolumn(getApplicationContext()))
						registerDialog(mZoomDialog).show();
				}
			});
				
			/* SAND old style, nonskinnable
			ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,spinArr);
			if(Settings.getNightMode()){
				spinnerAdapter.setDropDownViewResource(R.layout.zspinbox_n);
			}
			else {
				spinnerAdapter.setDropDownViewResource(R.layout.zspinbox_d);
			}

			mSpinner.setAdapter(spinnerAdapter);

			// mSpinner.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT));
			mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				public void onItemSelected(AdapterView<?> parent,
						View view, int pos, long id) {
					double fov=Double.parseDouble(spinArr[pos]);
				//	Global.currentZoomLevel=pos;
					Point.setFOV(fov);//resetting fov
					cView.FOVchanged();
					StarBoldness.setFov(fov);
					//  cView.invalidate();
				}
				public void onNothingSelected(AdapterView<?> parent) {
					// Do nothing.
				}
			});
			 */
			//Zoom buttons, etc
			OnClickListener ocl1 = new OnClickListener() {
				public void onClick(View v){ zoomChange(true); }
			};
			OnClickListener ocl2 = new OnClickListener() {
				public void onClick(View v){ zoomChange(false); }
			};
			OnLongClickListener ocl3 = new OnLongClickListener() {
				public boolean onLongClick(View v) {
					//openOptionsMenu(); SAND old menu style
					if(ALEX_MENU_FLAG) doMenu(v);
					return true;
				}
			};
			TextView b1 = (TextView)bottomBar.findViewById(R.id.zIn);
			b1.setOnClickListener(ocl1);
			b1.setOnLongClickListener(ocl3);
			
			TextView b2 = (TextView)bottomBar.findViewById(R.id.zOut);
			b2.setOnClickListener(ocl2);
			b2.setOnLongClickListener(ocl3);
			
			mSpinner.setOnLongClickListener(ocl3);

			//Top bar's L and R zones
			OnClickListener oclTop = new OnClickListener() {
				public void onClick(View v){ topBarClick(v); }
			};
			View bL = bottomBar.findViewById(R.id.leftPane);
			bL.setOnClickListener(oclTop);
			View bR = bottomBar.findViewById(R.id.rightPane);
			bR.setOnClickListener(oclTop);
			bottomBar.bringToFront();


			//BOLDNESS GADGET (HIDDEN)
			//=========================
			//View bv = vi.inflate(R.layout.boldness, null, false);
			boldnessDialog = new StarBoldness(Graph1243.this);//StarBoldness.getInstance(Graph1243.this);
			boldnessDialog.Init();
			boldnessDialog.bringToFront();
			
			starmagsDialog=new StarMags(Graph1243.this);//StarMags.getInstance(Graph1243.this);
			starmagsDialog.Init();
			starmagsDialog.bringToFront();

			//CUSTOM VIEW (SKY)
			//=========================
			cView = (CuV)bottomBar.findViewById(R.id.sky_view); //new CustomView(this);
			//cView.stWind(Graph1243.this);
			//cView.layout(0, topBar.getBottom(), topBar.getRight(), boldnessDialog.getBottom());
			//LayoutParams lParams = layout.getLayoutParams();
			//lParams.height = LayoutParams.FILL_PARENT;
			//cView.setLayoutParams(lParams );
			cView.global_object=Settings1243.getObjectFromSharedPreferencesNew(Constants.GRAPH_OBJECT, Graph1243.this);
			Log.d(TAG,"global_object="+cView.global_object);
			//Compose layouts
			//layout.addView(cView); //Stars
			layout.addView(bottomBar);
			layout.addView(boldnessDialog); //hidden boldness control
			layout.addView(starmagsDialog);
			
			if(Settings1243.getInverseSky()&&!nightMode)
				layout.setBackgroundColor(0xffffffff);
			else
				layout.setBackgroundColor(0xff000000);
			
			setContentView(layout);
			layout.requestFocus(); 
	//		Log.d(TAG,"layout set");
			
	//		myManager = (SensorManager)getSystemService(SENSOR_SERVICE);
			
			//mSpinner.setSelection(2); //45 deg (fix blank screen on first start)
			//init(gr);

			//set time

			//  Point.setLST(AstroTools.sdTime(Global.cal));
			//   Log.d(TAG,"year="+year+" month="+month+" day="+day+" hour="+hour+" min="+minute);
			//    Log.d(TAG,"sdTime="+AstroTools.sdTime(year,month,day,hour,minute));
			// Global.sd.raiseDatabaseNewPointFlag();

			//Object cursor
			ObjCursor.setParameters(PreferenceManager.getDefaultSharedPreferences(Global.getAppContext()));
			
			//CUSTOM ALEX MENU
			if (ALEX_MENU_FLAG) initAlexMenu(Graph1243.this);
			
			Global.lockCursor = false;
          //  Settings.setBrightnessFlick();
			Settings1243.setAntialiasing();
			realTimeMode(Settings1243.isAutoTimeUpdating());
			//onResumeCode();
			if(Global.FREE_VERSION){
				showDSSDialog();
			}
			
			
			//menu.setVisibility(View.GONE);
			
			
			
			/*FloatingActionButton fabButton2 = new FloatingActionButton.Builder(Graph1243.this)
	        .withDrawable(getResources().getDrawable(R.drawable.ram_tplus))
	        .withButtonColor(0x10ff0000)
	        .withGravity(Gravity.BOTTOM | Gravity.LEFT)
	        .withMargins(5, 0,0, 124)
	        .create();*/
			
			
			
		}
	};
	
	
	private void showDSSDialog(){
		int count=Settings1243.getDSSCount();
		Log.d(TAG,"count="+count);		
		if(count>com.astro.dsoplanner.DSS.FREE_LIMIT){
			InputDialog d=new InputDialog(this);
			String message=getString(R.string.you_have_exceeded_the_dss_image_count_free_version_please_make_sure_that_there_is_not_more_than_)+(com.astro.dsoplanner.DSS.FREE_LIMIT-20)+getString(R.string._images_in_)+Global.DSSpath+getString(R.string._folder);
			d.setMessage(message);
			d.setPositiveButton(getString(R.string.ok));
			d.show();
		}
	}
	
	private static final int NO_UPDATE_HIGH_ZOOM=1;
	private static final int CENTER_HIGH_ZOOM=2;
	//timer updating screen
	private Runnable mUpdateTimeTask = new Runnable() {
		public void run() {
			if(!Settings1243.isAutoTimeUpdating()) return; //skip this if deselected
			handler.postDelayed(this, Settings1243.getAutoTimeUpdatePeriod()*1000); //in ms

			int rt=Settings1243.getSharedPreferences(Graph1243.this).getInt(Constants.REAL_TIME_DIALOG_CHOICE, 0);
			if(rt==NO_UPDATE_HIGH_ZOOM
					&&Point.getFOV()<=Settings1243.getRealTimeFOV())return;
			
						/*SAND trick to use autotime with custome user time
			long dt = Calendar.getInstance().getTimeInMillis() - dd.getCalendar().getTimeInMillis();
			if(dt<0) dt = -dt;
			if(dt>600000){ //5 minutes difference => user time.
				dd.plusTime(30); //so, add 30 sec to current (fake) time.
			}
			else{ //set current time
				dd.setDateTime(Calendar.getInstance());
			}
			*/
			dd.setDateTime(Calendar.getInstance());
			AstroTools.setDefaultTime(dd.getDateTime(),Graph1243.this);
			Point.setLST(AstroTools.sdTime(dd.getDateTime()));
			setTimeLabel();
			cView.sgrChanged(false);
			if(rt==CENTER_HIGH_ZOOM &&Point.getFOV()<=Settings1243.getRealTimeFOV())
				parseMenuEvent(R.id.center);
		}
	};
	
	/*	@Override
    public boolean onSearchRequested() {
		Log.d(TAG,"Search");
		startSearch(null,false,null,false);
		newActivity=true;
		return true;
	}*/
	 @Override
	    protected void onStart() {
	        super.onStart();
	        
	       
	        
	        	Intent intent = new Intent(this, DownloadService.class);
	        	bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	    
	        registerReceivers();
	 }
	 
	@Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
       
        	if (mBound) {
        		unbindService(mConnection);
        		mBound = false;
        	}
        
        unregisterReceivers();
        
    }
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		if(requestCode==GET_CODE&&resultCode==RESULT_OK){//from DateTimePicker
			long time=data.getLongExtra(Constants.DATE_TIME_PICKER_MILLIS, Calendar.getInstance().getTimeInMillis());
			Calendar c=Calendar.getInstance();
			
			c.setTimeInMillis(time);
			AstroTools.setDefaultTime(c, this);
			
			Calendar defc=c;//AstroTools.getDefaultTime(this);
			Point.setLST(AstroTools.sdTime(defc));  
			if(prefs!=null) prefs.c=defc;
			if(dd!=null) dd.setDateTime(defc);
			if(cView!=null) cView.sgrChanged(false);
			//SAND:new algotithm mTimeChanged=false;//returning to autoupdate based on the current time
			/*cView.invalidate();
			cView.sgrChanged(false);
			setTimeLabel();*/
		}
		if(requestCode==AstroTools.SR_CODE){//from search result activity
			restart();
		}
	}
	
	@Override
	protected void onPause(){
		super.onPause(); 
		Log.d(TAG,"onPause start");
		handler.removeCallbacks(mUpdateTimeTask);
		int zPos = mSpinner.gtSelectedItemPosition();
		
		//set centered for the constellation is false so that after new start star chart is not centered on the constellation
		prefs=new GraphRec(zPos, Point.getAzCenter(),Point.getAltCenter(),AstroTools.getDefaultTime(this),
				cView.gtObjCurs().getObjSelected(),Point.orientationAngle,Point.mirror,cView.getSelectedConBoundary(),false);
		prefs.save(this);
	//	Global.graphCreate=prefs;
		Settings1243.putSharedPreferences(Constants.CURRENT_ZOOM_LEVEL, zPos, this);
		//Global.currentZoomLevel = zPos;
		Global.dss.clearDssList();//freeing memory
		
		//	if(gyroSensorProcessor.isOn())//other sensors not registered by default
		//		gyroSensorProcessor.setOff();
		//	else{
		unregisterSensorListener();
		StarMags.putMagLimitsToSharedPrefs(this);
		StarBoldness.putBoldnesstoPrefs();
		StarUploadThread.cacheManager.stop();
		Log.d(TAG,"onPause stop");
	//	unregisterReceivers();
		//}
	}
	
	private double gx=0;
/*	
	 * this is a helper method for determining landscape orientation correctly
	 * it unregisters automatically
	
	private void registerOrientationHelper(){
		final SensorManager myManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		if(myManager!=null){
			SensorEventListener listener=new SensorEventListener(){
				public void onSensorChanged(SensorEvent event)
				{
					double vx=event.values[0];
					Log.d(TAG,"vx="+vx);
					if(Math.abs(vx)>0.2){
						gx=vx;
						Log.d(TAG,"vx="+vx+" set");
						myManager.unregisterListener(this);
					}							
							
				}
				public void onAccuracyChanged(Sensor sensor, int accuracy) {
					//Log.d(TAG,"onAccuracy changed"+" sensor="+sensor+" accuracy="+accuracy);
				}
			};
			
			myManager.registerListener(listener, myManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
					SensorManager.SENSOR_DELAY_UI);
		}
	}*/
	
	
	
	
	boolean listenerRegistered=false;
	private void registerSensorListener(){
		if(listenerRegistered)return;
		if(mySensorListener==null&&(Settings1243.isAutoSkyOn()||Settings1243.isAutoRotationOn())){//||Settings.isLightSensorOn()
			sensorProcessor.init();
			mySensorListener = new SensorEventListener() {

				public void onSensorChanged(SensorEvent event)
				{
					sensorProcessor.processEvent(event);
				}
				public void onAccuracyChanged(Sensor sensor, int accuracy) {
					//Log.d(TAG,"onAccuracy changed"+" sensor="+sensor+" accuracy="+accuracy);
				}
			};
			SensorManager myManager = (SensorManager)getSystemService(SENSOR_SERVICE);

			if(myManager!=null){
					listenerRegistered=true;
					myManager.registerListener(mySensorListener,
							myManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
							SensorManager.SENSOR_DELAY_UI);

					myManager.registerListener(mySensorListener,
							myManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
							SensorManager.SENSOR_DELAY_UI);
				
					Log.d(TAG,"register sensor listener "+mySensorListener+" at "+Graph1243.this);
			}
		}
		
	}
	private void unregisterSensorListener(){
		SensorManager myManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		if (myManager!=null) {
			
			if(mySensorListener!=null){
				listenerRegistered=false;
				myManager.unregisterListener(mySensorListener);	
				Log.d(TAG,"unregister sensor listener "+mySensorListener+" at "+Graph1243.this);
				mySensorListener=null;
				Point.setRotAngle(0);
			}

		}
	}
	/*class GyroSensorProcessor{
		private static final float NS2S = 1.0f / 1000000000.0f;
		static final int MaxCount=10;
		private int count=0;

		private long timestamp=0;
		private final DMatrix eM=new DMatrix(new Line(1,0,0),new Line(0,1,0),new Line(0,0,1));
		private DMatrix totalMatrix=eM;
		private double altAdj=0;

		private boolean isOn=false;
		void processEvent(SensorEvent event){
			int type = event.sensor.getType();
			if(type==Sensor.TYPE_ACCELEROMETER){
				if((altInit!=0)||(azInit!=0)){
					float[] accels = event.values;
					double alt=90-asin(accels[1]/sqrt(accels[0]*accels[0]+accels[1]*accels[1]+accels[2]*accels[2]))*180/PI;
					altAdj=altInit-alt;
					double anY=0;//asin(accels[0]/sqrt(accels[0]*accels[0]+accels[1]*accels[1]+accels[2]*accels[2]))*180/PI;
					totalMatrix=new DMatrix(Axis.Z,azInit*PI/180).timesMatrix(new DMatrix(Axis.X,-(90+alt)*PI/180)).timesMatrix(new DMatrix(Axis.Z,-anY));
					altInit=0;
					azInit=0;
				}
			}

			if(type==Sensor.TYPE_GYROSCOPE){				
				count++;
				float[] ws=event.values;
				double dT=0;
				if(timestamp!=0)
					dT = (event.timestamp - timestamp) * NS2S;
				timestamp=event.timestamp;
				DMatrix mX=new DMatrix(Axis.X,-(ws[0]-Global.wavgx)*dT);
				DMatrix mY=new DMatrix(Axis.Y,-(ws[1]-Global.wavgy)*dT);
				DMatrix mZ=new DMatrix(Axis.Z,-(ws[2]-Global.wavgz)*dT);
				DMatrix mT=mZ.timesMatrix(mY).timesMatrix(mX);
				totalMatrix=totalMatrix.timesMatrix(mT);
				if (count>MaxCount){
					setCenter();
					count=0;
				}
				//Log.d(TAG,totalMatrix.toString());
			}
		}
		void setOff(){
			isOn=false;
			if (myManager!=null) {
				myManager.unregisterListener(mySensorListener);	
				InputDialog.message("Gyro tracking is off").show();


			}
		}
		void setOn(){
			isOn=true;
			count=0;
			timestamp=0;
			totalMatrix=eM;

			mySensorListener = new SensorEventListener() {

				public void onSensorChanged(SensorEvent event)
				{
					gyroSensorProcessor.processEvent(event);
				}
				public void onAccuracyChanged(Sensor sensor, int accuracy) {
					//Log.d(TAG,"onAccuracy changed"+" sensor="+sensor+" accuracy="+accuracy);
				}
			};
			if(myManager!=null){

				myManager.registerListener(mySensorListener,
						myManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
						SensorManager.SENSOR_DELAY_FASTEST);
				myManager.registerListener(mySensorListener,
						myManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
						SensorManager.SENSOR_DELAY_FASTEST);
				InputDialog.message(Global.context,"Gyro tracking is on" ,Toast.LENGTH_SHORT).show();


			}
		}
		boolean isOn(){
			return isOn;
		}


		double altInit=0;
		double azInit=0;

		void setInitialPos(ObjectInfo obj){
			Point p=obj.getCurrentRaDec(Global.cal);
			altInit=p.getAlt();
			azInit=p.getAz();
		}
		void setCenter(){//m - matrix from gyro
			DVector vec=new DVector(0,0,-1);
			DVector v1=totalMatrix.timesVector(vec);
			double alt=180/PI*(Math.asin(v1.z))+altAdj;
			double az=180/PI*Math.atan2(v1.x,v1.y);

			Point.setCenterAz(az, alt);
			cView.deltaX=Point.getWidth();//to start upload in CustomView
			cView.deltaY=cView.deltaX;
			setCenterLoc();
			cView.invalidate();
			Log.d(TAG,"az="+az+" alt="+alt);
		}
	}*/

	private class SensorProcessor{
		private float[] mags = new float[3];
		private float[] accels = new float[3];
		private float[] rm = new float[9];
		private float[] InclinationMat = new float[9];
		private final float declination=new GeomagneticField((float)Settings1243.getLattitude(),
				(float)Settings1243.getLongitude(),0,Calendar.getInstance().getTimeInMillis()).getDeclination();
		DMatrix m;


		private DVector uploadVector=null;
		private int Nattempts=3;
		private double maxDst=0.2;//threshold for skipping stray points
		private int attempts=0;
		private DVector v1prev=null;
		private DVector v2prev=null;
		private double alfa=0.1;//weight of the new

		private class Holder{
			DVector x;//storing DVector(0,0,-1);
			DVector y;//DVector(0,1,0)
			public Holder(DVector x,DVector y){
				this.x=x;
				this.y=y;
			}
		}
		private ArrayList<Holder> list=new ArrayList<Holder>();
		private boolean nadirDisabled;
		private double mACorrection = 0;
		//int orientation;
		int rotation;
		Display display;
		public void init(){
			Nattempts = Settings1243.getASnattempts();
			maxDst = Settings1243.getASmaxdst();
			alfa = Settings1243.getASalpha();
			nadirDisabled = Settings1243.getANadir();
			mACorrection  = Settings1243.getAScorangle();
			//orientation=getResources().getConfiguration().orientation;
			display = getWindowManager().getDefaultDisplay();
			
		}
		
		double valuex=0;
		public void processEvent(SensorEvent event){
			int type = event.sensor.getType();
			if(type == Sensor.TYPE_MAGNETIC_FIELD || type == Sensor.TYPE_ACCELEROMETER) {
				//Log.d(TAG,"processing event "+Graph1243.this);
				if(cView==null)return;
				if(type == Sensor.TYPE_MAGNETIC_FIELD) {
					mags = event.values;
				}
				if(type == Sensor.TYPE_ACCELEROMETER) {
					accels = event.values;
					//Log.d(TAG,accels[0]+" "+" "+accels[1]+" "+accels[2]);
				}
				if(nadirDisabled){//don't move if the device is not pointing to the sky
					if(accels[2]>0) return;
				}
				float gsq=(accels[0]*accels[0]+accels[1]*accels[1]+accels[2]*accels[2]);
				if(gsq>200) return;
				
				if(valuex!=0) valuex=accels[0];
				if((mags==null)||(accels==null)) return;

				SensorManager.getRotationMatrix(rm,	InclinationMat, accels, mags);

				m=new DMatrix(new Line(rm[0],rm[1],rm[2]),
						new Line(rm[3],rm[4],rm[5]),new Line(rm[6],rm[7],rm[8]));
				//m=m.timesMatrix(new DMatrix(Axis.Y,declination));
				DVector vec=new DVector(0,0,-1); //vector in the system connected with the phone. y-along the larger side, z - from the screen
				DVector v1=m.timesVector(vec);//the same vector in the system connected with the earth system. y - pointing to north and tangenial to the ground,z - to zenith
				//used for getting direction to the sky part to be shown

				/*	vec=new DVector(1,0,0);			//used for getting tilt of the phone			
					DVector v2=m.timesVector(vec);*/

				DVector v2=m.backMatrix().timesVector(new DVector(0,0,1));//vector z==-g pointing to zenith transferred to phone system of coords
				if(v1.isValid() && v2.isValid())		
					setCurrVector(v1,v2);
				else
					Log.d(TAG,"v1 or v2 not valid "+v1+" "+v2);

				/*attempts--;
				list.add(new Holder(v1,v2));
				if(attempts==0){
				attempts=Nattempts;
				setCurrVector();
				list=new ArrayList<Holder>();
			}*/
			}
		}

		private void setCurrVector(DVector v1,DVector v2){//v1 for direction, v2 for tilt
			if(v1prev!=null && v1prev.length()<1.1){
				DVector diff=v1.minusVector(v1prev);//v1-v1prev
				v1=v1prev.plusVector(diff.timesValue(alfa));				
			}
			v1prev=v1;
			v1prev.normalise();
			if(v2prev!=null && v2prev.length()<1.1){
				DVector diff=v2.minusVector(v2prev);//v1-v1prev
				v2=v2prev.plusVector(diff.timesValue(alfa));				
			}
			v2prev=v2;
			v2prev.normalise();
			attempts++;
			if(attempts<Nattempts)
				return;
			else
				attempts=0;


			double alt=180/PI*(Math.asin(v1.z));
			double az=180/PI*Math.atan2(v1.x,v1.y)+declination;
			//	Log.d(TAG,"setCurrVec"+v1+" "+v2);
			
			double correction=0;
		/*	if( orientation== Configuration.ORIENTATION_LANDSCAPE){
				if(gx>0){
					correction=-90;
				}
				if(gx<0)correction=90;
				
			}*/
			rotation=display.getRotation();
			if(rotation==Surface.ROTATION_90)
				correction=-90;
			if(rotation==Surface.ROTATION_270)
				correction=90;
			if(rotation==Surface.ROTATION_180)
				correction=180;
			if(Settings1243.isAutoRotationOn()){

				//DVector v=m.backMatrix().timesVector(new DVector(0,0,1));//vector z==-g pointing to zenith transferred to phone system of coords
				DVector vp=new DVector(v2.x,v2.y,0);//projection of z==-g to phone surface
				if(vp.length()<0.001) {
					Point.setRotAngle(0+mACorrection+correction);
					return;
				}

				vp.normalise();
				double angle=180/PI*Math.acos(vp.timesVector(new DVector(1,0,0)));	
				if(vp.timesVector(new DVector(0,1,0))<0)
					angle=180+(180-angle);
				/*	if(dv.z<0){//phone is upside down
					angle=180+(180-angle);
				}*/
				double rotan=-angle+90; //angle between g and shorter side of the phone

				
				
				Point.setRotAngle(rotan+mACorrection+correction);

			} 

			if(Settings1243.isAutoSkyOn()){
				Point.setCenterAz(az,alt); 
				double dst=1000;
				if (uploadVector!=null&&uploadVector.length()<2)//use length to cut out NaNs
					dst=v1.distance(uploadVector);
				if(dst*180/PI>Point.getFOV()/4){
					Log.d(TAG,"sensor upload");
					cView.deltaX=Point.getWidth();//to start upload in CustomView
					cView.deltaY=cView.deltaX;
					uploadVector=v1;
				}
			}
			setCenterLoc();
			cView.invalidate();


		}
		private void setCurrVector(){
			double sumx=0;
			double sumy=0;
			double sumz=0;
			for(Holder h:list){
				sumx=sumx+h.x.x;
				sumy=sumy+h.x.y;
				sumz=sumz+h.x.z;
			}
			DVector vAvg=new DVector(sumx/list.size(),sumy/list.size(),sumz/list.size());


			Iterator<Holder> it=list.iterator();			
			boolean flag=false;
			while(it.hasNext()){
				DVector v=it.next().x;
				if(v.distance(vAvg)>maxDst){
					it.remove();
					flag=true;
				}
			}

			if(flag){
				sumx=sumy=sumz=0;
				for(Holder h:list){
					sumx=sumx+h.x.x;
					sumy=sumy+h.x.y;
					sumz=sumz+h.x.z;
				}
				if(list.size()>0) {
					vAvg=new DVector(sumx/list.size(),sumy/list.size(),sumz/list.size());

				}
				else
					return;
			}

			vAvg.normalise();
			double alt=180/PI*(Math.asin(vAvg.z));
			double az=180/PI*Math.atan2(vAvg.x,vAvg.y)+declination;

			if(Settings1243.isAutoRotationOn()){
				/*	sumx=sumy=sumz=0;//calculating tilt of the phone
				for(Holder h:list){
					sumx=sumx+h.y.x;
					sumy=sumy+h.y.y;
					sumz=sumz+h.y.z;
				}
				DVector v2=new DVector(sumx/list.size(),sumy/list.size(),sumz/list.size());
				v2.normalise();*/
				DVector v=m.backMatrix().timesVector(new DVector(0,0,1));//vector z==-g pointing to zenith transferred to phone system of coords
				DVector vp=new DVector(v.x,v.y,0);//projection of z==-g to phone surface
				if(vp.length()<0.001) {
					Point.setRotAngle(0+mACorrection);
					return;
				}

				vp.normalise();
				double angle=180/PI*Math.acos(vp.timesVector(new DVector(1,0,0)));	
				if(vp.timesVector(new DVector(0,1,0))<0)
					angle=180+(180-angle);
				/*	if(dv.z<0){//phone is upside down
					angle=180+(180-angle);
				}*/
				double rotan=-angle+90; //angle between g and shorter side of the phone


				Point.setRotAngle(rotan+mACorrection);
			} 

			if(Settings1243.isAutoSkyOn()){
				Point.setCenterAz(az,alt); 
				double dst=1000;
				if (uploadVector!=null)
					dst=vAvg.distance(uploadVector);
				if(dst*180/PI>Point.getFOV()/4){
					Log.d(TAG,"sensor upload");
					cView.deltaX=Point.getWidth();//to start upload in CustomView
					cView.deltaY=cView.deltaX;
					uploadVector=vAvg;
				}
			}
			cView.invalidate();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	/*	if(initRequired){
			initRequired=false;
			return;
		}*/
		Log.d(TAG,"onResume start");
		Log.d(TAG,"cview="+cView);
		//if(finishedInOnResume)
		//	return;
		onResumeCode();
		Log.d(TAG,"time="+AstroTools.getDateFromCalendar(AstroTools.getDefaultTime(getApplicationContext())));
		Log.d(TAG,"onResume stop");
	}
	/**
	 * used to correctly manage global init
	 */
	private void onResumeCode(){
		//Global.context = this;
		if(redrawRequired){
			redrawRequired=false;
			restart();
		}
		
		StarUploadThread.cacheManager.init();
		Log.d(TAG,"onResume");
		init(prefs);
		cView.initDsoList();
		cView.initSettingsToDrawMap();
		if(realTimeMode(Settings1243.isAutoTimeUpdating())){
			handler.postDelayed(mUpdateTimeTask, 2000);
		}
		
		/* if(!Settings1243.getTychoStatus()){
			cView.clearTychoList();
			cView.clearTychoListShort();
		}
		if(!Settings1243.getUcac2Status())
			cView.clearUcac2List();
		if(!Settings1243.getUcac4Status())
			cView.clearUcac4List();
		if(!Settings1243.getPgcStatus()){
			cView.clearPgcList();
		}
		
		if(!Settings1243.getNgcIcStatus()){
			cView.clearNgcList();
		}
		if(!Settings1243.isConBoundaryOn(getApplicationContext())){
			cView.clearConBoundariesList();
		}
		
		if(!Settings1243.isMilkyWayOn(getApplicationContext()))
			cView.clearMilkyWayList();*/
		cView.clearListsIfRequired();
		
		//	Log.d(TAG,"1");
		//cView.sgrChanged(false);//say that time has changed to update		
		//SAND setTimeLabel() - done above in (realTimeMode)
		setCenterLoc();
		//	Log.d(TAG,"2");
		UploadRec u=CuV.makeUploadRec(); 
		if(Point.getFOV()<=Settings1243.getDSSZoom()&&Settings1243.isDSSon()) {
			//	Global.context = this;
			Global.dss.uploadDSS(u, cView, handler);
		}
		Point.setRotAngle(0);
		//	Log.d(TAG,"3");
		//boolean gyroOn=Settings.getGyroPreference()&&gyroSensorProcessor.isOn;
		//	if(Settings.getGyroPreference()&&gyroSensorProcessor.isOn){
		//		gyroSensorProcessor.setOn();

		//	}
		sensorProcessor.init();
		registerSensorListener();
		//registerOrientationHelper();
		//	registerReceivers();
		Settings1243.setAntialiasing();
		if(nearbyDialog){
			nearbyDialog=false;
			parseMenuEvent(R.id.nbyGraph);
		}
		//  Settings.setBrightnessFlick();
		//	if(AstroTools.changeNightModeIfNeeded(this,nightMode))return;
		//	Log.d(TAG,"onResume over");
		//parseMenuEvent(R.id.nbyGraph);
		
		/*if(fabButtonBack!=null){
			fabButtonBack.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {

					onKeyDownImpl(KeyEvent.KEYCODE_BACK, null);

				}
			});
		}*/
		setMenuBtnOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {

				doMenu(bottomBar);

			}
		});

	}
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case MyDateDialog.DATE_DIALOG_ID:
			return new DatePickerDialog(this,
					dd.mDateSetListener,
					dd.mYear, dd.mMonth, dd.mDay);
		case MyDateDialog.TIME_DIALOG_ID:
			Log.d(TAG,"hour="+dd.mHour);
			return new TimePickerDialog(this,
					dd.mTimeSetListener,
					dd.mHour, dd.mMinute,false);	
		case raDecDialog:
			final InputDialog d2 = new InputDialog(this);
			d2.setTitle(getString(R.string.set_center_s_ra_dec_2000_));
			d2.insertLayout(R.layout.getradec);
			d2.setPositiveButton(getString(R.string.ok), new OnButtonListener() {
				public void onClick(String s) {
					Double ra  = AstroTools.getRaDecValue(
							((EditText) (d2.findViewById(R.id.radec_ra))).getText().toString());
					Double dec  = AstroTools.getRaDecValue(
							((EditText) (d2.findViewById(R.id.radec_dec))).getText().toString());
					if((ra!=null)&&(dec!=null)){
						Point p=new Point(ra,dec);
						p.setXY();		            		
						Point.setCenterAz(p.getAz(), p.getAlt());
						setCenterLoc();
						raDecCenter=new CenterMarkObject(ra,dec);
						//raDecCenter=new CustomObject(AstroCatalog.CUSTOM_CATALOG,0,ra,dec,AstroTools.getConstellation(ra, dec),
						//		AstroObject.Custom,"",5,5,0,0,"center mark","center mark","");
						//cView.initDsoList();
						cView.upload(cView.makeUploadRec(),false,-1);
					}
				}
			});
			d2.setNegativeButton(getString(R.string.cancel));
			return d2;
		//case NBY_DIALOG:return getNearbySearchDialog();
		}
		
		return super.onCreateDialog(id);
	}
	
	private Dialog makeDSSselectionDialog(final Graph1243.DssImage image){
		final InputDialog d0 = new InputDialog(this);
		String[] sel;
		if(image.isPotential())
			sel= new String[]{getString(R.string.info),getString(R.string.download)};
		else
			sel=new String[]{getString(R.string.info),getString(R.string.remove)};
		d0.setTitle(getString(R.string.select_dss_action));
		d0.setPositiveButton(""); //disable
		d0.setValue("-1"); //remove checks
		d0.setListItems(sel, new InputDialog.OnButtonListener() {
			public void onClick(final String value) {
				final int i = AstroTools.getInteger(value, -1,-2,1000);
				if(i==-1) return; //nothing selected
				switch(i){
				case 0:
					DssImageRec rec=image.getInfo();
					String raStr=Details1243.doubleToGrad(rec.ra,'h','m');
					String decStr=Details1243.doubleToGrad(rec.dec,'\u00B0',(char)39);
					String fileName="".equals(rec.name)?"":FILENAME2+rec.name;
					String message=RA+raStr+"\n"+"dec "+decStr+"\nsize 30'\n"+fileName;
					registerDialog(InputDialog.message(Graph1243.this,message,0)).show();
					break;
				case 1:
					if(image.isPotential()){
						//download
						
						if(Global.FREE_VERSION){
							
							int count=Settings1243.getDSSCount();
							Log.d(TAG,"count="+count);		
							if(count>com.astro.dsoplanner.DSS.FREE_LIMIT-20){
								String mes=getString(R.string.sorry_but_you_have_reached_the_free_version_limit_would_you_like_to_open_google_play_to_purchase_dso_planner_pro_version_);
								registerDialog(AstroTools.getProVersionDialog(Graph1243.this,mes)).show();
								return;
							}
							
							
						}	
						
						if(mBound)
							Global.dss.downloadDSS(new Point(image.getInfo().ra,image.getInfo().dec), cView,mService);
					}
					else{
						image.delete();
						cView.invalidate();
					}
					break;
				}
			}
		});
		return d0;
	}

	private Dialog makeDSSdialog() {
		final InputDialog d0 = new InputDialog(Graph1243.this);
		String[] sel = {getString(R.string.single_image_small_object_),getString(R.string.nine_images_larger_object_),getString(R.string.show_dss_images),getString(R.string.show_dss_contours)};
        boolean on = true;
        if(Settings1243.isDSSon()){
            sel[2] = getString(R.string.hide_dss_images);
            on = false;
        }
        
        if(Settings1243.areDSScontoursOn()){
        	sel[3]=getString(R.string.hide_dss_contours);
        }
        final boolean setDSSon = on;

		d0.setTitle(getString(R.string.how_much_to_download_));
		d0.setPositiveButton(""); //disable
		d0.setValue("-1"); //remove checks
		d0.setListItems(sel, new InputDialog.OnButtonListener() {
			public void onClick(final String value) {
				Log.d(TAG,"makeDSSdialog, value="+value);
				final int i = AstroTools.getInteger(value, -1,-2,1000);
				if(i==-1) return; //nothing selected
				
				if(i==2){
					 Settings1243.setDSSon(setDSSon);//hide/show dss images
					 cView.initSettingsToDrawMap();
					 if(setDSSon){						
						if(Point.getFOV()<=5.1) {
						//	Global.context = Graph.this;
							UploadRec u=CuV.makeUploadRec();
							Global.dss.uploadDSS(u, cView, handler);
						}
					 }
					 else					 
						 cView.invalidate();
					 return;
				}
				else if(i==3){
					boolean contours=Settings1243.areDSScontoursOn();
					Settings1243.setDSScontours(!contours, getApplicationContext());
					cView.initSettingsToDrawMap();
					cView.invalidate();
					return;
				}
				InputDialog d = new InputDialog(Graph1243.this);
				d.setTitle(getString(R.string.please_confirm));
				d.setMessage(getString(R.string.the_download_is_about_to_start_));
				d.disableBackButton(true);
				d.setPositiveButton(getString(R.string.ok), new OnButtonListener() {
					public void onClick(String s) {
						ObjCursor o=cView.gtObjCurs();
						if (o!=null){
							//Global.context = Graph.this;
							switch(i){
							case 0:
								Global.dss.downloadDSS(o, cView,mService);
								break;
							case 1:
								Global.dss.downloadDSSimages(o, cView,mService);
								break;
                       //     case 2:
                       //         Settings.setDSSon(setDSSon);
                      //          break;
							}
							//popMessage("DSS image downloading");
							Log.d(TAG,"Start DSS "+ value + " " + i);
						}
					}
				});
				d.setNegativeButton(getString(R.string.cancel), new InputDialog.OnButtonListener() {
					@Override
					public void onClick(String value) {
						d0.dismiss();
					}
				});
				registerDialog(d).show();
			}
		});
		return d0;
	}

	//Show a text line at the top of sky view
/*	public void popMessage(final String string) {
		cView.setMessage(string);
	}*/
	
	@Override
	protected void onPrepareDialog(int id,Dialog dialog){
		switch (id){
		case MyDateDialog.TIME_DIALOG_ID:
			((TimePickerDialog)dialog).updateTime(dd.mHour, dd.mMinute);
			break;
		case MyDateDialog.DATE_DIALOG_ID:
			((DatePickerDialog)dialog).updateDate(dd.mYear, dd.mMonth, dd.mDay);
			break;
		}
	}

	private void update(){ //needed for MyDateDialog, sky updating

		Calendar defc=dd.getDateTime();	
		AstroTools.setDefaultTime(defc, this);
		Point.setLST(AstroTools.sdTime(defc));//setting static lst in Point class as sky is drawn on its basis
		// 	cView.invalidate();
		//SAND new algorithm mTimeChanged=true;
		cView.sgrChanged(false);
		setTimeLabel();
	}
	
	private Calendar def_time=null;//for calling from NoteList
	private boolean init_first_time_over=false;
	private void init(GraphRec gr){
		//Settings1243.setValidUntil(Details1243.checkLicense());
		if(gr==null) return;
		Point.setFOV(Double.parseDouble(spinArr[gr.FOV]));//resetting fov
		boldnessDialog.setFov(Point.getFOV());
		if(callingActivity==Constants.NOTE_LIST_GRAPH_CALLING&&!init_first_time_over){
			def_time=AstroTools.getDefaultTime(getApplicationContext());
			init_first_time_over=true;
		}
		
	//	Global.cal=gr.c;
		AstroTools.setDefaultTime(gr.c, this);
		if (cView.global_object!=null)
			cView.global_object.raiseNewPointFlag();//update internal calculations of az,alt as time has changed
		int obsList=Settings1243.getSharedPreferences(Graph1243.this).getInt(Constants.ACTIVE_OBS_LIST, InfoList.PrimaryObsList);
		Iterator it=ListHolder.getListHolder().get(obsList).iterator();
		for(;it.hasNext();){
			Object o=it.next();
			if(o instanceof ObsInfoListImpl.Item){
				((ObsInfoListImpl.Item)o).x.raiseNewPointFlag();
			}
		}  
		for(Planet pl:Global.planets){
			pl.raiseNewPointFlag();
			pl.recalculateRaDec(gr.c);
		}
		
		ObjCursor o=new ObjCursor(0,0);
		Point p;
		//setting object cursor on object
		
		
		p=gr.obj;//
		
		/*if(gr.selected_con!=GraphRec.NO_CONSTELLATION_SELECTED){
			cView.global_object=null;
			p=null;
		}*/
		
		if(p==null)
			p=cView.global_object;
		
		
	//	}
	/*	else{
			if(p!=null){
				o.setRaDec(p.getRa(), p.getDec());
				o.setObjSelected(p);
			}
		} */
		cView.stObjCurs(o);	

		Point.setLST(AstroTools.sdTime(gr.c)); //setting current time for looking through the sky 
		Point.setCurrentTime(gr.c);
		//Point.setCenter(Global.object.getRa(), Global.object.getDec());//setting object in the center
		//  if (Point.coordSystem)
		
		//this allows to center on last selected object if no object is passed
		//actually this never happens now as the only place from where the null object is passed
		//in dso main sky view now uses the last saved Global.graphCreate
		double azCenter=gr.azCenter;
		double altCenter=gr.altCenter;
		
		
		if(gr.obj==null&&cView.global_object!=null){
			azCenter=cView.global_object.getAz();
			altCenter=cView.global_object.getAlt();
		}
		Point.setCenterAz(azCenter, altCenter);	
		//ConFigure.raiseNewPointFlag();
		cView.conFigureRaiseNewPointFlag();
		
		if(gr.selected_con!=GraphRec.NO_CONSTELLATION_SELECTED){
			cView.initSelectedConBoundary(gr.selected_con,gr.set_centered);
			
		}
		
		
		mSpinner.stSelection(gr.FOV);//updating the screen
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putBoolean("destroyed", true);
		//new Prefs(this).save();
		Log.d(TAG,"onSaveInstanceState");
	}
	private boolean control(){
		class location{
			double lat;
			double lon;
			public location(double lat, double lon) {				
				this.lat = lat;
				this.lon = lon;
			}
			
		}
		location[] loc=new location[]{new location(53,41),new location(44,42)};
		double lattitude=Settings1243.getLattitude();
		double longitude=Settings1243.getLongitude();
		boolean flag=false;
		for(location l:loc){
			if((Math.abs(lattitude-l.lat)<2)&&(Math.abs(longitude-l.lon)<2)){				
				flag=true;
			}
		}
		return flag;
	}
	
	boolean initRequired=false;//global init
	int callingActivity=0;
	@Override
	public void onCreate(Bundle savedInstanceState) {
	//	Global.getAppContext() = getApplicationContext();
	//	if(savedInstanceState!=null){
	//		if(savedInstanceState.getBoolean("destroyed", false)){
				/*super.onCreate(savedInstanceState); 
				finish();
				Intent intent=new Intent(this,DSOmain.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				return;*/
				
		//	}
		//}
	/*	if (Init.initRequired())
			initRequired=true;
		nightMode = Settings.setDayNightSky(this);
		
		if(initRequired)
			Settings.setDayNightList(this);*/
		super.onCreate(savedInstanceState); 
		Log.d(TAG,"onCreate");
		nightMode = Settings1243.setDayNightSky(this);
		callingActivity=getIntent().getIntExtra(Constants.GRAPH_CALLING, 0);
		Log.d(TAG,"calling act on create="+callingActivity);
	/*	if(initRequired){
			if(Settings.getNightMode())
				setContentView(R.layout.progressnight);
			else
				setContentView(R.layout.progress);
			Init.runOnUi(this);
			new Init(this,initHandler).start();//starting global init thread
		}
		else{
			initHandler.handleMessage(null);
		}*/
		initHandler.handleMessage(null);
		
	}
	
	//What to do on zoom item click
	public void performZoomItemSelect(String value) {
		double fov=AstroTools.getDouble(value,-1,-1,200);
		Log.d(TAG,"value="+value+" fov="+fov);
		if(fov>0){
			//	Global.currentZoomLevel=pos;
			//AstroTools.logr("1");
			Point.setFOV(fov);//resetting fov
			cView.FOVchanged();
			//AstroTools.logr("2");
			boldnessDialog.setFov(fov);
			//AstroTools.logr("3");
			starmagsDialog.updateRangeSlider();
			//AstroTools.logr("4");
			//  cView.invalidate();
		}
	}

	//Top bar buttons common events handler
	private void topBarClick(View v){
		switch(v.getId()){
		case R.id.leftPane: //left side of the bar
			if(Settings1243.getCenterObjectStatus()){
				//first center it
				parseMenuEvent(R.id.center);
			}
			//then show the info graph
			parseMenuEvent(R.id.infoGraph);
			break;
		case R.id.rightPane: //right side of the bar
			//parseMenuEvent(R.id.setTimeGraph);
			parseMenuEvent(R.id.pickDateTime);
			break;
		}
	}

	//Left bottom field (object A/h)
	public void setLocationLabel(String s){ //updating az/alt text, called from custom view
		mTextLB.setText(s);
	}

	//Right bottom field (dynamic RA/DEC)
	public void setCenterLoc(){ //updating date/time text, called from custom view

		double dec=Point.getDec(Point.getAzCenter(), Point.getAltCenter());
		double ra=Point.getRa(Point.getAzCenter(),Point.getAltCenter());
		//SAND:Log.d(TAG,"center ra="+ra+" dec="+dec);
		Point p=new Point(ra,dec);
		p=AstroTools.precession(p,AstroTools.getDefaultTime(this));

		/*while(p.ra>=24) p.ra-=24;
		while(p.ra<0)   p.ra+=24;*/
		
		p.ra=(float)AstroTools.normalise24(p.ra);
		
		/*while(p.dec>=90) p.dec-=90;
		while(p.dec<=-90) p.dec+=90;*/
		
		p.dec=(float)AstroTools.normalise90(p.dec);
		
		boolean neg = (p.dec < 0);
		DMS r = AstroTools.d2dms(p.ra);
		DMS d = AstroTools.d2dms(neg?-p.dec:p.dec);

		mTextRB.setText(String.format(Locale.US,R_D_02D_02_0F_S_02D_02_0F, r.d, r.m+(r.s/60f+0.5), (neg?"-":"+"), d.d,d.m+(d.s/60f+0.5)));
	}

	//Left top field (obj name)
	public void setObjName(){ //updating date/time text, called from custom view
		/*String label = getString(R.string.no_data2);
		if (cView.gtObjCurs()!=null){
			Point obj = cView.gtObjCurs().getObjSelected();			
			boolean obslist_object=false;
			boolean indicate_obs=PreferenceManager.
					getDefaultSharedPreferences(this).getBoolean(getString(R.string.obs_colon), false);
			if (obj instanceof ObjectInfo){
				if(obj instanceof AstroObject){				
					
					
					AstroObject o=(AstroObject)obj;
					if(indicate_obs){
						for(AstroObject p:cView.objList){
							if(p.getCatalog()==o.getCatalog()&&p.getId()==o.getId()){
								obslist_object=true;
							}

						}
					}
					
					
					if(o.getCatalog()>NgcFactory.LAYER_OFFSET){
						AstroObject ob=getDbObj(o.getCatalog()-NgcFactory.LAYER_OFFSET,o.getId(),getApplicationContext());
						if(ob!=null){
							obj=ob;
						}
					}
				}
				
				label=((ObjectInfo)obj).getShortName()+(obslist_object?":":"");
			}
			
		}*/
		mTextLT.setText(cView.getSelectedObjectName());
	}

	//Right top field (date/time)
	public void setTimeLabel(){ //updating date/time text, called from custom view
		//	Log.d(TAG,"calendar="+Global.cal);
		Calendar defc=AstroTools.getDefaultTime(this);
		int rt=Settings1243.getSharedPreferences(Graph1243.this).getInt(Constants.REAL_TIME_DIALOG_CHOICE, 0);
		boolean rtb=true;
		if(rt==NO_UPDATE_HIGH_ZOOM
				&&Point.getFOV()<=Settings1243.getRealTimeFOV()){
			rtb=false;
		}

		if(realtimeMode&&rtb) //show time only with seconds
			mTextRT.setText(NOW+Details1243.makeTimeString(defc, true));
		else	
			mTextRT.setText(Details1243.makeShortDateTimeString(defc));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.graph_menu, menu);
		//setMenuBackground(); Disabled
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return parseMenuEvent(item.getItemId());
	}
	
	public static AstroObject getDbObj(int catalog,int id,Context context){
	/*	int pos=-1;
		InfoList iL=ListHolder.getListHolder().get(InfoList.DB_LIST);
		int i=0;
		for(Object o:iL){
			DbListItem item=(DbListItem)o;
			if(item.dbName.toUpperCase().equals(UGC)){
				pos=i;
				break;
			}
			i++;
		}
		if(pos==-1){
			
			return null;
		}
		DbListItem item=(DbListItem)iL.get(pos);
		
		*/
		
		DbListItem item=DbManager.getDbListItem(catalog);
		Log.d(TAG,"catalog="+catalog+" item="+item);
		AstroCatalog db;
		if(catalog==AstroCatalog.NGCIC_CATALOG)
			db=new NgcicDatabase(context);
		else if (item.ftypes.isEmpty())
			db=new CustomDatabase(context,item.dbFileName,catalog);
		else
			db=new CustomDatabaseLarge(context,item.dbFileName,catalog,item.ftypes);
		
		ErrorHandler eh=new ErrorHandler();
		db.open(eh);
		if(eh.hasError()){
			return null;
		}
		String s=Constants._ID+" = "+id;
		List<AstroObject> list=db.search(s);
		db.close();
		if(list.size()>0)
			return list.get(0);
		else
			return null;
	}
	
	private boolean compareUGCObjects(AstroObject o1,AstroObject o2){
		boolean resra=Math.abs(o1.getRa()-o2.getRa())<0.001;//&&
		boolean resdec=Math.abs(o1.getDec()-o2.getDec())<0.001;//&&
		boolean resa=Math.abs(o1.getA()-o2.getA())<0.001;//&&
		Log.d(TAG,o1.getA()+" "+o2.getA());
		boolean resb=Math.abs(o1.getB()-o2.getB())<0.001;//&&
		Log.d(TAG,o1.getB()+" "+o2.getB());
		boolean resname=o1.getShortName().equals(o2.getShortName());
		Log.d(TAG,"res="+resra+" "+resdec+" "+resa+" "+resb+" "+resname);
		return resra&&resdec&&resa&&resb&&resname;
	}
	public boolean parseMenuEvent(int id){
		switch (id) {
		case R.id.center://centering selected object
			ObjCursor oc=cView.gtObjCurs();
			if (oc!=null){
				//Point.setCenter(oc.getRa(), oc.getDec());
				if (Point.coordSystem)							
					Point.setCenterAz(oc.getAz(), oc.getAlt());

				setCenterLoc();

				//cView.invalidate();
				UploadRec u=CuV.makeUploadRec();
				cView.upload(u,false,-1);
				if(Point.getFOV()<=5.1&&PreferenceManager.getDefaultSharedPreferences(Global.getAppContext()).getBoolean(getString(R.string.dsson), true)) {
				//	Global.context = Graph.this;
					Global.dss.uploadDSS(u, cView, handler);
				}
			}
			return true;

		case R.id.radecGraph:
			showDialog(raDecDialog);
			return true;
			
		case R.id.updateGraph://updating sky to the current time, not called now
			//Global.cal=Calendar.getInstance();
			Calendar defc=Calendar.getInstance();
			AstroTools.setDefaultTime(defc, this);
			Point.setLST(AstroTools.sdTime(defc));  
			dd.setDateTime(defc);
			//SAND:new algotithm mTimeChanged=false;//returning to autoupdate based on the current time
			cView.invalidate();
			cView.sgrChanged(false);
			setTimeLabel();
			return true;
		
		case R.id.pickDateTime://changing date
			//realTimeMode(false);
			//DateTimePicker.setMode(DispMode.BOTH); //BOTH Date and Time
			//DateTimePicker.setCal(dd);
			Settings1243.putSharedPreferences(Constants.DTP_DISPLAY_MODE, DateTimePicker1243.BOTH, this);
			Settings1243.putSharedPreferences(Constants.DTP_TIME,dd.getDateTime().getTimeInMillis(),this);
			startActivityForResult(new Intent(this,DateTimePicker1243.class),GET_CODE);

			return true;
		case R.id.setDateGraph://changing date
			realTimeMode(false);
			dd.startDateDialog();
			return true;
			
		case R.id.setTimeGraph://changing time
			realTimeMode(false);
			dd.startTimeDialog();
			return true;
		
		case R.id.settingsGraph:
			startActivity(new Intent(this, SettingsGr1243.class));
			return true;
			
		case R.id.itemPlusHour:
			realTimeMode(false);
			Log.d(TAG,"before plus hour "+dd.getDateTime());
			dd.plusHour();
			//Global.cal=dd.getDateTime();
			AstroTools.setDefaultTime(dd.getDateTime(), this);
			Log.d(TAG,"after plus hour "+dd.getDateTime());
			Point.setLST(AstroTools.sdTime(dd.getDateTime()));
			//SAND:new algorithm mTimeChanged=true;
			cView.invalidate();
			cView.sgrChanged(false);
			setTimeLabel();
			//	setTimeLabel();
			return true;    		
			
		case R.id.itemMinusHour:
			realTimeMode(false);
			dd.minusHour();
			//Global.cal=dd.getDateTime();
			AstroTools.setDefaultTime(dd.getDateTime(), this);
			Point.setLST(AstroTools.sdTime(dd.getDateTime()));   
			//SAND:new algorithm mTimeChanged=true;
			cView.invalidate();
			cView.sgrChanged(false);
			setTimeLabel();
			return true;
			
		case R.id.orientPlus://rotating sky view upside down
			Point.orientationAngle+=180;
			if (Point.orientationAngle==360){
				Point.orientationAngle=0;
			}
			cView.invalidate();
			return true;
		case R.id.mirror://mirroring sky view
			Point.mirror=-Point.mirror;
			cView.invalidate();
			return true;
		case R.id.nbyGraph://    		
			//new Thread(new NearbyDSO(cView.getObjCursor().getObjSelected())).start();
			//cView.sgrChanged();
			//cView.invalidate();
			Log.d(TAG,"nby menu");
			//showDialog(NBY_DIALOG);
			if(isNearbySearchRunning()){
				registerDialog(InputDialog.message(this, R.string.search_nearby_running)).show();
				return true;
			}
			registerDialog(getNearbySearchDialog()).show();
		//	registerDialog(dn);
		
			
			return true;
		
		case R.id.infoGraph://getting info on selected object/star
			if (cView.gtObjCurs()!=null){
				Point p=cView.gtObjCurs().getObjSelected(); 
				if(p instanceof AstroObject){
					AstroObject obj=(AstroObject) p;
					if(obj.getCatalog()>NgcFactory.LAYER_OFFSET){
						int id1=obj.getId();
						AstroObject o=getDbObj(obj.getCatalog()-NgcFactory.LAYER_OFFSET,id1,getApplicationContext());
						if(o!=null){
							//if(compareUGCObjects(o, obj))
								p=o;
						}
					}
					else if(obj.getCatalog()==AstroCatalog.UCAC4_CATALOG){
						obj.con=AstroTools.getConstellation(obj.ra, obj.dec);
					}
					DetailsCommand command=new DetailsCommand((AstroObject)p,this);
					command.setCallerGraph();
					command.execute();
				}
					
			//	Details2.setCallerGraph(); //Announce the caller id
			//	startActivity(new Intent(this,Details2.class));
			}
			return true;
		
		case R.id.calibrGraph://running calibration screen
		/*	if(Global.FREE_VERSION){
				registerDialog(AstroTools.getProVersionDialog(this)).show();
				return true;
			}*/
			if (cView.gtObjCurs()!=null){
				Point p=cView.gtObjCurs().getObjSelected();
				if(p!=null)
					//SAND: startActivity(new Intent(this,Calibrate.class));
					calibrationDialog(p);
			}
			else {
				registerDialog(InputDialog.message(Graph1243.this,R.string.select_object_first)).show();
			}
			return true;

		case R.id.dssGraph:
			if(Global.FREE_VERSION){
				
				int count=Settings1243.getDSSCount();
				Log.d(TAG,"count="+count);		
				if(count>com.astro.dsoplanner.DSS.FREE_LIMIT-20){
					String message=getString(R.string.sorry_but_you_have_reached_the_free_version_limit_would_you_like_to_open_google_play_to_purchase_dso_planner_pro_version_);
					registerDialog(AstroTools.getProVersionDialog(this,message)).show();
					return true;
				}
				
				
			}
			registerDialog(makeDSSdialog()).show();
			
		/*	if(Settings.isDSSon()&&mBound){//service should have been binded to already
			//	Global.context = Graph.this;
				makeDSSdialog().show();
			}
			else
				InputDialog.message(Graph.this,"Enable DSS imagery in the Settings to allow downloading").show();
*/
			return true;	 

		/* SAND: deprecated (submenu in above)
		case R.id.dss9Graph:
			if(Settings.isDSSon()){
				Global.context = Graph.this;
				showDialog(DSS9_DIALOG_ID);
			}
			else
				InputDialog.message("Enable DSS imagery to allow downloading").show();

			return true;
		*/
		case R.id.starBold: //stars boldness control
			
			if(boldnessDialog!=null){
				if(boldnessDialog.isActive()){ //close it
					boldnessDialog.stop();
					//int y = boldnessDialog.getBottom();
					cView.layout(0, cView.getTop(), cView.getRight(), boldnessDialog.getBottom());
					cView.invalidate();
				}
				else { //start it
					boldnessDialog.start();
					//int y = boldnessDialog.getTop();
					cView.layout(0, cView.getTop(), cView.getRight(), boldnessDialog.getTop());
					cView.invalidate();
				}
			}
			return true;
			
			
		case R.id.starMags:
			changeStarMagLimit();
			
			return true;
		/*case R.id.gyro_graph:
			if(!Settings.getGyroPreference())
				return true;
			if(gyroSensorProcessor.isOn()){
				gyroSensorProcessor.setOff();
				return true;
			}
			if (cView.getObjCursor()!=null){
				Global.objectSel=cView.getObjCursor().getObjSelected();
				if(Global.objectSel!=null){
					if(Global.objectSel instanceof ObjectInfo){
						gyroSensorProcessor.setInitialPos((ObjectInfo)Global.objectSel);
						gyroSensorProcessor.setOn();
					}
				}
			}*/

		/*	case R.id.btsync_graph:
			boolean conn=BTComm.getBTComm().isConnected();

			if(conn){
				RaDecRec rec=BTComm.getBTComm().read();
				Point p= new Point(rec.ra,rec.dec);
				p.setXY();
				p.setDisplayXY();

					//Point.setCenter(oc.getRa(), oc.getDec());
					if (Point.coordSystem)							
						Point.setCenterAz(p.getAz(), p.getAlt());


					setCenterLoc();

					//cView.invalidate();
					UploadRec u=CustomView.makeUploadRec();
					cView.upload(u);
					if(Point.getFOV()<=5.1&&PreferenceManager.getDefaultSharedPreferences(Global.getAppContext()).getBoolean("DSSOn", true)) {
						Global.context = Graph.this;
						Global.dss.uploadDSS(u, cView, handler);
					}


			}
			cView.invalidate();
			return true;
		case R.id.bt_setup_graph: //moved to Settings
			Intent i=new Intent(this,SettingsBt.class);
			startActivity(i);
			return true; */
		case R.id.g_az_grid:
		//	Settings.toggle("gridOn");
		//	cView.invalidate();
			
			toggleAutoSky();
			return true;
		case R.id.g_fov://eyepieces
			InputDialog d0 = new InputDialog(Graph1243.this);
			boolean epson=Settings1243.isEpOn(getApplicationContext());
			String[] sel = {getString(R.string.eyepieces_database),epson?getString(R.string.hide2):getString(R.string.show2)};//getString(R.string.show_hide)
	        

			d0.setTitle(getString(R.string.select_action));			
			d0.setValue("-1"); //remove checks
			d0.setListItems(sel, new InputDialog.OnButtonListener() {
				public void onClick(final String value) {
					if("0".equals(value)){//database
						Intent intent=new Intent(getApplicationContext(),EyepiecesList1243.class);
						startActivity(intent);
					}
					else if("1".equals(value)){//show/hide
						Settings1243.toggle(getString(R.string.epson));
						cView.initSettingsToDrawMap();
						cView.invalidate();
					}
				}
			});
			d0.show();
			return true;
		case R.id.g_telrad:
			Settings1243.toggle(getString(R.string.telradon));
			cView.initSettingsToDrawMap();
			cView.invalidate();
			return true;
		case R.id.qfind:
			AstroTools.invokeSearchActivity(this);
			return true;
		case R.id.scopeGraph:
			if(Global.FREE_VERSION){
				registerDialog(AstroTools.getProVersionDialog(this)).show();
				return true;
			}
			if(BTComm.getBTComm().isConnected())
				BTComm.getBTComm().read();
			else {
				InputDialog d = new InputDialog(Graph1243.this,getString(R.string.dso_planner),getString(R.string.bluetooth_connection_required_would_you_like_to_setup_it_now_));
				d.setPositiveButton(getString(R.string.yes), new InputDialog.OnButtonListener() {
					public void onClick(String value) {
						startActivity(new Intent(Graph1243.this, SettingsBt1243.class));
					}
				});
				d.setNegativeButton(getString(R.string.later));
				registerDialog(d).show();
			}
			return true;
			
		}
		
		return false;
	}
	
	private void changeStarMagLimit(){
		if(starmagsDialog.isActive()){ //close it
			starmagsDialog.stop();
			//int y = boldnessDialog.getBottom();
			cView.layout(0, cView.getTop(), cView.getRight(), starmagsDialog.getBottom());
			cView.invalidate();
			return;
		}
		else { //start it
			int value=Settings1243.getSharedPreferences(this).getInt(Constants.STAR_MAG_LIMIT_CATALOG, 0);

			starmagsDialog.start(value);
			//int y = boldnessDialog.getTop();
			cView.layout(0, cView.getTop(), cView.getRight(), starmagsDialog.getTop());
			cView.invalidate();
		}
		
		
	/*	InputDialog d=new InputDialog(this);
		String names[]=new String[]{"Yale","Tycho-2","UCAC4","PGC"};
		int value=Settings1243.getSharedPreferences(this).getInt(Constants.STAR_MAG_LIMIT_CATALOG, 0);
    	d.setValue("-1");
    	d.setTitle("Select catalog to set max screen magnitude");
    	d.setListItems(names,new InputDialog.OnButtonListener() {
			public void onClick(String value) {
				int v=AstroTools.getInteger(value, -1, -1, 100);
				if(v==-1)return;
				Settings1243.putSharedPreferences(Constants.STAR_MAG_LIMIT_CATALOG, v, Graph1243.this);
				if(starmagsDialog!=null){
					if(starmagsDialog.isActive()){ //close it
						starmagsDialog.stop();
						//int y = boldnessDialog.getBottom();
						cView.layout(0, cView.getTop(), cView.getRight(), starmagsDialog.getBottom());
						cView.invalidate();
					}
					else { //start it
						starmagsDialog.start(v);
						//int y = boldnessDialog.getTop();
						cView.layout(0, cView.getTop(), cView.getRight(), starmagsDialog.getTop());
						cView.invalidate();
					}
				}
			}
    	});
    	registerDialog(d).show();*/
	}
	private void toggleAutoSky(){
		final SharedPreferences sh=PreferenceManager.getDefaultSharedPreferences(this);
		boolean autoSky=sh.getBoolean(getString(R.string.autosky), false);
		boolean level=sh.getBoolean(getString(R.string.autorotation), false);
		int value =0;
		if(autoSky&&!level)value=0;
		if(autoSky&&level)value=1;
		if(!autoSky&&level)value=2;
		if(!autoSky&&!level)value=3;
		
		Runnable r=new Runnable(){
			public void run(){
				sh.edit().putBoolean(ObjCursor.CROSS_GUIDE, true).commit();
				ObjCursor.setParameters(sh);
			}
		};
		final Dialog dm=AstroTools.getDialog(this, getString(R.string.would_you_like_to_turn_object_marker_guide_line_on_), r);
		
		InputDialog d=new InputDialog(this);
		String names[]=new String[]{getString(R.string.compass),getString(R.string.compass_level),getString(R.string.level2),getString(R.string.off)};
    	d.setValue(""+value);
    	d.setListItems(names,new InputDialog.OnButtonListener() {
			public void onClick(String value) {
				int which = AstroTools.getInteger(value, 0,-1,1000);
				switch(which){
				case 0:
					sh.edit().putBoolean(getString(R.string.autosky), true).commit();//turning compass on
					sh.edit().putBoolean(getString(R.string.autorotation), false).commit();
					if(!sh.getBoolean(ObjCursor.CROSS_GUIDE, false)){
						registerDialog(dm).show();
					}
					Point.setRotAngle(0);
					registerSensorListener();
					break;
				case 1:
					sh.edit().putBoolean(getString(R.string.autosky), true).commit();//turning compass on
					sh.edit().putBoolean(getString(R.string.autorotation), true).commit();
					registerSensorListener();
					if(!sh.getBoolean(ObjCursor.CROSS_GUIDE, false)){
						registerDialog(dm).show();
					}
					
					break;
				case 2:
					sh.edit().putBoolean(getString(R.string.autosky), false).commit();//turning compass on
					sh.edit().putBoolean(getString(R.string.autorotation), true).commit();
					registerSensorListener();
					break;
				case 3:
					sh.edit().putBoolean(getString(R.string.autosky), false).commit();//turning compass on
					sh.edit().putBoolean(getString(R.string.autorotation), false).commit();

					unregisterSensorListener();
					break;
				}
			}
    	});
		d.setTitle(getString(R.string.select_compass_level_mode));
		registerDialog(d).show();
	/*	if(!autoSky&&!level){//nothing turned on
			sh.edit().putBoolean(getString(R.string.autosky), true).commit();//turning compass on
			sh.edit().putBoolean(getString(R.string.autorotation), false).commit();
			registerSensorListener();
			InputDialog.message(this, R.string.compass_mode_is_turned_on).show();
			return;
		}
		if(autoSky){//switching to level
			sh.edit().putBoolean(getString(R.string.autosky), false).commit();
			sh.edit().putBoolean(getString(R.string.autorotation), true).commit();
			InputDialog.message(this,R.string.level_mode_is_turned_on).show();
			return;
		}
		
		if(level){//switching off
			sh.edit().putBoolean(getString(R.string.autosky), false).commit();
			sh.edit().putBoolean(getString(R.string.autorotation), false).commit();
			unregisterSensorListener();
			InputDialog.message(this,R.string.compass_and_level_mode_are_turned_off).show();
			return;
	
		}*/
		
	/*	if(autoSky){ //OFF
			sh.edit().putBoolean(getString(R.string.autosky), false).commit(); //SAND: user selectable option
			//sh.edit().putBoolean("autoRotation",false).commit();
			if(!level)unregisterSensorListener();
		}
		else{ //ON
			sh.edit().putBoolean(getString(R.string.autosky), true).commit(); //SAND: user selectable option
		//	sh.edit().putBoolean("autoRotation",true).commit();
			registerSensorListener();
		}*/
	}
	
	
	private boolean realTimeMode(boolean b) {
		realtimeMode = b;
		Settings1243.setAutoTimeUpdating(b);
		setTimeLabel();
		return b;
	}

	//SAND for pinch zoom
	//  returns false if no FOV change happen
	public boolean zoomChange(boolean wantToZoomIn) {
		int newPos = 0;
		int oldPos = mSpinner.gtSelectedItemPosition(); //spinner impl
		if(wantToZoomIn)	newPos = oldPos+1;
		else				newPos = oldPos-1;
		if(newPos<0 || newPos>=spinArr.length) 
			return false;
		mSpinner.stSelection(newPos); //triggers the onChange method changing the FOV
		return true;
	}

	class NearbyDSO implements Runnable{
		Point nearbyObject;
		int cat;
		double fov;
		double vis;
		Context context;
		
		public NearbyDSO(Point nearbyObject,int cat,double fov,double vis,Context context){
			this.nearbyObject=nearbyObject;
			this.cat=cat;
			this.fov=fov;
			this.vis=vis;
			this.context=context;
		}
		
		
		public void run(){
			Message message=new Message();
			message.arg1=4;//starting message;
			handler.sendMessage(message);
			
			threadListNearby=new ArrayList<AstroObject>();
			
			/*if(cat==-1){//ALL
				Iterator it=ListHolder.getListHolder().get(InfoList.DB_LIST).iterator();
				for(;it.hasNext();){
					DbListItem item=(DbListItem)it.next();
					threadListNearby.addAll(searchDb(item,nearbyObject,fov,vis));					
				}				
			}
			else{
				DbListItem item=AstroTools.findItemByCatId(cat);
				if(item!=null)
					threadListNearby.addAll(searchDb(item,nearbyObject,fov,vis));
			}	*/
			newsearch();
			
			message=new Message();
			message.arg1=5;//over
			handler.sendMessage(message);
			
			message=new Message();
			message.arg1=1;//stand for nearby
			handler.sendMessage(message);
		}
		
		private void newsearch(){
			List<Integer>catlist=Settings1243.getSelectedInternalCatalogs(context,Settings1243.SEARCH_NEARBY);
			
			if(catlist.contains(AstroCatalog.HERSHEL)){
				if(!catlist.contains(AstroCatalog.NGCIC_CATALOG))
					catlist.add(AstroCatalog.NGCIC_CATALOG);
			}			
			
			catlist.addAll(Settings1243.getCatalogSelectionPrefs(context,Settings1243.SEARCH_NEARBY));
			for(int cat:catlist){
				Log.d(TAG,"cat nearby="+cat);
				DbListItem item=AstroTools.findItemByCatId(cat);
				Log.d(TAG,"item="+item);
				if(item!=null){
					List<AstroObject>la=searchDb(item,nearbyObject,fov,vis);
					addObjects(la);
				}				
			}
			
			
		}
		private void addObjects(List<AstroObject>list){
			
			if(Settings1243.isRemovingDuplicates()){
			//	Log.d(TAG,"removing dups, before "+list.size());
				list.removeAll(threadListNearby);
			//	Log.d(TAG,"removing dups, after "+list.size());
				threadListNearby.addAll(list);
			}
			else{
				threadListNearby.addAll(list);
			}
		}

		private List<AstroObject> searchDb(DbListItem item,Point nearbyObject,double fov,double vis){
			List<AstroObject>list=new ArrayList<AstroObject>();

			AstroCatalog cat;
			Log.d(TAG,"item="+item);
			if(item.cat==AstroCatalog.NGCIC_CATALOG)
				cat=new NgcicDatabase(Graph1243.this);
			else if(item.cat==AstroCatalog.COMET_CATALOG)
				cat=new CometsDatabase(Graph1243.this,AstroCatalog.COMET_CATALOG);
			else if(item.cat==AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG)
				cat=new CometsDatabase(Graph1243.this,AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG);
			else if(item.ftypes.isEmpty())
				cat= new CustomDatabase(Graph1243.this,item.dbFileName,item.cat);			
			else
				cat= new CustomDatabaseLarge(Graph1243.this,item.dbFileName,item.cat,item.ftypes);
			final ErrorHandler eh=new ErrorHandler();
			cat.open(eh);
			if(!eh.hasError()){
				list=cat.searchNearby(nearbyObject, fov, vis);
				cat.close();
			}
			else{
				Graph1243.this.runOnUiThread(new Runnable(){
					public void run(){
						eh.showErrorInToast(Graph1243.this);
					}
				});
						
			}
			return list;
		}

	}

	public CuV getSkyView(){
		return cView;
	}

	
	
	
	
	private void initAlexMenu(Graph1243 v) {
		boolean dayMode = !nightMode;

		aMenu = new alexMenu(v, v, v.getLayoutInflater());
		aMenu.setHideOnSelect(true);
		aMenu.setItemsPerLineInPortraitOrientation(5);
		aMenu.setItemsPerLineInLandscapeOrientation(9);
		aMenu.setSkin(!dayMode, Settings1243.getDarkSkin());
		
		//mine
		float text_size=getResources().getDimension(R.dimen.text_size_small);//mine
		float density=getResources().getDisplayMetrics().density;
		text_size=text_size/density;
		aMenu.setTextSize((int)text_size);
		//load the menu items
		//This is kind of a tedious way to load up the menu items.
		//Am sure there is room for improvement.
		ArrayList<alexMenuItem> menuItems = new ArrayList<alexMenuItem>();

		// radecGraph        

		//menuItems.add(new alexMenuItem(R.id.pickDateTime, 
		//		"Time", dayMode?R.drawable.am_clock:R.drawable.ram_clock, true ));
        menuItems.add(new alexMenuItem(R.id.center,
        		getString(R.string.center), dayMode?R.drawable.am_center:R.drawable.ram_center, false ));
		menuItems.add(new alexMenuItem(R.id.itemMinusHour,
				getString(R.string._1_hour), dayMode?R.drawable.am_tminus:R.drawable.ram_tminus, false ));
		menuItems.add(new alexMenuItem(R.id.itemPlusHour, 
				getString(R.string._1_hour2), dayMode?R.drawable.am_tplus:R.drawable.ram_tplus, false ));
		menuItems.add(new alexMenuItem(R.id.mirror, 
				getString(R.string.mirror), dayMode?R.drawable.am_mirror:R.drawable.ram_mirror, false ));
		menuItems.add(new alexMenuItem(R.id.orientPlus, 
				getString(R.string.rotate_180_), dayMode?R.drawable.am_rotate:R.drawable.ram_rotate, false ));

		//menuItems.add(new alexMenuItem(R.id.infoGraph, 
		//		"Info", dayMode?R.drawable.am_info:R.drawable.ram_info, true ));
		menuItems.add(new alexMenuItem(R.id.g_telrad, 
				getString(R.string.telrad), dayMode?R.drawable.am_telrad:R.drawable.ram_telrad, false ));
        menuItems.add(new alexMenuItem(R.id.g_fov,
        		getString(R.string.eyepieces), dayMode?R.drawable.am_ep:R.drawable.ram_ep, false ));
		menuItems.add(new alexMenuItem(R.id.nbyGraph,
				getString(R.string.nearby), dayMode?R.drawable.am_neighbors:R.drawable.ram_neighbors , false ));
		menuItems.add(new alexMenuItem(R.id.dssGraph, 
				getString(R.string.dss), dayMode?R.drawable.am_dss:R.drawable.ram_dss, false ));
		if(AstroTools.doesBtExist()){
			menuItems.add(new alexMenuItem(R.id.scopeGraph, 
					getString(R.string.scope_go), dayMode?R.drawable.am_scope:R.drawable.ram_scope, true ));
		}
		//menuItems.add(new alexMenuItem(R.id.dss9Graph, 
		//		"DSS*9", dayMode?R.drawable.am_dss9:R.drawable.ram_dss9, false ));

		//menuItems.add(new alexMenuItem(R.id.updateGraph, 
		//		"UPDATE", dayMode?R.drawable.am_update:R.drawable.ram_update, false ));
        menuItems.add(new alexMenuItem(R.id.g_az_grid,
        		getString(R.string.compass), dayMode?R.drawable.am_gyro:R.drawable.ram_gyro, false ));
		menuItems.add(new alexMenuItem(R.id.calibrGraph,
				getString(R.string.align_star), dayMode?R.drawable.am_calibrate:R.drawable.ram_calibrate, true ));
		menuItems.add(new alexMenuItem(R.id.radecGraph, 
				getString(R.string.ra_dec), dayMode?R.drawable.am_radec:R.drawable.ram_radec, true ));
		menuItems.add(new alexMenuItem(R.id.starBold, 
				getString(R.string.boldness), dayMode?R.drawable.am_boldness:R.drawable.ram_boldness, true ));
		menuItems.add(new alexMenuItem(R.id.starMags, 
				getString(R.string.layers), dayMode?R.drawable.am_range:R.drawable.ram_range, true ));
		menuItems.add(new alexMenuItem(R.id.qfind,
				getString(R.string.search), dayMode?R.drawable.am_search:R.drawable.ram_search, true ));
		menuItems.add(new alexMenuItem(R.id.settingsGraph, 
				
				getString(R.string.settings), dayMode?R.drawable.am_settings:R.drawable.ram_settings, true ));
		
		/* Moveto SettingsGraph menuItems.add(new alexMenuItem(R.id.bt_setup_graph, 
				"BT setup", dayMode?R.drawable.am_settings:R.drawable.ram_settings, true ));
		*/


		if (aMenu.isNotShowing()){
			try {
				aMenu.setMenuItems(menuItems);
			} catch (Exception e) {
				InputDialog.message(Graph1243.this,getString(R.string.menu_error_) + e.getMessage(), 0).show();
			}	
		}
	}

	public void doMenu(View v)
	{
		//if (aMenu.isShowing()) {
		//	aMenu.hide();
		//} else {
		aMenu.show(v);//Note it doesn't matter what widget you send the menu as long as it gets view.
		//}

	}
	protected void onDestroy(){
		try{
			aMenu.hide();
		}
		catch(Exception e){}
		super.onDestroy();
		Log.d(TAG,"onDestroy");
		//StarMags.clearContext();
		//StarBoldness.clearContext();
		cView.setGraphDestroyedFlag();
		cView.clearLists();
		cView=null;
		Settings1243.removeSharedPreferencesKey(Constants.GRAPH_OBJECT, getApplicationContext());
	}
	public void MenuItemSelectedEvent(alexMenuItem selection) {
		parseMenuEvent(selection.getId());
	}

	//override the system search request in night mode only
	@Override
	public boolean onSearchRequested() {
		return AstroTools.invokeSearchActivity(this);
	}

	//overriding BACK button
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		/*if (keyCode == KeyEvent.KEYCODE_BACK){
			if(boldnessDialog!=null && boldnessDialog.isActive()){
				parseMenuEvent(R.id.starBold); //triggers its closing
				return true;
			}
			if(starmagsDialog!=null&&starmagsDialog.isActive()){
				int cat=starmagsDialog.getCatalog();
				Settings1243.putSharedPreferences(Constants.STAR_MAG_LIMIT_CATALOG, cat, Graph1243.this);

				parseMenuEvent(R.id.starMags);
				return true;
			}
			
			finish();
		} 
		
			return super.onKeyDown(keyCode, event); */
		return onKeyDownImpl(keyCode, event);
	} 
	/**
	 * 
	 * @param keyCode
	 * @param event null means emulation from the button. Parent is not called
	 * Actually null not used
	 * @return
	 */
	private boolean onKeyDownImpl(int keyCode, KeyEvent event){
		Log.d(TAG,"calling activity="+callingActivity+" def time="+def_time);
		if (keyCode == KeyEvent.KEYCODE_BACK){
			if(boldnessDialog!=null && boldnessDialog.isActive()){
				parseMenuEvent(R.id.starBold); //triggers its closing
				return true;
			}
			if(starmagsDialog!=null&&starmagsDialog.isActive()){
				int cat=starmagsDialog.getCatalog();
				Settings1243.putSharedPreferences(Constants.STAR_MAG_LIMIT_CATALOG, cat, Graph1243.this);

				parseMenuEvent(R.id.starMags);
				return true;
			}
			if(callingActivity==Constants.NOTE_LIST_GRAPH_CALLING&&def_time!=null){
				AstroTools.setDefaultTime(def_time, getApplicationContext());
				Log.d(TAG,"def time="+def_time);
			}
			finish();
		}
		if(event==null)
			return true;
		else
			return super.onKeyDown(keyCode, event);
	}
	
	private boolean nearbyDialog=false;
	//nearby objects dialog
	private Dialog getNearbySearchDialog(){
		final InputDialog d = new InputDialog(this);
		
		d.setTitle(getString(R.string.nearby_search));
		//d.setHelp(R.string.search_desc);
		//d.setMessage("Enter a part of the object's name to search for, like NGC number, or Messier designation.");
		d.insertLayout(R.layout.graph_nearby_dialog);
		
		final Button db_btn=(Button)d.findViewById(R.id.gnd_db_btn);
		final Button types_btn=(Button)d.findViewById(R.id.gnd_types_btn);
		final EditText et1=(EditText)d.findViewById(R.id.gnd_et1);//fov
		final EditText et2=(EditText)d.findViewById(R.id.gnd_et2);//dl
		aMenu.hide();
		
		TextView tvt=(TextView)d.findViewById(R.id.gnd_types_tv);
		tvt.setText(Settings1243.getObjTypesSummary(this, Settings1243.SEARCH_NEARBY));
		
		TextView tvdb=(TextView)d.findViewById(R.id.gnd_types_db);
		tvdb.setText(Settings1243.getSelectedCatalogsSummary(this, Settings1243.SEARCH_NEARBY));
		
		
		types_btn.setOnClickListener(new View.OnClickListener()
				 {
			
			@Override
			public void onClick(View v) {
				Intent i=new Intent(Graph1243.this,SettingsIncl1243.class);
				i.putExtra(Constants.XML_NUM, R.xml.settings_basic_search_obj_types_search_nearby_incl);
				startActivity(i);
				Settings1243.putSharedPreferences(Constants.GRAPH_NEARBY_STRING_FOV, et1.getText().toString(), Graph1243.this);
				Settings1243.putSharedPreferences(Constants.GRAPH_NEARBY_STRING_DL, et2.getText().toString(),Graph1243.this);
				nearbyDialog=true;
				d.dismiss();
				
			}
		});
		
		
		//current search prefs
	//	SharedPreferences prefs=Settings1243.getSharedPreferences(this);
	//	int db_num=prefs.getInt(Constants.GRAPH_NEARBY_SEARCH_DB, -1);
		
		String fov=Settings1243.getStringFromSharedPreferences(this,Constants.GRAPH_NEARBY_STRING_FOV,_120);
		String dl=Settings1243.getStringFromSharedPreferences(this,Constants.GRAPH_NEARBY_STRING_DL,_1);
		
		et1.setText(fov);//prefs.getString(Constants.GRAPH_NEARBY_STRING_FOV,_120));
		et2.setText(dl);//prefs.getString(Constants.GRAPH_NEARBY_STRING_DL,_1));
		
		
		
		//make an array for list
	/*	InfoList iL=ListHolder.getListHolder().get(InfoList.DB_LIST);
		final String[] db_names=new String[iL.getCount()+1];
		db_names[0]=getString(R.string.all);
		int i=1;
		int pos=0;//if db_num==-1 or does not exist pos is left 0
		for(Object o:iL){
			DbListItem item=(DbListItem)o;
			db_names[i]=item.dbName;
			if(item.cat==db_num)
				pos=i;
			i++;
		}
		
		db_btn.setTag(pos);//passing beginning pos
		db_btn.setText(db_names[pos]);*/
		
		db_btn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				/*InputDialog d=new InputDialog(Graph1243.this);
	        	d.setValue(String.valueOf((Integer)db_btn.getTag()));
	        	d.insertLayout(R.layout.input_dialog_ch_item);
	        	//d.setType(InputDialog.DType.INPUT_CHECKBOXES);
	        	d.setTitle(getString(R.string.select_database_to_search_in));
	        	d.setListItems(db_names,new InputDialog.OnButtonListener() {
					public void onClick(String value) {
						int which = AstroTools.getInteger(value, 0,-1,1000);
						db_btn.setText(db_names[which]);
						db_btn.setTag(which);
					}
	        	});
				Dialog d=AstroTools.getCheckBoxDialog(new String[]{"test1","test2"}, null, Graph1243.this);
	        	registerDialog(d).show();*/
				Intent i=new Intent(Graph1243.this,SettingsIncl1243.class);
				i.putExtra(Constants.XML_NUM, R.xml.settings_select_catalogs_nearby_incl);
				startActivity(i);
				Settings1243.putSharedPreferences(Constants.GRAPH_NEARBY_STRING_FOV, et1.getText().toString(), Graph1243.this);
				Settings1243.putSharedPreferences(Constants.GRAPH_NEARBY_STRING_DL, et2.getText().toString(),Graph1243.this);
				nearbyDialog=true;
				d.dismiss();
			}
		});
		
		d.setPositiveButton(getString(R.string.search), new InputDialog.OnButtonListener() {
			public void onClick(String value) {
				//form a compatible call
			    
				
				
				//SharedPreferences prefs=Settings1243.getSharedPreferences(Graph1243.this);
				Settings1243.putSharedPreferences(Constants.GRAPH_NEARBY_STRING_FOV, et1.getText().toString(), Graph1243.this);
				//prefs.edit().putString(Constants.GRAPH_NEARBY_STRING_FOV, et1.getText().toString()).commit();
				Settings1243.putSharedPreferences(Constants.GRAPH_NEARBY_STRING_DL, et2.getText().toString(),Graph1243.this);
				//prefs.edit().putString(Constants.GRAPH_NEARBY_STRING_DL, et2.getText().toString()).commit();
				/*int pos=(Integer)db_btn.getTag();
				DbListItem item=null;
				if(pos==0){//ALL 
					Settings1243.putSharedPreferences(Constants.GRAPH_NEARBY_SEARCH_DB,-1,Graph1243.this);
					//prefs.edit().putInt(Constants.GRAPH_NEARBY_SEARCH_DB,-1).commit();
				}
				
				else{
					InfoList iL=ListHolder.getListHolder().get(InfoList.DB_LIST);
					item=(DbListItem)iL.get(pos-1);
					if(ImportDatabaseIntentService.isBeingImported(item.dbFileName)){
						registerDialog(InputDialog.message(Graph1243.this,Global.DB_IMPORT_RUNNING,0)).show();
						return;
					}
					Settings1243.putSharedPreferences(Constants.GRAPH_NEARBY_SEARCH_DB, item.cat,Graph1243.this);
					//prefs.edit().putInt(Constants.GRAPH_NEARBY_SEARCH_DB, item.cat).commit();
				}
				startNearbySearch(item==null?-1:item.cat,et1.getText().toString(),et2.getText().toString());
				*/
				startNearbySearch(0,et1.getText().toString(),et2.getText().toString());

			}
		});
		return d;
		
		//dlg.disableBackButton(true);
		
	}
	
	private Thread nearbyThread;
	
	private boolean isNearbySearchRunning(){
		if(nearbyThread!=null)
			if(nearbyThread.isAlive())
				return true;
		return false;
	}
	
	private void startNearbySearch(int cat,String fovStr,String visStr){
		double fov=AstroTools.getDouble(fovStr, 0,0,10000);
		if(fov<=0)return;
		if(fov>600)
			fov=600;
		double vis=AstroTools.getDouble(visStr, 0,-1,5);
		
		
		/*if(nearbyThread!=null)
			if(nearbyThread.isAlive()){
				registerDialog(InputDialog.message(this, R.string.search_nearby_running)).show();
				return;
			}*/
		if(isNearbySearchRunning()){
			registerDialog(InputDialog.message(this, R.string.search_nearby_running)).show();
			return;
		}
		Point selobj=cView.gtObjCurs().getObjSelected();
		if(selobj==null)
			return;
		nearbyThread=new Thread(new NearbyDSO(selobj,cat,fov,vis,getApplicationContext()));
		nearbyThread.start();
		
	}
	//Replacing the calibration activity
	private void calibrationDialog(Point objSelected) {
		final Point p;

		final InputDialog dlg = new InputDialog(Graph1243.this);
		dlg.setTitle(getString(R.string.setting_circles_one_star_alignment));
		dlg.setHelp(R.string.helpCalibrateDlg);
		dlg.insertLayout(R.layout.calibrate_dialog);
		//dlg.disableBackButton(true);
		dlg.setNegativeButton(getString(R.string.cancel));

		//fields
		final TextView timetext	=(TextView)dlg.findViewById(R.id.currenttime_text);
		final TextView azt		=(TextView)dlg.findViewById(R.id.absaz_txt);
		final TextView altt		=(TextView)dlg.findViewById(R.id.absalt_txt);
		final EditText azte 	=(EditText)dlg.findViewById(R.id.aztext);
		final EditText altte 	=(EditText)dlg.findViewById(R.id.alttext);
		final Button orientBtn=(Button)dlg.findViewById(R.id.orientation_btn);
		
	//	SharedPreferences prefs=Graph1243.this.getSharedPreferences(Constants.PREFS,Context.MODE_PRIVATE);
		SharedPreferences prefs=Settings1243.getSharedPreferences(Graph1243.this);
		boolean orient=prefs.getBoolean(CALIBR_ORIENT, true);//false for Clockwise, true - for CounterClockwise
		
		int object_type=-1;
	//	DSO d=null;		
	//	HrStar st=null;
		Planet pl=null;
		//Point obj = Global.objectSel;
		String name=null;
		if(objSelected instanceof AstroObject)
			name =((AstroObject) objSelected).getLongName();
		
		//if(!(objSelected instanceof Planet))
			p = AstroTools.precession(objSelected, AstroTools.getDefaultTime(Graph1243.this));
		//else
		//	p = objSelected;

		TextView dsc=(TextView)dlg.findViewById(R.id.des_txt);
		dsc.setText(name); 
		Global.calibrationTime=null;
		
		orientBtn.setText(orient?R.string.counterclockwise:R.string.clockwise);
		orientBtn.setTag(orient);

		//Orientation button
		orientBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean orient=(Boolean)orientBtn.getTag();
				orient=!orient;
				orientBtn.setTag(orient);
				orientBtn.setText(orient?R.string.counterclockwise:R.string.clockwise);
			}
		});
		
		//NOW button
		//setting current time (when measurement is taken
		View nowButton = dlg.findViewById(R.id.currenttime_btn);
		nowButton.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) {
				Calendar c = Calendar.getInstance();
				String s = Details1243.makeDateString(c, true)+" "+Details1243.makeTimeString(c,true);
				timetext.setText(s);

				//updateAzAlt(c); inplace now
				double lst = AstroTools.sdTime(c);		
				double lat = Settings1243.getLattitude();
				
				Double absaz = AstroTools.Azimuth(lst, lat, p.ra, p.dec);
				String s1 = String.format(Locale.US,"%.1f", absaz);
				azt.setText(s1+'\u00B0');
				azt.setTag(absaz); //passing the var
				
				Double absalt = AstroTools.Altitude(lst, lat, p.ra, p.dec);
				String s2 = String.format(Locale.US,"%.1f", absalt);
				altt.setText(s2+'\u00B0');
				altt.setTag(absalt); //passing the var

				Global.calibrationTime = c; //used for calculating the exact star position when calibration is performed

			}
		});

		dlg.setPositiveButton(getString(R.string.one_star_align), new InputDialog.OnButtonListener() {
			public void onClick(String value) {
				double er = 999.9;
				double az = AstroTools.getDouble(azte.getText().toString(), er,-10000,10000);
				double alt= AstroTools.getDouble(altte.getText().toString(), er,-10000,10000);
				
				if(az!=er && alt!=er && Global.calibrationTime!=null){ //calculating adjustments.  These are WRONG calculations!!! They are wrong to take into account the wrong... 
					
					//azimuthal circle was glued to dobson base counterclockwire when it should have been clockwise or vice versa
					
					boolean orient=(Boolean)orientBtn.getTag();
					double adj_az=0;
					double adj_alt=0;
					
					if(orient){
						adj_az=(Double)azt.getTag()  - az;
						Settings1243.putSharedPreferences(Constants.ALIGN_ADJ_AZ,adj_az, Graph1243.this);
						//Global.adj_Az  = ; //for correct azimuathal circle equation should be like the one for Alt
						adj_alt=(Double)altt.getTag() - alt;
						Settings1243.putSharedPreferences(Constants.ALIGN_ADJ_ALT,adj_alt,Graph1243.this);
						//Global.adj_Alt = ;
					}
					else{
						adj_az=(Double)azt.getTag()  + az;
						adj_alt=(Double)altt.getTag() - alt;		
						
						Settings1243.putSharedPreferences(Constants.ALIGN_ADJ_AZ,adj_az , Graph1243.this);
						Settings1243.putSharedPreferences(Constants.ALIGN_ADJ_ALT,adj_alt,Graph1243.this);
						//Global.adj_Az  = (Double)azt.getTag()  + az; 
						//Global.adj_Alt = (Double)altt.getTag() - alt;
					}
					Settings1243.putSharedPreferences(Constants.ALIGN_TIME, Calendar.getInstance().getTimeInMillis(), Graph1243.this);
				//	SharedPreferences prefs=Graph1243.this.getSharedPreferences(Constants.PREFS,Context.MODE_PRIVATE);
					Settings1243.putSharedPreferences(CALIBR_ORIENT, orient, Graph1243.this);
					//prefs.edit().putBoolean(CALIBR_ORIENT, orient).commit();
					
					String s2=String.format(Locale.US,"%.1f", adj_az);
					String s3=String.format(Locale.US,"%.1f", adj_alt);
					
					InputDialog d = new InputDialog(Graph1243.this);
					d.setType(InputDialog.DType.MESSAGE_ONLY);
					d.setTitle(getString(R.string.setting_circles_calibration_successfull_));
					d.setMessage(getString(R.string.adjustments_daz_)+s2+getString(R.string._dalt_)+s3);
					registerDialog(d).show();
				}
			}
		});
		registerDialog(dlg).show();
	}

	public String[] getSpinArray() {
		return spinArr;
	}
	
	void restart(){
		Intent intent = getIntent();
	    overridePendingTransition(0, 0);
	    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
	    finish();
	    overridePendingTransition(0, 0);
	    startActivity(intent);
	}
	
	
	@Override
	protected int getTestActivityNumber(){
		return TestIntentService.GRAPH;
	}
	
	
	@Override
	protected void startTest(int action,int param){
		if(!Global.TEST_MODE)
			return;
		super.startTest(action,param);
		switch(action){
		case TestIntentService.GRAPH_START:
			performZoomItemSelect("2");
			break;
		case TestIntentService.GRAPH_LOG:
			TestIntentService.print("test="+TestIntentService.getTestNumber()+" result="+(cView.tychoList.size()>1000)+" tycho count="+cView.tychoList.size());
			TestIntentService.print("test="+TestIntentService.getTestNumber()+" result="+(cView.ucac4List.size()>500)+" ucac4 count="+cView.ucac4List.size());
			TestIntentService.print("test="+TestIntentService.getTestNumber()+" result="+(cView.pgcList.size()>1200)+" pgc count="+cView.pgcList.size());
			TestIntentService.print("test="+TestIntentService.getTestNumber()+" result="+(cView.ngcList.size()>25)+" ngc count="+cView.ngcList.size());

			/*Log.d(Global.TEST_TAG,"tycho count="+cView.tychoList.size());
			Log.d(Global.TEST_TAG,"ucac4 count="+cView.ucac4List.size());
			Log.d(Global.TEST_TAG,"pgc count="+cView.pgcList.size());
			Log.d(Global.TEST_TAG,"ngc count="+cView.ngcList.size());*/
			break;
		}
	}
	
	
	
	//Gesture Detector (just implement OnGestureListener in the Activity)
	GestureDetector gDetector = new GestureDetector(this);
	@Override 
	public boolean dispatchTouchEvent(MotionEvent me){ 
		gDetector.onTouchEvent(me);
		return super.dispatchTouchEvent(me); 
	}
	public boolean onFling(MotionEvent start, MotionEvent finish, float xVelocity, float yVelocity) {
		if(starmagsDialog.isActive()||boldnessDialog.isActive())return true;
		int bh = cView.getBottom();
		if(bh==Global.screenH) bh-=50; //for full screen mode
		if(start.getRawY() > bh){ //in the bottom bar
			if(start.getRawY() - finish.getRawY() > Global.flickLength)
				doMenu(bottomBar);
			else if(start.getRawX() - finish.getRawX() > Global.flickLength){
				super.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
				super.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
				Log.d(TAG, "dispatch back");
			}
		}				
		return true;
	}
	public void onLongPress(MotionEvent e) {
		if(starmagsDialog.isActive()||boldnessDialog.isActive())return ;
		try{
			Graph1243.DssImage image=cView.dssRectangles.pressed(e.getX(), e.getY());
			if(image!=null&&!image.isSelected()){//this automatically selectes and draw new potential rectangle objects
				cView.dssRectangles.deselectAll();//deselects all currently selected countours
				image.select();
				cView.invalidate();
				return;
			}

			//calling menu on second tap
			if(image!=null&&image.isSelected())	{//pressed on dss image
				image.select();//to make it vibrate

				registerDialog(makeDSSselectionDialog(image)).show();
				return;
			}

			View bar = findViewById(R.id.topBar);
			if(bar.getVisibility()==View.GONE)
				bar.setVisibility(View.VISIBLE);
			else
				bar.setVisibility(View.GONE);
		}
		catch(Exception ex){}
	}
	public void onShowPress(MotionEvent e) {}
	public boolean onDown(MotionEvent arg0) { return false; }
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {return false;}
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

}
