package com.thinking.machines.orm.utils;
public class DatabaseConfiguration
{
private String jdbc_driver;
private String connection_url;
private String username;
private String password;
private String package_name;
private String jar_file_name;
public DatabaseConfiguration()
{
this.jdbc_driver="";
this.connection_url="";
this.username="";
this.password="";
this.package_name="";
this.jar_file_name="";
}
public void setJdbc_driver(String jdbc_driver)
{
this.jdbc_driver=jdbc_driver;
}
public String getJdbc_driver()
{
return this.jdbc_driver;
}
public void setConnection_url(String connection_url)
{
this.connection_url=connection_url;
}
public String getConnection_url()
{
return this.connection_url;
}

public void setUsername(String username)
{
this.username=username;
}
public String getUsername()
{
return this.username;
}
public void setPassword(String password)
{
this.password=password;
}
public String getPassword()
{
return this.password;
}

public void setPackage_name(String package_name)
{
this.package_name=package_name;
}
public String getPackage_name()
{
return this.package_name;
}

public void setJar_file_name(String jar_file_name)
{
this.jar_file_name=jar_file_name;
}
public String getJar_file_name()
{
return this.jar_file_name;
}

}
