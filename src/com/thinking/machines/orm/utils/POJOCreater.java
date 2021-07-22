package com.thinking.machines.orm.utils;
import com.thinking.machines.orm.exceptions.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import com.google.gson.*;

public class POJOCreater
{
public static void createPOJOs() throws DataException
{
try
{
File file=new File("config.json");
if(!file.exists()) throw new DataException("Cannot find config.json");
RandomAccessFile randomAccessFile=new RandomAccessFile(file,"rw");
StringBuffer sb=new StringBuffer();
while(randomAccessFile.getFilePointer()<randomAccessFile.length()) sb.append(randomAccessFile.readLine());
randomAccessFile.close();
String rawData=sb.toString();
Gson gson=new Gson();
DatabaseConfiguration databaseConfiguration=gson.fromJson(rawData,DatabaseConfiguration.class);


String packageName=databaseConfiguration.getPackage_name();
String jarFileName=databaseConfiguration.getJar_file_name();
String path=packageName.replaceAll("\\.","\\\\");
path="src\\"+path;
File file1=new File(path);
String absolutePath=file1.getAbsolutePath();
file1=new File(absolutePath);
if(!file1.exists()) file1.mkdirs();
Class.forName(databaseConfiguration.getJdbc_driver());
Connection connection=DriverManager.getConnection(databaseConfiguration.getConnection_url(),databaseConfiguration.getUsername(),databaseConfiguration.getPassword());
DatabaseMetaData databaseMetaData=connection.getMetaData();
String table[]={"TABLE","VIEW"};
ResultSet resultSet=databaseMetaData.getTables(null,null,null,table);
String databaseTableName;
String tableName="";
String databaseAttributeName;
String attributeName;
ResultSet rs1;
ResultSet rs2;
int index;
String primaryKey="";
String type;
String foreignKey="";
String parentTable="";
String parentTableAttribute="";
int x=0;
while(resultSet.next())
{
databaseTableName=resultSet.getString(3);
type=resultSet.getString(4);
tableName=databaseTableName;
index=tableName.indexOf("_");
while(index!=-1)
{
tableName=tableName.substring(0,index)+tableName.substring(index+1,index+2).toUpperCase()+tableName.substring(index+2);
index=tableName.indexOf("_");
}
tableName=tableName.substring(0,1).toUpperCase()+tableName.substring(1);
file=new File(absolutePath+"/"+tableName+".java");
randomAccessFile=new RandomAccessFile(file,"rw");
randomAccessFile.writeBytes("package "+packageName+";\n");
randomAccessFile.writeBytes("import java.util.*;\nimport java.math.*;\nimport com.thinking.machines.orm.annotations.*;\n");

if(type.equalsIgnoreCase("TABLE"))randomAccessFile.writeBytes("@Table(name=\""+databaseTableName+"\")\npublic class "+tableName+"\n{\n");
if(type.equalsIgnoreCase("VIEW"))randomAccessFile.writeBytes("@View(name=\""+databaseTableName+"\")\npublic class "+tableName+"\n{\n");
rs1=databaseMetaData.getColumns(null,null,databaseTableName,null);
rs2=databaseMetaData.getPrimaryKeys(null,null,databaseTableName);
if(rs2.next()) primaryKey=rs2.getString(4);
rs2=databaseMetaData.getImportedKeys(null,null,databaseTableName);
if(rs2.next())
{
foreignKey=rs2.getString(8);
parentTable=rs2.getString(3);
parentTableAttribute=rs2.getString(4);
}
while(rs1.next())
{
databaseAttributeName=rs1.getString(4);
index=databaseAttributeName.indexOf("_");
attributeName=databaseAttributeName;
index=attributeName.indexOf("_");
while(index!=-1)
{
attributeName=attributeName.substring(0,index)+attributeName.substring(index+1,index+2).toUpperCase()+attributeName.substring(index+2);
index=attributeName.indexOf("_");
}

if(databaseAttributeName.equals(primaryKey)) randomAccessFile.writeBytes("@PrimaryKey\n");

if(databaseAttributeName.equals(foreignKey)) 
{
index=parentTable.indexOf("_");
while(index!=-1)
{
parentTable=parentTable.substring(0,index)+parentTable.substring(index+1,index+2).toUpperCase()+parentTable.substring(index+2);
index=parentTable.indexOf("_");
}
parentTable=parentTable.substring(0,1).toUpperCase()+parentTable.substring(1);
index=parentTableAttribute.indexOf("_");
while(index!=-1)
{
parentTableAttribute=parentTableAttribute.substring(0,index)+parentTableAttribute.substring(index+1,index+2).toUpperCase()+parentTableAttribute.substring(index+2);
index=parentTableAttribute.indexOf("_");
}
randomAccessFile.writeBytes("@ForeignKey(parent=\""+parentTable+"\",column=\""+parentTableAttribute+"\")\n");
if(rs2.next())
{
foreignKey=rs2.getString(8);
parentTable=rs2.getString(3);
parentTableAttribute=rs2.getString(4);
}
else
{
foreignKey="";
parentTable="";
parentTableAttribute="";
}
}


if(rs1.getString(23).equalsIgnoreCase("YES")) randomAccessFile.writeBytes("@AutoIncrement\n");

randomAccessFile.writeBytes("@Column(name=\""+databaseAttributeName+"\")\n");


type=rs1.getString(6);
if(type.equals("INT")) type="int";
else if(type.equals("CHAR") || type.equals("VARCHAR")) type="String";
else if(type.equals("DATE")) type="java.util.Date";
else if(type.equals("DECIMAL")) type="java.math.BigDecimal";
else if(type.equals("BIT") || type.equals("BOOLEAN") || type.equals("BOOL")) type="boolean";
else if(type.equals("BIGINT")) type="long";
else if(type.equals("FLOAT")) type="float";
else if(type.equals("DOUBLE")) type="double";
else if(type.equals("NUMERIC")) type="Object";
randomAccessFile.writeBytes("public "+type+" "+attributeName+";\n");
randomAccessFile.writeBytes("public void set"+attributeName.substring(0,1).toUpperCase()+attributeName.substring(1)+"("+type+" "+attributeName+")\n");
randomAccessFile.writeBytes("{\nthis."+attributeName+"="+attributeName+";\n}\n");
randomAccessFile.writeBytes("public "+type+" get"+attributeName.substring(0,1).toUpperCase()+attributeName.substring(1)+"()\n");
randomAccessFile.writeBytes("{\nreturn this."+attributeName+";\n}\n");
} // for attribute
randomAccessFile.writeBytes("}");
randomAccessFile.close();
}
file=new File("dist");
if(!file.exists()) file.mkdirs();
file=new File("classes");
if(!file.exists()) file.mkdirs();
Runtime runtime=Runtime.getRuntime();
String application1="javac -d classes -classpath ..\\dist\\*;. "+path+"\\*.java";
String application2="jar -cvf dist\\"+jarFileName+" classes\\";
Process process=runtime.exec(application1);
while(process.isAlive()){}
process=runtime.exec(application2);
while(process.isAlive()){}
}catch(Exception exception)
{
throw new DataException(exception.getMessage());
}
}
}