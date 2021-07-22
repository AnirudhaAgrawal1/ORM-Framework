package com.thinking.machines.orm;
import java.sql.*;
import com.google.gson.*;
import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import com.thinking.machines.orm.utils.*;
import com.thinking.machines.orm.exceptions.*;
import com.thinking.machines.orm.annotations.*;
public class DataManager
{
private final static DataManager dataManager;
private Connection connection;
private Map<String,Map<String,QueryHandler>> model;
private Map<String,Method> preparedStatementSetterMethods;
private Map<String,Map<String,Method>> pojoGetterMethods;
private DatabaseConfiguration databaseConfiguration;
private boolean isBeganExecuted;
static
{
dataManager=new DataManager();
}
private DataManager()
{
this.connection=null;
this.databaseConfiguration=null;
model=new HashMap<>();
preparedStatementSetterMethods=new HashMap<>();
pojoGetterMethods=new HashMap<>();
this.isBeganExecuted=false;
}
public static DataManager getDataManager()
{
return dataManager;
}
public void begin() throws DataException
{
try
{
if(this.isBeganExecuted) return;
File file=new File("config.json");
if(file==null) throw new DataException("Connot found config.json.");
RandomAccessFile randomAccessFile=new RandomAccessFile(file,"rw");
StringBuffer sb=new StringBuffer();
while(randomAccessFile.getFilePointer()<randomAccessFile.length()) sb.append(randomAccessFile.readLine());
randomAccessFile.close();
String rawData=sb.toString();
Gson gson=new Gson();
this.databaseConfiguration=gson.fromJson(rawData,DatabaseConfiguration.class);
String packageName=databaseConfiguration.getPackage_name();
this.populatePreparedStatementSetterMethods(preparedStatementSetterMethods);

Class.forName(this.databaseConfiguration.getJdbc_driver());
this.connection=DriverManager.getConnection(this.databaseConfiguration.getConnection_url(),this.databaseConfiguration.getUsername(),this.databaseConfiguration.getPassword());
DatabaseMetaData databaseMetaData=connection.getMetaData();
String a[]={"table"};
ResultSet resultSet=databaseMetaData.getTables(null,null,null,a);
String tableName;
String className;
Class c;
int index;
Map<String,QueryHandler> map;
Map<String,Method> map1;
while(resultSet.next())
{
tableName=resultSet.getString(3);
className=tableName;
index=className.indexOf("_");
while(index!=-1)
{
className=className.substring(0,index)+className.substring(index+1,index+2).toUpperCase()+className.substring(index+2);
index=className.indexOf("_");
}
className=className.substring(0,1).toUpperCase()+className.substring(1);
c=Class.forName(packageName+"."+className);
map1=new HashMap<>();
this.populatePojoGetterMethods(c,map1);
this.pojoGetterMethods.put(((Table)c.getAnnotation(Table.class)).name(),map1);
map=new HashMap<>();

this.createQueryStrings(c,map);

model.put(((Table)c.getAnnotation(Table.class)).name(),map);



}
resultSet.close();
this.isBeganExecuted=true;
}catch(Exception exception)
{
throw new DataException(exception.getMessage());
}
}

private void populatePreparedStatementSetterMethods(Map map) throws DataException
{
try
{
Class c=Class.forName("java.sql.PreparedStatement");
Method methods[]=c.getMethods();
for(Method m:methods)
{
if(m.getName().equals("setInt")) map.put("int",m);
else if(m.getName().equals("setLong")) map.put("long",m);
else if(m.getName().equals("setDouble")) map.put("double",m);
else if(m.getName().equals("setString")) map.put("String",m);
else if(m.getName().equals("setShort")) map.put("short",m);
else if(m.getName().equals("setBoolean")) map.put("boolean",m);
else if(m.getName().equals("setFloat")) map.put("float",m);
else if(m.getName().equals("setShort")) map.put("short",m);
else if(m.getName().equals("setByte")) map.put("byte",m);
else if(m.getName().equals("setDate")) map.put("Date",m);
}
}catch(Exception exception)
{
throw new DataException(exception.getMessage());
}
}

private void populatePojoGetterMethods(Class c,Map map) throws DataException
{
try
{
Field fields[]=c.getDeclaredFields();
String fieldName;
Method methods[]=c.getMethods();
String getterName;
for(Field f:fields)
{
fieldName=f.getName();
for(Method m:methods)
{
getterName="get"+f.getName();
if(getterName.equalsIgnoreCase(m.getName()))
{
map.put(((Column)f.getAnnotation(Column.class)).name(),m);
break;
}
}
}
}catch(Exception exception)
{
throw new DataException(exception.getMessage());
}
}

private void createQueryStrings(Class c,Map map) throws DataException
{
try
{
String tableName=((Table)c.getAnnotation(Table.class)).name();
PreparedStatement insertPreparedStatement;
PreparedStatement updatePreparedStatement;
PreparedStatement deletePreparedStatement;
PreparedStatement getByPrimaryKeyPreparedStatement;
PreparedStatement getByForeignKey;
String columnName;
QueryHandler queryHandler;
Field fields[]=c.getDeclaredFields();
String type;
String insertStatement1="";
String insertStatement2="";
String updateStatement1="";
String updateStatement2="";
insertStatement1="insert into "+tableName+" (";
insertStatement2=") values(";
updateStatement1="update "+tableName+" set ";
updateStatement2=" where ";
int i=0;
Method m1=null;
Method m2=null;
QueryHandler insertQueryHandler=new QueryHandler();
QueryHandler updateQueryHandler=new QueryHandler();
for(Field f:fields)
{
i++;
columnName=((Column)f.getAnnotation(Column.class)).name();

if(f.getAnnotation(PrimaryKey.class)!=null)
{
getByPrimaryKeyPreparedStatement=this.connection.prepareStatement("select * from "+tableName+" where "+columnName+" =?");
queryHandler=new QueryHandler();
queryHandler.setPreparedStatement(getByPrimaryKeyPreparedStatement);
queryHandler.addGetterMethod(this.pojoGetterMethods.get(tableName).get(f.getName()));
type=f.getType().getSimpleName();
if(type.equalsIgnoreCase("int")) queryHandler.addSetterMethod(this.preparedStatementSetterMethods.get("int"));
else if(type.equalsIgnoreCase("long")) queryHandler.addSetterMethod(this.preparedStatementSetterMethods.get("long"));
else if(type.equalsIgnoreCase("double")) queryHandler.addSetterMethod(this.preparedStatementSetterMethods.get("double"));
else if(type.equalsIgnoreCase("short")) queryHandler.addSetterMethod(this.preparedStatementSetterMethods.get("short"));
else if(type.equalsIgnoreCase("float")) queryHandler.addSetterMethod(this.preparedStatementSetterMethods.get("float"));
else if(type.equalsIgnoreCase("boolean")) queryHandler.addSetterMethod(this.preparedStatementSetterMethods.get("boolean"));
else if(type.equalsIgnoreCase("byte")) queryHandler.addSetterMethod(this.preparedStatementSetterMethods.get("byte"));
else if(type.equalsIgnoreCase("String")) queryHandler.addSetterMethod(this.preparedStatementSetterMethods.get("String"));
else if(type.equalsIgnoreCase("Date") || type.equalsIgnoreCase("java.util.Date")) queryHandler.addSetterMethod(this.preparedStatementSetterMethods.get("Date"));
map.put("getByPrimaryKey",queryHandler);
deletePreparedStatement=this.connection.prepareStatement("delete from "+tableName+" where "+columnName+" =?");
queryHandler=new QueryHandler();
queryHandler.setPreparedStatement(deletePreparedStatement);
queryHandler.addGetterMethod(this.pojoGetterMethods.get(tableName).get(f.getName()));
type=f.getType().getSimpleName();
if(type.equalsIgnoreCase("int")) queryHandler.addSetterMethod(this.preparedStatementSetterMethods.get("int"));
else if(type.equalsIgnoreCase("long")) queryHandler.addSetterMethod(this.preparedStatementSetterMethods.get("long"));
else if(type.equalsIgnoreCase("double")) queryHandler.addSetterMethod(this.preparedStatementSetterMethods.get("double"));
else if(type.equalsIgnoreCase("short")) queryHandler.addSetterMethod(this.preparedStatementSetterMethods.get("short"));
else if(type.equalsIgnoreCase("float")) queryHandler.addSetterMethod(this.preparedStatementSetterMethods.get("float"));
else if(type.equalsIgnoreCase("boolean")) queryHandler.addSetterMethod(this.preparedStatementSetterMethods.get("boolean"));
else if(type.equalsIgnoreCase("byte")) queryHandler.addSetterMethod(this.preparedStatementSetterMethods.get("byte"));
else if(type.equalsIgnoreCase("String")) queryHandler.addSetterMethod(this.preparedStatementSetterMethods.get("String"));
else if(type.equalsIgnoreCase("Date") || type.equalsIgnoreCase("java.util.Date")) queryHandler.addSetterMethod(this.preparedStatementSetterMethods.get("Date"));
map.put("delete",queryHandler);
updateStatement2+=columnName+"=?";
m1=this.pojoGetterMethods.get(tableName).get(f.getName());

type=f.getType().getSimpleName();
if(type.equalsIgnoreCase("int")) m2=this.preparedStatementSetterMethods.get("int");
else if(type.equalsIgnoreCase("long")) m2=this.preparedStatementSetterMethods.get("long");
else if(type.equalsIgnoreCase("double")) m2=this.preparedStatementSetterMethods.get("double");
else if(type.equalsIgnoreCase("short")) m2=this.preparedStatementSetterMethods.get("short");
else if(type.equalsIgnoreCase("float")) m2=this.preparedStatementSetterMethods.get("float");
else if(type.equalsIgnoreCase("boolean")) m2=this.preparedStatementSetterMethods.get("boolean");
else if(type.equalsIgnoreCase("byte")) m2=this.preparedStatementSetterMethods.get("byte");
else if(type.equalsIgnoreCase("String")) m2=this.preparedStatementSetterMethods.get("String");
else if(type.equalsIgnoreCase("Date") || type.equalsIgnoreCase("java.util.Date")) m2=this.preparedStatementSetterMethods.get("Date");


if(f.getAnnotation(AutoIncrement.class)!=null) continue;
insertStatement1+=columnName;
insertStatement2+="?";
if(i<fields.length) 
{
insertStatement1+=",";
insertStatement2+=",";
}
insertQueryHandler.addGetterMethod(this.pojoGetterMethods.get(tableName).get(f.getName()));
type=f.getType().getSimpleName();
if(type.equalsIgnoreCase("int")) insertQueryHandler.addSetterMethod(this.preparedStatementSetterMethods.get("int"));
else if(type.equalsIgnoreCase("long")) insertQueryHandler.addSetterMethod(this.preparedStatementSetterMethods.get("long"));
else if(type.equalsIgnoreCase("double")) insertQueryHandler.addSetterMethod(this.preparedStatementSetterMethods.get("double"));
else if(type.equalsIgnoreCase("short")) insertQueryHandler.addSetterMethod(this.preparedStatementSetterMethods.get("short"));
else if(type.equalsIgnoreCase("float")) insertQueryHandler.addSetterMethod(this.preparedStatementSetterMethods.get("float"));
else if(type.equalsIgnoreCase("boolean")) insertQueryHandler.addSetterMethod(this.preparedStatementSetterMethods.get("boolean"));
else if(type.equalsIgnoreCase("byte")) insertQueryHandler.addSetterMethod(this.preparedStatementSetterMethods.get("byte"));
else if(type.equalsIgnoreCase("String")) insertQueryHandler.addSetterMethod(this.preparedStatementSetterMethods.get("String"));
else if(type.equalsIgnoreCase("Date") || type.equalsIgnoreCase("java.util.Date")) insertQueryHandler.addSetterMethod(this.preparedStatementSetterMethods.get("Date"));
continue;
}
updateStatement1+=columnName+"=?";
if(i<fields.length) updateStatement1+=",";
updateQueryHandler.addGetterMethod(this.pojoGetterMethods.get(tableName).get(f.getName()));
type=f.getType().getSimpleName();
if(type.equalsIgnoreCase("int")) updateQueryHandler.addSetterMethod(this.preparedStatementSetterMethods.get("int"));
else if(type.equalsIgnoreCase("long")) updateQueryHandler.addSetterMethod(this.preparedStatementSetterMethods.get("long"));
else if(type.equalsIgnoreCase("double")) updateQueryHandler.addSetterMethod(this.preparedStatementSetterMethods.get("double"));
else if(type.equalsIgnoreCase("short")) updateQueryHandler.addSetterMethod(this.preparedStatementSetterMethods.get("short"));
else if(type.equalsIgnoreCase("float")) updateQueryHandler.addSetterMethod(this.preparedStatementSetterMethods.get("float"));
else if(type.equalsIgnoreCase("boolean")) updateQueryHandler.addSetterMethod(this.preparedStatementSetterMethods.get("boolean"));
else if(type.equalsIgnoreCase("byte")) updateQueryHandler.addSetterMethod(this.preparedStatementSetterMethods.get("byte"));
else if(type.equalsIgnoreCase("String")) updateQueryHandler.addSetterMethod(this.preparedStatementSetterMethods.get("String"));
else if(type.equalsIgnoreCase("Date") || type.equalsIgnoreCase("java.util.Date")) updateQueryHandler.addSetterMethod(this.preparedStatementSetterMethods.get("Date"));
if(f.getAnnotation(AutoIncrement.class)!=null) continue;
insertStatement1+=columnName;
insertStatement2+="?";
if(i<fields.length) 
{
insertStatement1+=",";
insertStatement2+=",";
}
insertQueryHandler.addGetterMethod(this.pojoGetterMethods.get(tableName).get(f.getName()));
type=f.getType().getSimpleName();
if(type.equalsIgnoreCase("int")) insertQueryHandler.addSetterMethod(this.preparedStatementSetterMethods.get("int"));
else if(type.equalsIgnoreCase("long")) insertQueryHandler.addSetterMethod(this.preparedStatementSetterMethods.get("long"));
else if(type.equalsIgnoreCase("double")) insertQueryHandler.addSetterMethod(this.preparedStatementSetterMethods.get("double"));
else if(type.equalsIgnoreCase("short")) insertQueryHandler.addSetterMethod(this.preparedStatementSetterMethods.get("short"));
else if(type.equalsIgnoreCase("float")) insertQueryHandler.addSetterMethod(this.preparedStatementSetterMethods.get("float"));
else if(type.equalsIgnoreCase("boolean")) insertQueryHandler.addSetterMethod(this.preparedStatementSetterMethods.get("boolean"));
else if(type.equalsIgnoreCase("byte")) insertQueryHandler.addSetterMethod(this.preparedStatementSetterMethods.get("byte"));
else if(type.equalsIgnoreCase("String")) insertQueryHandler.addSetterMethod(this.preparedStatementSetterMethods.get("String"));
else if(type.equalsIgnoreCase("Date") || type.equalsIgnoreCase("java.util.Date")) insertQueryHandler.addSetterMethod(this.preparedStatementSetterMethods.get("Date"));
}
insertStatement2+=")";
insertPreparedStatement=this.connection.prepareStatement(insertStatement1+insertStatement2,Statement.RETURN_GENERATED_KEYS);
insertQueryHandler.setPreparedStatement(insertPreparedStatement);
updatePreparedStatement=this.connection.prepareStatement(updateStatement1+updateStatement2);
updateQueryHandler.setPreparedStatement(updatePreparedStatement);
updateQueryHandler.addGetterMethod(m1);
updateQueryHandler.addSetterMethod(m2);
map.put("insert",insertQueryHandler);
map.put("update",updateQueryHandler);
}catch(Exception exception)
{
throw new DataException(exception.getMessage());
}
}

public Object save(Object object) throws DataException
{
try
{
if(!this.isBeganExecuted) throw new DataException("began should be executed in prior to save.");
if(object==null) throw new DataException("Argument can not ne null");
Class c=object.getClass();
if(c.getAnnotation(View.class)!=null) throw new DataException("Cannot perform insert operaton on view.");
if(c.getAnnotation(Table.class)==null) throw new DataException("Cannot find table associated with class :"+c.getSimpleName()+"\nAnnotation missing : \'Table\'");
String tableName=((Table)c.getAnnotation(Table.class)).name();
Map map=model.get(tableName);
if(map==null) throw new DataException("Cannot find table associated with class :"+c.getSimpleName());
DatabaseMetaData databaseMetaData=this.connection.getMetaData();
ResultSet resultSet;
String foreignKeyTable="";
String foreignKeyAttribute="";
String attribute="";
PreparedStatement preparedStatement;
QueryHandler queryHandler;
Method setter,getter;
ResultSet rs;
Object obj;
queryHandler=((QueryHandler)model.get(tableName).get("getByPrimaryKey"));
preparedStatement=queryHandler.getPreparedStatement();
setter=queryHandler.getSetterMethod(0);
getter=queryHandler.getGetterMethod(0);
setter.invoke(preparedStatement,1,getter.invoke(object));
rs=preparedStatement.executeQuery();
if(rs.next()) throw new DataException("Primary key exists.");
resultSet=databaseMetaData.getImportedKeys(null,null,tableName);
while(resultSet.next())
{
foreignKeyTable=resultSet.getString(3);
foreignKeyAttribute=resultSet.getString(4);
attribute=resultSet.getString(8);
queryHandler=((QueryHandler)model.get(foreignKeyTable).get("getByPrimaryKey"));
preparedStatement=queryHandler.getPreparedStatement();
setter=queryHandler.getSetterMethod(0);
getter=pojoGetterMethods.get(tableName).get(attribute);
setter.invoke(preparedStatement,1,getter.invoke(object));
rs=preparedStatement.executeQuery();
if(!rs.next()) throw new DataException("Foreign key constraint violated");
}
queryHandler=((QueryHandler)model.get(tableName).get("insert"));
preparedStatement=queryHandler.getPreparedStatement();
for(int i=0;i<queryHandler.getListSize();i++)
{
setter=queryHandler.getSetterMethod(i);
getter=queryHandler.getGetterMethod(i);
obj=getter.invoke(object);
if(obj.getClass().getName().equalsIgnoreCase("java.util.Date"))
{
java.util.Date date=(java.util.Date)obj;
java.sql.Date sqlDate=new java.sql.Date(date.getYear(),date.getMonth(),date.getDate());
setter.invoke(preparedStatement,i+1,sqlDate);
continue;
}
setter.invoke(preparedStatement,i+1,obj);
}
preparedStatement.executeUpdate();
rs=preparedStatement.getGeneratedKeys();
if(rs.next()) return rs.getInt(1);
return null;
}catch(Exception exception)
{
throw new DataException(exception.getMessage());
}
}

public void update(Object object) throws DataException
{
try
{
if(!this.isBeganExecuted) throw new DataException("began should be executed in prior to update.");
if(object==null) throw new DataException("Argument can not ne null");
Class c=object.getClass();
if(c.getAnnotation(View.class)!=null) throw new DataException("Cannot perform update operaton on view.");
if(c.getAnnotation(Table.class)==null) throw new DataException("Cannot find table associated with class :"+c.getSimpleName()+"\nAnnotation missing : \'Table\'");
String tableName=((Table)c.getAnnotation(Table.class)).name();
Map map=model.get(tableName);
if(map==null) throw new DataException("Cannot find table associated with class :"+c.getSimpleName());
DatabaseMetaData databaseMetaData=this.connection.getMetaData();
ResultSet resultSet;
String foreignKeyTable="";
String foreignKeyAttribute="";
String attribute="";
PreparedStatement preparedStatement;
QueryHandler queryHandler;
Method setter,getter;
ResultSet rs;
Object obj;
queryHandler=((QueryHandler)model.get(tableName).get("getByPrimaryKey"));
preparedStatement=queryHandler.getPreparedStatement();
setter=queryHandler.getSetterMethod(0);
getter=queryHandler.getGetterMethod(0);
setter.invoke(preparedStatement,1,getter.invoke(object));
rs=preparedStatement.executeQuery();
if(!rs.next()) throw new DataException("Can not find data to update.");
resultSet=databaseMetaData.getImportedKeys(null,null,tableName);
while(resultSet.next())
{
foreignKeyTable=resultSet.getString(3);
foreignKeyAttribute=resultSet.getString(4);
attribute=resultSet.getString(8);
queryHandler=((QueryHandler)model.get(foreignKeyTable).get("getByPrimaryKey"));
preparedStatement=queryHandler.getPreparedStatement();
setter=queryHandler.getSetterMethod(0);
getter=pojoGetterMethods.get(tableName).get(attribute);
setter.invoke(preparedStatement,1,getter.invoke(object));
rs=preparedStatement.executeQuery();
if(!rs.next()) throw new DataException("Foreign key constraint violated");
}
queryHandler=((QueryHandler)model.get(tableName).get("update"));
preparedStatement=queryHandler.getPreparedStatement();
for(int i=0;i<queryHandler.getListSize();i++)
{
setter=queryHandler.getSetterMethod(i);
getter=queryHandler.getGetterMethod(i);
obj=getter.invoke(object);
if(obj.getClass().getName().equalsIgnoreCase("java.util.Date"))
{
java.util.Date date=(java.util.Date)obj;
java.sql.Date sqlDate=new java.sql.Date(date.getYear(),date.getMonth(),date.getDate());
setter.invoke(preparedStatement,i+1,sqlDate);
continue;
}
setter.invoke(preparedStatement,i+1,obj);
}
preparedStatement.executeUpdate();
}catch(Exception exception)
{
throw new DataException(exception.getMessage());
}
}





public void delete(Object object) throws DataException
{
try
{
if(!this.isBeganExecuted) throw new DataException("began should be executed in prior to delete.");
if(object==null) throw new DataException("Argument can not ne null");
Class c=object.getClass();
if(c.getAnnotation(View.class)!=null) throw new DataException("Cannot perform delete operaton on view.");
if(c.getAnnotation(Table.class)==null) throw new DataException("Cannot find table associated with class :"+c.getSimpleName()+"\nAnnotation missing : \'Table\'");
String tableName=((Table)c.getAnnotation(Table.class)).name();
Map map=model.get(tableName);
if(map==null) throw new DataException("Cannot find table associated with class :"+c.getSimpleName());
DatabaseMetaData databaseMetaData=this.connection.getMetaData();
ResultSet resultSet;
PreparedStatement preparedStatement;
QueryHandler queryHandler;
Method setter,getter,m;
ResultSet rs;
Object obj;
queryHandler=((QueryHandler)model.get(tableName).get("getByPrimaryKey"));
preparedStatement=queryHandler.getPreparedStatement();
setter=queryHandler.getSetterMethod(0);
getter=queryHandler.getGetterMethod(0);
setter.invoke(preparedStatement,1,getter.invoke(object));
rs=preparedStatement.executeQuery();
if(!rs.next()) throw new DataException("Invalid Primary key.");
rs.close();
resultSet=databaseMetaData.getExportedKeys(null,null,tableName);
String foreignKeyTableName;
String foreignKeyColumnName;
while(resultSet.next())
{
foreignKeyTableName=resultSet.getString(7);
foreignKeyColumnName=resultSet.getString(8);
m=pojoGetterMethods.get(tableName).get(resultSet.getString(4));
preparedStatement=this.connection.prepareStatement("select * from "+foreignKeyTableName+" where "+foreignKeyColumnName+"=?");
setter=preparedStatementSetterMethods.get(m.getReturnType().getSimpleName());
setter.invoke(preparedStatement,1,m.invoke(object));
rs=preparedStatement.executeQuery();
if(rs.next())
{
rs.close();
throw new DataException("Can not delete parent data,key exists against "+foreignKeyTableName+"("+foreignKeyColumnName+").");
}
}
queryHandler=model.get(tableName).get("delete");
preparedStatement=queryHandler.getPreparedStatement();
queryHandler.getSetterMethod(0).invoke(preparedStatement,1,queryHandler.getGetterMethod(0).invoke(object));
preparedStatement.executeUpdate();

}catch(Exception exception)
{
throw new DataException(exception.getMessage());
}
} // delete function ends here

public Query query(Class className) throws DataException
{
if(!this.isBeganExecuted) throw new DataException("began should be executed in prior to query.");
if(className==null) throw new DataException("Argument should not be null.");
if(className.getAnnotation(Table.class)==null && className.getAnnotation(View.class)==null) throw new DataException("Unable to find table or view associated with class : "+className.getSimpleName());
Query query=new Query(this.connection,className);
return query;
}

public void end() throws DataException
{
try
{
if(!this.isBeganExecuted) return;
this.connection.close();
this.model.clear();
this.preparedStatementSetterMethods.clear();
pojoGetterMethods.clear();
this.isBeganExecuted=false;
}catch(Exception exception)
{
throw new DataException(exception.getMessage());
}
}
}