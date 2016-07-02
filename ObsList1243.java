package com.astro.dsoplanner;

import static com.astro.dsoplanner.Constants.constellations;
import static com.astro.dsoplanner.Global.ALEX_MENU_FLAG;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.astro.dsoplanner.InputDialog.OnButtonListener;
import com.astro.dsoplanner.ObsInfoListImpl.Item;
import com.astro.dsoplanner.alexmenu.alexMenu;
import com.astro.dsoplanner.alexmenu.alexMenu.OnMenuItemSelectedListener;
import com.astro.dsoplanner.alexmenu.alexMenuItem;
import com.astro.dsoplanner.d.DownloadService;
import com.astro.dsoplanner.d.DownloadService.LocalBinder;


//observation list
public class ObsList1243 extends ParentListActivity implements OnMenuItemSelectedListener, OnGestureListener {


	
	
	private static final int SET = 4;
	private static final int DIMENSION = 3;
	private static final int MAG = 2;
	private static final int CONSTELLATION = 1;
	private static final int NAME=0;
	private static final int RA=5;
	private static final int DEC=6;
	private static final int TYPE=7;
	private static final int ALT2=8;
	@ObfuscateOPEN
	//private static final String WITH_LAST_NOTE_AGE_12_MONTHS = R.string._with_last_note_age_12_months;
	//private static final String OBSERVATION_LIST_1_4 = "Observation List [1-4][\\:][ ]";
	private static final String STRING = "--";
	//private static final String DSS = R.string.dss;
	//private static final String RENAME = R.string.rename;
	//private static final String SEARCH = R.string.search;
	//private static final String MOVE = R.string.move;
//	private static final String DISPLAYED = R.string._displayed_;
	//private static final String MARK_OBJECTS = R.string.mark_objects_;
	//private static final String SHOW_HIDE_OBJECTS_WITH_MARKS = R.string.show_hide_objects_with_marks; //6;
	//private static final String REMOVE_ALL_MARKS = R.string.remove_all_marks;
	//private static final String WITH_ANY_NOTES_RECORDED = R.string._with_any_notes_recorded;
	//private static final String WITH_LAST_NOTE_AGE_6_MONTHS = R.string._with_last_note_age_6_months;
	//private static final String WITH_LAST_NOTE_AGE_1_MONTH = R.string._with_last_note_age_1_month;
	//private static final String WITH_LAST_NOTE_AGE_1_WEEK = R.string._with_last_note_age_1_week;
	//private static final String WITH_LAST_NOTE_AGE_1_DAY = R.string._with_last_note_age_1_day;
	private static final String MENU_ERROR2 = "Menu error! ";
	//private static final String EDIT2 = R.string.edit;
	//private static final String REMOVE_ALL = R.string.remove_all;
	//private static final String REMOVE_CHECKED = R.string.remove_marked;
	//private static final String REMOVE_FROM_LIST = R.string.remove_from_list;
	//private static final String SEE_ALL_NOTES = R.string.see_all_notes;
	//private static final String SEE_OBJECT_NOTES = R.string.see_object_notes;
	//private static final String ADD_NOTE = R.string.add_note;
	//private static final String DETAILS = R.string.details;
	//private static final String SHOW_IMAGE = R.string.show_image;
	private static final String MENU_ERROR = MENU_ERROR2;
	//private static final String FIND = R.string.find;
	//private static final String NEXT = R.string.next;
	//private static final String MARK = R.string.mark;
	//private static final String SHARE = R.string.share;
	//private static final String EXPORT = R.string.export;
	//private static final String IMPORT = R.string.import2;
	//private static final String PASTE = R.string.paste;
	//private static final String ADD_OBJECT = R.string.add_object;
	//private static final String LIST_4 = R.string.list_4;
	//private static final String LIST_3 = R.string.list_3;
	//private static final String LIST_2 = R.string.list_2;
	//private static final String LIST_1 = R.string.list_1;	
	private static final String SIZE = " Size ";	
	private static final String CONSTELL = "Constell";
	private static final String CATALOG_POSITION = "catalogPosition";
	private static final String OBJ_POS = "objPos";
	private static final String EDIT = "edit";
	//private static final String DO_YOU_WANT_TO_REMOVE_CHECKED_NOTES = R.string.do_you_want_to_remove_marked_objects_;
	//private static final String OBJ_HIDDEN = R.string._obj_hidden;
	//private static final String OBJ = R.string._obj;
	private static final String SHOW_SELECTED = "showSelected";	
//	private static final String MM_EYEPIECE = R.string._mm_eyepiece;
//	private static final String BEST_VIS = R.string.best_vis_;
	private static final String ALT = " Alt=";
	private static final String AZ = "Az=";
	private static final String DIM = " dim=";
	private static final String MAG2 = " mag=";
	//private static final String CATALOG="catalog";
	@ObfuscateCLOSE

	private static final String TAG="ObsList";@MarkTAG
	//Comment comment;
	private TextView tv;
	private Map<Integer,Integer> cmpMap=new HashMap();
	private List<ObsInfoListImpl.Item> displayList=new ArrayList<ObsInfoListImpl.Item>();
	Handler handler=new Handler();
	boolean initOver=false;//used for onActivity result as it is started right after return from NCOA and initialisation is not ready yet
	private HandlerThread workerThread; //import
	private Handler workerHandler;//import
	private HandlerThread workerThread2; //pics
	private Handler workerHandler2; //pics
	long cmo_time=0;
	private TextView textViewObjNum;
	
	private boolean showpics=false;//used for tracking if there are pics in the list
	
	//private boolean picson=true;
	
	private boolean areImagesOn(){
		return Settings1243.getSharedPreferences(getApplicationContext()).getBoolean(Constants.SHOW_OBS_IMAGES, true);
	}
	private void setShowImagesFlag(boolean flag){
		Settings1243.putSharedPreferences(Constants.SHOW_OBS_IMAGES, flag, getApplicationContext());
	}
	
	private class Move{

		int whatpos;
		int wherepos;
		boolean startMove=false;

		public void setWhat(int pos){
			whatpos=pos;
		}
		public void setWhere(int pos){
			wherepos=pos;
		}

		public void startMove(){
			startMove=true;
		}
		public boolean isMoving(){
			return startMove;
		}
		public void move(){
			int obsList=Settings1243.getSharedPreferences(ObsList1243.this).getInt(Constants.ACTIVE_OBS_LIST, InfoList.PrimaryObsList);
			InfoList list=ListHolder.getListHolder().get(obsList);
			ObsInfoListImpl listi=(ObsInfoListImpl)list;
			listi.move(displayList.get(whatpos), displayList.get(wherepos));
			startMove=false;
		}
	}

	Move mMove=new Move();
	
	private CuV skyview;


	/**
	 * used for saving alt/az/vis info to speed up list drawing
	 * @author leonid
	 *
	 */
	private class data{
		//double az;
		//double alt;
		String azS;
		String altS;		
		AstroObject obj;
		double highvis=0;//highest visibility
		int highep=0;//eyepiece where it is reached
		public data(AstroObject obj){
			this.obj=obj;
		}
		/**
		 * calculate az, alt and visibility
		 */
		public void fill(){
			Point p=obj.getCurrentRaDec(AstroTools.getDefaultTime(ObsList1243.this));
			double az=AstroTools.Azimuth(lst, lat, p.ra, p.dec);
			double alt=AstroTools.Altitude(lst, lat, p.ra, p.dec);
			azS=Details1243.doubleToGrad(az,'\u00B0',(char)39);
			altS=Details1243.doubleToGrad(alt,'\u00B0',(char)39);			

			if(!obj.hasVisibility())return;
			highvis=0;
			highep=0;
			for(int e=0; e < Settings1243.getEPsNumber(); e++){
				double epfocus=Settings1243.getEpFocus(e);
				if(Settings1243.isCCD(epfocus)) //CCD
					continue;
				double magnification=Settings1243.getTFocus()/epfocus;				
				double vis=AstroTools.LogC(obj.getA(),obj.getB(),obj.getMag(),magnification,obj.getType());	
				if(vis>highvis){
					highvis=vis;
					highep=e;
				}
			}

		}		

	}
	Map<AstroObject,data>dataMap=new HashMap<AstroObject,data>();
	double lst;

	private double updateLst(){
		return AstroTools.sdTime(AstroTools.getDefaultTime(this));
	}

	private void updateDataMap(){
		updateLattitude();
		updateLst();
		for(Map.Entry<AstroObject, data> me:dataMap.entrySet()){
			me.getValue().fill();
		}
	}
	//private int infoListNumber=InfoList.PrimaryObsList; 
	private OLadapter mAdapter;

	private alexMenu aMenu;
	private alexMenu contextMenu;

	private boolean dirty=false;//tracking changes to observation list for pref saving
	private FindRunner findRunner=new FindRunner();
	private int item_selected=-1;//used in find/next

	//used for calculation of alt and az in obs list
	double lat;//=updateLattitude();

	private double updateLattitude(){
		return Settings1243.getLattitude();
	}
	/**
	 * prepares and sets astro images in asynchronous way 
	 * @param view - view to take image view from
	 * @param obj - object for which make picture
	 * @param handler - handler from ui thread
	 * @param id - image view resource id
	 * @param hide_invisible - hide null images
	 */
	public static void setImage(final Context context,final View view,final AstroObject obj,final Handler handler, int id,Handler wh,final boolean hide_invisible){
		//final List<String>list=Details1243.getPicturePaths(obj);
		//Log.d(TAG,"setimage start for "+obj);
		//	Log.d(TAG,"obj="+obj+"pics="+list);
		final ImageView iv = (ImageView) view.findViewById(id);
		iv.setTag(obj);//needed to check later if this view belongs to somebody else
		/*if(list.size()==0){
			iv.setImageBitmap(null);
			return;
		}*/
		Runnable r=new Runnable(){
			public void run(){
				final List<String>list=Details1243.getPicturePaths(context,obj);
				if(list.size()==0){
					handler.post(new Runnable(){
						public void run(){
							iv.setImageBitmap(null);
							if(hide_invisible)
								iv.setVisibility(View.GONE);
						}
					});
					
					return;
				}
				//BitmapFactory.Options options = new BitmapFactory.Options();
				//options.inSampleSize = 2;
				final Bitmap image=BitmapFactory.decodeFile(list.get(0));
				if (image!=null){
					//SAND in layout now: iv.setScaleType(ImageView.ScaleType.FIT_START);


					handler.post(new Runnable(){
						public void run(){
							//	ImageView iv = (ImageView) view.findViewById(R.id.obs_image);
							if(iv==null)return;//the view may have been killed
							Object tag=iv.getTag();
							if (tag==null)
								return;
							if(!obj.equals(tag))//this is to check if this view now belongs to another object)))
								return;
							iv.setImageBitmap(image);			
							if(Settings1243.getNightMode()){
								ColorFilter filter = new LightingColorFilter(0xffff0000, 1); 
								iv.setColorFilter(filter);
							}
							else
								iv.setColorFilter(null);
							iv.setVisibility(View.VISIBLE);
							//TextView tv=(TextView)view.findViewById(R.id.obslistitem_dso);
							//Log.d(TAG,"setting image to "+tv.getText()+" for "+obj);
						}
					});

				}
				return;
			}
		};
		//r.run();
		//new Thread(r).start();
		if(wh!=null)
			wh.post(r);
	}
	
	private String getObsListName(AstroObject obj){
		if(obj.getCatalog()==AstroCatalog.MESSIER||obj.getCatalog()==AstroCatalog.CALDWELL){
			return obj.getLongName()+" "+obj.getShortName();
		}
		else
			return obj.getLongName();
	}
	
	
	
	
	private String best_vis;//=getString(R.string.best_vis);
	private String _mm_eyepiece;//=getString(R.string._mm_eyepiece);
	private String _at_;//=getString(R.string._at_);
	private class OLadapter extends BaseAdapter{

		
		private LayoutInflater mInflater;
		private static final int NON_BOLD=1;
		private static final int BOLD=2;

		public OLadapter() {
			mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		}

		public View getView(int position, View convertView,ViewGroup parent){
			boolean bold=(position==item_selected);
			boolean remake=false;//remake convert view if its structure is different from the one indicated by shows pics

			if(convertView!=null){
				ImageView iv = (ImageView) convertView.findViewById(R.id.obs_image);
				if(showpics){
					remake=(iv==null);
					if(iv!=null){
						iv.setImageBitmap(null);
					}
				}
				else{
					remake=(iv!=null);
				}
			}
			if (convertView == null||remake) {

				if(bold){
					convertView = mInflater.inflate(showpics?R.layout.obslist_item_bold:R.layout.obslist_item_no_pic_bold, null);
					convertView.setTag(BOLD);
				}
				else{
					convertView = mInflater.inflate(showpics?R.layout.obslist_item:R.layout.obslist_item_no_pic, null);
					convertView.setTag(NON_BOLD);
				}				

			} 


			Object tag=convertView.getTag();
			int itag=0;

			if(tag!=null){
				try{
					itag=(Integer)tag;
				}
				catch(Exception e){

				}
			}
			boolean redo=false;
			if(bold&&itag!=BOLD)redo=true;
			if(!bold&&itag!=NON_BOLD)redo=true;
			if(redo){
				Log.d(TAG,"redone");
				if(bold){
					convertView = mInflater.inflate(showpics?R.layout.obslist_item_bold:R.layout.obslist_item_no_pic_bold, null);
					convertView.setTag(BOLD);
				}
				else{
					convertView = mInflater.inflate(showpics?R.layout.obslist_item:R.layout.obslist_item_no_pic, null);
					convertView.setTag(NON_BOLD);
				}
			}

			ObsInfoListImpl.Item h=displayList.get(position);//(ObsInfoListImpl.Item)ListHolder.getListHolder().get(Global.activeObsList).get(position);
			AstroObject d=h.x;


			//	Log.d(TAG,"inside adapter "+"pos="+position+"noteRec="+n);
			String s=getObsListName(d)+" "+d.getTypeString();
			/*if(d.getCatalog()==AstroCatalog.MESSIER||d.getCatalog()==AstroCatalog.CALDWELL){
				s=d.getLongName()+" "+d.getShortName()+" "+d.getTypeString();
			}
			else
				s=d.getLongName()+" "+d.getTypeString();	*/
			
			
			TextView tv=((TextView) convertView.findViewById(R.id.obslistitem_dso));			
			tv.setText(s);
			if(showpics) setImage(getApplicationContext(),convertView,d,handler,R.id.obs_image,workerHandler2,false);
			/*	//analysing if bold
			Typeface tf=tv.getTypeface();
			Log.d(TAG,"tf="+tf);
			if(tf==null)//normal style
				redo=bold;
			else
				redo=(tf.isBold()==bold);
			if(redo){
				Typeface tfc;
				if(bold)
					tfc=Typeface.defaultFromStyle(Typeface.BOLD);
				else
					tfc=Typeface.defaultFromStyle(Typeface.NORMAL);				
				tv.setTypeface(tfc);
			}*/

			String mag=Double.isNaN(d.getMag())?STRING:String.format(Locale.US,"%.1f",d.getMag());

			//String dim=d.getA()+"x"+d.getB();//String.format("%.1f",Math.max(d.getA(),d.getB()));
			if(d.hasDimension()){
				String dima=Double.isNaN(d.getA())?STRING:String.format(Locale.US,"%.1f",d.getA());
				String dimb=Double.isNaN(d.getB())?STRING:String.format(Locale.US,"%.1f",d.getB());
				s=d.getConString()+" "+mag+"m  "+(Double.isNaN(d.getA())&&Double.isNaN(d.getB())?STRING:dima+"x"+dimb+"'");
			}
			else
				s=d.getConString()+" "+mag+"m";
			
			if(d.getCatalog()==AstroCatalog.WDS||d.getCatalog()==AstroCatalog.HAAS){
				if(d instanceof DoubleStarObject){
					DoubleStarObject o=(DoubleStarObject)d;
					s=d.getConString()+" "+o.getDsoSelNameSecondLine();
				}
			}

			data dat=dataMap.get(d);

			String azS;
			String altS;
			if(dat==null){				
				dat=new data(d);
				dat.fill();
				dataMap.put(d, dat);
				Log.d(TAG,"new data");
			}

			azS=dat.azS;
			altS=dat.altS;
			//double az=AstroTools.Azimuth(lst, lat, d.ra, d.dec);
			//double alt=AstroTools.Altitude(lst, lat, d.ra, d.dec);
			String azaltStr=AZ+azS+ALT+altS;

			String visStr="";
			if(d.hasVisibility())
				visStr=best_vis+"="+String.format(Locale.US,"%.1f",dat.highvis);
			if(dat.highvis>0.0001)
				visStr+=_at_+Settings1243.getEpFocus(dat.highep)+_mm_eyepiece;
			s=s+"\n"+azaltStr+(d.hasVisibility()?"\n"+visStr:"");
			((TextView) convertView.findViewById(R.id.obslistitem_info)).setText(s);

			ChB ch=(ChB)convertView.findViewById(R.id.obslistitem_ch);
			ch.setChecked(h.y);
			ch.setOnClickListener(new OnClickListener() {   
				public void onClick(View v) {       
					int pos=((ChB)v). position;
					boolean status=((ChB)v).isChecked();
					ObsInfoListImpl.Item h=displayList.get(pos);//(ObsInfoListImpl.Item)ListHolder.getListHolder().
					//get(Global.activeObsList).get(pos);
					h. y=status; //as display list and underlying list share the same objects, underlying list objects update automatically
					if(isSkyViewOn()&& skyview!= null){
						ObsList1243. this.runOnUiThread( new Runnable(){
							@Override
							public void run(){

								skyview.initDsoList();
								skyview.invalidate();

							}
						});
					}
				}
			});
			ch.position=position;

			//make dark background
			if(Settings1243.getDarkSkin()||Settings1243.getNightMode())
				convertView.setBackgroundColor(0xff000000);
			Log.d(TAG,"returning view for object "+d);
			return convertView;
		}
		public int getCount(){
			return displayList.size();//ListHolder.getListHolder().
			//get(Global.activeObsList).getCount();
		}
		public Object getItem(int position){
			return displayList.get(position);//ListHolder.getListHolder().
			//get(Global.activeObsList).get(position);
		}
		public long getItemId(int position){
			return position;
		}
	}

	@Override
	protected void onResume(){
		onResumeCode();
		super.onResume();
		//Global.context = this;
		/*	if(initRequired){
			initRequired=false;
			return;
		}*/



	}
	private void onResumeCode(){
		Settings1243.nightGuardReset();
		updateDataMap();
		
		if(!getImportRunningFlag())
			updateList(); 
		if(isSkyViewOn()&&skyview!=null){
			skyview.initDraw();
			skyview.initExternal( new GraphRec(this));
		}
	}
	
	private boolean isSkyViewOn(){
		return Settings1243.getSharedPreferences(getApplicationContext()).getBoolean(Constants.SHOW_OBS_CUV, false);
	}
	private void setSkyView(boolean show){
		Settings1243.putSharedPreferences(Constants.SHOW_OBS_CUV, show, getApplicationContext());

	}
	private void updateSkyView(){
		boolean show=isSkyViewOn();//Settings1243.getSharedPreferences(getApplicationContext()).getBoolean(Constants.SHOW_OBS_CUV, true);
		//View view=findViewById(R.id.sky_view);
		
		if(skyview!=null){
			
			if(show){
				skyview.setVisibility(View.VISIBLE);
				//skyview=(CuV)view;
				skyview.setCallType(CuV.EXTERNAL);
				skyview.setProcessLongTouch();
				skyview.enableDraw();
				//skyview.initOther( new GraphRec(Query1243.this));
				

				skyview.initDraw();
				skyview.initExternal( new GraphRec(this));
				
			}
			else{
				skyview.setVisibility(View.GONE);
				skyview.disableDraw();				
				skyview.clearLists();
			}
			
			
			
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putBoolean("destroyed", true);
		//new Prefs(this).save();
	}
	//override the system search request in night mode only
	@Override
	public boolean onSearchRequested() {
		return AstroTools.invokeSearchActivity(this);
	}


	DownloadService mService;
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
	boolean mBound=false;

	/**
	 * internal purpose procedure. download dss pics for all objects in obs list
	 */
	private void downloadList(){
		boolean download=Settings1243.getSharedPreferences(this).getBoolean(Constants.OBS_DOWNLOAD,true);		
		if(!download)return;
		Settings1243.putSharedPreferences(Constants.OBS_DOWNLOAD, false, this);
		Log.d(TAG,"mService="+mService);
		//Settings1243.setValidUntil(Details1243.checkLicense());

		for(ObsInfoListImpl.Item item:displayList){
			Global.dss.downloadDSS(item.x, null,mService);
		}

	}
	@Override
	protected void onStart(){
		super.onStart();
		if(Global.DOWNLOAD_OBS){
			Intent intent = new Intent(this, DownloadService.class);
			bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		}
	}
	
	/**
	 * called from skyview on long touch
	 */
	public void goFullSkyView(){
		if(skyview==null)return;
		skyview.saveScreenIntoPrefs();
		final Intent i = new Intent(this, Graph1243.class);
		startActivity(i);
	}

	Handler initHandler=new Handler(){
		@Override
		public void handleMessage(Message msg){
			lst=updateLst();
			lat=updateLattitude();

			setContentView(R.layout.obslist);  	
			tv=(TextView)findViewById(R.id.obs_num_obj);
			textViewObjNum=((TextView)findViewById(R.id.obs_num_obj));
			int obsList=Settings1243.getSharedPreferences(ObsList1243.this).getInt(Constants.ACTIVE_OBS_LIST, InfoList.PrimaryObsList);
			InfoList iL=ListHolder.getListHolder().get(obsList);
			//int count=iL.getCount();
			//tv.setText(count+ " obj");
			//setTitle(iL.getListName()); 
			setListName(getListName());
			
			cmpMap.put(ObsInfoListImpl.SORT_NUMBER, 1);
			cmpMap.put(ObsInfoListImpl.SORT_CONSTELLATION, 1);
			cmpMap.put(ObsInfoListImpl.SORT_MAGNITUDE, 1);
			cmpMap.put(ObsInfoListImpl.SORT_DIMENSION, 1);
			cmpMap.put(ObsInfoListImpl.SORT_SETTIME, 1);
			cmpMap.put(ObsInfoListImpl.SORT_RA, 1);
			cmpMap.put(ObsInfoListImpl.SORT_DEC, 1);
			cmpMap.put(ObsInfoListImpl.SORT_TYPE, 1);
			cmpMap.put(ObsInfoListImpl.SORT_ALT, 1);

			workerThread2 = new HandlerThread("");
			workerThread2.start();
			workerHandler2 = new Handler(workerThread2.getLooper());

			mAdapter=new OLadapter();
			setListAdapter(mAdapter);
			Log.d(TAG,"adapter set");
			//Disable fading edge at night
			boolean nightmode = Settings1243.getNightMode();
			getListView().setVerticalFadingEdgeEnabled(!nightmode);

			if(ALEX_MENU_FLAG){
				initAlexMenu(nightmode);
				initAlexContextMenu();
			}
			else
				registerForContextMenu(getListView());
			findRunner.setMatcher(new FindRunner.Matcher() {			
				@Override
				public boolean match(Object o, String searchString) {
					if(o instanceof ObsInfoListImpl.Item){
						ObsInfoListImpl.Item obj=(ObsInfoListImpl.Item)o;
						/*if(!"".equals(searchString)&&(obj.x.getShortName().toUpperCase().contains(searchString.toUpperCase())
								||obj.x.getLongName().toUpperCase().contains(searchString.toUpperCase()))){
							return true;*/
							if(!"".equals(searchString)&&(getObsListName(obj.x).toUpperCase().contains(searchString.toUpperCase()))){
								return true;
						}
					}				
					return false;
				}
			});
			initConstellationList();
			
			workerThread = new HandlerThread("");
			workerThread.start();
			workerHandler = new Handler(workerThread.getLooper());
			
			View view=findViewById(R.id.sky_view);
			if(view!=null)
				skyview=(CuV)view;
			
			updateSkyView();
			//	onResumeCode();

			/*  Analisator an=new Analisator();
	        an.addVar("x",5);
	        an.setInputString("x#4");
	        an.compile();
	        Log.d(TAG,""+an.calculate());*/
		}
	};

	//boolean initRequired=false;//global init


	@Override
	public void onCreate(Bundle savedInstanceState) {
		//	Global.appContext = getApplicationContext();
		//	Log.d(TAG,"on create");
		/*	if(Init.initRequired()){
			super.onCreate(savedInstanceState); 
			finish();
			Intent intent=new Intent(this,DSOmain.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return;
		}*/

		//	if (Init.initRequired())
		//		initRequired=true;

		//	if(savedInstanceState!=null){
		//			if(savedInstanceState.getBoolean("destroyed", false)){
		//super.onCreate(savedInstanceState); 
		/*finish();
				Intent intent=new Intent(this,DSOmain.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				return;*/
		//	if (Init.initRequired())
		//		initRequired=true;
		//		}
		//	}
		//	Log.d(TAG,"initRequired="+initRequired);
		//boolean nightMode = Settings.setDayNightSky(this);
		//	Settings.setDayNightList(this);
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

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
		
		best_vis=getString(R.string.best_vis);
		_mm_eyepiece=getString(R.string._mm_eyepiece);
		_at_=getString(R.string._at_);
		initHandler.handleMessage(null);
	}

	

	protected void updateList(){
		//	SharedPreferences prefs=getSharedPreferences(Constants.PREFS,Context.MODE_PRIVATE);
		SharedPreferences prefs=Settings1243.getSharedPreferences(this);
		boolean showSelected=prefs.getBoolean(SHOW_SELECTED, true);
		int obsList=Settings1243.getSharedPreferences(this).getInt(Constants.ACTIVE_OBS_LIST, InfoList.PrimaryObsList);
		Iterator it=ListHolder.getListHolder().get(obsList).iterator();
		displayList=new ArrayList<ObsInfoListImpl.Item>();
		int hidden=0;
		Calendar c=AstroTools.getDefaultTime(this);
		for(;it.hasNext();){
			ObsInfoListImpl.Item item=(ObsInfoListImpl.Item)it.next();
			item.x.recalculateRaDec(c);
			if(showSelected)
				displayList.add(item);
			else{
				if(!item.y)//adding non selected items only
					displayList.add(item);
				else
					hidden++;
			}			
		}
		
		boolean imageson=areImagesOn();
		if(imageson&&!getImportRunningFlag()){
			new ArePicsNeededTask().execute(null );
		}
		else 
			showpics=false;

		//showpics=areImagesOn()&&showPics()&&!getImportRunningFlag();
		mAdapter.notifyDataSetChanged();
		String objnumStr=displayList.size()+ getString(R.string._obj);
		if(hidden>0)
			objnumStr+=getString(R.string._displayed_)+hidden+getString(R.string._obj_hidden);
		//((TextView)findViewById(R.id.obs_num_obj)).setText(objnumStr);
		setObjNum(objnumStr);
		dirty=true;
		findRunner.reset();

	}
	private void setObjNum(String text){
		textViewObjNum.setText(text);
	}
	class ArePicsNeededTask extends AsyncTask<Void,Void,Boolean>{
        @Override
        protected Boolean doInBackground(Void...ds){
             return showPics();
       }
        @Override
        protected void onPostExecute(Boolean  res) {
             showpics= res;
             mAdapter.notifyDataSetChanged();
            
       }
 }

	
	/**
	 * 
	 * @return true if there is at least one pic in the list
	 * so that the correct format is used
	 */
	private boolean showPics(){
		for(ObsInfoListImpl.Item item:displayList){
			List<String>list=Details1243.getPicturePaths(getApplicationContext(),item.x);
			if(list.size()>0)
				return true;
		}
		return false;
	}
	@Override
	public void onCreateContextMenu(ContextMenu menu,View v,ContextMenuInfo menuInfo){
		super.onCreateContextMenu(menu, v, menuInfo);
		Log.d(TAG,"onCreateContextMenu");
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.obs_context_menu, menu);
	}

	public boolean onContextItemSelected(MenuItem item){

		AdapterContextMenuInfo info=(AdapterContextMenuInfo)item.getMenuInfo();
		//	String s=list.get((int) info.id).get(NAME);
		//	int name=Integer.parseInt(s);  
		int pos=(int) info.id;

		return parseContextMenu(item.getItemId(), pos) ? true : super.onContextItemSelected(item);
	}
	private Dialog getMoveDialog(){
		InputDialog dimp=new InputDialog(this);              
		dimp.setMessage(getString(R.string.please_press_on_list_item_to_put_this_object_before));
		dimp.setPositiveButton(getString(R.string.ok), new InputDialog.OnButtonListener() {
			public void onClick(String v) {
				int count=Settings1243.getSharedPreferences(ObsList1243.this).getInt(Constants.OBS_LIST_MOVE_DIALOG, 0);
				count++;
				Settings1243.putSharedPreferences(Constants.OBS_LIST_MOVE_DIALOG, count, ObsList1243.this);

			}
		});


		return dimp;
	}
	private boolean parseContextMenu(int itemId, int pos) {
		int obsList=Settings1243.getSharedPreferences(this).getInt(Constants.ACTIVE_OBS_LIST, InfoList.PrimaryObsList);
		final InfoList infoList=ListHolder.getListHolder().get(obsList);
		final ObsInfoListImpl.Item h=displayList.get(pos);//(ObsInfoListImpl.Item)infoList.get(pos);
		AstroObject dso=h.x;
		Log.d(TAG,"dso="+dso);
		Command command;
		if(getImportRunningFlag()){
			InputDialog.message(this, R.string.import_is_running_).show();
			return true;
		}
		switch (itemId){
		case R.id.details_obs_menu:
			new DetailsCommand(dso,this).execute();
			return true;
		case R.id.picture_obs_menu:
			command=new PictureCommand(this,dso);
			command.execute();
			return true;
		case R.id.move_obs_menu:
			mMove.setWhat(pos);
			mMove.startMove();
			int count=Settings1243.getSharedPreferences(ObsList1243.this).getInt(Constants.OBS_LIST_MOVE_DIALOG, 0);
			if(count<2){
				registerDialog(getMoveDialog()).show();
			}


			return true;
		case R.id.remove:		
			InputDialog dl=new InputDialog(this); 
			dl.setTitle(getString(R.string.observation_list_confirmation));
			dl.setMessage(getString(R.string.do_you_want_to_remove_the_object_));
			dl.setPositiveButton(getString(R.string.ok), new InputDialog.OnButtonListener() {

				@Override
				public void onClick(String value) {
					int upos=findPos(h);//looking for position in underlying list
					if(upos!=-1)
						infoList.remove(upos);
					updateList();
					if(skyview!=null)
						skyview.updateObsList();
				}
			});
			dl.setNegativeButton(getString(R.string.cancel));
			registerDialog(dl).show();
			return true;
		case R.id.removeall:			
			dl=new InputDialog(this); 
			dl.setTitle(getString(R.string.observation_list_confirmation));
			dl.setMessage(getString(R.string.do_you_want_to_remove_all_objects_));
			dl.setPositiveButton(getString(R.string.ok), new InputDialog.OnButtonListener() {

				@Override
				public void onClick(String value) {
					infoList.removeAll();
					updateList();	
					setListName("");
					if(skyview!=null)
						skyview.updateObsList();
				}
			});
			dl.setNegativeButton(getString(R.string.cancel));
			registerDialog(dl).show();

			return true;
		case R.id.removech:			
			dl=new InputDialog(this); 
			dl.setTitle(getString(R.string.observation_list_confirmation));
			dl.setMessage(getString(R.string.do_you_want_to_remove_marked_objects_));
			dl.setPositiveButton(getString(R.string.ok), new InputDialog.OnButtonListener() {

				@Override
				public void onClick(String value) {
					Iterator it=infoList.iterator();
					for(;it.hasNext();){
						ObsInfoListImpl.Item item=(ObsInfoListImpl.Item)it.next();
						if(item.y)
							it.remove();
					}
					updateList();	
					if(skyview!=null)
						skyview.updateObsList();
				}
			});
			dl.setNegativeButton(getString(R.string.cancel));
			registerDialog(dl).show();
			return true;
		case R.id.oaddnote:

			command=new NewNoteCommand(this, dso, Calendar.getInstance(), "");
			command.execute();

			return true;
		case R.id.oseenotes:

			command=new GetObjectNotesCommand(this,dso);
			command.execute();
			return true;	
		case R.id.allnotes_obs_menu:

			command=new GetAllNotesCommand(this);
			command.execute();
			return true;
		case R.id.oedit:
			/*	Log.d(TAG,"catalog="+dso.getCatalog());
			if(dso.getCatalog()!=AstroCatalog.CUSTOM_CATALOG){
				InputDialog.message("Applicable only to objects without catalogs").show();
				return true;
			}
			if(!(dso instanceof CustomObject)){
				InputDialog.message("Applicable only to objects without catalogs").show();
				return true;
			}
			Intent i=new Intent(this,NewObjectActivity.class);

			i.putExtra("edit", true);
			Global.obs_pass=(CustomObject)dso;
			startActivity(i);*/
			
			if(SearchRules.isEdited(dso.getCatalog())){
				InfoList list=ListHolder.getListHolder().get(obsList);
				int position=-1;
				//looking for obj position in global obs list
				for(int i=0;i<list.getCount();i++){
					ObsInfoListImpl.Item item=(ObsInfoListImpl.Item)list.get(i);
					if(item.x.equals(dso)){
						position=i;
						break;
					}
				}
				if(position==-1)//this should not be
					return true;
				//Global.obs_pass=(CustomObject)dso;
				Settings1243.putSharedPreferences(Constants.NCOA_OBJECT, dso, this);
				Intent i=new Intent(this,NewCustomObjectActivityAsList1243.class);
				i.putExtra(EDIT, true);	
				int p=AstroTools.findPosByCatId(dso.getCatalog());
				if(dso.getCatalog()==AstroCatalog.CUSTOM_CATALOG){
					p=-1;
					i.putExtra(OBJ_POS,position);//object pos in global list
				}
				i.putExtra(CATALOG_POSITION,p);


				startActivityForResult(i,GET_CODE);
			}
			else registerDialog(InputDialog.message(ObsList1243.this,R.string.not_applicable)).show();
			return true;
		default: return false;
		}
	}
	/**
	 * 
	 * @param item - item to be found in InfoList
	 * @return item and position in underlying InfoList
	 */
	private int findPos(ObsInfoListImpl.Item item){
		int obsList=Settings1243.getSharedPreferences(this).getInt(Constants.ACTIVE_OBS_LIST, InfoList.PrimaryObsList);
		Iterator it=ListHolder.getListHolder().get(obsList).iterator();
		int k=0;
		for(;it.hasNext();){
			ObsInfoListImpl.Item i=(ObsInfoListImpl.Item)it.next();
			if(i.x.equals(item.x))
				return k;
			k++;
		}
		return -1;
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		if(requestCode==GET_CODE){//from NewObjectActivity
			/*Bundle b=data.getExtras();
			boolean changed=data.getBooleanExtra("changed",false);
			Exportable e=AstroTools.getExportableFromBundle(b);
			if(e instanceof AstroObject){
				InfoListFiller filler=new ObsListFiller(Arrays.asList(new AstroObject[]{(AstroObject)e}) );
				int obsList=Settings.getSharedPreferences(this).getInt(Constants.ACTIVE_OBS_LIST, InfoList.PrimaryObsList);
				ListHolder.getListHolder().get(obsList).fill(filler);
				updateList();
			}
			else
				if(changed)
					updateList();*/
			if(mAdapter!=null)//to avoid issues with coming back to killed activity
				updateList();

			/*	int objType=b.getInt("objType");
			byte[] objrep=b.getByteArray("byteArray");
			if(objrep!=null){
				DataInputStream dis=new DataInputStream(new ByteArrayInputStream(objrep));
				Object o=ObjectInflater.getInflater().inflate(objType,dis);

			}*/
		}
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.obs_menu, menu);
		return true;
	}
	/* The AstroTools one used instead
	private InputDialog getDialog(String title,final Runnable r){
		InputDialog dimp=new InputDialog(this);                
		dimp.setMessage(title);
		dimp.setPositiveButton("OK", new OnButtonListener() {
			public void onClick(String s) {
				r.run();
			}
		});
		dimp.setNegativeButton("Cancel");
		dimp.create();
		return dimp;

	}
	 */

	private static int GET_CODE=1;
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return parseMenu(item.getItemId());
	}

	private static final int DSOPLANNER_FORMAT=0;
	private static final int SKYSAFARI_FORMAT=1;
	private static final int SKYTOOLS_FORMAT=2;
	private static final int NSOG=3;
	
	/**
	 * list name after Observation List 1 or empty string of active obs list
	 * @return
	 */
	private String getListName(){
		final int obsList=Settings1243.getSharedPreferences(this).getInt(Constants.ACTIVE_OBS_LIST, InfoList.PrimaryObsList);
		/*final InfoList iL=ListHolder.getListHolder().get(obsList);

		String name=iL.getListName();
		String pat="[a-zA-Z[ ]]+[1-4][\\:][ ]";
		Pattern pattern=Pattern.compile(pat);
		Matcher matcher=pattern.matcher(name);
		if(matcher.find()){
			name=name.substring(matcher.end());
		}
		else
			name="";
		return name;*/
		return getListName(obsList-InfoList.PrimaryObsList+1);
	}
	/**
	 * list name  after Observation List 1 or empty string of any obs list
	 * @param num
	 * @return
	 */
	private String getListName(int num){
		final InfoList iL=ListHolder.getListHolder().get(InfoList.PrimaryObsList+num-1);

		String name=iL.getListName();
		Log.d(TAG,"1 name="+name);
		String pat=".+[1-4][\\:][ ]";
		Pattern pattern=Pattern.compile(pat);
		Matcher matcher=pattern.matcher(name);
		if(matcher.find()){
			name=name.substring(matcher.end());
		}
		else
			name="";
		Log.d(TAG,"2 name="+name);
		return name;
	}
	private void setListName(String name){
		final int obsList=Settings1243.getSharedPreferences(this).getInt(Constants.ACTIVE_OBS_LIST, InfoList.PrimaryObsList);
		final InfoList iL=ListHolder.getListHolder().get(obsList);

		String name2=getString(R.string.observation_list_)+(obsList-InfoList.PrimaryObsList+1)+("".equals(name)?"":": ")+name;
		iL.setListName(name2);
		dirty=true;
		setTitle(name2);
	}
	class data2{
		InfoList list;
		ErrorHandler eh;
		public data2(InfoList list, ErrorHandler eh) {
			super();
			this.list = list;
			this.eh = eh;
		}
		
	}
	class data3{
		String message;
		ErrorHandler eh;
		public data3(String message, ErrorHandler eh) {
			super();
			this.message = message;
			this.eh = eh;
		}
		@Override
		public String toString() {
			return "data3 [message=" + message + ", eh=" + eh + "]";
		}
		
		
		
	}
	
	private boolean stop_import=false;
	/**
	 * 
	 * @param flag true to stop, false to clear
	 */
	private synchronized void setStopImportFlag(boolean flag){
		stop_import=flag;
	}
	private synchronized boolean getStopImportFlag(){
		return stop_import;
	}
	
	class ImportHandler extends Handler{
		/*volatile int count=0;
		volatile int obsList;
		InfoListArrayFiller filler;
		public void init(){
			count=0;
			obsList=Settings1243.getSharedPreferences(getApplicationContext()).getInt(Constants.ACTIVE_OBS_LIST, InfoList.PrimaryObsList);
		}
		public ImportHandler(){
			super();
			filler=new InfoListArrayFiller();
		}*/
		@Override
		public void handleMessage(Message msg){
			
		
			data3 d=(data3)msg.obj;
			
			if(d.eh!=null&&!ObsList1243.this.isFinishing()){
				setImportRunningFlag(false);
				setProgressBarIndeterminateVisibility(false);
				if(d.eh.hasError())
					d.eh.showError(ObsList1243.this);
				updateList();
			}
			
			if(d.eh==null)				
				setObjNum(d.message);
					
			
			//Log.d(TAG,"d="+d);
			
		}
	}
	private ImportHandler processHandler=new ImportHandler();
	
	public boolean parseMenu(int itemid) {
		int order;
		final int obsList=Settings1243.getSharedPreferences(this).getInt(Constants.ACTIVE_OBS_LIST, InfoList.PrimaryObsList);
		final InfoList iL=ListHolder.getListHolder().get(obsList);
		//	File f=new File(getApplicationContext().getDir("",MODE_WORLD_READABLE ),"prefObs");
		//final File f=new File(Global.path,"observationList.txt");
		if(getImportRunningFlag()){
			InputDialog.message(this, R.string.import_is_running_).show();
			return true;
		}
		
		switch (itemid) {
		case R.id.showhide_pics:
			setShowImagesFlag(!areImagesOn());
			updateList();
			return true;
		
		case R.id.otoggle:
			boolean show=isSkyViewOn();
			setSkyView(!show);
			//Settings1243.putSharedPreferences(Constants.SHOW_QUERY_CUV, !show, getApplicationContext());
			updateSkyView();
			if(show&&skyview!=null){//save current screen when switching sky view off as it is not saved in onPause if sky view is off
				skyview.saveScreenIntoPrefs();
			}
			//modifyContentView(checkboxes);
			return true;
		
		
		case R.id.dss_obs:
			if(Global.FREE_VERSION){
				registerDialog(AstroTools.getProVersionDialog(this)).show();
				return true;
			}
			
			InputDialog d4 = new InputDialog(this);
			d4.setTitle(getString(R.string.please_confirm));
			d4.setMessage(getString(R.string.download_dss_images_of_observation_list_objects_));			
			d4.setPositiveButton(getString(R.string.ok), new OnButtonListener() {
				public void onClick(String s) {
					List<Double>list=new ArrayList<Double>();
					for(Object o:iL){
						ObsInfoListImpl.Item item=(ObsInfoListImpl.Item)o;
						list.add(item.x.getRa());
						list.add(item.x.getDec());
						list.add(item.x.getA());
					}
					double[] a=new double[list.size()];
					int i=0;
					for(Double d:list){
						a[i++]=d;
					}
					
					Intent intent= new Intent(ObsList1243.this, MultipleDSSdownloadIntentService.class);
					intent.putExtra(Constants.MDSS_DATA, a);
					startService(intent);
					
					
				}
			});
			d4.setNegativeButton(getString(R.string.cancel));
			registerDialog(d4).show();
			return true;
		case R.id.name_obs:
			
			//Ask for the list name
			InputDialog d5 = new InputDialog(ObsList1243.this);
			d5.setValue(getListName());
			d5.setType(InputDialog.DType.INPUT_STRING);
			d5.setTitle(getString(R.string.set_list_name));
			d5.setMessage(getString(R.string.please_set_list_name));
			d5.setPositiveButton(getString(R.string.ok), new InputDialog.OnButtonListener() {
				public void onClick(String value) {
					
					/*String name="Observation List "+(obsList-InfoList.PrimaryObsList+1)+("".equals(value)?"":": ")+value;
					iL.setListName(name);
					dirty=true;
					setTitle(name);*/
					setListName(value);
				}
			});
			d5.setNegativeButton(getString(R.string.cancel), new InputDialog.OnButtonListener() {
				public void onClick(String value) {
					
				}
			});
			registerDialog(d5).show();
			
			
			return true;
		case R.id.search_obs:
			onSearchRequested();
			return true;
		
		case R.id.add_obs:
			if(Global.FREE_VERSION){
				registerDialog(AstroTools.getProVersionDialog(this)).show();
				return true;
			}
			if(Global.BASIC_VERSION)
				return true;
			Intent i=new Intent(this,NewCustomObjectActivityAsList1243.class);
			i.putExtra(EDIT, false);//new object
			i.putExtra(CATALOG_POSITION, -1);//custom catalog
			startActivityForResult(i, GET_CODE);

			return true;
		case R.id.list1_obs:
			Settings1243.putSharedPreferences(Constants.ACTIVE_OBS_LIST, InfoList.PrimaryObsList, this);
			//	Global.activeObsList=InfoList.PrimaryObsList;
			updateList();
			//setTitle(ListHolder.getListHolder().get(InfoList.PrimaryObsList).getListName());
			setListName(getListName(1));
			if(skyview!=null)
				skyview.updateObsList();
			return true;
		case R.id.list2_obs:

			//	Global.activeObsList=InfoList.PrimaryObsList+1;
			Settings1243.putSharedPreferences(Constants.ACTIVE_OBS_LIST, InfoList.PrimaryObsList+1, this);
			updateList();
			//setTitle(ListHolder.getListHolder().get(InfoList.PrimaryObsList+1).getListName());
			setListName(getListName(2));
			if(skyview!=null)
				skyview.updateObsList();
			return true;
		case R.id.list3_obs:

			//Global.activeObsList=InfoList.PrimaryObsList+2;
			Settings1243.putSharedPreferences(Constants.ACTIVE_OBS_LIST, InfoList.PrimaryObsList+2, this);

			updateList();
			//setTitle(ListHolder.getListHolder().get(InfoList.PrimaryObsList+2).getListName());
			setListName(getListName(3));
			if(skyview!=null)
				skyview.updateObsList();
			return true;
		case R.id.list4_obs:

			//Global.activeObsList=InfoList.PrimaryObsList+3;
			Settings1243.putSharedPreferences(Constants.ACTIVE_OBS_LIST, InfoList.PrimaryObsList+3, this);

			updateList();
			//setTitle(ListHolder.getListHolder().get(InfoList.PrimaryObsList+3).getListName());
			setListName(getListName(4));
			if(skyview!=null)
				skyview.updateObsList();
			return true;
		case R.id.save_obs:

			//The save runnable
			final Runnable r=new Runnable(){
				public void run(){
					boolean noError=false;
					try{
						if(!AstroTools.isExternalStorageAvailable(AstroTools.EXT_STORAGE_WRITABLE)){
							AstroTools.showExtStorageNotAvailableMessage(AstroTools.EXT_STORAGE_WRITABLE, ObsList1243.this);
							return;
						}

						InfoListSaver saver=new InfoListStringSaverImp(
								new FileOutputStream(Global.exportImportPath + InputDialog.getResult()));						
						AnotherImpl infoList=new AnotherImpl();

						for(ObsInfoListImpl.Item item:displayList){
							infoList.add(item);//saving visible items only
						}
						noError=infoList.save(saver);						
					}
					catch(Throwable e){
						Log.d(TAG,"Exception="+e);
						noError=true;
					}
					String message=(!noError?getString(R.string.export_error):getString(R.string.export_successfull_));
					registerDialog(InputDialog.message(ObsList1243.this,message)).show();
				}
			};

			//Ask for the file name
			InputDialog d = new InputDialog(ObsList1243.this);
			d.setType(InputDialog.DType.INPUT_STRING);
			d.setValue(getListName());
			d.setTitle(getString(R.string.export_to_the_file));
			d.setMessage(getString(R.string.please_enter_the_file_name_for_exporting_warning_the_file_with_the_same_name_will_be_silently_overwritten_in_the_) + Global.exportImportPath + getString(R.string._folder_));
			d.setPositiveButton(getString(R.string.ok), new InputDialog.OnButtonListener() {
				public void onClick(String value) {
					r.run();
				}
			});
			registerDialog(d).show();
			/* Old way 
			GetStringDialog d = new GetStringDialog();
			d.show(this,	"Export to the file", 
					"Please enter the file name for exporting.\n\n" + 
					"WARNING: the file with the same name will be silently overwritten " + 
					"in the " + Global.path + " folder.", r);
			 */
			return true;
		case R.id.paste_obs:
			Runnable r1=new Runnable(){
				public void run(){
					ObsList1243.this.updateList();
				}
			};

			Command command=new PasteCommand(iL,obsList,r1,this,
					getString(R.string.do_you_want_to_paste_into_current_observation_list_));
			command.execute();

			return true;

		case R.id.load_obs: //Import
			//Ask to pick a file
			/*	if(Global.FREE_VERSION){
				registerDialog(AstroTools.getProVersionDialog(this)).show();
				return true;
			}*/
			if(!AstroTools.isExternalStorageAvailable(AstroTools.EXT_STORAGE_READABLE)){
				AstroTools.showExtStorageNotAvailableMessage(AstroTools.EXT_STORAGE_READABLE, this);
				return true;
			}

			class Listener implements IPickFileCallback{

				int mode;
				public void setMode(int mode){
					this.mode=mode;
				}
				public void callbackCall(File file) {

					final File fl = file; //the path could be any
					int obsList=Settings1243.getSharedPreferences(ObsList1243.this).getInt(Constants.ACTIVE_OBS_LIST, InfoList.PrimaryObsList);

					final InfoList iL=ListHolder.getListHolder().get(obsList);

					//Fire the data loading thread (original code)
					final String name=iL.getListName();		

					class DialogRunnable implements Runnable{
						boolean external;
						public DialogRunnable(boolean external){
							this.external=external;
						}
						public void run(){

							try{
								//	Global.context = ObsList.this;

								final InfoListLoader listLoader;
								switch(mode){
								case DSOPLANNER_FORMAT:
									InfoListStringLoaderObsListImp loader=new InfoListStringLoaderObsListImp(new FileInputStream(fl));
									if(external){
										loader.setIgnoreCustomDbRefsFlag();
										//if(!Global.FREE_VERSION)
										//	loader.setIgnoreNgcicRefsFlag();
									}
									listLoader=loader;
									break;
								case SKYSAFARI_FORMAT:
									
									
									listLoader=new SkySafariLoaderImpl(
											new BufferedReader(new InputStreamReader(new FileInputStream(fl))), ObsList1243.this);
									break;
								case SKYTOOLS_FORMAT:
									
									
									listLoader=new SkyToolsLoader(
											new BufferedReader(new InputStreamReader(new FileInputStream(fl))), ObsList1243.this);
									break;
								default:listLoader=null;
									
								}

								if(listLoader!=null){
									class ImportRunnable implements Runnable{
										
										int count=0;
										int obsList;
										InfoListArrayFiller filler;
										InfoList iL;
										public void init(){
											count=0;
											obsList=Settings1243.getSharedPreferences(getApplicationContext()).getInt(Constants.ACTIVE_OBS_LIST, InfoList.PrimaryObsList);
											iL=ListHolder.getListHolder().get(obsList);
										}
										public ImportRunnable(){
											super();
											filler=new InfoListArrayFiller();
										}
										public void run(){
											/*ErrorHandler eh=new ErrorHandler();
											InfoList list=new ObsInfoListImpl("");
											eh=list.load(listLoader);
											Log.d(TAG,"list size="+list.getCount());
											Message msg=Message.obtain();												
											data2 d=new data2(list,eh);
											msg.obj=d;
											processHandler.sendMessage(msg)	;	*/
											ErrorHandler eh=new ErrorHandler();
											ObsInfoListImpl list=new ObsInfoListImpl("");
											Holder2<Item,Boolean>h;
											int i=0;
											init();
											boolean over=false;
											do{
												h=list.next(listLoader, eh);
												
													
												over=h.y;
												if(i++>Global.OBS_LIST_NUM_OBJECTS_LIMIT){
													over=true;
												}
												
												if(h.x!=null&&h.x.x!=null){
													process(h.x);
												}
												
												
												
												
												
												
												count++;
												if(count%100==0||over){
													Message msg=Message.obtain();	
													data3 d=new data3(count+" imported",over?eh:null);
													msg.obj=d;
													processHandler.sendMessage(msg)	;
												}
												

												if(getStopImportFlag()){
													setStopImportFlag(false);
													break;
												}

											}
											while(!over);
											
											
												
											
										}
										
										public void process(Item item){
														
												//InfoListFiller filler=new InfoListArrayFiller(new Object[]{d.item});
												filler.setObject(new Object[]{item});												
												iL.fill(filler);
										}
										
									};
										
										
									
									
									
											
									
								setProgressBarIndeterminateVisibility(true);	
								setImportRunningFlag(true);
								workerHandler.post(new ImportRunnable());	
								String lname=getListName();
								if("".equals(lname))
									setListName(fl.getName());
									
								}
							}
							catch(IOException e){
								Log.d(TAG,"Exception="+e);
							}
							//updateList();
						//	iL.setListName(name);//do not change name of the list after loading
							//Settings1243.redrawRequired=true;//new object may have been added to the custom database, so redraw of DSO Selection is required
						}			
					}




					Runnable r0=new Runnable(){
						public void run(){
							InputDialog d = new InputDialog(ObsList1243.this);
							d.setMessage(getResources().getString(R.string.did_you_export));
							d.setPositiveButton(getString(R.string.yes), new InputDialog.OnButtonListener() {

								@Override
								public void onClick(String value) {
									new DialogRunnable(false).run();

								}
							});
							d.setNegativeButton(getString(R.string.no),new InputDialog.OnButtonListener() {

								@Override
								public void onClick(String value) {
									new DialogRunnable(true).run();

								}
							});
							registerDialog(d).show();
						}
					};


					switch(mode){
					case DSOPLANNER_FORMAT:
						InputDialog d=AstroTools.getDialog(ObsList1243.this,
								getString(R.string.do_you_want_to_import_observation_list_from_)+fl.getAbsolutePath()+"?",r0);
						registerDialog(d).show();
						break;
					case SKYTOOLS_FORMAT:
					case SKYSAFARI_FORMAT:						
						new DialogRunnable(false).run();
						break;
					}
					//Confirmation
					//	Global.context = this;


				}

			}


			InputDialog d2=new InputDialog(this);
			//int value=Settings1243.getSharedPreferences(this).getInt(Constants.OBS_IMPORT_DIALOG_CHOICE, 0);
			d2.setTitle(getString(R.string.please_select_data_format_for_the_text_file_being_imported_internal_observation_list));
			d2.setValue("-1");
			String[] import_names;
			if(Global.BASIC_VERSION)
				import_names=new String[]{getString(R.string.dso_planner_import_),getString(R.string.sky_safari_import_),getString(R.string.sky_tools_import_)};
			else
				import_names=new String[]{getString(R.string.dso_planner_import_),getString(R.string.sky_safari_import_),getString(R.string.sky_tools_import_),getString(R.string.night_sky_observer_guide_internal_)};

			d2.setListItems(import_names,new InputDialog.OnButtonListener() {
				public void onClick(String value) {
					int val=AstroTools.getInteger(value, -1, -1, 10);
					Listener lis=new Listener();
					lis.setMode(val);
					if(val==-1)return;
					
					switch(val){
					case SKYTOOLS_FORMAT:
					case SKYSAFARI_FORMAT:
						if(Global.FREE_VERSION){
							registerDialog(AstroTools.getProVersionDialog(ObsList1243.this)).show();
							return;
						}
						break;
					case NSOG:
						if(Global.FREE_VERSION){
							registerDialog(AstroTools.getProVersionDialog(ObsList1243.this)).show();
							return;
						}
						processNSOG();
						return;
					}
					
					
					/*if(val==NSOG){
						
					}*/
					
					//FileDialogActivity1243.setPath(Global.exportImportPath);
					SelectFileActivity1243.setPath(Settings1243.getFileDialogPath(getApplicationContext()));
					SelectFileActivity1243.setListener(lis);
					Intent fileDialog = new Intent(ObsList1243.this, SelectFileActivity1243.class);
					startActivity(fileDialog);

				}
			});

			registerDialog(d2).show();



			/* SAND moved to the listener 
			String flname = FileDialogActivity.getName();
			final File fl=new File(Global.path,flname);

			//Fire the loading thread
			final String name=iL.getListName();
			Runnable r1=new Runnable(){
				public void run(){
					try{
						InfoListLoader loader=new InfoListStringLoaderObsListImp(new FileInputStream(fl));
						iL.load(loader);
					}
					catch(IOException e){
						Log.d(TAG,"Exception="+e);
					}
					updateList();
					iL.setListName(name);//do not change name of the list after loading
				}
			};			
			Dialog dialog1=getDialog("Do you want to import observation list from "+fl.getAbsolutePath()+"? That will override the current list's data.",r1);
			dialog1.show();
			 */
			return true;

		case R.id.con_obs:
			InputDialog d10=new InputDialog(this);
			d10.setTitle(getString(R.string.please_select_sort_order));
			d10.setValue("-1");
			d10.setListItems(new String[]{getString(R.string.name2),getString(R.string.constellation),getString(R.string.magnitude),getString(R.string.size),getString(R.string.set_time),
					getString(R.string.right_ascension),getString(R.string.declination),getString(R.string.type2),getString(R.string.altitude)},new InputDialog.OnButtonListener() {
				public void onClick(String value) {
					int val=AstroTools.getInteger(value, 0, 0, 20);
					switch(val){
					case NAME:
						int order=cmpMap.get(ObsInfoListImpl.SORT_NUMBER);
						cmpMap.put(ObsInfoListImpl.SORT_NUMBER, -order);
						iL.sort(ObsInfoListImpl.SORT_NUMBER, order);
						updateList();
						break;
					case CONSTELLATION:
						order=cmpMap.get(ObsInfoListImpl.SORT_CONSTELLATION);
						cmpMap.put(ObsInfoListImpl.SORT_CONSTELLATION, -order);
						iL.sort(ObsInfoListImpl.SORT_CONSTELLATION, order);
						updateList();
						break;
					case MAG:
						order=cmpMap.get(ObsInfoListImpl.SORT_MAGNITUDE);
						cmpMap.put(ObsInfoListImpl.SORT_MAGNITUDE, -order);
						iL.sort(ObsInfoListImpl.SORT_MAGNITUDE, order);
						updateList();
						break;
					case DIMENSION:
						order=cmpMap.get(ObsInfoListImpl.SORT_DIMENSION);
						cmpMap.put(ObsInfoListImpl.SORT_DIMENSION, -order);
						iL.sort(ObsInfoListImpl.SORT_DIMENSION, order);
						updateList();
						break;
					case SET:
						order=cmpMap.get(ObsInfoListImpl.SORT_SETTIME);
						cmpMap.put(ObsInfoListImpl.SORT_SETTIME, -order);
						iL.sort(ObsInfoListImpl.SORT_SETTIME, order);
						updateList();
						break;
					case RA:
						sort(iL,ObsInfoListImpl.SORT_RA);
						break;
					case DEC:
						sort(iL,ObsInfoListImpl.SORT_DEC);
						break;
					case TYPE:
						sort(iL,ObsInfoListImpl.SORT_TYPE);
						break;
					case ALT2:
						sort(iL,ObsInfoListImpl.SORT_ALT);
						break;
					}
				}
			});
			d10.show();
			return true;
			
			
			
		/*case R.id.mag_obs:
			

			//mAdapter.notifyDataSetChanged();
			return true;	
		case R.id.dim_obs:
			

			//mAdapter.notifyDataSetChanged();
			return true;
		case R.id.set_obs:
			

			//mAdapter.notifyDataSetChanged();
			return true;*/
		case R.id.oshare:
			/*if(Global.DOWNLOAD_OBS)
				downloadList();*/

			if(Settings1243.nightGuard(this)) return true;

			ByteArrayOutputStream out=null;
			try{
				out=new ByteArrayOutputStream();				
				InfoListSaver saver=new InfoListStringSaverImp(out,Global.SHARE_LINES_LIMIT,new Handler());
				//InfoList infoList=ListHolder.getListHolder().get(Global.activeObsList);
				AnotherImpl infoList=new AnotherImpl();
				//Iterator it=ListHolder.getListHolder().get(Global.activeObsList).getIterator();
				for(ObsInfoListImpl.Item item:displayList){
					infoList.add(item);//saving visible items only
				}
				infoList.save(saver);
			}
			catch(Exception e){
				Log.d(TAG,"Exception="+e);
			}
			if(out!=null){
				String s=out.toString();
				new ShareCommand(this,s).execute();
			}
			return true;
		case R.id.noted_obs:
			registerDialog(getSelectRangeDialog()).show();
			return true;
		case R.id.deselectall_obs:
			Iterator it=iL.iterator();

			for(;it.hasNext();){
				ObsInfoListImpl.Item h=(ObsInfoListImpl.Item)it.next();
				h.y=false;
			}
			updateList();
			return true;
		case R.id.showhide_obs:
			SharedPreferences prefs=Settings1243.getSharedPreferences(this);//getSharedPreferences(Constants.PREFS,Context.MODE_PRIVATE);
			boolean status=prefs.getBoolean(SHOW_SELECTED,true);    		
			prefs.edit().putBoolean(SHOW_SELECTED, !status).commit();
			updateList();
			return true;
		case R.id.find_obs:
			item_selected=-1;//clear previous "find"
			mAdapter.notifyDataSetChanged();
			String init_string=Settings1243.getStringFromSharedPreferences(this, Constants.OBS_FIND_STRING, "");//Settings1243.getSharedPreferences(this).
			//getString(Constants.OBS_FIND_STRING, "");


			//Ask for the "find" string
			d = new InputDialog(ObsList1243.this);
			d.setType(InputDialog.DType.INPUT_STRING);
			d.setValue(init_string);
			d.setTitle(getString(R.string.find));
			d.setMessage(getString(R.string.please_enter_the_string_to_find_in_the_list));
			d.setPositiveButton(getString(R.string.ok), new InputDialog.OnButtonListener() {
				public void onClick(String value) {					
					String find_string=InputDialog.getResult();
					Settings1243.putSharedPreferences(Constants.OBS_FIND_STRING, find_string, ObsList1243.this);	
					findRunner.setSearch(new FindRunner.BasicListAdapter(displayList), find_string);
					makeFind();
				}
			});
			registerDialog(d).show();
			return true;
		case R.id.next_obs:
			if(Global.DOWNLOAD_OBS){
				for(ObsInfoListImpl.Item item:displayList){
					Log.d(TAG,"dss="+makeName(30,item.x.ra,item.x.dec)+" obj="+item.x);
				}
			}
			makeFind();
			return true;

		}
		return false;
	}   
	
	private void sort(InfoList iL,int sort_type){
		int order=cmpMap.get(sort_type);
		cmpMap.put(sort_type, -order);
		iL.sort(sort_type, order);
		updateList();
	}
	
	boolean import_running=false;
	private void setImportRunningFlag(boolean flag){
		import_running=flag;
	}
	
	private boolean getImportRunningFlag(){
		return import_running;
	}
	
	private void processNSOG(){
		
		final InputDialog d = new InputDialog(ObsList1243.this);
		d.setTitle(getString(R.string.select_constellation));
		d.setMessage("");
		d.setValue(String.valueOf(-1));
		d.setListItems(spinStringArr, new InputDialog.OnButtonListener() {
			public void onClick(String value) {
				int pos = AstroTools.getInteger(value, 0,0,10000);
				if(pos!=-1){
					InfoListLoader listLoader=new NSOGLoader(ObsList1243.this,"nsog.db",pos+1);

					ErrorHandler eh=new ErrorHandler();
					int obsList=Settings1243.getSharedPreferences(ObsList1243.this).getInt(Constants.ACTIVE_OBS_LIST, InfoList.PrimaryObsList);

					final InfoList iL=ListHolder.getListHolder().get(obsList);
					String name=iL.getListName();
					((ObsInfoListImpl)iL).sortLoadedList();
					eh=iL.load(listLoader);
					if(eh.hasError()){
						eh.showError(ObsList1243.this);
						Log.d(TAG,"eh="+eh);
					}
					updateList();
					iL.setListName(name);//do not change name of the list after loading


				}
			}
		});
		registerDialog(d).show();
	}
	
	private String[] spinStringArr=new String[constellations.length-1];
	//private int[] spinIntArr=new int[constellations.length];
	private void initConstellationList(){
		
		for(int i=0;i<spinStringArr.length-1;i++)
			spinStringArr[i]=constellations[i+1];
		

	}
	
	private String makeName(int size,double ra,double dec){
		String sRa=String.format(Locale.US,"%.5f", ra);
		sRa=sRa.replaceAll(",", ".");
		String sDec=String.format(Locale.US,"%.5f", dec);
		sDec=sDec.replaceAll(",", ".");
		return size+" "+sRa+" "+sDec;		
	}
	private void makeFind(){
		int pos=findRunner.find();
		if(pos>-1){
			item_selected=pos;
			mAdapter.notifyDataSetChanged();
			getListView().setSelection(pos);
		}
		else{
			InputDialog d=InputDialog.message(ObsList1243.this,R.string.no_match_found_,0);
			d.setTitle(this.getString(R.string.find_results));
			registerDialog(d).show();
		}

	}
	
	
	private boolean skyViewIntent=false;
	public boolean isIntentToGraph(){
		return skyViewIntent;
	}
	public void clearGraphIntentFlag(){
		skyViewIntent=false;
	}
	
	
	AstroObject prevObject=null;
	public void onListItemClick(ListView parent,View v,int position,long id){
		//	String s=list.get(position).get(NAME);  
		if(getImportRunningFlag()){
			InputDialog.message(this, R.string.import_is_running_).show();
			return;
		}
		
		boolean show=isSkyViewOn();
		
		if(mMove.isMoving()){
			mMove.setWhere(position);
			mMove.move();
			updateList();
			return;
		}
		ObsInfoListImpl.Item h=displayList.get(position);//(ObsInfoListImpl.Item)ListHolder.getListHolder().get(Global.activeObsList).get(position);
		AstroObject obj=h.x;
		
		if(show&&skyview!=null)
			Settings1243.putSharedPreferences(Constants.CURRENT_ZOOM_LEVEL, skyview.getZoom(), this);

		//SAND Use current time not NOW! Global.cal=Calendar.getInstance();
		Calendar defc=AstroTools.getDefaultTime(this);
		Point.setLST(AstroTools.sdTime(defc));//need to calculate Alt and Az
		int zoom=Settings1243.getSharedPreferences(this).getInt(Constants.CURRENT_ZOOM_LEVEL, Constants.DEFAULT_ZOOM_LEVEL);
		obj.recalculateRaDec(defc);
		//Global.graphCreate=
		GraphRec rec=new GraphRec(zoom,obj.getAz(),obj.getAlt(),defc,obj,0,1);
		rec.save(this);//add graph settings for Graph Activity to process it					
		if(show&&skyview!=null&&!obj.equals(prevObject)){
			skyview.initExternal(rec);
		}
		else{
			Intent i = new Intent(this, Graph1243.class);
			skyViewIntent=true;
			startActivity(i); 
		}
		prevObject=obj;
		 

	}	

	//Custom menu implementation is below
	private void initAlexMenu(boolean nightMode) {

		boolean dayMode = !nightMode;

		aMenu = new alexMenu(this, this, getLayoutInflater());
		aMenu.setHideOnSelect(true);
		aMenu.setItemsPerLineInPortraitOrientation(4);
		aMenu.setItemsPerLineInLandscapeOrientation(4);
		aMenu.setSkin(nightMode, Settings1243.getDarkSkin());
		//mine
		float text_size=getResources().getDimension(R.dimen.text_size_small);//mine
		float density=getResources().getDisplayMetrics().density;
		text_size=text_size/density;
		aMenu.setTextSize((int)text_size);

		//load the menu items
		ArrayList<alexMenuItem> menuItems = new ArrayList<alexMenuItem>();
		menuItems.add(new alexMenuItem(R.id.search_obs,
				getString(R.string.search),  dayMode?R.drawable.am_search:R.drawable.ram_search, true ));
		menuItems.add(new alexMenuItem(R.id.name_obs,
				getString(R.string.rename),  dayMode?R.drawable.am_rename:R.drawable.ram_rename, true ));
		menuItems.add(new alexMenuItem(R.id.dss_obs,
				getString(R.string.dss),  dayMode?R.drawable.am_dss:R.drawable.ram_dss, true ));
		menuItems.add(new alexMenuItem(R.id.con_obs, 
				getString(R.string.sort), dayMode?R.drawable.am_sort:R.drawable.ram_sort, false ));
		/*menuItems.add(new alexMenuItem(R.id.ngcicdb, 
				"NGC numbr", dayMode?R.drawable.am_sort:R.drawable.ram_sort, false ));*/
		/*menuItems.add(new alexMenuItem(R.id.mag_obs, 
				MAGNITUDE, dayMode?R.drawable.am_sort:R.drawable.ram_sort, false ));
		menuItems.add(new alexMenuItem(R.id.dim_obs, 
				SIZE, dayMode?R.drawable.am_sort:R.drawable.ram_sort, false ));
		menuItems.add(new alexMenuItem(R.id.set_obs, 
				SET_TIME, dayMode?R.drawable.am_sort:R.drawable.ram_sort, false ));*/
		menuItems.add(new alexMenuItem(R.id.list1_obs, 
				getString(R.string.list_1), dayMode?R.drawable.am_file:R.drawable.ram_file, true ));
		menuItems.add(new alexMenuItem(R.id.list2_obs, 
				getString(R.string.list_2), dayMode?R.drawable.am_file:R.drawable.ram_file, true ));
		menuItems.add(new alexMenuItem(R.id.list3_obs, 
				getString(R.string.list_3), dayMode?R.drawable.am_file:R.drawable.ram_file, true ));
		menuItems.add(new alexMenuItem(R.id.list4_obs, 
				getString(R.string.list_4), dayMode?R.drawable.am_file:R.drawable.ram_file, true ));
		if(!Global.BASIC_VERSION)
			menuItems.add(new alexMenuItem(R.id.add_obs, 
					getString(R.string.add_object), dayMode?R.drawable.am_addobj:R.drawable.ram_addobj, true ));
		/*menuItems.add(new alexMenuItem(R.id.clip_obs, 
				"Clipboard", dayMode?R.drawable.am_addobj:R.drawable.ram_addobj, true ));*/
		menuItems.add(new alexMenuItem(R.id.paste_obs, 
				getString(R.string.paste), dayMode?R.drawable.am_paste:R.drawable.ram_paste, true ));
		menuItems.add(new alexMenuItem(R.id.load_obs, 
				getString(R.string.import2), dayMode?R.drawable.am_save:R.drawable.ram_save, true ));
		menuItems.add(new alexMenuItem(R.id.save_obs, 
				getString(R.string.export), dayMode?R.drawable.am_load:R.drawable.ram_load, true ));
		menuItems.add(new alexMenuItem(R.id.oshare,
				getString(R.string.share), dayMode?R.drawable.am_share:R.drawable.ram_share, true ));
		menuItems.add(new alexMenuItem(R.id.noted_obs,
				getString(R.string.mark),  dayMode?R.drawable.am_check:R.drawable.ram_check, true ));
		menuItems.add(new alexMenuItem(R.id.find_obs,
				getString(R.string.find),  dayMode?R.drawable.am_find_in_page:R.drawable.ram_find_in_page, true ));
		menuItems.add(new alexMenuItem(R.id.next_obs,
				getString(R.string.next),  dayMode?R.drawable.am_tplus:R.drawable.ram_tplus, true ));
		menuItems.add(new alexMenuItem(R.id.otoggle,
				getString(R.string.side_chart),  dayMode?R.drawable.am_flip:R.drawable.ram_flip, true ));
		menuItems.add(new alexMenuItem(R.id.showhide_pics,
				getString(R.string.images),  dayMode?R.drawable.am_image:R.drawable.ram_image, true ));


		
		//menuItems.add(new alexMenuItem(R.id.deselectall_obs,
		//		"Deselect All", 0, true ));
		//menuItems.add(new alexMenuItem(R.id.showhide_obs,
		//		"Show/Hide Checked", 0, true ));
		if (aMenu.isNotShowing()){
			try {
				aMenu.setMenuItems(menuItems);
			} catch (Exception e) {
				InputDialog.message(ObsList1243.this,MENU_ERROR + e.getMessage(), 0).show();
			}	
		}
	}

	public void MenuItemSelectedEvent(alexMenuItem selection) {
		parseMenu(selection.getId());
	}

	private void initAlexContextMenu() {
		contextMenu = new alexMenu(this, new OnMenuItemSelectedListener() {
			public void MenuItemSelectedEvent(alexMenuItem selection) {
				parseContextMenu(selection.getId(), contextMenu.getMenuItemId());
			}}, 
			getLayoutInflater());
		contextMenu.setHideOnSelect(true);
		contextMenu.setItemsPerLineInPortraitOrientation(1);
		contextMenu.setItemsPerLineInLandscapeOrientation(1);
		contextMenu.setSkin(Settings1243.getNightMode(), Settings1243.getDarkSkin());

		//mine
		float text_size=getResources().getDimension(R.dimen.table_main_text_size);//mine
		float density=getResources().getDisplayMetrics().density;
		text_size=text_size/density;
		contextMenu.setTextSize((int)text_size);//contextMenu.setTextSize(18)

		contextMenu.makeFloat();

		//load the menu items
		ArrayList<alexMenuItem> menuItems = new ArrayList<alexMenuItem>();
		menuItems.add(new alexMenuItem(R.id.move_obs_menu, getString(R.string.move), 0, true ));
		menuItems.add(new alexMenuItem(R.id.picture_obs_menu, getString(R.string.show_image), 0, true ));
		menuItems.add(new alexMenuItem(R.id.details_obs_menu, getString(R.string.details), 0, true ));
		menuItems.add(new alexMenuItem(R.id.oaddnote, getString(R.string.add_note), 0, true ));
		menuItems.add(new alexMenuItem(R.id.oseenotes, getString(R.string.see_object_notes), 0, true ));
		menuItems.add(new alexMenuItem(R.id.allnotes_obs_menu, getString(R.string.see_all_notes), 0, true ));
		menuItems.add(new alexMenuItem(R.id.remove, getString(R.string.remove_from_list), 0, true ));
		menuItems.add(new alexMenuItem(R.id.removech, getString(R.string.remove_marked), 0, true ));
		menuItems.add(new alexMenuItem(R.id.removeall, getString(R.string.remove_all), 0, true ));
		menuItems.add(new alexMenuItem(R.id.oedit, getString(R.string.edit), 0, true ));
		//menuItems.add(new alexMenuItem(R.id.oshare, "Share", 0, true ));
		if (contextMenu.isNotShowing()){
			try {
				contextMenu.setMenuItems(menuItems);
			} catch (Exception e) {
				InputDialog.message(ObsList1243.this,MENU_ERROR2 + e.getMessage(), 0).show();
			}	
		}
		getListView().setOnItemLongClickListener(new OnItemLongClickListener(){

			public boolean onItemLongClick(AdapterView<?> arg0, View v,	int index, long arg3) {
				contextMenu.setMenuItemId(index);
				contextMenu.setHeader(((TextView)v.findViewById(R.id.obslistitem_dso)).getText());
				contextMenu.show(v);
				return true;
			}

		});

	}
	
	private void saveListsIfNecessary(){
		if(dirty){//for lists
			
			Runnable r=new Runnable(){
				@Override
				public void run(){
					Set<Integer> set=new HashSet<Integer>();
					set.add(InfoList.PrimaryObsList);
					set.add(InfoList.PrimaryObsList+1);
					set.add(InfoList.PrimaryObsList+2);
					set.add(InfoList.PrimaryObsList+3);
					new Prefs(getApplicationContext()).saveLists(set);
					dirty=false;
				}
			};
			workerHandler.post(r);
			
		}
	}
	
	@Override
	public void onPause(){
		super.onPause();

		///orientationListener.disable();

		/*if(dirty){//for lists
			Set<Integer> set=new HashSet<Integer>();
			set.add(InfoList.PrimaryObsList);
			set.add(InfoList.PrimaryObsList+1);
			set.add(InfoList.PrimaryObsList+2);
			set.add(InfoList.PrimaryObsList+3);
			new Prefs(this).saveLists(set);
			dirty=false;
		}*/
		saveListsIfNecessary();
		
		if(isIntentToGraph()){
        	clearGraphIntentFlag();
        }
        else{
        	if(skyview!=null&&isSkyViewOn()){
        		skyview.saveScreenIntoPrefs();
        		skyview.clearLists();
        	}
        }

		//new Prefs(this).save();
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) { 
		if (ALEX_MENU_FLAG && keyCode==KeyEvent.KEYCODE_MENU) {
			aMenu.show(tv);
			return true; //always eat it!
		}
		else if(keyCode==KeyEvent.KEYCODE_BACK) {
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event); 
	}
	@Override
	protected void onDestroy(){
		try{
			aMenu.hide();
			contextMenu.hide();
		}
		catch(Exception e){}
		super.onDestroy();
		Log.d(TAG,"onDestroy");
		workerThread.getLooper().quit();
		workerThread2.getLooper().quit();
		setStopImportFlag(true);
		if(skyview!=null){
			skyview.setGraphDestroyedFlag();
			skyview.clearLists();
		}
	}
	//
	//Callback from pick import file dialog
	//

	//Gesture Detector (just implement OnGestureListener in the Activity)
	GestureDetector gDetector = new GestureDetector(this);
	@Override
	public boolean onTouchEvent(MotionEvent me) {
		return gDetector.onTouchEvent(me);
	}
	public boolean onFling(MotionEvent start, MotionEvent finish, float xVelocity, float yVelocity) {
		if(start==null || finish==null) return false;
		float dy = start.getRawY() - finish.getRawY();
		float dx = start.getRawX() - finish.getRawX();
		if (dy>Global.flickLength){ //up
			super.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MENU));
			super.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MENU));
			return true;
		}
		else if(dx > Global.flickLength) { //left
			super.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
			super.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
			return true;
		}
		return false;
	}
	public void onLongPress(MotionEvent e) {}
	public void onShowPress(MotionEvent e) {}
	public boolean onDown(MotionEvent e) {return true;}
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {return false;}
	public boolean onSingleTapUp(MotionEvent e) {return false;}
	//-----------

	/**
	 * 
	 * @return a dialog which allows to select all the objects having notes in the specified time period
	 */
	private Dialog getSelectRangeDialog(){
		InputDialog d0 = new InputDialog(ObsList1243.this);
		String[] items=new String[]{
				getString(R.string._with_last_note_age_1_day), //0
				getString(R.string._with_last_note_age_1_week),//1
				getString(R.string._with_last_note_age_1_month),//2
				getString(R.string._with_last_note_age_6_months),//3
				getString(R.string._with_last_note_age_12_months),//4
				getString(R.string._with_any_notes_recorded),//5
				getString(R.string.remove_all_marks),//6
				getString(R.string.show_hide_objects_with_marks)
		};
		d0.setTitle(getString(R.string.mark_objects_));
		d0.setPositiveButton(""); //disable
		d0.setValue("-1"); //remove checks
		d0.setListItems(items, new InputDialog.OnButtonListener() {
			public void onClick(final String value) {
				final int i = AstroTools.getInteger(value, -1,-2,1000);
				if(i==-1) return; //nothing selected
				int days=0;
				boolean all=false;
				switch(i){
				case 0: days=1;		break;
				case 1: days=7;		break;
				case 2: days=30;	break;
				case 3: days=180;	break;
				case 4: days=365;   break;
				case 5: all=true;	break;
				case 6: parseMenu(R.id.deselectall_obs);
				return;
				case 7: parseMenu(R.id.showhide_obs);
				return;
				}
				Calendar c=Calendar.getInstance();
				List<NoteRecord> list;
				Set<String> set;
				NoteDatabase db=new NoteDatabase(ObsList1243.this);
				ErrorHandler eh=new ErrorHandler();
				db.open(eh);
				if(eh.hasError()){
					eh.showError(ObsList1243.this);
					return;
				}
				/*if(all)
					list=db.search(null);
				else{
					long end=c.getTimeInMillis();
					long start=end-days*3600*24*1000L;
					Log.d(TAG,"days="+days+" start="+start+" end="+end);
					list=db.search(start, end);
				}				
				List<AstroObject> objList=db.getObjects(list,eh);
				if(eh.hasError()){
					eh.showError(ObsList1243.this);

				}
				int obsList=Settings1243.getSharedPreferences(ObsList1243.this).getInt(Constants.ACTIVE_OBS_LIST, InfoList.PrimaryObsList);

				InfoList iL=ListHolder.getListHolder().get(obsList);
				Iterator it=iL.iterator();

				for(;it.hasNext();){
					ObsInfoListImpl.Item h=(ObsInfoListImpl.Item)it.next();
					if(objList.contains(h.x))
						h.y=true;
				}*/
				
				if(all)
					set=db.searchNames(0, Long.MAX_VALUE);
				else{
					long end=c.getTimeInMillis();
					long start=end-days*3600*24*1000L;
					set=db.searchNames(start, end);
				}
				int obsList=Settings1243.getSharedPreferences(ObsList1243.this).getInt(Constants.ACTIVE_OBS_LIST, InfoList.PrimaryObsList);

				InfoList iL=ListHolder.getListHolder().get(obsList);
				Iterator it=iL.iterator();

				for(;it.hasNext();){
					ObsInfoListImpl.Item h=(ObsInfoListImpl.Item)it.next();
					String name=h.x.getCanonicName().toUpperCase(Locale.US);
					if(set.contains(name))
						h.y=true;					
				}		
				
				
				updateList();

			}
		});
		return d0;
	}

	class AnotherImpl extends ObsInfoListImpl{
		public AnotherImpl(){
			super("");
		}
		public void add(ObsInfoListImpl.Item item){
			list.add(item);
		}
	}
	/**
	 * 
	 * @param obj - object to be found in Observation List and to be updated if it is changed somewhere else (eg in the databases)
	 */
	public static void updateObsLists(CustomObject obj,Context context){
		for(int listid=InfoList.PrimaryObsList;listid<InfoList.PrimaryObsList+4;listid++){
			InfoList infolist=ListHolder.getListHolder().get(listid);
			Iterator it=infolist.iterator();
			boolean changed=false;
			for(;it.hasNext();){
				ObsInfoListImpl.Item item=(ObsInfoListImpl.Item)it.next();
				AstroObject o=item.x;
				if(o instanceof CustomObject&&o.getCatalog()==obj.getCatalog()&&o.getId()==obj.getId()){
					CustomObject co=(CustomObject)o;
					copyObjectFields(co,obj);
					changed=true;
				}
			}
			if(changed)
				new Prefs(context).saveList(listid);
		}
	}

	/**
	 * 
	 * @param dest - object to which fields are copied
	 * @param src - object from which fields are copied
	 */
	public static void copyObjectFields(CustomObject dest,CustomObject src){
		dest.a=src.a;
		dest.b=src.b;
		dest.catalog=src.catalog;
		dest.comment=src.comment;
		dest.con=src.con;
		dest.dec=src.dec;
		dest.catalog=src.catalog;
		dest.id=src.id;
		dest.mag=src.mag;
		dest.name1=src.name1;
		dest.name2=src.name2;
		dest.pa=src.pa;
		dest.ra=src.ra;
		dest.type=src.type;
		dest.typeStr=src.typeStr;
		if(dest instanceof CustomObjectLarge&&src instanceof CustomObjectLarge){
			CustomObjectLarge d=(CustomObjectLarge)dest;
			CustomObjectLarge s=(CustomObjectLarge)src;
			d.fields=s.fields;
		}
	}
}
