package com.astro.onetable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.astro.crossrefs.SimbadDataTranslation;
import com.astro.onetable.Main.Names;
import com.astro.onetable.Main.Obj;
import com.astro.onetable.Main.Obj2;
import com.astro.simbaddata.AstroTools.RaDecRec;

public class Parsers {

	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println(getCorrectPKName("PK12+32.1"));

	}
	public static void p(String s){
		System.out.println(s);
	}
	/**
	 * load RA/DEC info from the file into map
	 * @param in
	 */
	public static List<Obj> loadSimbadRaDec(File f)throws Exception{
		List<Obj>list=new ArrayList<Main.Obj>();
		BufferedReader reader=new BufferedReader(new InputStreamReader(new FileInputStream(f)));
		String s;
		while((s=reader.readLine())!=null){
			String arr[]=s.split(":");
			if(arr.length!=2)throw new Exception("Wrong split "+s);
			if(arr[1].contains("No Coord"))
				continue;
			Pattern p=Pattern.compile("[0-9[\\.\\+\\-]]+");
			Matcher matcher = p.matcher(arr[1]);
			boolean res=matcher.find();//substring match
			if(!res) throw new Exception("No ra "+s);
			String rastr=arr[1].substring(matcher.start(),matcher.end());
			double ra=Double.parseDouble(rastr);
			ra=ra*24/360;

			res=matcher.find();
			if(!res) throw new Exception("No dec "+s);
			String decstr=arr[1].substring(matcher.start(),matcher.end());
			double dec=Double.parseDouble(decstr);
			list.add(new Obj(arr[0],new RaDecRec(ra, dec)));
			
			
		}
		reader.close();
		return list;
	}
	
	/**
	 * load RA/DEC/mag info from the file into map
	 * @param in
	 */
	public static List<Obj> loadSimbadRaDecMag(File f)throws Exception{
		List<Obj>list=new ArrayList<Main.Obj>();
		BufferedReader reader=new BufferedReader(new InputStreamReader(new FileInputStream(f)));
		String s;
		int j=1;
		while((s=reader.readLine())!=null){
			String arr[]=s.split(":");
			if(arr.length!=3)throw new Exception("Wrong split "+s);
			if(arr[1].contains("No Coord"))
				continue;
			Pattern p=Pattern.compile("[0-9[\\.\\+\\-]]+");
			Matcher matcher = p.matcher(arr[1]);
			boolean res=matcher.find();//substring match
			if(!res) throw new Exception("No ra "+s);
			String rastr=arr[1].substring(matcher.start(),matcher.end());
			double ra=Double.parseDouble(rastr);
			ra=ra*24/360;

			res=matcher.find();
			if(!res) throw new Exception("No dec "+s);
			String decstr=arr[1].substring(matcher.start(),matcher.end());
			double dec=Double.parseDouble(decstr);
			
			double mag=0;
			try{
				mag=Double.parseDouble(arr[2].replace(" ", ""));
			}
			catch(Exception e){}
			list.add(new Obj(arr[0],new RaDecRec(ra, dec),mag));
			Main.p(""+j++);
			
		}
		reader.close();
		return list;
	}
	
	
	/**
	 * load RA/DEC info from the file into map
	 * @param in
	 * @rep key=simbad name, value=my db name
	 */
	public static List<Obj> loadSimbadRaDec(File f, Map<String,String> rep)throws Exception{
		List<Obj>list=new ArrayList<Main.Obj>();
		BufferedReader reader=new BufferedReader(new InputStreamReader(new FileInputStream(f)));
		String s;
		while((s=reader.readLine())!=null){
			String arr[]=s.split(":");
			if(arr.length!=2)throw new Exception("Wrong split "+s);
			if(arr[1].contains("No Coord"))
				continue;
			Pattern p=Pattern.compile("[0-9[\\.\\+\\-]]+");
			Matcher matcher = p.matcher(arr[1]);
			boolean res=matcher.find();//substring match
			if(!res) throw new Exception("No ra "+s);
			String rastr=arr[1].substring(matcher.start(),matcher.end());
			double ra=Double.parseDouble(rastr);
			ra=ra*24/360;

			res=matcher.find();
			if(!res) throw new Exception("No dec "+s);
			String decstr=arr[1].substring(matcher.start(),matcher.end());
			double dec=Double.parseDouble(decstr);
			
			String name=rep.get(arr[0]);
			if(name==null) name=arr[0];//throw new Exception("No obj name at map "+s);
			
			list.add(new Obj(name,new RaDecRec(ra, dec)));
			
			
		}
		reader.close();
		return list;
	}
	
	/**
	 * processing Simbad result and writing processed result to the file
	 * truncate Simbad result to start from the next string after ::data::: 
	 * for SAC run after makeSACscript as it makes replacement file!!! 
	 * @param namein
	 * @param nameout
	 * @param map - map of back replacement (to get the original my db name from the modified name submitted to simbad). key=simbad name, value=my db name
	 * @param checkcoords whether to check coords. LoadRaDec before
	 * @throws Exception
	 */
	public static List<Obj2> processSimbadNames(String dir,String namein,Map<String,String> map)throws Exception{
		BufferedReader in=new BufferedReader(new InputStreamReader(new FileInputStream(new File(dir,namein))));
		//PrintWriter pw=new PrintWriter(new FileOutputStream(new File(dir,nameout)));
		String s="";
		String obj="";
		boolean next=false;
		int j=0;
		int skipped=0;
		double dstmax=0;
		List<Obj2>ll=new ArrayList<Main.Obj2>();
		while((s=in.readLine())!=null){
			//p(s);
			if(next){
					
				List<String> list=new SimbadDataTranslation(s).getIdentifiers();
				
				if(list.size()==0)throw new Exception("Empty list at "+j+" "+s);
				String origname=null;
				if(map!=null){
					origname=map.get(obj.trim());
				}
				String objname=origname==null?obj.toUpperCase().trim():origname.toUpperCase().trim();
				//if(list.size()!=0)
				ll.add(new Obj2(objname,new Names(list)));
				
				for(String name:list){
					
					//pw.println(origname==null?obj.toUpperCase().trim()+";"+name:origname.toUpperCase().trim()+";"+name);
				}
				next=false;
				continue;
			}
			else if(s.matches("[a-zA-Z1-9]+.+")){
				obj=s;
				//p(obj);
				next=true;
				continue;
			}	
			j++;
		}
	//	pw.close();
		in.close();
		return ll;
	}
	
	
	/**
	 * 
	 * @param filename text file with original db name;simbad name pairs
	 * @return map of back replacement (to get the original my db name from the modified name submitted to simbad). key=simbad name, value=my db name
	 */
	public static Map<String,String>fillReplacementMap(String dir,String filename)throws Exception{
		BufferedReader in=new BufferedReader(new InputStreamReader(new FileInputStream(new File(dir,filename))));
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
	
	public static String getCorrectPKName(String s){
		if(!s.matches("PK[0-9]+[\\+\\-][0-9]+.*"))
			return null;
		Pattern p=Pattern.compile("PK[0-9]+");
		Matcher m=p.matcher(s);
		String s2="";
		String s3;
		if(m.find()){
			s2=s.substring(m.start(),m.end());
			if(s2.length()<5){
				int count=5-s2.length();
				s3=s2.replace("PK", "");
				for(int i=0;i<count;i++){
					s3="0"+s3;
					
				}
				s3="PK"+s3;
			}
			else
				s3=s2;
		}
		else
			return null;
		//p(s3);
		
		String s4=s.replace(s2, "");
		//p(s4);
		if(!(s4.charAt(0)=='+'||s4.charAt(0)=='-'))
			return null;
		s3=s3+s4.charAt(0);
		//p("a "+s3);
		s4=s4.substring(1,s4.length());		
		p=Pattern.compile("[0-9]+");
		m=p.matcher(s4);
		
		if(m.find()){
			int st=m.start();
			int e=m.end();
			String s5=s4.substring(st,e);
			if(s5.length()<2){
				s5="0"+s5;
			}
			s4=s3+s5+s4.substring(e, s4.length());
		}
		else
			return null;
		
		return s4;
		
	}
	

}
