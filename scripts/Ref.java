package com.astro.onetable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.astro.crossrefs.CrossRefMakeDbMap;
import com.astro.dsoplanner.AstroObject;
import com.astro.sqlite.Db;
import com.astro.stars.WDS;


public class Ref {
	
	public static final String PATHDB=Main.PATH+"/dbs/";
	public static final String PATH_MISSING_SAO="/home/leonid/DSOPlanner/LargeDb/CrossRef/Simbad/SAO/out_missing_without_hr.txt";
	
	/**
	 * id in total database - common id
	 */
	Map<Integer,Integer>map=new HashMap<Integer,Integer>();
	int maxid=1;
	
	/**
	 * adding link id1 - id2
	 * @param id1
	 * @param id2
	 */
	public void add(int id1,int id2){
		
		
		if(id1==id2)
			return;
		Integer cid1=map.get(id1);			
		Integer cid2=map.get(id2);
		
		if((cid1==null)&&(cid2==null)){
			maxid++;			
			map.put(id1, maxid);
			map.put(id2, maxid);
		}
		else if (cid1==null){
			
			map.put(id1, cid2);
		}
		else if (cid2==null){			
			map.put(id2, cid1);
		}
		else{ //both!=-1
			join(cid1,cid2);
		}
	}
	/**
	 * joining two chains for name1 and name2
	 * assume that id1 and id2 exists and are different
	 * @param name1
	 * @param name2
	 */
	private void join(int id1,int id2){
		if(id1==id2)
			return;
		
		List<Integer>list2=new ArrayList<Integer>();
		for(Map.Entry<Integer, Integer> e:map.entrySet()){
			int value=e.getValue();
			
			if(value==id2){
				list2.add(e.getKey());
			}
		}
		for(Integer i:list2 ){
			map.put(i,id1);
		}
	}
	
	
	public void init() throws Exception{
		
		Db db=new Db(Main.PATH,Main.DBNAME);	
		
		//name - name

		String sq="select q1.id,q2.id,q1.ra,q1.dec,q1.a,q2.ra,q2.dec,q1.name from TOTAL as q1 join TOTAL as q2 on q1.name=q2.name where q1.id!=q2.id;";
		List<String[]>list0=db.exec(sq,-1);

		for(String[] a:list0){
			int id1=Integer.parseInt(a[0]);
			int id2=Integer.parseInt(a[1]);
			double ra1=Double.parseDouble(a[2]);
			double dec1=Double.parseDouble(a[3]);
			double ra2=Double.parseDouble(a[5]);
			double dec2=Double.parseDouble(a[6]);
			double dim=0;
			try{
				dim=Double.parseDouble(a[4]);
			}
			catch(Exception e){}
			double dst=Main.distance(ra1, dec1, ra2, dec2)*60;
			//String name=a[7];
			//if(name.equals("B137"))
			//	Main.p(""+dst+" "+a[0]+" "+a[1]+" "+dim);
			if(dst>Math.max(30, dim))
				continue;
			add(id1,id2);
		}
		
		
		
		
		
		
		//first name - second n
		for(int j=1;j<=12;j++){
			String sql="select q1.id,q2.id from TOTAL as q1 join TOTAL as q2 on q1.name=q2.n"+j+
					";";//and q1.dst<3 and q2.dst<3
			List<String[]>list=db.exec(sql,-1);
			
			for(String[] a:list){
				int id1=Integer.parseInt(a[0]);
				int id2=Integer.parseInt(a[1]);
				add(id1,id2);
			}
			Main.p("init "+j);
		}
		//second name - first n
		for(int j=1;j<=12;j++){
			String sql="select  q1.id,q2.id from TOTAL as q1 join TOTAL as q2 on q2.name=q1.n"+j+";"
					;//and q1.dst<3 and q2.dst<3
			List<String[]>list=db.exec(sql,-1);

			for(String[] a:list){
				int id1=Integer.parseInt(a[0]);
				int id2=Integer.parseInt(a[1]);
				add(id1,id2);
			}
			Main.p("init "+j);

		}


	
		// n - n
		
		for(int i=1;i<=12;i++){
			for(int j=1;j<=12;j++){
				String sql="select q1.id,q2.id from TOTAL as q1 join TOTAL as q2 on q1.n"+i+"=q2.n"+j+";"
						;//and q1.dst<3 and q2.dst<3
				List<String[]>list=db.exec(sql,-1);
				
				for(String[] a:list){
					int id1=Integer.parseInt(a[0]);
					int id2=Integer.parseInt(a[1]);
					add(id1,id2);
					
				}
				Main.p("init "+i+" "+j);
				
			}
		}
		
		
		
		//ngc - ugc
		db.start();
		db.addCommand("attach database '"+Main.PATH+"pgcnames.db' as pgc;");
		db.addCommand("drop table if exists ngcidpgc;");
		db.addCommand("drop table if exists ugcidpgc;");
		db.addCommand("create table ngcidpgc as select id as id,q1.n1 as pgc from TOTAL as q1 join names as q2 on q1.n1=q2.n1 where dbname='hcngc.db';");
		db.addCommand("create table ugcidpgc as select id as id,q2.n1 as pgc from TOTAL as q1 join names as q2 on q1.name=q2.n3 where dbname='hugc.db';");
		db.addCommand("select q1.id,q2.id from ngcidpgc as q1 join ugcidpgc as q2 on q1.pgc=q2.pgc;");
		List<String[]>list=db.end(-1);

		for(String[] a:list){
			int id1=Integer.parseInt(a[0]);
			int id2=Integer.parseInt(a[1]);
			add(id1,id2);
		}
		
	
		
		//ugc-ugc
		list=db.exec("select q1.id,q2.id from ugcidpgc as q1 join ugcidpgc as q2 on q1.pgc=q2.pgc where q1.id!=q2.id;",-1);

		for(String[] a:list){
			int id1=Integer.parseInt(a[0]);
			int id2=Integer.parseInt(a[1]);
			add(id1,id2);
		}
		
		

		Main.p("init over");
		
		
	}
	
	public void save() throws Exception{
		Db db=new Db(Main.PATH,Main.DBNAME);	
		db.start();
		db.addCommand("drop table if exists refs;");
		db.addCommand("create table refs (id integer,cid integer);");
		db.addCommand("begin;");
		for(Map.Entry<Integer, Integer> e:map.entrySet()){
			db.addCommand("insert into refs (id,cid) values("+e.getKey()+","+e.getValue()+");");
		}
		db.addCommand("commit;");
		db.end(-1);
		
		//create refc table id - id for further analisys
		db.exec("drop table if exists refc;",-1);
		db.exec("create table refc as select q1.id as id1,q2.id as id2 from refs " +
				"as q1 join refs as q2 on q1.cid=q2.cid where q1.id!=q2.id;", -1);
		
		Main.p("save over");
	}
	
	public void insertRefsIntoDbs() throws Exception{
		Db db=new Db(Main.PATH,Main.DBNAME);
		db.exec("update total set ref=null;", -1);
		List<String[]>list=db.exec("select id,cid from refs",-1);
		db.start();
		db.addCommand("begin;");
		for(String[] a:list){
			db.addCommand("update total set ref="+a[1]+" where id="+a[0]+";");
		}
		db.addCommand("commit;");
		db.end(-1);
		
		//copy ngcic
		Db dbb=new Db(PATHDB,"ngcic.db");
		dbb.start();
		dbb.addCommand("drop table if exists ngcic;");
		dbb.addCommand("drop index if exists name_index;");
		dbb.addCommand("CREATE TABLE ngcic (_id INTEGER PRIMARY KEY AUTOINCREMENT, name INTEGER,ra FLOAT,dec FLOAT,mag FLOAT,a FLOAT,b FLOAT,constellation INTEGER,type INTEGER,pa FLOAT,messier INTEGER,caldwell INTEGER,hershel INTEGER,name1 TEXT,comment TEXT,class TEXT,ref integer);");
		dbb.addCommand("create index name_index on ngcic(name);");
		dbb.addCommand("attach database '"+Main.PATH+"hcngc.db' as hcngc;");
		dbb.addCommand("insert into ngcic(_id,name,ra,dec,mag,a,b,constellation,type,pa,messier,caldwell,hershel,name1,comment,class) select _id,name,ra,dec,mag,a,b,constellation,type,pa,messier,caldwell,hershel,name1,comment,class from hcngc.ngcic;");
		dbb.end(-1);
		
		dbb.start();
		dbb.addCommand("attach database '"+Main.PATH+"total.db' as total;");		
		dbb.addCommand("select _id,ref from total where dbname='hcngc.db' and ref is not null;");		
		List<String[]>list2=dbb.end(-1);
		dbb.start();
		dbb.addCommand("begin;");
		for(String[] a:list2){
			dbb.addCommand("update ngcic set ref="+a[1]+" where _id="+a[0]+";");
		}
		dbb.addCommand("commit;");
		dbb.end(-1);
		dbb.exec("vacuum;");
		dbb.exec("PRAGMA user_version=2;",-1);
		dbb.exec("PRAGMA schema_version=2;",-1);
		dbb.start();
		dbb.addCommand(".output "+PATHDB+"ngcic.db.txt");
		dbb.addCommand(".dump ngcic");
		dbb.end(-1);
		
		
		
		
		String sqlc="CREATE TABLE customdbb (_id INTEGER PRIMARY KEY AUTOINCREMENT, dec FLOAT, ra FLOAT, a FLOAT, mag FLOAT, b FLOAT, constellation INTEGER, type INTEGER, pa FLOAT, typestr TEXT, name1 TEXT, name2 TEXT, comment TEXT,ref integer,class TEXT);";
		String sqli="insert into customdbb(_id,name1,name2,ra,dec,mag,a,b,constellation,type,pa,comment,class)  select _id,name1,name2,ra,dec,mag,a,b,constellation,type,pa,comment,class from db.customdbb;";
		createCustomDb("ugc.db","hugc.db", sqlc, sqli,true);
		
		sqlc="CREATE TABLE customdbb (_id INTEGER PRIMARY KEY AUTOINCREMENT, dec FLOAT, ra FLOAT, a FLOAT, mag FLOAT, b FLOAT, constellation INTEGER, type INTEGER, pa FLOAT, typestr TEXT, name1 TEXT, name2 TEXT, comment TEXT,ref integer,richness integer);";
		sqli="insert into customdbb(_id,name1,name2,ra,dec,mag,a,b,constellation,type,pa,comment,richness)  select _id,name1,name2,ra,dec,mag,a,b,constellation,type,pa,comment,richness from db.customdbb;";
		createCustomDb("abell.db","abell.db", sqlc, sqli,false);
		
		sqlc="CREATE TABLE customdbb (_id INTEGER PRIMARY KEY AUTOINCREMENT, dec FLOAT, ra FLOAT, a FLOAT, mag FLOAT, b FLOAT, constellation INTEGER, type INTEGER, pa FLOAT, typestr TEXT, name1 TEXT, name2 TEXT, comment TEXT, ref integer,opacity INTEGER);";
		sqli="insert into customdbb(_id,name1,name2,ra,dec,mag,a,b,constellation,type,pa,comment,opacity)  select _id,name1,name2,ra,dec,mag,a,b,constellation,type,pa,comment,opacity from db.customdbb;";
		createCustomDb("barnard.db","barnard.db", sqlc, sqli,false);
		dbb=new Db(PATHDB,"barnard.db");
		dbb.exec("update customdbb set mag=null;",-1);
		dbb.exec("update customdbb set pa=null;",-1);
		dbb.exec("update customdbb set a=null where a=0;",-1);
		dbb.exec("update customdbb set b=null where b=0;",-1);
		

		sqlc="CREATE TABLE customdbb (_id INTEGER PRIMARY KEY AUTOINCREMENT, dec FLOAT, ra FLOAT, a FLOAT, mag FLOAT, b FLOAT, constellation INTEGER, type INTEGER, pa FLOAT, typestr TEXT, name1 TEXT, name2 TEXT, comment TEXT,ref integer,count integer);";
		sqli="insert into customdbb(_id,name1,name2,ra,dec,mag,a,b,constellation,type,pa,comment,count)  select _id,name1,name2,ra,dec,mag,a,b,constellation,type,pa,comment,count from db.customdbb;";
		createCustomDb("hcg.db","hcg.db", sqlc, sqli,true);
		
		sqlc="CREATE TABLE customdbb (_id INTEGER PRIMARY KEY AUTOINCREMENT, dec FLOAT, ra FLOAT, a FLOAT, mag FLOAT, b FLOAT, constellation INTEGER, type INTEGER, pa FLOAT, typestr TEXT, name1 TEXT, name2 TEXT, comment TEXT,ref integer,brightness integer);";
		sqli="insert into customdbb(_id,name1,name2,ra,dec,mag,a,b,constellation,type,pa,comment,brightness)  select _id,name1,name2,ra,dec,mag,a,b,constellation,type,pa,comment,brightness from db.customdbb;";
		createCustomDb("lbn.db","hlbn.db", sqlc, sqli,true);

		sqlc="CREATE TABLE customdbb (_id INTEGER PRIMARY KEY AUTOINCREMENT, dec FLOAT, ra FLOAT, a FLOAT, mag FLOAT, b FLOAT, constellation INTEGER, type INTEGER, pa FLOAT, typestr TEXT, name1 TEXT, name2 TEXT, comment TEXT,ref integer,opacity integer);";
		sqli="insert into customdbb(_id,name1,name2,ra,dec,mag,a,b,constellation,type,pa,comment,opacity)  select _id,name1,name2,ra,dec,mag,a,b,constellation,type,pa,comment,opacity from db.customdbb;";
		createCustomDb("ldn.db","hldn.db", sqlc, sqli,true);

		sqlc="CREATE TABLE customdbb (_id INTEGER PRIMARY KEY AUTOINCREMENT, dec FLOAT, ra FLOAT, a FLOAT, mag FLOAT, b FLOAT, constellation INTEGER, type INTEGER, pa FLOAT, typestr TEXT, name1 TEXT, name2 TEXT, comment TEXT,ref integer);";
		sqli="insert into customdbb(_id,name1,name2,ra,dec,mag,a,b,constellation,type,pa,comment)  select _id,name1,name2,ra,dec,mag,a,b,constellation,type,pa,comment from db.customdbb;";
		createCustomDb("pk.db","hpk.db", sqlc, sqli,true);
		
		sqlc="CREATE TABLE customdbb (_id INTEGER PRIMARY KEY AUTOINCREMENT, dec FLOAT, ra FLOAT, a FLOAT, mag FLOAT, b FLOAT, constellation INTEGER, type INTEGER, pa FLOAT, typestr TEXT, name1 TEXT, name2 TEXT, comment TEXT,ref integer,brightness integer, form text, struct text);";
		sqli="insert into customdbb(_id,name1,name2,ra,dec,mag,a,b,constellation,type,pa,comment,brightness,form,struct)  select _id,name1,name2,ra,dec,mag,a,b,constellation,type,pa,comment,brightness,form,struct from db.customdbb;";
		createCustomDb("sh2.db","sh2.db", sqlc, sqli,true);
		
		sqlc="CREATE TABLE customdbb (_id INTEGER PRIMARY KEY AUTOINCREMENT, dec FLOAT, ra FLOAT, a FLOAT, mag FLOAT, b FLOAT, constellation INTEGER, type INTEGER, pa FLOAT, typestr TEXT, name1 TEXT, name2 TEXT, comment TEXT, ref integer,BCHM TEXT, BRSTR TEXT, CLASS TEXT, DESCR TEXT, NOTES TEXT, NSTS TEXT, OTHERNAME TEXT, SACTYPE TEXT, SUBR FLOAT, TI INTEGER, U2K INTEGER);";
		sqli="insert into customdbb(_id,name1,name2,ra,dec,mag,a,b,constellation,type,pa,comment,typestr,BCHM, BRSTR, CLASS, DESCR, NOTES, NSTS, OTHERNAME, SACTYPE, SUBR, TI, U2K)  select _id,name1,name2,ra,dec,mag,a,b,constellation,type,pa,comment,typestr,BCHM, BRSTR, CLASS, DESCR, NOTES, NSTS, OTHERNAME, SACTYPE, SUBR, TI, U2K from db.customdbb;";
		createCustomDb("sac.db","hsac.db", sqlc, sqli,false);

		sqlc="CREATE TABLE customdbb (_id INTEGER PRIMARY KEY AUTOINCREMENT, dec FLOAT, ra FLOAT, a FLOAT, mag FLOAT, b FLOAT, constellation INTEGER, type INTEGER, pa FLOAT, typestr TEXT, name1 TEXT, name2 TEXT, comment TEXT, ref integer);";
		sqli="insert into customdbb(_id,name1,name2,ra,dec,mag,a,b,constellation,type,pa,comment,typestr)  select _id,name1,name2,ra,dec,mag,a,b,constellation,type,pa,comment,typestr from db.customdbb;";
		createCustomDb("m.db","m.db", sqlc, sqli,false);
		
		sqlc="CREATE TABLE customdbb (_id INTEGER PRIMARY KEY AUTOINCREMENT, dec FLOAT, ra FLOAT, a FLOAT, mag FLOAT, b FLOAT, constellation INTEGER, type INTEGER, pa FLOAT, typestr TEXT, name1 TEXT, name2 TEXT, comment TEXT, ref integer);";
		sqli="insert into customdbb(_id,name1,name2,ra,dec,mag,a,b,constellation,type,pa,comment,typestr)  select _id,name1,name2,ra,dec,mag,a,b,constellation,type,pa,comment,typestr from db.customdbb;";
		createCustomDb("c.db","c.db", sqlc, sqli,false);

		
		Main.p("insertRefsIntoDbs over");

	}
	
	/**
	 * database attached as db
	 * @param olddbname
	 * @param createTable	 
	 * @param index make index
	 * @throws Exception
	 */
	public void createCustomDb(String newdbname,String olddbname,String createTable,String insert,boolean index) throws Exception{
		Db dbb=new Db(PATHDB,newdbname);
		dbb.start();
		dbb.addCommand("drop table if exists customdbb;");
		dbb.addCommand("drop index if exists name1_index;");		
		dbb.addCommand(createTable);
		if(index)
			dbb.addCommand("create index name1_index on customdbb(name1);");		

		dbb.addCommand("attach database '"+Main.PATH+olddbname+"' as db;");
		dbb.addCommand(insert);
		dbb.end(-1);
		
		dbb.start();
		dbb.addCommand("attach database '"+Main.PATH+"total.db' as total;");		
		dbb.addCommand("select _id,ref from total where dbname='"+olddbname+"' and ref is not null;");		
		List<String[]>list2=dbb.end(-1);
	//	Main.p(newdbname+" "+list2.size());
		dbb.start();
		dbb.addCommand("begin;");
		for(String[] a:list2){
			dbb.addCommand("update customdbb set ref="+a[1]+" where _id="+a[0]+";");
		}
		dbb.addCommand("commit;");
		dbb.end(-1);
		dbb.exec("vacuum;");
		dbb.exec("PRAGMA user_version=2;",-1);
		dbb.exec("PRAGMA schema_version=2;",-1);
	/*	dbb.start();
		dbb.addCommand(".output "+PATHDB+newdbname+".txt");
		dbb.addCommand(".dump customdbb");
		dbb.end(-1);*/
	}
	/**
	 * copying data from same refs in ngcic database
	 * @throws Exception 
	 */
	public static void copyNgcNgc() throws Exception{
		Db db=new Db(PATHDB,"ngcic.db");
		List<String>instructions=new ArrayList<String>();
		List<String[]>list=db.exec("select ref from ngcic group by ref having count(ref)>1;");
		for(String[] a:list){
			String ref=a[0];
			if(!"".equals(ref)){
				List<String[]>list2=db.exec("select _id from ngcic where ref="+ref+";");
				List<Integer>listid=new ArrayList<Integer>();
				for(String[] a2:list2){
					listid.add(Integer.parseInt(a2[0]));
				}
				if(listid.size()>1){
					Collections.sort(listid);
				/*	for(int i:listid){
						Main.p(""+i);
					}*/
					List<String[]>dl=db.exec("select ra,dec,a,b,mag,pa,constellation,type from ngcic where _id="+listid.get(0)+";");
					String[]d=dl.get(0);
					for(int i=1;i<listid.size();i++){
						/*List<String[]>list3=db.exec("select ra,dec from ngcic where _id="+listid.get(i)+";");
						String[]d2=list3.get(0);
						double ra2=Double.parseDouble(d2[0]);
						double dec2=Double.parseDouble(d2[1]);
						double ra1=Double.parseDouble(d[0]);
						double dec1=Double.parseDouble(d[1]);
						double dim=0;
						try{
							dim=Double.parseDouble(d[2]);
						}
						catch(Exception e){}
						double dst=Main.distance(ra1, dec1, ra2, dec2)*60;
						if(dst>Math.max(3, dim)){
							Main.p("id "+listid.get(0)+" "+listid.get(i)+" "+dst+" "+dim);
						}*/
						String sql="update ngcic set ra="+d[0]+",dec="+d[1]+",a="+("".equals(d[2])?"null":d[2])+",b="+("".equals(d[3])?"null":d[3])+",mag="+("".equals(d[4])?"null":d[4])+",pa="+("".equals(d[5])?"null":d[5])+",constellation="+d[6]+",type="+d[7]+" where _id="+listid.get(i)+";";
						instructions.add(sql);
					}
				}
				
			}
			
		}
		db.start();
		db.addCommand("begin;");
		for(String s:instructions){
			db.addCommand(s);
		}
		db.addCommand("commit;");
		db.end();
	}
	/**
	 * copying data from same refs into other databases
	 * @typestr - use for copying data for certain types only
	 * @throws Exception 
	 */
	public static void copyNgc(String dbname,String type_str) throws Exception{
		Db db=new Db(PATHDB,"ngcic.db");
		Db db2=new Db(PATHDB,dbname);
		List<String>instructions=new ArrayList<String>();
		db.start();
		db.addCommand("attach database '"+PATHDB+dbname+"' as ext;");
		
				
		db.addCommand("select q1._id,q2._id from ngcic as q1 join customdbb as q2 on q1.ref=q2.ref "+type_str+";");
		List<String[]>ids=db.end();
		
		for(String[] a:ids){
			List<String[]>dl=db.exec("select ra,dec,a,b,mag,pa,constellation,type from ngcic where _id="+a[0]+";");
			String[]d=dl.get(0);

			List<String[]>list3=db2.exec("select ra,dec from customdbb where _id="+a[1]+";");
			String[]d2=list3.get(0);
			double ra2=Double.parseDouble(d2[0]);
			double dec2=Double.parseDouble(d2[1]);
			double ra1=Double.parseDouble(d[0]);
			double dec1=Double.parseDouble(d[1]);
			double dim=0;
			try{
				dim=Double.parseDouble(d[2]);
			}
			catch(Exception e){}
			double dst=Main.distance(ra1, dec1, ra2, dec2)*60;
			if(dst>Math.max(30, 2*dim)){
				Main.p("id "+a[0]+" "+a[1]+" "+dst+" "+dim);
			}
			else{
				String sql="update customdbb set ra="+d[0]+",dec="+d[1]+",a="+("".equals(d[2])?"null":d[2])+",b="+("".equals(d[3])?"null":d[3])+",mag="+("".equals(d[4])?"null":d[4])+",pa="+("".equals(d[5])?"null":d[5])+",constellation="+d[6]+",type="+d[7]+" where _id="+a[1]+";";
				instructions.add(sql);
			}
			
		}
		
		
		
		db2.start();
		db2.addCommand("begin;");
		for(String s:instructions){
			db2.addCommand(s);
		}
		db2.addCommand("commit;");
		db2.end();
		
	}
	
	
	
	public void run() throws Exception{
		init();
		save();
		insertRefsIntoDbs();
		copyNgcNgc();
		copyNgc("sac.db"," where q1.type="+AstroObject.Gxy);
		copyNgc("ugc.db","");
		makeRefDb();
		CrossRefMakeDbMap.fill();
		addStarsToRef();
	}
	
	public static void addStarsToRef() throws Exception{
		Db db=new Db(Main.PATH,"cross.db");
		List<String[]> list=db.exec("select max(id) from cross;" );
		int maxid=Integer.parseInt(list.get(0)[0])+1;
		
		Db db2=new Db(WDS.PATH,"total.db");
		list=db2.exec("select name,wds,n1,n2,n3,n4,n5,n6,n7,n8,n9,n10,n11,n12,d1,d2,d3,d4,d5,d6,d7,d8,d9,d10,d11,d12 from total;");
		db.start();
		db.addCommand("begin;");
		int k=0;
		for(String[] a:list){
			k++;
			db.addCommand("insert into cross values ('"+a[0].replace("'", "''")+"',"+maxid+");");
			if(!a[1].equals("")){
				db.addCommand("insert into cross values ('"+"WDS"+a[1]+"',"+maxid+");");
				k++;
			}
			for(int i=1;i<=24;i++){
				if(i==2)continue;
				String n=a[i+1].replace("'", "''");
				if(!"".equals(n)){
					db.addCommand("insert into cross values ('"+n+"',"+maxid+");");
					k++;
				}
			}
			
			maxid++;
		}
		db.addCommand("commit;");
		db.end();
		Main.p("put "+k);
		
		db=new Db(Main.PATH,"cross.db");
		list=db.exec("select max(id) from cross;" );
		maxid=Integer.parseInt(list.get(0)[0])+1;
		
		db2=new Db(WDS.PATH,"haas.db");
		list=db2.exec("select name1,wds from customdbb;");
		db.start();
		db.addCommand("begin;");
		
		for(String[] a:list){
			db.addCommand("insert into cross values ('"+a[0]+"',"+maxid+");");
			db.addCommand("insert into cross values ('WDS"+a[1]+"',"+maxid+");");
			maxid++;
		}
		db.addCommand("commit;");
		db.end();
		
	}
	
	/**
	 * making a text file for creating ref database
	 * @throws Exception
	 */
	public void makeRefDb() throws Exception{
		Db db=new Db(Main.PATH,Main.DBNAME);
		PrintWriter pw=new PrintWriter(new FileOutputStream(new File(Main.PATH,"mainrefs.txt")));
		
		int n=0;
		//name - n
		
		for(int i=1;i<=12;i++){
			String sql="select name,n"+i+" from total where n"+i+" is not null;";
			List<String[]>list=db.exec(sql, -1);
			for(String[] a:list){
				pw.println(a[0]+";"+a[1]);
				n++;
			}
		}
		
		Main.p("name - n "+n);
		
		//refc1 - refc2
		
		String sql="select t1.name,t2.name from total as t1 " +
				"join refc on t1.id=refc.id1 join total as t2 on t2.id=refc.id2;"; 
		List<String[]>list=db.exec(sql, -1);		
		for(String[] a:list){
			pw.println(a[0]+";"+a[1]);
			n++;
			
		}
		Main.p("refc1 - refc2 "+n);
	/*	//ngcic name - n1 for pgc names
		List<String[]> list1=db.exec("select name, n1 from total where n1 like 'pgc%' and dbname='hcngc.db';",-1);
		for(String[] a:list1){
			pw.println(a[0]+";"+a[1]);
		}*/
		
		/*//ngcic name - pgcnames on n1(pgc) field, fields n1-6
		db.start();
		db.addCommand("attach database '"+Main.PATH+"pgcnames.db' as names;");
		db.addCommand("select q2.n1,q2.n3,q2.n4,q2.n5,q2.n6 from total as q1 join names as q2 on q1.n1=q2.n1 where dbname='hcngc.db';");
		List<String[]> list2=db.end(-1);
		Main.p("size="+list2.size());
		for(String[] a:list2){
			if(!"".equals(a[1])) {
				pw.println(a[0]+";"+a[1]);
				n++;
			}
			if(!"".equals(a[2])) {
				pw.println(a[0]+";"+a[2]);
				n++;
			}
			if(!"".equals(a[3])) {
				pw.println(a[0]+";"+a[3]);
				n++;
			}
			if(!"".equals(a[4])) {
				pw.println(a[0]+";"+a[4]);
				n++;
			}
		}
		Main.p("ngcic name - pgcnames on n1 "+n);
		//ugc name - pgcnames on n3(ugc) field, fields n1-6
		
		db.start();
		db.addCommand("attach database '"+Main.PATH+"pgcnames.db' as names;");
		db.addCommand("select q2.n1,q2.n3,q2.n4,q2.n5,q2.n6 from total as q1 join names as q2 on q1.name=q2.n3 where dbname='hugc.db';");
		List<String[]> list3=db.end(-1);
		Main.p("size="+list3.size());
		for(String[] a:list3){
			if(!"".equals(a[1])) {
				pw.println(a[0]+";"+a[1]);
				n++;
			}
			if(!"".equals(a[2])) {
				pw.println(a[0]+";"+a[2]);
				n++;
			}
			if(!"".equals(a[3])) {
				pw.println(a[0]+";"+a[3]);
				n++;
			}
			if(!"".equals(a[4])) {
				pw.println(a[0]+";"+a[4]);
				n++;
			}
		}
		Main.p("ugc name - pgcnames on n3(ugc) "+n);*/
		
		
		
		
		//full pgcnames		
		db.start();
		db.addCommand("attach database '"+Main.PATH+"pgcnames.db' as names;");
		db.addCommand("select n1,n3,n4,n5,n6 from names;");
		List<String[]> list3=db.end(-1);
		Main.p("size="+list3.size());
		for(String[] a:list3){
			if(!"".equals(a[1])) {
				pw.println(a[0]+";"+a[1]);
				n++;
			}
			if(!"".equals(a[2])) {
				pw.println(a[0]+";"+a[2]);
				n++;
			}
			if(!"".equals(a[3])) {
				pw.println(a[0]+";"+a[3]);
				n++;
			}
			if(!"".equals(a[4])) {
				pw.println(a[0]+";"+a[4]);
				n++;
			}
		}
		
		//remaining 
		
		//ugc - core if no correspondence to pgcnames		
		db.start();
		db.addCommand("attach database '"+Main.PATH+"pgcnamesother.db' as names;");
		db.addCommand("select n3,n1,n4,n5,n6 from names;");//n3 - ugc name which we always have
		List<String[]> list5=db.end(-1);
		Main.p("size="+list5.size());
		for(String[] a:list5){
			if(!"".equals(a[1])) {
				pw.println(a[0]+";"+a[1]);
				n++;
			}
			if(!"".equals(a[2])) {
				pw.println(a[0]+";"+a[2]);
				n++;
			}
			if(!"".equals(a[3])) {
				pw.println(a[0]+";"+a[3]);
				n++;
			}
			if(!"".equals(a[4])) {
				pw.println(a[0]+";"+a[4]);
				n++;
			}
		}
		Main.p("ugc - core "+n);
		
		//ngc - core if no correspondence to pgcnames		
		db.start();
		db.addCommand("attach database '"+Main.PATH+"pgcnamesotherngc.db' as names;");
		db.addCommand("select n1,n3,n4,n5,n6 from names;");
		List<String[]> list4=db.end(-1);
		Main.p("size="+list4.size());
		for(String[] a:list4){
			if(!"".equals(a[1])) {
				pw.println(a[0]+";"+a[1]);
				n++;
			}
			if(!"".equals(a[2])) {
				pw.println(a[0]+";"+a[2]);
				n++;
			}
			if(!"".equals(a[3])) {
				pw.println(a[0]+";"+a[3]);
				n++;
			}
			if(!"".equals(a[4])) {
				pw.println(a[0]+";"+a[4]);
				n++;
			}
		}
		Main.p("ngc-core "+n);
		
		
		
		//building names from file for star chart layer
		BufferedReader in=new BufferedReader(new InputStreamReader(new FileInputStream(new File(Main.PATH,"missing_pgclayer_names_result.txt"))));
		String s;
		while((s=in.readLine())!=null){
			String[] a=s.split("\\|");
			String objname=a[0].trim();
			String pgcname="PGC"+a[1].trim();
			//do not use hyperleda NGC refs
			if(!(objname.contains("NGC")||objname.contains("IC"))){
				pw.println(objname+";"+pgcname);
			}
		}
		in.close();
		
		//building names from file for missing SAO (sao - wds)
		in=new BufferedReader(new InputStreamReader(new FileInputStream(new File(PATH_MISSING_SAO))));		
		while((s=in.readLine())!=null){
			pw.println(s);
		}
		in.close();
		
	/*	//pgc.db
		
		db=new Db(Main.PATH,"pgc.db");
		db.start();
		db.addCommand("attach '"+Main.PATH+"pgcnames.db' as names;");
		db.addCommand("select name1,n1,n3,n4,n5,n6 from customdbb join names on name1=n1;");
		List<String[]> list6=db.end(-1);
		Main.p("size="+list6.size());
		for(String[] a:list6){
			
			if(!"".equals(a[1])) {
				pw.println(a[0]+";"+a[1]);
				n++;
			}
			if(!"".equals(a[2])) {
				pw.println(a[0]+";"+a[2]);
				n++;
			}
			if(!"".equals(a[3])) {
				pw.println(a[0]+";"+a[3]);
				n++;
			}
			if(!"".equals(a[4])) {
				pw.println(a[0]+";"+a[4]);
				n++;
			}
			if(!"".equals(a[5])) {
				pw.println(a[0]+";"+a[5]);
				n++;
			}
		}
		Main.p("pgc.db "+n);*/
		pw.close();
		
		Main.p("makeRefDb over");
	}
	
	/**
	 * 
	 */
	public static void buildRemainingLayerNames(){
		
		
	}
	
	
	public static void analyse() throws Exception{
		Db db=new Db(Main.PATH,Main.DBNAME);	
		String sql="select t1.name,t1.ra,t1.dec,t2.name,t2.ra,t2.dec,t1.a,t1.id,t2.id from total as t1 " +
				"join refc on t1.id=refc.id1 join total as t2 on t2.id=refc.id2;"; 
		String sql2="select t1.name,t1.ra,t1.dec,t2.name,t2.ra,t2.dec,t1.a,t1.id,t2.id,t1.dbname,t2.dbname from total as t1 " +
				"join refc on t1.id=refc.id1 join total as t2 on t2.id=refc.id2 where t2.dbname='hcngc.db';"; 

		List<String[]>list=db.exec(sql, -1);
		Main.p("size="+list.size());
		for(String[] a:list){
			double ra1=Double.parseDouble(a[1]);
			double dec1=Double.parseDouble(a[2]);
			double ra2=Double.parseDouble(a[4]);
			double dec2=Double.parseDouble(a[5]);
			double dst=Main.distance(ra1, dec1, ra2, dec2)*60;
			double dim=0;
			try{
				dim=Double.parseDouble(a[6]);
			}
			catch(Exception e){}
			if(dst>Math.max(100, dim)){
				
				Main.p("clearLink("+a[7]+","+a[8]+");//"+a[0]+" "+a[3]+" "+dst+" "+a[6]);
				List<String[]>list2=db.exec ("select * from total where id="+a[7],-1);
				List<String[]>list3=db.exec ("select * from total where id="+a[8],-1);
				for(String[] a2:list2){
					for(String s:a2){
						System.out.print(s+"|");
					}
					System.out.println();
				}
				for(String[] a3:list3){
					for(String s:a3){
						System.out.print(s+"|");
					}
					System.out.println();
				}
				System.out.println();
				System.out.println();
			}
			
		}
	}
	public static void main(String[] args) throws Exception{
		
		
		
		new Ref().run();
		
		//addStarsToRef();
		
	
	}
}
