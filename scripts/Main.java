package com.astro.onetable;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.astro.simbaddata.AstroTools;
import com.astro.simbaddata.AstroTools.RaDecRec;
import com.astro.sqlite.Db;

public class Main {
	public static String PATH="/home/leonid/DSOPlanner/LargeDb/CrossRef/Simbad/total/";
	public static final String DBNAME="total.db";
	
	
	static class Names{
		List<String>names=new ArrayList<String>();
		public void add(String name){
			names.add(name);			
		}
		public Names(List<String> names){
			this.names=names;
		}
		public List<String>get(){
			return names;
		}
		
	}
	static class Obj{
		String name;
		RaDecRec rec;
		double mag=Double.NaN;
		public Obj(String name, RaDecRec rec) {
			super();
			this.name = name;
			this.rec = rec;
		}
		
		public Obj(String name, RaDecRec rec,double mag) {
			this(name,rec);
			this.mag=mag;
		}
		
	}
	static class Obj2{
		String name;
		Names names;
		public Obj2(String name, Names names) {
			super();
			this.name = name;
			this.names = names;
		}
	}
	
	static class DsoDb{
		String dbname;
		String table;
		String simbad_coord_file;
		protected String simbad_name_file;
		
		

		public DsoDb(String dbname, String table, String simbad_coord_file,
				String simbad_name_file) {
			super();
			this.dbname = dbname;
			this.table = table;
			this.simbad_coord_file = simbad_coord_file;
			this.simbad_name_file = simbad_name_file;
		}
		boolean name2main=false;
		/**
		 * name2 will be used for filling total database
		 * use for messier and caldwell databases
		 */
		public void setName2asMain(){
			name2main=true;
		}
		public void setNameFile(String name_file){
			this.simbad_name_file = name_file;
		}
		public void addData()throws Exception{
			String field="name1";
			if(name2main){
				field="name2";
			}
			Db db=new Db(PATH,DBNAME);
			db.start();
			db.addCommand("attach database '"+PATH+ dbname+"' as ngc;");
			db.addCommand("insert into TOTAL(_id,dbname,name,ra,dec,a) select _id,'"+dbname+"',upper("+field+"),ra,dec,a from ngc."+table+";");			
			db.end(-1);
			p("added data for "+dbname);
		}
		public void addSimbadCoords() throws Exception{
			Db db=new Db(PATH,DBNAME);
			db.start();
			db.addCommand("begin;");
			List<Obj> list=Parsers.loadSimbadRaDec(new File(PATH,simbad_coord_file));
			for(Obj e:list){
				AstroTools.RaDecRec rec=e.rec;
				String sql="update TOTAL set ras="+rec.ra+",decs="+rec.dec+" where name='"+e.name.toUpperCase()+"' and dbname="+"'"+dbname+"';";
				db.addCommand(sql);
			}
			db.addCommand("commit;");
			db.end(-1);
			p("added simbad coords for "+dbname);
		}
		public void addSimbadCoords(Map<String,String> rep) throws Exception{
			Db db=new Db(PATH,DBNAME);
			db.start();
			db.addCommand("begin;");
			List<Obj> list=Parsers.loadSimbadRaDec(new File(PATH,simbad_coord_file),rep);
			for(Obj e:list){
				AstroTools.RaDecRec rec=e.rec;
				String sql="update TOTAL set ras="+rec.ra+",decs="+rec.dec+" where name='"+e.name.toUpperCase()+"' and dbname="+"'"+dbname+"';";
				db.addCommand(sql);
			}
			db.addCommand("commit;");
			db.end(-1);
			p("added simbad coords for "+dbname);
		}
		/**
		 * 
		 * @param filename text file with original db name;simbad name pairs
		 * @return map of back replacement (to get the original my db name from the modified name submitted to simbad). key=simbad name, value=my db name
		 */
		public Map<String,String>fillReplacementMap(String filename)throws Exception{
			BufferedReader in=new BufferedReader(new InputStreamReader(new FileInputStream(new File(PATH,filename))));
			String s;
			Map<String,String>map=new HashMap<String, String>();
			while((s=in.readLine())!=null){
				String[] arr=s.split(";");
				if(arr.length>2)throw new Exception ("Invalid string format at "+s);
				map.put(arr[1], arr[0]);
			}
			in.close();
			return map;
		}
		public void addSimbadNames() throws Exception{
			addSimbadNames(null);
		}
		/**
		 * 
		 * @param map - replacement map
		 * @throws Exception
		 */
		public void addSimbadNames(Map<String,String>map) throws Exception{
			List<Obj2> ll=Parsers.processSimbadNames(PATH, simbad_name_file, map);
			Db db=new Db(PATH,DBNAME);
			db.start();
			db.addCommand("begin;");
			for(Obj2 obj:ll){
				Names names=obj.names;
				String nameset="";
				int i=1;
				for(String s:names.get()){
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
					db.addCommand(sql);
				}
			}
			db.addCommand("commit;");
			db.end(-1);
			p("added simbad names for "+dbname);
		}
		/**
		 * distance to simbad objects
		 * @throws Exception 
		 */
		public void addDst() throws Exception{
			Db db=new Db(PATH,DBNAME);
			List<String[]> list=db.exec("select id,ra,dec,ras,decs from TOTAL where dbname='"+dbname+"';",  5);
			db.start();
			db.addCommand("begin;");
			for(String[] a:list){
				try{
					int id=Integer.parseInt(a[0]);
					double ra=Double.parseDouble(a[1]);
					double dec=Double.parseDouble(a[2]);
					double ras=Double.parseDouble(a[3]);
					double decs=Double.parseDouble(a[4]);
					double dst=60*distance(ra, dec, ras, decs);//im minutes
					String sql="update TOTAL set dst="+dst+" where id="+id+" and dbname="+"'"+dbname+"';";
					db.addCommand(sql);
				}
				catch(Exception e){
					continue;
				}
			}
			db.addCommand("commit;");
			db.end(-1);
			p("added distance for "+dbname);
		}
		public void run() throws Exception{
			addData();
			addSimbadCoords();
			addSimbadNames();
			addDst();
		}
		
	}
	
	static class NgcIc extends DsoDb{
		public NgcIc (String dbname, String table,
				String name_file) {
			super(dbname,table,null,name_file);
			
		}
		@Override
		public void addSimbadCoords(){
			
		}
		@Override
		public void addSimbadNames() throws Exception{
			Db db=new Db(PATH,DBNAME);
			db.exec(".read "+PATH+simbad_name_file, -1);
			p("added names for "+dbname);
		}
		@Override
		public void addDst(){
			
		}
		
	}
	
	static class Barnard extends DsoDb{
		String replacement_file;
		public Barnard (String dbname, String table,String simbad_coord_file,
				String simbad_name_file,String replacement_file) {
			super(dbname,table,simbad_coord_file,simbad_name_file);
			this.replacement_file=replacement_file;
			
		}
		@Override
		public void run() throws Exception{
			addData();
			addSimbadCoords();
			addSimbadNames(fillReplacementMap(replacement_file));
			addDst();
		}
		@Override
		public void addSimbadCoords() throws Exception{
			Db db=new Db(PATH,DBNAME);
			db.start();
			db.addCommand("begin;");
			List<Obj> list=Parsers.loadSimbadRaDec(new File(PATH,simbad_coord_file));
			for(Obj e:list){
				AstroTools.RaDecRec rec=e.rec;
				String sql="update TOTAL set ras="+rec.ra+",decs="+rec.dec+" where name='"+e.name.replace("Barnard", "B")+"' and dbname="+"'"+dbname+"';";
				db.addCommand(sql);
			}
			db.addCommand("commit;");
			db.end(-1);
			p("added simbad coords for "+dbname);
		}
	}
	/**
	 * uses replacement for names
	 * @author leonid
	 *
	 */
	static class Rep extends DsoDb{
		String replacement_file;
		public Rep (String dbname, String table,String simbad_coord_file,
				String simbad_name_file,String replacement_file) {
			super(dbname,table,simbad_coord_file,simbad_name_file);
			this.replacement_file=replacement_file;
			
		}
		@Override
		public void run() throws Exception{
			Map<String,String>rep=fillReplacementMap(replacement_file);
			addData();
			addSimbadCoords(rep);
			addSimbadNames(rep);
			addDst();
		}
		
	}
	
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args)throws Exception {
		
		/*createTOTALTable();
		DsoDb ngcic=new DsoDb("ngcicNEW.db","ngcic","simbad_ngcic_radec_trunc.txt","simbad_ngcic_truncated.txt");
		ngcic.run();
		
		DsoDb ugc=new DsoDb("ugcnew2.db","customdbb","simbad_ugc_ra_dec_trunc.txt","simbad_ugc_trunc.txt");
		ugc.run();
		p("over");*/
		//wrongUGC();
		//compareNames();
		
		//createTOTALTable();
		/*NgcIc ngcic=new NgcIc("ngcicNEW.db","ngcic","ngc_names_out.txt");
		ngcic.run();
		ngcic.setNameFile("ic_names_out.txt");
		ngcic.addSimbadNames();*/
		
		
		/*DsoDb ugc=new DsoDb("ugcnew2.db","customdbb","simbad_ugc_ra_dec_trunc.txt","simbad_ugc_trunc.txt");
		ugc.run();*/
		
		//wrongUGC();
	//	DsoDb lbn=new DsoDb("hlbn.db","customdbb","Simbad_lbn_ra_dec_trunc.txt","Simbad_lbn_trunc.txt");
	//	lbn.run();
		
		//LBN - NGC
	/*	String sql="select q1.name,q2.name,q1.ra,q1.dec,q2.ra,q2.dec,q1.a from TOTAL as q1 join TOTAL as q2 on q1.n4=q2.name " +
				"where q2.name like 'lbn%' and (q1.name like 'ngc%' or q1.name like 'ic%');";//and q1.dst<3 and q2.dst<3
		String sql2="select q1.name,q2.name,q1.ra,q1.dec,q2.ra,q2.dec,q1.a from TOTAL as q1 join TOTAL as q2 on q1.name=q2.n3 " +
				"where q2.name like 'lbn%' and (q1.name like 'ngc%' or q1.name like 'ic%');";//and q1.dst<3 and q2.dst<3

		
		wrong(sql,15);*/
		
		
		//diffLinks("(q1.name like 'ngc%' or q1.name like 'ic%')", "q2.name like 'lbn%'");
		//wrong("(q1.name like 'ngc%' or q1.name like 'ic%')","q2.name like 'lbn%'",15,1);
		//diffLinks("(q1.name like 'ngc%' or q1.name like 'ic%')", "q2.name like 'ugc%'");
		
		
		
		//build();
		//clearUGCLinks();
		
		//getClearList("where dbname='hlbn.db' and dst>max(15,a)");
		//clearLBNLinks();
		//wrong("(q1.name like 'ngc%' or q1.name like 'ic%')","(q2.name like 'ngc%' or q2.name like 'ic%')",1,0.2);
	//	compareWithItself("(q1.name like 'ngc%' or q1.name like 'ic%')","(q2.name like 'ngc%' or q2.name like 'ic%')");
	//	compareWithItself("(q1.name like 'lbn%')","(q2.name like 'lbn%')");
	//	compareWithItself("(q1.name like 'ngc%' or q1.name like 'ic%')","(q2.name like 'ngc%' or q2.name like 'ic%')");

		//clearNGCNGCLinks();
		//getClearList("where dbname='hldn.db' and dst>max(15,a)");
		//wrong("(q1.name like 'ngc%' or q1.name like 'ic%')","q2.name like 'lbd%'",15,1);
	
		//getClearList("where dbname='barnard.db' and dst>max(15,a)");
		//clearBarnardLinks();
		//wrong("(q1.name like 'ldn%')","q2.name like 'b%'",15,1);
		
		//DsoDb pk=new Rep("hpk.db","customdbb","simbad_data_pk_ra_dec_trunc.txt","simbad_pk_trunc","pk_replacement");
		//pk.run();
		
		//getClearList("where dbname='hpk.db' and dst>max(1,coalesce(a,0)/2)");
		
		//wrong("(q1.name like 'ngc%' or q1.name like 'ic%')","q2.name like 'pk%'",1.5,0.5);
		//clearPKLinks();
		//getCorrectPKNamesInNgc();
		//setCorrectPKNamesInNgc();
	//	new Ref().run();
	//	clearNGCNGCLinks();
		//clearUGCLinks();
		
		
		
		/*Rep sac2=new Rep("hsac.db","customdbb",null,"simbad_sac_truncated_pn.txt","sac_replacement_pn.txt");
		Map<String,String>rep=sac2.fillReplacementMap(sac2.replacement_file);
		sac2.addSimbadNames(rep);*/
		
		//Rep sac=new Rep("hsac.db","customdbb","simbad_sac_data_ra_dec_trunc.txt","simbad_sac_truncated_modified.txt","sac_replacement_main.txt");
		//sac.run();

		
		
		//wrongWithId("q1.dbname='hlbn.db'","q2.dbname='hsac.db' and (q2.name like 'lbn%')",0,0);
		
		//clearSACNames();
		
	/*	clearBarnardLinks();
		clearLBNLinks();
		clearLDNLinks();
		clearNGCNGCLinks();*/
	//	clearSACLinks();
		
		//Rep sac=new Rep("hsac.db","customdbb","simbad_sac_data_ra_dec_trunc.txt","simbad_sac_truncated_modified.txt","sac_replacement_main.txt");
		//sac.run();
		//clearSACNames();
		//clearSACLinks();
		//DsoDb sh2=new DsoDb("sh2.db","customdbb","simbad_sh2_ra_dec_trunc.txt","simbad_sh2_trunc.txt");
		//sh2.run();
		
		//wrong("q1.dbname='sh2.db'","q2.dbname='hcngc.db'",15,1);
		//clearSACLinks();
		
	/*	NgcIc ngcic=new NgcIc("hcngc.db","ngcic","ngc_names_out.txt");
		//ngcic.run();
		ngcic.addSimbadNames();
		ngcic.setNameFile("ic_names_out.txt");
		ngcic.addSimbadNames();*/
		
		
		//wrongNgcPgc();
		//wrongUgcPgc();
		//NgcIc ngcic=new NgcIc("hcngc.db","ngcic","ngc_names_out.txt");
		//ngcic.rearrangeNames();
		//replaceUgcWithPgc();
		//wrongNgcPgc();
		//clearNgcLinks();
		//clearUgcLinks();
		
		//compareWithItself("(q1.name like 'ngc%' or q1.name like 'ic%')","(q2.name like 'ngc%' or q2.name like 'ic%')");
		//clearNGCNGCLinks();
		//compareWithItself("q1.dbname='hcngc.db'","q2.dbname='hcngc.db'");
		//wrongNgcPgc2();
		//clearRemainingNgcLinks();
		//build();
		//getClearList("where dbname='hpk.db' and dst>max(1,coalesce(a,0)/2)");
		//		wrong("(q1.name like 'ngc%' or q1.name like 'ic%')","q2.name like 'pk%'",1.5,0.5);

		//clearRefLinks();
		
	//	build();
		
	//	DsoDb mc=new DsoDb("mc.db","customdbb",null,null);
	//	mc.addData();
		//clearSACNames();
		

		DsoDb m=new DsoDb("m.db","customdbb",null,null);//messier
		m.setName2asMain();
		m.addData();
		DsoDb c=new DsoDb("c.db","customdbb",null,null);//caldwell
		c.setName2asMain();
		c.addData();
		
		p("over");
	}
	
	
	
	
	/**
	 * method for building final db
	 * @throws Exception 
	 */
	public static void build() throws Exception{
		PATH="/home/leonid/DSOPlanner/LargeDb/CrossRef/Simbad/total/";
		File f=new File(PATH,DBNAME);
		f.delete();
		createTOTALTable();
		NgcIc ngcic=new NgcIc("hcngc.db","ngcic","ngc_names_out.txt");
		ngcic.run();
		ngcic.setNameFile("ic_names_out.txt");
		ngcic.addSimbadNames();
		rearrangeNames("hcngc.db");
		clearNgcLinks();	
		clearRemainingNgcLinks();
		setCorrectPKNamesInNgc();
		
		//replaceUgcWithPgc();
		DsoDb ugc=new DsoDb("hugc.db","customdbb","simbad_ugc_ra_dec_trunc.txt","simbad_ugc_trunc.txt");
		ugc.run();		
		rearrangeNames("hugc.db");		
		clearUgcLinks();
		
		
		DsoDb lbn=new DsoDb("hlbn.db","customdbb","Simbad_lbn_ra_dec_trunc.txt","Simbad_lbn_trunc.txt");
		lbn.run();		
		clearLBNLinks();
		
		DsoDb ldn=new DsoDb("hldn.db","customdbb","simbad_ldn_ra_dec_trunc.txt","simbad_ldn_trunc.txt");
		ldn.run();
		clearLDNLinks();
		
		DsoDb barnard=new Barnard("barnard.db","customdbb","simbad_barnard_ra_dec_trunc.txt","simbad_barnard_trunc.txt","barnard_replacement.txt");
		barnard.run();
		clearBarnardLinks();
		
		DsoDb pk=new Rep("hpk.db","customdbb","simbad_data_pk_ra_dec_trunc.txt","simbad_pk_trunc","pk_replacement");
		pk.run();
		clearPKLinks();
		
		DsoDb abell=new DsoDb("abell.db","customdbb","simbad_abell_ra_dec_trunc.txt","simbad_abell_data_trunc.txt");
		abell.run();
		
		DsoDb hcg=new DsoDb("hcg.db","customdbb","simbad_hcg_ra_dec_trunc.txt","simbad_hcg_data_trunc.txt");
		hcg.run();
		
		Rep sac=new Rep("hsac.db","customdbb","simbad_sac_data_ra_dec_trunc.txt","simbad_sac_truncated_modified.txt","sac_replacement_main.txt");
		sac.run();
		clearSACNames();
		clearSACLinks();
		
		DsoDb sh2=new DsoDb("sh2.db","customdbb","simbad_sh2_ra_dec_trunc.txt","simbad_sh2_trunc.txt");
		sh2.run();
		clearShLinks();
		
		DsoDb m=new DsoDb("m.db","customdbb",null,null);//messier
		m.setName2asMain();
		m.addData();
		DsoDb c=new DsoDb("c.db","customdbb",null,null);//caldwell
		c.setName2asMain();
		c.addData();
		
		clearRefLinks();
		new Ref().run();
	}
	
	public static void clearRefLinks() throws Exception{


		clearLink(402,2231);//NGC436 NGC2368 5971.404634510835 5.0
		//402|hcngc.db|402|NGC436|COLLINDER11|MELOTTE6|OCL320|LUND320|||||||||1.26602777777778|58.8172222222222|||5.0||1322.0|
		//2231|hcngc.db|2231|NGC2368|COLLINDER138|OCL571|LUND320||||||||||7.35175|-10.3716666666667|||5.0||1322.0|


		clearLink(388,421);//NGC422 NGC456 93.50906983951893 
		//388|hcngc.db|388|NGC422||KRON65|LINDSAY87|||ESO051-SC022|||||||1.157|-71.7661111111111|||||1313.0|
		//421|hcngc.db|421|NGC456||KRON65|LINDSAY94|||ESO029-SC038|||||||1.229|-73.2905555555556|||15.0||1313.0|


		clearLink(34877,34880);//NGC336 MCG-03-03-011 26.5588714862879 0.7
		//34877|hsac.db|1836|NGC336|||||||||||||0.966666666666667|-18.3833332061768|0.982778866666667|-18.742656|0.7|25.5689900900467|8.0|
		//34880|hsac.db|1839|MCG-03-03-011|NGC336|PGC3526|ESO541-4|IRASF00565-1900|||||||||0.98|-18.783332824707|0.982778866666667|-18.742656|3.0|3.4006373277884|8.0|


		clearLink(421,388);//NGC456 NGC422 93.50906983951893 15.0
		//421|hcngc.db|421|NGC456||KRON65|LINDSAY94|||ESO029-SC038|||||||1.229|-73.2905555555556|||15.0||1313.0|
		//388|hcngc.db|388|NGC422||KRON65|LINDSAY87|||ESO051-SC022|||||||1.157|-71.7661111111111|||||1313.0|


		clearLink(34880,313);//MCG-03-03-011 NGC336 26.06024058747663 3.0
		//34880|hsac.db|1839|MCG-03-03-011|NGC336|PGC3526|ESO541-4|IRASF00565-1900|||||||||0.98|-18.783332824707|0.982778866666667|-18.742656|3.0|3.4006373277884|8.0|
		//313|hcngc.db|313|NGC336|PGC3470||||||||||||0.967555555555556|-18.3866666666667|||0.7||8.0|


		clearLink(34880,34877);//MCG-03-03-011 NGC336 26.5588714862879 3.0
		//34880|hsac.db|1839|MCG-03-03-011|NGC336|PGC3526|ESO541-4|IRASF00565-1900|||||||||0.98|-18.783332824707|0.982778866666667|-18.742656|3.0|3.4006373277884|8.0|
		//34877|hsac.db|1836|NGC336|||||||||||||0.966666666666667|-18.3833332061768|0.982778866666667|-18.742656|0.7|25.5689900900467|8.0|


		clearLink(1574,40917);//NGC1673 SL17 3994.7645250615774 1.0
		//1574|hcngc.db|1574|NGC1673||SL17|KMH90-43|||ESO055-SC034|||||||4.71102777777778|-69.8213888888889|||1.0||657.0|
		//40917|hsac.db|7876|SL17|||||||||||||16.8833333333333|-43.5833320617676|16.8833333333333|-43.6|15.0|1.00007629421945|657.0|


		clearLink(1553,38928);//NGC1651 SL7 4041.682771705471 3.0
		//1553|hcngc.db|1553|NGC1651||SL7|KMH90-20|||ESO055-SC030|||||||4.62575|-70.5855555555555|||3.0||656.0|
		//38928|hsac.db|5887|SL7|||||||||||||16.03|-41.8666648864746|16.0433333333333|-41.707|60.0|13.1085007216464|656.0|

		clearLink(2231,402);//NGC2368 NGC436 5971.404634510835 5.0
		//2231|hcngc.db|2231|NGC2368|COLLINDER138|OCL571|LUND320||||||||||7.35175|-10.3716666666667|||5.0||1322.0|
		//402|hcngc.db|402|NGC436|COLLINDER11|MELOTTE6|OCL320|LUND320|||||||||1.26602777777778|58.8172222222222|||5.0||1322.0|


		clearLink(2266,40644);//NGC2409 BOCHUM4 16.889243901341292 2.3
		//2266|hcngc.db|2266|NGC2409|BOCHUM4|LUND1128|||||||||||7.52686111111111|-17.1905555555556|||2.3||57.0|
		//40644|hsac.db|7603|BOCHUM4|NGC2409|FIRSSE 213|C0728-168|Bochum4|||||||||7.51666666666667|-16.9500007629395|7.52693333333333|-17.193|23.0|17.0468366950638|57.0|


		clearLink(2116,2112);//NGC2243 NGC2239 2173.727409194277 5.0
		//2116|hcngc.db|2116|NGC2243|COLLINDER98|MELOTTE46||LUND222||ESO426-SC016|||||||6.49291666666667|-31.2813888888889|||5.0||832.0|
		//2112|hcngc.db|2112|NGC2239|LUND222|OCL515|COLLINDER99|MELOTTE47|CED76B||||||||6.53211111111111|4.94305555555556|||24.0||832.0|


		clearLink(2116,2117);//NGC2243 NGC2244 2173.727409194277 5.0
		//2116|hcngc.db|2116|NGC2243|COLLINDER98|MELOTTE46||LUND222||ESO426-SC016|||||||6.49291666666667|-31.2813888888889|||5.0||832.0|
		//2117|hcngc.db|2117|NGC2244|LUND222|OCL515|COLLINDER99|MELOTTE47|CED76B||||||||6.53211111111111|4.94305555555556|||24.0||832.0|


		clearLink(2117,2116);//NGC2244 NGC2243 2173.727409194277 24.0
		//2117|hcngc.db|2117|NGC2244|LUND222|OCL515|COLLINDER99|MELOTTE47|CED76B||||||||6.53211111111111|4.94305555555556|||24.0||832.0|
		//2116|hcngc.db|2116|NGC2243|COLLINDER98|MELOTTE46||LUND222||ESO426-SC016|||||||6.49291666666667|-31.2813888888889|||5.0||832.0|


		clearLink(2112,2116);//NGC2239 NGC2243 2173.727409194277 24.0
		//2112|hcngc.db|2112|NGC2239|LUND222|OCL515|COLLINDER99|MELOTTE47|CED76B||||||||6.53211111111111|4.94305555555556|||24.0||832.0|
		//2116|hcngc.db|2116|NGC2243|COLLINDER98|MELOTTE46||LUND222||ESO426-SC016|||||||6.49291666666667|-31.2813888888889|||5.0||832.0|


		clearLink(33575,42541);//VDBH81 PISMIS16 3578.2846722601516 6.0
		//33575|hsac.db|534|VDBH81|||||||||||||17.0666666666667|-51.0833320617676|17.0666666666667|-51.08|6.0|0.199923711887978|721.0|
		//42541|hsac.db|9500|PISMIS16|OCl790|C0949-529|Pismis16|VDBH81|||||||||9.855|-53.1833343505859|9.85446666666667|-53.167|1.5|1.02141553862997|721.0|




		clearLink(3474,5845);//NGC3680 NGC6134 3024.7121124139053 12.0
		//3474|hcngc.db|3474|NGC3680|COLLINDER247||MELOTTE106|LUND588|OCL823|ESO265-SC032|||||||11.4269722222222|-43.25|||12.0||1320.0|
		//5845|hcngc.db|5845|NGC6134|COLLINDER303||MELOTTE106|LUND703|OCL968|ESO226-SC009|||||||16.4629166666667|-49.1511111111111|||6.0||1320.0|

		clearLink(38928,1553);//SL7 NGC1651 4041.682771705471 60.0
		//38928|hsac.db|5887|SL7|||||||||||||16.03|-41.8666648864746|16.0433333333333|-41.707|60.0|13.1085007216464|656.0|
		//1553|hcngc.db|1553|NGC1651||SL7|KMH90-20|||ESO055-SC030|||||||4.62575|-70.5855555555555|||3.0||656.0|

		clearLink(40917,1574);//SL17 NGC1673 3994.7645250615774 15.0
		//40917|hsac.db|7876|SL17|||||||||||||16.8833333333333|-43.5833320617676|16.8833333333333|-43.6|15.0|1.00007629421945|657.0|
		//1574|hcngc.db|1574|NGC1673||SL17|KMH90-43|||ESO055-SC034|||||||4.71102777777778|-69.8213888888889|||1.0||657.0|

		clearLink(5845,3474);//NGC6134 NGC3680 3024.7121124139053 6.0
		//5845|hcngc.db|5845|NGC6134|COLLINDER303||MELOTTE106|LUND703|OCL968|ESO226-SC009|||||||16.4629166666667|-49.1511111111111|||6.0||1320.0|
		//3474|hcngc.db|3474|NGC3680|COLLINDER247||MELOTTE106|LUND588|OCL823|ESO265-SC032|||||||11.4269722222222|-43.25|||12.0||1320.0|


		clearLink(6536,6548);//NGC6871 NGC6883 65.04560504967776 30.0
		//6536|hcngc.db|6536|NGC6871|COLLINDER413|LUND921|OCL148||||||||||20.0998055555556|35.7772222222222|||30.0||1321.0|
		//6548|hcngc.db|6548|NGC6883|COLLINDER415|LUND929|OCL148||||||||||20.1888055555556|35.8322222222222|||35.0||1321.0|


		clearLink(6548,6536);//NGC6883 NGC6871 65.04560504967776 35.0
		//6548|hcngc.db|6548|NGC6883|COLLINDER415|LUND929|OCL148||||||||||20.1888055555556|35.8322222222222|||35.0||1321.0|
		//6536|hcngc.db|6536|NGC6871|COLLINDER413|LUND921|OCL148||||||||||20.0998055555556|35.7772222222222|||30.0||1321.0|


		clearLink(6638,6652);//NGC6979 NGC6995 97.98471030920678 
		//6638|hcngc.db|6638|NGC6979|GN20.49.5.01||||||||||||20.8410833333333|32.0258333333333|||||1142.0|
		//6652|hcngc.db|6652|NGC6995|GN20.49.5.01|CED182C|||||||||||20.9529722222222|31.2352777777778|||12.0||1142.0|


		clearLink(6633,6652);//NGC6974 NGC6995 85.79357515066181 
		//6633|hcngc.db|6633|NGC6974|GN20.49.5.01||||||||||||20.8511944444444|31.8280555555556|||||1142.0|
		//6652|hcngc.db|6652|NGC6995|GN20.49.5.01|CED182C|||||||||||20.9529722222222|31.2352777777778|||12.0||1142.0|


		clearLink(6652,6633);//NGC6995 NGC6974 85.79357515066181 12.0
		//6652|hcngc.db|6652|NGC6995|GN20.49.5.01|CED182C|||||||||||20.9529722222222|31.2352777777778|||12.0||1142.0|
		//6633|hcngc.db|6633|NGC6974|GN20.49.5.01||||||||||||20.8511944444444|31.8280555555556|||||1142.0|


		clearLink(6652,6638);//NGC6995 NGC6979 97.98471030920678 12.0
		//6652|hcngc.db|6652|NGC6995|GN20.49.5.01|CED182C|||||||||||20.9529722222222|31.2352777777778|||12.0||1142.0|
		//6638|hcngc.db|6638|NGC6979|GN20.49.5.01||||||||||||20.8410833333333|32.0258333333333|||||1142.0|



		clearLink(6622,43492);//NGC6960 SH2-109 603.5848520444122 70.0
		//6622|hcngc.db|6622|NGC6960|LBN191|GN20.49.5.01|SH2-109||||||||||20.7661388888889|30.5952777777778|||70.0||190.0|
		//43492|sh2.db|109|SH2-109|||||||||||||20.5606553554535|40.3387680053711|20.56|40.33|1080.0|0.692031652587672|190.0|



		clearLink(6237,6210);//NGC6551 NGC6522 75.99944745529133 5.6
		//6237|hcngc.db|6237|NGC6551||GCL82|ESO456-**060|||ESO456-SC043|||||||18.1498888888889|-29.5577777777778|||5.6||1318.0|
		//6210|hcngc.db|6210|NGC6522||GCL82||||ESO456-SC043|||||||18.05975|-30.035|||5.6||1318.0|



		clearLink(6210,6237);//NGC6522 NGC6551 75.99944745529133 5.6
		//6210|hcngc.db|6210|NGC6522||GCL82||||ESO456-SC043|||||||18.05975|-30.035|||5.6||1318.0|
		//6237|hcngc.db|6237|NGC6551||GCL82|ESO456-**060|||ESO456-SC043|||||||18.1498888888889|-29.5577777777778|||5.6||1318.0|

		clearLink(43435,30075);//SH2-52 PK017-21.1 16.87447564664486 2.0
		//43435|sh2.db|52|SH2-52|ESO526-3||||||||||||19.7963237762451|-23.0754661560059|19.7761694666667|-23.136928|2.0|17.0864548463101|1311.0|
		//30075|hpk.db|1356|PK017-21.1|G017.3-21.9|A6665|ARO36|VV'513|ESO526-3||||||||19.776667|-23.15|19.7761694666667|-23.136928|2.511|0.885827981596086|1311.0|

		clearLink(26055,7763);//LBN601 IC353 404.6812389911387 2.0
		//26055|hlbn.db|601|LBN601|IC353|Ced24|||||||||||3.752396|32.32278|3.74666666666667|32.4|2.0|6.35903925384094|222.0|
		//7763|hcngc.db|7763|IC353|LBN601|Ced24|||||||||||3.88339996337891|25.7999992370605|||180.0||222.0|


		clearLink(26055,41642);//LBN601 IC353 422.13518306102645 2.0
		//26055|hlbn.db|601|LBN601|IC353|Ced24|||||||||||3.752396|32.32278|3.74666666666667|32.4|2.0|6.35903925384094|222.0|
		//41642|hsac.db|8601|IC353|||||||||||||3.88333333333333|25.5|3.74666666666667|32.4|180.0|427.730175301926|222.0|

		clearLink(25550,25547);//LBN96 LBN93 90.54291338414382 8.0
		//25550|hlbn.db|96|LBN96|||||||||||||-5.490958|1.0354613|18.4162253333333|0.85997|8.0||343.0|
		//25547|hlbn.db|93|LBN93|LBN96|SH2-68|||||||||||-5.5909047|0.86155593|18.4162253333333|0.85997|8.0|6.417011423852|343.0|

		clearLink(41642,26055);//IC353 LBN601 422.13518306102645 180.0
		//41642|hsac.db|8601|IC353|||||||||||||3.88333333333333|25.5|3.74666666666667|32.4|180.0|427.730175301926|222.0|
		//26055|hlbn.db|601|LBN601|IC353|Ced24|||||||||||3.752396|32.32278|3.74666666666667|32.4|2.0|6.35903925384094|222.0|

		clearLink(42541,33575);//PISMIS16 VDBH81 3578.2846722601516 1.5
		//42541|hsac.db|9500|PISMIS16|OCl790|C0949-529|Pismis16|VDBH81|||||||||9.855|-53.1833343505859|9.85446666666667|-53.167|1.5|1.02141553862997|721.0|
		//33575|hsac.db|534|VDBH81|||||||||||||17.0666666666667|-51.0833320617676|17.0666666666667|-51.08|6.0|0.199923711887978|721.0|



	}
	
	/**
	 * rearranging pgc,mgc, etc to correct order
	 * applies to ngc and ugc
	 * @throws Exception 
	 */
	public static void rearrangeNames(String dbname) throws Exception{
		Db db=new Db(PATH,DBNAME);
		List<String[]>list=db.exec("select id,n1,n2,n3,n4,n5,n6,n7,n8,n9,n10,n11,n12 from total where dbname='"+dbname+"';",-1);
		db.start();
		db.addCommand("begin;");
		for(String[] a:list){
			String[] names=new String[13];//ignore 0
			for(int i=1;i<a.length;i++){
				names[i]=a[i].replace("'", "''");
				if(names[i].equals("")){
					names[i]="null";
				}
				else
					names[i]="'"+names[i]+"'";
			}
			boolean changed=false;
			boolean exit=false;
			int j=0;
			do{
				for(int i=1;i<a.length;i++){
					p("i="+i);
					for(String s:names){
						p(s);
					}
					
					if(names[i].startsWith("'PGC")&&(i!=1)&&!names[1].startsWith("'PGC")){
						String s2=names[1];
						names[1]=names[i];
						names[i]=s2;
						changed=true;
						break;
					}
					else if(names[i].startsWith("'NGC")&&(i!=2)&&!names[2].startsWith("'NGC")){
						String s2=names[2];
						names[2]=names[i];
						names[i]=s2;
						changed=true;
						break;
					}
					else if(names[i].startsWith("'UGC")&&(i!=3)&&!names[3].startsWith("'UGC")){
						String s2=names[3];
						names[3]=names[i];
						names[i]=s2;
						changed=true;
						break;
					}
					else if(names[i].startsWith("'MCG")&&(i!=4)&&!names[4].startsWith("'MCG")){
						String s2=names[4];
						names[4]=names[i];
						names[i]=s2;
						changed=true;
						break;
					}
					else if(names[i].startsWith("'CGCG")&&(i!=5)&&!names[5].startsWith("'CGCG")){
						String s2=names[5];
						names[5]=names[i];
						names[i]=s2;
						changed=true;
						break;
					}
					else if(names[i].startsWith("'ESO")&&(i!=6)&&!names[6].startsWith("'ESO")){
						String s2=names[6];
						names[6]=names[i];
						names[i]=s2;
						changed=true;
						break;
					}
					
					if(i==a.length-1)exit=true;
				}
			}while(!exit);
			p(""+j++);
			if(changed){
				db.addCommand("update total set n1="+names[1]+",n2="+names[2]+",n3="+names[3]+",n4="+names[4]+",n5="+names[5]+",n6="+names[6]+",n7="+names[7]+",n8="+names[8]+
						",n9="+names[9]+",n10="+names[10]+",n11="+names[11]+",n12="+names[12]+" where id="+a[0]+";");
			}
			
		}
		db.addCommand("commit;");
		db.end(-1);
	}
	
	
	
	/**
	 * copying PGC data into UGC db
	 * @throws Exception 
	 */
	public static void replaceUgcWithPgc() throws Exception{
		Db db=new Db(PATH,"hugc.db");
		db.exec("alter table customdbb add column ra2 real;",-1);
		db.exec("alter table customdbb add column dec2 real;",-1);
		db.exec("alter table customdbb add column mag2 real;",-1);
		db.exec("alter table customdbb add column a2 real;",-1);
		db.exec("alter table customdbb add column b2 real;",-1);
		db.exec("alter table customdbb add column pa2 real;",-1);
		db.start();
		db.addCommand("attach database '"+PATH+"pgcnames.db' as pgc;");
		
		//copying from pgcnames
		db.addCommand("select q1._id,q2.ra,q2.dec,q2.mag,q2.a,q2.b,q2.pa from customdbb as q1 join names as q2 on q1.name1=q2.n3 where q2.ra is not null;");
		List<String[]>list=db.end(-1);
		db.start();
		db.addCommand("begin;");
		for(String[] a:list){
			for(int i=0;i<a.length;i++){
				if(a[i].equals("")){
					a[i]="null";
				}
			}
			db.addCommand("update customdbb set ra2="+a[1]+",dec2="+a[2]+",mag2="+a[3]+",a2="+a[4]+",b2="+a[5]+",pa2="+a[6]+" where _id="+a[0]+";");
		}
		db.addCommand("commit;");
		db.end(-1);
		
		
		db.start();
		db.addCommand("attach database '"+PATH+"sqdb.db' as pgc;");
		
		//copying remaining from core database
		db.addCommand("select q1._id,q2.ra,q2.dec,q2.mag,q2.a,q2.b,q2.pa from customdbb as q1 join core as q2 on q1.name1=q2.name where q2.ra is not null and q1.ra2 is null;");
		List<String[]>list2=db.end(-1);
		db.start();
		db.addCommand("begin;");
		for(String[] a:list2){
			for(int i=0;i<a.length;i++){
				if(a[i].equals("")){
					a[i]="null";
				}
			}
			db.addCommand("update customdbb set ra2="+a[1]+",dec2="+a[2]+",mag2="+a[3]+",a2="+a[4]+",b2="+a[5]+",pa2="+a[6]+" where _id="+a[0]+";");
		}
		db.addCommand("commit;");
		db.end(-1);
		
		//copying from fields2 into the fields
		db.exec("update customdbb set ra=ra2,dec=dec2,mag=mag2,a=a2,b=b2,pa=pa2 where ra2 is not null;",-1);
	}
	/**
	 * clearing all ugc links
	 * all objects except for 48 not linking to PGC
	 * assumed that replaceUgcWithPgc was already run
	 * @throws Exception 
	 */
	public static void clearUgcLinks() throws Exception{
		Db db=new Db(PATH,DBNAME);
		db.start();
		db.addCommand("attach database '"+PATH+"hugc.db' as ugc;");
		db.addCommand("select id from total as q1 join customdbb as q2 on q1.name=q2.name1 where dbname='hugc.db' and ra2 is not null;");
		List<String[]>list=db.end(-1);
		db.start();
		db.addCommand("begin;");
		for(String[] a:list){
			db.addCommand("update TOTAL set n1=null,n2=null,n3=null,n4=null,n5=null,n6=null,n7=null,n8=null,n9=null,n10=null," +
				"n11=null,n12=null,dst=null where id="+a[0]+" and dbname='hugc.db';");
		}
		db.addCommand("commit;");
		
		db.end(-1);
	}
	/**
	 * clearing ngcic n2-n12 for objects having counterparts
	 * @throws Exception 
	 */
	public static void clearNgcLinks() throws Exception{
		Db db=new Db(PATH,DBNAME);
		db.start();
		db.addCommand("attach database '"+PATH+"pgcnames.db' as pgc;");
		String sql="select id from total as q1 join names as q2 on q1.n1=q2.n1 where dbname='hcngc.db';";
		db.addCommand(sql);
		List<String[]>list=db.end(-1);
		db.start();
		db.addCommand("begin;");
		for(String[] a:list){
			db.addCommand("update total set n2=null,n3=null,n4=null,n5=null,n6=null,n7=null,n8=null,n9=null,n10=null,n11=null,n12=null where id="+a[0]+";");
		}
		db.addCommand("commit;");
		db.end(-1);
		
		//wrongNgcPgc();
		clearAllNames("NGC296","hcngc.db");//NGC296 PGC3274 dst=8.608686007027307 a=1.2
		clearAllNames("NGC618","hcngc.db");//NGC618 PGC5933 dst=5.210383741293298 a=1.6
		clearAllNames("NGC1078","hcngc.db");//NGC1078 PGC10326 dst=1349.5762422526523 a=0.3
		clearAllNames("NGC1100","hcngc.db");//NGC1100 PGC10483 dst=35.90320668326617 a=1.7
		clearAllNames("NGC1384","hcngc.db");//NGC1384 PGC13377 dst=2049.628307280758 a=0.8
		clearAllNames("NGC1690","hcngc.db");//NGC1690 PGC16289 dst=1.7631511827638704 a=1.1
		clearAllNames("NGC1720","hcngc.db");//NGC1720 PGC16458 dst=847.9877276340445 a=1.6
		clearAllNames("NGC1759","hcngc.db");//NGC1759 PGC16547 dst=1.19585239588744 a=1.4
		clearAllNames("NGC1979","hcngc.db");//NGC1979 PGC17452 dst=2788.289123601714 a=2.2
		clearAllNames("NGC2602","hcngc.db");//NGC2602 PGC24099 dst=5.00296973263012 a=0.3
		clearAllNames("NGC2617","hcngc.db");//NGC2617 PGC24136 dst=2.4057086890074846 a=1.1
		clearAllNames("NGC2720","hcngc.db");//NGC2720 PGC28238 dst=2331.151354526173 a=1.2
		clearAllNames("NGC3116","hcngc.db");//NGC3116 PGC29386 dst=1166.0527540953688 a=0.3
		clearAllNames("NGC3171","hcngc.db");//NGC3171 PGC29837 dst=3558.357888268333 a=1.7
		clearAllNames("NGC3732","hcngc.db");//NGC3732 PGC35731 dst=1341.4720548369023 a=1.2
		clearAllNames("NGC3788","hcngc.db");//NGC3788 PGC36156 dst=2477.87724158442 a=2.1
		clearAllNames("NGC3823","hcngc.db");//NGC3823 PGC36311 dst=1453.0749275754667 a=1.5
		clearAllNames("NGC3869","hcngc.db");//NGC3869 PGC36696 dst=2974.2067127664004 a=1.9
		clearAllNames("NGC3917","hcngc.db");//NGC3917 PGC37073 dst=11.440623339112785 a=5.1
		clearAllNames("NGC4023","hcngc.db");//NGC4023 PGC37723 dst=325.420515012829 a=0.9
		clearAllNames("NGC4838","hcngc.db");//NGC4838 PGC44382 dst=2440.5386950444945 a=1.6
		clearAllNames("NGC4908","hcngc.db");//NGC4908 PGC44828 dst=2.211353246839416 a=1.1
		clearAllNames("NGC5054","hcngc.db");//NGC5054 PGC46115 dst=27.135301668350582 a=5.1
		clearAllNames("NGC5059","hcngc.db");//NGC5059 PGC46224 dst=3287.30619684489 a=1.0
		clearAllNames("NGC5441","hcngc.db");//NGC5441 PGC50057 dst=4.932175352753265 a=0.5
		clearAllNames("NGC5693","hcngc.db");//NGC5693 PGC52294 dst=5034.123811814466 a=1.8
		clearAllNames("NGC5717","hcngc.db");//NGC5717 PGC52441 dst=2591.986733410243 a=0.7
		clearAllNames("NGC6004","hcngc.db");//NGC6004 PGC56116 dst=865.6208512268286 a=1.9
		clearAllNames("NGC6420","hcngc.db");//NGC6420 PGC60553 dst=1.2503391595940319 a=0.6
		clearAllNames("NGC6591","hcngc.db");//NGC6591 PGC61610 dst=1.7336458399573749 a=0.3
		clearAllNames("NGC6685","hcngc.db");//NGC6685 PGC6220 dst=4151.220351580948 a=1.1
		clearAllNames("NGC6983","hcngc.db");//NGC6983 PGC65372 dst=2664.0658773833356 a=0.8
		clearAllNames("NGC7377","hcngc.db");//NGC7377 PGC69670 dst=2006.7559465999711 a=3.0
		clearAllNames("NGC7651","hcngc.db");//NGC7651 PGC71344 dst=2.325194093855695 a=0.7
		clearAllNames("NGC7663","hcngc.db");//NGC7663 PGC71445 dst=1836.5185717458412 a=0.9
		clearAllNames("NGC7727","hcngc.db");//NGC7727 PGC72020 dst=1066.7255518070974 a=4.7
		clearAllNames("IC672","hcngc.db");//IC672 PGC33725 dst=2.3712043580116235 a=0.5
		clearAllNames("IC815","hcngc.db");//IC815 PGC43080 dst=2.3756948633367383 a=0.5
		clearAllNames("IC1107","hcngc.db");//IC1107 PGC54391 dst=2.7577265735791077 a=0.5
		clearAllNames("IC1414","hcngc.db");//IC1414 PGC67763 dst=5.531634471936771 a=0.5
		clearAllNames("IC1490","hcngc.db");//IC1490 PGC73143 dst=3.902277300402436 a=1.70000004768372
		clearAllNames("IC1524","hcngc.db");//IC1524 PGC73143 dst=3.902277300402436 a=1.70000004768372
		clearAllNames("IC1528","hcngc.db");//IC1528 PGC312 dst=1.4842718734019078 a=2.40000009536743
		clearAllNames("IC1670","hcngc.db");//IC1670 PGC4707 dst=1.0194862613379343 a=1.89999997615814
		clearAllNames("IC1670","hcngc.db");//IC1670 PGC4707 dst=1.0194862613379343 a=1.89999997615814
		clearAllNames("IC1803","hcngc.db");//IC1803 PGC9462 dst=3.558495681971379 a=0.300000011920929
		clearAllNames("IC2339","hcngc.db");//IC2339 PGC23545 dst=2183.011734896746 a=1.10000002384186
		clearAllNames("IC2468","hcngc.db");//IC2468 PGC26691 dst=1.8205664084548556 a=0.400000005960464
		clearAllNames("IC3018","hcngc.db");//IC3018 PGC39627 dst=3215.584315094052 a=0.600000023841858
		clearAllNames("IC3640","hcngc.db");//IC3640 PGC42478 dst=3.0443708398314575 a=0.400000005960464
		clearAllNames("IC3646","hcngc.db");//IC3646 PGC169892 dst=2.9932744598941703 a=0.699999988079071
		clearAllNames("IC5081","hcngc.db");//IC5081 PGC65971 dst=3.251014663085095 a=0.300000011920929
		clearAllNames("IC5144","hcngc.db");//IC5144 PGC67614 dst=2.740397104847057 a=0.600000023841858
		clearAllNames("IC5324","hcngc.db");//IC5324 PGC71526 dst=1.1804023805241661 a=1.10000002384186
			
		//wrongNgcPgc2()
		clearAllNames("NGC1155","hcngc.db");//NGC1155 PGC11223 dst=2518.3400977399588 a=0.8
		clearAllNames("NGC1383","hcngc.db");//NGC1383 PGC13337 dst=3029.4621078589707 a=1.9
		clearAllNames("NGC1401","hcngc.db");//NGC1401 PGC13475 dst=2554.942434523779 a=2.4
		clearAllNames("NGC2179","hcngc.db");//NGC2179 PGC18543 dst=5232.385324395194 a=1.7
		clearAllNames("NGC2603","hcngc.db");//NGC2603 PGC3133653 dst=4.014341214853499 a=0.6
		clearAllNames("NGC2829","hcngc.db");//NGC2829 PGC26356 dst=4.582509101493222 a=0.3
		clearAllNames("NGC3099","hcngc.db");//NGC3099 PGC29088 dst=1.2713803564460953 a=0.4
		clearAllNames("NGC3492","hcngc.db");//NGC3492 PGC33429 dst=1146.535537588057 a=1.1
		clearAllNames("NGC3553","hcngc.db");//NGC3553 PGC33933 dst=1.3271883147944648 a=0.5
		clearAllNames("NGC3853","hcngc.db");//NGC3853 PGC36595 dst=1125.0984446662412 a=1.7
		clearAllNames("NGC4210","hcngc.db");//NGC4210 PGC39148 dst=3922.427356511721 a=2.0
		clearAllNames("NGC5070","hcngc.db");//NGC5070 PGC46437 dst=3.7278171042965664 a=0.7
		clearAllNames("NGC5072","hcngc.db");//NGC5072 PGC46437 dst=3.7278171042965664 a=1.0
		clearAllNames("NGC5340","hcngc.db");//NGC5340 PGC49012 dst=4.204331882152327 a=0.8
		clearAllNames("NGC5813","hcngc.db");//NGC5813 PGC53543 dst=2797.8650740389776 a=4.2
		clearAllNames("NGC5851","hcngc.db");//NGC5851 PGC9365 dst=10196.970734533701 a=1.1
		clearAllNames("NGC7402","hcngc.db");//NGC7402 PGC66914 dst=1236.2513391336624 a=0.7
		clearAllNames("NGC7579","hcngc.db");//NGC7579 PGC70964 dst=1.4810798053034102 a=0.5
		clearAllNames("IC15","hcngc.db");//IC15 PGC165498 dst=6393.126091355635 a=0.400000005960464
		clearAllNames("IC298","hcngc.db");//IC298 PGC11893 dst=2.6331881353439512 a=0.699999988079071
		clearAllNames("IC2620","hcngc.db");//IC2620 PGC33332 dst=12.91332882320085 a=1.0
		clearAllNames("IC2766","hcngc.db");//IC2766 PGC1415831 dst=5345.9421690468835 a=0.5
		clearAllNames("IC3055","hcngc.db");//IC3055 PGC39104 dst=2.615646826505575 a=0.400000005960464
		clearAllNames("IC3744","hcngc.db");//IC3744 PGC2090098 dst=2501.9420366442487 a=0.400000005960464
		clearAllNames("IC3919","hcngc.db");//IC3919 PGC3088211 dst=139.24987126955793 a=0.300000011920929
		clearAllNames("IC4027","hcngc.db");//IC4027 PGC2093824 dst=2201.8183206553595 a=0.300000011920929
		clearAllNames("IC4610","hcngc.db");//IC4610 PGC58499 dst=1.5801932956370048 a=0.600000023841858
		clearAllNames("IC5195","hcngc.db");//IC5195 PGC68435 dst=1.2656309048862862 a=0.300000011920929
		

	}
	
	public static void clearAllNames(String name,String dbname) throws Exception{
		Db db=new Db(PATH,DBNAME);
		String sql="update TOTAL set n1=null,n2=null,n3=null,n4=null,n5=null,n6=null,n7=null,n8=null,n9=null,n10=null," +
				"n11=null,n12=null,dst=null where name='"+name+"' and dbname='"+dbname+"';";
		db.exec(sql, -1);

	}
	public static void clearShLinks() throws Exception{
		clearAllNames("SH2-62", "sh2.db");
		clearAllNames("SH2-104", "sh2.db");
		clearAllNames("SH2-105", "sh2.db");
		
		//wrong("q1.dbname='hlbn.db'","q2.dbname='sh2.db'",15,1);

		clearLink("LBN589","SH2-171");//LBN589 SH2-171 dst=88.21139986125755 a=20.0
		clearLink("LBN13","SH2-14");//LBN13 SH2-14 dst=1721.3810067146087 a=35.0
		clearLink("LBN554","SH2-164");//LBN554 SH2-164 dst=180.00070965199495 a=5.0
		clearLink("LBN56","SH2-42");//LBN56 SH2-42 dst=81.01209223074684 a=3.0
		clearLink("LBN96","SH2-68");//LBN96 SH2-68 dst=81.08924915111788 a=8.0
		//wrong("q1.dbname='sh2.db'","q2.dbname='hcngc.db'",15,1);

		clearLink("SH2-42","IC4701");//SH2-42 IC4701 dst=82.66143641370229 a=3.0
		clearLink("SH2-274","NGC2238");//SH2-274 NGC2238 dst=995.8304018441097 a=8.0
	}
	public static void clearLDNLinks() throws Exception{
		//getClearList("where dbname='hldn.db' and dst>max(15,a)");
		
		clearAllNames("LDN81","hldn.db");//dst=66.6761737639002 a=5.6920996657869
		clearAllNames("LDN207","hldn.db");//dst=935.201100815912 a=84.8528137423857
		clearAllNames("LDN508","hldn.db");//dst=741.421537111317 a=11.3841993315738
		clearAllNames("LDN567","hldn.db");//dst=88.7292574919539 a=9.29516007129616
		clearAllNames("LDN669","hldn.db");//dst=940.296705757439 a=67.8822502352653
		clearAllNames("LDN709","hldn.db");//dst=253.45245359774 a=2.68328163672426
		clearAllNames("LDN767","hldn.db");//dst=679.987601854164 a=26.899814460781
		clearAllNames("LDN988","hldn.db");//dst=84.576741403395 a=27.4954537405106
		clearAllNames("LDN1092","hldn.db");//dst=315.831733961247 a=7.09929584928542
		clearAllNames("LDN1469","hldn.db");//dst=31.1599267766779 a=4.64758003564808
		clearAllNames("LDN1527","hldn.db");//dst=32.7492434027974 a=5.99999993294477
		clearAllNames("LDN1547","hldn.db");//dst=7188.32602227679 a=13.2815659990304
		clearAllNames("LDN1689","hldn.db");//dst=37.1509614899146 a=5.01996023667923
		clearAllNames("LDN1695","hldn.db");//dst=219.598826666396 a=44.090816246103
		clearAllNames("LDN1699","hldn.db");//dst=6585.73613988413 a=11.0634536493754

	}
	public static void clearLBNLinks() throws Exception{
		//getClearList("where dbname='hlbn.db' and dst>max(15,a)");

		
		clearAllNames("LBN13","hlbn.db");//dst=1720.95777855028 a=35.0
		clearAllNames("LBN56","hlbn.db");//dst=81.903356007046 a=3.0
		
		clearAllNames("LBN62","hlbn.db");//dst=69.1327075115632 a=20.0
		clearAllNames("LBN85","hlbn.db");//dst=895.687609254793 a=3.0
		clearAllNames("LBN96","hlbn.db");//dst=84.1846278707848 a=8.0
		clearAllNames("LBN181","hlbn.db");//dst=47.8983365889662 a=15.0
		clearAllNames("LBN195","hlbn.db");//dst=130.085189976339 a=7.0
		
		clearAllNames("LBN411","hlbn.db");//dst=19.6005552339908 a=2.0
		
		clearAllNames("LBN502","hlbn.db");//dst=39.0447133258834 a=10.0
		clearAllNames("LBN554","hlbn.db");//dst=181.045200729486 a=5.0
		
		clearAllNames("LBN589","hlbn.db");//dst=72.729249180758 a=20.0
		clearAllNames("LBN590","hlbn.db");//dst=60.6781255848647 a=2.0
		
		clearAllNames("LBN767","hlbn.db");//dst=17.1616655961828 a=3.0
		clearAllNames("LBN802","hlbn.db");//dst=25.9888688700724 a=2.0
		
		
		clearAllNames("LBN925","hlbn.db");//dst=17.4026505210174 a=2.0
		clearAllNames("LBN1125","hlbn.db");//dst=21.2491858971797 a=2.0
		
		
		//partly, wrong("(q1.name like 'ngc%' or q1.name like 'ic%')","q2.name like 'lbn%'",15,1);

		clearLink("IC4606","LBN1107");//IC4606 LBN1107 dst=42.94682675058455 a=7.40000009536743
		clearLink("IC1871","LBN675");//IC1871 LBN675 dst=43.72492692504755 a=0.0
		clearLink("IC336","LBN773");//IC336 LBN773 dst=93.31174958392434 a=0.0
		clearLink("NGC7822","LBN589");//NGC7822 LBN589 dst=71.95600874577079 a=20.0
		clearLink("IC1283","LBN47");//IC1283 LBN47 dst=17.47201462282564 a=3.0
		
		//diffLinks("(q1.name like 'ngc%' or q1.name like 'ic%')", "q2.name like 'lbn%'");
		//no valid results
	}
	
	public static void clearBarnardLinks() throws Exception{
		//getClearList("where dbname='barnard.db' and dst>max(15,a)");

		clearAllNames("B50","barnard.db");//dst=82.2305404906349 a=15.0
		clearAllNames("B67","barnard.db");//dst=298.70348366656 a=0.0
		clearAllNames("B83","barnard.db");//dst=261.877753744721 a=7.0		
		clearAllNames("B117","barnard.db");//dst=148.000500609216 a=1.0	
		
		//wrong("(q1.name like 'ldn%')","q2.name like 'b%'",15,1);

		clearLink("LDN109","B83");//LDN109 B83 dst=261.4479691253003 a=5.01996023667923
		clearLink("LDN1744","B45");//LDN1744 B45 dst=93.08051874749744 a=18.878559215915
		clearLink("LDN509","B117");//LDN509 B117 dst=148.2176242719643 a=3.28633535931398
		clearLink("LDN233","B83A");//LDN233 B83A dst=262.8444676120026 a=1.89736664116107
		
		
	}
	/**
	 * clearing names. fully for NGC,IC and  where there is a counterpart in other databases
	 * @throws Exception 
	 */
	public static void clearSACNames() throws Exception{
		//String sql="select id from total where dbname='hsac.db' and (name like 'ngc%' or name like 'ic%') and name in (select name from total where dbname='hcngc.db');";
		String sql="select id from total where dbname='hsac.db' and (name like 'ngc%' or name like 'ic%');";
		Db db=new Db(PATH,DBNAME);
		List<String[]> list=db.exec(sql, -1);
		db.start();
		db.addCommand("begin;");
		for(String[] a:list){
			db.addCommand("update TOTAL set n1=null,n2=null,n3=null,n4=null,n5=null,n6=null,n7=null,n8=null,n9=null,n10=null," +
				"n11=null,n12=null where id="+a[0]+";");
		}
		db.addCommand("commit;");
		db.end(-1);
		
		sql="select id from total where dbname='hsac.db' and (name like 'pk%') and name in (select name from total where dbname='hpk.db');";
		db=new Db(PATH,DBNAME);
		list=db.exec(sql, -1);
		db.start();
		db.addCommand("begin;");
		for(String[] a:list){
			db.addCommand("update TOTAL set n1=null,n2=null,n3=null,n4=null,n5=null,n6=null,n7=null,n8=null,n9=null,n10=null," +
				"n11=null,n12=null where id="+a[0]+";");
		}
		db.addCommand("commit;");
		db.end(-1);
		
		sql="select id from total where dbname='hsac.db' and (name like 'ugc%') and name in (select name from total where dbname='hugc.db');";
		db=new Db(PATH,DBNAME);
		list=db.exec(sql, -1);
		db.start();
		db.addCommand("begin;");
		for(String[] a:list){
			db.addCommand("update TOTAL set n1=null,n2=null,n3=null,n4=null,n5=null,n6=null,n7=null,n8=null,n9=null,n10=null," +
				"n11=null,n12=null where id="+a[0]+";");
		}
		db.addCommand("commit;");
		db.end(-1);
		
		sql="select * from total where dbname='hsac.db' and (name like 'b0%' or name like 'b1%' or name like 'b2%' or name like 'b3%' or name like 'b4%' or name like 'b5%' or name like 'b6%' or name like 'b7%' or name like 'b8%' or name like 'b9%');";
		db=new Db(PATH,DBNAME);
		list=db.exec(sql, -1);
		db.start();
		db.addCommand("begin;");
		for(String[] a:list){
			db.addCommand("update TOTAL set n1=null,n2=null,n3=null,n4=null,n5=null,n6=null,n7=null,n8=null,n9=null,n10=null," +
				"n11=null,n12=null where id="+a[0]+";");
		}
		db.addCommand("commit;");
		db.end(-1);
		
		sql="update TOTAL set n1=null,n2=null,n3=null,n4=null,n5=null,n6=null,n7=null,n8=null,n9=null,n10=null," +
				"n11=null,n12=null where dbname='hsac.db' and dst>max(15,a);";
		db.exec(sql, -1);

		

	}
	/**
	 * getting the same PK name format as in Simbad
	 * @throws Exception
	 */
	public static void getCorrectPKNamesInNgc() throws Exception{
		Db db=new Db(PATH,DBNAME);
		for(int i=1;i<=12;i++){
			String sql="select id,name,n"+i+" from total where n"+i+" like 'pk%' and dbname='hcngc.db';";
			List<String[]>list=db.exec(sql,-1);
			for(String[] a:list){
				String name=Parsers.getCorrectPKName(a[2]);
				if(name==null)throw new Exception ("Wrong PK name "+a[0]+" "+a[1]+" "+a[2]);
				p("db.addCommand(\"update total set n"+i+"='"+name+"' where id="+a[0]+" and name='"+a[1]+"';\");");
				
			}
		}
		
	}
	/**
	 * settings names received in getCorrectPKNamesInNgc
	 * @throws Exception
	 */
	public static void setCorrectPKNamesInNgc() throws Exception{
		
		//getCorrectPKNamesInNgc();
		Db db=new Db(PATH,DBNAME);
		db.start();
		db.addCommand("begin;");
		db.addCommand("update total set n1='PK120+09.1' where id=35 and name='NGC40';");
		db.addCommand("update total set n1='PK118-74.1' where id=229 and name='NGC246';");
		db.addCommand("update total set n1='PK220-53.1' where id=1281 and name='NGC1360';");
		db.addCommand("update total set n1='PK144+06.1' where id=1408 and name='NGC1501';");
		db.addCommand("update total set n1='PK165-15.1' where id=1421 and name='NGC1514';");
		db.addCommand("update total set n1='PK206-40.1' where id=1441 and name='NGC1535';");
		db.addCommand("update total set n1='PK176+00.1' where id=1872 and name='NGC1985';");
		db.addCommand("update total set n1='PK196-10.1' where id=1905 and name='NGC2022';");
		db.addCommand("update total set n1='PK215+03.1' where id=2210 and name='NGC2346';");
		db.addCommand("update total set n1='PK189+19.1' where id=2234 and name='NGC2371';");
		db.addCommand("update total set n1='PK189+19.1' where id=2235 and name='NGC2372';");
		db.addCommand("update total set n1='PK197+17.1' where id=2251 and name='NGC2392';");
		db.addCommand("update total set n1='PK231+04.2' where id=2292 and name='NGC2438';");
		db.addCommand("update total set n1='PK239+13.1' where id=2451 and name='NGC2610';");
		db.addCommand("update total set n1='PK292+01.1' where id=3493 and name='NGC3699';");
		db.addCommand("update total set n1='PK298-04.1' where id=3855 and name='NGC4071';");
		db.addCommand("update total set n1='PK294+43.1' where id=4134 and name='NGC4361';");
		db.addCommand("update total set n1='PK307-03.1' where id=4935 and name='NGC5189';");
		db.addCommand("update total set n1='PK312+10.1' where id=5050 and name='NGC5307';");
		db.addCommand("update total set n1='PK309-04.2' where id=5057 and name='NGC5315';");
		db.addCommand("update total set n1='PK317-05.1' where id=5567 and name='NGC5844';");
		db.addCommand("update total set n1='PK331+16.1' where id=5594 and name='NGC5873';");
		db.addCommand("update total set n1='PK327+10.1' where id=5602 and name='NGC5882';");
		db.addCommand("update total set n1='PK322-05.1' where id=5695 and name='NGC5979';");
		db.addCommand("update total set n1='PK341+13.1' where id=5741 and name='NGC6026';");
		db.addCommand("update total set n1='PK064+48.1' where id=5772 and name='NGC6058';");
		db.addCommand("update total set n1='PK342+10.1' where id=5785 and name='NGC6072';");
		db.addCommand("update total set n1='PK341+05.1' where id=5863 and name='NGC6153';");
		db.addCommand("update total set n1='PK043+37.1' where id=5919 and name='NGC6210';");
		db.addCommand("update total set n1='PK349+01.1' where id=6007 and name='NGC6302';");
		db.addCommand("update total set n1='PK009+14.1' where id=6014 and name='NGC6309';");
		db.addCommand("update total set n1='PK338-08.1' where id=6031 and name='NGC6326';");
		db.addCommand("update total set n1='PK349-01.1' where id=6041 and name='NGC6337';");
		db.addCommand("update total set n1='PK002+05.1' where id=6069 and name='NGC6369';");
		db.addCommand("update total set n1='PK011+05.1' where id=6134 and name='NGC6439';");
		db.addCommand("update total set n1='PK008+03.1' where id=6140 and name='NGC6445';");
		db.addCommand("update total set n1='PK010+00.1' where id=6223 and name='NGC6537';");
		db.addCommand("update total set n1='PK096+29.1' where id=6229 and name='NGC6543';");
		db.addCommand("update total set n1='PK358-07.1' where id=6249 and name='NGC6563';");
		db.addCommand("update total set n1='PK003-04.5' where id=6250 and name='NGC6565';");
		db.addCommand("update total set n1='PK011-00.2' where id=6252 and name='NGC6567';");
		db.addCommand("update total set n1='PK034+11.1' where id=6257 and name='NGC6572';");
		db.addCommand("update total set n1='PK010-01.1' where id=6262 and name='NGC6578';");
		db.addCommand("update total set n1='PK005-06.1' where id=6303 and name='NGC6620';");
		db.addCommand("update total set n1='PK009-05.1' where id=6312 and name='NGC6629';");
		db.addCommand("update total set n1='PK008-07.2' where id=6327 and name='NGC6644';");
		db.addCommand("update total set n1='PK033-02.1' where id=6414 and name='NGC6741';");
		db.addCommand("update total set n1='PK078+18.1' where id=6415 and name='NGC6742';");
		db.addCommand("update total set n1='PK029-05.1' where id=6423 and name='NGC6751';");
		db.addCommand("update total set n1='PK062+09.1' where id=6437 and name='NGC6765';");
		db.addCommand("update total set n1='PK082+07.1' where id=6438 and name='NGC6766';");
		db.addCommand("update total set n1='PK033-06.1' where id=6443 and name='NGC6772';");
		db.addCommand("update total set n1='PK034-06.1' where id=6448 and name='NGC6778';");
		db.addCommand("update total set n1='PK041-02.1' where id=6451 and name='NGC6781';");
		db.addCommand("update total set n1='PK034-06.1' where id=6455 and name='NGC6785';");
		db.addCommand("update total set n1='PK037-06.1' where id=6460 and name='NGC6790';");
		db.addCommand("update total set n1='PK046-04.1' where id=6471 and name='NGC6803';");
		db.addCommand("update total set n1='PK045-04.1' where id=6472 and name='NGC6804';");
		db.addCommand("update total set n1='PK042-06.1' where id=6475 and name='NGC6807';");
		db.addCommand("update total set n1='PK025-17.1' where id=6486 and name='NGC6818';");
		db.addCommand("update total set n1='PK083+12.1' where id=6494 and name='NGC6826';");
		db.addCommand("update total set n1='PK082+11.1' where id=6501 and name='NGC6833';");
		db.addCommand("update total set n1='PK065+00.1' where id=6509 and name='NGC6842';");
		db.addCommand("update total set n1='PK042-14.1' where id=6519 and name='NGC6852';");
		db.addCommand("update total set n1='PK070+01.2' where id=6524 and name='NGC6857';");
		db.addCommand("update total set n1='PK057-08.1' where id=6544 and name='NGC6879';");
		db.addCommand("update total set n1='PK074+02.1' where id=6546 and name='NGC6881';");
		db.addCommand("update total set n1='PK082+07.1' where id=6549 and name='NGC6884';");
		db.addCommand("update total set n1='PK060-07.2' where id=6551 and name='NGC6886';");
		db.addCommand("update total set n1='PK054-12.1' where id=6556 and name='NGC6891';");
		db.addCommand("update total set n1='PK069-02.1' where id=6558 and name='NGC6894';");
		db.addCommand("update total set n1='PK061-09.1' where id=6568 and name='NGC6905';");
		db.addCommand("update total set n1='PK093+05.2' where id=6664 and name='NGC7008';");
		db.addCommand("update total set n1='PK037-34.1' where id=6665 and name='NGC7009';");
		db.addCommand("update total set n1='PK089+00.1' where id=6681 and name='NGC7026';");
		db.addCommand("update total set n1='PK084-03.1' where id=6682 and name='NGC7027';");
		db.addCommand("update total set n1='PK088-01.1' where id=6702 and name='NGC7048';");
		db.addCommand("update total set n1='PK101+08.1' where id=6729 and name='NGC7076';");
		db.addCommand("update total set n1='PK066-28.1' where id=6746 and name='NGC7094';");
		db.addCommand("update total set n1='PK104+07.1' where id=6785 and name='NGC7139';");
		db.addCommand("update total set n1='PK036-57.1' where id=6928 and name='NGC7293';");
		db.addCommand("update total set n1='PK107+02.1' where id=6983 and name='NGC7354';");
		db.addCommand("update total set n1='PK106-17.1' where id=7269 and name='NGC7662';");
		db.addCommand("update total set n1='PK138+02.1' where id=7703 and name='IC289';");
		db.addCommand("update total set n1='PK159-15.1' where id=7761 and name='IC351';");
		db.addCommand("update total set n1='PK215-24.1' where id=7824 and name='IC418';");
		db.addCommand("update total set n1='PK326+42.1' where id=8355 and name='IC972';");
		db.addCommand("update total set n1='PK345-08.1' where id=8638 and name='IC1266';");
		db.addCommand("update total set n1='PK358-21.1' where id=8659 and name='IC1297';");
		db.addCommand("update total set n1='PK130+01.1' where id=9076 and name='IC1747';");
		db.addCommand("update total set n1='PK161-14.1' where id=9330 and name='IC2003';");
		db.addCommand("update total set n1='PK166+10.1' where id=9446 and name='IC2149';");
		db.addCommand("update total set n1='PK221-12.1' where id=9460 and name='IC2165';");
		db.addCommand("update total set n1='PK285-14.1' where id=9615 and name='IC2448';");
		db.addCommand("update total set n1='PK281-05.1' where id=9664 and name='IC2501';");
		db.addCommand("update total set n1='PK285-05.1' where id=9714 and name='IC2553';");
		db.addCommand("update total set n1='PK291-04.1' where id=9779 and name='IC2621';");
		db.addCommand("update total set n1='PK304-04.1' where id=10988 and name='IC4191';");
		db.addCommand("update total set n1='PK319+15.1' where id=11189 and name='IC4406';");
		db.addCommand("update total set n1='PK025+40.1' where id=11371 and name='IC4593';");
		db.addCommand("update total set n1='PK338+05.1' where id=11377 and name='IC4599';");
		db.addCommand("update total set n1='PK000+12.1' where id=11407 and name='IC4634';");
		db.addCommand("update total set n1='PK345+00.1' where id=11409 and name='IC4637';");
		db.addCommand("update total set n1='PK334-09.1' where id=11414 and name='IC4642';");
		db.addCommand("update total set n1='PK346-08.1' where id=11431 and name='IC4663';");
		db.addCommand("update total set n1='PK007+01.1' where id=11435 and name='IC4670';");
		db.addCommand("update total set n1='PK348-13.1' where id=11458 and name='IC4699';");
		db.addCommand("update total set n1='PK002-13.1' where id=11531 and name='IC4776';");
		db.addCommand("update total set n1='PK058-10.1' where id=11743 and name='IC4997';");
		db.addCommand("update total set n1='PK089-05.1' where id=11853 and name='IC5117';");
		db.addCommand("update total set n1='PK100-05.1' where id=11940 and name='IC5217';");
		db.addCommand("update total set n2='PK130-10.1' where id=605 and name='NGC650';");
		db.addCommand("update total set n2='PK130-10.1' where id=606 and name='NGC651';");
		db.addCommand("update total set n2='PK170+15.1' where id=2115 and name='NGC2242';");
		db.addCommand("update total set n2='PK234+02.1' where id=2294 and name='NGC2440';");
		db.addCommand("update total set n2='PK243-01.1' where id=2305 and name='NGC2452';");
		db.addCommand("update total set n2='PK265+04.1' where id=2624 and name='NGC2792';");
		db.addCommand("update total set n2='PK261+08.1' where id=2649 and name='NGC2818';");
		db.addCommand("update total set n2='PK278-05.1' where id=2696 and name='NGC2867';");
		db.addCommand("update total set n2='PK277-03.1' where id=2725 and name='NGC2899';");
		db.addCommand("update total set n2='PK272+12.1' where id=2945 and name='NGC3132';");
		db.addCommand("update total set n2='PK296-20.1' where id=3005 and name='NGC3195';");
		db.addCommand("update total set n2='PK286-04.1' where id=3020 and name='NGC3211';");
		db.addCommand("update total set n2='PK261+32.1' where id=3050 and name='NGC3242';");
		db.addCommand("update total set n2='PK148+57.1' where id=3382 and name='NGC3587';");
		db.addCommand("update total set n2='PK294+04.1' where id=3705 and name='NGC3918';");
		db.addCommand("update total set n2='PK063+13.1' where id=6396 and name='NGC6720';");
		db.addCommand("update total set n2='PK060-03.1' where id=6520 and name='NGC6853';");
		db.addCommand("update total set n2='PK327+10.1' where id=8491 and name='IC1108';");
		db.addCommand("update total set n2='PK117+18.1' where id=8805 and name='IC1454';");
		db.addCommand("update total set n2='PK123+34.1' where id=10508 and name='IC3568';");
		db.addCommand("update total set n2='PK307-03.1' where id=11064 and name='IC4274';");
		db.addCommand("update total set n2='PK002-52.1' where id=11881 and name='IC5148';");
		db.addCommand("update total set n2='PK002-52.1' where id=11883 and name='IC5150';");
		db.addCommand("update total set n4='PK328-17.1' where id=11429 and name='IC4662';");

		
		db.addCommand("commit;");
		db.end(-1);
	}
	public static void clearPKLinks() throws Exception{
		
		//mostly same VV catalog in principally different objects
		
		//getClearList("where dbname='hpk.db' and dst>max(1,coalesce(a,0)/2)");
		//wrong("(q1.name like 'ngc%' or q1.name like 'ic%')","q2.name like 'pk%'",1.5,0.5);

	/*	clearLink("NGC5218","PK215+03.1");//NGC5218 PK215+03.1 dst=5600.396237869465 a=1.8
		clearLink("NGC3788","PK041-02.1");//NGC3788 PK041-02.1 dst=6416.900237551642 a=2.1
		clearLink("NGC3396","PK060-03.1");//NGC3396 PK060-03.1 dst=6663.877846543772 a=3.1
		clearLink("IC3481","PK231+04.2");//IC3481 PK231+04.2 dst=4600.158093108559 a=0.800000011920929
		clearLink("NGC4054","PK002-02.4");//NGC4054 PK002-02.4 dst=6815.6028933978305 a=0.5
		clearLink("NGC742","PK024+03.1");//NGC742 PK024+03.1 dst=6848.137465766654 a=0.2
		clearLink("NGC2799","PK265+04.1");//NGC2799 PK265+04.1 dst=5065.087746861399 a=1.9
		clearLink("NGC935","PK051-03.1");//NGC935 PK051-03.1 dst=5740.066126968244 a=1.7
		clearLink("NGC5614","PK342+10.1");//NGC5614 PK342+10.1 dst=4528.6957067083595 a=2.5
		clearLink("NGC507","PK012-09.1");//NGC507 PK012-09.1 dst=6527.54526589686 a=3.1
		clearLink("NGC3227","PK021-05.1");//NGC3227 PK021-05.1 dst=7736.3538803442325 a=5.4
		clearLink("IC3481","PK231+04.2");//IC3481 PK231+04.2 dst=4600.980810638461 a=0.200000002980232
		clearLink("NGC4676","PK033-06.1");//NGC4676 PK033-06.1 dst=5849.681720566448 a=2.2
		clearLink("NGC5395","PK239+13.1");//NGC5395 PK239+13.1 dst=5584.840241915813 a=2.9
		clearLink("IC3483","PK231+04.2");//IC3483 PK231+04.2 dst=4603.256957975288 a=0.5
		clearLink("NGC1875","PK003-06.1");//NGC1875 PK003-06.1 dst=9237.156193262672 a=1.6
		clearLink("NGC4015","PK032-02.1");//NGC4015 PK032-02.1 dst=6237.278842149355 a=1.4
		clearLink("NGC379","PK015-04.1");//NGC379 PK015-04.1 dst=6349.409338731557 a=1.4
		clearLink("NGC3561","PK052-02.2");//NGC3561 PK052-02.2 dst=6723.570077835802 a=5.3
		clearLink("NGC3239","PK354+04.1");//NGC3239 PK354+04.1 dst=6543.631475356295 a=5.0
		clearLink("NGC385","PK015-04.1");//NGC385 PK015-04.1 dst=6349.475854281997 a=1.1
		clearLink("NGC6040","PK003-14.1");//NGC6040 PK003-14.1 dst=3878.6310273425697 a=1.3
		clearLink("NGC5279","PK206-40.1");//NGC5279 PK206-40.1 dst=7676.098084118206 a=0.6
		clearLink("NGC3447","PK060-07.2");//NGC3447 PK060-07.2 dst=7564.787321041473 a=3.7
		clearLink("NGC7317","PK118+08.1");//NGC7317 PK118+08.1 dst=2306.5143522072653 a=1.1
		clearLink("NGC5514","PK331+16.1");//NGC5514 PK331+16.1 dst=2868.4334795809937 a=2.2
		clearLink("IC3862","PK089-05.1");//IC3862 PK089-05.1 dst=5241.274908141449 a=1.29999995231628
		clearLink("NGC5213","PK147+04.1");//NGC5213 PK147+04.1 dst=6660.059628363799 a=1.0
		clearLink("NGC380","PK015-04.1");//NGC380 PK015-04.1 dst=6349.372712084076 a=1.4
		clearLink("NGC7714","PK261+08.1");//NGC7714 PK261+08.1 dst=7965.156082148903 a=1.9
		clearLink("NGC4782","PK022-03.1");//NGC4782 PK022-03.1 dst=5088.787812957038 a=1.8
		clearLink("NGC72","PK007-03.1");//NGC72 PK007-03.1 dst=6118.651341639335 a=1.1
		clearLink("NGC2426","PK107-13.1");//NGC2426 PK107-13.1 dst=4215.279673102588 a=1.1
		clearLink("NGC3432","PK147-02.1");//NGC3432 PK147-02.1 dst=4271.026232404301 a=6.8
		clearLink("NGC5579","PK003-02.3");//NGC5579 PK003-02.3 dst=4892.42677688 a=1.9
		clearLink("NGC5306","PK357-05.1");//NGC5306 PK357-05.1 dst=3815.418150468852 a=1.4
		clearLink("NGC4485","PK189+07.1");//NGC4485 PK189+07.1 dst=4384.798266476493 a=2.3
		clearLink("NGC7319","PK118+08.1");//NGC7319 PK118+08.1 dst=2304.240631840065 a=1.7
		clearLink("NGC3994","PK057-08.1");//NGC3994 PK057-08.1 dst=6403.508954068988 a=1.0
		clearLink("IC18","PK042-06.1");//IC18 PK042-06.1 dst=4506.345712657738 a=1.10000002384186
		clearLink("NGC4098","PK294+04.1");//NGC4098 PK294+04.1 dst=4671.617375497221 a=0.8
		clearLink("NGC7829","PK103+00.1");//NGC7829 PK103+00.1 dst=4468.466103759071 a=0.7
		clearLink("NGC3104","PK359-01.1");//NGC3104 PK359-01.1 dst=7716.591794431784 a=3.3
		clearLink("NGC5257","PK285-05.1");//NGC5257 PK285-05.1 dst=4473.77648265486 a=1.8
		clearLink("NGC70","PK007-03.1");//NGC70 PK007-03.1 dst=6118.313001346637 a=1.4
		clearLink("NGC6041","PK025-04.2");//NGC6041 PK025-04.2 dst=2980.10153229125 a=1.2
		clearLink("NGC4061","PK009-05.1");//NGC4061 PK009-05.1 dst=6154.387072092468 a=1.2
		clearLink("NGC7318","PK118+08.1");//NGC7318 PK118+08.1 dst=2305.0232022792297 a=1.2
		clearLink("NGC7753","PK130-11.1");//NGC7753 PK130-11.1 dst=1766.6592784971756 a=3.3
		clearLink("NGC6099","PK017-02.1");//NGC6099 PK017-02.1 dst=2901.1653120516735 a=1.3
		clearLink("NGC5910","PK003-02.2");//NGC5910 PK003-02.2 dst=3729.6855699935663 a=0.9
		clearLink("NGC4055","PK285-14.1");//NGC4055 PK285-14.1 dst=5724.156361470035 a=1.2
		clearLink("NGC5394","PK239+13.1");//NGC5394 PK239+13.1 dst=5584.6442488062985 a=1.7
		clearLink("NGC3664","PK082+07.1");//NGC3664 PK082+07.1 dst=6865.491370530772 a=2.0
		clearLink("NGC5544","PK064+15.1");//NGC5544 PK064+15.1 dst=3243.2552462737153 a=1.0
		clearLink("NGC985","PK106-17.1");//NGC985 PK106-17.1 dst=4016.945237801855 a=1.0
		clearLink("NGC4438","PK008-07.2");//NGC4438 PK008-07.2 dst=5793.558748340972 a=8.5
		clearLink("NGC4055","PK009-05.1");//NGC4055 PK009-05.1 dst=6154.387072092468 a=1.2
		clearLink("NGC2295","PK012-02.1");//NGC2295 PK012-02.1 dst=8016.299165928931 a=2.1
		clearLink("NGC4061","PK285-14.1");//NGC4061 PK285-14.1 dst=5724.156361470035 a=1.2
		clearLink("NGC3303","PK327+10.1");//NGC3303 PK327+10.1 dst=5382.360754049354 a=3.0
		clearLink("NGC4647","PK024-02.1");//NGC4647 PK024-02.1 dst=5544.78038309347 a=2.9
		clearLink("NGC3981","PK133-08.1");//NGC3981 PK133-08.1 dst=8364.147066034539 a=5.2
		clearLink("NGC6098","PK017-02.1");//NGC6098 PK017-02.1 dst=2901.735706672603 a=1.6
		clearLink("NGC7578","PK007-06.2");//NGC7578 PK007-06.2 dst=4995.867406639959 a=0.8
		clearLink("NGC67","PK007-03.1");//NGC67 PK007-03.1 dst=6115.773991839916 a=0.4
		clearLink("NGC5994","PK144+06.1");//NGC5994 PK144+06.1 dst=6066.412368783359 a=0.4
		clearLink("NGC5258","PK285-05.1");//NGC5258 PK285-05.1 dst=4473.737992651041 a=1.7
		clearLink("NGC386","PK015-04.1");//NGC386 PK015-04.1 dst=6350.831420140244 a=0.9
		clearLink("NGC388","PK015-04.1");//NGC388 PK015-04.1 dst=6353.503592657529 a=0.9
		clearLink("NGC7783","PK051+09.1");//NGC7783 PK051+09.1 dst=4614.1672267430595 a=1.3
		clearLink("NGC5954","PK082+11.1");//NGC5954 PK082+11.1 dst=3687.9333643347095 a=1.3
		clearLink("NGC7393","PK309-04.2");//NGC7393 PK309-04.2 dst=6052.690057648695 a=1.8
		clearLink("NGC5745","PK356+04.1");//NGC5745 PK356+04.1 dst=2338.2012690431375 a=1.7
		clearLink("NGC68","PK007-03.1");//NGC68 PK007-03.1 dst=6117.332043323175 a=2.0
		clearLink("NGC7715","PK261+08.1");//NGC7715 PK261+08.1 dst=7964.070384931154 a=2.6
		clearLink("NGC4194","PK084-03.1");//NGC4194 PK084-03.1 dst=4520.483014436588 a=1.8
		clearLink("NGC6027","PK006+03.2");//NGC6027 PK006+03.2 dst=3021.9598076644784 a=0.4
		clearLink("NGC2944","PK043+37.1");//NGC2944 PK043+37.1 dst=5404.835431723608 a=1.1
		clearLink("NGC5421","PK355-04.2");//NGC5421 PK355-04.2 dst=5237.076193404062 a=1.2
		clearLink("NGC5278","PK206-40.1");//NGC5278 PK206-40.1 dst=7675.775147208336 a=1.3
		clearLink("NGC750","PK014-04.1");//NGC750 PK014-04.1 dst=7032.803563700316 a=1.7
		clearLink("NGC4649","PK024-02.1");//NGC4649 PK024-02.1 dst=5542.6582084081 a=7.4
		clearLink("NGC71","PK007-03.1");//NGC71 PK007-03.1 dst=6118.163652705223 a=1.2
		clearLink("NGC1487","PK329-02.2");//NGC1487 PK329-02.2 dst=4954.949190306763 a=2.0
		clearLink("NGC5953","PK082+11.1");//NGC5953 PK082+11.1 dst=3688.597403250371 a=1.6
		clearLink("NGC7828","PK103+00.1");//NGC7828 PK103+00.1 dst=4468.079285126479 a=0.9
		clearLink("NGC5252","PK011+11.1");//NGC5252 PK011+11.1 dst=3593.161866449028 a=1.4
		clearLink("NGC751","PK014-04.1");//NGC751 PK014-04.1 dst=7032.858590843568 a=1.4
		clearLink("NGC4038","PK065+00.1");//NGC4038 PK065+00.1 dst=7398.745111569123 a=5.2
		clearLink("NGC5545","PK064+15.1");//NGC5545 PK064+15.1 dst=3242.6411494017425 a=1.3
		clearLink("NGC5410","PK058-10.1");//NGC5410 PK058-10.1 dst=4958.497810438343 a=1.5
		clearLink("NGC515","PK232-01.1");//NGC515 PK232-01.1 dst=5952.088813602799 a=1.4
		clearLink("NGC3786","PK041-02.1");//NGC3786 PK041-02.1 dst=6417.810455697367 a=2.2
		clearLink("NGC7436","PK349+04.1");//NGC7436 PK349+04.1 dst=6238.612302977486 a=2.0
		clearLink("NGC3656","PK215-24.1");//NGC3656 PK215-24.1 dst=5979.63988568435 a=1.6
		clearLink("NGC7433","PK349+04.1");//NGC7433 PK349+04.1 dst=6237.892303100368 a=0.7
		clearLink("NGC2798","PK265+04.1");//NGC2798 PK265+04.1 dst=5065.413001673822 a=2.6
		clearLink("NGC5996","PK144+06.1");//NGC5996 PK144+06.1 dst=6065.735376681978 a=1.7
		clearLink("NGC5829","PK130+01.1");//NGC5829 PK130+01.1 dst=5543.159092231657 a=1.8
		clearLink("NGC2445","PK011+05.1");//NGC2445 PK011+05.1 dst=8741.896825892994 a=1.4
		clearLink("IC4677","PK357-03.2");//IC4677 PK357-03.2 dst=5964.3885434197055 a=1.10000002384186
		clearLink("NGC383","PK015-04.1");//NGC383 PK015-04.1 dst=6350.089701440729 a=1.6
		clearLink("NGC4651","PK286-04.1");//NGC4651 PK286-04.1 dst=5043.604966052336 a=4.0
		clearLink("NGC508","PK012-09.1");//NGC508 PK012-09.1 dst=6528.029722524118 a=1.3
		clearLink("IC694","PK008+03.1");//IC694 PK008+03.1 dst=6580.175550983935 a=0.300000011920929
		clearLink("NGC3926","PK003-17.1");//NGC3926 PK003-17.1 dst=7012.16420899836 a=1.0
		clearLink("NGC3509","PK329+02.1");//NGC3509 PK329+02.1 dst=4959.579495380638 a=2.1
		clearLink("NGC7320","PK118+08.1");//NGC7320 PK118+08.1 dst=2305.866837673258 a=2.2
		clearLink("IC575","PK346-08.1");//IC575 PK346-08.1 dst=6244.002730555565 a=1.20000004768372
		clearLink("NGC5331","PK054-12.1");//NGC5331 PK054-12.1 dst=5707.337572375254 a=0.6
		clearLink("NGC4190","PK007+07.1");//NGC4190 PK007+07.1 dst=5614.47311267237 a=1.7
		clearLink("NGC5615","PK342+10.1");//NGC5615 PK342+10.1 dst=4529.120926528805 a=0.2
		clearLink("NGC517","PK232-01.1");//NGC517 PK232-01.1 dst=5950.303700578805 a=2.0
		clearLink("NGC3226","PK021-05.1");//NGC3226 PK021-05.1 dst=7737.2380762024195 a=3.2
		clearLink("NGC5216","PK215+03.1");//NGC5216 PK215+03.1 dst=5600.339089115413 a=2.5
		clearLink("NGC4039","PK065+00.1");//NGC4039 PK065+00.1 dst=7399.02819646659 a=3.1
		clearLink("NGC2535","PK138+02.1");//NGC2535 PK138+02.1 dst=3661.9205965063647 a=2.5
		clearLink("NGC4893","PK039-02.1");//NGC4893 PK039-02.1 dst=5394.1101863425765 a=1.0
		clearLink("NGC4490","PK189+07.1");//NGC4490 PK189+07.1 dst=4386.666103605431 a=6.3
		clearLink("NGC2536","PK138+02.1");//NGC2536 PK138+02.1 dst=3663.619339009569 a=0.9
		clearLink("NGC3256","PK307-03.1");//NGC3256 PK307-03.1 dst=1998.8684136691302 a=3.8
		clearLink("NGC2444","PK011+05.1");//NGC2444 PK011+05.1 dst=8741.709223063957 a=1.2
		clearLink("NGC3545","PK016-01.1");//NGC3545 PK016-01.1 dst=6880.429887066817 a=0.2
		clearLink("NGC5613","PK342+10.1");//NGC5613 PK342+10.1 dst=4530.646821762585 a=1.0
		clearLink("NGC2429","PK107-13.1");//NGC2429 PK107-13.1 dst=4216.040756196175 a=1.8
		clearLink("NGC3995","PK057-08.1");//NGC3995 PK057-08.1 dst=6401.678922911679 a=2.8
		clearLink("NGC2537","PK359-04.3");//NGC2537 PK359-04.3 dst=9086.386675625015 a=1.7
		clearLink("NGC1347","PK196-10.1");//NGC1347 PK196-10.1 dst=2707.335304292442 a=1.5
		clearLink("NGC382","PK015-04.1");//NGC382 PK015-04.1 dst=6349.786294063137 a=0.7
		clearLink("NGC7609","PK190-17.1");//NGC7609 PK190-17.1 dst=5092.242492816723 a=1.3
		clearLink("NGC2623","PK025+40.1");//NGC2623 PK025+40.1 dst=6296.71498825698 a=2.4
		clearLink("NGC4099","PK294+04.1");//NGC4099 PK294+04.1 dst=4671.617375497221 a=1.1
		clearLink("NGC14","PK331-01.1");//NGC14 PK331-01.1 dst=7168.073592635315 a=2.8
		clearLink("NGC7727","PK312+10.1");//NGC7727 PK312+10.1 dst=6624.4102470675325 a=4.7
		clearLink("NGC3395","PK060-03.1");//NGC3395 PK060-03.1 dst=6664.954024937524 a=2.1
		clearLink("NGC7752","PK130-11.1");//NGC7752 PK130-11.1 dst=1768.6680834243805 a=0.8
		clearLink("IC1165","PK353+06.1");//IC1165 PK353+06.1 dst=2919.1522405339265 a=0.400000005960464
		clearLink("NGC3690","PK008+03.1");//NGC3690 PK008+03.1 dst=6579.447941045993 a=2.4
		clearLink("NGC384","PK015-04.1");//NGC384 PK015-04.1 dst=6348.704465499315 a=1.1
		clearLink("NGC3445","PK171-25.1");//NGC3445 PK171-25.1 dst=4900.254482538079 a=1.6
		clearLink("NGC69","PK007-03.1");//NGC69 PK007-03.1 dst=6117.056916320062 a=0.9  */
		
		clearLink("IC694","PK008+03.1");//IC694 PK008+03.1 dst=6580.175550983935 a=0.300000011920929
		clearLink("NGC7318","PK118+08.1");//NGC7318 PK118+08.1 dst=2305.0232022792297 a=1.2
		clearLink("IC4677","PK357-03.2");//IC4677 PK357-03.2 dst=5964.3885434197055 a=1.10000002384186

	}
	
	/**
	 * list of discrepancies in respect of simbad coords
	 * @param where
	 * @throws Exception
	 */
	public static void getClearList(String where) throws Exception{
		Db db=new Db(PATH,DBNAME);
		List<String[]>list=db.exec("select dbname,name,a,dst from total "+where+";",-1);
		for(String[] a:list){
			p("clearAllNames(\""+a[1]+"\",\""+a[0]+"\");//dst="+a[3]+" a="+a[2]);
		}
	}
	
	public static void clearSACLinks() throws Exception{
		Db db=new Db(PATH,DBNAME);
		db.start();
		db.addCommand("begin;");		
		//manual processing of as the names are the same, wrongWithId("q1.dbname='hcngc.db'","q2.dbname='hsac.db' and (q2.name like 'ngc%' or q2.name like 'ic%')",2,0.5);
		//removed links where we need to correct hcngc links
		
		db.addCommand("update total set n4=null where id=55210;");//NGC78 NGC78B dst=100.01861811730276 a=1.1
		db.addCommand("update total set n2=null where id=10845;");//IC4016 NGC4893A dst=107.55024920561726 a=0.400000005960464
		db.addCommand("update total set n1=null where id=55209;");//NGC78 NGC78A dst=100.02049261848788 a=1.1
		db.addCommand("update total set n1=null where id=55210;");//NGC78 NGC78B dst=100.01861811730276 a=1.1
		db.addCommand("update total set n3=null where id=55493;");//NGC7783 NGC7783A dst=45.95271975039637 a=1.3
		db.addCommand("update total set n1=null where id=55493;");//NGC7783 NGC7783A dst=45.95271975039637 a=1.3
		db.addCommand("update total set n2=null where id=55209;");//NGC78 NGC78A dst=100.02049261848788 a=1.1
		db.addCommand("commit;");
		db.end(-1);
		clearAllNames("NGC78B", "hsac.db");
		


		clearLink(313,60094);//NGC336 MCG-03-03-011 26.06024058747663 0.7
		//313|hcngc.db|313|NGC336|PGC3470|ES0541-IG002|IRAS00555-1839||||||||||0.967555555555556|-18.3866666666667|||0.7||
		//60094|hsac.db|1839|MCG-03-03-011|NGC336|PGC3526|ESO541-4|IRASF00565-1900|||||||||0.98|-18.783332824707|0.982778866666667|-18.742656|3.0|3.4006373277884|
		
		clearLink(1574,66131);//NGC1673 SL17 3994.7645250615774 1.0
		//1574|hcngc.db|1574|NGC1673|ESO055-SC034|SL17|KMH90-43||||||||||4.71102777777778|-69.8213888888889|||1.0||
		//66131|hsac.db|7876|SL17|||||||||||||16.8833333333333|-43.5833320617676|16.8833333333333|-43.6|15.0|1.00007629421945|

		clearLink(1553,64142);//NGC1651 SL7 4041.682771705471 3.0
		//1553|hcngc.db|1553|NGC1651|ESO055-SC030|SL7|KMH90-20||||||||||4.62575|-70.5855555555555|||3.0||
		//64142|hsac.db|5887|SL7|||||||||||||16.03|-41.8666648864746|16.0433333333333|-41.707|60.0|13.1085007216464|

		//clearLink(34809,62298);//ABELL50 NGC6742 6122.413934139709 0.5
		//34809|abell.db|50|ABELL50|ACO50||||||||||||0.523366570472717|-22.2239990234375|0.523193333333333|-22.2522|0.5|1.69820184872843|
		//62298|hsac.db|4043|NGC6742|||||||||||||18.9883333333333|48.466667175293|18.9888972|48.465344||0.345730580558803|
		//6415|hcngc.db|6415|NGC6742|PK078+18.1|ABELL50|||||||||||18.9888611111111|48.4655555555556|||0.516666666666667|
		//убрать ABELL50

		clearField(6415,2);//6415|hcngc.db|6415|NGC6742|PK078+18.1|ABELL50|||||||||||18.9888611111111|48.4655555555556|||0.516666666666667|

		clearLink(65859,65858);//NGC2409 BOCHUM4 16.43229491643127 2.5
		//65859|hsac.db|7604|NGC2409|||||||||||||7.52666666666667|-17.1833324432373|7.52693333333333|-17.193|2.5|0.623724251116931|
		//65858|hsac.db|7603|BOCHUM4|NGC2409|FIRSSE 213|C0728-168|Bochum4|||||||||7.51666666666667|-16.9500007629395|7.52693333333333|-17.193|23.0|17.0468366950638|

		clearLink(2559,67712);//NGC2726 MRK18 6535.164915509792 1.6
		
		clearLink(67755,58789);//PISMIS16 VDBH81 3578.2846722601516 1.5
		//67755|hsac.db|9500|PISMIS16|OCl790|C0949-529|Pismis16|VDBH81|||||||||9.855|-53.1833343505859|9.85446666666667|-53.167|1.5|1.02141553862997|
		//58789|hsac.db|534|VDBH81|||||||||||||17.0666666666667|-51.0833320617676|17.0666666666667|-51.08|6.0|0.199923711887978|

		clearLink(7855,59607);//IC450 MRK6 1597.9900832479148 0.800000011920929
		//7855|hcngc.db|7855|IC450|PGC19756|UGC3547|MCG12-7-18|MK6|||||||||6.8701000213623|74.4274978637695|||0.800000011920929||
		//59607|hsac.db|1352|MRK6|IC450||||||||||||2.49333333333333|60.6500015258789|6.87010026666667|74.427011|4.5|1597.99270597187|

		clearLink(8749,59914);//IC1396 TR37 36.338441567539476 12.0
		//8749|hcngc.db|8749|IC1396|LBN451||||||||||||21.5750007629395|57.4667015075684|||12.0||
		//59914|hsac.db|1659|TR37|IC1396|OCl222|C2137+572|Collinder439|Trumpler37||||||||21.65|57.5|21.6357466666667|57.4467|50.0|7.60281477105821|

		clearLink(11430,64885);//IC4662 PK328-17.1 34.8330287447836 1.39999997615814
		//11430|hcngc.db|11430|IC4662|PGC61002|ESO102-16|IRAS17465-6456||||||||||17.860200881958|-64.9593963623047|||1.39999997615814||
		//64885|hsac.db|6630|PK328-17.1|IC4662|IRAS17422-6437|PGC60851|PGC60849|ESO102-14||||||||17.785|-64.6333312988281|17.7858866666667|-64.638681|0.2|0.468915910043099|

		clearLink(16069,59607);//UGC3547 MRK6 1597.5492906987045 1.0
		//16069|hugc.db|3554|UGC3547|IC450|IRAS06457+7429|PGC19756|MCG+12-07-018|Z330-17||||||||6.86783845606587|74.4077892474598|6.87010026666667|74.427011|1.0|1.27637415288439|
		//59607|hsac.db|1352|MRK6|IC450||||||||||||2.49333333333333|60.6500015258789|6.87010026666667|74.427011|4.5|1597.99270597187|

		clearLink(17254,67712);//UGC4730 MRK18 6548.2099665933465 0.9
		//17254|hugc.db|4739|UGC4730|IRAS08580+6020|PGC25370|MCG+10-13-052|Z288-17|||||||||9.03233487291692|60.1531605593985|9.03288166666667|60.151733|0.9|0.259467732722291|
		//67712|hsac.db|9457|MRK18|PGC25370|	MCG+10-13-052|UGC4730|Z288-17|||||||||9.01|-48.9833335876465|9.03288166666667|60.151733|2.0|6548.12532077022|

		clearLink(18842,66715);//UGC6315 MARKARIAN38 6815.0327392992085 1.1
		//18842|hugc.db|6327|UGC6315|PGC34553|MCG+09-19-034|Z268-17|IRASF11154+5401|||||||||11.3041338817551|53.7598527926607|11.3047414|53.749864|1.1|0.680953433301586|
		//66715|hsac.db|8460|MARKARIAN38|PGC34553|MCG+09-19-034|UGC6315|Z268-17|||||||||18.255|-18.9833335876465|11.3047414|53.749864|2.0|6814.70653387393|


		clearLink(24517,65013);//UGC11984 NGC7253A 60.91187049037997 1.8
		//24517|hugc.db|12002|UGC11984|||||||||||||22.3250872010193|28.3848024229221|22.3243611333333|29.395792|1.8||
		//65013|hsac.db|6758|NGC7253A|NGC7253|APG278|IRAS22171+2908|PGC68572|MCG+05-52-010|UGC11984|Z494-14||||||22.325|29.3999996185303|22.3243611333333|29.395792|1.7|0.560960213015024|


		clearLink(26055,66856);//LBN601 IC353 422.13518306102645 2.0
		//26055|hlbn.db|601|LBN601|IC353|Ced24|||||||||||3.752396|32.32278|3.74666666666667|32.4|2.0|6.35903925384094|
		//66856|hsac.db|8601|IC353|||||||||||||3.88333333333333|25.5|3.74666666666667|32.4|180.0|427.730175301926|

		clearLink(60091,60094);//NGC336 MCG-03-03-011 26.5588714862879 0.7
		//60091|hsac.db|1836|NGC336|||||||||||||0.966666666666667|-18.3833332061768|0.982778866666667|-18.742656|0.7|25.5689900900467|
		//60094|hsac.db|1839|MCG-03-03-011|NGC336|PGC3526|ESO541-4|IRASF00565-1900|||||||||0.98|-18.783332824707|0.982778866666667|-18.742656|3.0|3.4006373277884|


		clearLink(58789,67755);//VDBH81 PISMIS16 3578.2846722601516 6.0
		//58789|hsac.db|534|VDBH81|||||||||||||17.0666666666667|-51.0833320617676|17.0666666666667|-51.08|6.0|0.199923711887978|
		//67755|hsac.db|9500|PISMIS16|OCl790|C0949-529|Pismis16|VDBH81|||||||||9.855|-53.1833343505859|9.85446666666667|-53.167|1.5|1.02141553862997|

		
		
	}
	/*public static void clearUGCLinks() throws Exception{
		Db db=new Db(PATH,DBNAME);
		String sql="update TOTAL set n1=null,n2=null,n3=null,n4=null,n5=null,n6=null,n7=null,n8=null,n9=null,n10=null," +
				"n11=null,n12=null,dst=null where dbname='hugc.db' and dst>max(4,a/2);";
		db.exec(sql, -1);
		
		//wrong("(q1.name like 'ngc%' or q1.name like 'ic%')","q2.name like 'ugc%'",3,0.3);

		clearLink("NGC6669","UGC11302");//NGC6669 UGC11302 dst=9.944536830240525 a=0.0
		clearLink("IC1190","UGC10199");//IC1190 UGC10199 dst=5.480105426787147 a=1.29999995231628
		clearLink("NGC5480","UGC9026");//NGC5480 UGC9026 dst=18.781106658197835 a=1.7
		clearLink("IC1100","UGC9764");//IC1100 UGC9764 dst=118.26375536818581 a=0.800000011920929
		clearLink("NGC4272","UGC7378");//NGC4272 UGC7378 dst=9.968912035742834 a=1.0
		clearLink("NGC5276","UGC8675");//NGC5276 UGC8675 dst=4.4744979292902025 a=1.0
		clearLink("NGC5474","UGC8981");//NGC5474 UGC8981 dst=43.721851014700285 a=4.8
		clearLink("NGC296","UGC565");//NGC296 UGC565 dst=8.101957817828211 a=1.2
		clearLink("NGC7727","UGC12720");//NGC7727 UGC12720 dst=1067.2703400704709 a=4.7
		clearLink("IC2613","UGC5982");//IC2613 UGC5982 dst=29.60504037108157 a=2.09999990463257
		clearLink("NGC364","UGC666");//NGC364 UGC666 dst=25.790047168041994 a=1.4
		clearLink("NGC5649","UGC9333");//NGC5649 UGC9333 dst=5.119496626621682 a=1.0
		clearLink("IC830","UGC8003");//IC830 UGC8003 dst=20.05770825936687 a=0.800000011920929
		clearLink("NGC1062","UGC2201");//NGC1062 UGC2201 dst=4.377464462089022 a=0.0
		clearLink("NGC7304","UGC12065");//NGC7304 UGC12065 dst=3.2622768646692513 a=0.0
		clearLink("IC107","UGC978");//IC107 UGC978 dst=3.5959918581382566 a=1.0
		clearLink("IC1488","UGC12586");//IC1488 UGC12586 dst=12.90553696798771 a=0.899999976158142
		clearLink("NGC5717","UGC9459");//NGC5717 UGC9459 dst=2592.7311997321735 a=0.7
		clearLink("NGC3920","UGC6803");//NGC3920 UGC6803 dst=9.782801580504842 a=1.3
		clearLink("NGC3991","UGC6936");//NGC3991 UGC6936 dst=3.9925832228251354 a=1.4
		clearLink("NGC7727","UGC12721");//NGC7727 UGC12721 dst=2364.248341998713 a=4.7
		clearLink("NGC5569","UGC9175");//NGC5569 UGC9175 dst=4.950604682634081 a=1.7
		clearLink("NGC7112","UGC11794");//NGC7112 UGC11794 dst=4.783615685334331 a=0.9
		clearLink("IC843","UGC8137");//IC843 UGC8137 dst=179.9651429737199 a=1.10000002384186
		clearLink("IC4867","UGC11438");//IC4867 UGC11438 dst=22.452146639147923 a=1.20000004768372
		clearLink("NGC2630","UGC4547");//NGC2630 UGC4547 dst=6.9413251873668225 a=0.0
		clearLink("NGC3500","UGC6056");//NGC3500 UGC6056 dst=9.80235171583667 a=1.5
		clearLink("NGC1312","UGC2711");//NGC1312 UGC2711 dst=9.090406948152907 a=0.0
		clearLink("NGC3807","UGC6641");//NGC3807 UGC6641 dst=15.543657189011554 a=0.0
		clearLink("NGC3924","UGC6849");//NGC3924 UGC6849 dst=15.651507472452534 a=1.9
		clearLink("NGC162","UGC354");//NGC162 UGC354 dst=3.0735739974627374 a=0.0
		clearLink("NGC305","UGC571");//NGC305 UGC571 dst=5.377737659262541 a=0.0
		clearLink("NGC4435","UGC7574");//NGC4435 UGC7574 dst=4.406214679691653 a=2.8
		clearLink("NGC2965","UGC5191");//NGC2965 UGC5191 dst=3.7885837939974656 a=1.2
		clearLink("NGC6635","UGC11239");//NGC6635 UGC11239 dst=3.3000100371403724 a=1.0
		clearLink("NGC4063","UGC7042");//NGC4063 UGC7042 dst=4.038675739367724 a=1.2
		clearLink("IC5195","UGC1345");//IC5195 UGC1345 dst=2573.2330151681135 a=0.300000011920929
		clearLink("NGC6965","UGC11630");//NGC6965 UGC11630 dst=5.91485809936364 a=0.7
		clearLink("NGC4625","UGC7853");//NGC4625 UGC7853 dst=8.150944917795394 a=2.2
		clearLink("NGC5222","UGC8559");//NGC5222 UGC8559 dst=5.165015994016735 a=1.6
		clearLink("NGC6393","UGC10889");//NGC6393 UGC10889 dst=7.1523912798409475 a=0.3
		clearLink("NGC3911","UGC6795");//NGC3911 UGC6795 dst=9.733920280039982 a=1.4
		clearLink("NGC6763","UGC11405");//NGC6763 UGC11405 dst=16.689309258452976 a=1.6
		clearLink("IC960","UGC8849");//IC960 UGC8849 dst=179.6164977050519 a=1.39999997615814
		clearLink("NGC1259","UGC10869");//NGC1259 UGC10869 dst=4588.741158699243 a=0.6
		clearLink("NGC6685","UGC1182");//NGC6685 UGC1182 dst=4150.967513376866 a=1.1
		clearLink("IC1301","UGC11438");//IC1301 UGC11438 dst=22.452146639147923 a=1.20000004768372
		clearLink("NGC7825","UGC37");//NGC7825 UGC37 dst=5.476950303399689 a=1.3
		clearLink("NGC470","UGC864");//NGC470 UGC864 dst=5.0222715054719895 a=2.8
		clearLink("NGC4495","UGC7663");//NGC4495 UGC7663 dst=59.72680319550892 a=1.4
		clearLink("NGC7739","UGC12757");//NGC7739 UGC12757 dst=13.210182498585429 a=1.1
		clearLink("NGC4007","UGC6948");//NGC4007 UGC6948 dst=120.06696771555559 a=1.2
		clearLink("NGC4171","UGC7204");//NGC4171 UGC7204 dst=4.19538845487045 a=0.0
		clearLink("NGC2631","UGC4547");//NGC2631 UGC4547 dst=6.9413251873668225 a=0.0
		clearLink("NGC5656","UGC9332");//NGC5656 UGC9332 dst=10.542370391718551 a=1.9
		clearLink("NGC72","UGC174");//NGC72 UGC174 dst=3.3226128236657035 a=1.1
		clearLink("NGC4912","UGC8125");//NGC4912 UGC8125 dst=3.81259246618637 a=0.0
		clearLink("NGC6550","UGC11115");//NGC6550 UGC11115 dst=3.8744632725080246 a=1.4
		clearLink("NGC7756","UGC12788");//NGC7756 UGC12788 dst=5.935445617254682 a=0.0
		clearLink("NGC2819","UGC2924");//NGC2819 UGC2924 dst=4401.160600355028 a=1.4
		clearLink("NGC4017","UGC6954");//NGC4017 UGC6954 dst=6.672217787548209 a=1.8
		clearLink("NGC6122","UGC10343");//NGC6122 UGC10343 dst=4.60446420356557 a=0.9
		clearLink("IC3322","UGC7518");//IC3322 UGC7518 dst=20.477737010962272 a=3.40000009536743
		clearLink("NGC5909","UGC9778");//NGC5909 UGC9778 dst=14.206889478331167 a=1.1
		clearLink("NGC4186","UGC7223");//NGC4186 UGC7223 dst=11.392610783680924 a=1.1
		clearLink("IC783","UGC7415");//IC783 UGC7415 dst=10.011103937847981 a=0.5
		clearLink("NGC7353","UGC12134");//NGC7353 UGC12134 dst=39.01853930656579 a=0.6
		clearLink("NGC618","UGC1140");//NGC618 UGC1140 dst=5.185296421689137 a=1.6
		clearLink("NGC67","UGC174");//NGC67 UGC174 dst=3.5114866459165204 a=0.4
		clearLink("NGC4023","UGC6971");//NGC4023 UGC6971 dst=324.97642356625806 a=0.9
		clearLink("NGC7316","UGC12098");//NGC7316 UGC12098 dst=3.231040665782558 a=1.1
		clearLink("NGC1015","UGC2124");//NGC1015 UGC2124 dst=184.10750091853066 a=2.6
		clearLink("NGC4555","UGC7762");//NGC4555 UGC7762 dst=180.11733556090297 a=1.9
		clearLink("NGC69","UGC174");//NGC69 UGC174 dst=3.3924233481367536 a=0.9
		clearLink("NGC7253","UGC11984");//NGC7253 UGC11984 dst=60.69926444913587 a=1.8
		clearLink("IC1502","UGC12105");//IC1502 UGC12105 dst=227.1229396215514 a=1.10000002384186
		clearLink("NGC7663","UGC12462");//NGC7663 UGC12462 dst=883.5690750893308 a=0.9
		clearLink("NGC3165","UGC5512");//NGC3165 UGC5512 dst=9.45092357112369 a=1.6
		clearLink("NGC7547","UGC12456");//NGC7547 UGC12456 dst=3.4141002682610972 a=1.1
		clearLink("IC3881","UGC8036");//IC3881 UGC8036 dst=3.705049732786066 a=0.200000002980232
		clearLink("IC1111","UGC9800");//IC1111 UGC9800 dst=56.30807682727407 a=2.40000009536743
		clearLink("NGC7514","UGC12415");//NGC7514 UGC12415 dst=9.563431675446743 a=1.4
		clearLink("NGC21","UGC98");//NGC21 UGC98 dst=21.608672179933063 a=1.6
		clearLink("NGC2909","UGC5188");//NGC2909 UGC5188 dst=42.78102735892678 a=0.0
		clearLink("IC4301","UGC8579");//IC4301 UGC8579 dst=4.785375825496274 a=0.899999976158142
		clearLink("NGC3732","UGC6553");//NGC3732 UGC6553 dst=1342.1458078494518 a=1.2
		clearLink("NGC6762","UGC11405");//NGC6762 UGC11405 dst=16.689309258452976 a=1.4
		clearLink("IC1700","UGC978");//IC1700 UGC978 dst=3.5959918581382566 a=1.0
		clearLink("NGC6237","UGC10564");//NGC6237 UGC10564 dst=19.081715551075877 a=0.0
		clearLink("NGC7339","UGC12122");//NGC7339 UGC12122 dst=9.573017275120788 a=3.0
		clearLink("IC909","UGC8661");//IC909 UGC8661 dst=6.8778366405593605 a=0.400000005960464
		clearLink("NGC4338","UGC7459");//NGC4338 UGC7459 dst=22.23326198432751 a=2.3
		clearLink("NGC6071","UGC10157");//NGC6071 UGC10157 dst=12.755486945311695 a=0.8
		clearLink("IC3582","UGC7778");//IC3582 UGC7778 dst=3.157661151810317 a=0.200000002980232
		clearLink("IC2308","UGC4355");//IC2308 UGC4355 dst=16.800254639810966 a=0.600000023841858
		clearLink("NGC3984","UGC6943");//NGC3984 UGC6943 dst=64.01112337127273 a=1.4
		clearLink("NGC3751","UGC6601");//NGC3751 UGC6601 dst=7.923272535679223 a=0.8
		clearLink("NGC3187","UGC5559");//NGC3187 UGC5559 dst=5.514435630865922 a=3.0
		clearLink("NGC2519","UGC4221");//NGC2519 UGC4221 dst=6.924863571058518 a=0.0
		clearLink("NGC5881","UGC9764");//NGC5881 UGC9764 dst=118.23168888095525 a=1.0
		clearLink("NGC6473","UGC10989");//NGC6473 UGC10989 dst=4.7004259821909695 a=0.0
		clearLink("NGC6111","UGC10304");//NGC6111 UGC10304 dst=43.10386455342219 a=1.6
		clearLink("NGC5865","UGC9742");//NGC5865 UGC9742 dst=4.159966445907293 a=1.1
		clearLink("NGC5195","UGC8493");//NGC5195 UGC8493 dst=4.5121898029264855 a=5.8
		clearLink("NGC1037","UGC2119");//NGC1037 UGC2119 dst=31.282379024411583 a=0.0
		clearLink("NGC4895","UGC8113");//NGC4895 UGC8113 dst=7.3802901003334735 a=1.8
		clearLink("NGC7459","UGC12302");//NGC7459 UGC12302 dst=5.7060450690693045 a=0.8
		clearLink("IC3545","UGC7762");//IC3545 UGC7762 dst=180.04994646324351 a=1.89999997615814
		clearLink("NGC700","UGC1336");//NGC700 UGC1336 dst=3.1331400925157182 a=0.9
		clearLink("IC960","UGC8849");//IC960 UGC8849 dst=180.3539526174882 a=0.899999976158142
		clearLink("NGC3917","UGC6825");//NGC3917 UGC6825 dst=11.685062262295244 a=5.1
		clearLink("IC17","UGC275");//IC17 UGC275 dst=11.469859530277038 a=0.5
		clearLink("NGC7549","UGC12456");//NGC7549 UGC12456 dst=5.113752953189462 a=2.8
		clearLink("IC897","UGC8544");//IC897 UGC8544 dst=10.020268970487688 a=0.600000023841858
		clearLink("NGC6797","UGC11432");//NGC6797 UGC11432 dst=5209.601654423172 a=0.0
		clearLink("NGC4173","UGC384");//NGC4173 UGC384 dst=7070.363756857553 a=5.0
		clearLink("NGC2276","UGC3798");//NGC2276 UGC3798 dst=6.490012760281858 a=2.6
		clearLink("NGC7682","UGC12618");//NGC7682 UGC12618 dst=4.849709787739856 a=1.2
		clearLink("NGC5658","UGC9348");//NGC5658 UGC9348 dst=39.64491548381898 a=0.0
		
		//compareWithItself("(q1.name like 'ugc%')","(q2.name like 'ugc%')");
		db.exec("update TOTAL set n1=null where name='UGC3405';",-1);
		db.exec("update TOTAL set n1=null where name='UGC3410';",-1);
				
	}*/
	
	
	
	/**
	 * when clearing fields for cross refs obj1 is main and obj2 field is cleared
	 * 
	 * @param obj1
	 * @param obj2
	 * @throws Exception
	 */
	public static void clearLink(String obj1,String obj2) throws Exception{
		
		Db db=new Db(PATH,DBNAME);	
		//obj1 name,obj2 n
		for(int j=1;j<12;j++){
			String sql="select q2.id from TOTAL as q1 join TOTAL as q2 on q1.name=q2.n"+j+" " +
					"where q2.name like '"+obj2+"' and q1.name like '"+obj1+"';";
			List<String[]>list=db.exec(sql,-1);
			for(String[] a:list){
				sql="update TOTAL set n"+j+"=null where id="+a[0]+";";
				db.exec(sql, -1);
			}
		}
		//obj1 n,obj2 name
		for(int j=1;j<12;j++){
			String sql="select q1.id from TOTAL as q1 join TOTAL as q2 on q2.name=q1.n"+j+" " +
					"where q2.name like '"+obj2+"' and q1.name like '"+obj1+"';";
			List<String[]>list=db.exec(sql,-1);
			for(String[] a:list){
				sql="update TOTAL set n"+j+"=null where id="+a[0]+";";
				db.exec(sql, -1);
			}
		}
		
		//fields cross refs
		for(int i=1;i<=12;i++){
			for(int j=1;j<12;j++){
				String sql="select q2.id from TOTAL as q1 join TOTAL as q2 on q1.n"+i+"=q2.n"+j+" " +
						"where q2.name like '"+obj2+"' and q1.name like '"+obj1+"';";//and q1.dst<3 and q2.dst<3
				List<String[]>list=db.exec(sql,-1);
				for(String[] a:list){
					sql="update TOTAL set n"+j+"=null where id="+a[0]+";";
					db.exec(sql, -1);
				}

			}
		}
		p("cleared "+obj1+" "+obj2);
	}
	
	/**
	 * getting info on all objects with the same cid
	 * @param id
	 * @throws Exception 
	 */
	public static void getAllObjs(int id) throws Exception{
		Db db=new Db(PATH,DBNAME);
		List<String[]>list=db.exec("select cid from refs where id="+id+";", -1);
		String cid=list.get(0)[0];
		list=db.exec("select id from refs where cid="+cid+";",-1);
		for(String[] a:list){
			List<String[]>ll=db.exec("select * from total where id="+a[0]+";", -1);
			for(String[] a2:ll){
				for(String s:a2){
					System.out.print(s+"|");
				}
				System.out.println();
			}
		}
	}
	
	public static void clearField(int id,int field) throws Exception{
		Db db=new Db(PATH,DBNAME);
		String sql="update TOTAL set n"+field+"=null where id="+id+";";
		db.exec(sql, -1);
	}
	/**
	 * when clearing fields for cross refs obj1 is main and obj2 field is cleared
	 * 
	 * @param obj1
	 * @param obj2
	 * @throws Exception
	 */
	public static void clearLink(int id1,int id2) throws Exception{
		
		Db db=new Db(PATH,DBNAME);	
		//obj1 name,obj2 n
		for(int j=1;j<12;j++){
			String sql="select q2.id from TOTAL as q1 join TOTAL as q2 on q1.name=q2.n"+j+" " +
					"where q2.id="+id2+" and q1.id="+id1+";";
			List<String[]>list=db.exec(sql,-1);
			for(String[] a:list){
				sql="update TOTAL set n"+j+"=null where id="+a[0]+";";
				db.exec(sql, -1);
			}
		}
		//obj1 n,obj2 name
		for(int j=1;j<12;j++){
			String sql="select q1.id from TOTAL as q1 join TOTAL as q2 on q2.name=q1.n"+j+" " +
					"where q2.id="+id2+" and q1.id="+id1+";";
			List<String[]>list=db.exec(sql,-1);
			for(String[] a:list){
				sql="update TOTAL set n"+j+"=null where id="+a[0]+";";
				db.exec(sql, -1);
			}
		}
		
		//fields cross refs
		for(int i=1;i<=12;i++){
			for(int j=1;j<12;j++){
				String sql="select q2.id from TOTAL as q1 join TOTAL as q2 on q1.n"+i+"=q2.n"+j+" " +
						"where q2.id="+id2+" and q1.id="+id1+";";//and q1.dst<3 and q2.dst<3
				List<String[]>list=db.exec(sql,-1);
				for(String[] a:list){
					sql="update TOTAL set n"+j+"=null where id="+a[0]+";";
					db.exec(sql, -1);
				}

			}
		}
		p("cleared "+id1+" "+id2);
	}
	/**
	 * finding cross link  name - n
	 * @param sql like
	 * "select q1.name,q2.name,q1.ra,q1.dec,q2.ra,q2.dec,q1.a from TOTAL as q1 join TOTAL as q2 on q1.n1=q2.name " +
		"where q2.name like 'ugc%' and (q1.name like 'ngc%' or q1.name like 'ic%');";//and q1.dst<3 and q2.dst<3

	 * @throws Exception
	 */
	public static void wrong(String sql,double limit)throws Exception{
		Db db=new Db(PATH,DBNAME);	
		List<String[]>list=db.exec(sql,-1);
		int j=0;
		for(String[] a:list){
			double ra1=Double.parseDouble(a[2]);
			double dec1=Double.parseDouble(a[3]);
			double ra2=Double.parseDouble(a[4]);
			double dec2=Double.parseDouble(a[5]);
			double dst=distance(ra1, dec1, ra2, dec2)*60;
			if(dst>Math.max(limit,Double.parseDouble(a[6]))){
				p(a[0]+" "+a[1]+" "+dst+" "+a[6]);
				j++;
			}
		}
		p("total="+j);
	}
	/**
	 * first contains links to second, but second does not to first and vice versa
	 * @param first
	 * @param second
	 * @throws Exception 
	 */
	
	public static void diffLinks(String first,String second) throws Exception{
		Set<String>set1=new HashSet<String>();
		Db db=new Db(PATH,DBNAME);	
		//first name - second n
		for(int j=1;j<12;j++){
			String sql="select q1.name,q2.name,q1.ra,q1.dec,q2.ra,q2.dec,q1.a from TOTAL as q1 join TOTAL as q2 on q1.name=q2.n"+j+" " +
					"where "+second+" and "+first+";";//and q1.dst<3 and q2.dst<3
			List<String[]>list=db.exec(sql,-1);

			for(String[] a:list){
				double ra1=Double.parseDouble(a[2]);
				double dec1=Double.parseDouble(a[3]);
				double ra2=Double.parseDouble(a[4]);
				double dec2=Double.parseDouble(a[5]);
				double dst=distance(ra1, dec1, ra2, dec2)*60;
				
				set1.add("clearLink(\""+a[0]+"\",\""+a[1]+"\");//"+a[0]+" "+a[1]+" dst="+dst+" a="+a[6]);

				
			}
			p(""+j);
		}
		Set<String>set2=new HashSet<String>();
		//second name - first n
		for(int j=1;j<12;j++){
			String sql="select q1.name,q2.name,q1.ra,q1.dec,q2.ra,q2.dec,q1.a from TOTAL as q1 join TOTAL as q2 on q2.name=q1.n"+j+" " +
					"where "+second+" and "+first+";";//and q1.dst<3 and q2.dst<3
			List<String[]>list=db.exec(sql,-1);

			for(String[] a:list){
				double ra1=Double.parseDouble(a[2]);
				double dec1=Double.parseDouble(a[3]);
				double ra2=Double.parseDouble(a[4]);
				double dec2=Double.parseDouble(a[5]);
				double dst=distance(ra1, dec1, ra2, dec2)*60;
				
					set2.add("clearLink(\""+a[0]+"\",\""+a[1]+"\");//"+a[0]+" "+a[1]+" dst="+dst+" a="+a[6]);

				
			}
			p(""+j);

		}
		p("links second to first only " +second+"\n\n");
		
		Set<String>set11=new HashSet<String>(set1);
		set11.removeAll(set2);
		for(String s:set11){
			p(s);
		}
		
		
		p("links first to second only "+first+" \n\n");
		Set<String>set21=new HashSet<String>(set2);
		set21.removeAll(set1);
		for(String s:set21){
			p(s);
		}
		
		/*p("in both\n\n");
		
		set1.retainAll(set2);
		for(String s:set1){
			p(s);
		}*/
	}
	/**
	 * return wrong links to PGC objects, need to clear all fields n1-n12
	 * @throws Exception
	 */
	public static void wrongNgcPgc() throws Exception{
		Set<String>set=new HashSet<String>();
		Db db=new Db(PATH,DBNAME);	
		db.start();
		db.addCommand("attach database '"+PATH+"pgcnames.db' as pgc;");
		String sql="select q1.name,q2.n1,q1.ra,q1.dec,q2.ra,q2.dec,q1.a from total as q1 join names as q2 on q1.n1=q2.n1 where dbname='hcngc.db';";
		db.addCommand(sql);

		List<String[]>list=db.end(-1);
		p("list size="+list.size());
		int j=0;
		for(String[] a:list){
			double ra1=Double.parseDouble(a[2]);
			double dec1=Double.parseDouble(a[3]);
			double ra2=Double.parseDouble(a[4]);
			double dec2=Double.parseDouble(a[5]);
			double dst=distance(ra1, dec1, ra2, dec2)*60;
			if(dst>Math.max(Double.parseDouble(a[6])/4, 1)){
				String s="clearAllNames(\""+a[0]+"\",\"hcngc.db\");//"+a[0]+" "+a[1]+" dst="+dst+" a="+a[6];
			//	set.add(s);
				p(s);
				j++;

			}
		}
		p(""+j);
		
	}
	
	/**
	 * return wrong links to PGC objects (core), need to clear all fields n1-n12
	 * for those objects which do not have links to pgcnames
	 * @throws Exception
	 */
	public static void wrongNgcPgc2() throws Exception{
		
		Db db=new Db(PATH,DBNAME);	
		db.start();
		db.addCommand("attach database '"+PATH+"sqdb.db' as pgc;");
		db.addCommand("attach database '"+PATH+"pgcnames.db' as names;");
		//corresponding ngc objects that do not have links to names to core
		String sql="select q1.name,q1.n1,q1.ra,q1.dec,q2.ra,q2.dec,q1.a from (select name,dbname,n1,ra,dec,a from total where n1 like 'pgc%' and n1 not in (select n1 from names)) as q1 join core as q2 on q1.n1=q2.name where q1.dbname='hcngc.db';";
		db.addCommand(sql);

		List<String[]>list=db.end(-1);
		p("list size="+list.size());
		int j=0;
		for(String[] a:list){
			double ra1=Double.parseDouble(a[2]);
			double dec1=Double.parseDouble(a[3]);
			double ra2=Double.parseDouble(a[4]);
			double dec2=Double.parseDouble(a[5]);
			double dst=distance(ra1, dec1, ra2, dec2)*60;
			if(dst>Math.max(Double.parseDouble(a[6])/4, 1)){
				String s="clearAllNames(\""+a[0]+"\",\"hcngc.db\");//"+a[0]+" "+a[1]+" dst="+dst+" a="+a[6];
			//	set.add(s);
				p(s);
				j++;

			}
		}
		p(""+j);
		//select * from total where n1 like 'pgc%' and n1 not in (select n1 from names)
	}
	/**
	 * clear ngc objects not corresponding to pgcnames.db and core.db
	 * @throws Exception 
	 */
	public static void clearRemainingNgcLinks() throws Exception{
		Db db=new Db(PATH,DBNAME);	
		db.start();
		db.addCommand("attach database '"+PATH+"sqdb.db' as pgc;");
		db.addCommand("attach database '"+PATH+"pgcnames.db' as names;");
		//corresponding ngc objects that do not have links to names and to core
		String sql="select id from (select id,n1 from total where n1 like 'pgc%' and n1 not in (select n1 from names) and dbname='hcngc.db') where n1 not in " +
				"(select name from core where name like 'pgc%') ;";
		db.addCommand(sql);
		List<String[]>list=db.end(-1);
		db.start();
		db.addCommand("begin;");
		for(String[] a:list){
			String sql2="update TOTAL set n1=null,n2=null,n3=null,n4=null,n5=null,n6=null,n7=null,n8=null,n9=null,n10=null," +
					"n11=null,n12=null,dst=null where id="+a[0]+";";
			db.addCommand(sql2);

		}
		db.addCommand("commit;");
		db.end(-1);
		
		//remaining ngc objects, clear all except n1
		List<String[]>list2=db.exec("select id from total where n1 like 'pgc%' and (n2 is not null or n3 is not null or n4 is not null or n5 is not null or n6 is not null or n7 is not null) and dbname='hcngc.db';", -1);
		db.start();
		db.addCommand("begin;");
		for(String[] a:list2){
			String sql3="update TOTAL set n2=null,n3=null,n4=null,n5=null,n6=null,n7=null,n8=null,n9=null,n10=null," +
					"n11=null,n12=null,dst=null where id="+a[0]+";";
			db.addCommand(sql3);
		}
		db.addCommand("commit;");
		db.end(-1);
	}

	public static void wrongUgcPgc() throws Exception{
		Set<String>set=new HashSet<String>();
		Db db=new Db(PATH,DBNAME);	
		db.start();
		db.addCommand("attach database '"+PATH+"pgcnames.db' as pgc;");
		String sql="select q1.name,q2.n1,q1.ra,q1.dec,q2.ra,q2.dec,q1.a from total as q1 join names as q2 on q1.name=q2.n3;";
		db.addCommand(sql);

		List<String[]>list=db.end(-1);
		p("list size="+list.size());
		int j=0;
		for(String[] a:list){
			double ra1=Double.parseDouble(a[2]);
			double dec1=Double.parseDouble(a[3]);
			double ra2=Double.parseDouble(a[4]);
			double dec2=Double.parseDouble(a[5]);
			double dst=distance(ra1, dec1, ra2, dec2)*60;
			if(dst>Math.max(Double.parseDouble(a[6])/2, 3)){
				String s="clearLink(\""+a[0]+"\",\""+a[1]+"\");//"+a[0]+" "+a[1]+" dst="+dst+" a="+a[6];
			//	set.add(s);
				p(s);
				j++;

			}
		}
		p(""+j);
		
	}
	
	
	
	/**
	 * 
	 * 
	 * @param first - first catalog: q1.name like 'ngc%' or q1.name like 'ic%'
	 * @param second - second catalog: q2.name like 'ugc%'
	 * @param limit
	 * @param coeff  dst>Math.max(a*coeff,limit)
	 * @throws Exception
	 */
	public static void wrong(String first,String second,double limit,double coeff) throws Exception{
		Set<String>set=new HashSet<String>();
		Db db=new Db(PATH,DBNAME);	
		
		//first name - second n
		for(int j=1;j<=12;j++){
			String sql="select q1.name,q2.name,q1.ra,q1.dec,q2.ra,q2.dec,q1.a from TOTAL as q1 join TOTAL as q2 on q1.name=q2.n"+j+" " +
					"where "+second+" and "+first+";";//and q1.dst<3 and q2.dst<3
			List<String[]>list=db.exec(sql,-1);
			
			for(String[] a:list){
				double ra1=Double.parseDouble(a[2]);
				double dec1=Double.parseDouble(a[3]);
				double ra2=Double.parseDouble(a[4]);
				double dec2=Double.parseDouble(a[5]);
				double dst=distance(ra1, dec1, ra2, dec2)*60;
				if(dst>Math.max(Double.parseDouble(a[6])*coeff, limit)){
					String s="clearLink(\""+a[0]+"\",\""+a[1]+"\");//"+a[0]+" "+a[1]+" dst="+dst+" a="+a[6];
					set.add(s);
					//p(s);
					
				}
			}
			p(""+j);
		}
		//second name - first n
		for(int j=1;j<=12;j++){
			String sql="select q1.name,q2.name,q1.ra,q1.dec,q2.ra,q2.dec,q1.a from TOTAL as q1 join TOTAL as q2 on q2.name=q1.n"+j+" " +
					"where "+second+" and "+first+";";//and q1.dst<3 and q2.dst<3
			List<String[]>list=db.exec(sql,-1);

			for(String[] a:list){
				double ra1=Double.parseDouble(a[2]);
				double dec1=Double.parseDouble(a[3]);
				double ra2=Double.parseDouble(a[4]);
				double dec2=Double.parseDouble(a[5]);
				double dst=distance(ra1, dec1, ra2, dec2)*60;
				if(dst>Math.max(Double.parseDouble(a[6])*coeff, limit)){
					String s="clearLink(\""+a[0]+"\",\""+a[1]+"\");//"+a[0]+" "+a[1]+" dst="+dst+" a="+a[6];
					set.add(s);
					//p(s);

				}
			}
			p(""+j);

		}


	
		// n - n
		
		for(int i=1;i<=12;i++){
			for(int j=1;j<=12;j++){
				String sql="select q1.name,q2.name,q1.ra,q1.dec,q2.ra,q2.dec,q1.a from TOTAL as q1 join TOTAL as q2 on q1.n"+i+"=q2.n"+j+" " +
						"where "+second+" and "+first+";";//and q1.dst<3 and q2.dst<3
				List<String[]>list=db.exec(sql,-1);
				//p("size="+list.size());
				for(String[] a:list){
					double ra1=Double.parseDouble(a[2]);
					double dec1=Double.parseDouble(a[3]);
					double ra2=Double.parseDouble(a[4]);
					double dec2=Double.parseDouble(a[5]);
					double dst=distance(ra1, dec1, ra2, dec2)*60;
					if(dst>Math.max(Double.parseDouble(a[6])*coeff, limit)){
						String s="clearLink(\""+a[0]+"\",\""+a[1]+"\");//"+a[0]+" "+a[1]+" dst="+dst+" a="+a[6];
						set.add(s);
						//p(s);
						
					}
				}
				p(i+" "+j);
			}
		}
		for(String s:set){
			p(s);
		}
	}
	/**
	 * 
	 * 
	 * @param first - first catalog: q1.name like 'ngc%' or q1.name like 'ic%'
	 * @param second - second catalog: q2.name like 'ugc%'
	 * @param limit
	 * @param coeff  dst>Math.max(a*coeff,limit)
	 * @throws Exception
	 */
	public static void wrongWithId(String first,String second,double limit,double coeff) throws Exception{
		Set<String>set=new HashSet<String>();
		Db db=new Db(PATH,DBNAME);	
		
		//first name - second n
		for(int j=1;j<=12;j++){
			String sql="select q1.name,q2.name,q1.ra,q1.dec,q2.ra,q2.dec,q1.a,q1.id,q2.id from TOTAL as q1 join TOTAL as q2 on q1.name=q2.n"+j+" " +
					"where "+second+" and "+first+";";//and q1.dst<3 and q2.dst<3
			List<String[]>list=db.exec(sql,-1);
			
			for(String[] a:list){
				double ra1=Double.parseDouble(a[2]);
				double dec1=Double.parseDouble(a[3]);
				double ra2=Double.parseDouble(a[4]);
				double dec2=Double.parseDouble(a[5]);
				double dst=distance(ra1, dec1, ra2, dec2)*60;
				if(dst>Math.max(Double.parseDouble(a[6])*coeff, limit)){
					//String s="clearLink(\""+a[0]+"\",\""+a[1]+"\");//"+a[0]+" "+a[1]+" dst="+dst+" a="+a[6];
					String s="db.addCommand(\"update total set q2.n"+j+"=null where id="+a[8]+";\");//"+a[0]+" "+a[1]+" dst="+dst+" a="+a[6];;
					set.add(s);
					//p(s);
					
				}
			}
			p(""+j);
		}
		//second name - first n
		for(int j=1;j<=12;j++){
			String sql="select q1.name,q2.name,q1.ra,q1.dec,q2.ra,q2.dec,q1.a,q1.id,q2.id from TOTAL as q1 join TOTAL as q2 on q2.name=q1.n"+j+" " +
					"where "+second+" and "+first+";";//and q1.dst<3 and q2.dst<3
			List<String[]>list=db.exec(sql,-1);

			for(String[] a:list){
				double ra1=Double.parseDouble(a[2]);
				double dec1=Double.parseDouble(a[3]);
				double ra2=Double.parseDouble(a[4]);
				double dec2=Double.parseDouble(a[5]);
				double dst=distance(ra1, dec1, ra2, dec2)*60;
				if(dst>Math.max(Double.parseDouble(a[6])*coeff, limit)){
					//String s="clearLink(\""+a[0]+"\",\""+a[1]+"\");//"+a[0]+" "+a[1]+" dst="+dst+" a="+a[6];
					String s="db.addCommand(\"update total set q1.n"+j+"=null where id="+a[7]+";\");//"+a[0]+" "+a[1]+" dst="+dst+" a="+a[6];

					set.add(s);
					//p(s);

				}
			}
			p(""+j);

		}


	
		// n - n
		
		for(int i=1;i<=12;i++){
			for(int j=1;j<=12;j++){
				String sql="select q1.name,q2.name,q1.ra,q1.dec,q2.ra,q2.dec,q1.a,q1.id,q2.id from TOTAL as q1 join TOTAL as q2 on q1.n"+i+"=q2.n"+j+" " +
						"where "+second+" and "+first+";";//and q1.dst<3 and q2.dst<3
				List<String[]>list=db.exec(sql,-1);
				//p("size="+list.size());
				for(String[] a:list){
					double ra1=Double.parseDouble(a[2]);
					double dec1=Double.parseDouble(a[3]);
					double ra2=Double.parseDouble(a[4]);
					double dec2=Double.parseDouble(a[5]);
					double dst=distance(ra1, dec1, ra2, dec2)*60;
					if(dst>Math.max(Double.parseDouble(a[6])*coeff, limit)){
						//String s="clearLink(\""+a[0]+"\",\""+a[1]+"\");//"+a[0]+" "+a[1]+" dst="+dst+" a="+a[6];
						String s="db.addCommand(\"update total set q2.n"+j+"=null where id="+a[8]+";\");//"+a[0]+" "+a[1]+" dst="+dst+" a="+a[6];

						set.add(s);
						//p(s);
						
					}
				}
				p(i+" "+j);
			}
		}
		for(String s:set){
			p(s);
		}
	}
	/**
	 * mostly needed for NgcIc as  there are references to catalogs lke VV where there is 
	 * the same designation for different objects. 
	 * makes a FILE with instruction for removing n - n links if there is no name - n relation
	 * @param first (q1.name like 'ngc%' or q1.name like 'ic%')
	 * @param second (q2.name like 'ngc%' or q2.name like 'ic%')
	 * @throws Exception
	 * @return 
	 */
	public static void compareWithItself(String first,String second) throws Exception{
		
		class Pair{
			String name1;
			String name2;
			public Pair(String name1, String name2) {
				super();
				this.name1 = name1;
				this.name2 = name2;
				
			}
			
			@Override
			public String toString() {
				return "Pair [name1=" + name1 + ", name2=" + name2 + "]";
			}

			@Override
			public boolean equals(Object o){
				if(!(o instanceof Pair))
					return false;
				Pair p=(Pair)o;
				if((name1.equals(p.name1)&&name2.equals(p.name2))||(name1.equals(p.name2)&&name2.equals(p.name1))){
					return true;
				}
				else
					return false;
			}
			
			@Override
			public int hashCode(){
				int res=name1.compareTo(name2);
				if(res>0){
					return name1.hashCode()*17+name2.hashCode();
				}
				else 
					return name2.hashCode()*17+name1.hashCode();
			}
			
		}
		PrintStream out=new PrintStream(new FileOutputStream(new File(PATH,"compareitself.txt")));

		Set<Pair>set=new HashSet<Pair>();//existing ngcic - ngcic relations
		List<String>ll=new ArrayList<String>();
		Db db=new Db(PATH,DBNAME);	
		
		//first name - second n
		for(int j=1;j<12;j++){
			String sql="select q1.name,q2.name from TOTAL as q1 join TOTAL as q2 on q1.name=q2.n"+j+" " +
					"where "+second+" and "+first+" and q1.name!=q2.name;";//and q1.dst<3 and q2.dst<3
			List<String[]>list=db.exec(sql,-1);
			
			for(String[] a:list){
									
				set.add(new Pair(a[0],a[1]));
			}
			p(""+j);
		}
		
		out.println("existing pairs:");
		for(Pair pair:set){
			out.println(""+pair);
		}

	
		// n - n
		
		for(int i=1;i<=12;i++){
			for(int j=1;j<12;j++){
				String sql="select q1.name,q2.name,q1.ra,q1.dec,q2.ra,q2.dec,q1.n1,q1.n2,q1.n3,q1.n4,q1.n5,q1.n6,q1.n7,q1.n8,q1.n9,q1.n10,q1.n11,q1.n12 from TOTAL as q1 join TOTAL as q2 on q1.n"+i+"=q2.n"+j+" " +
						"where "+second+" and "+first+" and q1.name!=q2.name;";//and q1.dst<3 and q2.dst<3
				List<String[]>list=db.exec(sql,-1);
				
				for(String[] a:list){
					
					if(!(set.contains(new Pair(a[0],a[1]))||set.contains(new Pair(a[1],a[0])))){
						int col=i+5;
						//keep it if the same pgc mcg cgcg numbers
						double ra1=Double.parseDouble(a[2]);
						double dec1=Double.parseDouble(a[3]);
						double ra2=Double.parseDouble(a[4]);
						double dec2=Double.parseDouble(a[5]);
						double dst=distance(ra1, dec1, ra2, dec2)*60;
						if(a[col].contains("PGC")||a[col].contains("MCG")||a[col].contains("CGCG")){
							if(dst<3)continue;
						}

						//keep it if distance <1 min
						if(dst<1)continue;
						String s1="db.addCommand(\"update TOTAL set n"+i+"=null where name='"+a[0]+"';"+"\");";					
						String s2="db.addCommand(\"update TOTAL set n"+j+"=null where name='"+a[1]+"';"+"\");";
						ll.add(s1);
						ll.add(s2);
					}
				}
				p(i+" "+j);
			}
		}
		out.println("\n\nto remove");
		for(String s:ll){
			out.println(""+s);
		}
		out.close();
	}
	
	public static void wrongUGC() throws Exception{
		//String sql="select q1.name,q2.name,q1.ra,q1.dec,q2.ra,q2.dec from TOTAL as q1 join TOTAL as q2 on q1.name=q2.n1 " +
		//		"where q2.name like 'ugc%' and (q1.name like 'ngc%' or q1.name like 'ic%') and q2.dst<3;";//and q1.dst<3 and q2.dst<3
		
		String sql="select q1.name,q2.name,q1.ra,q1.dec,q2.ra,q2.dec from TOTAL as q1 join TOTAL as q2 on q1.n1=q2.name " +
				"where q2.name like 'ugc%' and (q1.name like 'ngc%' or q1.name like 'ic%');";//and q1.dst<3 and q2.dst<3

		
		Db db=new Db(PATH,DBNAME);	
		List<String[]>list=db.exec(sql,-1);
		int j=0;
		for(String[] a:list){
			double ra1=Double.parseDouble(a[2]);
			double dec1=Double.parseDouble(a[3]);
			double ra2=Double.parseDouble(a[4]);
			double dec2=Double.parseDouble(a[5]);
			double dst=distance(ra1, dec1, ra2, dec2)*60;
			if(dst>3){
				p(a[0]+" "+a[1]+" "+dst);
				j++;
			}
		}
		p("total="+j);
	}
	
	public static void wrongUGC2() throws Exception{
		//String sql="select q1.name,q2.name,q1.ra,q1.dec,q2.ra,q2.dec from TOTAL as q1 join TOTAL as q2 on q1.name=q2.n1 " +
		//		"where q2.name like 'ugc%' and (q1.name like 'ngc%' or q1.name like 'ic%') and q2.dst<3;";//and q1.dst<3 and q2.dst<3
		Set<String>set=new HashSet<String>();
		Db db=new Db(PATH,DBNAME);	
		for(int i=1;i<=12;i++){
			for(int j=1;j<12;j++){
				String sql="select q1.name,q2.name,q1.ra,q1.dec,q2.ra,q2.dec from TOTAL as q1 join TOTAL as q2 on q1.n"+i+"=q2.n"+j+" " +
						"where q2.name like 'ugc%' and (q1.name like 'ngc%' or q1.name like 'ic%');";//and q1.dst<3 and q2.dst<3
				List<String[]>list=db.exec(sql,-1);
				
				for(String[] a:list){
					double ra1=Double.parseDouble(a[2]);
					double dec1=Double.parseDouble(a[3]);
					double ra2=Double.parseDouble(a[4]);
					double dec2=Double.parseDouble(a[5]);
					double dst=distance(ra1, dec1, ra2, dec2)*60;
					if(dst>3){
						set.add(a[0]+" "+a[1]+" "+dst);
						
					}
				}
				p(i+" "+j);
			}
		}
		for(String s:set){
			p(s);
		}
		
		
		
		
		
		
	}
	
	public static void compareNames() throws Exception{
		String sql="select q1.name,q1.n1,q1.n2,q1.n3,q1.n4,q1.n5,q1.n6,q2.n1,q2.n2,q2.n3,q2.n4,q2.n5,q2.n6,q1.dst" +
				" from TOTAL as q1 join NAMES as q2 on q1.name=q2.name;";
		Db db=new Db(PATH,DBNAME);	
		List<String[]>list=db.exec(sql,-1);
		for(String[] a:list){
			Set<String>set=new HashSet<String>();
			for(int i=1;i<=6;i++){
				if(!"".equals(a[i]))
					set.add(a[i].toUpperCase());
			}
			boolean contains=false;
			for(int i=7;i<=12;i++){
				if(set.contains(a[i]))
					contains=true;
			}
			if(!contains)p(a[0]+" "+a[13]);
		}
	}
	
	public static void p(String s){
		System.out.println(s);
	}
	public static void createTOTALTable()throws Exception{
		Db db=new Db(PATH,DBNAME);		
		String sql="create table TOTAL (id integer primary key autoincrement, dbname text," +
				"_id integer,name text,n1 text,n2 text,n3 text,n4 text,n5 text,n6 text,n7 text,n8 text,n9 text,n10 text,n11 text,n12 text,ra real," +
				"dec real,ras real,decs real,a real,dst,ref real);";
		db.exec(sql,-1);
		/*db.start();
		db.addCommand("attach database '"+PATH+ "ngcicNEW.db' as ngc;");
		db.addCommand("insert into TOTAL(_id,dbname,name,ra,dec) select _id,'ngcic.db',upper(name1),ra,dec from ngcic;");
		
		db.end(-1);*/
		
	}
	
	
	/**
	 * 
	 * @param ra1
	 * @param dec1
	 * @param ra2
	 * @param dec2
	 * @return distance in degrees
	 */
	public static double distance(double ra1,double dec1,double ra2,double dec2){
		double cosd=sin(dec1*PI/180) * sin(dec2*PI/180) + cos(dec1*PI/180) * cos(dec2*PI/180) * cos((ra1-ra2)*PI/12);
		if(cosd>1)cosd=1;
		if(cosd<-1)cosd=-1;
		return Math.acos(cosd)*180/PI;
	}
	/*public static void addSimbadCoords()throws Exception{
		Db db=new Db(PATH,DBNAME);
		db.start();
		db.addCommand("begin;");
		Map<String,AstroTools.RaDecRec> map=Parsers.loadSimbadRaDec(new File(PATH,"simbad_ngcic_radec_trunc.txt"));
		for(Map.Entry<String, AstroTools.RaDecRec> e:map.entrySet()){
			AstroTools.RaDecRec rec=e.getValue();
			String sql="update TOTAL set ras="+rec.ra+",decs="+rec.dec+" where name='"+e.getKey()+"' and dbname="+"'ngcic.db';";
			db.addCommand(sql);
		}
		db.addCommand("commit;");
		db.end(-1);
	}*/
	
	public static void clearNGCNGCLinks() throws Exception{
		Db db=new Db(PATH,DBNAME);
		db.start();
		db.addCommand("begin;");
		//compareWithItself("(q1.name like 'ngc%' or q1.name like 'ic%')","(q2.name like 'ngc%' or q2.name like 'ic%')");

		db.addCommand("update TOTAL set n1=null where name='NGC1435';");
		db.addCommand("update TOTAL set n1=null where name='IC349';");
		db.addCommand("update TOTAL set n1=null where name='NGC1874';");
		db.addCommand("update TOTAL set n1=null where name='NGC1876';");
		db.addCommand("update TOTAL set n1=null where name='NGC1876';");
		db.addCommand("update TOTAL set n1=null where name='NGC1874';");
		db.addCommand("update TOTAL set n1=null where name='NGC2023';");
		db.addCommand("update TOTAL set n1=null where name='IC434';");
		db.addCommand("update TOTAL set n1=null where name='NGC2237';");
		db.addCommand("update TOTAL set n1=null where name='NGC2246';");
		db.addCommand("update TOTAL set n1=null where name='NGC2246';");
		db.addCommand("update TOTAL set n1=null where name='NGC2237';");
		db.addCommand("update TOTAL set n1=null where name='NGC2442';");
		db.addCommand("update TOTAL set n1=null where name='NGC2443';");
		db.addCommand("update TOTAL set n1=null where name='NGC2443';");
		db.addCommand("update TOTAL set n1=null where name='NGC2442';");
		db.addCommand("update TOTAL set n1=null where name='NGC5317';");
		db.addCommand("update TOTAL set n1=null where name='NGC5364';");
		db.addCommand("update TOTAL set n1=null where name='NGC5364';");
		db.addCommand("update TOTAL set n1=null where name='NGC5317';");
		db.addCommand("update TOTAL set n1=null where name='NGC5994';");
		db.addCommand("update TOTAL set n1=null where name='NGC5996';");
		db.addCommand("update TOTAL set n1=null where name='NGC5996';");
		db.addCommand("update TOTAL set n1=null where name='NGC5994';");
		db.addCommand("update TOTAL set n1=null where name='NGC6522';");
		db.addCommand("update TOTAL set n1=null where name='NGC6551';");
		db.addCommand("update TOTAL set n1=null where name='NGC6551';");
		db.addCommand("update TOTAL set n1=null where name='NGC6522';");
		db.addCommand("update TOTAL set n1=null where name='NGC6974';");
		db.addCommand("update TOTAL set n1=null where name='NGC6979';");
		db.addCommand("update TOTAL set n1=null where name='NGC6974';");
		db.addCommand("update TOTAL set n1=null where name='NGC6995';");
		db.addCommand("update TOTAL set n1=null where name='NGC6979';");
		db.addCommand("update TOTAL set n1=null where name='NGC6974';");
		db.addCommand("update TOTAL set n1=null where name='NGC6979';");
		db.addCommand("update TOTAL set n1=null where name='NGC6995';");
		db.addCommand("update TOTAL set n1=null where name='NGC6995';");
		db.addCommand("update TOTAL set n1=null where name='NGC6974';");
		db.addCommand("update TOTAL set n1=null where name='NGC6995';");
		db.addCommand("update TOTAL set n1=null where name='NGC6979';");
		db.addCommand("update TOTAL set n1=null where name='NGC7140';");
		db.addCommand("update TOTAL set n1=null where name='NGC7141';");
		db.addCommand("update TOTAL set n1=null where name='NGC7141';");
		db.addCommand("update TOTAL set n1=null where name='NGC7140';");
		db.addCommand("update TOTAL set n1=null where name='NGC7322';");
		db.addCommand("update TOTAL set n1=null where name='NGC7334';");
		db.addCommand("update TOTAL set n1=null where name='NGC7334';");
		db.addCommand("update TOTAL set n1=null where name='NGC7322';");
		db.addCommand("update TOTAL set n1=null where name='IC349';");
		db.addCommand("update TOTAL set n1=null where name='NGC1435';");
		db.addCommand("update TOTAL set n1=null where name='IC434';");
		db.addCommand("update TOTAL set n1=null where name='NGC2023';");
		db.addCommand("update TOTAL set n1=null where name='IC4954';");
		db.addCommand("update TOTAL set n1=null where name='IC4955';");
		db.addCommand("update TOTAL set n1=null where name='IC4955';");
		db.addCommand("update TOTAL set n1=null where name='IC4954';");
		db.addCommand("update TOTAL set n1=null where name='NGC67';");
		db.addCommand("update TOTAL set n2=null where name='NGC70';");
		db.addCommand("update TOTAL set n1=null where name='NGC1440';");
		db.addCommand("update TOTAL set n2=null where name='NGC1439';");
		db.addCommand("update TOTAL set n1=null where name='NGC1442';");
		db.addCommand("update TOTAL set n2=null where name='NGC1439';");
		db.addCommand("update TOTAL set n1=null where name='NGC1458';");
		db.addCommand("update TOTAL set n2=null where name='NGC1439';");
		db.addCommand("update TOTAL set n1=null where name='NGC2572';");
		db.addCommand("update TOTAL set n2=null where name='IC2308';");
		db.addCommand("update TOTAL set n1=null where name='NGC3754';");
		db.addCommand("update TOTAL set n2=null where name='NGC3746';");
		db.addCommand("update TOTAL set n1=null where name='NGC3754';");
		db.addCommand("update TOTAL set n2=null where name='NGC3751';");
		db.addCommand("update TOTAL set n1=null where name='NGC6120';");
		db.addCommand("update TOTAL set n2=null where name='NGC6122';");
		db.addCommand("update TOTAL set n1=null where name='NGC6967';");
		db.addCommand("update TOTAL set n2=null where name='NGC6965';");
		db.addCommand("update TOTAL set n1=null where name='NGC6974';");
		db.addCommand("update TOTAL set n2=null where name='NGC6960';");
		db.addCommand("update TOTAL set n1=null where name='NGC6979';");
		db.addCommand("update TOTAL set n2=null where name='NGC6960';");
		db.addCommand("update TOTAL set n1=null where name='NGC6995';");
		db.addCommand("update TOTAL set n2=null where name='NGC6960';");
		db.addCommand("update TOTAL set n1=null where name='NGC2602';");
		db.addCommand("update TOTAL set n3=null where name='NGC2605';");
		db.addCommand("update TOTAL set n1=null where name='NGC3754';");
		db.addCommand("update TOTAL set n3=null where name='NGC3745';");
		db.addCommand("update TOTAL set n1=null where name='NGC3754';");
		db.addCommand("update TOTAL set n3=null where name='NGC3748';");
		db.addCommand("update TOTAL set n1=null where name='NGC3754';");
		db.addCommand("update TOTAL set n3=null where name='NGC3750';");
		db.addCommand("update TOTAL set n1=null where name='NGC7452';");
		db.addCommand("update TOTAL set n3=null where name='NGC7558';");
		db.addCommand("update TOTAL set n1=null where name='NGC2239';");
		db.addCommand("update TOTAL set n4=null where name='NGC2243';");
		db.addCommand("update TOTAL set n1=null where name='NGC2244';");
		db.addCommand("update TOTAL set n4=null where name='NGC2243';");
		db.addCommand("update TOTAL set n1=null where name='IC3174';");
		db.addCommand("update TOTAL set n4=null where name='IC3128';");
		db.addCommand("update TOTAL set n1=null where name='IC3174';");
		db.addCommand("update TOTAL set n4=null where name='IC3128';");
		db.addCommand("update TOTAL set n1=null where name='IC5132';");
		db.addCommand("update TOTAL set n4=null where name='NGC7129';");
		db.addCommand("update TOTAL set n1=null where name='NGC67';");
		db.addCommand("update TOTAL set n5=null where name='NGC68';");
		db.addCommand("update TOTAL set n1=null where name='NGC67';");
		db.addCommand("update TOTAL set n5=null where name='NGC69';");
		db.addCommand("update TOTAL set n1=null where name='NGC67';");
		db.addCommand("update TOTAL set n5=null where name='NGC70';");
		db.addCommand("update TOTAL set n1=null where name='NGC67';");
		db.addCommand("update TOTAL set n5=null where name='NGC71';");
		db.addCommand("update TOTAL set n1=null where name='NGC67';");
		db.addCommand("update TOTAL set n5=null where name='NGC72';");
		db.addCommand("update TOTAL set n1=null where name='NGC2174';");
		db.addCommand("update TOTAL set n6=null where name='NGC2175';");
		db.addCommand("update TOTAL set n1=null where name='IC4703';");
		db.addCommand("update TOTAL set n6=null where name='NGC6611';");
		db.addCommand("update TOTAL set n2=null where name='NGC70';");
		db.addCommand("update TOTAL set n1=null where name='NGC67';");
		db.addCommand("update TOTAL set n2=null where name='NGC1439';");
		db.addCommand("update TOTAL set n1=null where name='NGC1440';");
		db.addCommand("update TOTAL set n2=null where name='NGC1439';");
		db.addCommand("update TOTAL set n1=null where name='NGC1442';");
		db.addCommand("update TOTAL set n2=null where name='NGC1439';");
		db.addCommand("update TOTAL set n1=null where name='NGC1458';");
		db.addCommand("update TOTAL set n2=null where name='NGC3746';");
		db.addCommand("update TOTAL set n1=null where name='NGC3754';");
		db.addCommand("update TOTAL set n2=null where name='NGC3751';");
		db.addCommand("update TOTAL set n1=null where name='NGC3754';");
		db.addCommand("update TOTAL set n2=null where name='NGC6122';");
		db.addCommand("update TOTAL set n1=null where name='NGC6120';");
		db.addCommand("update TOTAL set n2=null where name='NGC6960';");
		db.addCommand("update TOTAL set n1=null where name='NGC6974';");
		db.addCommand("update TOTAL set n2=null where name='NGC6960';");
		db.addCommand("update TOTAL set n1=null where name='NGC6979';");
		db.addCommand("update TOTAL set n2=null where name='NGC6960';");
		db.addCommand("update TOTAL set n1=null where name='NGC6995';");
		db.addCommand("update TOTAL set n2=null where name='NGC6965';");
		db.addCommand("update TOTAL set n1=null where name='NGC6967';");
		db.addCommand("update TOTAL set n2=null where name='IC2308';");
		db.addCommand("update TOTAL set n1=null where name='NGC2572';");
		db.addCommand("update TOTAL set n2=null where name='NGC28';");
		db.addCommand("update TOTAL set n2=null where name='NGC31';");
		db.addCommand("update TOTAL set n2=null where name='NGC31';");
		db.addCommand("update TOTAL set n2=null where name='NGC28';");
		db.addCommand("update TOTAL set n2=null where name='NGC422';");
		db.addCommand("update TOTAL set n2=null where name='NGC456';");
		db.addCommand("update TOTAL set n2=null where name='NGC456';");
		db.addCommand("update TOTAL set n2=null where name='NGC422';");
		db.addCommand("update TOTAL set n2=null where name='NGC833';");
		db.addCommand("update TOTAL set n2=null where name='NGC835';");
		db.addCommand("update TOTAL set n2=null where name='NGC833';");
		db.addCommand("update TOTAL set n2=null where name='NGC839';");
		db.addCommand("update TOTAL set n2=null where name='NGC835';");
		db.addCommand("update TOTAL set n2=null where name='NGC833';");
		db.addCommand("update TOTAL set n2=null where name='NGC835';");
		db.addCommand("update TOTAL set n2=null where name='NGC839';");
		db.addCommand("update TOTAL set n2=null where name='NGC839';");
		db.addCommand("update TOTAL set n2=null where name='NGC833';");
		db.addCommand("update TOTAL set n2=null where name='NGC839';");
		db.addCommand("update TOTAL set n2=null where name='NGC835';");
		db.addCommand("update TOTAL set n2=null where name='NGC1241';");
		db.addCommand("update TOTAL set n2=null where name='NGC1242';");
		db.addCommand("update TOTAL set n2=null where name='NGC1242';");
		db.addCommand("update TOTAL set n2=null where name='NGC1241';");
		db.addCommand("update TOTAL set n2=null where name='NGC1304';");
		db.addCommand("update TOTAL set n2=null where name='NGC1307';");
		db.addCommand("update TOTAL set n2=null where name='NGC1307';");
		db.addCommand("update TOTAL set n2=null where name='NGC1304';");
		db.addCommand("update TOTAL set n2=null where name='NGC1367';");
		db.addCommand("update TOTAL set n2=null where name='NGC1371';");
		db.addCommand("update TOTAL set n2=null where name='NGC1371';");
		db.addCommand("update TOTAL set n2=null where name='NGC1367';");
		db.addCommand("update TOTAL set n2=null where name='NGC1596';");
		db.addCommand("update TOTAL set n2=null where name='NGC1602';");
		db.addCommand("update TOTAL set n2=null where name='NGC1602';");
		db.addCommand("update TOTAL set n2=null where name='NGC1596';");
		db.addCommand("update TOTAL set n2=null where name='NGC1721';");
		db.addCommand("update TOTAL set n2=null where name='NGC1725';");
		db.addCommand("update TOTAL set n2=null where name='NGC1721';");
		db.addCommand("update TOTAL set n2=null where name='NGC1728';");
		db.addCommand("update TOTAL set n2=null where name='NGC1725';");
		db.addCommand("update TOTAL set n2=null where name='NGC1721';");
		db.addCommand("update TOTAL set n2=null where name='NGC1725';");
		db.addCommand("update TOTAL set n2=null where name='NGC1728';");
		db.addCommand("update TOTAL set n2=null where name='NGC1728';");
		db.addCommand("update TOTAL set n2=null where name='NGC1721';");
		db.addCommand("update TOTAL set n2=null where name='NGC1728';");
		db.addCommand("update TOTAL set n2=null where name='NGC1725';");
		db.addCommand("update TOTAL set n2=null where name='NGC1973';");
		db.addCommand("update TOTAL set n2=null where name='NGC1975';");
		db.addCommand("update TOTAL set n2=null where name='NGC1975';");
		db.addCommand("update TOTAL set n2=null where name='NGC1973';");
		db.addCommand("update TOTAL set n2=null where name='NGC1977';");
		db.addCommand("update TOTAL set n2=null where name='NGC1981';");
		db.addCommand("update TOTAL set n2=null where name='NGC1981';");
		db.addCommand("update TOTAL set n2=null where name='NGC1977';");
		db.addCommand("update TOTAL set n2=null where name='NGC2007';");
		db.addCommand("update TOTAL set n2=null where name='NGC2008';");
		db.addCommand("update TOTAL set n2=null where name='NGC2008';");
		db.addCommand("update TOTAL set n2=null where name='NGC2007';");
		db.addCommand("update TOTAL set n2=null where name='NGC2221';");
		db.addCommand("update TOTAL set n2=null where name='NGC2222';");
		db.addCommand("update TOTAL set n2=null where name='NGC2222';");
		db.addCommand("update TOTAL set n2=null where name='NGC2221';");
		db.addCommand("update TOTAL set n2=null where name='NGC2233';");
		db.addCommand("update TOTAL set n2=null where name='NGC2235';");
		db.addCommand("update TOTAL set n2=null where name='NGC2235';");
		db.addCommand("update TOTAL set n2=null where name='NGC2233';");
		db.addCommand("update TOTAL set n2=null where name='NGC2305';");
		db.addCommand("update TOTAL set n2=null where name='NGC2307';");
		db.addCommand("update TOTAL set n2=null where name='NGC2307';");
		db.addCommand("update TOTAL set n2=null where name='NGC2305';");
		db.addCommand("update TOTAL set n2=null where name='NGC2442';");
		db.addCommand("update TOTAL set n2=null where name='NGC2443';");
		db.addCommand("update TOTAL set n2=null where name='NGC2443';");
		db.addCommand("update TOTAL set n2=null where name='NGC2442';");
		db.addCommand("update TOTAL set n2=null where name='NGC2686';");
		db.addCommand("update TOTAL set n2=null where name='NGC2687';");
		db.addCommand("update TOTAL set n2=null where name='NGC2687';");
		db.addCommand("update TOTAL set n2=null where name='NGC2686';");
		db.addCommand("update TOTAL set n2=null where name='NGC2992';");
		db.addCommand("update TOTAL set n2=null where name='NGC2993';");
		db.addCommand("update TOTAL set n2=null where name='NGC2993';");
		db.addCommand("update TOTAL set n2=null where name='NGC2992';");
		db.addCommand("update TOTAL set n2=null where name='NGC3746';");
		db.addCommand("update TOTAL set n2=null where name='NGC3751';");
		db.addCommand("update TOTAL set n2=null where name='NGC3746';");
		db.addCommand("update TOTAL set n2=null where name='NGC3753';");
		db.addCommand("update TOTAL set n2=null where name='NGC3751';");
		db.addCommand("update TOTAL set n2=null where name='NGC3746';");
		db.addCommand("update TOTAL set n2=null where name='NGC3751';");
		db.addCommand("update TOTAL set n2=null where name='NGC3753';");
		db.addCommand("update TOTAL set n2=null where name='NGC3753';");
		db.addCommand("update TOTAL set n2=null where name='NGC3746';");
		db.addCommand("update TOTAL set n2=null where name='NGC3753';");
		db.addCommand("update TOTAL set n2=null where name='NGC3751';");
		db.addCommand("update TOTAL set n2=null where name='NGC5216';");
		db.addCommand("update TOTAL set n2=null where name='NGC5218';");
		db.addCommand("update TOTAL set n2=null where name='NGC5218';");
		db.addCommand("update TOTAL set n2=null where name='NGC5216';");
		db.addCommand("update TOTAL set n2=null where name='NGC5595';");
		db.addCommand("update TOTAL set n2=null where name='NGC5597';");
		db.addCommand("update TOTAL set n2=null where name='NGC5597';");
		db.addCommand("update TOTAL set n2=null where name='NGC5595';");
		db.addCommand("update TOTAL set n2=null where name='NGC5829';");
		db.addCommand("update TOTAL set n2=null where name='IC4526';");
		db.addCommand("update TOTAL set n2=null where name='NGC6522';");
		db.addCommand("update TOTAL set n2=null where name='NGC6551';");
		db.addCommand("update TOTAL set n2=null where name='NGC6551';");
		db.addCommand("update TOTAL set n2=null where name='NGC6522';");
		db.addCommand("update TOTAL set n2=null where name='NGC6734';");
		db.addCommand("update TOTAL set n2=null where name='NGC6736';");
		db.addCommand("update TOTAL set n2=null where name='NGC6736';");
		db.addCommand("update TOTAL set n2=null where name='NGC6734';");
		db.addCommand("update TOTAL set n2=null where name='NGC6769';");
		db.addCommand("update TOTAL set n2=null where name='NGC6770';");
		db.addCommand("update TOTAL set n2=null where name='NGC6770';");
		db.addCommand("update TOTAL set n2=null where name='NGC6769';");
		db.addCommand("update TOTAL set n2=null where name='NGC6861';");
		db.addCommand("update TOTAL set n2=null where name='NGC6868';");
		db.addCommand("update TOTAL set n2=null where name='NGC6861';");
		db.addCommand("update TOTAL set n2=null where name='NGC6870';");
		db.addCommand("update TOTAL set n2=null where name='NGC6868';");
		db.addCommand("update TOTAL set n2=null where name='NGC6861';");
		db.addCommand("update TOTAL set n2=null where name='NGC6868';");
		db.addCommand("update TOTAL set n2=null where name='NGC6870';");
		db.addCommand("update TOTAL set n2=null where name='NGC6870';");
		db.addCommand("update TOTAL set n2=null where name='NGC6861';");
		db.addCommand("update TOTAL set n2=null where name='NGC6870';");
		db.addCommand("update TOTAL set n2=null where name='NGC6868';");
		db.addCommand("update TOTAL set n2=null where name='NGC6876';");
		db.addCommand("update TOTAL set n2=null where name='NGC6877';");
		db.addCommand("update TOTAL set n2=null where name='NGC6877';");
		db.addCommand("update TOTAL set n2=null where name='NGC6876';");
		db.addCommand("update TOTAL set n2=null where name='NGC7117';");
		db.addCommand("update TOTAL set n2=null where name='NGC7118';");
		db.addCommand("update TOTAL set n2=null where name='NGC7118';");
		db.addCommand("update TOTAL set n2=null where name='NGC7117';");
		db.addCommand("update TOTAL set n2=null where name='NGC7125';");
		db.addCommand("update TOTAL set n2=null where name='NGC7126';");
		db.addCommand("update TOTAL set n2=null where name='NGC7126';");
		db.addCommand("update TOTAL set n2=null where name='NGC7125';");
		db.addCommand("update TOTAL set n2=null where name='NGC7140';");
		db.addCommand("update TOTAL set n2=null where name='NGC7141';");
		db.addCommand("update TOTAL set n2=null where name='NGC7141';");
		db.addCommand("update TOTAL set n2=null where name='NGC7140';");
		db.addCommand("update TOTAL set n2=null where name='NGC7232';");
		db.addCommand("update TOTAL set n2=null where name='NGC7233';");
		db.addCommand("update TOTAL set n2=null where name='NGC7233';");
		db.addCommand("update TOTAL set n2=null where name='NGC7232';");
		db.addCommand("update TOTAL set n2=null where name='NGC7650';");
		db.addCommand("update TOTAL set n2=null where name='NGC7652';");
		db.addCommand("update TOTAL set n2=null where name='NGC7652';");
		db.addCommand("update TOTAL set n2=null where name='NGC7650';");
		db.addCommand("update TOTAL set n2=null where name='IC180';");
		db.addCommand("update TOTAL set n2=null where name='IC181';");
		db.addCommand("update TOTAL set n2=null where name='IC181';");
		db.addCommand("update TOTAL set n2=null where name='IC180';");
		db.addCommand("update TOTAL set n2=null where name='IC1835';");
		db.addCommand("update TOTAL set n2=null where name='IC1847';");
		db.addCommand("update TOTAL set n2=null where name='IC1847';");
		db.addCommand("update TOTAL set n2=null where name='IC1835';");
		db.addCommand("update TOTAL set n2=null where name='IC3134';");
		db.addCommand("update TOTAL set n2=null where name='IC3209';");
		db.addCommand("update TOTAL set n2=null where name='IC3209';");
		db.addCommand("update TOTAL set n2=null where name='IC3134';");
		db.addCommand("update TOTAL set n2=null where name='IC4526';");
		db.addCommand("update TOTAL set n2=null where name='NGC5829';");
		db.addCommand("update TOTAL set n2=null where name='IC4954';");
		db.addCommand("update TOTAL set n2=null where name='IC4955';");
		db.addCommand("update TOTAL set n2=null where name='IC4955';");
		db.addCommand("update TOTAL set n2=null where name='IC4954';");
		db.addCommand("update TOTAL set n2=null where name='NGC28';");
		db.addCommand("update TOTAL set n3=null where name='NGC37';");
		db.addCommand("update TOTAL set n2=null where name='NGC31';");
		db.addCommand("update TOTAL set n3=null where name='NGC37';");
		db.addCommand("update TOTAL set n2=null where name='NGC215';");
		db.addCommand("update TOTAL set n3=null where name='NGC212';");
		db.addCommand("update TOTAL set n2=null where name='NGC375';");
		db.addCommand("update TOTAL set n3=null where name='NGC383';");
		db.addCommand("update TOTAL set n2=null where name='NGC599';");
		db.addCommand("update TOTAL set n3=null where name='NGC601';");
		db.addCommand("update TOTAL set n2=null where name='NGC833';");
		db.addCommand("update TOTAL set n3=null where name='NGC838';");
		db.addCommand("update TOTAL set n2=null where name='NGC833';");
		db.addCommand("update TOTAL set n3=null where name='NGC848';");
		db.addCommand("update TOTAL set n2=null where name='NGC835';");
		db.addCommand("update TOTAL set n3=null where name='NGC838';");
		db.addCommand("update TOTAL set n2=null where name='NGC835';");
		db.addCommand("update TOTAL set n3=null where name='NGC848';");
		db.addCommand("update TOTAL set n2=null where name='NGC839';");
		db.addCommand("update TOTAL set n3=null where name='NGC838';");
		db.addCommand("update TOTAL set n2=null where name='NGC839';");
		db.addCommand("update TOTAL set n3=null where name='NGC848';");
		db.addCommand("update TOTAL set n2=null where name='NGC2233';");
		db.addCommand("update TOTAL set n3=null where name='NGC2229';");
		db.addCommand("update TOTAL set n2=null where name='NGC2233';");
		db.addCommand("update TOTAL set n3=null where name='NGC2230';");
		db.addCommand("update TOTAL set n2=null where name='NGC2235';");
		db.addCommand("update TOTAL set n3=null where name='NGC2229';");
		db.addCommand("update TOTAL set n2=null where name='NGC2235';");
		db.addCommand("update TOTAL set n3=null where name='NGC2230';");
		db.addCommand("update TOTAL set n2=null where name='NGC3746';");
		db.addCommand("update TOTAL set n3=null where name='NGC3748';");
		db.addCommand("update TOTAL set n2=null where name='NGC3746';");
		db.addCommand("update TOTAL set n3=null where name='NGC3750';");
		db.addCommand("update TOTAL set n2=null where name='NGC3751';");
		db.addCommand("update TOTAL set n3=null where name='NGC3745';");
		db.addCommand("update TOTAL set n2=null where name='NGC3751';");
		db.addCommand("update TOTAL set n3=null where name='NGC3748';");
		db.addCommand("update TOTAL set n2=null where name='NGC3751';");
		db.addCommand("update TOTAL set n3=null where name='NGC3750';");
		db.addCommand("update TOTAL set n2=null where name='NGC3753';");
		db.addCommand("update TOTAL set n3=null where name='NGC3745';");
		db.addCommand("update TOTAL set n2=null where name='NGC3753';");
		db.addCommand("update TOTAL set n3=null where name='NGC3748';");
		db.addCommand("update TOTAL set n2=null where name='NGC4039';");
		db.addCommand("update TOTAL set n3=null where name='NGC4038';");
		db.addCommand("update TOTAL set n2=null where name='NGC6285';");
		db.addCommand("update TOTAL set n3=null where name='NGC6286';");
		db.addCommand("update TOTAL set n2=null where name='NGC6872';");
		db.addCommand("update TOTAL set n3=null where name='IC4970';");
		db.addCommand("update TOTAL set n2=null where name='NGC6880';");
		db.addCommand("update TOTAL set n3=null where name='IC4981';");
		db.addCommand("update TOTAL set n2=null where name='NGC7170';");
		db.addCommand("update TOTAL set n3=null where name='NGC7140';");
		db.addCommand("update TOTAL set n2=null where name='NGC7170';");
		db.addCommand("update TOTAL set n3=null where name='NGC7141';");
		db.addCommand("update TOTAL set n2=null where name='NGC7317';");
		db.addCommand("update TOTAL set n3=null where name='NGC7319';");
		db.addCommand("update TOTAL set n2=null where name='NGC7655';");
		db.addCommand("update TOTAL set n3=null where name='IC5320';");
		db.addCommand("update TOTAL set n2=null where name='NGC7655';");
		db.addCommand("update TOTAL set n3=null where name='IC5322';");
		db.addCommand("update TOTAL set n2=null where name='NGC7655';");
		db.addCommand("update TOTAL set n3=null where name='IC5323';");
		db.addCommand("update TOTAL set n2=null where name='NGC7655';");
		db.addCommand("update TOTAL set n3=null where name='IC5324';");
		db.addCommand("update TOTAL set n2=null where name='IC1847';");
		db.addCommand("update TOTAL set n3=null where name='IC1835';");
		db.addCommand("update TOTAL set n2=null where name='IC2337';");
		db.addCommand("update TOTAL set n3=null where name='IC2290';");
		db.addCommand("update TOTAL set n2=null where name='IC2414';");
		db.addCommand("update TOTAL set n3=null where name='IC2392';");
		db.addCommand("update TOTAL set n2=null where name='NGC28';");
		db.addCommand("update TOTAL set n4=null where name='NGC25';");
		db.addCommand("update TOTAL set n2=null where name='NGC31';");
		db.addCommand("update TOTAL set n4=null where name='NGC25';");
		db.addCommand("update TOTAL set n2=null where name='NGC375';");
		db.addCommand("update TOTAL set n4=null where name='NGC385';");
		db.addCommand("update TOTAL set n2=null where name='NGC375';");
		db.addCommand("update TOTAL set n4=null where name='NGC386';");
		db.addCommand("update TOTAL set n2=null where name='NGC2174';");
		db.addCommand("update TOTAL set n4=null where name='NGC2175';");
		db.addCommand("update TOTAL set n2=null where name='NGC2924';");
		db.addCommand("update TOTAL set n4=null where name='NGC2933';");
		db.addCommand("update TOTAL set n2=null where name='NGC3410';");
		db.addCommand("update TOTAL set n4=null where name='NGC341';");
		db.addCommand("update TOTAL set n2=null where name='NGC6868';");
		db.addCommand("update TOTAL set n4=null where name='IC4949';");
		db.addCommand("update TOTAL set n2=null where name='NGC6870';");
		db.addCommand("update TOTAL set n4=null where name='IC4949';");
		db.addCommand("update TOTAL set n2=null where name='IC3255';");
		db.addCommand("update TOTAL set n4=null where name='IC775';");
		db.addCommand("update TOTAL set n2=null where name='IC4434';");
		db.addCommand("update TOTAL set n4=null where name='IC999';");
		db.addCommand("update TOTAL set n2=null where name='NGC70';");
		db.addCommand("update TOTAL set n5=null where name='NGC69';");
		db.addCommand("update TOTAL set n2=null where name='NGC70';");
		db.addCommand("update TOTAL set n5=null where name='NGC71';");
		db.addCommand("update TOTAL set n2=null where name='NGC70';");
		db.addCommand("update TOTAL set n5=null where name='NGC72';");
		db.addCommand("update TOTAL set n2=null where name='NGC375';");
		db.addCommand("update TOTAL set n5=null where name='NGC379';");
		db.addCommand("update TOTAL set n2=null where name='NGC375';");
		db.addCommand("update TOTAL set n5=null where name='NGC380';");
		db.addCommand("update TOTAL set n2=null where name='NGC375';");
		db.addCommand("update TOTAL set n5=null where name='NGC382';");
		db.addCommand("update TOTAL set n2=null where name='NGC375';");
		db.addCommand("update TOTAL set n5=null where name='NGC384';");
		db.addCommand("update TOTAL set n2=null where name='NGC5615';");
		db.addCommand("update TOTAL set n5=null where name='NGC5613';");
		db.addCommand("update TOTAL set n2=null where name='NGC67';");
		db.addCommand("update TOTAL set n6=null where name='NGC68';");
		db.addCommand("update TOTAL set n2=null where name='NGC67';");
		db.addCommand("update TOTAL set n6=null where name='NGC69';");
		db.addCommand("update TOTAL set n2=null where name='NGC67';");
		db.addCommand("update TOTAL set n6=null where name='NGC70';");
		db.addCommand("update TOTAL set n2=null where name='NGC67';");
		db.addCommand("update TOTAL set n6=null where name='NGC71';");
		db.addCommand("update TOTAL set n2=null where name='NGC67';");
		db.addCommand("update TOTAL set n6=null where name='NGC72';");
		db.addCommand("update TOTAL set n2=null where name='NGC375';");
		db.addCommand("update TOTAL set n6=null where name='NGC388';");
		db.addCommand("update TOTAL set n2=null where name='NGC4618';");
		db.addCommand("update TOTAL set n6=null where name='NGC4625';");
		db.addCommand("update TOTAL set n2=null where name='NGC7317';");
		db.addCommand("update TOTAL set n6=null where name='NGC7320';");
		db.addCommand("update TOTAL set n2=null where name='NGC7317';");
		db.addCommand("update TOTAL set n7=null where name='NGC7318';");
		db.addCommand("update TOTAL set n3=null where name='NGC2605';");
		db.addCommand("update TOTAL set n1=null where name='NGC2602';");
		db.addCommand("update TOTAL set n3=null where name='NGC3745';");
		db.addCommand("update TOTAL set n1=null where name='NGC3754';");
		db.addCommand("update TOTAL set n3=null where name='NGC3748';");
		db.addCommand("update TOTAL set n1=null where name='NGC3754';");
		db.addCommand("update TOTAL set n3=null where name='NGC3750';");
		db.addCommand("update TOTAL set n1=null where name='NGC3754';");
		db.addCommand("update TOTAL set n3=null where name='NGC7558';");
		db.addCommand("update TOTAL set n1=null where name='NGC7452';");
		db.addCommand("update TOTAL set n3=null where name='NGC37';");
		db.addCommand("update TOTAL set n2=null where name='NGC28';");
		db.addCommand("update TOTAL set n3=null where name='NGC37';");
		db.addCommand("update TOTAL set n2=null where name='NGC31';");
		db.addCommand("update TOTAL set n3=null where name='NGC212';");
		db.addCommand("update TOTAL set n2=null where name='NGC215';");
		db.addCommand("update TOTAL set n3=null where name='NGC383';");
		db.addCommand("update TOTAL set n2=null where name='NGC375';");
		db.addCommand("update TOTAL set n3=null where name='NGC601';");
		db.addCommand("update TOTAL set n2=null where name='NGC599';");
		db.addCommand("update TOTAL set n3=null where name='NGC838';");
		db.addCommand("update TOTAL set n2=null where name='NGC833';");
		db.addCommand("update TOTAL set n3=null where name='NGC838';");
		db.addCommand("update TOTAL set n2=null where name='NGC835';");
		db.addCommand("update TOTAL set n3=null where name='NGC838';");
		db.addCommand("update TOTAL set n2=null where name='NGC839';");
		db.addCommand("update TOTAL set n3=null where name='NGC848';");
		db.addCommand("update TOTAL set n2=null where name='NGC833';");
		db.addCommand("update TOTAL set n3=null where name='NGC848';");
		db.addCommand("update TOTAL set n2=null where name='NGC835';");
		db.addCommand("update TOTAL set n3=null where name='NGC848';");
		db.addCommand("update TOTAL set n2=null where name='NGC839';");
		db.addCommand("update TOTAL set n3=null where name='NGC2229';");
		db.addCommand("update TOTAL set n2=null where name='NGC2233';");
		db.addCommand("update TOTAL set n3=null where name='NGC2229';");
		db.addCommand("update TOTAL set n2=null where name='NGC2235';");
		db.addCommand("update TOTAL set n3=null where name='NGC2230';");
		db.addCommand("update TOTAL set n2=null where name='NGC2233';");
		db.addCommand("update TOTAL set n3=null where name='NGC2230';");
		db.addCommand("update TOTAL set n2=null where name='NGC2235';");
		db.addCommand("update TOTAL set n3=null where name='NGC3745';");
		db.addCommand("update TOTAL set n2=null where name='NGC3751';");
		db.addCommand("update TOTAL set n3=null where name='NGC3745';");
		db.addCommand("update TOTAL set n2=null where name='NGC3753';");
		db.addCommand("update TOTAL set n3=null where name='NGC3748';");
		db.addCommand("update TOTAL set n2=null where name='NGC3746';");
		db.addCommand("update TOTAL set n3=null where name='NGC3748';");
		db.addCommand("update TOTAL set n2=null where name='NGC3751';");
		db.addCommand("update TOTAL set n3=null where name='NGC3748';");
		db.addCommand("update TOTAL set n2=null where name='NGC3753';");
		db.addCommand("update TOTAL set n3=null where name='NGC3750';");
		db.addCommand("update TOTAL set n2=null where name='NGC3746';");
		db.addCommand("update TOTAL set n3=null where name='NGC3750';");
		db.addCommand("update TOTAL set n2=null where name='NGC3751';");
		db.addCommand("update TOTAL set n3=null where name='NGC4038';");
		db.addCommand("update TOTAL set n2=null where name='NGC4039';");
		db.addCommand("update TOTAL set n3=null where name='NGC6286';");
		db.addCommand("update TOTAL set n2=null where name='NGC6285';");
		db.addCommand("update TOTAL set n3=null where name='NGC7140';");
		db.addCommand("update TOTAL set n2=null where name='NGC7170';");
		db.addCommand("update TOTAL set n3=null where name='NGC7141';");
		db.addCommand("update TOTAL set n2=null where name='NGC7170';");
		db.addCommand("update TOTAL set n3=null where name='NGC7319';");
		db.addCommand("update TOTAL set n2=null where name='NGC7317';");
		db.addCommand("update TOTAL set n3=null where name='IC1835';");
		db.addCommand("update TOTAL set n2=null where name='IC1847';");
		db.addCommand("update TOTAL set n3=null where name='IC2290';");
		db.addCommand("update TOTAL set n2=null where name='IC2337';");
		db.addCommand("update TOTAL set n3=null where name='IC2392';");
		db.addCommand("update TOTAL set n2=null where name='IC2414';");
		db.addCommand("update TOTAL set n3=null where name='IC4970';");
		db.addCommand("update TOTAL set n2=null where name='NGC6872';");
		db.addCommand("update TOTAL set n3=null where name='IC4981';");
		db.addCommand("update TOTAL set n2=null where name='NGC6880';");
		db.addCommand("update TOTAL set n3=null where name='IC5320';");
		db.addCommand("update TOTAL set n2=null where name='NGC7655';");
		db.addCommand("update TOTAL set n3=null where name='IC5322';");
		db.addCommand("update TOTAL set n2=null where name='NGC7655';");
		db.addCommand("update TOTAL set n3=null where name='IC5323';");
		db.addCommand("update TOTAL set n2=null where name='NGC7655';");
		db.addCommand("update TOTAL set n3=null where name='IC5324';");
		db.addCommand("update TOTAL set n2=null where name='NGC7655';");
		db.addCommand("update TOTAL set n3=null where name='NGC87';");
		db.addCommand("update TOTAL set n3=null where name='NGC88';");
		db.addCommand("update TOTAL set n3=null where name='NGC87';");
		db.addCommand("update TOTAL set n3=null where name='NGC89';");
		db.addCommand("update TOTAL set n3=null where name='NGC87';");
		db.addCommand("update TOTAL set n3=null where name='NGC92';");
		db.addCommand("update TOTAL set n3=null where name='NGC88';");
		db.addCommand("update TOTAL set n3=null where name='NGC87';");
		db.addCommand("update TOTAL set n3=null where name='NGC88';");
		db.addCommand("update TOTAL set n3=null where name='NGC89';");
		db.addCommand("update TOTAL set n3=null where name='NGC88';");
		db.addCommand("update TOTAL set n3=null where name='NGC92';");
		db.addCommand("update TOTAL set n3=null where name='NGC89';");
		db.addCommand("update TOTAL set n3=null where name='NGC87';");
		db.addCommand("update TOTAL set n3=null where name='NGC89';");
		db.addCommand("update TOTAL set n3=null where name='NGC88';");
		db.addCommand("update TOTAL set n3=null where name='NGC89';");
		db.addCommand("update TOTAL set n3=null where name='NGC92';");
		db.addCommand("update TOTAL set n3=null where name='NGC92';");
		db.addCommand("update TOTAL set n3=null where name='NGC87';");
		db.addCommand("update TOTAL set n3=null where name='NGC92';");
		db.addCommand("update TOTAL set n3=null where name='NGC88';");
		db.addCommand("update TOTAL set n3=null where name='NGC92';");
		db.addCommand("update TOTAL set n3=null where name='NGC89';");
		db.addCommand("update TOTAL set n3=null where name='NGC323';");
		db.addCommand("update TOTAL set n3=null where name='NGC328';");
		db.addCommand("update TOTAL set n3=null where name='NGC328';");
		db.addCommand("update TOTAL set n3=null where name='NGC323';");
		db.addCommand("update TOTAL set n3=null where name='NGC434';");
		db.addCommand("update TOTAL set n3=null where name='NGC440';");
		db.addCommand("update TOTAL set n3=null where name='NGC440';");
		db.addCommand("update TOTAL set n3=null where name='NGC434';");
		db.addCommand("update TOTAL set n3=null where name='NGC460';");
		db.addCommand("update TOTAL set n3=null where name='NGC465';");
		db.addCommand("update TOTAL set n3=null where name='NGC465';");
		db.addCommand("update TOTAL set n3=null where name='NGC460';");
		db.addCommand("update TOTAL set n3=null where name='NGC507';");
		db.addCommand("update TOTAL set n3=null where name='NGC508';");
		db.addCommand("update TOTAL set n3=null where name='NGC508';");
		db.addCommand("update TOTAL set n3=null where name='NGC507';");
		db.addCommand("update TOTAL set n3=null where name='NGC614';");
		db.addCommand("update TOTAL set n3=null where name='NGC618';");
		db.addCommand("update TOTAL set n3=null where name='NGC618';");
		db.addCommand("update TOTAL set n3=null where name='NGC614';");
		db.addCommand("update TOTAL set n3=null where name='NGC618';");
		db.addCommand("update TOTAL set n3=null where name='NGC627';");
		db.addCommand("update TOTAL set n3=null where name='NGC627';");
		db.addCommand("update TOTAL set n3=null where name='NGC618';");
		db.addCommand("update TOTAL set n3=null where name='NGC838';");
		db.addCommand("update TOTAL set n3=null where name='NGC848';");
		db.addCommand("update TOTAL set n3=null where name='NGC848';");
		db.addCommand("update TOTAL set n3=null where name='NGC838';");
		db.addCommand("update TOTAL set n3=null where name='NGC1229';");
		db.addCommand("update TOTAL set n3=null where name='NGC1230';");
		db.addCommand("update TOTAL set n3=null where name='NGC1230';");
		db.addCommand("update TOTAL set n3=null where name='NGC1229';");
		db.addCommand("update TOTAL set n3=null where name='NGC1241';");
		db.addCommand("update TOTAL set n3=null where name='NGC1242';");
		db.addCommand("update TOTAL set n3=null where name='NGC1242';");
		db.addCommand("update TOTAL set n3=null where name='NGC1241';");
		db.addCommand("update TOTAL set n3=null where name='NGC1367';");
		db.addCommand("update TOTAL set n3=null where name='NGC1371';");
		db.addCommand("update TOTAL set n3=null where name='NGC1371';");
		db.addCommand("update TOTAL set n3=null where name='NGC1367';");
		db.addCommand("update TOTAL set n3=null where name='NGC1554';");
		db.addCommand("update TOTAL set n3=null where name='NGC1555';");
		db.addCommand("update TOTAL set n3=null where name='NGC1555';");
		db.addCommand("update TOTAL set n3=null where name='NGC1554';");
		db.addCommand("update TOTAL set n3=null where name='NGC1658';");
		db.addCommand("update TOTAL set n3=null where name='NGC1660';");
		db.addCommand("update TOTAL set n3=null where name='NGC1660';");
		db.addCommand("update TOTAL set n3=null where name='NGC1658';");
		db.addCommand("update TOTAL set n3=null where name='NGC2200';");
		db.addCommand("update TOTAL set n3=null where name='NGC2201';");
		db.addCommand("update TOTAL set n3=null where name='NGC2201';");
		db.addCommand("update TOTAL set n3=null where name='NGC2200';");
		db.addCommand("update TOTAL set n3=null where name='NGC2229';");
		db.addCommand("update TOTAL set n3=null where name='NGC2230';");
		db.addCommand("update TOTAL set n3=null where name='NGC2230';");
		db.addCommand("update TOTAL set n3=null where name='NGC2229';");
		db.addCommand("update TOTAL set n3=null where name='NGC2442';");
		db.addCommand("update TOTAL set n3=null where name='NGC2443';");
		db.addCommand("update TOTAL set n3=null where name='NGC2443';");
		db.addCommand("update TOTAL set n3=null where name='NGC2442';");
		db.addCommand("update TOTAL set n3=null where name='NGC2444';");
		db.addCommand("update TOTAL set n3=null where name='NGC2445';");
		db.addCommand("update TOTAL set n3=null where name='NGC2445';");
		db.addCommand("update TOTAL set n3=null where name='NGC2444';");
		db.addCommand("update TOTAL set n3=null where name='NGC2535';");
		db.addCommand("update TOTAL set n3=null where name='NGC2536';");
		db.addCommand("update TOTAL set n3=null where name='NGC2536';");
		db.addCommand("update TOTAL set n3=null where name='NGC2535';");
		db.addCommand("update TOTAL set n3=null where name='NGC2798';");
		db.addCommand("update TOTAL set n3=null where name='NGC2799';");
		db.addCommand("update TOTAL set n3=null where name='NGC2799';");
		db.addCommand("update TOTAL set n3=null where name='NGC2798';");
		db.addCommand("update TOTAL set n3=null where name='NGC2854';");
		db.addCommand("update TOTAL set n3=null where name='NGC2856';");
		db.addCommand("update TOTAL set n3=null where name='NGC2856';");
		db.addCommand("update TOTAL set n3=null where name='NGC2854';");
		db.addCommand("update TOTAL set n3=null where name='NGC2872';");
		db.addCommand("update TOTAL set n3=null where name='NGC2874';");
		db.addCommand("update TOTAL set n3=null where name='NGC2874';");
		db.addCommand("update TOTAL set n3=null where name='NGC2872';");
		db.addCommand("update TOTAL set n3=null where name='NGC3187';");
		db.addCommand("update TOTAL set n3=null where name='NGC3193';");
		db.addCommand("update TOTAL set n3=null where name='NGC3193';");
		db.addCommand("update TOTAL set n3=null where name='NGC3187';");
		db.addCommand("update TOTAL set n3=null where name='NGC3212';");
		db.addCommand("update TOTAL set n3=null where name='NGC3215';");
		db.addCommand("update TOTAL set n3=null where name='NGC3215';");
		db.addCommand("update TOTAL set n3=null where name='NGC3212';");
		db.addCommand("update TOTAL set n3=null where name='NGC3226';");
		db.addCommand("update TOTAL set n3=null where name='NGC3227';");
		db.addCommand("update TOTAL set n3=null where name='NGC3227';");
		db.addCommand("update TOTAL set n3=null where name='NGC3226';");
		db.addCommand("update TOTAL set n3=null where name='NGC3267';");
		db.addCommand("update TOTAL set n3=null where name='NGC3268';");
		db.addCommand("update TOTAL set n3=null where name='NGC3267';");
		db.addCommand("update TOTAL set n3=null where name='NGC3269';");
		db.addCommand("update TOTAL set n3=null where name='NGC3267';");
		db.addCommand("update TOTAL set n3=null where name='NGC3271';");
		db.addCommand("update TOTAL set n3=null where name='NGC3267';");
		db.addCommand("update TOTAL set n3=null where name='NGC3273';");
		db.addCommand("update TOTAL set n3=null where name='NGC3268';");
		db.addCommand("update TOTAL set n3=null where name='NGC3267';");
		db.addCommand("update TOTAL set n3=null where name='NGC3268';");
		db.addCommand("update TOTAL set n3=null where name='NGC3269';");
		db.addCommand("update TOTAL set n3=null where name='NGC3268';");
		db.addCommand("update TOTAL set n3=null where name='NGC3271';");
		db.addCommand("update TOTAL set n3=null where name='NGC3268';");
		db.addCommand("update TOTAL set n3=null where name='NGC3273';");
		db.addCommand("update TOTAL set n3=null where name='NGC3269';");
		db.addCommand("update TOTAL set n3=null where name='NGC3267';");
		db.addCommand("update TOTAL set n3=null where name='NGC3269';");
		db.addCommand("update TOTAL set n3=null where name='NGC3268';");
		db.addCommand("update TOTAL set n3=null where name='NGC3269';");
		db.addCommand("update TOTAL set n3=null where name='NGC3271';");
		db.addCommand("update TOTAL set n3=null where name='NGC3269';");
		db.addCommand("update TOTAL set n3=null where name='NGC3273';");
		db.addCommand("update TOTAL set n3=null where name='NGC3271';");
		db.addCommand("update TOTAL set n3=null where name='NGC3267';");
		db.addCommand("update TOTAL set n3=null where name='NGC3271';");
		db.addCommand("update TOTAL set n3=null where name='NGC3268';");
		db.addCommand("update TOTAL set n3=null where name='NGC3271';");
		db.addCommand("update TOTAL set n3=null where name='NGC3269';");
		db.addCommand("update TOTAL set n3=null where name='NGC3271';");
		db.addCommand("update TOTAL set n3=null where name='NGC3273';");
		db.addCommand("update TOTAL set n3=null where name='NGC3273';");
		db.addCommand("update TOTAL set n3=null where name='NGC3267';");
		db.addCommand("update TOTAL set n3=null where name='NGC3273';");
		db.addCommand("update TOTAL set n3=null where name='NGC3268';");
		db.addCommand("update TOTAL set n3=null where name='NGC3273';");
		db.addCommand("update TOTAL set n3=null where name='NGC3269';");
		db.addCommand("update TOTAL set n3=null where name='NGC3273';");
		db.addCommand("update TOTAL set n3=null where name='NGC3271';");
		db.addCommand("update TOTAL set n3=null where name='NGC3309';");
		db.addCommand("update TOTAL set n3=null where name='NGC3311';");
		db.addCommand("update TOTAL set n3=null where name='NGC3309';");
		db.addCommand("update TOTAL set n3=null where name='NGC3312';");
		db.addCommand("update TOTAL set n3=null where name='NGC3311';");
		db.addCommand("update TOTAL set n3=null where name='NGC3309';");
		db.addCommand("update TOTAL set n3=null where name='NGC3311';");
		db.addCommand("update TOTAL set n3=null where name='NGC3312';");
		db.addCommand("update TOTAL set n3=null where name='NGC3312';");
		db.addCommand("update TOTAL set n3=null where name='NGC3309';");
		db.addCommand("update TOTAL set n3=null where name='NGC3312';");
		db.addCommand("update TOTAL set n3=null where name='NGC3311';");
		db.addCommand("update TOTAL set n3=null where name='NGC3331';");
		db.addCommand("update TOTAL set n3=null where name='NGC3335';");
		db.addCommand("update TOTAL set n3=null where name='NGC3335';");
		db.addCommand("update TOTAL set n3=null where name='NGC3331';");
		db.addCommand("update TOTAL set n3=null where name='NGC3373';");
		db.addCommand("update TOTAL set n3=null where name='NGC3389';");
		db.addCommand("update TOTAL set n3=null where name='NGC3389';");
		db.addCommand("update TOTAL set n3=null where name='NGC3373';");
		db.addCommand("update TOTAL set n3=null where name='NGC3395';");
		db.addCommand("update TOTAL set n3=null where name='NGC3396';");
		db.addCommand("update TOTAL set n3=null where name='NGC3396';");
		db.addCommand("update TOTAL set n3=null where name='NGC3395';");
		db.addCommand("update TOTAL set n3=null where name='NGC3680';");
		db.addCommand("update TOTAL set n3=null where name='NGC6134';");
		db.addCommand("update TOTAL set n3=null where name='NGC3742';");
		db.addCommand("update TOTAL set n3=null where name='NGC3749';");
		db.addCommand("update TOTAL set n3=null where name='NGC3745';");
		db.addCommand("update TOTAL set n3=null where name='NGC3748';");
		db.addCommand("update TOTAL set n3=null where name='NGC3745';");
		db.addCommand("update TOTAL set n3=null where name='NGC3750';");
		db.addCommand("update TOTAL set n3=null where name='NGC3748';");
		db.addCommand("update TOTAL set n3=null where name='NGC3745';");
		db.addCommand("update TOTAL set n3=null where name='NGC3748';");
		db.addCommand("update TOTAL set n3=null where name='NGC3750';");
		db.addCommand("update TOTAL set n3=null where name='NGC3749';");
		db.addCommand("update TOTAL set n3=null where name='NGC3742';");
		db.addCommand("update TOTAL set n3=null where name='NGC3750';");
		db.addCommand("update TOTAL set n3=null where name='NGC3745';");
		db.addCommand("update TOTAL set n3=null where name='NGC3750';");
		db.addCommand("update TOTAL set n3=null where name='NGC3748';");
		db.addCommand("update TOTAL set n3=null where name='NGC3799';");
		db.addCommand("update TOTAL set n3=null where name='NGC3800';");
		db.addCommand("update TOTAL set n3=null where name='NGC3800';");
		db.addCommand("update TOTAL set n3=null where name='NGC3799';");
		db.addCommand("update TOTAL set n3=null where name='NGC3994';");
		db.addCommand("update TOTAL set n3=null where name='NGC3995';");
		db.addCommand("update TOTAL set n3=null where name='NGC3995';");
		db.addCommand("update TOTAL set n3=null where name='NGC3994';");
		db.addCommand("update TOTAL set n3=null where name='NGC4016';");
		db.addCommand("update TOTAL set n3=null where name='NGC4017';");
		db.addCommand("update TOTAL set n3=null where name='NGC4017';");
		db.addCommand("update TOTAL set n3=null where name='NGC4016';");
		db.addCommand("update TOTAL set n3=null where name='NGC4485';");
		db.addCommand("update TOTAL set n3=null where name='NGC4490';");
		db.addCommand("update TOTAL set n3=null where name='NGC4490';");
		db.addCommand("update TOTAL set n3=null where name='NGC4485';");
		db.addCommand("update TOTAL set n3=null where name='NGC4903';");
		db.addCommand("update TOTAL set n3=null where name='NGC4905';");
		db.addCommand("update TOTAL set n3=null where name='NGC4905';");
		db.addCommand("update TOTAL set n3=null where name='NGC4903';");
		db.addCommand("update TOTAL set n3=null where name='NGC5048';");
		db.addCommand("update TOTAL set n3=null where name='NGC5051';");
		db.addCommand("update TOTAL set n3=null where name='NGC5051';");
		db.addCommand("update TOTAL set n3=null where name='NGC5048';");
		db.addCommand("update TOTAL set n3=null where name='NGC5090';");
		db.addCommand("update TOTAL set n3=null where name='NGC5091';");
		db.addCommand("update TOTAL set n3=null where name='NGC5091';");
		db.addCommand("update TOTAL set n3=null where name='NGC5090';");
		db.addCommand("update TOTAL set n3=null where name='NGC5194';");
		db.addCommand("update TOTAL set n3=null where name='NGC5195';");
		db.addCommand("update TOTAL set n3=null where name='NGC5195';");
		db.addCommand("update TOTAL set n3=null where name='NGC5194';");
		db.addCommand("update TOTAL set n3=null where name='NGC5216';");
		db.addCommand("update TOTAL set n3=null where name='NGC5218';");
		db.addCommand("update TOTAL set n3=null where name='NGC5218';");
		db.addCommand("update TOTAL set n3=null where name='NGC5216';");
		db.addCommand("update TOTAL set n3=null where name='NGC5221';");
		db.addCommand("update TOTAL set n3=null where name='NGC5222';");
		db.addCommand("update TOTAL set n3=null where name='NGC5222';");
		db.addCommand("update TOTAL set n3=null where name='NGC5221';");
		db.addCommand("update TOTAL set n3=null where name='NGC5257';");
		db.addCommand("update TOTAL set n3=null where name='NGC5258';");
		db.addCommand("update TOTAL set n3=null where name='NGC5258';");
		db.addCommand("update TOTAL set n3=null where name='NGC5257';");
		db.addCommand("update TOTAL set n3=null where name='NGC5394';");
		db.addCommand("update TOTAL set n3=null where name='NGC5395';");
		db.addCommand("update TOTAL set n3=null where name='NGC5395';");
		db.addCommand("update TOTAL set n3=null where name='NGC5394';");
		db.addCommand("update TOTAL set n3=null where name='NGC5560';");
		db.addCommand("update TOTAL set n3=null where name='NGC5566';");
		db.addCommand("update TOTAL set n3=null where name='NGC5560';");
		db.addCommand("update TOTAL set n3=null where name='NGC5569';");
		db.addCommand("update TOTAL set n3=null where name='NGC5566';");
		db.addCommand("update TOTAL set n3=null where name='NGC5560';");
		db.addCommand("update TOTAL set n3=null where name='NGC5566';");
		db.addCommand("update TOTAL set n3=null where name='NGC5569';");
		db.addCommand("update TOTAL set n3=null where name='NGC5569';");
		db.addCommand("update TOTAL set n3=null where name='NGC5560';");
		db.addCommand("update TOTAL set n3=null where name='NGC5569';");
		db.addCommand("update TOTAL set n3=null where name='NGC5566';");
		db.addCommand("update TOTAL set n3=null where name='NGC5613';");
		db.addCommand("update TOTAL set n3=null where name='NGC5614';");
		db.addCommand("update TOTAL set n3=null where name='NGC5613';");
		db.addCommand("update TOTAL set n3=null where name='NGC5615';");
		db.addCommand("update TOTAL set n3=null where name='NGC5614';");
		db.addCommand("update TOTAL set n3=null where name='NGC5613';");
		db.addCommand("update TOTAL set n3=null where name='NGC5615';");
		db.addCommand("update TOTAL set n3=null where name='NGC5613';");
		db.addCommand("update TOTAL set n3=null where name='NGC5625';");
		db.addCommand("update TOTAL set n3=null where name='IC1520';");
		db.addCommand("update TOTAL set n3=null where name='NGC5754';");
		db.addCommand("update TOTAL set n3=null where name='NGC5755';");
		db.addCommand("update TOTAL set n3=null where name='NGC5755';");
		db.addCommand("update TOTAL set n3=null where name='NGC5754';");
		db.addCommand("update TOTAL set n3=null where name='NGC6134';");
		db.addCommand("update TOTAL set n3=null where name='NGC3680';");
		db.addCommand("update TOTAL set n3=null where name='NGC6427';");
		db.addCommand("update TOTAL set n3=null where name='NGC6431';");
		db.addCommand("update TOTAL set n3=null where name='NGC6431';");
		db.addCommand("update TOTAL set n3=null where name='NGC6427';");
		db.addCommand("update TOTAL set n3=null where name='NGC6769';");
		db.addCommand("update TOTAL set n3=null where name='NGC6770';");
		db.addCommand("update TOTAL set n3=null where name='NGC6770';");
		db.addCommand("update TOTAL set n3=null where name='NGC6769';");
		db.addCommand("update TOTAL set n3=null where name='NGC6871';");
		db.addCommand("update TOTAL set n3=null where name='NGC6883';");
		db.addCommand("update TOTAL set n3=null where name='NGC6883';");
		db.addCommand("update TOTAL set n3=null where name='NGC6871';");
		db.addCommand("update TOTAL set n3=null where name='NGC7016';");
		db.addCommand("update TOTAL set n3=null where name='NGC7017';");
		db.addCommand("update TOTAL set n3=null where name='NGC7017';");
		db.addCommand("update TOTAL set n3=null where name='NGC7016';");
		db.addCommand("update TOTAL set n3=null where name='NGC7103';");
		db.addCommand("update TOTAL set n3=null where name='IC5122';");
		db.addCommand("update TOTAL set n3=null where name='NGC7103';");
		db.addCommand("update TOTAL set n3=null where name='IC5124';");
		db.addCommand("update TOTAL set n3=null where name='NGC7140';");
		db.addCommand("update TOTAL set n3=null where name='NGC7141';");
		db.addCommand("update TOTAL set n3=null where name='NGC7141';");
		db.addCommand("update TOTAL set n3=null where name='NGC7140';");
		db.addCommand("update TOTAL set n3=null where name='NGC7733';");
		db.addCommand("update TOTAL set n3=null where name='NGC7734';");
		db.addCommand("update TOTAL set n3=null where name='NGC7734';");
		db.addCommand("update TOTAL set n3=null where name='NGC7733';");
		db.addCommand("update TOTAL set n3=null where name='IC1520';");
		db.addCommand("update TOTAL set n3=null where name='NGC5625';");
		db.addCommand("update TOTAL set n3=null where name='IC2038';");
		db.addCommand("update TOTAL set n3=null where name='IC2039';");
		db.addCommand("update TOTAL set n3=null where name='IC2039';");
		db.addCommand("update TOTAL set n3=null where name='IC2038';");
		db.addCommand("update TOTAL set n3=null where name='IC3108';");
		db.addCommand("update TOTAL set n3=null where name='IC3109';");
		db.addCommand("update TOTAL set n3=null where name='IC3109';");
		db.addCommand("update TOTAL set n3=null where name='IC3108';");
		db.addCommand("update TOTAL set n3=null where name='IC4584';");
		db.addCommand("update TOTAL set n3=null where name='IC4585';");
		db.addCommand("update TOTAL set n3=null where name='IC4585';");
		db.addCommand("update TOTAL set n3=null where name='IC4584';");
		db.addCommand("update TOTAL set n3=null where name='IC4704';");
		db.addCommand("update TOTAL set n3=null where name='IC4705';");
		db.addCommand("update TOTAL set n3=null where name='IC4705';");
		db.addCommand("update TOTAL set n3=null where name='IC4704';");
		db.addCommand("update TOTAL set n3=null where name='IC4720';");
		db.addCommand("update TOTAL set n3=null where name='IC4721';");
		db.addCommand("update TOTAL set n3=null where name='IC4721';");
		db.addCommand("update TOTAL set n3=null where name='IC4720';");
		db.addCommand("update TOTAL set n3=null where name='IC4796';");
		db.addCommand("update TOTAL set n3=null where name='IC4797';");
		db.addCommand("update TOTAL set n3=null where name='IC4797';");
		db.addCommand("update TOTAL set n3=null where name='IC4796';");
		db.addCommand("update TOTAL set n3=null where name='IC4837';");
		db.addCommand("update TOTAL set n3=null where name='IC4839';");
		db.addCommand("update TOTAL set n3=null where name='IC4839';");
		db.addCommand("update TOTAL set n3=null where name='IC4837';");
		db.addCommand("update TOTAL set n3=null where name='IC4860';");
		db.addCommand("update TOTAL set n3=null where name='IC4862';");
		db.addCommand("update TOTAL set n3=null where name='IC4862';");
		db.addCommand("update TOTAL set n3=null where name='IC4860';");
		db.addCommand("update TOTAL set n3=null where name='IC4941';");
		db.addCommand("update TOTAL set n3=null where name='IC4942';");
		db.addCommand("update TOTAL set n3=null where name='IC4942';");
		db.addCommand("update TOTAL set n3=null where name='IC4941';");
		db.addCommand("update TOTAL set n3=null where name='IC5001';");
		db.addCommand("update TOTAL set n3=null where name='IC5002';");
		db.addCommand("update TOTAL set n3=null where name='IC5002';");
		db.addCommand("update TOTAL set n3=null where name='IC5001';");
		db.addCommand("update TOTAL set n3=null where name='IC5053';");
		db.addCommand("update TOTAL set n3=null where name='IC5054';");
		db.addCommand("update TOTAL set n3=null where name='IC5054';");
		db.addCommand("update TOTAL set n3=null where name='IC5053';");
		db.addCommand("update TOTAL set n3=null where name='IC5122';");
		db.addCommand("update TOTAL set n3=null where name='IC5124';");
		db.addCommand("update TOTAL set n3=null where name='IC5122';");
		db.addCommand("update TOTAL set n3=null where name='NGC7103';");
		db.addCommand("update TOTAL set n3=null where name='IC5124';");
		db.addCommand("update TOTAL set n3=null where name='IC5122';");
		db.addCommand("update TOTAL set n3=null where name='IC5124';");
		db.addCommand("update TOTAL set n3=null where name='NGC7103';");
		db.addCommand("update TOTAL set n3=null where name='IC5209';");
		db.addCommand("update TOTAL set n3=null where name='IC5212';");
		db.addCommand("update TOTAL set n3=null where name='IC5212';");
		db.addCommand("update TOTAL set n3=null where name='IC5209';");
		db.addCommand("update TOTAL set n3=null where name='IC5219';");
		db.addCommand("update TOTAL set n3=null where name='IC5221';");
		db.addCommand("update TOTAL set n3=null where name='IC5221';");
		db.addCommand("update TOTAL set n3=null where name='IC5219';");
		db.addCommand("update TOTAL set n3=null where name='IC5320';");
		db.addCommand("update TOTAL set n3=null where name='IC5323';");
		db.addCommand("update TOTAL set n3=null where name='IC5320';");
		db.addCommand("update TOTAL set n3=null where name='IC5324';");
		db.addCommand("update TOTAL set n3=null where name='IC5322';");
		db.addCommand("update TOTAL set n3=null where name='IC5323';");
		db.addCommand("update TOTAL set n3=null where name='IC5322';");
		db.addCommand("update TOTAL set n3=null where name='IC5324';");
		db.addCommand("update TOTAL set n3=null where name='IC5323';");
		db.addCommand("update TOTAL set n3=null where name='IC5320';");
		db.addCommand("update TOTAL set n3=null where name='IC5323';");
		db.addCommand("update TOTAL set n3=null where name='IC5322';");
		db.addCommand("update TOTAL set n3=null where name='IC5323';");
		db.addCommand("update TOTAL set n3=null where name='IC5324';");
		db.addCommand("update TOTAL set n3=null where name='IC5324';");
		db.addCommand("update TOTAL set n3=null where name='IC5320';");
		db.addCommand("update TOTAL set n3=null where name='IC5324';");
		db.addCommand("update TOTAL set n3=null where name='IC5322';");
		db.addCommand("update TOTAL set n3=null where name='IC5324';");
		db.addCommand("update TOTAL set n3=null where name='IC5323';");
		db.addCommand("update TOTAL set n3=null where name='IC5351';");
		db.addCommand("update TOTAL set n3=null where name='IC5352';");
		db.addCommand("update TOTAL set n3=null where name='IC5351';");
		db.addCommand("update TOTAL set n3=null where name='IC5356';");
		db.addCommand("update TOTAL set n3=null where name='IC5351';");
		db.addCommand("update TOTAL set n3=null where name='IC5357';");
		db.addCommand("update TOTAL set n3=null where name='IC5352';");
		db.addCommand("update TOTAL set n3=null where name='IC5351';");
		db.addCommand("update TOTAL set n3=null where name='IC5352';");
		db.addCommand("update TOTAL set n3=null where name='IC5356';");
		db.addCommand("update TOTAL set n3=null where name='IC5352';");
		db.addCommand("update TOTAL set n3=null where name='IC5357';");
		db.addCommand("update TOTAL set n3=null where name='IC5356';");
		db.addCommand("update TOTAL set n3=null where name='IC5351';");
		db.addCommand("update TOTAL set n3=null where name='IC5356';");
		db.addCommand("update TOTAL set n3=null where name='IC5352';");
		db.addCommand("update TOTAL set n3=null where name='IC5356';");
		db.addCommand("update TOTAL set n3=null where name='IC5357';");
		db.addCommand("update TOTAL set n3=null where name='IC5357';");
		db.addCommand("update TOTAL set n3=null where name='IC5351';");
		db.addCommand("update TOTAL set n3=null where name='IC5357';");
		db.addCommand("update TOTAL set n3=null where name='IC5352';");
		db.addCommand("update TOTAL set n3=null where name='IC5357';");
		db.addCommand("update TOTAL set n3=null where name='IC5356';");
		db.addCommand("update TOTAL set n3=null where name='NGC37';");
		db.addCommand("update TOTAL set n4=null where name='NGC25';");
		db.addCommand("update TOTAL set n3=null where name='NGC383';");
		db.addCommand("update TOTAL set n4=null where name='NGC385';");
		db.addCommand("update TOTAL set n3=null where name='NGC383';");
		db.addCommand("update TOTAL set n4=null where name='NGC386';");
		db.addCommand("update TOTAL set n3=null where name='NGC546';");
		db.addCommand("update TOTAL set n4=null where name='NGC544';");
		db.addCommand("update TOTAL set n3=null where name='NGC846';");
		db.addCommand("update TOTAL set n4=null where name='NGC6228';");
		db.addCommand("update TOTAL set n3=null where name='NGC847';");
		db.addCommand("update TOTAL set n4=null where name='NGC6228';");
		db.addCommand("update TOTAL set n3=null where name='NGC1531';");
		db.addCommand("update TOTAL set n4=null where name='NGC1532';");
		db.addCommand("update TOTAL set n3=null where name='NGC2300';");
		db.addCommand("update TOTAL set n4=null where name='NGC2276';");
		db.addCommand("update TOTAL set n3=null where name='NGC2368';");
		db.addCommand("update TOTAL set n4=null where name='NGC436';");
		db.addCommand("update TOTAL set n3=null where name='NGC2535';");
		db.addCommand("update TOTAL set n4=null where name='NGC2636';");
		db.addCommand("update TOTAL set n3=null where name='NGC2536';");
		db.addCommand("update TOTAL set n4=null where name='NGC2636';");
		db.addCommand("update TOTAL set n3=null where name='NGC2832';");
		db.addCommand("update TOTAL set n4=null where name='NGC2830';");
		db.addCommand("update TOTAL set n3=null where name='NGC3171';");
		db.addCommand("update TOTAL set n4=null where name='NGC3161';");
		db.addCommand("update TOTAL set n3=null where name='NGC3788';");
		db.addCommand("update TOTAL set n4=null where name='NGC3786';");
		db.addCommand("update TOTAL set n3=null where name='NGC3994';");
		db.addCommand("update TOTAL set n4=null where name='NGC3991';");
		db.addCommand("update TOTAL set n3=null where name='NGC3995';");
		db.addCommand("update TOTAL set n4=null where name='NGC3991';");
		db.addCommand("update TOTAL set n3=null where name='NGC4631';");
		db.addCommand("update TOTAL set n4=null where name='NGC4627';");
		db.addCommand("update TOTAL set n3=null where name='NGC4647';");
		db.addCommand("update TOTAL set n4=null where name='NGC4649';");
		db.addCommand("update TOTAL set n3=null where name='NGC4908';");
		db.addCommand("update TOTAL set n4=null where name='IC4051';");
		db.addCommand("update TOTAL set n3=null where name='NGC5427';");
		db.addCommand("update TOTAL set n4=null where name='NGC5426';");
		db.addCommand("update TOTAL set n3=null where name='NGC5457';");
		db.addCommand("update TOTAL set n4=null where name='NGC5474';");
		db.addCommand("update TOTAL set n3=null where name='NGC5994';");
		db.addCommand("update TOTAL set n4=null where name='NGC5996';");
		db.addCommand("update TOTAL set n3=null where name='NGC6872';");
		db.addCommand("update TOTAL set n4=null where name='IC4970';");
		db.addCommand("update TOTAL set n3=null where name='NGC7174';");
		db.addCommand("update TOTAL set n4=null where name='NGC7173';");
		db.addCommand("update TOTAL set n3=null where name='NGC7317';");
		db.addCommand("update TOTAL set n4=null where name='NGC7320';");
		db.addCommand("update TOTAL set n3=null where name='NGC7550';");
		db.addCommand("update TOTAL set n4=null where name='NGC7547';");
		db.addCommand("update TOTAL set n3=null where name='NGC7682';");
		db.addCommand("update TOTAL set n4=null where name='NGC7679';");
		db.addCommand("update TOTAL set n3=null where name='NGC7715';");
		db.addCommand("update TOTAL set n4=null where name='NGC7714';");
		db.addCommand("update TOTAL set n3=null where name='IC3481';");
		db.addCommand("update TOTAL set n4=null where name='IC3483';");
		db.addCommand("update TOTAL set n3=null where name='NGC93';");
		db.addCommand("update TOTAL set n5=null where name='NGC90';");
		db.addCommand("update TOTAL set n3=null where name='NGC383';");
		db.addCommand("update TOTAL set n5=null where name='NGC379';");
		db.addCommand("update TOTAL set n3=null where name='NGC383';");
		db.addCommand("update TOTAL set n5=null where name='NGC380';");
		db.addCommand("update TOTAL set n3=null where name='NGC383';");
		db.addCommand("update TOTAL set n5=null where name='NGC384';");
		db.addCommand("update TOTAL set n3=null where name='NGC474';");
		db.addCommand("update TOTAL set n5=null where name='NGC470';");
		db.addCommand("update TOTAL set n3=null where name='NGC786';");
		db.addCommand("update TOTAL set n5=null where name='NGC78';");
		db.addCommand("update TOTAL set n3=null where name='NGC1000';");
		db.addCommand("update TOTAL set n5=null where name='NGC999';");
		db.addCommand("update TOTAL set n3=null where name='NGC1229';");
		db.addCommand("update TOTAL set n5=null where name='NGC1228';");
		db.addCommand("update TOTAL set n3=null where name='NGC1230';");
		db.addCommand("update TOTAL set n5=null where name='NGC1228';");
		db.addCommand("update TOTAL set n3=null where name='NGC2174';");
		db.addCommand("update TOTAL set n5=null where name='NGC2175';");
		db.addCommand("update TOTAL set n3=null where name='NGC4438';");
		db.addCommand("update TOTAL set n5=null where name='NGC4435';");
		db.addCommand("update TOTAL set n3=null where name='NGC5426';");
		db.addCommand("update TOTAL set n5=null where name='NGC5427';");
		db.addCommand("update TOTAL set n3=null where name='NGC7317';");
		db.addCommand("update TOTAL set n5=null where name='NGC7319';");
		db.addCommand("update TOTAL set n3=null where name='NGC7433';");
		db.addCommand("update TOTAL set n5=null where name='NGC7436';");
		db.addCommand("update TOTAL set n3=null where name='NGC7550';");
		db.addCommand("update TOTAL set n5=null where name='NGC7549';");
		db.addCommand("update TOTAL set n3=null where name='NGC383';");
		db.addCommand("update TOTAL set n6=null where name='NGC388';");
		db.addCommand("update TOTAL set n3=null where name='NGC3187';");
		db.addCommand("update TOTAL set n6=null where name='NGC3190';");
		db.addCommand("update TOTAL set n3=null where name='NGC3193';");
		db.addCommand("update TOTAL set n6=null where name='NGC3190';");
		db.addCommand("update TOTAL set n3=null where name='NGC7319';");
		db.addCommand("update TOTAL set n6=null where name='NGC7320';");
		db.addCommand("update TOTAL set n3=null where name='NGC3193';");
		db.addCommand("update TOTAL set n7=null where name='NGC3187';");
		db.addCommand("update TOTAL set n3=null where name='NGC7319';");
		db.addCommand("update TOTAL set n7=null where name='NGC7318';");
		db.addCommand("update TOTAL set n3=null where name='IC694';");
		db.addCommand("update TOTAL set n7=null where name='NGC3690';");
		db.addCommand("update TOTAL set n3=null where name='NGC7317';");
		db.addCommand("update TOTAL set n8=null where name='NGC7318';");
		db.addCommand("update TOTAL set n3=null where name='NGC7753';");
		db.addCommand("update TOTAL set n8=null where name='NGC7752';");
		db.addCommand("update TOTAL set n4=null where name='NGC2243';");
		db.addCommand("update TOTAL set n1=null where name='NGC2239';");
		db.addCommand("update TOTAL set n4=null where name='NGC2243';");
		db.addCommand("update TOTAL set n1=null where name='NGC2244';");
		db.addCommand("update TOTAL set n4=null where name='NGC7129';");
		db.addCommand("update TOTAL set n1=null where name='IC5132';");
		db.addCommand("update TOTAL set n4=null where name='IC3128';");
		db.addCommand("update TOTAL set n1=null where name='IC3174';");
		db.addCommand("update TOTAL set n4=null where name='IC3128';");
		db.addCommand("update TOTAL set n1=null where name='IC3174';");
		db.addCommand("update TOTAL set n4=null where name='NGC25';");
		db.addCommand("update TOTAL set n2=null where name='NGC28';");
		db.addCommand("update TOTAL set n4=null where name='NGC25';");
		db.addCommand("update TOTAL set n2=null where name='NGC31';");
		db.addCommand("update TOTAL set n4=null where name='NGC341';");
		db.addCommand("update TOTAL set n2=null where name='NGC3410';");
		db.addCommand("update TOTAL set n4=null where name='NGC385';");
		db.addCommand("update TOTAL set n2=null where name='NGC375';");
		db.addCommand("update TOTAL set n4=null where name='NGC386';");
		db.addCommand("update TOTAL set n2=null where name='NGC375';");
		db.addCommand("update TOTAL set n4=null where name='NGC2175';");
		db.addCommand("update TOTAL set n2=null where name='NGC2174';");
		db.addCommand("update TOTAL set n4=null where name='NGC2933';");
		db.addCommand("update TOTAL set n2=null where name='NGC2924';");
		db.addCommand("update TOTAL set n4=null where name='IC775';");
		db.addCommand("update TOTAL set n2=null where name='IC3255';");
		db.addCommand("update TOTAL set n4=null where name='IC999';");
		db.addCommand("update TOTAL set n2=null where name='IC4434';");
		db.addCommand("update TOTAL set n4=null where name='IC4949';");
		db.addCommand("update TOTAL set n2=null where name='NGC6868';");
		db.addCommand("update TOTAL set n4=null where name='IC4949';");
		db.addCommand("update TOTAL set n2=null where name='NGC6870';");
		db.addCommand("update TOTAL set n4=null where name='NGC25';");
		db.addCommand("update TOTAL set n3=null where name='NGC37';");
		db.addCommand("update TOTAL set n4=null where name='NGC385';");
		db.addCommand("update TOTAL set n3=null where name='NGC383';");
		db.addCommand("update TOTAL set n4=null where name='NGC386';");
		db.addCommand("update TOTAL set n3=null where name='NGC383';");
		db.addCommand("update TOTAL set n4=null where name='NGC436';");
		db.addCommand("update TOTAL set n3=null where name='NGC2368';");
		db.addCommand("update TOTAL set n4=null where name='NGC544';");
		db.addCommand("update TOTAL set n3=null where name='NGC546';");
		db.addCommand("update TOTAL set n4=null where name='NGC1532';");
		db.addCommand("update TOTAL set n3=null where name='NGC1531';");
		db.addCommand("update TOTAL set n4=null where name='NGC2276';");
		db.addCommand("update TOTAL set n3=null where name='NGC2300';");
		db.addCommand("update TOTAL set n4=null where name='NGC2636';");
		db.addCommand("update TOTAL set n3=null where name='NGC2535';");
		db.addCommand("update TOTAL set n4=null where name='NGC2636';");
		db.addCommand("update TOTAL set n3=null where name='NGC2536';");
		db.addCommand("update TOTAL set n4=null where name='NGC2830';");
		db.addCommand("update TOTAL set n3=null where name='NGC2832';");
		db.addCommand("update TOTAL set n4=null where name='NGC3161';");
		db.addCommand("update TOTAL set n3=null where name='NGC3171';");
		db.addCommand("update TOTAL set n4=null where name='NGC3786';");
		db.addCommand("update TOTAL set n3=null where name='NGC3788';");
		db.addCommand("update TOTAL set n4=null where name='NGC3991';");
		db.addCommand("update TOTAL set n3=null where name='NGC3994';");
		db.addCommand("update TOTAL set n4=null where name='NGC3991';");
		db.addCommand("update TOTAL set n3=null where name='NGC3995';");
		db.addCommand("update TOTAL set n4=null where name='NGC4627';");
		db.addCommand("update TOTAL set n3=null where name='NGC4631';");
		db.addCommand("update TOTAL set n4=null where name='NGC4649';");
		db.addCommand("update TOTAL set n3=null where name='NGC4647';");
		db.addCommand("update TOTAL set n4=null where name='NGC5426';");
		db.addCommand("update TOTAL set n3=null where name='NGC5427';");
		db.addCommand("update TOTAL set n4=null where name='NGC5474';");
		db.addCommand("update TOTAL set n3=null where name='NGC5457';");
		db.addCommand("update TOTAL set n4=null where name='NGC5996';");
		db.addCommand("update TOTAL set n3=null where name='NGC5994';");
		db.addCommand("update TOTAL set n4=null where name='NGC6228';");
		db.addCommand("update TOTAL set n3=null where name='NGC846';");
		db.addCommand("update TOTAL set n4=null where name='NGC6228';");
		db.addCommand("update TOTAL set n3=null where name='NGC847';");
		db.addCommand("update TOTAL set n4=null where name='NGC7173';");
		db.addCommand("update TOTAL set n3=null where name='NGC7174';");
		db.addCommand("update TOTAL set n4=null where name='NGC7320';");
		db.addCommand("update TOTAL set n3=null where name='NGC7317';");
		db.addCommand("update TOTAL set n4=null where name='NGC7547';");
		db.addCommand("update TOTAL set n3=null where name='NGC7550';");
		db.addCommand("update TOTAL set n4=null where name='NGC7679';");
		db.addCommand("update TOTAL set n3=null where name='NGC7682';");
		db.addCommand("update TOTAL set n4=null where name='NGC7714';");
		db.addCommand("update TOTAL set n3=null where name='NGC7715';");
		db.addCommand("update TOTAL set n4=null where name='IC3483';");
		db.addCommand("update TOTAL set n3=null where name='IC3481';");
		db.addCommand("update TOTAL set n4=null where name='IC4051';");
		db.addCommand("update TOTAL set n3=null where name='NGC4908';");
		db.addCommand("update TOTAL set n4=null where name='IC4970';");
		db.addCommand("update TOTAL set n3=null where name='NGC6872';");
		db.addCommand("update TOTAL set n4=null where name='NGC142';");
		db.addCommand("update TOTAL set n4=null where name='NGC143';");
		db.addCommand("update TOTAL set n4=null where name='NGC142';");
		db.addCommand("update TOTAL set n4=null where name='NGC144';");
		db.addCommand("update TOTAL set n4=null where name='NGC143';");
		db.addCommand("update TOTAL set n4=null where name='NGC142';");
		db.addCommand("update TOTAL set n4=null where name='NGC143';");
		db.addCommand("update TOTAL set n4=null where name='NGC144';");
		db.addCommand("update TOTAL set n4=null where name='NGC144';");
		db.addCommand("update TOTAL set n4=null where name='NGC142';");
		db.addCommand("update TOTAL set n4=null where name='NGC144';");
		db.addCommand("update TOTAL set n4=null where name='NGC143';");
		db.addCommand("update TOTAL set n4=null where name='NGC168';");
		db.addCommand("update TOTAL set n4=null where name='NGC172';");
		db.addCommand("update TOTAL set n4=null where name='NGC172';");
		db.addCommand("update TOTAL set n4=null where name='NGC168';");
		db.addCommand("update TOTAL set n4=null where name='NGC230';");
		db.addCommand("update TOTAL set n4=null where name='NGC235';");
		db.addCommand("update TOTAL set n4=null where name='NGC235';");
		db.addCommand("update TOTAL set n4=null where name='NGC230';");
		db.addCommand("update TOTAL set n4=null where name='NGC385';");
		db.addCommand("update TOTAL set n4=null where name='NGC386';");
		db.addCommand("update TOTAL set n4=null where name='NGC386';");
		db.addCommand("update TOTAL set n4=null where name='NGC385';");
		db.addCommand("update TOTAL set n4=null where name='NGC1228';");
		db.addCommand("update TOTAL set n4=null where name='NGC1230';");
		db.addCommand("update TOTAL set n4=null where name='NGC1230';");
		db.addCommand("update TOTAL set n4=null where name='NGC1228';");
		db.addCommand("update TOTAL set n4=null where name='NGC1367';");
		db.addCommand("update TOTAL set n4=null where name='NGC1371';");
		db.addCommand("update TOTAL set n4=null where name='NGC1371';");
		db.addCommand("update TOTAL set n4=null where name='NGC1367';");
		db.addCommand("update TOTAL set n4=null where name='NGC1374';");
		db.addCommand("update TOTAL set n4=null where name='NGC1375';");
		db.addCommand("update TOTAL set n4=null where name='NGC1375';");
		db.addCommand("update TOTAL set n4=null where name='NGC1374';");
		db.addCommand("update TOTAL set n4=null where name='NGC3226';");
		db.addCommand("update TOTAL set n4=null where name='NGC3227';");
		db.addCommand("update TOTAL set n4=null where name='NGC3227';");
		db.addCommand("update TOTAL set n4=null where name='NGC3226';");
		db.addCommand("update TOTAL set n4=null where name='NGC3627';");
		db.addCommand("update TOTAL set n4=null where name='NGC3628';");
		db.addCommand("update TOTAL set n4=null where name='NGC3628';");
		db.addCommand("update TOTAL set n4=null where name='NGC3627';");
		db.addCommand("update TOTAL set n4=null where name='NGC4004';");
		db.addCommand("update TOTAL set n4=null where name='NGC6054';");
		db.addCommand("update TOTAL set n4=null where name='NGC4105';");
		db.addCommand("update TOTAL set n4=null where name='NGC4106';");
		db.addCommand("update TOTAL set n4=null where name='NGC4106';");
		db.addCommand("update TOTAL set n4=null where name='NGC4105';");
		db.addCommand("update TOTAL set n4=null where name='NGC4373';");
		db.addCommand("update TOTAL set n4=null where name='IC3290';");
		db.addCommand("update TOTAL set n4=null where name='NGC5317';");
		db.addCommand("update TOTAL set n4=null where name='NGC5364';");
		db.addCommand("update TOTAL set n4=null where name='NGC5364';");
		db.addCommand("update TOTAL set n4=null where name='NGC5317';");
		db.addCommand("update TOTAL set n4=null where name='NGC5519';");
		db.addCommand("update TOTAL set n4=null where name='NGC5570';");
		db.addCommand("update TOTAL set n4=null where name='NGC5570';");
		db.addCommand("update TOTAL set n4=null where name='NGC5519';");
		db.addCommand("update TOTAL set n4=null where name='NGC5898';");
		db.addCommand("update TOTAL set n4=null where name='NGC5903';");
		db.addCommand("update TOTAL set n4=null where name='NGC5903';");
		db.addCommand("update TOTAL set n4=null where name='NGC5898';");
		db.addCommand("update TOTAL set n4=null where name='NGC6054';");
		db.addCommand("update TOTAL set n4=null where name='NGC4004';");
		db.addCommand("update TOTAL set n4=null where name='NGC7140';");
		db.addCommand("update TOTAL set n4=null where name='NGC7141';");
		db.addCommand("update TOTAL set n4=null where name='NGC7141';");
		db.addCommand("update TOTAL set n4=null where name='NGC7140';");
		db.addCommand("update TOTAL set n4=null where name='NGC7173';");
		db.addCommand("update TOTAL set n4=null where name='NGC7176';");
		db.addCommand("update TOTAL set n4=null where name='NGC7176';");
		db.addCommand("update TOTAL set n4=null where name='NGC7173';");
		db.addCommand("update TOTAL set n4=null where name='NGC7752';");
		db.addCommand("update TOTAL set n4=null where name='NGC7753';");
		db.addCommand("update TOTAL set n4=null where name='NGC7753';");
		db.addCommand("update TOTAL set n4=null where name='NGC7752';");
		db.addCommand("update TOTAL set n4=null where name='NGC7778';");
		db.addCommand("update TOTAL set n4=null where name='NGC7779';");
		db.addCommand("update TOTAL set n4=null where name='NGC7779';");
		db.addCommand("update TOTAL set n4=null where name='NGC7778';");
		db.addCommand("update TOTAL set n4=null where name='IC1157';");
		db.addCommand("update TOTAL set n4=null where name='IC1160';");
		db.addCommand("update TOTAL set n4=null where name='IC1160';");
		db.addCommand("update TOTAL set n4=null where name='IC1157';");
		db.addCommand("update TOTAL set n4=null where name='IC1561';");
		db.addCommand("update TOTAL set n4=null where name='IC1562';");
		db.addCommand("update TOTAL set n4=null where name='IC1562';");
		db.addCommand("update TOTAL set n4=null where name='IC1561';");
		db.addCommand("update TOTAL set n4=null where name='IC1811';");
		db.addCommand("update TOTAL set n4=null where name='IC1813';");
		db.addCommand("update TOTAL set n4=null where name='IC1813';");
		db.addCommand("update TOTAL set n4=null where name='IC1811';");
		db.addCommand("update TOTAL set n4=null where name='IC1948';");
		db.addCommand("update TOTAL set n4=null where name='IC1949';");
		db.addCommand("update TOTAL set n4=null where name='IC1949';");
		db.addCommand("update TOTAL set n4=null where name='IC1948';");
		db.addCommand("update TOTAL set n4=null where name='IC3274';");
		db.addCommand("update TOTAL set n4=null where name='IC3303';");
		db.addCommand("update TOTAL set n4=null where name='IC3290';");
		db.addCommand("update TOTAL set n4=null where name='NGC4373';");
		db.addCommand("update TOTAL set n4=null where name='IC3303';");
		db.addCommand("update TOTAL set n4=null where name='IC3274';");
		db.addCommand("update TOTAL set n4=null where name='IC3481';");
		db.addCommand("update TOTAL set n4=null where name='IC3483';");
		db.addCommand("update TOTAL set n4=null where name='IC3483';");
		db.addCommand("update TOTAL set n4=null where name='IC3481';");
		db.addCommand("update TOTAL set n4=null where name='IC4687';");
		db.addCommand("update TOTAL set n4=null where name='IC4689';");
		db.addCommand("update TOTAL set n4=null where name='IC4689';");
		db.addCommand("update TOTAL set n4=null where name='IC4687';");
		db.addCommand("update TOTAL set n4=null where name='IC4751';");
		db.addCommand("update TOTAL set n4=null where name='IC4753';");
		db.addCommand("update TOTAL set n4=null where name='IC4753';");
		db.addCommand("update TOTAL set n4=null where name='IC4751';");
		db.addCommand("update TOTAL set n4=null where name='IC5174';");
		db.addCommand("update TOTAL set n4=null where name='IC5175';");
		db.addCommand("update TOTAL set n4=null where name='IC5175';");
		db.addCommand("update TOTAL set n4=null where name='IC5174';");
		db.addCommand("update TOTAL set n4=null where name='NGC230';");
		db.addCommand("update TOTAL set n5=null where name='NGC232';");
		db.addCommand("update TOTAL set n4=null where name='NGC235';");
		db.addCommand("update TOTAL set n5=null where name='NGC232';");
		db.addCommand("update TOTAL set n4=null where name='NGC385';");
		db.addCommand("update TOTAL set n5=null where name='NGC379';");
		db.addCommand("update TOTAL set n4=null where name='NGC385';");
		db.addCommand("update TOTAL set n5=null where name='NGC380';");
		db.addCommand("update TOTAL set n4=null where name='NGC385';");
		db.addCommand("update TOTAL set n5=null where name='NGC382';");
		db.addCommand("update TOTAL set n4=null where name='NGC385';");
		db.addCommand("update TOTAL set n5=null where name='NGC384';");
		db.addCommand("update TOTAL set n4=null where name='NGC386';");
		db.addCommand("update TOTAL set n5=null where name='NGC379';");
		db.addCommand("update TOTAL set n4=null where name='NGC386';");
		db.addCommand("update TOTAL set n5=null where name='NGC380';");
		db.addCommand("update TOTAL set n4=null where name='NGC386';");
		db.addCommand("update TOTAL set n5=null where name='NGC382';");
		db.addCommand("update TOTAL set n4=null where name='NGC386';");
		db.addCommand("update TOTAL set n5=null where name='NGC384';");
		db.addCommand("update TOTAL set n4=null where name='NGC1228';");
		db.addCommand("update TOTAL set n5=null where name='NGC1229';");
		db.addCommand("update TOTAL set n4=null where name='NGC1230';");
		db.addCommand("update TOTAL set n5=null where name='NGC1229';");
		db.addCommand("update TOTAL set n4=null where name='NGC2426';");
		db.addCommand("update TOTAL set n5=null where name='NGC2429';");
		db.addCommand("update TOTAL set n4=null where name='NGC3187';");
		db.addCommand("update TOTAL set n5=null where name='NGC3193';");
		db.addCommand("update TOTAL set n4=null where name='NGC3627';");
		db.addCommand("update TOTAL set n5=null where name='NGC3623';");
		db.addCommand("update TOTAL set n4=null where name='NGC3628';");
		db.addCommand("update TOTAL set n5=null where name='NGC3623';");
		db.addCommand("update TOTAL set n4=null where name='NGC3745';");
		db.addCommand("update TOTAL set n5=null where name='NGC3748';");
		db.addCommand("update TOTAL set n4=null where name='NGC3745';");
		db.addCommand("update TOTAL set n5=null where name='NGC3750';");
		db.addCommand("update TOTAL set n4=null where name='NGC3745';");
		db.addCommand("update TOTAL set n5=null where name='NGC3753';");
		db.addCommand("update TOTAL set n4=null where name='NGC3745';");
		db.addCommand("update TOTAL set n5=null where name='NGC3754';");
		db.addCommand("update TOTAL set n4=null where name='NGC4039';");
		db.addCommand("update TOTAL set n5=null where name='NGC4038';");
		db.addCommand("update TOTAL set n4=null where name='NGC4568';");
		db.addCommand("update TOTAL set n5=null where name='NGC4567';");
		db.addCommand("update TOTAL set n4=null where name='NGC5375';");
		db.addCommand("update TOTAL set n5=null where name='NGC5275';");
		db.addCommand("update TOTAL set n4=null where name='NGC5396';");
		db.addCommand("update TOTAL set n5=null where name='NGC5275';");
		db.addCommand("update TOTAL set n4=null where name='NGC7320';");
		db.addCommand("update TOTAL set n5=null where name='NGC7319';");
		db.addCommand("update TOTAL set n4=null where name='NGC7547';");
		db.addCommand("update TOTAL set n5=null where name='NGC7549';");
		db.addCommand("update TOTAL set n4=null where name='NGC385';");
		db.addCommand("update TOTAL set n6=null where name='NGC388';");
		db.addCommand("update TOTAL set n4=null where name='NGC386';");
		db.addCommand("update TOTAL set n6=null where name='NGC388';");
		db.addCommand("update TOTAL set n4=null where name='NGC3018';");
		db.addCommand("update TOTAL set n7=null where name='NGC3023';");
		db.addCommand("update TOTAL set n4=null where name='NGC3187';");
		db.addCommand("update TOTAL set n7=null where name='NGC3190';");
		db.addCommand("update TOTAL set n4=null where name='NGC7320';");
		db.addCommand("update TOTAL set n8=null where name='NGC7318';");
		db.addCommand("update TOTAL set n5=null where name='NGC68';");
		db.addCommand("update TOTAL set n1=null where name='NGC67';");
		db.addCommand("update TOTAL set n5=null where name='NGC69';");
		db.addCommand("update TOTAL set n1=null where name='NGC67';");
		db.addCommand("update TOTAL set n5=null where name='NGC70';");
		db.addCommand("update TOTAL set n1=null where name='NGC67';");
		db.addCommand("update TOTAL set n5=null where name='NGC71';");
		db.addCommand("update TOTAL set n1=null where name='NGC67';");
		db.addCommand("update TOTAL set n5=null where name='NGC72';");
		db.addCommand("update TOTAL set n1=null where name='NGC67';");
		db.addCommand("update TOTAL set n5=null where name='NGC69';");
		db.addCommand("update TOTAL set n2=null where name='NGC70';");
		db.addCommand("update TOTAL set n5=null where name='NGC71';");
		db.addCommand("update TOTAL set n2=null where name='NGC70';");
		db.addCommand("update TOTAL set n5=null where name='NGC72';");
		db.addCommand("update TOTAL set n2=null where name='NGC70';");
		db.addCommand("update TOTAL set n5=null where name='NGC379';");
		db.addCommand("update TOTAL set n2=null where name='NGC375';");
		db.addCommand("update TOTAL set n5=null where name='NGC380';");
		db.addCommand("update TOTAL set n2=null where name='NGC375';");
		db.addCommand("update TOTAL set n5=null where name='NGC382';");
		db.addCommand("update TOTAL set n2=null where name='NGC375';");
		db.addCommand("update TOTAL set n5=null where name='NGC384';");
		db.addCommand("update TOTAL set n2=null where name='NGC375';");
		db.addCommand("update TOTAL set n5=null where name='NGC5613';");
		db.addCommand("update TOTAL set n2=null where name='NGC5615';");
		db.addCommand("update TOTAL set n5=null where name='NGC78';");
		db.addCommand("update TOTAL set n3=null where name='NGC786';");
		db.addCommand("update TOTAL set n5=null where name='NGC90';");
		db.addCommand("update TOTAL set n3=null where name='NGC93';");
		db.addCommand("update TOTAL set n5=null where name='NGC379';");
		db.addCommand("update TOTAL set n3=null where name='NGC383';");
		db.addCommand("update TOTAL set n5=null where name='NGC380';");
		db.addCommand("update TOTAL set n3=null where name='NGC383';");
		db.addCommand("update TOTAL set n5=null where name='NGC384';");
		db.addCommand("update TOTAL set n3=null where name='NGC383';");
		db.addCommand("update TOTAL set n5=null where name='NGC470';");
		db.addCommand("update TOTAL set n3=null where name='NGC474';");
		db.addCommand("update TOTAL set n5=null where name='NGC999';");
		db.addCommand("update TOTAL set n3=null where name='NGC1000';");
		db.addCommand("update TOTAL set n5=null where name='NGC1228';");
		db.addCommand("update TOTAL set n3=null where name='NGC1229';");
		db.addCommand("update TOTAL set n5=null where name='NGC1228';");
		db.addCommand("update TOTAL set n3=null where name='NGC1230';");
		db.addCommand("update TOTAL set n5=null where name='NGC2175';");
		db.addCommand("update TOTAL set n3=null where name='NGC2174';");
		db.addCommand("update TOTAL set n5=null where name='NGC4435';");
		db.addCommand("update TOTAL set n3=null where name='NGC4438';");
		db.addCommand("update TOTAL set n5=null where name='NGC5427';");
		db.addCommand("update TOTAL set n3=null where name='NGC5426';");
		db.addCommand("update TOTAL set n5=null where name='NGC7319';");
		db.addCommand("update TOTAL set n3=null where name='NGC7317';");
		db.addCommand("update TOTAL set n5=null where name='NGC7436';");
		db.addCommand("update TOTAL set n3=null where name='NGC7433';");
		db.addCommand("update TOTAL set n5=null where name='NGC7549';");
		db.addCommand("update TOTAL set n3=null where name='NGC7550';");
		db.addCommand("update TOTAL set n5=null where name='NGC232';");
		db.addCommand("update TOTAL set n4=null where name='NGC230';");
		db.addCommand("update TOTAL set n5=null where name='NGC232';");
		db.addCommand("update TOTAL set n4=null where name='NGC235';");
		db.addCommand("update TOTAL set n5=null where name='NGC379';");
		db.addCommand("update TOTAL set n4=null where name='NGC385';");
		db.addCommand("update TOTAL set n5=null where name='NGC379';");
		db.addCommand("update TOTAL set n4=null where name='NGC386';");
		db.addCommand("update TOTAL set n5=null where name='NGC380';");
		db.addCommand("update TOTAL set n4=null where name='NGC385';");
		db.addCommand("update TOTAL set n5=null where name='NGC380';");
		db.addCommand("update TOTAL set n4=null where name='NGC386';");
		db.addCommand("update TOTAL set n5=null where name='NGC382';");
		db.addCommand("update TOTAL set n4=null where name='NGC385';");
		db.addCommand("update TOTAL set n5=null where name='NGC382';");
		db.addCommand("update TOTAL set n4=null where name='NGC386';");
		db.addCommand("update TOTAL set n5=null where name='NGC384';");
		db.addCommand("update TOTAL set n4=null where name='NGC385';");
		db.addCommand("update TOTAL set n5=null where name='NGC384';");
		db.addCommand("update TOTAL set n4=null where name='NGC386';");
		db.addCommand("update TOTAL set n5=null where name='NGC1229';");
		db.addCommand("update TOTAL set n4=null where name='NGC1228';");
		db.addCommand("update TOTAL set n5=null where name='NGC1229';");
		db.addCommand("update TOTAL set n4=null where name='NGC1230';");
		db.addCommand("update TOTAL set n5=null where name='NGC2429';");
		db.addCommand("update TOTAL set n4=null where name='NGC2426';");
		db.addCommand("update TOTAL set n5=null where name='NGC3193';");
		db.addCommand("update TOTAL set n4=null where name='NGC3187';");
		db.addCommand("update TOTAL set n5=null where name='NGC3623';");
		db.addCommand("update TOTAL set n4=null where name='NGC3627';");
		db.addCommand("update TOTAL set n5=null where name='NGC3623';");
		db.addCommand("update TOTAL set n4=null where name='NGC3628';");
		db.addCommand("update TOTAL set n5=null where name='NGC3748';");
		db.addCommand("update TOTAL set n4=null where name='NGC3745';");
		db.addCommand("update TOTAL set n5=null where name='NGC3750';");
		db.addCommand("update TOTAL set n4=null where name='NGC3745';");
		db.addCommand("update TOTAL set n5=null where name='NGC3753';");
		db.addCommand("update TOTAL set n4=null where name='NGC3745';");
		db.addCommand("update TOTAL set n5=null where name='NGC3754';");
		db.addCommand("update TOTAL set n4=null where name='NGC3745';");
		db.addCommand("update TOTAL set n5=null where name='NGC4038';");
		db.addCommand("update TOTAL set n4=null where name='NGC4039';");
		db.addCommand("update TOTAL set n5=null where name='NGC4567';");
		db.addCommand("update TOTAL set n4=null where name='NGC4568';");
		db.addCommand("update TOTAL set n5=null where name='NGC5275';");
		db.addCommand("update TOTAL set n4=null where name='NGC5375';");
		db.addCommand("update TOTAL set n5=null where name='NGC5275';");
		db.addCommand("update TOTAL set n4=null where name='NGC5396';");
		db.addCommand("update TOTAL set n5=null where name='NGC7319';");
		db.addCommand("update TOTAL set n4=null where name='NGC7320';");
		db.addCommand("update TOTAL set n5=null where name='NGC7549';");
		db.addCommand("update TOTAL set n4=null where name='NGC7547';");
		db.addCommand("update TOTAL set n5=null where name='NGC68';");
		db.addCommand("update TOTAL set n5=null where name='NGC69';");
		db.addCommand("update TOTAL set n5=null where name='NGC68';");
		db.addCommand("update TOTAL set n5=null where name='NGC71';");
		db.addCommand("update TOTAL set n5=null where name='NGC68';");
		db.addCommand("update TOTAL set n5=null where name='NGC72';");
		db.addCommand("update TOTAL set n5=null where name='NGC69';");
		db.addCommand("update TOTAL set n5=null where name='NGC68';");
		db.addCommand("update TOTAL set n5=null where name='NGC69';");
		db.addCommand("update TOTAL set n5=null where name='NGC70';");
		db.addCommand("update TOTAL set n5=null where name='NGC69';");
		db.addCommand("update TOTAL set n5=null where name='NGC71';");
		db.addCommand("update TOTAL set n5=null where name='NGC69';");
		db.addCommand("update TOTAL set n5=null where name='NGC72';");
		db.addCommand("update TOTAL set n5=null where name='NGC70';");
		db.addCommand("update TOTAL set n5=null where name='NGC69';");
		db.addCommand("update TOTAL set n5=null where name='NGC70';");
		db.addCommand("update TOTAL set n5=null where name='NGC71';");
		db.addCommand("update TOTAL set n5=null where name='NGC70';");
		db.addCommand("update TOTAL set n5=null where name='NGC72';");
		db.addCommand("update TOTAL set n5=null where name='NGC71';");
		db.addCommand("update TOTAL set n5=null where name='NGC68';");
		db.addCommand("update TOTAL set n5=null where name='NGC71';");
		db.addCommand("update TOTAL set n5=null where name='NGC69';");
		db.addCommand("update TOTAL set n5=null where name='NGC71';");
		db.addCommand("update TOTAL set n5=null where name='NGC70';");
		db.addCommand("update TOTAL set n5=null where name='NGC71';");
		db.addCommand("update TOTAL set n5=null where name='NGC72';");
		db.addCommand("update TOTAL set n5=null where name='NGC72';");
		db.addCommand("update TOTAL set n5=null where name='NGC68';");
		db.addCommand("update TOTAL set n5=null where name='NGC72';");
		db.addCommand("update TOTAL set n5=null where name='NGC69';");
		db.addCommand("update TOTAL set n5=null where name='NGC72';");
		db.addCommand("update TOTAL set n5=null where name='NGC70';");
		db.addCommand("update TOTAL set n5=null where name='NGC72';");
		db.addCommand("update TOTAL set n5=null where name='NGC71';");
		db.addCommand("update TOTAL set n5=null where name='NGC379';");
		db.addCommand("update TOTAL set n5=null where name='NGC380';");
		db.addCommand("update TOTAL set n5=null where name='NGC379';");
		db.addCommand("update TOTAL set n5=null where name='NGC382';");
		db.addCommand("update TOTAL set n5=null where name='NGC379';");
		db.addCommand("update TOTAL set n5=null where name='NGC384';");
		db.addCommand("update TOTAL set n5=null where name='NGC380';");
		db.addCommand("update TOTAL set n5=null where name='NGC379';");
		db.addCommand("update TOTAL set n5=null where name='NGC380';");
		db.addCommand("update TOTAL set n5=null where name='NGC382';");
		db.addCommand("update TOTAL set n5=null where name='NGC380';");
		db.addCommand("update TOTAL set n5=null where name='NGC384';");
		db.addCommand("update TOTAL set n5=null where name='NGC382';");
		db.addCommand("update TOTAL set n5=null where name='NGC379';");
		db.addCommand("update TOTAL set n5=null where name='NGC382';");
		db.addCommand("update TOTAL set n5=null where name='NGC380';");
		db.addCommand("update TOTAL set n5=null where name='NGC382';");
		db.addCommand("update TOTAL set n5=null where name='NGC384';");
		db.addCommand("update TOTAL set n5=null where name='NGC383';");
		db.addCommand("update TOTAL set n5=null where name='NGC385';");
		db.addCommand("update TOTAL set n5=null where name='NGC383';");
		db.addCommand("update TOTAL set n5=null where name='NGC386';");
		db.addCommand("update TOTAL set n5=null where name='NGC384';");
		db.addCommand("update TOTAL set n5=null where name='NGC379';");
		db.addCommand("update TOTAL set n5=null where name='NGC384';");
		db.addCommand("update TOTAL set n5=null where name='NGC380';");
		db.addCommand("update TOTAL set n5=null where name='NGC384';");
		db.addCommand("update TOTAL set n5=null where name='NGC382';");
		db.addCommand("update TOTAL set n5=null where name='NGC385';");
		db.addCommand("update TOTAL set n5=null where name='NGC383';");
		db.addCommand("update TOTAL set n5=null where name='NGC385';");
		db.addCommand("update TOTAL set n5=null where name='NGC386';");
		db.addCommand("update TOTAL set n5=null where name='NGC386';");
		db.addCommand("update TOTAL set n5=null where name='NGC383';");
		db.addCommand("update TOTAL set n5=null where name='NGC386';");
		db.addCommand("update TOTAL set n5=null where name='NGC385';");
		db.addCommand("update TOTAL set n5=null where name='NGC515';");
		db.addCommand("update TOTAL set n5=null where name='NGC517';");
		db.addCommand("update TOTAL set n5=null where name='NGC517';");
		db.addCommand("update TOTAL set n5=null where name='NGC515';");
		db.addCommand("update TOTAL set n5=null where name='NGC2444';");
		db.addCommand("update TOTAL set n5=null where name='NGC2445';");
		db.addCommand("update TOTAL set n5=null where name='NGC2445';");
		db.addCommand("update TOTAL set n5=null where name='NGC2444';");
		db.addCommand("update TOTAL set n5=null where name='NGC3395';");
		db.addCommand("update TOTAL set n5=null where name='NGC3396';");
		db.addCommand("update TOTAL set n5=null where name='NGC3396';");
		db.addCommand("update TOTAL set n5=null where name='NGC3395';");
		db.addCommand("update TOTAL set n5=null where name='NGC3748';");
		db.addCommand("update TOTAL set n5=null where name='NGC3750';");
		db.addCommand("update TOTAL set n5=null where name='NGC3748';");
		db.addCommand("update TOTAL set n5=null where name='NGC3753';");
		db.addCommand("update TOTAL set n5=null where name='NGC3748';");
		db.addCommand("update TOTAL set n5=null where name='NGC3754';");
		db.addCommand("update TOTAL set n5=null where name='NGC3750';");
		db.addCommand("update TOTAL set n5=null where name='NGC3748';");
		db.addCommand("update TOTAL set n5=null where name='NGC3750';");
		db.addCommand("update TOTAL set n5=null where name='NGC3754';");
		db.addCommand("update TOTAL set n5=null where name='NGC3753';");
		db.addCommand("update TOTAL set n5=null where name='NGC3748';");
		db.addCommand("update TOTAL set n5=null where name='NGC3754';");
		db.addCommand("update TOTAL set n5=null where name='NGC3748';");
		db.addCommand("update TOTAL set n5=null where name='NGC3754';");
		db.addCommand("update TOTAL set n5=null where name='NGC3750';");
		db.addCommand("update TOTAL set n5=null where name='NGC3994';");
		db.addCommand("update TOTAL set n5=null where name='NGC3995';");
		db.addCommand("update TOTAL set n5=null where name='NGC3995';");
		db.addCommand("update TOTAL set n5=null where name='NGC3994';");
		db.addCommand("update TOTAL set n5=null where name='NGC4485';");
		db.addCommand("update TOTAL set n5=null where name='NGC4490';");
		db.addCommand("update TOTAL set n5=null where name='NGC4490';");
		db.addCommand("update TOTAL set n5=null where name='NGC4485';");
		db.addCommand("update TOTAL set n5=null where name='NGC5221';");
		db.addCommand("update TOTAL set n5=null where name='NGC5222';");
		db.addCommand("update TOTAL set n5=null where name='NGC5222';");
		db.addCommand("update TOTAL set n5=null where name='NGC5221';");
		db.addCommand("update TOTAL set n5=null where name='NGC379';");
		db.addCommand("update TOTAL set n6=null where name='NGC388';");
		db.addCommand("update TOTAL set n5=null where name='NGC380';");
		db.addCommand("update TOTAL set n6=null where name='NGC388';");
		db.addCommand("update TOTAL set n5=null where name='NGC382';");
		db.addCommand("update TOTAL set n6=null where name='NGC388';");
		db.addCommand("update TOTAL set n5=null where name='NGC383';");
		db.addCommand("update TOTAL set n6=null where name='NGC379';");
		db.addCommand("update TOTAL set n5=null where name='NGC383';");
		db.addCommand("update TOTAL set n6=null where name='NGC380';");
		db.addCommand("update TOTAL set n5=null where name='NGC383';");
		db.addCommand("update TOTAL set n6=null where name='NGC384';");
		db.addCommand("update TOTAL set n5=null where name='NGC384';");
		db.addCommand("update TOTAL set n6=null where name='NGC388';");
		db.addCommand("update TOTAL set n5=null where name='NGC385';");
		db.addCommand("update TOTAL set n6=null where name='NGC379';");
		db.addCommand("update TOTAL set n5=null where name='NGC385';");
		db.addCommand("update TOTAL set n6=null where name='NGC380';");
		db.addCommand("update TOTAL set n5=null where name='NGC385';");
		db.addCommand("update TOTAL set n6=null where name='NGC382';");
		db.addCommand("update TOTAL set n5=null where name='NGC385';");
		db.addCommand("update TOTAL set n6=null where name='NGC384';");
		db.addCommand("update TOTAL set n5=null where name='NGC386';");
		db.addCommand("update TOTAL set n6=null where name='NGC379';");
		db.addCommand("update TOTAL set n5=null where name='NGC386';");
		db.addCommand("update TOTAL set n6=null where name='NGC380';");
		db.addCommand("update TOTAL set n5=null where name='NGC386';");
		db.addCommand("update TOTAL set n6=null where name='NGC382';");
		db.addCommand("update TOTAL set n5=null where name='NGC386';");
		db.addCommand("update TOTAL set n6=null where name='NGC384';");
		db.addCommand("update TOTAL set n5=null where name='NGC508';");
		db.addCommand("update TOTAL set n6=null where name='NGC507';");
		db.addCommand("update TOTAL set n5=null where name='NGC518';");
		db.addCommand("update TOTAL set n6=null where name='NGC522';");
		db.addCommand("update TOTAL set n5=null where name='NGC3628';");
		db.addCommand("update TOTAL set n6=null where name='NGC3623';");
		db.addCommand("update TOTAL set n5=null where name='NGC3748';");
		db.addCommand("update TOTAL set n6=null where name='NGC3746';");
		db.addCommand("update TOTAL set n5=null where name='NGC3750';");
		db.addCommand("update TOTAL set n6=null where name='NGC3746';");
		db.addCommand("update TOTAL set n5=null where name='NGC3753';");
		db.addCommand("update TOTAL set n6=null where name='NGC3746';");
		db.addCommand("update TOTAL set n5=null where name='NGC3754';");
		db.addCommand("update TOTAL set n6=null where name='NGC3746';");
		db.addCommand("update TOTAL set n5=null where name='NGC5474';");
		db.addCommand("update TOTAL set n6=null where name='NGC5457';");
		db.addCommand("update TOTAL set n5=null where name='NGC5613';");
		db.addCommand("update TOTAL set n6=null where name='NGC5614';");
		db.addCommand("update TOTAL set n5=null where name='NGC5994';");
		db.addCommand("update TOTAL set n6=null where name='NGC5996';");
		db.addCommand("update TOTAL set n5=null where name='NGC7174';");
		db.addCommand("update TOTAL set n6=null where name='NGC7173';");
		db.addCommand("update TOTAL set n5=null where name='NGC383';");
		db.addCommand("update TOTAL set n7=null where name='NGC388';");
		db.addCommand("update TOTAL set n5=null where name='NGC385';");
		db.addCommand("update TOTAL set n7=null where name='NGC388';");
		db.addCommand("update TOTAL set n5=null where name='NGC386';");
		db.addCommand("update TOTAL set n7=null where name='NGC388';");
		db.addCommand("update TOTAL set n5=null where name='NGC2536';");
		db.addCommand("update TOTAL set n7=null where name='NGC2535';");
		db.addCommand("update TOTAL set n5=null where name='NGC3193';");
		db.addCommand("update TOTAL set n7=null where name='NGC3190';");
		db.addCommand("update TOTAL set n5=null where name='NGC3193';");
		db.addCommand("update TOTAL set n8=null where name='NGC3187';");
		db.addCommand("update TOTAL set n5=null where name='NGC7319';");
		db.addCommand("update TOTAL set n8=null where name='NGC7318';");
		db.addCommand("update TOTAL set n5=null where name='NGC7682';");
		db.addCommand("update TOTAL set n8=null where name='NGC7679';");
		db.addCommand("update TOTAL set n5=null where name='NGC3628';");
		db.addCommand("update TOTAL set n9=null where name='NGC3627';");
		db.addCommand("update TOTAL set n6=null where name='NGC2175';");
		db.addCommand("update TOTAL set n1=null where name='NGC2174';");
		db.addCommand("update TOTAL set n6=null where name='NGC6611';");
		db.addCommand("update TOTAL set n1=null where name='IC4703';");
		db.addCommand("update TOTAL set n6=null where name='NGC68';");
		db.addCommand("update TOTAL set n2=null where name='NGC67';");
		db.addCommand("update TOTAL set n6=null where name='NGC69';");
		db.addCommand("update TOTAL set n2=null where name='NGC67';");
		db.addCommand("update TOTAL set n6=null where name='NGC70';");
		db.addCommand("update TOTAL set n2=null where name='NGC67';");
		db.addCommand("update TOTAL set n6=null where name='NGC71';");
		db.addCommand("update TOTAL set n2=null where name='NGC67';");
		db.addCommand("update TOTAL set n6=null where name='NGC72';");
		db.addCommand("update TOTAL set n2=null where name='NGC67';");
		db.addCommand("update TOTAL set n6=null where name='NGC388';");
		db.addCommand("update TOTAL set n2=null where name='NGC375';");
		db.addCommand("update TOTAL set n6=null where name='NGC4625';");
		db.addCommand("update TOTAL set n2=null where name='NGC4618';");
		db.addCommand("update TOTAL set n6=null where name='NGC7320';");
		db.addCommand("update TOTAL set n2=null where name='NGC7317';");
		db.addCommand("update TOTAL set n6=null where name='NGC388';");
		db.addCommand("update TOTAL set n3=null where name='NGC383';");
		db.addCommand("update TOTAL set n6=null where name='NGC3190';");
		db.addCommand("update TOTAL set n3=null where name='NGC3187';");
		db.addCommand("update TOTAL set n6=null where name='NGC3190';");
		db.addCommand("update TOTAL set n3=null where name='NGC3193';");
		db.addCommand("update TOTAL set n6=null where name='NGC7320';");
		db.addCommand("update TOTAL set n3=null where name='NGC7319';");
		db.addCommand("update TOTAL set n6=null where name='NGC388';");
		db.addCommand("update TOTAL set n4=null where name='NGC385';");
		db.addCommand("update TOTAL set n6=null where name='NGC388';");
		db.addCommand("update TOTAL set n4=null where name='NGC386';");
		db.addCommand("update TOTAL set n6=null where name='NGC379';");
		db.addCommand("update TOTAL set n5=null where name='NGC383';");
		db.addCommand("update TOTAL set n6=null where name='NGC379';");
		db.addCommand("update TOTAL set n5=null where name='NGC385';");
		db.addCommand("update TOTAL set n6=null where name='NGC379';");
		db.addCommand("update TOTAL set n5=null where name='NGC386';");
		db.addCommand("update TOTAL set n6=null where name='NGC380';");
		db.addCommand("update TOTAL set n5=null where name='NGC383';");
		db.addCommand("update TOTAL set n6=null where name='NGC380';");
		db.addCommand("update TOTAL set n5=null where name='NGC385';");
		db.addCommand("update TOTAL set n6=null where name='NGC380';");
		db.addCommand("update TOTAL set n5=null where name='NGC386';");
		db.addCommand("update TOTAL set n6=null where name='NGC382';");
		db.addCommand("update TOTAL set n5=null where name='NGC385';");
		db.addCommand("update TOTAL set n6=null where name='NGC382';");
		db.addCommand("update TOTAL set n5=null where name='NGC386';");
		db.addCommand("update TOTAL set n6=null where name='NGC384';");
		db.addCommand("update TOTAL set n5=null where name='NGC383';");
		db.addCommand("update TOTAL set n6=null where name='NGC384';");
		db.addCommand("update TOTAL set n5=null where name='NGC385';");
		db.addCommand("update TOTAL set n6=null where name='NGC384';");
		db.addCommand("update TOTAL set n5=null where name='NGC386';");
		db.addCommand("update TOTAL set n6=null where name='NGC388';");
		db.addCommand("update TOTAL set n5=null where name='NGC379';");
		db.addCommand("update TOTAL set n6=null where name='NGC388';");
		db.addCommand("update TOTAL set n5=null where name='NGC380';");
		db.addCommand("update TOTAL set n6=null where name='NGC388';");
		db.addCommand("update TOTAL set n5=null where name='NGC382';");
		db.addCommand("update TOTAL set n6=null where name='NGC388';");
		db.addCommand("update TOTAL set n5=null where name='NGC384';");
		db.addCommand("update TOTAL set n6=null where name='NGC507';");
		db.addCommand("update TOTAL set n5=null where name='NGC508';");
		db.addCommand("update TOTAL set n6=null where name='NGC522';");
		db.addCommand("update TOTAL set n5=null where name='NGC518';");
		db.addCommand("update TOTAL set n6=null where name='NGC3623';");
		db.addCommand("update TOTAL set n5=null where name='NGC3628';");
		db.addCommand("update TOTAL set n6=null where name='NGC3746';");
		db.addCommand("update TOTAL set n5=null where name='NGC3748';");
		db.addCommand("update TOTAL set n6=null where name='NGC3746';");
		db.addCommand("update TOTAL set n5=null where name='NGC3750';");
		db.addCommand("update TOTAL set n6=null where name='NGC3746';");
		db.addCommand("update TOTAL set n5=null where name='NGC3753';");
		db.addCommand("update TOTAL set n6=null where name='NGC3746';");
		db.addCommand("update TOTAL set n5=null where name='NGC3754';");
		db.addCommand("update TOTAL set n6=null where name='NGC5457';");
		db.addCommand("update TOTAL set n5=null where name='NGC5474';");
		db.addCommand("update TOTAL set n6=null where name='NGC5614';");
		db.addCommand("update TOTAL set n5=null where name='NGC5613';");
		db.addCommand("update TOTAL set n6=null where name='NGC5996';");
		db.addCommand("update TOTAL set n5=null where name='NGC5994';");
		db.addCommand("update TOTAL set n6=null where name='NGC7173';");
		db.addCommand("update TOTAL set n5=null where name='NGC7174';");
		db.addCommand("update TOTAL set n6=null where name='NGC68';");
		db.addCommand("update TOTAL set n6=null where name='NGC69';");
		db.addCommand("update TOTAL set n6=null where name='NGC68';");
		db.addCommand("update TOTAL set n6=null where name='NGC71';");
		db.addCommand("update TOTAL set n6=null where name='NGC68';");
		db.addCommand("update TOTAL set n6=null where name='NGC72';");
		db.addCommand("update TOTAL set n6=null where name='NGC69';");
		db.addCommand("update TOTAL set n6=null where name='NGC68';");
		db.addCommand("update TOTAL set n6=null where name='NGC69';");
		db.addCommand("update TOTAL set n6=null where name='NGC70';");
		db.addCommand("update TOTAL set n6=null where name='NGC69';");
		db.addCommand("update TOTAL set n6=null where name='NGC71';");
		db.addCommand("update TOTAL set n6=null where name='NGC69';");
		db.addCommand("update TOTAL set n6=null where name='NGC72';");
		db.addCommand("update TOTAL set n6=null where name='NGC70';");
		db.addCommand("update TOTAL set n6=null where name='NGC69';");
		db.addCommand("update TOTAL set n6=null where name='NGC70';");
		db.addCommand("update TOTAL set n6=null where name='NGC71';");
		db.addCommand("update TOTAL set n6=null where name='NGC70';");
		db.addCommand("update TOTAL set n6=null where name='NGC72';");
		db.addCommand("update TOTAL set n6=null where name='NGC71';");
		db.addCommand("update TOTAL set n6=null where name='NGC68';");
		db.addCommand("update TOTAL set n6=null where name='NGC71';");
		db.addCommand("update TOTAL set n6=null where name='NGC69';");
		db.addCommand("update TOTAL set n6=null where name='NGC71';");
		db.addCommand("update TOTAL set n6=null where name='NGC70';");
		db.addCommand("update TOTAL set n6=null where name='NGC71';");
		db.addCommand("update TOTAL set n6=null where name='NGC72';");
		db.addCommand("update TOTAL set n6=null where name='NGC72';");
		db.addCommand("update TOTAL set n6=null where name='NGC68';");
		db.addCommand("update TOTAL set n6=null where name='NGC72';");
		db.addCommand("update TOTAL set n6=null where name='NGC69';");
		db.addCommand("update TOTAL set n6=null where name='NGC72';");
		db.addCommand("update TOTAL set n6=null where name='NGC70';");
		db.addCommand("update TOTAL set n6=null where name='NGC72';");
		db.addCommand("update TOTAL set n6=null where name='NGC71';");
		db.addCommand("update TOTAL set n6=null where name='NGC379';");
		db.addCommand("update TOTAL set n6=null where name='NGC380';");
		db.addCommand("update TOTAL set n6=null where name='NGC379';");
		db.addCommand("update TOTAL set n6=null where name='NGC382';");
		db.addCommand("update TOTAL set n6=null where name='NGC379';");
		db.addCommand("update TOTAL set n6=null where name='NGC384';");
		db.addCommand("update TOTAL set n6=null where name='NGC380';");
		db.addCommand("update TOTAL set n6=null where name='NGC379';");
		db.addCommand("update TOTAL set n6=null where name='NGC380';");
		db.addCommand("update TOTAL set n6=null where name='NGC382';");
		db.addCommand("update TOTAL set n6=null where name='NGC380';");
		db.addCommand("update TOTAL set n6=null where name='NGC384';");
		db.addCommand("update TOTAL set n6=null where name='NGC382';");
		db.addCommand("update TOTAL set n6=null where name='NGC379';");
		db.addCommand("update TOTAL set n6=null where name='NGC382';");
		db.addCommand("update TOTAL set n6=null where name='NGC380';");
		db.addCommand("update TOTAL set n6=null where name='NGC382';");
		db.addCommand("update TOTAL set n6=null where name='NGC384';");
		db.addCommand("update TOTAL set n6=null where name='NGC384';");
		db.addCommand("update TOTAL set n6=null where name='NGC379';");
		db.addCommand("update TOTAL set n6=null where name='NGC384';");
		db.addCommand("update TOTAL set n6=null where name='NGC380';");
		db.addCommand("update TOTAL set n6=null where name='NGC384';");
		db.addCommand("update TOTAL set n6=null where name='NGC382';");
		db.addCommand("update TOTAL set n6=null where name='NGC2798';");
		db.addCommand("update TOTAL set n6=null where name='NGC2799';");
		db.addCommand("update TOTAL set n6=null where name='NGC2799';");
		db.addCommand("update TOTAL set n6=null where name='NGC2798';");
		db.addCommand("update TOTAL set n6=null where name='NGC3786';");
		db.addCommand("update TOTAL set n6=null where name='NGC3788';");
		db.addCommand("update TOTAL set n6=null where name='NGC3788';");
		db.addCommand("update TOTAL set n6=null where name='NGC3786';");
		db.addCommand("update TOTAL set n6=null where name='NGC379';");
		db.addCommand("update TOTAL set n7=null where name='NGC388';");
		db.addCommand("update TOTAL set n6=null where name='NGC380';");
		db.addCommand("update TOTAL set n7=null where name='NGC388';");
		db.addCommand("update TOTAL set n6=null where name='NGC382';");
		db.addCommand("update TOTAL set n7=null where name='NGC388';");
		db.addCommand("update TOTAL set n6=null where name='NGC384';");
		db.addCommand("update TOTAL set n7=null where name='NGC388';");
		db.addCommand("update TOTAL set n6=null where name='NGC3190';");
		db.addCommand("update TOTAL set n7=null where name='NGC3187';");
		db.addCommand("update TOTAL set n6=null where name='NGC5395';");
		db.addCommand("update TOTAL set n7=null where name='NGC5394';");
		db.addCommand("update TOTAL set n6=null where name='NGC5653';");
		db.addCommand("update TOTAL set n7=null where name='NGC5648';");
		db.addCommand("update TOTAL set n6=null where name='NGC5653';");
		db.addCommand("update TOTAL set n7=null where name='NGC5649';");
		db.addCommand("update TOTAL set n6=null where name='NGC7320';");
		db.addCommand("update TOTAL set n7=null where name='NGC7318';");
		db.addCommand("update TOTAL set n6=null where name='NGC3215';");
		db.addCommand("update TOTAL set n8=null where name='NGC3212';");
		db.addCommand("update TOTAL set n6=null where name='NGC3623';");
		db.addCommand("update TOTAL set n9=null where name='NGC3627';");
		db.addCommand("update TOTAL set n6=null where name='NGC7715';");
		db.addCommand("update TOTAL set n9=null where name='NGC7714';");
		db.addCommand("update TOTAL set n7=null where name='NGC7318';");
		db.addCommand("update TOTAL set n2=null where name='NGC7317';");
		db.addCommand("update TOTAL set n7=null where name='NGC3187';");
		db.addCommand("update TOTAL set n3=null where name='NGC3193';");
		db.addCommand("update TOTAL set n7=null where name='NGC3690';");
		db.addCommand("update TOTAL set n3=null where name='IC694';");
		db.addCommand("update TOTAL set n7=null where name='NGC7318';");
		db.addCommand("update TOTAL set n3=null where name='NGC7319';");
		db.addCommand("update TOTAL set n7=null where name='NGC3023';");
		db.addCommand("update TOTAL set n4=null where name='NGC3018';");
		db.addCommand("update TOTAL set n7=null where name='NGC3190';");
		db.addCommand("update TOTAL set n4=null where name='NGC3187';");
		db.addCommand("update TOTAL set n7=null where name='NGC388';");
		db.addCommand("update TOTAL set n5=null where name='NGC383';");
		db.addCommand("update TOTAL set n7=null where name='NGC388';");
		db.addCommand("update TOTAL set n5=null where name='NGC385';");
		db.addCommand("update TOTAL set n7=null where name='NGC388';");
		db.addCommand("update TOTAL set n5=null where name='NGC386';");
		db.addCommand("update TOTAL set n7=null where name='NGC2535';");
		db.addCommand("update TOTAL set n5=null where name='NGC2536';");
		db.addCommand("update TOTAL set n7=null where name='NGC3190';");
		db.addCommand("update TOTAL set n5=null where name='NGC3193';");
		db.addCommand("update TOTAL set n7=null where name='NGC388';");
		db.addCommand("update TOTAL set n6=null where name='NGC379';");
		db.addCommand("update TOTAL set n7=null where name='NGC388';");
		db.addCommand("update TOTAL set n6=null where name='NGC380';");
		db.addCommand("update TOTAL set n7=null where name='NGC388';");
		db.addCommand("update TOTAL set n6=null where name='NGC382';");
		db.addCommand("update TOTAL set n7=null where name='NGC388';");
		db.addCommand("update TOTAL set n6=null where name='NGC384';");
		db.addCommand("update TOTAL set n7=null where name='NGC3187';");
		db.addCommand("update TOTAL set n6=null where name='NGC3190';");
		db.addCommand("update TOTAL set n7=null where name='NGC5394';");
		db.addCommand("update TOTAL set n6=null where name='NGC5395';");
		db.addCommand("update TOTAL set n7=null where name='NGC5648';");
		db.addCommand("update TOTAL set n6=null where name='NGC5653';");
		db.addCommand("update TOTAL set n7=null where name='NGC5649';");
		db.addCommand("update TOTAL set n6=null where name='NGC5653';");
		db.addCommand("update TOTAL set n7=null where name='NGC7318';");
		db.addCommand("update TOTAL set n6=null where name='NGC7320';");
		db.addCommand("update TOTAL set n7=null where name='NGC3190';");
		db.addCommand("update TOTAL set n8=null where name='NGC3187';");
		db.addCommand("update TOTAL set n7=null where name='NGC4649';");
		db.addCommand("update TOTAL set n8=null where name='NGC4647';");
		db.addCommand("update TOTAL set n7=null where name='NGC7752';");
		db.addCommand("update TOTAL set n8=null where name='NGC7753';");
		db.addCommand("update TOTAL set n7=null where name='NGC5258';");
		db.addCommand("update TOTAL set n9=null where name='NGC5257';");
		db.addCommand("update TOTAL set n8=null where name='NGC7318';");
		db.addCommand("update TOTAL set n3=null where name='NGC7317';");
		db.addCommand("update TOTAL set n8=null where name='NGC7752';");
		db.addCommand("update TOTAL set n3=null where name='NGC7753';");
		db.addCommand("update TOTAL set n8=null where name='NGC7318';");
		db.addCommand("update TOTAL set n4=null where name='NGC7320';");
		db.addCommand("update TOTAL set n8=null where name='NGC3187';");
		db.addCommand("update TOTAL set n5=null where name='NGC3193';");
		db.addCommand("update TOTAL set n8=null where name='NGC7318';");
		db.addCommand("update TOTAL set n5=null where name='NGC7319';");
		db.addCommand("update TOTAL set n8=null where name='NGC7679';");
		db.addCommand("update TOTAL set n5=null where name='NGC7682';");
		db.addCommand("update TOTAL set n8=null where name='NGC3212';");
		db.addCommand("update TOTAL set n6=null where name='NGC3215';");
		db.addCommand("update TOTAL set n8=null where name='NGC3187';");
		db.addCommand("update TOTAL set n7=null where name='NGC3190';");
		db.addCommand("update TOTAL set n8=null where name='NGC4647';");
		db.addCommand("update TOTAL set n7=null where name='NGC4649';");
		db.addCommand("update TOTAL set n8=null where name='NGC7753';");
		db.addCommand("update TOTAL set n7=null where name='NGC7752';");
		db.addCommand("update TOTAL set n8=null where name='NGC7673';");
		db.addCommand("update TOTAL set n8=null where name='NGC7677';");
		db.addCommand("update TOTAL set n8=null where name='NGC7677';");
		db.addCommand("update TOTAL set n8=null where name='NGC7673';");
		db.addCommand("update TOTAL set n9=null where name='NGC3627';");
		db.addCommand("update TOTAL set n5=null where name='NGC3628';");
		db.addCommand("update TOTAL set n9=null where name='NGC3627';");
		db.addCommand("update TOTAL set n6=null where name='NGC3623';");
		db.addCommand("update TOTAL set n9=null where name='NGC7714';");
		db.addCommand("update TOTAL set n6=null where name='NGC7715';");
		db.addCommand("update TOTAL set n9=null where name='NGC5257';");
		db.addCommand("update TOTAL set n7=null where name='NGC5258';");
		
		db.addCommand("update TOTAL set n1=null where name='NGC614';");
		db.addCommand("update TOTAL set n1=null where name='NGC618';");
		db.addCommand("update TOTAL set n1=null where name='NGC618';");
		db.addCommand("update TOTAL set n1=null where name='NGC614';");
		db.addCommand("update TOTAL set n1=null where name='NGC618';");
		db.addCommand("update TOTAL set n1=null where name='NGC627';");
		db.addCommand("update TOTAL set n1=null where name='NGC627';");
		db.addCommand("update TOTAL set n1=null where name='NGC618';");
		db.addCommand("update TOTAL set n1=null where name='NGC647';");
		db.addCommand("update TOTAL set n1=null where name='NGC649';");
		db.addCommand("update TOTAL set n1=null where name='NGC649';");
		db.addCommand("update TOTAL set n1=null where name='NGC647';");
		db.addCommand("update TOTAL set n1=null where name='NGC1290';");
		db.addCommand("update TOTAL set n1=null where name='NGC1295';");
		db.addCommand("update TOTAL set n1=null where name='NGC1295';");
		db.addCommand("update TOTAL set n1=null where name='NGC1290';");
		db.addCommand("update TOTAL set n1=null where name='NGC1740';");
		db.addCommand("update TOTAL set n1=null where name='NGC1741';");
		db.addCommand("update TOTAL set n1=null where name='NGC1741';");
		db.addCommand("update TOTAL set n1=null where name='NGC1740';");
		db.addCommand("update TOTAL set n1=null where name='NGC4399';");
		db.addCommand("update TOTAL set n1=null where name='NGC4401';");
		db.addCommand("update TOTAL set n1=null where name='NGC4401';");
		db.addCommand("update TOTAL set n1=null where name='NGC4399';");
		db.addCommand("update TOTAL set n1=null where name='IC1802';");
		db.addCommand("update TOTAL set n1=null where name='IC1803';");
		db.addCommand("update TOTAL set n1=null where name='IC1803';");
		db.addCommand("update TOTAL set n1=null where name='IC1802';");
		db.addCommand("update TOTAL set n1=null where name='NGC2605';");
		db.addCommand("update TOTAL set n2=null where name='NGC2602';");
		db.addCommand("update TOTAL set n1=null where name='NGC2885';");
		db.addCommand("update TOTAL set n2=null where name='NGC2896';");
		db.addCommand("update TOTAL set n1=null where name='NGC4657';");
		db.addCommand("update TOTAL set n3=null where name='NGC4656';");
		db.addCommand("update TOTAL set n1=null where name='NGC6111';");
		db.addCommand("update TOTAL set n3=null where name='NGC2256';");
		db.addCommand("update TOTAL set n1=null where name='NGC6965';");
		db.addCommand("update TOTAL set n3=null where name='NGC6967';");
		db.addCommand("update TOTAL set n2=null where name='NGC2602';");
		db.addCommand("update TOTAL set n1=null where name='NGC2605';");
		db.addCommand("update TOTAL set n2=null where name='NGC2896';");
		db.addCommand("update TOTAL set n1=null where name='NGC2885';");
		db.addCommand("update TOTAL set n2=null where name='NGC614';");
		db.addCommand("update TOTAL set n2=null where name='NGC618';");
		db.addCommand("update TOTAL set n2=null where name='NGC618';");
		db.addCommand("update TOTAL set n2=null where name='NGC614';");
		db.addCommand("update TOTAL set n2=null where name='NGC618';");
		db.addCommand("update TOTAL set n2=null where name='NGC627';");
		db.addCommand("update TOTAL set n2=null where name='NGC627';");
		db.addCommand("update TOTAL set n2=null where name='NGC618';");
		db.addCommand("update TOTAL set n2=null where name='NGC2458';");
		db.addCommand("update TOTAL set n2=null where name='NGC2462';");
		db.addCommand("update TOTAL set n2=null where name='NGC2462';");
		db.addCommand("update TOTAL set n2=null where name='NGC2458';");
		db.addCommand("update TOTAL set n2=null where name='NGC4399';");
		db.addCommand("update TOTAL set n2=null where name='NGC4401';");
		db.addCommand("update TOTAL set n2=null where name='NGC4401';");
		db.addCommand("update TOTAL set n2=null where name='NGC4399';");
		db.addCommand("update TOTAL set n2=null where name='IC3108';");
		db.addCommand("update TOTAL set n2=null where name='IC3109';");
		db.addCommand("update TOTAL set n2=null where name='IC3109';");
		db.addCommand("update TOTAL set n2=null where name='IC3108';");
		db.addCommand("update TOTAL set n2=null where name='NGC3889';");
		db.addCommand("update TOTAL set n3=null where name='NGC389';");
		db.addCommand("update TOTAL set n2=null where name='NGC4763';");
		db.addCommand("update TOTAL set n3=null where name='NGC4963';");
		db.addCommand("update TOTAL set n2=null where name='NGC6135';");
		db.addCommand("update TOTAL set n3=null where name='NGC2258';");
		db.addCommand("update TOTAL set n2=null where name='NGC6961';");
		db.addCommand("update TOTAL set n3=null where name='NGC6983';");
		db.addCommand("update TOTAL set n2=null where name='NGC1000';");
		db.addCommand("update TOTAL set n4=null where name='NGC999';");
		db.addCommand("update TOTAL set n2=null where name='NGC3823';");
		db.addCommand("update TOTAL set n5=null where name='NGC3819';");
		db.addCommand("update TOTAL set n2=null where name='NGC4139';");
		db.addCommand("update TOTAL set n5=null where name='NGC4073';");
		db.addCommand("update TOTAL set n3=null where name='NGC2256';");
		db.addCommand("update TOTAL set n1=null where name='NGC6111';");
		db.addCommand("update TOTAL set n3=null where name='NGC4656';");
		db.addCommand("update TOTAL set n1=null where name='NGC4657';");
		db.addCommand("update TOTAL set n3=null where name='NGC6967';");
		db.addCommand("update TOTAL set n1=null where name='NGC6965';");
		db.addCommand("update TOTAL set n3=null where name='NGC389';");
		db.addCommand("update TOTAL set n2=null where name='NGC3889';");
		db.addCommand("update TOTAL set n3=null where name='NGC2258';");
		db.addCommand("update TOTAL set n2=null where name='NGC6135';");
		db.addCommand("update TOTAL set n3=null where name='NGC4963';");
		db.addCommand("update TOTAL set n2=null where name='NGC4763';");
		db.addCommand("update TOTAL set n3=null where name='NGC6983';");
		db.addCommand("update TOTAL set n2=null where name='NGC6961';");
		db.addCommand("update TOTAL set n3=null where name='NGC4892';");
		db.addCommand("update TOTAL set n3=null where name='NGC4992';");
		db.addCommand("update TOTAL set n3=null where name='NGC4992';");
		db.addCommand("update TOTAL set n3=null where name='NGC4892';");
		db.addCommand("update TOTAL set n3=null where name='NGC5044';");
		db.addCommand("update TOTAL set n3=null where name='NGC5054';");
		db.addCommand("update TOTAL set n3=null where name='NGC5054';");
		db.addCommand("update TOTAL set n3=null where name='NGC5044';");
		db.addCommand("update TOTAL set n3=null where name='IC2411';");
		db.addCommand("update TOTAL set n3=null where name='IC2414';");
		db.addCommand("update TOTAL set n3=null where name='IC2414';");
		db.addCommand("update TOTAL set n3=null where name='IC2411';");
		db.addCommand("update TOTAL set n3=null where name='NGC3732';");
		db.addCommand("update TOTAL set n5=null where name='NGC3731';");
		db.addCommand("update TOTAL set n3=null where name='NGC3791';");
		db.addCommand("update TOTAL set n8=null where name='NGC3788';");
		db.addCommand("update TOTAL set n4=null where name='NGC999';");
		db.addCommand("update TOTAL set n2=null where name='NGC1000';");
		db.addCommand("update TOTAL set n4=null where name='NGC444';");
		db.addCommand("update TOTAL set n4=null where name='NGC447';");
		db.addCommand("update TOTAL set n4=null where name='NGC447';");
		db.addCommand("update TOTAL set n4=null where name='NGC444';");
		db.addCommand("update TOTAL set n4=null where name='NGC614';");
		db.addCommand("update TOTAL set n4=null where name='NGC618';");
		db.addCommand("update TOTAL set n4=null where name='NGC618';");
		db.addCommand("update TOTAL set n4=null where name='NGC614';");
		db.addCommand("update TOTAL set n4=null where name='NGC618';");
		db.addCommand("update TOTAL set n4=null where name='NGC627';");
		db.addCommand("update TOTAL set n4=null where name='NGC627';");
		db.addCommand("update TOTAL set n4=null where name='NGC618';");
		db.addCommand("update TOTAL set n4=null where name='NGC2602';");
		db.addCommand("update TOTAL set n4=null where name='NGC2605';");
		db.addCommand("update TOTAL set n4=null where name='NGC2605';");
		db.addCommand("update TOTAL set n4=null where name='NGC2602';");
		db.addCommand("update TOTAL set n4=null where name='NGC5717';");
		db.addCommand("update TOTAL set n4=null where name='NGC5718';");
		db.addCommand("update TOTAL set n4=null where name='NGC5718';");
		db.addCommand("update TOTAL set n4=null where name='NGC5717';");
		db.addCommand("update TOTAL set n4=null where name='NGC7372';");
		db.addCommand("update TOTAL set n4=null where name='NGC7377';");
		db.addCommand("update TOTAL set n4=null where name='NGC7377';");
		db.addCommand("update TOTAL set n4=null where name='NGC7372';");
		db.addCommand("update TOTAL set n4=null where name='NGC3171';");
		db.addCommand("update TOTAL set n5=null where name='NGC3161';");
		db.addCommand("update TOTAL set n4=null where name='NGC3802';");
		db.addCommand("update TOTAL set n5=null where name='NGC3794';");
		db.addCommand("update TOTAL set n4=null where name='NGC3802';");
		db.addCommand("update TOTAL set n5=null where name='NGC3804';");
		db.addCommand("update TOTAL set n4=null where name='NGC4023';");
		db.addCommand("update TOTAL set n5=null where name='NGC4020';");
		db.addCommand("update TOTAL set n5=null where name='NGC3819';");
		db.addCommand("update TOTAL set n2=null where name='NGC3823';");
		db.addCommand("update TOTAL set n5=null where name='NGC4073';");
		db.addCommand("update TOTAL set n2=null where name='NGC4139';");
		db.addCommand("update TOTAL set n5=null where name='NGC3731';");
		db.addCommand("update TOTAL set n3=null where name='NGC3732';");
		db.addCommand("update TOTAL set n5=null where name='NGC3161';");
		db.addCommand("update TOTAL set n4=null where name='NGC3171';");
		db.addCommand("update TOTAL set n5=null where name='NGC3794';");
		db.addCommand("update TOTAL set n4=null where name='NGC3802';");
		db.addCommand("update TOTAL set n5=null where name='NGC3804';");
		db.addCommand("update TOTAL set n4=null where name='NGC3802';");
		db.addCommand("update TOTAL set n5=null where name='NGC4020';");
		db.addCommand("update TOTAL set n4=null where name='NGC4023';");
		db.addCommand("update TOTAL set n6=null where name='NGC3917';");
		db.addCommand("update TOTAL set n6=null where name='NGC3931';");
		db.addCommand("update TOTAL set n6=null where name='NGC3931';");
		db.addCommand("update TOTAL set n6=null where name='NGC3917';");
		db.addCommand("update TOTAL set n8=null where name='NGC3788';");
		db.addCommand("update TOTAL set n3=null where name='NGC3791';");
		
		db.addCommand("commit;");
		db.end(-1);
		
	}
}
