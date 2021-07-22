package com.thinking.machines.orm.utils;
import java.lang.*;
import java.sql.*;
import java.util.*;
import java.lang.reflect.*;
import com.thinking.machines.orm.exceptions.*;
import com.thinking.machines.orm.annotations.*;
public class Query
{
private Connection connection;
private Class className;
private String queryString;
private boolean isWhereClauseApplied;
private boolean isOrderByApplied;
public Query(Connection connection,Class className)
{
this.connection=connection;
this.className=className;
if(className.getAnnotation(Table.class)!=null) this.queryString="select * from "+((Table)className.getAnnotation(Table.class)).name();
if(className.getAnnotation(View.class)!=null) this.queryString="select * from "+((View)className.getAnnotation(View.class)).name();
this.isWhereClauseApplied=false;
this.isOrderByApplied=false;
}

public List fire() throws DataException
{
try
{
List list=new LinkedList();
Statement statement=this.connection.createStatement();
ResultSet resultSet=statement.executeQuery(this.queryString);
Field fields[]=className.getDeclaredFields();
Object object;
while(resultSet.next())
{
object=className.newInstance();
String type;
String columnName;
for(Field field:fields)
{
columnName=((Column)field.getAnnotation(Column.class)).name();
type=field.getType().getSimpleName();
if(type.equals("int") || type.equals("Integer")) field.set(object,resultSet.getInt(columnName));
else if(type.equalsIgnoreCase("LONG")) field.set(object,resultSet.getLong(columnName));
else if(type.equalsIgnoreCase("Short")) field.set(object,resultSet.getShort(columnName));
else if(type.equalsIgnoreCase("Double")) field.set(object,resultSet.getDouble(columnName));
else if(type.equalsIgnoreCase("boolean")) field.set(object,resultSet.getBoolean(columnName));
else if(type.equalsIgnoreCase("float")) field.set(object,resultSet.getFloat(columnName));
else if(type.equalsIgnoreCase("byte")) field.set(object,resultSet.getByte(columnName));
else if(type.equalsIgnoreCase("String")) field.set(object,resultSet.getString(columnName));
else if(type.equalsIgnoreCase("Date")) field.set(object,resultSet.getDate(columnName));
}
list.add(object);
}
return list;
}catch(Exception exception)
{
throw new DataException(exception.getMessage());
}
} // fire function ends here

public Query where(String attribute) throws DataException
{
if(attribute==null || attribute.length()==0) throw new DataException("Argument cannot be null and length should be greater than zero while calling \'where\' function.");
Field fields[]=className.getDeclaredFields();
boolean found=false;
String columnName="";
for(Field field:fields)
{
if(field.getName().equalsIgnoreCase(attribute))
{
found=true;
columnName=((Column)field.getAnnotation(Column.class)).name();
break;
}
}
if(found==false) throw new DataException("Unable to find "+attribute+" property in class "+this.className.getSimpleName());
this.queryString=this.queryString+" where "+columnName;
this.isWhereClauseApplied=true;
return this;
} // where function ends here

public Query eq(Object value) throws DataException
{
if(!isWhereClauseApplied) throw new DataException("Cannot call eq method without where(String) method");
if(value==null) throw new DataException("Argument cannot be null.");
String type=value.getClass().getSimpleName();
if(type.equals("String")) this.queryString=this.queryString+"=\""+value.toString()+"\"";
else if(type.equals("Date")) this.queryString=this.queryString+"=\""+value.toString()+"\"";
else this.queryString=this.queryString+"="+value;
return this;
} // eq function ends here

public Query lt(Object value) throws DataException
{
if(!isWhereClauseApplied) throw new DataException("Cannot call lt method without where(String) method");
if(value==null) throw new DataException("Argument cannot be null.");
String type=value.getClass().getSimpleName();
if(type.equals("String")) this.queryString=this.queryString+"<\""+value.toString()+"\"";
else if(type.equals("Date")) this.queryString=this.queryString+"<\""+value.toString()+"\"";
else this.queryString=this.queryString+"<"+value;
return this;
} // lt function ends here

public Query gt(Object value) throws DataException
{
if(!isWhereClauseApplied) throw new DataException("Cannot call gt method without where(String) method");
if(value==null) throw new DataException("Argument cannot be null.");
String type=value.getClass().getSimpleName();
if(type.equals("String")) this.queryString=this.queryString+">\""+value.toString()+"\"";
else if(type.equals("Date")) this.queryString=this.queryString+">\""+value.toString()+"\"";
else this.queryString=this.queryString+">"+value;
return this;
} // gt function ends here

public Query le(Object value) throws DataException
{
if(!isWhereClauseApplied) throw new DataException("Cannot call le method without where(String) method");
if(value==null) throw new DataException("Argument cannot be null.");
String type=value.getClass().getSimpleName();
if(type.equals("String")) this.queryString=this.queryString+"<=\""+value.toString()+"\"";
else if(type.equals("Date")) this.queryString=this.queryString+"<=\""+value.toString()+"\"";
else this.queryString=this.queryString+"<="+value;
return this;
} // le function ends here

public Query ge(Object value) throws DataException
{
if(!isWhereClauseApplied) throw new DataException("Cannot call ge method without where(String) method");
if(value==null) throw new DataException("Argument cannot be null.");
String type=value.getClass().getSimpleName();
if(type.equals("String")) this.queryString=this.queryString+">=\""+value.toString()+"\"";
else if(type.equals("Date")) this.queryString=this.queryString+">=\""+value.toString()+"\"";
else this.queryString=this.queryString+">="+value;
return this;
} // ge function ends here

public Query ne(Object value) throws DataException
{
if(!isWhereClauseApplied) throw new DataException("Cannot call ne method without where(String) method");
if(value==null) throw new DataException("Argument cannot be null.");
String type=value.getClass().getSimpleName();
if(type.equals("String")) this.queryString=this.queryString+"!=\""+value.toString()+"\"";
else if(type.equals("Date")) this.queryString=this.queryString+"!=\""+value.toString()+"\"";
else this.queryString=this.queryString+"!="+value;
return this;
} // ne function ends here

public Query orderBy(String attribute) throws DataException
{
if(attribute==null || attribute.length()==0)throw new DataException("Argument cannot be null and length should be greater than zero while calling \'orderBy\' function.");
Field fields[]=className.getDeclaredFields();
boolean found=false;
String columnName="";
for(Field field:fields)
{
if(field.getName().equalsIgnoreCase(attribute))
{
found=true;
columnName=((Column)field.getAnnotation(Column.class)).name();
break;
}
}
if(found==false) throw new DataException("Unable to find "+attribute+" property in class "+this.className.getSimpleName());
this.isOrderByApplied=true;
this.queryString=this.queryString+" order by "+columnName;
return this;
} // orderBy function ends herex

public Query and(String attribute) throws DataException
{
if(attribute==null || attribute.length()==0)throw new DataException("Argument cannot be null and length should be greater than zero while calling \'and\' function.");
if(!isWhereClauseApplied) throw new DataException("Cannot call and method without where(String) method");
Field fields[]=className.getDeclaredFields();
boolean found=false;
String columnName="";
for(Field field:fields)
{
if(field.getName().equalsIgnoreCase(attribute))
{
found=true;
columnName=((Column)field.getAnnotation(Column.class)).name();
break;
}
}
if(found==false) throw new DataException("Unable to find "+attribute+" property in class "+this.className.getSimpleName());
this.queryString=this.queryString+" and "+columnName;
return this;
} // and function ends here

public Query or(String attribute) throws DataException
{
if(attribute==null || attribute.length()==0)throw new DataException("Argument cannot be null and length should be greater than zero while calling \'or\' function.");
if(!isWhereClauseApplied) throw new DataException("Cannot call or method without where(String) method");
Field fields[]=className.getDeclaredFields();
boolean found=false;
String columnName="";
for(Field field:fields)
{
if(field.getName().equalsIgnoreCase(attribute))
{
found=true;
columnName=((Column)field.getAnnotation(Column.class)).name();
break;
}
}
if(found==false) throw new DataException("Unable to find "+attribute+" property in class "+this.className.getSimpleName());
this.queryString=this.queryString+" or "+columnName;
return this;
} // or function ends here

public Query ascending() throws DataException
{
if(!isOrderByApplied) throw new DataException("Cannot call ascending method without orderBy(String) method");
else this.queryString=this.queryString+" asc";
return this;
} // ascending function ends here

public Query descending() throws DataException
{
if(!isOrderByApplied) throw new DataException("Cannot call descending method without orderBy(String) method");
else this.queryString=this.queryString+" desc";
return this;
} // descending function ends here


}