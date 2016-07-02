package com.astro.onetable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.astro.hcngc.Ngc;
import com.astro.simbaddata.AstroTools;
import com.astro.sqlite.Db;
/**
 * making working full pgc database
 * and making pgc database for dso selection
 * @author leonid
 *
 */
public class PGC {
	public static final String DBNAME="pgc.db";
	
	public static final String FULL_PATH="/home/leonid/DSOPlanner/LargeDb/PGCLarge/";
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		//trim();
		//compareNgc();
		build();
		//analyse();
		//remove();
		//replaceNgc();
		//createHngcWithPgcNames();
		//create();
		//trim();
		//removeRowsNgcUgc();
		
		//build();
		//fillMissingObjsMap();
		
		//buildNameTypeDb();
		//insertTypeIntoDb();
	}
	/**
	 * building pgc.db for dso selection
	 * @throws Exception
	 */
	public static void build() throws Exception{
		fillMissingObjsMap();
		create();
		remove();
		removeRowsNgcUgc();
		
		Db db=new Db(Main.PATH,DBNAME);
		db.exec("vacuum;", -1);
		db.exec("PRAGMA user_version=2;",-1);
		db.exec("PRAGMA schema_version=2;",-1);
		Main.p("over");
	}
	
	static Map<String,String>missingobjs=new HashMap<String,String>();
	/**
	 * making map of ngc,ic,eso objs that are left after replacing names from pgcnames.
	 * The list was obtained from hyperleda. The request list was obtained with 
	 * select name1 from customdbb where not name1 like 'pgc%'; 
	 * @throws Exception 
	 */
	public static void fillMissingObjsMap() throws Exception{
		BufferedReader in=new BufferedReader(new InputStreamReader(new FileInputStream(new File(Main.PATH,"pgcmissingobjs_result.txt"))));
		String s;
		while((s=in.readLine())!=null){
			String[] a=s.split("\\|");
			missingobjs.put(a[0].trim(), a[1].trim());
		}
		in.close();
		//Main.p("missing objs="+missingobjs);
	
	}
	
	public static void create() throws Exception{
		
		//inserting galaxies from core
		Db db=new Db(Main.PATH,DBNAME);
		db.start();
		db.addCommand("attach database '"+Main.PATH+"sqdb.db' as pgc;");//pgc layer db
		db.addCommand("drop table if exists customdbb;");
		db.addCommand("drop table if exists customdbb2;");
		db.addCommand("drop index if exists name1_index;");
		db.addCommand("CREATE TABLE customdbb (_id INTEGER PRIMARY KEY AUTOINCREMENT, dec FLOAT, ra FLOAT, a FLOAT, mag FLOAT, b FLOAT, constellation INTEGER, type INTEGER, pa FLOAT, typestr TEXT, name1 TEXT, name2 TEXT, comment TEXT,typec text);");
		
		db.addCommand("insert into customdbb(ra,dec,mag,a,b,constellation,type,pa,name1,name2,typec) select ra,dec,mag,a,b,null,"+Ngc.Gxy+",pa,name,name,type from core where mag<=16 and not name like 'hd%';");
		
		db.end(-1);
		
		//setting constellation
		List<String[]>list0=db.exec("select ra,dec,rowid from customdbb;");		
		db.start();
		db.addCommand("begin;");
		for(String[]a:list0){
			double ra=Double.parseDouble(a[0]);
			double dec=Double.parseDouble(a[1]);
			db.addCommand("update customdbb set constellation="+AstroTools.getConstellation(ra, dec)+" where rowid="+a[2]+";");
		}
		db.addCommand("commit;");
		db.end();
		
		
		db.exec("delete from customdbb where not (typec='G' or typec='M2' or typec='M3' or typec='MC' or typec='MG' or typec='PG' or typec='g');");				
		db.exec("delete from customdbb where typec is null;");
		
		
		db.start();
		db.addCommand("attach database '"+Main.PATH+"pgcnames.db' as names;");
		//replacing ugc names with pgc
		db.addCommand("select q1._id,q2.n1 from customdbb as q1 join names as q2 on q1.name1=q2.n3;" );//ugc
		List<String[]>list=db.end(-1);
		db.start();
		db.addCommand("begin;");
		for(String[] a:list){
			db.addCommand("update customdbb set name1='"+a[1]+"' where _id="+a[0]+";");
		}
		db.addCommand("update customdbb set name1='PGC3406' where name1='NGC0331:';");
		db.addCommand("commit;");
		db.end(-1);
		
		db.start();
		db.addCommand("attach database '"+Main.PATH+"pgcnames.db' as names;");
		//replacing ngc names with pgc
		db.addCommand("select q1._id,q2.n1 from customdbb as q1 join names as q2 on q1.name1=q2.n2;" );//ngcic
		List<String[]>list2=db.end(-1);
		db.start();
		db.addCommand("begin;");
		for(String[] a:list2){
			db.addCommand("update customdbb set name1='"+a[1]+"' where _id="+a[0]+";");
		}
		db.addCommand("commit;");
		db.end(-1);
		
		//replacing eso names with pgc
		db.start();
		db.addCommand("attach database '"+Main.PATH+"pgcnames.db' as names;");
		db.addCommand("select q1._id,q2.n1 from customdbb as q1 join names as q2 on q1.name1=q2.n6;" );//eso
		List<String[]>list3=db.end(-1);
		db.start();
		db.addCommand("begin;");
		for(String[] a:list3){
			db.addCommand("update customdbb set name1='"+a[1]+"' where _id="+a[0]+";");
		}
		
		
		for(Map.Entry<String, String> e:missingobjs.entrySet()){
			db.addCommand("update customdbb set name1='"+"PGC"+e.getValue()+"' where name1='"+e.getKey()+"';");
		}
		db.addCommand("commit;");
		db.end(-1);
		
		//removing non-pgc names
	//	db.exec("delete from customdbb where name1 not like 'pgc%';",-1);
		
		//setting name2=name1
		db.exec("update customdbb set name2=name1;", -1);
		
		
		
		db.exec("alter table customdbb rename to customdbb2;", -1);
		db.exec("CREATE TABLE customdbb (_id INTEGER PRIMARY KEY AUTOINCREMENT, dec FLOAT, ra FLOAT, a FLOAT, mag FLOAT, b FLOAT, constellation INTEGER, type INTEGER, pa FLOAT, typestr TEXT, name1 TEXT, name2 TEXT, comment TEXT);",-1);
		db.exec("insert into customdbb(ra,dec,mag,a,b,constellation,type,pa,name1,name2) select ra,dec,mag,a,b,constellation,"+Ngc.Gxy+",pa,name1,name2 from customdbb2 order by name1 asc;",-1);
		db.exec("drop table customdbb2;",-1);
		db.exec("create index name1_index on customdbb(name1);");
		db.exec("vacuum;");
		db.exec("PRAGMA user_version=2;",-1);
		db.exec("PRAGMA schema_version=2;",-1);
		Main.p("create over");
		
	}
	public static void remove() throws Exception {
		Db db=new Db(Main.PATH,DBNAME);
		db.exec("delete from customdbb where name1 like 'SDSSJ%'",-1);
		db.exec("delete from customdbb where name1 like 'ESOLV%'",-1);
		db.exec("delete from customdbb where name1 like 'PEGASUS%'",-1);
		db.exec("delete from customdbb where name1 like 'HR%'",-1);
		db.exec("delete from customdbb where name1 like 'WINGSJ%'",-1);
		db.exec("delete from customdbb where name1 like 'SAGITTARIUS%'",-1);
		db.exec("delete from customdbb where name1 like 'SAO%'",-1);
		db.exec("delete from customdbb where name1 like 'LGG%'",-1);
		db.exec("delete from customdbb where name1 like '6dFJ%'",-1);
		db.exec("delete from customdbb where name1 like 'BD%'",-1);
		db.exec("delete from customdbb where name1 like 'HIPASSJ%'",-1);
		db.exec("delete from customdbb where name1 like 'BD%'",-1);
		db.exec("delete from customdbb where name1 like '[HG%'",-1);
		db.exec("delete from customdbb where name1 like '[RC%'",-1);
		db.exec("delete from customdbb where name1 like '3C%'",-1);
		db.exec("delete from customdbb where name1 like 'CGCG%'",-1);
		db.exec("delete from customdbb where name1 like 'TYC%'",-1);
		db.exec("delete from customdbb where name1 like '[KKH%'",-1);
		db.exec("delete from customdbb where name1 like '2MASXJ%'",-1);
		db.exec("delete from customdbb where name1 like 'HIP%'",-1);
		db.exec("delete from customdbb where name1 like 'MCG%'",-1);
		db.exec("delete from customdbb where name1 like 'LCRSB%'",-1);
		db.exec("delete from customdbb where name1 like 'EONJ%'",-1);
		db.exec("delete from customdbb where name1 like 'CNOC%'",-1);
		db.exec("delete from customdbb where name1 like 'NPM%'",-1);
		db.exec("delete from customdbb where name1 like 'SN%'",-1);
		db.exec("delete from customdbb where name1 like 'AGESJ%'",-1);
		db.exec("delete from customdbb where name1 like 'HCG%'",-1);
		db.exec("delete from customdbb where name1 like 'IISZ%'",-1);
		
		Main.p("remove over");
				
	}
	/**
	 * remove rows that corresponds to ugc and ngc
	 * @throws Exception 
	 */
	public static void removeRowsNgcUgc() throws Exception{
		Db db=new Db(Main.PATH,"hugc.db");
		db.start();
		db.addCommand("alter table customdbb add column pgc text;" );
		db.addCommand("alter table customdbb add column fromnames integer;" );

		db.addCommand("update customdbb set pgc=null;");
		db.addCommand("update customdbb set fromnames=null;");
		db.addCommand("attach database '"+Main.PATH+"pgcnames.db' as pgc;");
		//copyin pgc name from names
		db.addCommand("select q1._id,q2.n1 from customdbb as q1 join names as q2 on q1.name1=q2.n3 where q2.ra is not null;");
		List<String[]>list=db.end(-1);
		db.start();
		db.addCommand("begin;");
		for(String[] a:list){			
			db.addCommand("update customdbb set pgc='"+a[1]+"',fromnames=1 where _id=" +a[0]+";");
		}
		db.addCommand("commit;");
		db.end(-1);
		Main.p("copyin pgc name from names over");
		
		db.start();
		db.addCommand("attach database '"+Main.PATH+"sqdb.db' as pgc;");
		
		//copying remaining pgc name from core database
		db.addCommand("select q1._id,q2.name from customdbb as q1 join core as q2 on q1.name1=q2.name where q2.ra is not null and q1.fromnames is null;");
		List<String[]>list2=db.end(-1);
		db.start();
		db.addCommand("begin;");
		for(String[] a:list2){
			
			db.addCommand("update customdbb set pgc='"+a[1]+"' where _id="+a[0]+";");
		}
		db.addCommand("commit;");
		db.end(-1);
		Main.p("copying remaining pgc name from core database over");
		
		//removing ugc objects from pgc database
		Db db3=new Db(Main.PATH,DBNAME);
		db3.start();
		db3.addCommand("attach database '"+Main.PATH+"hugc.db' as ugc;");
		db3.addCommand("select q1._id from main.customdbb as q1 join ugc.customdbb as q2 on q1.name1=q2.pgc;");
		
		List<String[]>list3=db3.end(-1);
		db3.start();
		db3.addCommand("begin;");
		for(String[] a:list3){
			db3.addCommand("delete from customdbb where _id="+a[0]+";");
		}
		db3.addCommand("commit;");
		db3.end(-1);
		Main.p("removing ugc objects from pgc database over");
		
		
		Db db4=new Db(Main.PATH,DBNAME);
		db4.start();
		db4.addCommand("attach database '"+Main.PATH+"total.db' as total;");
		db4.addCommand("select q1._id from customdbb as q1 join total as q2 on q1.name1=q2.n1 where dbname='hcngc.db';");
		
		List<String[]>list4=db4.end(-1);
		db4.start();
		db4.addCommand("begin;");
		for(String[] a:list4){
			db4.addCommand("delete from customdbb where _id="+a[0]+";");
		}
		db4.addCommand("commit;");
		db4.end(-1);
		
		Main.p("removeRowsNgcUgc over" );
		
	}
	public static void replaceNgc() throws Exception{
		Db db=new Db(Main.PATH,DBNAME);
		db.exec("alter table customdbb add column ngcref text;",-1);
		db.start();
		db.addCommand("attach database '"+Main.PATH+"total.db' as t;");
		db.addCommand("update customdbb set ngcref=(select id from total where name=name1 and dbname='hcngc.db' and (name1 like 'ngc%' or name1 like 'ic%');");
		/*
		update customdbb set ugcref=(select id from prun9 where n9=name1) where ugcref is null
		create table prun9 as select n9,id from total where dbname='hugc.db' and n9 like 'pgc%';
		*
		*
		*
		*/
		db.end(-1);
		
	}
	public static void analyse() throws Exception{
		Db db=new Db(Main.PATH,DBNAME);
		List<String[]>list=db.exec("select name1 from customdbb;" , -1);
		Set<String>set=new HashSet<String>();
		for(String[] a:list){
			Pattern p=Pattern.compile("[0-9[\\[]]*[a-zA-Z]+");
			Matcher m=p.matcher(a[0]);
			if(m.find()){
				set.add(a[0].substring(m.start(),m.end()));
			}
		}
		Main.p(""+set);
		
	}
	/**
	 * trimming sqdb.db
	 * @throws Exception
	 */
	public static void trim() throws Exception{
		Db db=new Db(Main.PATH,"sqdb.db");
		
		for(int i=0;i<400;i++){
			
			String sql="select rowid,name,a,b,pa,mag from core where rowid>="+(i*10000)+" and rowid<"+(i+1)*10000+";";
			List<String[]>list=db.exec(sql, -1);
			if(list.size()==0)break;
			db.start();
			db.addCommand("begin;");

			for(String[] a:list){
				String name=a[1];
				
			/*	double ad=Double.NaN;
				try{
					ad=Double.parseDouble(a[2]);
				}
				catch(Exception e){}
				if(Double.isNaN(ad)){
					db.addCommand("update core set a=null where rowid="+a[0]+";");
				}
				
				double b=Double.NaN;
				try{
					b=Double.parseDouble(a[3]);
				}
				catch(Exception e){}
				if(Double.isNaN(b)){
					db.addCommand("update core set b=null where rowid="+a[0]+";");
				}
				
				double pa=Double.NaN;
				try{
					pa=Double.parseDouble(a[4]);
				}
				catch(Exception e){}
				if(Double.isNaN(pa)){
					db.addCommand("update core set pa=null where rowid="+a[0]+";");
				}
				
				
				
				double mag=Double.NaN;
				try{
					mag=Double.parseDouble(a[5]);
				}
				catch(Exception e){}
				if(Double.isNaN(mag)){
					db.addCommand("update core set mag=null where rowid="+a[0]+";");
				}
				*/
				
				String name2=null;
				if(name.startsWith("PGC"))
					name2=process("PGC", name);
				if(name.startsWith("UGC"))
					name2=process("UGC", name);
				if(name.startsWith("IC"))
					name2=process("IC", name);
				if(name.startsWith("NGC"))
					name2=process("NGC", name);
				if(name2!=null)
					db.addCommand("update core set name='"+name2+"' where rowid="+a[0]+";");

			}
			db.addCommand("commit;");
			db.end(-1);
			Main.p("i="+i);
		}
		Main.p("trim over");
	}
	
	
	/**
	 * removes zeros inside name
	 * @param base
	 * @param name
	 * @return
	 */
	public static String process(String base,String name){
		if(name.matches(base+"[0-9]+[a-zA-Z]*")){
			String s=name.replace(base, "");
			int i=0;
			while(i<s.length()&&s.charAt(i)=='0')
				i++;
			if(i==0)return null;
			return base+s.substring(i, s.length());
		}
		return null;
	}
	
	public static void compareNgc() throws Exception{
		Set<String>set=new HashSet<String>();
		Db db=new Db(Main.PATH,Main.DBNAME);	
		
		//name ngc - pgc
		db.start();
		
		db.addCommand("attach database '"+Main.PATH+"sqdb.db' as pgc;");
		String sql="select q1.name,q2.name,q1.ra,q1.dec,q2.ra,q2.dec,q1.a from TOTAL as q1 join pgc.core as q2 on q1.name=q2.name where q1.dbname='hugc.db';";//and q1.dst<3 and q2.dst<3
		db.addCommand(sql);
		List<String[]>list=db.end(-1);

		for(String[] a:list){
			double ra1=Double.parseDouble(a[2]);
			double dec1=Double.parseDouble(a[3]);
			double ra2=Double.parseDouble(a[4]);
			double dec2=Double.parseDouble(a[5]);
			double dst=Main.distance(ra1, dec1, ra2, dec2)*60;
			if(dst>3){
				String s="nn "+a[0]+","+a[1]+" dst="+dst+" a="+a[6];
				Main.p(s);
				//p(s);

			}
		}


		//second name - first n
		for(int j=1;j<=12;j++){
			db.start();			
			db.addCommand("attach database '"+Main.PATH+"sqdb.db' as pgc;");

			
			String sql2="select q1.name,q2.name,q1.ra,q1.dec,q2.ra,q2.dec,q1.a from TOTAL as q1 join pgc.core as q2 on q1.n"+j+"=q2.name where q1.dbname='hugc.db';";//and q1.dst<3 and q2.dst<3
			db.addCommand(sql2);
			List<String[]> list2=db.end(-1);

			for(String[] a:list2){
				double ra1=Double.parseDouble(a[2]);
				double dec1=Double.parseDouble(a[3]);
				double ra2=Double.parseDouble(a[4]);
				double dec2=Double.parseDouble(a[5]);
				double dst=Main.distance(ra1, dec1, ra2, dec2)*60;
				if(dst>3){
					String s="fn "+a[0]+","+a[1]+" dst="+dst+" a="+a[6];
					Main.p(s);
					//p(s);

				}
			}
			Main.p("j="+j);

		}


	
		
	}
	
	
	public static void createHngcWithPgcNames() throws Exception{
		Db db=new Db(Main.PATH,"total.db");
		db.start();
	//	db.addCommand("attach database '"+Main.PATH+"pgcnames.db' as names;");
		db.addCommand("drop table if exists ngcnames;");
		db.addCommand("create table ngcnames (id integer,name text,n1 text,n2 text,n3 text,n4 text,n5 text,n6 text,ra real,dec real,a real);");
		db.addCommand("begin;");
		List<String[]> list=db.exec("select id,name,n1,n2,n3,n4,n5,n6,n7,n8,n9,n10,n11,n12,ra,dec,a from total where dbname='hcngc.db';", -1);
		for(String[] a:list){
			String[] names=new String[6];
			
			
			names[1]=a[1];
			
			for(int i=1;i<=12;i++){
				String name=a[i+1];
				if(name.contains("PGC")){
					names[0]=name;
				}				
				else if(name.contains("UGC")){
					names[2]=name;
				}
				else if(name.contains("MCG")){
					names[3]=name;
				}
				else if(name.contains("CGCG")){
					names[4]=name;					
				}
				else if(name.contains("ESO")){
					names[5]=name;
				}
			}
			for(int i=0;i<names.length;i++){
				if(names[i]!=null){
					names[i]="'"+names[i]+"'";
				}
				
			}
			String sql="insert into ngcnames values ("+a[0]+",'"+a[1]+"',"+names[0]+","+
			names[1]+","+names[2]+","+names[3]+","+names[4]+","+names[5]+","+a[14]+","+a[15]+","+a[16]+");";
			db.addCommand(sql);
		}
		db.addCommand("commit;");
		db.end(-1);
		
		
		
		
		Main.p("over");
		
	}
	/**
	 * file name - type downloaded from hyperleda
	 * @throws Exception
	 */
	public static void buildNameTypeDb() throws Exception{
		Db db=new Db(FULL_PATH,"nt.db");
		db.exec("drop table if exists nt;");
		db.exec("create table nt (name text,type text);");
		db.start();
		db.addCommand("begin;");
		BufferedReader in=new BufferedReader(new InputStreamReader(new FileInputStream(new File(FULL_PATH,"nametype.txt"))));
		String s;
		while((s=in.readLine())!=null){
			String[] a=s.split("\\|");
			String name=a[0].trim();
			String type=a[1].trim();
			String name2=null;
			if(name.startsWith("PGC"))
				name2=process("PGC", name);
			if(name.startsWith("UGC"))
				name2=process("UGC", name);
			if(name.startsWith("IC"))
				name2=process("IC", name);
			if(name.startsWith("NGC"))
				name2=process("NGC", name);
			if(name2!=null)
				name=name2;
			db.addCommand("insert into nt values ('"+name+"','"+type+"');");
		}
		in.close();
		db.addCommand("commit;");
		db.end();

	}
	/**
	 * inserting type into main db
	 * table ntt created as
	 * create table ntt as select q1.rowid as cid,q2.rowid as nid from core as q1 join nt as q2 on q1.name=q2.name;
	 * @throws Exception 
	 */
	public static void insertTypeIntoDb() throws Exception{
		Db db=new Db(Main.PATH,"sqdb.db");
		db.exec("alter table core add column type text;");
		db.start();
		db.addCommand("attach database '"+Main.PATH+"nt.db' as nt;");
		db.addCommand("select core.rowid,nt.type from core join ntt join nt on core.rowid=ntt.cid and ntt.nid=nt.rowid;");
		List<String[]>list=db.end();
		Main.p("size="+list.size());
		db.start();
		db.addCommand("begin;");
		for(String[] a:list){
			db.addCommand("update core set type='"+a[1]+"' where rowid="+a[0]+";");
		}
		db.addCommand("commit;");
		db.end();
	}

}
