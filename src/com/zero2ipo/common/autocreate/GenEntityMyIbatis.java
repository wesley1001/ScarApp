package com.zero2ipo.common.autocreate;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Date;

public class GenEntityMyIbatis {
	
	private String packageOutPath = "com.zero2ipo.common.entity.app";//指定实体生成所在包的路径
	private String authorName = "郑云飞";//作者名字
	private String tablename = "orders";//表名
	private String[] colnames; // 列名数组
	private String[] colTypes; //列名类型数组
	private int[] colSizes; //列名大小数组
	private boolean f_util = false; // 是否需要导入包java.util.*
	private boolean f_sql = false; // 是否需要导入包java.sql.*
    //数据库连接
	private static final String URL ="jdbc:mysql://172.168.7.10:3306/scar";
	private static final String NAME = "root";
	private static final String PASS = "zhengyunfei";
	private static final String DRIVER ="com.mysql.jdbc.Driver";

	/*
	 * 构造函数
	 */
	public GenEntityMyIbatis(){
    	//创建连接
    	Connection con;
		//查要生成实体类的表
    	String sql = "select * from " + tablename;
    	PreparedStatement pStemt = null;
    	try {
    		try {
				Class.forName(DRIVER);
			} catch (ClassNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
    		con = DriverManager.getConnection(URL,NAME,PASS);
			pStemt = con.prepareStatement(sql);
			ResultSetMetaData rsmd = pStemt.getMetaData();
			int size = rsmd.getColumnCount();	//统计列
			colnames = new String[size];
			colTypes = new String[size];
			colSizes = new int[size];
			for (int i = 0; i < size; i++) {
				colnames[i] = rsmd.getColumnName(i + 1);
				colTypes[i] = rsmd.getColumnTypeName(i + 1);
				if(colTypes[i].equalsIgnoreCase("datetime")){
					f_util = true;
				}
				if(colTypes[i].equalsIgnoreCase("image") || colTypes[i].equalsIgnoreCase("text")){
					f_sql = true;
				}
				colSizes[i] = rsmd.getColumnDisplaySize(i + 1);
			}
			
			String content = parsexml(colnames,colTypes,colSizes);
			
			try {
				File directory = new File("");
				//System.out.println("绝对路径："+directory.getAbsolutePath());
				//System.out.println("相对路径："+directory.getCanonicalPath());
				String path=this.getClass().getResource("").getPath();
				
				System.out.println(path);
				System.out.println("src/?/"+path.substring(path.lastIndexOf("/com/", path.length())) );
//				String outputPath = directory.getAbsolutePath()+ "/src/"+path.substring(path.lastIndexOf("/com/", path.length()), path.length()) + initcap(tablename) + ".java";
				String outputPath = directory.getAbsolutePath()+ "/src/"+this.packageOutPath.replace(".", "/")+"/"+initcap(tablename) + "_sql.xml";
				FileWriter fw = new FileWriter(outputPath);
				PrintWriter pw = new PrintWriter(fw);
				pw.println(content);
				pw.flush();
				pw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
//			try {
//				con.close();
//			} catch (SQLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
    }

	/**
	 * 功能：生成实体类主体代码
	 * @param colnames
	 * @param colTypes
	 * @param colSizes
	 * @return
	 */
	private String parse(String[] colnames, String[] colTypes, int[] colSizes) {
		StringBuffer sb = new StringBuffer();
		sb.append("package " + this.packageOutPath + ";\r\n");
		//判断是否导入工具包
		if(f_util){
			sb.append("import java.util.Date;\r\n");
		}
		if(f_sql){
			sb.append("import java.sql.*;\r\n");
		}
		sb.append("\r\n");
		//注释部分
		sb.append("   /**\r\n");
		tablename=replaceUnderlineAndfirstToUpper(tablename.toLowerCase(),"_","");
		sb.append("    * "+tablename+" 实体类\r\n");
		sb.append("    * "+new Date()+" "+this.authorName+"\r\n");
		sb.append("    */ \r\n");
		//实体部分
		sb.append("\r\n\r\npublic class " + initcap(tablename) + "{\r\n");
		processAllAttrs(sb);//属性
		processAllMethod(sb);//get set方法
		boToEntity(sb);
		entityToBo(sb);
		sb.append("}\r\n");
		
    	//System.out.println(sb.toString());
		return sb.toString();
	}
	/**
	 * ibatis xml文件自动生成
	 *@title
	 *@date 2014-9-18
	 *@author ZhengYunfei
	 * @param colnames
	 * @param colTypes
	 * @param colSizes
	 * @return
	 */
	public String parsexml(String[] colnames, String[] colTypes, int[] colSizes){
		StringBuffer sb = new StringBuffer();
		//生成ibatis头文件
		sb.append("<!DOCTYPE sqlMap PUBLIC \"-//ibatis.apache.org//DTD SQL Map 2.0//EN\"" );
		sb.append("\r\n\t\t \"http://ibatis.apache.org/dtd/sql-map-2.dtd\">");	
		sb.append("\r\n<sqlMap>\r\n");
		sb.append("<typeAlias alias=\""+tablename+"\" type=\""+this.packageOutPath+"."+initcap(tablename)+"\"/>\r\n");
		//生成resultMap
	    sb.append("<resultMap class=\""+initcap(tablename)+"\""+" id=\""+initcap(tablename)+"\">\r\n");
	    for (int i = 0; i < colnames.length; i++) {
			String colname=replaceUnderlineAndfirstToUpper(colnames[i].toLowerCase(),"_","");//将数据库列转小写，去掉下划线并首字母大写
			sb.append("\t<result property=\"" + colname + "\" column=\"" + colnames[i] + "\"/>"); 
			sb.append("\n");
		}
	    sb.append("</resultMap>\r\n");
	    //插入查询语句
	    sb.append("<!--查询-->\r\n");
	    sb.append("\r\n<select id='find"+initcap(tablename)+"List' resultMap='"+initcap(tablename)+"'>\r\n");
	    sb.append("SELECT\r\n");
	    for (int i = 0; i < colnames.length; i++) {
	    	if(i<colnames.length-1){
	    		sb.append("\t"+colnames[i] +",\t\r\n"); 
	    	}else{
	    		sb.append("\t"+colnames[i] +"\t\r\n"); 
	    	}
			
		}
	    sb.append("FROM "+tablename+"\r\n");
	    sb.append("<dynamic prepend=\"WHERE\">\r\n");
	    sb.append("<isNotEmpty prepend=\"AND\" property=\"userId\">\r\n");
	    sb.append("INSTR(USER_ID,#userId#)>0\r\n");
	    sb.append("</isNotEmpty>\r\n");
	    sb.append("</dynamic>\r\n</select>\r\n");
	    //新增语句
	    sb.append("<!--新增-->\r\n");
	    sb.append("<insert id=\"addVipManage\" parameterClass=\"vipEntity\">\r\n");
	    sb.append("\tINSERT INTO "+tablename+"(\r\n");
	    for (int i = 0; i < colnames.length; i++) {
	    	if(i<colnames.length-1){
	    		sb.append("\t"+colnames[i] +",\t\r\n"); 
	    	}else{
	    		sb.append("\t"+colnames[i] +"\t\r\n"); 
	    	}
		}
	    sb.append(")VALUES(\r\n");
	    for (int i = 0; i < colnames.length; i++) {
			String colname=replaceUnderlineAndfirstToUpper(colnames[i].toLowerCase(),"_","");//将数据库列转小写，去掉下划线并首字母大写
			if(i<colnames.length-1){
				sb.append("\t#"+colname +"#,\t\r\n"); 
			}else{
				sb.append("\t#"+colname +"#\t\r\n"); 
			}
		}
	    sb.append(")\r\n");
	    sb.append("</insert>\r\n");
	    //修改
	    sb.append("<!--修改-->\r\n");
	    sb.append("<update id=\"updVipManage\" parameterClass=\"vipEntity\">\r\n");
	    sb.append("\tUPDATE\r\n");
	    sb.append("\t"+tablename+"\r\n"); 
	    sb.append("\tSET\r\n");
	    for (int i = 0; i < colnames.length; i++) {
			String colname=replaceUnderlineAndfirstToUpper(colnames[i].toLowerCase(),"_","");//将数据库列转小写，去掉下划线并首字母大写
			if(i<colnames.length-1){
				sb.append("\t"+colnames[i]+"=#"+colname +"#,\t\r\n"); 
			}else{
				sb.append("\t"+colnames[i]+"=#"+colname +"#\t\r\n"); 
			}
		}
		sb.append(" \tWHERE \r\n");
		sb.append("\tID=#id#\r\n");
		sb.append("</update>\r\n");
		//删除
		sb.append("<!--删除-->\r\n");
		sb.append("<delete id=\"delelete\" parameterClass=\"java.util.Map\">\r\n");
		sb.append("\tDELETE FROM \r\n");
		sb.append("\t"+tablename+"\r\n");
		sb.append("\tWHERE\r\n");
		sb.append("\tID IN\r\n");
		sb.append("\t<iterate property=\"id\" conjunction=\",\" close=\")\" open=\"(\">\r\n");
		sb.append("\t#id[]#\r\n");
		sb.append("\t</iterate> \r\n");
		sb.append("</delete>\r\n");
	    sb.append("</sqlMap>");
	    return sb.toString();
	}
	/**
	 * 功能：生成所有属性
	 * @param sb
	 */
	private void processAllAttrs(StringBuffer sb) {
		
		for (int i = 0; i < colnames.length; i++) {
			String colname=replaceUnderlineAndfirstToUpper(colnames[i].toLowerCase(),"_","");//将数据库列转小写，去掉下划线并首字母大写
			sb.append("\tprivate " + sqlType2JavaType(colTypes[i]) + " " + colname+ ";\r\n");
		}
		
	}
	/** 
	* 首字母大写 
	*  
	* @param srcStr 
	* @return 
	*/  
	public static String firstCharacterToUpper(String srcStr) {  
	   return srcStr.substring(0, 1).toUpperCase() + srcStr.substring(1);  
	}  
	/** 
	* 替换字符串并让它的下一个字母为大写 
	* @param srcStr 
	* @param org 
	* @param ob 
	* @return 
	*/  
	public static String replaceUnderlineAndfirstToUpper(String srcStr,String org,String ob)  
	{  
	   String newString = "";  
	   int first=0;  
	   while(srcStr.indexOf(org)!=-1)  
	   {  
	    first=srcStr.indexOf(org);  
	    if(first!=srcStr.length())  
	    {  
	     newString=newString+srcStr.substring(0,first)+ob;  
	     srcStr=srcStr.substring(first+org.length(),srcStr.length());  
	     srcStr=firstCharacterToUpper(srcStr);  
	    }  
	   }  
	   newString=newString+srcStr;  
	   return newString;  
	}  
	/**
	 * 功能：生成所有方法
	 * @param sb
	 */
	private void processAllMethod(StringBuffer sb) {
		
		for (int i = 0; i < colnames.length; i++) {
			String colname=replaceUnderlineAndfirstToUpper(colnames[i].toLowerCase(),"_","");//将数据库列转小写，去掉下划线并首字母大写
			sb.append("\tpublic void set" + initcap(colname) + "(" + sqlType2JavaType(colTypes[i]) + " " + 
					colname + "){\r\n");
			sb.append("\tthis." + colname + "=" + colname + ";\r\n");
			sb.append("\t}\r\n");
			sb.append("\tpublic " + sqlType2JavaType(colTypes[i]) + " get" + initcap(colname) + "(){\r\n");
			sb.append("\t\treturn " + colname + ";\r\n");
			sb.append("\t}\r\n");
		}
		
	}
public void boToEntity(StringBuffer sb){
	sb.append("\tpublic static "+initcap(tablename)+" boToEntity(" + initcap(tablename) + " bo){");
	sb.append("\r\n\t"+initcap(tablename)+" entity=new "+initcap(tablename)+"();\r\n");
	for (int i = 0; i < colnames.length; i++) {
		String colname=replaceUnderlineAndfirstToUpper(colnames[i].toLowerCase(),"_","");//将数据库列转小写，去掉下划线并首字母大写
		sb.append("\tentity.set" + initcap(colname) + "(bo.get" +initcap(colname) + "());\r\n");
	}
	sb.append("\treturn entity;\r\n\t}\r\n");
}
public void entityToBo(StringBuffer sb){
	sb.append("\tpublic static "+initcap(tablename)+" entityToBo(" + initcap(tablename) + " bo){");
	sb.append("\r\n\t"+initcap(tablename)+" entity=new "+initcap(tablename)+"();\r\n");
	for (int i = 0; i < colnames.length; i++) {
		String colname=replaceUnderlineAndfirstToUpper(colnames[i].toLowerCase(),"_","");//将数据库列转小写，去掉下划线并首字母大写
		sb.append("\tentity.set" + initcap(colname) + "(bo.get" +initcap(colname) + "());\r\n");
	}
	sb.append("\treturn entity;\r\n\t}\r\n");
}

	/**
	 * 功能：将输入字符串的首字母改成大写
	 * @param str
	 * @return
	 */
	private String initcap(String str) {
		
		char[] ch = str.toCharArray();
		if(ch[0] >= 'a' && ch[0] <= 'z'){
			ch[0] = (char)(ch[0] - 32);
		}
		
		return new String(ch);
	}

	/**
	 * 功能：获得列的数据类型
	 * @param sqlType
	 * @return
	 */
	private String sqlType2JavaType(String sqlType) {
		
		if(sqlType.equalsIgnoreCase("bit")){
			return "boolean";
		}else if(sqlType.equalsIgnoreCase("tinyint")){
			return "byte";
		}else if(sqlType.equalsIgnoreCase("smallint")){
			return "short";
		}else if(sqlType.equalsIgnoreCase("int")){
			return "int";
		}else if(sqlType.equalsIgnoreCase("bigint")){
			return "long";
		}else if(sqlType.equalsIgnoreCase("float")){
			return "float";
		}else if(sqlType.equalsIgnoreCase("decimal") || sqlType.equalsIgnoreCase("numeric") 
				|| sqlType.equalsIgnoreCase("real") || sqlType.equalsIgnoreCase("money") 
				|| sqlType.equalsIgnoreCase("smallmoney")){
			return "double";
		}else if(sqlType.equalsIgnoreCase("varchar") || sqlType.equalsIgnoreCase("char") 
				|| sqlType.equalsIgnoreCase("nvarchar") || sqlType.equalsIgnoreCase("nchar") 
				|| sqlType.equalsIgnoreCase("text")){
			return "String";
		}else if(sqlType.equalsIgnoreCase("datetime")){
			return "Date";
		}else if(sqlType.equalsIgnoreCase("image")){
			return "Blod";
		}
		
		return null;
	}
	
	/**
	 * 出口
	 * TODO
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("aaaaaaaaaaaaaaaaa");
		new GenEntityMyIbatis();
		
	}

}