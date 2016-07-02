package com.astro.onetable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import com.astro.onetable.Main.DsoDb;
import com.astro.onetable.Main.Names;
import com.astro.onetable.Main.Obj;
import com.astro.onetable.Main.Obj2;
import com.astro.simbaddata.AstroTools;
import com.astro.sqlite.Db;
import com.astro.stars.WDS;

public class Stars {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		build();
	}
	
	/**
	 * building total.db for stars based on hr catalog
	 * @throws Exception
	 */
	public static void build() throws Exception{
		Main.PATH="/home/leonid/DSOPlanner/LargeDb/Stars/";
		createTOTALTable();
		StarDb db=new StarDb("hrdb.db","customdbb","simbad_hr_data_ra_dec_mag_trunc.txt","simbad_hr_data_trunc.txt");
		db.run();
		WDS.processWDSNames();
		WDS.putComponent();
		WDS.addDiscovererToWDS();//actually need to be done one time only
		WDS.addDiscovererToTotal();
		WDS.addSeparationToTotal();
		WDS.removeSpaces();//actually need to be done one time only
		WDS.refactorADS();
		WDS.makeCompDb();
	}
	public static void createTOTALTable()throws Exception{
		Db db=new Db(Main.PATH,Main.DBNAME);		
		db.exec("drop table if exists total;");
		String sql="create table TOTAL (id integer primary key autoincrement, dbname text," +
				"_id integer,name text,n1 text,n2 text,n3 text,n4 text,n5 text,n6 text,n7 text,n8 text,n9 text,n10 text,n11 text,n12 text,ra real," +
				"dec real,ras real,decs real,mag real,mags real,dst,ref real,d1 text,d2 text,d3 text,d4 text,d5 text,d6 text,d7 text,d8 text,d9 text,d10 text,d11 text,d12 text);";
		db.exec(sql,-1);
		
		
	}
	
	
	
	static class StarDb extends DsoDb{
		public StarDb(String dbname, String table, String simbad_coord_file,
				String simbad_name_file){
			super(dbname,table,simbad_coord_file,simbad_name_file);
		}
		@Override
		public void addData()throws Exception{
			String field="name1";
			if(name2main){
				field="name2";
			}
			Db db=new Db(Main.PATH,Main.DBNAME);
			db.start();
			db.addCommand("attach database '"+Main.PATH+ dbname+"' as ngc;");
			db.addCommand("insert into TOTAL(_id,dbname,name,ra,dec,mag) select _id,'"+dbname+"',upper("+field+"),ra,dec,mag from ngc."+table+";");			
			db.end(-1);
			Main.p("added data for "+dbname);
		}
		@Override
		public void addSimbadCoords() throws Exception{
			Db db=new Db(Main.PATH,Main.DBNAME);
			db.start();
			db.addCommand("begin;");
			List<Obj> list=Parsers.loadSimbadRaDecMag(new File(Main.PATH,simbad_coord_file));
			for(Obj e:list){
				AstroTools.RaDecRec rec=e.rec;
				String sql="update TOTAL set ras="+rec.ra+",decs="+rec.dec+",mags="+e.mag+" where name='"+e.name.toUpperCase()+"' and dbname="+"'"+dbname+"';";
				db.addCommand(sql);
			}
			db.addCommand("commit;");
			db.end(-1);
			Main.p("added simbad coords for "+dbname);
		}
		@Override
		public void addSimbadNames() throws Exception{
			List<Obj2> ll=Parsers.processSimbadNames(Main.PATH, simbad_name_file, null);
			
			//Main.p("llsize="+ll.size());
			
			
			Db db=new Db(Main.PATH,Main.DBNAME);
			db.start();
			db.addCommand("begin;");
			for(Obj2 obj:ll){
				Names names=obj.names;
				String nameset="";
				int i=1;
				
				
				List<String>list=new ArrayList<String>();
				list.add("");
				list.add("");
				List<String>list2=names.get();
				ListIterator<String> it=list2.listIterator();
				while(it.hasNext()){
					String s=it.next();
					if(s.contains("TYC")){
						list.set(0, s);
						it.remove();
					}
					else if(s.contains("WDS")){
						list.set(1, s);
						it.remove();
					}
						
				}
				for(String s:list2){
					list.add(s);
				}
				
				
				for(String s:list){
					s=s.replace("'", "''");
					if(s.equals(obj.name))
						continue;
					if(i>=13)continue;
					nameset+="n"+i++ +"='"+s+"',";
				}
				if(nameset.length()>1)
					nameset=nameset.substring(0,nameset.length()-1);
				if(!"".equals(nameset)){
					String sql="update TOTAL set "+nameset +" where name='"+obj.name+"' and dbname="+"'"+dbname+"';";
					//Main.p("sql="+sql);
					db.addCommand(sql);
				}
			}
			db.addCommand("commit;");
			db.end(-1);
			Main.p("added simbad names for "+dbname);
		}
	}
	
	
}
