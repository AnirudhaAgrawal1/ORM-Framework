# ORM-Framework

*A java based framework for Object Relational Mapping. ORM-Framework is specifically designed for MySQL Database Management System.*

## Why to use this framework

- No need to write SQL statements for insertion,deletion,retrival and updation.

- No need to apply validations related to data.

- No need to create pojo's for the database tables, all the pojos will be created in the specified folder structure and jar file will be created of compiled pojo's.

- User only need to create a file named as config.json in the working directory that contains required information to establish connection with the database.

## Documentation

### Annotations

If the user solely depends upon the tools user need not to use or worry about the annotations. But if the user manually creates the pojo's user has to use the following annotations :

**Package :** com.thinking.machines.orm.annotations

|Annotation | Description |
| --- | ---|
| Table | This annotation is used with the class. The value of this annotation is the table_name the user wants to map to the class. |
| Column |  The annotation is used with the fields of the class. The value of this annotation is the column name of the table user wants to map with that field.|
| PrimaryKey |  Annotation used with the field that is mapped to the primary key of the table.|
| AutoIncrement | This annotation is used with the field mapped to a column which is auto incremented. |
| ForeignKey | Annotation used with the field that is mapped to the foreign key of the table.|
| View | This annotation is used with the class. The value of this annotation is the table_name(view name) the user wants to map to the class. |

### DataManager

**Package :** com.thinking.machines.orm
The connection is established and stored in the DataManager object with the necessary data structure to perform operations and validations.

**Note :**  User only perform retrival operation on 'view' user can not perform insertion,updation and deletion on views.

Following are the DataManager methods :


| Return Type | Method | Description |
| --- | --- | --- |
| DataManager | **getDataManager()** |   A static method returns the instance of the DataManager. The Framework uses **Singleton** design technique. |
| void    |   **begin()** |   To perform the below operations user need to call begin() method first, it stablish the connection with database. |
| void    |   **save(Object object)**   |   Does relational mapping of the received object and saves the information extracted into the database. |
| void   |   **delete(Object object)**  |   Delete the row from the database by looking for the field mapped to the primary key in the object. |
| void   |   **update(Object object)**   |   Updates all the details of the given object into the database. |
| Query |  **query(Class<?> tableClass)**  |   Returns the list of data retrival from the database. |
| void   | **end()**                |    Closes the connection with database. |

### Query

Necessary query statement with data related to the retrival operation stores in the Query object.

**Package :** com.thinking.machines.orm.utils

Following are the Query methods:

| Return Type | Method | Description |
| --- | --- | --- |
| Query  |    **where(String value)**   | Adds the 'where' clause to the query |
| Query  |      **eq(Object value)**    | Adds '=' followed by the parameter to the query.|
| Query   |     **le(Object value)**     | Adds '<=' followed by the parameter to the query.|
| Query   |     **lt(Object value)**     | Adds '<' followed by the parameter to the query.|
| Query   |     **gt(Object value)**     | Adds '>' followed by the parameter to the query.|
| Query   |    **ge(Object value)**    | Adds '>=' followed by the parameter to the query.|
| Query   |    **ne(Object value)**          | Adds '!=' followed by the parameter to the query.|
| Query   |     **orderBy(String value)**     | Adds the ORDERBY clause followed by the parameter to the query.|
| Query   |     **ascending()**             | Adds 'ASC' to the query.||
| Query   |     **descending()**            | Adds 'DESC' to the query.||
| Query   |     **and(String value)**         | Adds the AND  followed by the parameter to the query.|
| Query   |     **or(String value)**          | Adds the OR followed by the parameter to the query.|
| ```java.util.List```     |     **fire()**                 | To execute the query. |


### Exception

**DataException**

**package :** com.thinking.machines.orm.exceptions

An exception that provide information related to accessing or setting up the database,validation of data arrived to perform some operation.

## How to use this framework (Tutorial)

### Setting up

Download the jar files from the dist folder and put it into the working directory or in the lib folder.

Then the user need to create a file named as config.json in the working directory with the information related to database,packaging structure and jar file.

config.json file should be created as shown below :-

```
{
"jdbc-driver":"com.mysql.cj.jdbc.Driver",
"connection-url":"jdbc:mysql://localhost:3306/",
"database":"tmschool",
"username":"tm",
"password":"tm",
"package-name":"com.thinking.machines.myApplication.pojo",
"jar-file-name":"pojos.jar"
}
```
### Creating POJO's

To create POJO's user need to write the following code

```
import com.thinking.machines.orm.utils.*;
class GeneratePOJO
{
public static void main(String gg[])
{
try
{
POJOCreator.createPOJOs();
}catch(DataException dataException)
{
System.out.println(dataException);
}
}
}
```
### Perform operations
- Insertion

```
import java.text.*;
import java.util.*;
import com.thinking.machines.orm.exceptions.*;
import com.thinking.machines.orm.annotations.*;
import com.thinking.machines.orm.*;
import com.thinking.machines.myApplication.pojo.*;
class SchoolManager
{
public static void main(String gg[])
{
try
{
DataManager dataManager=DataManager.getDataManager();
Course c=new Course();
c.title="Machine Learning";
dataManager.begin();
dataManager.save(c);
SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
Student s=new Student();
s.firstName="Anirudha";
s.lastName="Agrawal";
s.rollNumber=001;
s.dateOfBirth=format. parse("24-07-2001");
s.addharCardNumber="abcd";
s.gender="M";
s.courseCode=1;
dataManager.begin();
dataManager.save(s);
}catch(DataException dataExeption)
{
System.out.println(dataException);
}
}
}
```
- Updation

```
import java.text.*;
import java.util.*;
import com.thinking.machines.orm.exceptions.*;
import com.thinking.machines.orm.annotations.*;
import com.thinking.machines.orm.*;
import com.thinking.machines.myApplication.pojo.*;

class SchoolManager
{
public static void main(String gg[])
{
try
{
DataManager dataManager=DataManager.getDataManager();

SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
Student s=new Student();
s.firstName="Amit";
s.lastName="Jain";
s.rollNumber=001;
s.dateOfBirth=format. parse("2001-07-24");
s.addharCardNumber="abcf";
s.gender="M";
s.courseCode=2;
dataManager.begin();
dataManager.update(s);
dataManager.end();

}catch(DataException dataException)
{
System.out.println(dataException);
}
}
}
```
- Deletion

```
import java.text.*;
import java.util.*;
import com.thinking.machines.orm.exceptions.*;
import com.thinking.machines.orm.annotations.*;
import com.thinking.machines.orm.*;
import com.thinking.machines.myApplication.pojo.*;

class SchoolManager
{
public static void main(String gg[])
{
try
{
DataManager dataManager=DataManager.getDataManager();

Course c=new Course();
c.code=4;
dataManager.begin();
dataManager.delete(c);
dataManager.end();

}catch(DataException dataException)
{
System.out.println(dataException);
}
}
}
```
- Retrival

```
import java.text.*;
import java.util.*;
import com.thinking.machines.orm.exceptions.*;
import com.thinking.machines.orm.annotations.*;
import com.thinking.machines.orm.*;
import com.thinking.machines.myApplication.pojo.*;

class SchoolManager
{
public static void main(String gg[])
{
try
{
DataManager dataManager=DataManager.getDataManager();
dataManager.begin();
List<Student> list=dataManager.query(Student.class).where("rollNumber").eq(1).fire();
for(Student student:list)
{
	System.out.println("roll_no::"+student.rollNumber+"   ,Name::"+student.firstName+"   ,gender::"+student.gender);
}
}catch(DataException dataException)
{
System.out.println(dataExcepion);
}
}
}
```
