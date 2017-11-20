// DatabaseConnector.java
// Provides easy connection and creation of UserContacts database.
package au.edu.monash.fit2081.addressbook;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public class DatabaseConnector{
   // database name
   private static final String DATABASE_NAME = "UserContacts";
   private SQLiteDatabase database; 				// database object
   private DatabaseOpenHelper databaseOpenHelper; 	// database helper

   // public constructor for DatabaseConnector - the other classes (they are all Activities) create one every time they need a db access
   public DatabaseConnector(Context context){
      // create a new DatabaseOpenHelper (its an inner class coded below that extends SQLiteOpenHelper, see its constructor for this constructors parameter semantics)
      databaseOpenHelper = new DatabaseOpenHelper(context, DATABASE_NAME, null, 1);
   }

   // open the database connection
   public void open() throws SQLException{ //any error in the method will cause the specified (imported) exception
      // get a reference to a SQLiteDatabase object which we can use to maintain the database's data
	  // this statement either opens an existing database (with name DATABASE_NAME) or creates a new one with DatabaseOpenHelper.onCreate(...) 
      database = databaseOpenHelper.getWritableDatabase(); // method inherited from SQLiteOpenHelper which DatabaseOpenHelper extends 
   }

   // close the database connection
   public void close(){
      if (database != null)
         database.close(); //inherited from SQLiteOpenHelper which DatabaseOpenHelper extends
   }

   
   // insert (Add), update (Edit) and delete do not require any display so no cursor returned (in either case there is a return to the "intenting" Activity as soon as Save/Delete(after confirm dialog) button pressed
   // inserts a new contact in the database
   public void insertContact(String name, String email, String favourite, String phone, String state, String city){
      ContentValues newContact = new ContentValues(); // required as parameter type by SQLiteDatabase.insert(...) - key/value data structure
      
      newContact.put("name", name);
      newContact.put("email", email);
      newContact.put("favourite",favourite);
      newContact.put("phone", phone);
      newContact.put("street", state);
      newContact.put("city", city);

      open(); 											// open()coded above
      database.insert("contacts", null, newContact);	// parameters: table, not used here (has to do with inserting empty records), ContentValues object
      close(); 											// close() coded above
   }

   // updates an existing contact in the database
   public void updateContact(long id, String name, String email, String favourite, String phone, String state, String city){
      ContentValues editContact = new ContentValues(); // required as parameter type by SQLiteDatabase.update(...) - key/value data structure
      
      editContact.put("name", name);
      editContact.put("email", email);
      editContact.put("favourite",favourite);
      editContact.put("phone", phone);
      editContact.put("street", state);
      editContact.put("city", city);

      open();
      database.update("contacts", editContact, "_id=" + id, null); // parameters: table, ContentValues object, where clause, where arguments (not used here but allows compound where conditions)
      close();
   }

   // delete the contact specified by the given String name
   public void deleteContact(long id){
      open();
      // parameters: table, where clause without where, ... 
      database.delete("contacts", "_id=" + id, null); // parameters: 
      close();
   }

   // viewing all or just one contact require data to be returned (via a cursor) to the call point for display
   // return a Cursor with all contact information in the database
   public Cursor getAllContacts(){
	  // parameters: table, columns in a String array, ... other SQL SELECT statement stuff 
      return database.query("contacts", new String[] {"_id", "name","favourite"}, null, null, null, null, "favourite");
   }

   // get a Cursor containing all information about the contact specified by the given id
   public Cursor getOneContact(long id){
	  // parameters: table, null = return all columns, where clause without where,  ... other SQL SELECT statement stuff 
      return database.query("contacts", null, "_id=" + id, null, null, null,null);
   }

   
   private class DatabaseOpenHelper extends SQLiteOpenHelper{

      public DatabaseOpenHelper(Context context, String name, CursorFactory factory, int version){
    	 // if a db schema version higher than the one on the device is supplied onUpgrade will run to upgrade the schema appropriately (which we must code of course)
    	 // so new versions of the App using new schema versions of the db will be able to update the db schema and will function correctly (without data loss) 
         super(context, name, factory, version); // parameters: from constructor call (context, DATABASE_NAME, null, 1)
         										 // null = use the default cursor factory, 1 = version 1 of the database wrt structure (schema) NOT data
      }

      // creates the contacts table when the database is created
      @Override //required
      public void onCreate(SQLiteDatabase db){ //only executes if db does not already exist
         // query to create a new table named contacts
    	 // the naming of the column _id is important since this specifies the record id used elsewhere when accessing the table
         String createQuery = 
        	"CREATE TABLE contacts" +
            "(_id integer primary key autoincrement," +
            "name TEXT, email TEXT, favourite TEXT, phone TEXT," +
            "street TEXT, city TEXT);";
                  
         db.execSQL(createQuery);
      }

      @Override //required
      public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
          //code here will update the db schema if version number supplied in constructor
          //is greater than that currently stored in the db (which will also be updated)
          //no data loss during schema change except that associated with deleted schema elements
      }
      
   }
}

/**************************************************************************
 * (C) Copyright 1992-2012 by Deitel & Associates, Inc. and               *
 * Pearson Education, Inc. All Rights Reserved.                           *
 *                                                                        *
 * DISCLAIMER: The authors and publisher of this book have used their     *
 * best efforts in preparing the book. These efforts include the          *
 * development, research, and testing of the theories and programs        *
 * to determine their effectiveness. The authors and publisher make       *
 * no warranty of any kind, expressed or implied, with regard to these    *
 * programs or to the documentation contained in these books. The authors *
 * and publisher shall not be liable in any event for incidental or       *
 * consequential damages in connection with, or arising out of, the       *
 * furnishing, performance, or use of these programs.                     *
 **************************************************************************/
