package com.thinking.machines.orm.utils;
import java.util.*;
import java.sql.*;
import java.lang.reflect.*;
public class QueryHandler
{
private PreparedStatement preparedStatement;
private List<Method> setterMethodsList;
private List<Method> getterMethodsList;
public QueryHandler()
{
preparedStatement=null;
setterMethodsList=new LinkedList<>();
getterMethodsList=new LinkedList<>();
}
public void setPreparedStatement(PreparedStatement preparedStatement)
{
this.preparedStatement=preparedStatement;
}
public PreparedStatement getPreparedStatement()
{
return this.preparedStatement;
}
public void addSetterMethod(Method method)
{
this.setterMethodsList.add(method);
}
public Method getSetterMethod(int i)
{
return this.setterMethodsList.get(i);
}
public void addGetterMethod(Method method)
{
this.getterMethodsList.add(method);
}
public Method getGetterMethod(int i)
{
return this.getterMethodsList.get(i);
}
public int getListSize()
{
return this.setterMethodsList.size();
}
}