package com.astro.dsoplanner;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.astro.dsoplanner.ObsInfoListImpl.Item;



public class QueryController extends Controller {
	@ObfuscateOPEN
	private static final String QCS = "qcs=";
	private static final String IMPORT_IS_ALREADY_RUNNING = "Import is already running";
	
	private static final String CATALOG_POSITION = "catalogPosition";	
	private static final String SEARCH_IS_ALREADY_RUNNING = "Search is already running";
	private static final String WORKER_THREAD = "Worker Thread";
	@ObfuscateCLOSE
	private static final String TAG="QueryController";@MarkTAG
	Activity activity;
	private HandlerThread workerThread;
	private Handler workerHandler;
	
	public static final int MESSAGE_INIT = 1;
	public static final int MESSAGE_ERROR_HANDLER=2;
	public static final int MESSAGE_TEXT=3;
	public static final int MESSAGE_UPDATE_VIEW=4;
	public static final int MESSAGE_INPROGRESS=5;
	public static final int MESSAGE_SET_ACTIVITY_NAME=6;
	public static final int MESSAGE_SAVE=7;
	public static final int MESSAGE_SET_TIME=8;
	public static final int MESSAGE_SEE_PICTURE=9;
	public static final int MESSAGE_ADD_OBSLIST=10;
	public static final int MESSAGE_ADD_ALL_OBSLIST=11;
	public static final int MESSAGE_ADD_NOTE=12;
	public static final int MESSAGE_SEE_NOTES=13;
	public static final int MESSAGE_SEE_ALL_NOTES=14;
	public static final int MESSAGE_UPDATE_CATALOG=15;
	public static final int MESSAGE_UPDATE_LIST=16;
	public static final int MESSAGE_EXPORT=17;
	public static final int MESSAGE_SHARE=18;
	public static final int MESSAGE_EXECUTE_ON_UI_THREAD=19;
	public static final int MESSAGE_SKY_VIEW=20;
	//public static final int DATA_ASTRO_CATALOG=21;
	//public static final int MESSAGE_IMPORT=22;
	public static final int MESSAGE_REMOVE_INPROGRESS=23;
	public static final int MESSAGE_KEEP_CURRENT_LIST=24;
	public static final int MESSAGE_FIND=25;
	public static final int MESSAGE_SET_LIST_LOCATION=26;
	public static final int MESSAGE_FIND_NEXT=27;
	public static final int MESSAGE_FIND_RESET=28;
	public static final int MESSAGE_TEXT_FIND=29;
	public static final int MESSAGE_UPDATE_FILTER_BTN=30;
	public static final int MESSAGE_CHECKBOX_CHOSEN=31;
	public static final int MESSAGE_ADD_MARKED_OBSLIST=32;
	public static final int MESSAGE_SHOW_ON_STAR_CHART=33;
	//public static final int DATA_DBLIST_POS=31;
	
	private FindRunner findRunner;
	private boolean skyviewFlag=false;
	
	/**
	 * indicate that SkyView is used with the list
	 */
	public void setSkyViewFlag(){
		skyviewFlag=true;
	}
	
	public void clearSkyViewFlag(){
		skyviewFlag=false;
	}
	public QueryController(Activity activity) {
		this.activity = activity;
		workerThread = new HandlerThread(WORKER_THREAD);
		workerThread.start();
		workerHandler = new Handler(workerThread.getLooper());
		findRunner=new FindRunner();
		findRunner.setMatcher(new FindRunner.Matcher() {			
			@Override
			public boolean match(Object o, String searchString) {
				if(o instanceof Item){
					AstroObject obj=((Item)o).x;
					/*if(!"".equals(searchString)&&(obj.getShortName().toUpperCase().contains(searchString.toUpperCase())
							||obj.getLongName().toUpperCase().contains(searchString.toUpperCase()))){
						return true;*/
					if(!"".equals(searchString)&&obj.getDsoSelName().toUpperCase(Locale.US).contains(searchString.toUpperCase()))
							return true;
					
				}				
				return false;
			}
		});
	}
	
	@Override
	public void dispose() {
		super.dispose();
		workerThread.getLooper().quit();
		disposed=true;
		Log.d(TAG,"controller disposed");
	}
	
/*	public Object getData(int what,Object data){
		switch(what){
	//	case DATA_ASTRO_CATALOG:
	//		return currentCatalog.getCurrentCatalogInt();
	//	case DATA_DBLIST_POS:
	//		return currentCatalog.getCurrentCatalogPos();
		}
		return null;
	}*/
	@Override
	public boolean handleMessage(int what, final Object data) {
		switch(what){
		case MESSAGE_INIT:
			init((Intent)data);
			return true;			
		
		case MESSAGE_SAVE:
			workerHandler.post(new Runnable(){
				public void run(){
					save((Integer)data);//spinPos passed from activity for saving in addition to the other data
				}
			});
			//save((Integer)data);//spinPos passed from activity for saving in addition to the other data
			return true;	
		case MESSAGE_SET_TIME:
			Point.setLST(AstroTools.sdTime(AstroTools.getDefaultTime(activity)));
			return true;
		case MESSAGE_SEE_PICTURE:
			Command command=new PictureCommand(activity,((Item)data).x);
			command.execute();
			return true;
		case MESSAGE_ADD_OBSLIST:
			InfoListFiller filler=new ObsListFiller(Arrays.asList(new Item[]{(Item)data}),false );
			int obsList=Settings1243.getSharedPreferences(activity).getInt(Constants.ACTIVE_OBS_LIST, InfoList.PrimaryObsList);

			ListHolder.getListHolder().get(obsList).fill(filler);			
			dirtyObsPref=true;
			return true;
		case MESSAGE_ADD_MARKED_OBSLIST:
			List<AstroObject>list2=new ArrayList<AstroObject>();
			List<Item>li=(List<Item>)data;
			for(Item item:li){
				if(item.y)
					list2.add(item.x);
			}
			
			InfoListFiller filler2=new ObsListFiller(list2);
			int obsList2=Settings1243.getSharedPreferences(activity).getInt(Constants.ACTIVE_OBS_LIST, InfoList.PrimaryObsList);

			InfoList iL2=ListHolder.getListHolder().get(obsList2);
			iL2.fill(filler2);
			dirtyObsPref=true;
			return true;
			
		case MESSAGE_ADD_ALL_OBSLIST:			
			InfoListFiller filler1=new ObsListFiller((List)data,false);
			int obsList1=Settings1243.getSharedPreferences(activity).getInt(Constants.ACTIVE_OBS_LIST, InfoList.PrimaryObsList);

			InfoList iL=ListHolder.getListHolder().get(obsList1);
			iL.fill(filler1);
			dirtyObsPref=true;
			return true;
		case MESSAGE_ADD_NOTE:
			command=new NewNoteCommand(activity, ((Item)data).x, Calendar.getInstance(), "");
			command.execute();
			return true;
		case MESSAGE_SEE_NOTES:
			command=new GetObjectNotesCommand(activity,((Item)data).x);
			command.execute();
			return true;
		case MESSAGE_SEE_ALL_NOTES:
			command=new GetAllNotesCommand(activity);
			command.execute();
			return true;
		case MESSAGE_UPDATE_CATALOG:
			if(searchRequestRunning){
				notifyOutboxHandlers(MESSAGE_TEXT,0,0,SEARCH_IS_ALREADY_RUNNING);	
				return true;
			}
			updateCatalog((DbListItem)data);
			return true;
		case MESSAGE_UPDATE_LIST:
			if(searchRequestRunning){
				notifyOutboxHandlers(MESSAGE_TEXT,0,0,SEARCH_IS_ALREADY_RUNNING);	
				return true;
			}
			notifyOutboxHandlers(MESSAGE_INPROGRESS,0,0,null);
			workerHandler.post(new SearchPerformer());
			dirtyPref=true;
			findRunner.reset();
			//last_position_found=-1;
			return true;
		case MESSAGE_EXPORT:
			
			workerHandler.post(new Runnable(){
				public void run(){
					export(data);
				}
			});
			
			return true;
		case MESSAGE_SHARE:
			workerHandler.post(new Runnable(){
				public void run(){
					share((List<Item>)data);
				}
			});
			return true;
		case MESSAGE_SKY_VIEW:
			skyView(((Item)data).x);
			return true;
		case MESSAGE_KEEP_CURRENT_LIST:
			
		
			if(dirtyPref){
				InfoList list=(InfoList)data;
				Log.d(TAG,"list size="+list.getCount());
				InfoList mainlist=ListHolder.getListHolder().get(InfoList.NGCIC_SELECTION_LIST);
				if(!mainlist.equals(list)){//if there was no update then list is just a reference of original list
					mainlist.setListName("");
					mainlist.removeAll();
					mainlist.fill(new InfoListIteratorFiller(list.iterator()));
					Log.d(TAG,"main list size="+mainlist.getCount());
				}
			}
			return true;
		case MESSAGE_FIND:
			//last_position_found=-1;
			//find(data);
			Holder2<String,List<Item>> h=(Holder2<String,List<Item>>)data;
			findRunner.setSearch(new FindRunner.BasicListAdapter(h.y), h.x);
			makeFind();
			return true;
			
		case MESSAGE_FIND_NEXT:
			makeFind();
			return true;
		
		case MESSAGE_FIND_RESET:
			findRunner.reset();
			return true;
		case MESSAGE_CHECKBOX_CHOSEN:
			dirtyPref=true;
			return true;
		}
		return false;
	}
	private void makeFind(){
		int pos=findRunner.find();
		if(pos>-1)
			notifyOutboxHandlers(MESSAGE_SET_LIST_LOCATION,pos,0,null);
		else
			notifyOutboxHandlers(MESSAGE_TEXT_FIND,0,0,activity.getString(R.string.no_match_found_));
		
	}
	//CurrentCatalog currentCatalog;
	
	private volatile boolean searchRequestRunning=false;//maybe more correct to make sync query to this variable
	private volatile boolean importRunning=false;
	
	
	private String getActivityName(){
		return Settings1243.getSelectedCatalogsSummary(activity, Settings1243.DSO_SELECTION);
		
		/*List<String>list=Settings1243.getSelectedCatalogsNames(activity,Settings1243.DSO_SELECTION);
		String summary="";
		for(String s:list){
			summary=summary+s+" ";
		}
		if(!"".equals(summary))
			summary=summary.substring(0,summary.length()-1);
		if("".equals(summary)){
			summary="No catalog";
		}
		return summary;*/
	}
	private void init(Intent i){
		
		

		String aname=Settings1243.getStringFromSharedPreferences(activity, Constants.QUERY_ACTIVITY_NAME, "");
		aname=activity.getString(R.string.dso_selection_)+aname;
		notifyOutboxHandlers(MESSAGE_SET_ACTIVITY_NAME,0,0,aname);
		InfoList infoList=ListHolder.getListHolder().get(InfoList.NGCIC_SELECTION_LIST);//info list to work with

		if (infoList.getCount()==0){//empty list
			Log.d(TAG,"init, search");
			notifyOutboxHandlers(MESSAGE_INPROGRESS,0,0,null);
			Settings1243.putSharedPreferences(Constants.SETTINGS_SEARCH_CATALOG_UPDATE, true, activity);
			notifyOutboxHandlers(MESSAGE_UPDATE_FILTER_BTN,0,0,null);
			workerHandler.post(new SearchPerformer());
			dirtyPref=true;
		}
		else{
			SharedPreferences prefs=Settings1243.getSharedPreferences(activity);
			int spinPos=prefs.getInt(Constants.QUERY_CONTROLLER_SPIN_POS,0);	
			Log.d(TAG,"init, spin pos="+spinPos);
			notifyOutboxHandlers(MESSAGE_UPDATE_VIEW,spinPos,0,infoList);
		}
		
	}

	private boolean dirtyPref=false;//for tracking pref saving necessity
	private boolean dirtyObsPref=false;//for tracking obs list pref saving
	/**
	 * saving current InfoList.NGCIC_SELECTION_LIST. Update it with
	 * MESSAGE_KEEP_CURRENT_LIST before saving
	 * @param spinPos
	 */
	private void save(int spinPos){
		
		Set<Integer> set=new HashSet<Integer>();
		if(dirtyPref){
			set.add(InfoList.NGCIC_SELECTION_LIST);
			dirtyPref=false;
		}
		if(dirtyObsPref){
			int obsList=Settings1243.getSharedPreferences(activity).getInt(Constants.ACTIVE_OBS_LIST, InfoList.PrimaryObsList);

			set.add(obsList);
			dirtyObsPref=false;
		}
		Log.d(TAG,"save, set="+set);
		if(!set.isEmpty())
			new Prefs(activity).saveLists(set);
		//saving chosen constellation
	//	SharedPreferences prefs=activity.getSharedPreferences(Constants.PREFS,Context.MODE_PRIVATE);
		Settings1243.putSharedPreferences(Constants.QUERY_CONTROLLER_SPIN_POS, spinPos, activity);
		//prefs.edit().putInt(Constants.QUERY_CONTROLLER_SPIN_POS, spinPos).commit();
	}
	private void updateCatalog(DbListItem dbitem){
		if(Global.FREE_VERSION)return;
		
	//	if(currentCatalog.getCurrentCatalogInt()!=dbitem.cat){//if another catalog chosen
			InfoList iL=ListHolder.getListHolder().get(InfoList.DB_LIST);
			int pos=findPos(dbitem.menuId);	
			
			DbListItem item=(DbListItem)iL.get(pos);
			if(ImportDatabaseIntentService.isBeingImported(item.dbFileName)){
			//	pos=0;//NGCIC
				Settings1243.putSharedPreferences(Constants.SETTINGS_SEARCH_CATALOG_UPDATE, false, activity);
				notifyOutboxHandlers(MESSAGE_UPDATE_FILTER_BTN,0,0,null);
				notifyOutboxHandlers(MESSAGE_TEXT,0,0,Global.DB_IMPORT_RUNNING);
				return;
			}
			
		//	currentCatalog.setCatalogPos(pos);
			//infoList.setListName(""+dbitem.cat);
			//updateSearch();
			notifyOutboxHandlers(MESSAGE_INPROGRESS,0,0,null);
			workerHandler.post(new SearchPerformer());
			dirtyPref=true;
			findRunner.reset();
			//last_position_found=-1;
	//	}
	/*	else{
			handleMessage(MESSAGE_UPDATE_LIST,null);
		}*/

	}
	private int findPos(int menuid){
		InfoList iL=ListHolder.getListHolder().get(InfoList.DB_LIST);
		Iterator it=iL.iterator();
		int pos=0;
		for(;it.hasNext();){
			DbListItem dbitem=(DbListItem)it.next();
			if(dbitem.menuId==menuid)
				return pos;
			pos++;
		}
		return 0;
	}
	private void export(Object data){
		boolean noError=true;
		ErrorHandler eh=new ErrorHandler();
		InfoListSaver saver=null;
		try{
			if(!AstroTools.isExternalStorageAvailable(AstroTools.EXT_STORAGE_WRITABLE)){
				eh=AstroTools.getExtStorageNotAvailableMessage(AstroTools.EXT_STORAGE_WRITABLE, activity);
				//	notifyOutboxHandlers(MESSAGE_ERROR_HANDLER,0,0,eh);
				noError=false;
			}
			else{
				
				Holder2<String,List<Item>> h=(Holder2<String,List<Item>>)data;
			//	if(currentCatalog.getCurrentCatalogInt()==AstroCatalog.NGCIC_CATALOG){
					//set limits on exporting

			//		saver=new InfoListStringSaverImp(
			//				new FileOutputStream(Global.dsoPath + h.x));	
			//	}
			//	else
					saver=new InfoListStringSaverImp(
							new FileOutputStream(Global.exportImportPath + h.x));
				saver.addName("")	;
				for(Item item: h.y){
					saver.addObject(item.x);
				}
			}
			
		}
		catch(Throwable e){
			Log.d(TAG,"Exception="+e);
			noError=false;
		}
		finally{
			try{
				saver.close();
			}
			catch (Exception e){}
		}
		notifyOutboxHandlers(MESSAGE_ERROR_HANDLER,0,0,eh);
		String message=(!noError?activity.getString(R.string.export_error_):activity.getString(R.string.export_successfull_));
		notifyOutboxHandlers(MESSAGE_TEXT,0,0,message);
		
		
	}
	private void share(List<Item>list){
		ByteArrayOutputStream out=new ByteArrayOutputStream();
		ErrorHandler eh=new ErrorHandler();
		InfoListSaver saver=new InfoListStringSaverImp(out,Global.SHARE_LINES_LIMIT,eh);
		//int i=0;
		for(Item o:list){
			try{
				saver.addObject(o.x);
			}
			catch(Exception e){}			
				
		}
		notifyOutboxHandlers(MESSAGE_ERROR_HANDLER,0,0,eh);
		final String s=out.toString();
		//Log.d(TAG,"s length="+s.length());
		notifyOutboxHandlers(MESSAGE_EXECUTE_ON_UI_THREAD,0,0,new Executable(){
			public void run(){
				new ShareCommand(context,s).execute();
			}
		});
		
	}
	
	AstroObject prevObject=null;
	
	private boolean skyViewIntent=false;
	public boolean isIntentToGraph(){
		return skyViewIntent;
	}
	public void clearGraphIntentFlag(){
		skyViewIntent=false;
	}
	
	private void skyView(AstroObject obj){
		Calendar defc=AstroTools.getDefaultTime(activity);
		Point.setLST(AstroTools.sdTime(defc));//need to calculate Alt and Az
		Settings1243.putSharedPreferences(Constants.GRAPH_OBJECT, obj, activity);
		int zoom=Settings1243.getSharedPreferences(activity).getInt(Constants.CURRENT_ZOOM_LEVEL, Constants.DEFAULT_ZOOM_LEVEL);
		obj.recalculateRaDec(defc);
	//	Global.graphCreate=
		GraphRec rec=new GraphRec(zoom,obj.getAz(),obj.getAlt(),defc,obj,0,1);
		rec.save(activity);//add graph settings for Graph Activity to process it		
		
		if(skyviewFlag&&!obj.equals(prevObject)){
			notifyOutboxHandlers(MESSAGE_SHOW_ON_STAR_CHART, 0, 0, rec);
		}
		else{


			final Intent i = new Intent(activity, Graph1243.class);
			notifyOutboxHandlers(MESSAGE_EXECUTE_ON_UI_THREAD,0,0,new Executable(){
				public void run(){
					skyViewIntent=true;
					context.startActivity(i);
				}
			});
		}
		prevObject=obj;
		
	}
	/*private void importFile(File f){
		notifyOutboxHandlers(MESSAGE_INPROGRESS,0,0,null);
		workerHandler.post(new ImportPerformer(currentCatalog.getCurrentCatalog(),currentCatalog.getCurrentDbListItem(),f));
		
	}*/
/*	private int last_position_found=-1;
	private Holder2<String,List<AstroObject>> lastdata=new Holder2<String,List<AstroObject>>("",new ArrayList<AstroObject>());
	private void find(Object data){
		Holder2<String,List<AstroObject>> h;
		if(last_position_found!=-1)
			h=lastdata;
		else{
			if(data==null)return;
			h=(Holder2<String,List<AstroObject>>)data;
			lastdata=h;
		}
		
		if(h.y.size()==0)return;
		int i=0;
		if(last_position_found+1>=h.y.size())
			i=0;
		else
			i=last_position_found+1;
		
		if(i==0)
			last_position_found=h.y.size();
		boolean incycle=true;
		boolean morelpf=(i>last_position_found);
		int k=0;
		while(incycle){
			AstroObject obj=h.y.get(i);
			String name1=obj.getShortName();
			String name2=obj.getLongName();
			if(name1.toUpperCase().contains(h.x.toUpperCase())||name2.toUpperCase().contains(h.x.toUpperCase())){
				notifyOutboxHandlers(MESSAGE_SET_LIST_LOCATION,i,0,null);
				last_position_found=i;
				return;
			}
			i++;
			k++;
			if(i>=h.y.size()){
				i=0;
				morelpf=false;
			}
			if(k>h.y.size())
				break;
			if(!morelpf){		
				
				incycle=(i<=last_position_found);//to show the same search again i<=last_position_found
			}
		}
		last_position_found=-1;
		notifyOutboxHandlers(MESSAGE_TEXT,0,0,"No coincidence found!");
		return;
	}*/
	
	class DsoSelFillerMod implements InfoListFiller{
		List<AstroObject> objlist=new ArrayList<AstroObject>();
		public Iterator getIterator(){
			List<ObsInfoListImpl.Item> itemlist=new ArrayList<ObsInfoListImpl.Item>();
			for(AstroObject obj:objlist){
				itemlist.add(new Item(obj,false));
			}
			objlist=null;
			return itemlist.iterator();
		}
		public DsoSelFillerMod(){
			List<Integer>catlist=Settings1243.getSelectedInternalCatalogs(activity,Settings1243.DSO_SELECTION);
			/*if(catlist.contains(AstroCatalog.MESSIER)){
				if(!catlist.contains(AstroCatalog.NGCIC_CATALOG))
					catlist.add(AstroCatalog.NGCIC_CATALOG);
			}
			if(catlist.contains(AstroCatalog.CALDWELL)){
				if(!catlist.contains(AstroCatalog.NGCIC_CATALOG))
					catlist.add(AstroCatalog.NGCIC_CATALOG);
			}*/
			if(catlist.contains(AstroCatalog.HERSHEL)){
				if(!catlist.contains(AstroCatalog.NGCIC_CATALOG))
					catlist.add(AstroCatalog.NGCIC_CATALOG);
			}			
			/*for(int key:keys){
				boolean defvalue=false;
				if(key==R.string.select_catalog_caldwell||key==R.string.select_catalog_hershell||key==R.string.select_catalog_messier||key==R.string.select_catalog_ngcic){
					defvalue=true;
				}
				boolean res=sh.getBoolean(activity.getString(key), defvalue);
				Log.d(TAG,"key="+key+" res="+res);
				if(res){
					int cat=Settings1243.getCatalogFromKey(key);
					if(cat!=-1){
						
						if(cat==AstroCatalog.MESSIER){
							if(!catlist.contains(AstroCatalog.MESSIER))
								catlist.add(AstroCatalog.MESSIER);
							if(!catlist.contains(AstroCatalog.NGCIC_CATALOG))
								catlist.add(AstroCatalog.NGCIC_CATALOG);
						}
						else if(cat==AstroCatalog.CALDWELL){
							if(!catlist.contains(AstroCatalog.CALDWELL))
								catlist.add(AstroCatalog.CALDWELL);
							if(!catlist.contains(AstroCatalog.NGCIC_CATALOG))
								catlist.add(AstroCatalog.NGCIC_CATALOG);
						}
						else if(cat==AstroCatalog.HERSHEL){
							
							if(!catlist.contains(AstroCatalog.NGCIC_CATALOG))
								catlist.add(AstroCatalog.NGCIC_CATALOG);
						}
						else
							catlist.add(cat);
					}
				}
			}*/
			catlist.addAll(Settings1243.getCatalogSelectionPrefs(activity,Settings1243.DSO_SELECTION));
			for(int cat:catlist){
				if(Query1243.isStopping())
					break;
				List<AstroObject> la=search(cat);
				Log.d(TAG,"searching "+cat+" size="+la.size());
				int totalsize=la.size()+objlist.size();
				if(totalsize>Global.SQL_SEARCH_LIMIT){
					int neededsize=Global.SQL_SEARCH_LIMIT-objlist.size();
					if(neededsize<=0)
						break;
					neededsize=Math.min(neededsize, la.size());

					List<AstroObject> la2=new ArrayList<AstroObject>();
					for(int i=0;i<neededsize;i++){
						la2.add(la.get(i));							
					}
					addObjects(la2);

					break;

				}
				else{
					addObjects(la);
				}
			}
			
		}
		private void addObjects(List<AstroObject>list){
			/*for(AstroObject o:objlist){
				Log.d(TAG,"obj list="+o);
			}
			for(AstroObject o:list){
				Log.d(TAG,"list="+o);
			}*/
			//Log.d(TAG,"contains="+list.containsAll(objlist));
			if(Settings1243.isRemovingDuplicates()){
				Log.d(TAG,"removing dups, before "+list.size());
				list.removeAll(objlist);
				Log.d(TAG,"removing dups, after "+list.size());
				objlist.addAll(list);
			}
			else{
				objlist.addAll(list);
			}
		}
		private List<AstroObject> search(int cat){
			Log.d(TAG,"searching "+cat);
			List<AstroObject>list=new ArrayList<AstroObject>();
			DbListItem dbitem=DbManager.getDbListItem(cat);
			if(dbitem==null)
				return list;
			//Log.d(TAG,"searching 1"+cat);
			ErrorHandler eh=new ErrorHandler();
			AstroCatalog catalog=getCurrentCatalog(dbitem);



			catalog.open(eh); 
			if(eh.hasError()) {
				notifyOutboxHandlers(MESSAGE_ERROR_HANDLER,0,0,eh);				
				return list;
			}

			int search_type=Settings1243.getSearchType();
			boolean flag=search_type==Settings1243.ADVANVCED_SEARCH;
			//boolean flag=Settings1243.getSearchRequestPreference();
			SearchRequestItem item=Settings1243.getSearchRequestItem();	
			if(flag&&item==null){
				eh.addError(ErrorHandler.SQL_DB, activity.getString(R.string.no_sql_expression_selected_)+dbitem.dbName);
				notifyOutboxHandlers(MESSAGE_ERROR_HANDLER,0,0,eh);				
				return list;
			}
			
			if(flag)//adv search
			{
				Analisator an=new Analisator();
				Analisator anl=new Analisator();
				an.setInputString(item.sqlString);
				an.dsoInitSQLrequest();

				//DbListItem dbitem=currentCatalog.getCurrentDbListItem();
				if(!dbitem.ftypes.isEmpty()){//catalog with additional fields
					Set<String> set=dbitem.ftypes.getNumericFields();
					for(String s:set){
						an.addVar(s, 0);
					}						
				}
				anl.setInputString(item.localString);
				anl.dsoInitLocalrequest();			
				
				Log.d(TAG,"adv search,an="+an+"\nanl="+anl+"\ncat="+cat);
				//AstroTools.logr("adv search,an="+an+"\nanl="+anl+"\ncat="+cat);
				if(item.localString.equals(""))
					anl=null;
				try{
					an.compile();	
					Log.d(TAG,"sql request="+an.getRecStr());
					if(anl!=null) anl.compile();

					Calendar sc=Calendar.getInstance();
					long start=Settings1243.getSharedPreferences(Global.getAppContext()).getLong(Constants.START_OBSERVATION_TIME, 0);
					sc.setTimeInMillis(start);

					Calendar ec=Calendar.getInstance();
					long end=Settings1243.getSharedPreferences(Global.getAppContext()).getLong(Constants.END_OBSERVATION_TIME, 0);
					ec.setTimeInMillis(end);

					double LSTfin=AstroTools.sdTime(ec);
					double LSTstart=AstroTools.sdTime(sc);

					if(catalog instanceof CometsDatabase){
						CometsDatabase db=(CometsDatabase)catalog;
						list=db.searchMod(item.sqlString, item.localString, LSTstart, LSTfin, sc);
					}
					else{
						String recStr=an.getRecStr();
						if("".equals(recStr))recStr=null;
						list=catalog.search(recStr,anl,LSTstart,LSTfin);
					}

				}
				catch (UnsupportedOperationException e){						
					Log.d(TAG,"Exception="+e.getMessage());
				//	AstroTools.logr("Exception="+e.getMessage());
				//	notifyOutboxHandlers(MESSAGE_TEXT,0,0,e.getMessage());						
					catalog.close();						
				}

			}
			else {//basic search

				list=catalog.search();
			}


			catalog.close();
			return list;
		}
		
		private AstroCatalog getCurrentCatalog(DbListItem item){
			
			
				switch(item.cat){
				case AstroCatalog.NGCIC_CATALOG:
					return new NgcicDatabase(activity);
				case AstroCatalog.COMET_CATALOG:
					return new CometsDatabase(activity,AstroCatalog.COMET_CATALOG);
				case AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG:
					return new CometsDatabase(activity,AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG);
				}
				
				if(item.ftypes.isEmpty())
					return new CustomDatabase(activity,item.dbFileName,item.cat);
				else
					return new CustomDatabaseLarge(activity,item.dbFileName,item.cat,item.ftypes);
			}
			
	}
	
	
	class DsoSelFiller implements InfoListFiller{
		private AstroCatalog catalog;
		List<AstroObject> list=new ArrayList<AstroObject>();
		DbListItem dbitem;
		public DsoSelFiller(AstroCatalog cat,boolean full,DbListItem dbitem){
			this.dbitem=dbitem;
			catalog=cat;
			ErrorHandler eh=new ErrorHandler();
			catalog.open(eh); 
			if(eh.hasError()) {
				notifyOutboxHandlers(MESSAGE_ERROR_HANDLER,0,0,eh);				
				return;
			}
			
			if(!full){				
				//boolean flag=Settings1243.getSearchRequestPreference();
				//SearchRequestItem item=Settings1243.getSearchRequestItem();	
				
				
				int search_type=Settings1243.getSearchType();
				boolean flag=search_type==Settings1243.ADVANVCED_SEARCH;				
				SearchRequestItem item=Settings1243.getSearchRequestItem();	
				
				if(flag&&item!=null)
				{
					Analisator an=new Analisator();
					Analisator anl=new Analisator();
					an.setInputString(item.sqlString);
					an.dsoInitSQLrequest();
					
					//DbListItem dbitem=currentCatalog.getCurrentDbListItem();
					if(!dbitem.ftypes.isEmpty()){//catalog with additional fields
						Set<String> set=dbitem.ftypes.getNumericFields();
						for(String s:set){
							an.addVar(s, 0);
						}						
					}
					anl.setInputString(item.localString);
					anl.dsoInitLocalrequest();			
				
					if(item.localString.equals(""))
						anl=null;
					try{
						an.compile();	
						Log.d(TAG,"sql request="+an.getRecStr());
						if(anl!=null) anl.compile();
						
						Calendar sc=Calendar.getInstance();
						long start=Settings1243.getSharedPreferences(Global.getAppContext()).getLong(Constants.START_OBSERVATION_TIME, 0);
						sc.setTimeInMillis(start);
						
						Calendar ec=Calendar.getInstance();
						long end=Settings1243.getSharedPreferences(Global.getAppContext()).getLong(Constants.END_OBSERVATION_TIME, 0);
						ec.setTimeInMillis(end);
						
						double LSTfin=AstroTools.sdTime(ec);
						double LSTstart=AstroTools.sdTime(sc);
						
						if(catalog instanceof CometsDatabase){
							CometsDatabase db=(CometsDatabase)catalog;
							list=db.searchMod(item.sqlString, item.localString, LSTstart, LSTfin, sc);
						}
						else{
							String recStr=an.getRecStr();
							if("".equals(recStr))recStr=null;
							list=catalog.search(recStr,anl,LSTstart,LSTfin);
						}

					}
					catch (UnsupportedOperationException e){						
						Log.d(TAG,"Exception="+e.getMessage());
						notifyOutboxHandlers(MESSAGE_TEXT,0,0,e.getMessage());						
						catalog.close();						
					}

				}
				else {
					
						list=catalog.search();
				}
			}
			else
				list=catalog.search(null);//all catalog
			if(Settings1243.isRemovingDuplicates())
				list=replace(list);
			//list=AstroTools.replaceObjects(list, activity);
			catalog.close();			
		}
		
		public void search(){
			
		}
		public void update(){
			list=new ArrayList<AstroObject>();
			ErrorHandler eh=new ErrorHandler();
			catalog.open(eh); 
			if(eh.hasError()) {
				notifyOutboxHandlers(MESSAGE_ERROR_HANDLER,0,0,eh);
				return;
			}
			list=catalog.search();
			catalog.close();
		}
		public Iterator getIterator(){
			return list.iterator();
		}
		
		private List<AstroObject> replace (List<AstroObject> list){
			List<AstroObject> li=new ArrayList<AstroObject>();
			List<Integer> lint=new ArrayList<Integer>();
			for(AstroObject obj:list){
				AstroObject o=obj;
				if(obj.getCatalog()==AstroCatalog.UGC){
					if(obj.ref!=0){
						o=null;
						lint.add(obj.ref);
					}
				}
				if(o!=null){
					li.add(o);
				}
			}
			if(lint.size()>0){
				li.addAll(replaceForCrossRefs(lint));
			}
			return li;
		}
		/**
		 * 
		 * @param list - list of _ids to NGCIC db
		 * @return
		 */
		private List<AstroObject> replaceForCrossRefs(List<Integer> list){
			List<AstroObject>lis=new ArrayList<AstroObject>();
			NgcicDatabase cat=new NgcicDatabase();
			ErrorHandler eh=new ErrorHandler();
			cat.open(eh);
			if(eh.hasError()){
				return lis;
			}
			
			final int size=60;
			int j=0;
			String req="";
			for(int i=0;i<list.size();i++){
				req+=Constants._ID+"="+list.get(i)+" OR ";
				if(i%60==0&&i!=0){
					req=req.substring(0, req.length()-4);
					List<AstroObject>li=cat.search(req);
					lis.addAll(li);
					req="";
				}
			}
			if(!"".equals(req)){
				req=req.substring(0, req.length()-4);
				List<AstroObject>li=cat.search(req);
				lis.addAll(li);
			}
			cat.close();
			return lis;
		}
	}
	
	class CurrentCatalog{
		
		private int pos=0; //in DbList
		void setCatalogPos(int pos){
			this.pos=pos;
			//Global.queryActCat=cat;
			
		}
		@Override
		public String toString(){
			return "pos="+pos+"item="+getCurrentDbListItem();
		}
		public CurrentCatalog(){
			
		}
		/**
		 * catalog number in AstroCatalog, not position in db list
		 * @return
		 */
		int getCurrentCatalogInt(){
			InfoList iL=ListHolder.getListHolder().get(InfoList.DB_LIST);
			DbListItem item=(DbListItem)iL.get(pos);
			return item.cat;
		}
		/**
		 * position in db list
		 * @return
		 */
		int getCurrentCatalogPos(){
			return pos;
		}
		DbListItem getCurrentDbListItem(){
			InfoList iL=ListHolder.getListHolder().get(InfoList.DB_LIST);
			DbListItem item=(DbListItem)iL.get(pos);
			return item;
		}
		AstroCatalog getCurrentCatalog(){
			InfoList iL=ListHolder.getListHolder().get(InfoList.DB_LIST);
			DbListItem item=(DbListItem)iL.get(pos);    		
			if(pos==0)
				return new NgcicDatabase();
			else{
				switch(item.cat){
				case AstroCatalog.COMET_CATALOG:
					return new CometsDatabase(activity,AstroCatalog.COMET_CATALOG);
				case AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG:
					return new CometsDatabase(activity,AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG);
				}
				
				if(item.ftypes.isEmpty())
					return new CustomDatabase(activity,item.dbFileName,item.cat);
				else
					return new CustomDatabaseLarge(activity,item.dbFileName,item.cat,item.ftypes);
			}
			//return null; SAND it was causing fatal crash
		}
		/*void setActivityName(){
			InfoList iL=ListHolder.getListHolder().get(InfoList.DB_LIST);
			Log.d(TAG,"setActivityName, iL="+iL);
			DbListItem item=(DbListItem)iL.get(pos);
			String name="DSO Selection: "+item.dbName;
			Query2.this.setTitle(name);
			curDb = item.cat; 
			
			
		}*/
		
	}
	class SearchPerformer implements Runnable{

		
		
		//AstroCatalog catalog;
		//String name;
		/*public SearchPerformer(AstroCatalog catalog,String name){
			this.name=name;
			this.catalog=catalog;
		}*/
		
		public void run(){			
			if(searchRequestRunning){
				notifyOutboxHandlers(MESSAGE_TEXT,0,0,SEARCH_IS_ALREADY_RUNNING);								
				return;
			}
			if(importRunning){
				notifyOutboxHandlers(MESSAGE_TEXT,0,0,IMPORT_IS_ALREADY_RUNNING);				
				return;
			}
			Query1243.clearStopFlag();
			searchRequestRunning=true;
			InfoListFiller filler=new DsoSelFillerMod();//new DsoSelFiller(catalog,false,currentCatalog.getCurrentDbListItem()); 
			InfoList infoList=new InfoListImpl("",ObsInfoListImpl.Item.class);				
			infoList.fill(filler);	
			
			//spinPos=0 in here
			Settings1243.putSharedPreferences(Constants.QUERY_UPDATE, false, activity);
			Settings1243.putSharedPreferences(Constants.SETTINGS_SEARCH_CATALOG_UPDATE, false, activity);
			notifyOutboxHandlers(MESSAGE_UPDATE_VIEW,0,0,infoList);
			
			String aname=getActivityName();
			Settings1243.putSharedPreferences(Constants.QUERY_ACTIVITY_NAME, aname, activity);
			notifyOutboxHandlers(MESSAGE_SET_ACTIVITY_NAME,0,0,activity.getString(R.string.dso_selection_)+aname);
			searchRequestRunning=false;
			Log.d(TAG,"search finished");
		}
	}
	/**
	 * Not used for the time being
	 * @author leonid
	 *
	 */
	/*class ImportPerformer implements Runnable{
		
		AstroCatalog catalog;
		DbListItem dbitem;
		File f;
		public ImportPerformer(AstroCatalog catalog,DbListItem item,File f){
			this.f=f;
			this.catalog=catalog;
			this.dbitem=item;
		}
		public void run(){
			
			if(searchRequestRunning){
				notifyOutboxHandlers(MESSAGE_TEXT,0,0,SEARCH_IS_ALREADY_RUNNING);				
				return;
			}
			if(importRunning){
				notifyOutboxHandlers(MESSAGE_TEXT,0,0,IMPORT_IS_ALREADY_RUNNING);				
				return;
			}
			importRunning=true;

			InfoList iL=new InfoListImpl("temp",CustomObject.class);//when loading the list all non-custom objects are discarded
			//File f=new File(Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"dso"+File.separator+"importDatabase.txt");
			try{
				
				InfoListLoader loader=new InfoListStringLoaderImp(new FileInputStream(f));
				ErrorHandler eh=iL.load(loader);
				notifyOutboxHandlers(MESSAGE_ERROR_HANDLER,0,0,eh);
			}
			catch(IOException e){
				Log.d(TAG,"Exception="+e);		
				importRunning=false;
				notifyOutboxHandlers(MESSAGE_REMOVE_INPROGRESS,0,0,null);
				return;
			}
			Iterator it=iL.iterator();
			//DbListItem dbitem=currentCatalog.getCurrentDbListItem();
			AstroCatalog astrocat;
			if(dbitem.ftypes.isEmpty())
				astrocat=new CustomDatabase(activity,dbitem.dbFileName,dbitem.cat);
			else
				astrocat=new CustomDatabaseLarge(activity,dbitem.dbFileName,dbitem.cat,dbitem.ftypes);
			
			ErrorHandler eh=new ErrorHandler();
			astrocat.open(eh);
			if(eh.hasError()){
				notifyOutboxHandlers(MESSAGE_ERROR_HANDLER,0,0,eh);
				return;
			}
			for(;it.hasNext();){
				CustomObject obj=(CustomObject)it.next();
				astrocat.add(obj,eh);    			
			}
			astrocat.close();
			notifyOutboxHandlers(MESSAGE_ERROR_HANDLER,0,0,eh);
			//Thread t=new Thread(new SearchPerformer(handler,catalog));
			//t.start();   
			workerHandler.post(new SearchPerformer(catalog,""+dbitem.cat));
			importRunning=false;
		}
	}*/
}
