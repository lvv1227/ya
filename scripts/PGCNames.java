package com.astro.onetable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.astro.sqlite.Db;

public class PGCNames {
	public static final String PATH="/home/leonid/DSOPlanner/LargeDb/PGCLarge/";
	
	String line;
	
	public PGCNames(String line){
		this.line=line;
	}
	
	public List<String> getSql(){
		String[] a=line.split(",");
		String[]list=new String[10];//0 - pgc, 1 - ngc/ic, 2 - ugc, 3 - mcg,4 - cgcg,5 - eso,6 - ngc/ic,7 - ngc/ic,8 - ngc/ic,9 - ugc
		
		for(String s:a){
			s=s.replace("|", "").trim();
			s=s.replace(":", "");
			if(s.matches("PGC[0-9]+")){
				list[0]=process("PGC", s);
				if(list[0]!=null)
					list[0]="'"+list[0]+"'";
			}
			else if(s.matches("NGC[0-9]+[a-zA-Z]*")){
				String name=process("NGC",s);
				
				
				if(name!=null){
					name="'"+name+"'";
				}
				else
					continue;
				if(list[1]==null)
					list[1]=name;
				else if(list[6]==null)
					list[6]=name;
				else if(list[7]==null)
					list[7]=name;
				else
					list[8]=name;
				
				
			}
			else if(s.matches("IC[0-9]+[a-zA-Z]*")){
				String name=process("IC",s);
				if(name!=null){
					name="'"+name+"'";
				}
				else
					continue;
				if(list[1]==null)
					list[1]=name;
				else if(list[6]==null)
					list[6]=name;
				else if(list[7]==null)
					list[7]=name;
				else
					list[8]=name;
			}
			else if (s.matches("UGC[0-9]+[a-zA-Z]*")){
				String name=process("UGC",s);
				if(name!=null){
					name="'"+name+"'";
				}
				else
					continue;
				if(list[2]==null)
					list[2]=name;
				else 
					list[9]=name;
				
			}
			else if (s.matches("MCG.+")){
				list[3]="'"+s+"'";
			}
			else if (s.matches("CGCG.+")){
				list[4]="'"+s+"'";
			}
			else if (s.matches("ESO[0-9]+.*")){
				list[5]="'"+s+"'";
			}
		}
		
		if(list[0]==null)
			return null;
		String sql="insert into names (n1,n2,n3,n4,n5,n6) values ("+list[0]+","+list[1]+","+
		list[2]+","+list[3]+","+list[4]+","+list[5]+");";
		List<String>ll=new ArrayList<String>();
		ll.add(sql);
		
		if(list[6]!=null){
			String sql2="insert into names (n1,n2,n3,n4,n5,n6) values ("+list[0]+","+list[6]+","+
					list[2]+","+list[3]+","+list[4]+","+list[5]+");";
			ll.add(sql2);

		}
		if(list[7]!=null){
			String sql3="insert into names (n1,n2,n3,n4,n5,n6) values ("+list[0]+","+list[7]+","+
					list[2]+","+list[3]+","+list[4]+","+list[5]+");";
			ll.add(sql3);

		}
		if(list[8]!=null){
			String sql3="insert into names (n1,n2,n3,n4,n5,n6) values ("+list[0]+","+list[8]+","+
					list[2]+","+list[3]+","+list[4]+","+list[5]+");";
			ll.add(sql3);

		}
		if(list[9]!=null){
			String sql4="insert into names (n1,n2,n3,n4,n5,n6) values ("+list[0]+","+list[1]+","+
					list[9]+","+list[3]+","+list[4]+","+list[5]+");";
			ll.add(sql4);

		}
		return ll;
	}
	
	/**
	 * removes zeros inside name
	 * @param base
	 * @param name
	 * @return
	 */
	public static String process(String base,String name){
		if(name.matches(base+"[0-9]+.*")){
			String s=name.replace(base, "");
			int i=0;
			while(i<s.length()&&s.charAt(i)=='0')
				i++;
			if(i==0)return name;
			return base+s.substring(i, s.length());
		}
		return null;
	}
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		//build();
		clear();
		
	}
	/**
	 * clear unnecessary refs
	 * @throws Exception
	 */
	public static void clear() throws Exception{
		Main.p("pgcnames.db");
		while(!testClear("pgcnames.db")){		
			clearRefs("pgcnames.db");		
		}
		Main.p("pgcnamesother.db");
		while(!testClear("pgcnamesother.db")){		
			clearRefs("pgcnamesother.db");		
		}
		Main.p("pgcnamesotherngc.db");
		while(!testClear("pgcnamesotherngc.db")){		
			clearRefs("pgcnamesotherngc.db");		
		}
	}
	/**
	 * 
	 * @return true if no unnecessary refs, false if there are unnecessary refs
	 * @throws Exception
	 */
	public static boolean testClear(String dbname) throws Exception{
		Db db=new Db(PATH,dbname);
		boolean res=true;
		for(int j=2;j<=6;j++){
			List<String[]>list=db.exec("select q1.n1,q2.n1 from names as q1 join names as q2 on q1.n"+j+"=q2.n"+j+" where q1.n1!=q2.n1;",-1);
			Main.p("j="+j+" size="+list.size());
			if(list.size()!=0)res=false;
		}
		return res;
	}
	
	/**
	 * clearing fields n2 - n6 if they have the same value for different n1 value
	 * @throws Exception 
	 */
	public static void clearRefs(String dbname) throws Exception{
		Db db=new Db(PATH,dbname);
		
		for(int j=2;j<=6;j++){
			List<String[]>list=db.exec("select q1.n1,q2.n1 from names as q1 join names as q2 on q1.n"+j+"=q2.n"+j+" where q1.n1!=q2.n1;",-1);
			db.start();
			db.addCommand("begin;");
			Set<String>set=new HashSet<String>();//to keep track of same pairs 1-2, 2-1. we need to clear only 1, not 2
			for(String[] a:list){
				if(!set.contains(a[0])){
					db.addCommand("update names set n"+j+"=null where n1='"+a[0]+"';");
					set.add(a[1]);
				}
				
			}
			db.addCommand("commit;");
			db.end(-1);

		}
	}
	
	/**
	 * copying pgcnames, pgcnamesother and pgcnamesotherngc into working directory
	 * @throws IOException
	 */
	public static void copy() throws IOException{
		String[] command=new String[]{"cp",PATH+"pgcnames.db",Main.PATH };		
		Runtime runTime = Runtime.getRuntime();
		runTime.exec(command);
		
		command=new String[]{"cp",PATH+"pgcnamesother.db",Main.PATH };		
		runTime = Runtime.getRuntime();
		runTime.exec(command);
		
		command=new String[]{"cp",PATH+"pgcnamesotherngc.db",Main.PATH };		
		runTime = Runtime.getRuntime();
		runTime.exec(command);
	}
	/**
	 * making reference database with pgc name and other object name
	 * file downloaded from hyperleda for mag<=16.5
	 * @throws Exception
	 */
	
	public static void build() throws Exception{
		BufferedReader in=new BufferedReader(new InputStreamReader(new FileInputStream(new File(PATH,"names3.txt"))));
		Db db=new Db(PATH,"pgcnames.db");
		db.start();
		db.addCommand("drop table if exists names;");
		db.addCommand("create table names(n1 text,n2 text,n3 text,n4 text,n5 text,n6 text,ra real,dec real,a real,b real,mag real,pa real);");
		db.addCommand("begin;");
		String s;
		while((s=in.readLine())!=null){
			PGCNames pgc=new PGCNames(s);
			List<String>list=pgc.getSql();
			if(list==null)
				continue;
			for(String s2:list){
				db.addCommand(s2);
			}
			
		}
		
		db.addCommand("commit;");
		db.end(-1);
		in.close();
		
		addCoords();
		buildRemainingUGCNames();
		buildRemainingNGCNames();
		clear();
		copy();
		
		System.out.println("build over");
	}
	/**
	 * building db for remaining names which has UGC-core counterparts but do not have
	 * UGC - pgcnames counterparts
	 * comment in getSql:
	 * if(list[0]==null)
	 *		return null;
	 * @throws Exception
	 */
	public static void buildRemainingUGCNames() throws Exception{
		BufferedReader in=new BufferedReader(new InputStreamReader(new FileInputStream(new File(PATH,"hyperleda_ugc_names.txt"))));
		Db db=new Db(PATH,"pgcnamesother.db");
		db.start();
		db.addCommand("drop table if exists names;");
		db.addCommand("create table names(n1 text,n2 text,n3 text,n4 text,n5 text,n6 text);");
		db.addCommand("begin;");
		String s;
		while((s=in.readLine())!=null){
			
			String[] a=s.split("\\|");
			PGCNames pgc=new PGCNames(a[1]);
			List<String>list=pgc.getSql();
			if(list==null)
				continue;
			for(String s2:list){
				db.addCommand(s2);
			}
			
		}
		
		db.addCommand("commit;");
		db.end(-1);
		in.close();
		System.out.println("build over");
	}
	/**
	 * building db for remaining names which has NGC.n1-core counterparts but do not have
	 * NGC.n1 - pgcnames.db counterparts
	 
	 * @throws Exception
	 */
	public static void buildRemainingNGCNames() throws Exception{
		//file of objects for hyperleda made with command  select * from total where n1 like 'pgc%' and n1 not in (select n1 from names);
		//after total was cleared of pgc names not in pgcnames.db and core.db
		BufferedReader in=new BufferedReader(new InputStreamReader(new FileInputStream(new File(PATH,"hyperleda_ngc_names.txt"))));
		Db db=new Db(PATH,"pgcnamesotherngc.db");
		db.start();
		db.addCommand("drop table if exists names;");
		db.addCommand("create table names(n1 text,n2 text,n3 text,n4 text,n5 text,n6 text);");
		db.addCommand("begin;");
		String s;
		int i=0;
		while((s=in.readLine())!=null){
			System.out.println("i="+i);
			i++;
			String[] a=s.split("\\|");
			PGCNames pgc=new PGCNames(a[1]);
			List<String>list=pgc.getSql();
			if(list==null)
				continue;
			for(String s2:list){
				db.addCommand(s2);
			}
			
		}
		
		db.addCommand("commit;");
		db.end(-1);
		in.close();
		System.out.println("build over");
	}
	
	
	public static void addCoords() throws Exception{
		Db db=new Db(PATH,"pgcnames.db");
		
		for(int i=1;i<7;i++){
			db.start();
			db.addCommand("attach database '"+Main.PATH+"sqdb.db' as pgc;");
			//over n1 - n6!!!
			db.addCommand("select core.ra,core.dec,core.a,core.b,core.mag,core.pa, names.rowid from names join core on n"+i+"=name where core.mag<=18;");

			List<String[]>list=db.end(-1);
			db.start();
			db.addCommand("begin;");
			for(String[] a:list){
				if("".equals(a[0]))
					a[0]="null";
				if("".equals(a[1]))
					a[1]="null";
				if("".equals(a[2]))
					a[2]="null";
				if("".equals(a[3]))
					a[3]="null";
				if("".equals(a[4]))
					a[4]="null";
				if("".equals(a[5]))
					a[5]="null";
				db.addCommand("update names set ra="+a[0]+",dec="+a[1]+",a="+a[2]+",b="+a[3]+",mag="+a[4]+",pa="+a[5]+" where names.rowid="+a[6]+" and (ra is null and dec is null);");
			}
			db.addCommand("commit;");
			db.end(-1);
		}
		Main.p("over");
		
		//delete from names where ra is null;

	}
}
