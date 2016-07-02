package com.astro.dsoplanner;

import static com.astro.dsoplanner.Global.ALEX_MENU_FLAG;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
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
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.astro.dsoplanner.alexmenu.alexMenu;
import com.astro.dsoplanner.alexmenu.alexMenu.OnMenuItemSelectedListener;
import com.astro.dsoplanner.alexmenu.alexMenuItem;
//list with observation notes


public class NoteList1243 extends ParentListActivity implements OnGestureListener {
	@ObfuscateOPEN
	private static final String AUDIO = " audio";
	//private static final String ADD_ALL_TO_OBSERVATION_LIST = "Add ALL to Observation List";
	//private static final String REMOVE_ALL = "Remove ALL";
	//private static final String REMOVE = "Remove";
	//private static final String DETAILS = "Details";
	//private static final String SHOW_IMAGE = "Show Image";
	//private static final String PLAY = "Play";
	//private static final String SKY_VIEW = R.string.star_chart;
	private static final String MENU_ERROR = "Menu error!";
	//private static final String SEARCH_TEXT = R.string.search_text;
	//private static final String SHARE_ALL = R.string.share_all;
	//private static final String PASTE = "Paste";
	//private static final String NOTE_TIME = R.string.note_time;
	//private static final String NAME = "Name";
	//private static final String IMPORT = "Import";
	//private static final String EXPORT = "Export";
	//private static final String NOTES2 = R.string._notes;
//	private List<NoteListItem> list;
	
	private static final String BUNDLE = "bundle";
	private static final String NOTES = "Notes";
	@ObfuscateCLOSE
	
	int dbID=-1;
	int listID=-1;
	int ngc=0;
	InputDialog pd=null;
	TextView tv;
	private boolean allNotes=false;//shows all notes or notes for one object
	private static final String TAG="NoteList";@MarkTAG
//	private Map<NoteRec.CompType,Integer> cmpMap=new HashMap();
	private boolean newcomment = false;
	Player mPlayer=null;
	
	private alexMenu aMenu;
    private alexMenu contextMenu;
  //  private boolean nightMode=false;
    private boolean dirtyObsPref=false;
    
    private boolean showPics=false; //whether to show pics. set by updHandler
	
    InfoList noteList=new InfoListImpl(NOTES,NoteRecord.class);
    private HandlerThread workerThread2;
	private Handler workerHandler2;
	
	private Handler handler=new Handler(){
		@Override 
		public void handleMessage(Message msg){
		//	Global.context = NoteList.this;
			if(pd!=null) pd.dismiss();        	
        	updateListArray();
			
		}
	};
	
	class NoteListFiller implements InfoListFiller{
		private AstroCatalog catalog;
		List<NoteRecord> list=new ArrayList<NoteRecord>();
		
		/**
		 * search for notes corresponding to the object
		 * @param obj
		 */
		public NoteListFiller(AstroObject obj){
			NoteDatabase db=new NoteDatabase();
			ErrorHandler eh=new ErrorHandler();
			db.open(eh);
			if(eh.hasError()){
				eh.showError(NoteList1243.this);
				return;
			}
				
			list=db.search(obj);
		
			db.close();
			
		}
		/**
		 * search for notes by name
		 * @param obj 
		 * @param flag not used as such, needed just to call this constructor
		 */
	/*	public NoteListFiller(AstroObject obj, boolean flag){
			if(obj==null)return;
			NoteDatabase db=new NoteDatabase();
			ErrorHandler eh=new ErrorHandler();
			db.open(eh);
			if(eh.hasError()){
				eh.showError(NoteList1243.this);
				return;
			}
			list=db.searchNameExact(obj.getNoteNames());
			db.close();
		}*/
		
		
		public NoteListFiller(String searchString){
			NoteDatabase db=new NoteDatabase();
			ErrorHandler eh=new ErrorHandler();
			db.open(eh);
			if(eh.hasError()){
				eh.showError(NoteList1243.this);
				return;
			}
			list=db.searchContentInclusive(searchString);
			db.close();
		}
		public Iterator getIterator(){
			return list.iterator();
		}




	}
	private NLadapter mAdapter;
	Map<NoteRecord,AstroObject>map=new HashMap<NoteRecord,AstroObject>();//keeping refs to astro objects
	
	private class NLadapter extends BaseAdapter{
		
		private LayoutInflater mInflater;
		private static final int NON_BOLD=1;
		private static final int BOLD=2;
		//private InfoList list=ListHolder.getListHolder().get(InfoList.NOTE_LIST);
		
		 public NLadapter() {
	            mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            
	        }
		 
		public View getView(int position, View convertView,ViewGroup parent){
			
			boolean remake=false;//remake convert view if its structure is different from the one indicated by shows pics
			
			if(convertView!=null){
				ImageView iv = (ImageView) convertView.findViewById(R.id.note_image);
				if(showPics){
					remake=(iv==null);
				}
				else{
					remake=(iv!=null);
				}				
			}			
			Log.d(TAG,"convertView="+convertView+" remake="+remake+" show="+showPics);
			if (convertView == null||remake) {
				convertView = mInflater.inflate(showPics?R.layout.notelist_item:R.layout.notelist_item_no_pic, null);				
				
			} 
		//	Log.d(TAG,"showPics="+showPics);
			
			
			
		/*	if (convertView == null) {
				convertView = mInflater.inflate(R.layout.notelist_item, null);
			}   
			
			NoteRecord rec=(NoteRecord)noteList.get(position);
			AstroObject obj=null;
			if(map.containsKey(rec)){
				obj=map.get(rec);
				
			}
			else{
				NoteDatabase db=new NoteDatabase(NoteList.this);
				ErrorHandler eh=new ErrorHandler();
				db.open(eh);
				if(!eh.hasError()){
					obj=db.getObject(rec);
					db.close();
					map.put(rec, obj);
				}
			}*/
			NoteRecord rec=(NoteRecord)noteList.get(position);
			
			if(showPics){
				AstroObject obj=null;
				if(map.containsKey(rec)){
					obj=map.get(rec);

					if(obj!=null){
						ObsList1243.setImage(getApplicationContext(),convertView, obj, handler, R.id.note_image,workerHandler2,true);
					}
					
				}
				if(obj==null){
					ImageView iv=(ImageView)convertView.findViewById(R.id.note_image);
					iv.setTag(rec);//every convert view should have a tag!!!otherwise
					//it may get a convert view with an object tag which it does not override!
					iv.setImageBitmap(null);
					iv.setVisibility(View.GONE);
				}
					
				//Log.d(TAG,"nr="+rec+" obj="+obj);
			}
			
			//Log.d(TAG,"inside adapter "+"pos="+position+"date="+rec.date);
			Calendar c=Calendar.getInstance();
			c.setTimeInMillis(rec.date);
			String s=Details1243.makeDateString(c, true)+" "+Details1243.makeTimeString(c,false);
			if(!rec.path.equals("")) s=s+AUDIO;
			((TextView) convertView.findViewById(R.id.notelist_datetime)).setText(s);
			
			((TextView) convertView.findViewById(R.id.notelist_dso)).setText(""+rec.name);
			((TextView) convertView.findViewById(R.id.notelist_note)).setText(""+rec.note);

			//make dark background
			if(Settings1243.getDarkSkin()||Settings1243.getNightMode())
				convertView.setBackgroundColor(0xff000000);
			
			return convertView;
		}
		public int getCount(){
			Log.d(TAG,"getCount="+noteList.getCount());
			return noteList.getCount();
		}
		public Object getItem(int position){
			return noteList.get(position);
		}
		public long getItemId(int position){
			return position;
		}
	}
	
	/**
	 * prepares and sets astro images in asynchronous way 
	 * every convert view should have a tag even if it does not have a picture!!!
	 * otherwise
	 * @param view - view to take image view from
	 * @param obj - object for which make picture
	 * @param handler - handler from ui thread
	 * @param id - image view resource id
	 */
	public static void setImage(Context context,final View view,final AstroObject obj,final Handler handler, int id){
		final List<String>list=Details1243.getPicturePaths(context,obj);
		//Log.d(TAG,"setimage start for "+obj);
	//	Log.d(TAG,"obj="+obj+"pics="+list);
		final ImageView iv = (ImageView) view.findViewById(id);
		iv.setTag(obj);//needed to check later if this view belongs to somebody else
		if(list.size()==0){
			iv.setImageBitmap(null);
			return;
		}
		Runnable r=new Runnable(){
			public void run(){
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
							//TextView tv=(TextView)view.findViewById(R.id.obslistitem_dso);
							//Log.d(TAG,"setting image to "+tv.getText()+" for "+obj);
						}
					});
					
				}
				return;
			}
		};
		//r.run();
		new Thread(r).start();
	}
	
	
	private static final int SHOW_PIC=1;
	private static final int DO_NOT_SHOW_PIC=2;
	Handler updHandler=new Handler(){
		@Override
		public void handleMessage(Message msg){
		/*	map=(Map<NoteRecord,AstroObject>)msg.obj;
			showPics=false;
			for(Map.Entry<NoteRecord,AstroObject>e:map.entrySet()){
				List<String>list=Details1243.getPicturePaths(getApplicationContext(),e.getValue());
				if(list.size()>0)
					showPics=true;
			}*/
			switch(msg.arg1){
			case SHOW_PIC:
				showPics=true;
				break;
			case DO_NOT_SHOW_PIC:
				showPics=false;
				break;
			}
			mAdapter.notifyDataSetChanged();
		}
	};
	/**
	 * updates map<NoteRecord,AstroObject> where NoteRecord corresponds to AstroObject
	 * this map is needed to draw pictures quickly
	 * and to determine if there are pics at all
	 * the map is built in a non ui thread as it is a time consuming process not to be done in UI
	 * especially for long note lists 
	 * @author leonid
	 *
	 */
	class UpdatingThread extends Thread{
		//Map<NoteRecord,AstroObject>map=new HashMap<NoteRecord,AstroObject>();
		List<NoteRecord>list=new ArrayList<NoteRecord>();
		Handler handler;
		public UpdatingThread(Handler handler){
			this.handler=handler;
			for(Object o:noteList){
				list.add((NoteRecord)o);
			}			
		}
		public void run(){
			NoteDatabase db=new NoteDatabase(NoteList1243.this);
			ErrorHandler eh=new ErrorHandler();
			db.open(eh);
			if(eh.hasError())
				return;
			//ErrorHandler eh=new ErrorHandler();
			for(NoteRecord rec:list){
				AstroObject obj=db.getObject(rec,eh);
				if(obj!=null)
					map.put(rec, obj);
			}
			
			boolean showpic=false;
			
			if(areImagesOn()){
				for(Map.Entry<NoteRecord,AstroObject>e:map.entrySet()){					
					List<String>list=Details1243.getPicturePaths(getApplicationContext(),e.getValue());
					Log.d(TAG,"upd thread, obj="+e.getValue()+" size="+list.size());
					if(list.size()>0)
						showpic=true;
				}
			}
			
				
			
			Message msg=new Message();
			if(showpic)
				msg.arg1=SHOW_PIC;
			else
				msg.arg1=DO_NOT_SHOW_PIC;
			
			msg.obj=map;
			handler.sendMessage(msg);
		}
		
	}
	
	//private static final File fnote=new File(Environment.getExternalStorageDirectory(),"notesDSO.txt");
	
	
	NoteRequest request;
	private Button bN;
	@Override
	public void onPause(){
		super.onPause();
		if(mPlayer!=null) mPlayer.release();
		mPlayer=null;
		if(dirtyObsPref){
			int obsList=Settings1243.getSharedPreferences(this).getInt(Constants.ACTIVE_OBS_LIST, InfoList.PrimaryObsList);

			new Prefs(this).saveList(obsList);
			dirtyObsPref=false;
		}
	}
	@Override
	public void onResume(){
		//Global.context = this;
		Log.d(TAG,"request="+request);
		onResumeCode();
		super.onResume();
	/*	if(initRequired){
			initRequired=false;
			return;
		}*/
		
	}
	
	private void onResumeCode(){
		initList();
	}
	//override the system search request in night mode only
	@Override
	public boolean onSearchRequested() {
		return AstroTools.invokeSearchActivity(this);
	}
	
	int answer=NOT_DEFINED;
	final static int NOT_DEFINED=0;
	final static int  SHOW_NOTES_WITH_NAMES=1;
	final static int  DONOT_SHOW_NOTES_WITH_NAMES=2;
	List<NoteRecord>list;//=new ArrayList<NoteRecord>();
	List<NoteRecord>list2;//=
	
	private void initList(){
		list=new ArrayList<NoteRecord>();
		list2=new ArrayList<NoteRecord>();
		noteList.removeAll();
		
		NoteDatabase db=new NoteDatabase();
		ErrorHandler eh=new ErrorHandler();
		db.open(eh);
		if(eh.hasError()){
			eh.showError(NoteList1243.this);
			return;
		}
		
		if(request.action==NoteRequest.SEARCH_NOTES){
			list=db.searchContentInclusive(request.record.name);
			noteList.fill(new InfoListCollectionFiller(list));
			updateList();
		}
		else{
			list=db.search(request.obj);//null to see all notes
			noteList.fill(new InfoListCollectionFiller(list));
			updateList();
			
			/*if(request.obj!=null)
				list2=db.searchNameExact(request.obj.getNoteNames());
			list2.removeAll(list);
			
			if(list.size()==0&&list2.size()!=0){
				Runnable r=new Runnable(){
					public void run(){
						handler.post(new Runnable() { //from ui thread
							
							@Override
							public void run() {
								answer=SHOW_NOTES_WITH_NAMES;
								noteList.fill(new InfoListCollectionFiller(list2));
								list2=null;
								updateList();
								
							}
						});
						
						
						
					}
				};
				if(answer==NOT_DEFINED){
					answer=DONOT_SHOW_NOTES_WITH_NAMES;
					InputDialog d=AstroTools.getDialog(this, getString(R.string.no_notes_corresponding_to_the_object_were_found_do_you_wish_to_show_notes_for_the_objects_with_the_same_name_instead_), r);
					registerDialog(d);
					d.show();
				}
				else if(answer==SHOW_NOTES_WITH_NAMES){
					r.run();
				}
			}
			else if(list.size()!=0&&list2.size()!=0){
				Runnable r2=new Runnable(){
					public void run(){
						answer=SHOW_NOTES_WITH_NAMES;
						handler.post(new Runnable() { //from ui thread
							
							@Override
							public void run() {
								answer=SHOW_NOTES_WITH_NAMES;
								noteList.fill(new InfoListCollectionFiller(list2));
								list2=null;
								updateList();
								
							}
						});
						
					}
				};
				if(answer==NOT_DEFINED){
					answer=DONOT_SHOW_NOTES_WITH_NAMES;
					InputDialog d=AstroTools.getDialog(this, getString(R.string.do_you_wish_to_show_notes_for_the_objects_with_the_same_name_as_well_),r2 );
					registerDialog(d);
					d.show();
				}
				else if(answer==SHOW_NOTES_WITH_NAMES){
					r2.run();
				}
				
			}*/
			
		}
		db.close();
		
	}
	
	
	/*private void initList2(){
		//InfoList list=ListHolder.getListHolder().get(InfoList.NOTE_LIST);
		noteList.removeAll();
		//Log.d(TAG,"obj="+request.obj);
		final InfoList noteList2=new InfoListImpl(NOTES,NoteRecord.class);
		if(request.action==NoteRequest.SEARCH_NOTES){
			noteList.fill(new NoteListFiller(request.record.name));
		}
		//else if (request.action==NoteRequest.GET_OBJECT_NOTES_BY_NAME){
		//	noteList.fill(new NoteListFiller(request.obj,true));
		//}
		else{
			noteList.fill(new NoteListFiller(request.obj));
			//if(noteList.getCount()==0){//no corresponding notes found
				
				noteList2.fill(new NoteListFiller(request.obj,true));//search by name
			//}
		}
		updateList();
	
		Runnable switchlist=new Runnable(){
			public void run(){
				noteList=noteList2;
				updateList();
			}
		};
		Runnable switchlist2=new Runnable(){
			public void run(){
				InfoListFiller filler=new InfoListIteratorFiller(noteList2.iterator());
				noteList.fill(filler);
				updateList();
			}
		};
		if(noteList.getCount()==0&&noteList2.getCount()!=0){
			InputDialog d=AstroTools.getDialog(this, "No notes corresponding to the object were found. Do you wish to show notes for the objects with the same name instead?", switchlist);
			registerDialog(d);
			d.show();
		}
		else if(noteList.getCount()!=0&&noteList2.getCount()!=0){
			InputDialog d=AstroTools.getDialog(this, "Do you wish to show notes for the objects with the same name as well?", switchlist2);
			registerDialog(d);
			d.show();
		}
	
	}*/
	private void updateList(){
		switch(currentSort){
		case SORT_TIME:
				noteList.sort(cmp);
				break;
		case SORT_NAME:
				noteList.sort(cmpS);
				break;
		}
		mAdapter.notifyDataSetChanged();
		tv.setText(""+noteList.getCount()+getString(R.string._notes));
		
		for(Object o:noteList){
			Log.d(TAG,"o="+o);
		}
		
		new UpdatingThread(updHandler).start();
	}
	@Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
    	savedInstanceState.putBoolean("destroyed", true);
    }
	Handler initHandler=new Handler(){
		@Override
		public void handleMessage(Message msg){
			Bundle b=getIntent().getBundleExtra(BUNDLE);
			if(b==null){
				finish();
				return;
			}
			request=new NoteRequest(b);


			if(request.action==NoteRequest.GET_ALL_NOTES)
				setContentView(R.layout.notelist2); 
			else
				setContentView(R.layout.notelist);
			
			if(ImportDatabaseIntentService.isBeingImported(Constants.NOTE_DATABASE_NAME)){
				InputDialog.toast(getString(R.string.import_for_this_database_is_running_), NoteList1243.this).show();
				finish();
				return;
			}
			workerThread2 = new HandlerThread("");
			workerThread2.start();
			workerHandler2 = new Handler(workerThread2.getLooper());
			
			tv=(TextView)findViewById(R.id.text_notelist);
			mAdapter=new NLadapter();	    	
			setListAdapter(mAdapter);
			
			if (ALEX_MENU_FLAG) {
	        	initAlexMenu();
	        	initAlexContextMenu();
	        }
	        else
	        	registerForContextMenu(getListView()); //old style

			// tv.setText(Global.noteList.size()+" notes");

			//SAND add note button

			final Activity a = NoteList1243.this;
			OnClickListener oclNote = new OnClickListener() {
				public void onClick(View v){
					if(request.obj==null)
						return;
					Command command=new NewNoteCommand(NoteList1243.this, request.obj, Calendar.getInstance(), "");
					command.execute();
				}
			};
			
			bN = (Button)findViewById(R.id.bNoteListNewNote); //SAND: need one global view (was under if before
			if(request.action==NoteRequest.GET_OBJECT_NOTES){
				//adding new note for the object on button click
				bN.setOnClickListener(oclNote);
			}
			//onResumeCode();
		}
	};
//	boolean initRequired=false;//global init
    @Override
	public void onCreate(Bundle savedInstanceState) {
    //	Global.context=getApplicationContext();
    //	Global.appContext=getApplicationContext();
    /*	if(savedInstanceState!=null){
    		if(savedInstanceState.getBoolean("destroyed", false)){
    			if (Init.initRequired())
					initRequired=true;
    		}
    	}*/
    /*	if(Init.initRequired()){
			super.onCreate(savedInstanceState); 
			finish();
			Intent intent=new Intent(this,DSOmain.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return;
		}*/
   /* 	if (Init.initRequired())
			initRequired=true;
		Settings.setDayNightList(this);
		nightMode=Settings.getNightMode();*/
		
		super.onCreate(savedInstanceState);
		/*	cmpMap.put(NoteRec.CompType.NGC, 1);
    	cmpMap.put(NoteRec.CompType.Time, -1);

    	int order=cmpMap.get(NoteRec.CompType.Time);
		cmpMap.put(NoteRec.CompType.Time, -order);
		Collections.sort(Global.noteList,NoteRec.getComparator(NoteRec.CompType.Time, order));

		 */
		/*if(initRequired){
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
    /* Never Used. Use AstroTools one or InputDialog if necessary.
	private InputDialog getDialog(String title,final String pdTitle,final Runnable r){
		InputDialog dimp=new InputDialog(NoteList.this);                
		dimp.setMessage(title);
		dimp.setPositiveButton("OK", new InputDialog.OnButtonListener() {
			public void onClick(String v) {
			    pd=InputDialog.pleaseWait(NoteList.this, "Please wait...").pop();
				//pd = ProgressDialog.show(NoteList.this, "Please Wait", pdTitle, true,false);
				Thread t=new Thread(r);					
				t.start();
			}
		});
		dimp.setNegativeButton("Cancel");
		dimp.create();
		return dimp;
		
	}
	*/
	@Override
	 public boolean onCreateOptionsMenu(Menu menu) {
	    	super.onCreateOptionsMenu(menu);
	    	MenuInflater inflater = getMenuInflater();
	    	inflater.inflate(R.menu.notelist_menu, menu);
	    	
	    	return true;
	    }
	
	public boolean onOptionsItemSelected(MenuItem item) {
		return parseMenu(item.getItemId());
	}
	
	//SORT
	int orderTime=1;//sort order by time
	int orderName=1;//sort order by name
	
	private static final int SORT_TIME=2;
	private static final int SORT_NAME=3;	
	int currentSort=SORT_TIME;
	//sort by time
	Comparator cmp=new Comparator(){
		public int compare(Object lhs,Object rhs){
			if(lhs instanceof NoteRecord&&rhs instanceof NoteRecord){
				NoteRecord lhsRec=(NoteRecord)lhs;
				NoteRecord rhsRec=(NoteRecord)rhs;
				if(lhsRec.date==rhsRec.date) return 0;
				if(lhsRec.date<rhsRec.date) return -1*orderTime;
				return orderTime;
			}
			throw new ClassCastException();
		}
	};
	//sort by name
	Comparator cmpS=new Comparator(){
		public int compare(Object lhs,Object rhs){
			if(lhs instanceof NoteRecord&&rhs instanceof NoteRecord){
				NoteRecord lhsRec=(NoteRecord)lhs;
				NoteRecord rhsRec=(NoteRecord)rhs;
				return lhsRec.name.compareTo(rhsRec.name)*orderName;
			}
			throw new ClassCastException();
		}
	};
	
	
	private boolean areImagesOn(){
		return Settings1243.getSharedPreferences(getApplicationContext()).getBoolean(Constants.SHOW_NOTELIST_IMAGES, true);
	}
	private void setShowImagesFlag(boolean flag){
		Settings1243.putSharedPreferences(Constants.SHOW_NOTELIST_IMAGES, flag, getApplicationContext());
	}
	
	
	public boolean parseMenu(int id){
		int order;
		//InfoList iL=ListHolder.getListHolder().get(InfoList.NOTE_LIST);
    	switch (id) {
    	case R.id.images_notelist_menu:
    		setShowImagesFlag(!areImagesOn());
			updateList();
			return true;
    	case R.id.export_notelist_menu:
    		final Runnable r=new Runnable(){
    			public void run(){
					boolean noError=true;
    				try{
    					if(!AstroTools.isExternalStorageAvailable(AstroTools.EXT_STORAGE_WRITABLE)){
							AstroTools.showExtStorageNotAvailableMessage(AstroTools.EXT_STORAGE_WRITABLE, NoteList1243.this);
							return;
						}
    					
    					InfoListSaver saver=new InfoListStringSaverImp(
								new FileOutputStream(Global.exportImportPath + InputDialog.getResult()));
					//	InfoList iL=ListHolder.getListHolder().get(InfoList.NOTE_LIST);
						noError=noteList.save(saver);
					}
					catch(Throwable e){
						Log.d(TAG,"Exception="+e);
						noError=false;
					}
					//handler.sendEmptyMessage(0);
					String message=(!noError?getString(R.string.export_error_):getString(R.string.export_successfull_));
					registerDialog(InputDialog.message(NoteList1243.this,message)).show();
				}
			};
    		
			InputDialog d = new InputDialog(NoteList1243.this);
			d.setType(InputDialog.DType.INPUT_STRING);
			d.setTitle(getString(R.string.export_to_the_file));
			d.setMessage(getString(R.string.please_enter_the_file_name_for_exporting_warning_the_file_with_the_same_name_will_be_silently_overwritten_in_the_) + Global.exportImportPath + getString(R.string._folder_));
			d.setPositiveButton(getString(R.string.ok), new InputDialog.OnButtonListener() {
				public void onClick(String value) {
					r.run();
				}
			});
			registerDialog(d).show();
			
    		/*Dialog dimp=getDialog("Do you want to export notes to "+fnote.getAbsolutePath()+"?","Exporting ....",r);
    		dimp.show();
    		
    		GetStringDialog d = new GetStringDialog();
			d.show(this,	"Export to the file", 
					"Please enter the file name for exporting.\n\n" + 
					"WARNING: the file with the same name will be silently overwritten " + 
					"in the " + Global.path + " folder.", r);
    		
    		//Other implementation
    		Dialog dimpe=new AlertDialog.Builder(NoteList.this)                
    		.setTitle("Do you want to export notes to "+fnote.getAbsolutePath())
    		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int whichButton) {
                    pd=InputDialog.pleaseWait(NoteList.this, "Please wait...").pop();
    				//pd = ProgressDialog.show(NoteList.this, "Please Wait", "Exporting ....", true,false);
    				Thread t=new Thread(new Runnable(){
    					public void run(){
    						try{
    							InfoListSaver saver=new InfoListStringSaverImp(new FileOutputStream(fnote));
    							InfoList iL=ListHolder.getListHolder().get(InfoList.NOTE_LIST);
    							iL.save(saver);
    						}
    						catch(IOException e){
    							Log.d(TAG,"Exception="+e);
    						}
    						handler.sendEmptyMessage(0);
    					}
    				});
    				t.start();
    				

    			}
    		})
    		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int whichButton) {

    				
    			}
    		})
    		.create();*/
			
			
    	//	FileDialog f=new FileDialog(this,new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"//dso//"));
    	//	f.create().show();
    	//	Log.d(TAG,"chosen file="+f.getChosenFile());
    	/*	try{
				InfoListSaver saver=new InfoListStringSaverImp(new FileOutputStream(fnote));
				iL.save(saver);
			}
			catch(IOException e){
				Log.d(TAG,"Exception="+e);
			}*/
    		return true; 
    	case R.id.paste_notelist_menu:
    	/*	Runnable r1=new Runnable(){
				public void run(){
					NoteList.this.initList();
				}
			};
			Command command=new PasteCommand(noteList,-1,r1,this,
					"Do you want to paste into current note list?");
			command.execute();*/
    		startPasting();
			return true;
    	case R.id.import_notelist_menu:
    		/*Runnable r1=new Runnable(){
    			public void run(){
					try{
						InfoListLoader loader=new InfoListStringLoaderImp(new FileInputStream(fnote));
						ListHolder.getListHolder().get(InfoList.NOTE_LIST).load(loader);
					}
					catch(IOException e){
						Log.d(TAG,"Exception="+e);
					}
					handler.sendEmptyMessage(0);
				}
			};*/
    		
 /*   		Dialog dimp1=getDialog("Do you want to import notes from "+fnote.getAbsolutePath()+"?","Loading ....",r1);
    		dimp1.show();*/
    /*		class DialogRunnable implements Runnable{
    			boolean external;
    			File file;
    			public DialogRunnable(File file,boolean external){
    				this.external=external;
    				this.file=file;
    			}
    			public void run(){
    				
    				try{
        				//	Global.context = NoteList.this;
    						InfoListStringLoaderImp loader=new InfoListStringLoaderImp(new FileInputStream(file));
    						if(external)
    							loader.setExternalFlag();
    						ErrorHandler eh=noteList.load(loader);
    						if(eh.hasError())
    							eh.showError(NoteList.this);
    					}
    					catch(IOException e){
    						Log.d(TAG,"Exception="+e);
    					}
    					handler.sendEmptyMessage(0);
    			}
    		}*/
    		
    		
    		
    		
    		IPickFileCallback listener=new IPickFileCallback(){
    			public void callbackCall(final File file){
    				InputDialog d = new InputDialog(NoteList1243.this);
    				d.setMessage(getResources().getString(R.string.did_you_export));
    				d.setPositiveButton(getString(R.string.yes), new InputDialog.OnButtonListener() {
    					
    					@Override
    					public void onClick(String value) {
    						startImporting(file.getAbsolutePath(),false);//do not ignore custom db refs
    						
    					}
    				});
    				d.setNegativeButton(getString(R.string.no),new InputDialog.OnButtonListener() {
    					
    					@Override
    					public void onClick(String value) {
    						startImporting(file.getAbsolutePath(),true);//ignore custom db refs
    						
    					}
    				});
    				registerDialog(d).show();
    				
    			}
    		};
    		if(!AstroTools.isExternalStorageAvailable(AstroTools.EXT_STORAGE_READABLE)){
				AstroTools.showExtStorageNotAvailableMessage(AstroTools.EXT_STORAGE_READABLE, this);
				return true;
			}
    		
    		/*FileDialogActivity1243.setPath(Settings1243.getFileDialogPath(getApplicationContext()));
			FileDialogActivity1243.setListener(listener);
			Intent fileDialog = new Intent(this, FileDialogActivity1243.class);
			startActivity(fileDialog);*/
    		
			SelectFileActivity1243.setPath(Settings1243.getFileDialogPath(getApplicationContext()));
			SelectFileActivity1243.setListener(listener);
			Intent fileDialog = new Intent(this, SelectFileActivity1243.class);
			startActivity(fileDialog);
    		
    		
			return true;
    	case R.id.share_notelist_menu:
    		if(Settings1243.nightGuard(this)) return true;
    		
		//	InfoList list=ListHolder.getListHolder().get(InfoList.NOTE_LIST);
			ByteArrayOutputStream out=new ByteArrayOutputStream();				
			InfoListSaver saver=new InfoListStringSaverImp(out,Global.SHARE_LINES_LIMIT,new Handler());
			noteList.save(saver);
			String s=out.toString();
			//Log.d(TAG,"s length="+s.length());
			new ShareCommand(this,s).execute();
			return true;
    	case R.id.time_notelist_menu:
    		orderTime=-orderTime;
    		noteList.sort(cmp);
    		
    		mAdapter.notifyDataSetChanged();
    		return true;
    	case R.id.ngc_notelist_menu:
    		orderName=-orderName;
    		noteList.sort(cmpS);
    		
    		mAdapter.notifyDataSetChanged();
    		return true;
    	case R.id.search_notelist_menu:
    		InputDialog d1 = new InputDialog(NoteList1243.this);
			d1.setType(InputDialog.DType.INPUT_STRING);
			d1.setTitle(getString(R.string.search_note_database_for_the_string_));
			d1.setValue("");
			
			d1.setPositiveButton(getString(R.string.ok), new InputDialog.OnButtonListener() {
				public void onClick(String value) {
					if(!value.equals(""))
						new SearchNotes(NoteList1243.this,value).execute();
				}
			});
			registerDialog(d1).show();
			return true;
    		
   /* 	case R.id.ngc_notelist_menu:
    		order=cmpMap.get(NoteRec.CompType.NGC);
    		cmpMap.put(NoteRec.CompType.NGC, -order);
    		Collections.sort(Global.noteList,NoteRec.getComparator(NoteRec.CompType.NGC, order));
    		mAdapter=new NLadapter(Global.noteList);//need to start from the top
        	setListAdapter(mAdapter);
        	registerForContextMenu(getListView());
    		updateListArray();
    		return true;
    	case R.id.time_notelist_menu: 	
    		order=cmpMap.get(NoteRec.CompType.Time);
    		cmpMap.put(NoteRec.CompType.Time, -order);
    		Collections.sort(Global.noteList,NoteRec.getComparator(NoteRec.CompType.Time, order));
    		mAdapter=new NLadapter(Global.noteList);//need to start from the top
        	setListAdapter(mAdapter);
        	registerForContextMenu(getListView());
    		updateListArray();
    		return true;*/
    	}
    	return false;
    }   
	@Override
	public void onCreateContextMenu(ContextMenu menu,View v,ContextMenuInfo menuInfo){
	//	Log.d(TAG,"onCreateContextMenu"); 
		super.onCreateContextMenu(menu, v, menuInfo);
		
		MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.notelist_context_menu, menu);
	}
	
	public boolean onContextItemSelected(MenuItem item){
		AdapterContextMenuInfo info=(AdapterContextMenuInfo)item.getMenuInfo();
		return parseContextMenu(item.getItemId(), (int)info.id) ? true : super.onContextItemSelected(item);
	}
	
	private boolean parseContextMenu(int itemId, int id) {
		final NoteRecord rec=(NoteRecord)noteList.get(id);
		Log.d(TAG,"rec="+rec);
		NoteRequest request;
		Command command;
		ErrorHandler eh=new ErrorHandler();
		switch (itemId){
		case R.id.notelist_play:
			Log.d(TAG,"playing, rec="+rec);
			boolean playing=false;
			if(!rec.path.equals("")){//there is some recording path				
				final File f=new File(Global.notesPath,rec.path);
				if(f.exists()){
					mPlayer=new Player(f.getAbsolutePath());
					mPlayer.startPlaying();	
					playing=true;
				}
			}
			if(!playing)
				registerDialog(InputDialog.message(NoteList1243.this,R.string.there_is_no_audio_for_this_note,0)).show();
			return true;
		case R.id.picture_notelist_menu:
			command=new PictureCommand(this,rec);
			command.execute();
			return true;
		case R.id.detail_notelist_menu:
			AstroObject obj=new NoteDatabase().getObject(rec,eh);
			Log.d(TAG,"obj="+obj+" rec="+rec);
			if(eh.hasError())
				eh.showError(this);
			if (obj==null) return true;
			new DetailsCommand(obj,this).execute();
			return true;
		case R.id.notelist_graph:			
			final AstroObject obj1=new NoteDatabase().getObject(rec,eh);
			if(eh.hasError())
				eh.showError(this);
			if(obj1==null) {
				registerDialog(InputDialog.message(this, R.string.this_action_could_not_be_performed_for_temporary_object_note, 0)).show();
				return true;
			}
			goToStarChart(obj1, rec);			
			
			
			
			//SAND: Use last, Not Now! Global.cal=Calendar.getInstance();
	    	//Point.setLST(AstroTools.sdTime(AstroTools.getDefaultTime(this)));//need to calculate Alt and Az
	    				return true;
		case R.id.remove_notelist_menu:
			class DialogImplRemove implements InputDialog.OnButtonListener{
				private NoteRecord nr;//position in NoteDatabase
				private int listID;//position in NoteList
				public DialogImplRemove(NoteRecord nr,int listID){
					this.nr=nr;
					this.listID=listID;
				}
				public void onClick(String v) {
                	
					noteList.remove(listID);
					NoteDatabase db=new NoteDatabase();
					ErrorHandler eh=new ErrorHandler();
					db.open(eh);
					if(eh.hasError()){
						eh.showError(NoteList1243.this);
						return;
					}
                	db.remove(nr);
                	db.close();
					updateListArray();
                }
			}
			
			
			int listID = id;
			
			InputDialog dl=new InputDialog(NoteList1243.this); 
			dl.setTitle(getString(R.string.note_list_confirmation));
            dl.setMessage(getString(R.string.do_you_want_to_remove_the_note_));
            dl.setPositiveButton(getString(R.string.ok), new DialogImplRemove(rec,listID));
            dl.setNegativeButton(getString(R.string.cancel));
            registerDialog(dl).show();
			
			
			return true;
		case R.id.removeall_notelist_menu:
			class DialogImplRemoveAll implements InputDialog.OnButtonListener{
				private Set<NoteRecord> nrSet;
				private Set<Integer> listIDSet;//position in NoteList
				
				public void onClick(String v) {
                	//InfoList list=ListHolder.getListHolder().get(InfoList.NOTE_LIST);
                	Iterator it=noteList.iterator();					
					NoteDatabase db=new NoteDatabase();
					ErrorHandler eh=new ErrorHandler();
					db.open(eh);
					if(eh.hasError()){
						eh.showError(NoteList1243.this);
						return;
					}
					for(;it.hasNext();){
						NoteRecord nr=(NoteRecord)it.next();
						db.remove(nr);
					}
                	db.close();
                	noteList.removeAll();
					updateListArray();
                }
			}
			
			
			InputDialog dl2=new InputDialog(NoteList1243.this);
			dl2.setTitle(getString(R.string.note_list_confirmation));
            dl2.setMessage(getString(R.string.do_you_want_to_remove_all_of_the_notes_));
            dl2.setPositiveButton(getString(R.string.ok),new DialogImplRemoveAll() );
            dl2.setNegativeButton(getString(R.string.cancel));
            registerDialog(dl2).show();
			
			return true;
		case R.id.addall_notelist_menu:
			NoteDatabase db=new NoteDatabase();
			//ErrorHandler eh=new ErrorHandler();
			db.open(eh);
			if(eh.hasError()){
				eh.showError(NoteList1243.this);
				return true;
			}
			List<NoteRecord>list1=new ArrayList<NoteRecord>();
			Iterator it=noteList.iterator();
			for(;it.hasNext();){
				list1.add((NoteRecord)it.next());
			}
			List<AstroObject> list2=db.getObjects(list1,eh);
			if(eh.hasError()){
				eh.showError(NoteList1243.this);
				
			}
			InfoListFiller filler=new ObsListFiller(list2);
			int obsList=Settings1243.getSharedPreferences(this).getInt(Constants.ACTIVE_OBS_LIST, InfoList.PrimaryObsList);
			InfoList iL=ListHolder.getListHolder().get(obsList);
			iL.fill(filler);
			dirtyObsPref=true;
		default: return false;
		}
		
	}
	
	private void goToStarChart(final AstroObject obj1,final NoteRecord rec){
		InputDialog dimp=new InputDialog(NoteList1243.this);              
		dimp.setMessage(getString(R.string.would_you_like_to_set_the_time_and_location_as_recorded_with_this_note_));
		dimp.setPositiveButton(getString(R.string.yes), new InputDialog.OnButtonListener() {
			public void onClick(String v) {
				Settings1243.putSharedPreferences(Constants.GRAPH_OBJECT, obj1, getApplicationContext());
		    	int zoom=Settings1243.getSharedPreferences(getApplicationContext()).getInt(Constants.CURRENT_ZOOM_LEVEL, Constants.DEFAULT_ZOOM_LEVEL);
		    	//Global.graphCreate=
		    	long millis=rec.date;
		    	Calendar calendar=Calendar.getInstance();
		    	calendar.setTimeInMillis(millis);
		    	Point.setLST(AstroTools.sdTime(calendar));		
		    	//new GraphRec(zoom,obj1.getAz(),obj1.getAlt(),AstroTools.getDefaultTime(this),obj1,0,1).save(this);//add graph settings for Graph Activity to process it		
		    	new GraphRec(zoom,obj1.getAz(),obj1.getAlt(),calendar,obj1,0,1).save(getApplicationContext());//add graph settings for Graph Activity to process it		

		    	Intent i = new Intent(NoteList1243.this, Graph1243.class);
		    	i.putExtra(Constants.GRAPH_CALLING, Constants.NOTE_LIST_GRAPH_CALLING);
				startActivity(i); 
			}
		});
		dimp.setNegativeButton(getString(R.string.no), new InputDialog.OnButtonListener() {
			public void onClick(String v) {
				Settings1243.putSharedPreferences(Constants.GRAPH_OBJECT, obj1, getApplicationContext());
		    	int zoom=Settings1243.getSharedPreferences(getApplicationContext()).getInt(Constants.CURRENT_ZOOM_LEVEL, Constants.DEFAULT_ZOOM_LEVEL);
		    	//Global.graphCreate=
		    	
		    	Point.setLST(AstroTools.sdTime(AstroTools.getDefaultTime(getApplicationContext())));		
		    	new GraphRec(zoom,obj1.getAz(),obj1.getAlt(),AstroTools.getDefaultTime(getApplicationContext()),obj1,0,1).save(getApplicationContext());//add graph settings for Graph Activity to process it		
		    	//new GraphRec(zoom,obj1.getAz(),obj1.getAlt(),calendar,obj1,0,1).save(this);//add graph settings for Graph Activity to process it		

		    	Intent i = new Intent(NoteList1243.this, Graph1243.class);
		    	
				startActivity(i); 
			}
		});
		registerDialog(dimp).show();
	}
	
	private void startImporting(String filename, boolean ignoreCustomDbRefs){
		Intent intent= new Intent(this, ImportDatabaseIntentService.class);
		
		intent.putExtra(Constants.IDIS_DBNAME, Constants.NOTE_DATABASE_NAME);
		intent.putExtra(Constants.IDIS_PASTING, false);
		intent.putExtra(Constants.IDIS_FILENAME, filename);
	//	Settings.putSharedPreferences(Constants.IDIS_WORKOVER, false, this);
		intent.putExtra(Constants.IDIS_NOTES, true);
		intent.putExtra(Constants.IDIS_IGNORE_NGCIC_REF, false);
		intent.putExtra(Constants.IDIS_IGNORE_CUSTOMDB_REF, ignoreCustomDbRefs);
		ImportDatabaseIntentService.registerImportToService(Constants.NOTE_DATABASE_NAME);
		startService(intent);		
		finish();
	}
	
	
	private void startPasting(){
		Intent intent= new Intent(this, ImportDatabaseIntentService.class);
		
		intent.putExtra(Constants.IDIS_DBNAME, Constants.NOTE_DATABASE_NAME);
		intent.putExtra(Constants.IDIS_PASTING, true);		
		intent.putExtra(Constants.IDIS_NOTES, true);
		intent.putExtra(Constants.IDIS_IGNORE_NGCIC_REF, false);
		intent.putExtra(Constants.IDIS_IGNORE_CUSTOMDB_REF, true);
		ImportDatabaseIntentService.registerImportToService(Constants.NOTE_DATABASE_NAME);
		startService(intent);
		
		finish();
	}
	/*@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
	//	super.onActivityResult(requestCode, resultCode, data);
		
		if (data!=null){
			//Log.d(TAG,"requestCode="+requestCode+"resultCode="+resultCode+"Intent="+data);
			if(newcomment){ //SAND for add new processing 
				long id=comment.process(data);
				NoteRec n=Comment.getNote(this, (int)id);
				Global.noteList.add(n);
				//Global.noteList=Comment.getNoteList(this,ngc);
				
				newcomment=false;
			}
			else {
				comment.processToReplace(data,dbID,allNotes);//replacing the existing note if changes were made to it
				NoteRec n=Comment.getNote(this, dbID);
				Global.noteList.set(listID, n);
			}
			//String s=Details.makeDateString(n.c, true)+" "+Details.makeTimeString(n.c,true);
			//NoteListItem nli=new NoteListItem(s,n.note,Details.getShortDsoDescription(Query.makeDSO(this, String.valueOf(n.ngc))),n.ngc,n.c,n.id);
			//list.set(listID, nli);
			updateListArray();
		//	handler.sendEmptyMessage(0);
		}
			
	}*/
/*	private void importNotes(){
		List<NoteRec> lnr=new ArrayList<NoteRec>();
		try{
			BufferedReader in=new BufferedReader(new InputStreamReader(new FileInputStream(fnote)));
			String s=null;

			while((s=in.readLine())!=null){
				NoteRec n=Comment.parseNote(s);
				if(n!=null){
					lnr.add(n);
					//Log.d(TAG,"importing Note "+n);
				}

			}
			in.close();
		}
		catch (Exception e){
			InputDialog.message("I/O exception" ).show();
			Log.d(TAG,"exception"+e);
		}
		if(lnr.size()>0){
			Comment.addNotes(NoteList.this, lnr);
			
			
			Global.noteList=Comment.getNoteList(NoteList.this,ngc );
			//makeListArray();
					
				
		}
	}
	private void export(){
		BufferedOutputStream bos;
		PrintStream out;
		try{
			bos=new BufferedOutputStream(new FileOutputStream(fnote,false));
			out=new PrintStream(bos);
			for (NoteRec n:Global.noteList){
				String name;
				if(n.ngc>10000)
					name="IC"+(n.ngc-10000);
				else
					name="NGC"+n.ngc;
				String s=name+Global.delimiter_char+Details.makeDateString(n.c, true)
						+Global.delimiter_char+Details.makeTimeString(n.c, true)
						+Global.delimiter_char+n.note; 
				out.println(s);
			}
			
			out.close();
			InputDialog.message("Notes saved to "+fnote.getAbsolutePath() ).show();

		}
		catch (Exception e){
			InputDialog.message("I/O exception" ).show();
		}
	}*/
	
	private void updateListArray(){
		//ListAdapter adapter=new SimpleAdapter(this,list,R.layout.notelist_item,new String[]{"date","dso","note"},new int[] {R.id.notelist_datetime,R.id.notelist_dso,R.id.notelist_note});
		//ListAdapter adapter=mAdapter;
	//	mAdapter=new NLadapter(Global.noteList);
     //   setListAdapter(mAdapter);
		mAdapter.notifyDataSetChanged();
		//InfoList list=ListHolder.getListHolder().get(InfoList.NOTE_LIST);
		tv.setText(""+noteList.getCount()+getString(R.string._notes));
		new UpdatingThread(updHandler).start();
	//	getListView().invalidate();
      //  tv.setText(list.size()+" notes");
	}
/*	private void makeListArray(){		
		list=new ArrayList<NoteListItem>();	
		Set<Integer> set=new HashSet<Integer>();
		for(NoteRec n:Global.noteList){
			set.add(n.ngc);
		}
		Map<Integer,DSO> map=Query.makeDSOMap(this, set);
		
		for (NoteRec n:Global.noteList){	
				String s=Details.makeDateString(n.c, true)+" "+Details.makeTimeString(n.c,true);
				DSO d=map.get(n.ngc);//Query.makeDSO(this, String.valueOf(n.ngc));
				if(d==null){
					Log.d(TAG,"null DSO="+n.ngc);
				}
				if(d!=null)
					list.add(new NoteListItem(s,n.note,Details.getShortDsoDescription(d),n.ngc,n.c,n.id)); 			       		
				}		
				   

}*/
	public void onListItemClick(ListView parent,View v,int position,long id){
		NoteRecord rec=(NoteRecord)noteList.get(position);
		NoteRequest request=new NoteRequest(rec);
		Intent i=new Intent(this, Note1243.class);
		i.putExtra(BUNDLE, request.getBundle());
		startActivity(i);
		
		
		
		

	}	
	
	//overriding menu button
	public boolean onKeyDown(int keyCode, KeyEvent event) { 
		if (ALEX_MENU_FLAG && keyCode==KeyEvent.KEYCODE_MENU) {
			aMenu.show(tv);
			return true;
		}
		else if(keyCode==KeyEvent.KEYCODE_BACK) {
			finish();
			return true;
		}

		return super.onKeyDown(keyCode, event); 
	} 
	
	private void initAlexMenu() {
		
		boolean dayMode = !nightMode;
		
		aMenu = new alexMenu(this,new OnMenuItemSelectedListener() {
			public void MenuItemSelectedEvent(alexMenuItem selection) {
				parseMenu(selection.getId());
			}}, 
				getLayoutInflater());
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
		
		menuItems.add(new alexMenuItem(R.id.export_notelist_menu, 
				getString(R.string.export), dayMode?R.drawable.am_load:R.drawable.ram_load, true ));
		menuItems.add(new alexMenuItem(R.id.import_notelist_menu, 
				getString(R.string.import2), dayMode?R.drawable.am_save:R.drawable.ram_save, true ));
		menuItems.add(new alexMenuItem(R.id.ngc_notelist_menu, 
				getString(R.string.name2), dayMode?R.drawable.am_sort:R.drawable.ram_sort, true ));
		menuItems.add(new alexMenuItem(R.id.time_notelist_menu, 
				getString(R.string.note_time), dayMode?R.drawable.am_sort:R.drawable.ram_sort , true ));
		menuItems.add(new alexMenuItem(R.id.paste_notelist_menu, 
				getString(R.string.paste), dayMode?R.drawable.am_paste:R.drawable.ram_paste , true ));
		menuItems.add(new alexMenuItem(R.id.share_notelist_menu,
				getString(R.string.share_all), dayMode?R.drawable.am_share:R.drawable.ram_share , true ));
		menuItems.add(new alexMenuItem(R.id.search_notelist_menu,
				getString(R.string.search_text), dayMode?R.drawable.am_search:R.drawable.ram_search , true ));
		
		menuItems.add(new alexMenuItem(R.id.images_notelist_menu,
				getString(R.string.images), dayMode?R.drawable.am_image:R.drawable.ram_image , true ));

		if (aMenu.isNotShowing()){
			try {
				aMenu.setMenuItems(menuItems);
			} catch (Exception e) {
				InputDialog alert = new InputDialog(NoteList1243.this);
				alert.show(MENU_ERROR,e.getMessage());
			}	
		}
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
		contextMenu.setSkin(nightMode, Settings1243.getDarkSkin());
		
		//mine
		float text_size=getResources().getDimension(R.dimen.table_main_text_size);//mine
		float density=getResources().getDisplayMetrics().density;
		text_size=text_size/density;
		contextMenu.setTextSize((int)text_size);//contextMenu.setTextSize(18)

		contextMenu.makeFloat();
		
		//load the menu items
		ArrayList<alexMenuItem> menuItems = new ArrayList<alexMenuItem>();
	
		menuItems.add(new alexMenuItem(R.id.notelist_graph, getString(R.string.star_chart), 0, true ));
		menuItems.add(new alexMenuItem(R.id.notelist_play,  getString(R.string.play), 0, true ));
		menuItems.add(new alexMenuItem(R.id.picture_notelist_menu, getString(R.string.show_image), 0, true ));
		menuItems.add(new alexMenuItem(R.id.detail_notelist_menu, getString(R.string.details), 0, true ));
		menuItems.add(new alexMenuItem(R.id.remove_notelist_menu, getString(R.string.remove), 0, true ));
		menuItems.add(new alexMenuItem(R.id.removeall_notelist_menu, getString(R.string.remove_all), 0, true ));
		menuItems.add(new alexMenuItem(R.id.addall_notelist_menu, getString(R.string.add_all_to_observation_list), 0, true ));
		//menuItems.add(new alexMenuItem(R.id.share_notelist_menu, "Share", 0, true ));
		if (contextMenu.isNotShowing()){
			try {
				contextMenu.setMenuItems(menuItems);
			} catch (Exception e) {
				InputDialog.message(NoteList1243.this,"Menu error! " + e.getMessage(), 0).show();
			}	
		}
		getListView().setOnItemLongClickListener(new OnItemLongClickListener(){

			public boolean onItemLongClick(AdapterView<?> arg0, View v,	int index, long arg3) {
				contextMenu.setMenuItemId(index);
				contextMenu.setHeader(((TextView)v.findViewById(R.id.notelist_dso)).getText());
				contextMenu.show(v);
				return true;
			}
			
		});
	}
    protected void onDestroy(){
		try{
			aMenu.hide();
			contextMenu.hide();
		}
		catch(Exception e){}
		super.onDestroy();
		
		workerThread2.getLooper().quit();
	}
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
}
