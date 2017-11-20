// AddressBook.java
// Main activity for the Address Book app.
package au.edu.monash.fit2081.addressbook;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import au.edu.monash.fit2081.addressbook.R;

public class AddressBook extends ListActivity{ 		//NOTE: EXTENDS LISTACTIVITY not Activity
   public static final String ROW_ID = "row_id"; 	// a key for a value passed between activities during Intents
   private ListView contactListView; 				// reference for built-in ListView (set in onCreate)
   private CursorAdapter contactAdapter; 			// Adapter that exposes data from a Cursor to a ListView widget (Android Doco.)
   													// Cursor: This interface provides random read-write access to the result set returned by a database query (Android Doco.)
   
   // called when the activity is first created
   @Override
   public void onCreate(Bundle savedInstanceState){
      super.onCreate(savedInstanceState);
      
      // ListView is implicit so no need for layout or inflation (can be explicit if need to customise, some conventions must be followed though)

      contactListView = getListView(); 									// get ref to the built-in ListView from ListActivity inherited method
      contactListView.setOnItemClickListener(viewContactListener);		// set listener for when list item is clicked       

      // map each contact's name to a TextView in the ListView layout
      String[] from = { "name" ,"favourite"};			// String array of list column names (just one in this case)
      int[] to = { R.id.contactTextView , R.id.favouriteCheckBox};	// parallel array of widgets references to display each column's data (just one in this case)
      
      // params: context, list item's layout, Cursor (we will set it later), db column name array, widget reference array
      // next line: strikethrough indicates that this constructor is deprecated (i.e. still works but can't promise it will in the future)
      contactAdapter = new SimpleCursorAdapter(this, R.layout.contact_list_item, null, from, to);
      setListAdapter(contactAdapter); 		// connect list view and adapter using ListActivity inherited method
   }

   
   //executes each time the Activity regains the focus (including the first time) after being partially or fully hidden
   @Override
   protected void onResume(){ 
      super.onResume();
      
       // instantiate a new anonymous GetContactsTask object (it's an AsyncTask) and execute it
       // execute's doco indicates a variable number of parameters can be passed (here we pass none by casting null into an acceptable type)
       // btw these parameters turn up in the doInBackground method of the new task (GetContactsTask in this case)
       new GetContactsTask().execute((Object[]) null);	// GetContactsTask is an inner class defined below
    }

   // executes whenever the Activity is completely hidden (i.e. another Activity has the focus)
   @Override
   protected void onStop(){
      Cursor cursor = contactAdapter.getCursor(); // get contactAdapter's Cursor
      
      if (cursor != null) 
         cursor.deactivate(); 				// release cursor resources (deprecated requery() will make cursor valid again)
      	 //cursor.close();					// alternative (since requery() not used anywhere, see doco)
      
      contactAdapter.changeCursor(null); 	// set adapter's cursor to no cursor  
      
      super.onStop(); // REQUIRED - start or end of method, usually doesn't matter, Java says first, some community debate
   }

   
   // performs database query outside GUI thread
   // 3 generic types required by any AsyncTask - they are generic so totally flexible
   // First is the type of the data passed to the AsyncTask (not used here)
   //   this is the type of the calling execute method's actual parameter and the doInBackGround formal parameter, the result of the former is passed to the latter
   // Second is the type of progress units published during the doInBackground computation (not used here) e.g. to inform a progress bar
   // Third is the type of the AsyncTask's result
   //   this is the type of the return value of doInBackground (Cursor in this case) 
   
   // onPostExecute method executes in GUI thread after doInBackground completes execution outside the GUI thread
   // which allows the calling Activity to safely use the AsyncTask's results
   private class GetContactsTask extends AsyncTask<Object, Object, Cursor>{
      // context is "AddressBook.this" NOT "this" since we are an inner class
      DatabaseConnector databaseConnector = new DatabaseConnector(AddressBook.this); // see DatabaseConnector.java

      //Note no constructor so Java default operates

      //CURSOR DEFINITION: an object that can traverse (move first, next, previous etc.) [and provide access to] the results of a database query (i.e. the result set)
      //ANDROID CURSOR INTERFACE: This interface provides random read-write access to the result set returned by a database query
      
      @Override // executes in a separate thread (in this case it's a database access)
      protected Cursor doInBackground(Object... params){ // means any number of objects can be passed and accessed as params[0],params[1], again very flexible, they are called Var Args
         databaseConnector.open();

         // get a cursor containing all contacts
         return databaseConnector.getAllContacts(); //returns a Cursor 
      }

      
      @Override // executes in the GUI thread after doInBackground completes and is passed that methods return value (type Cursor in this case)
      protected void onPostExecute(Cursor result){
         contactAdapter.changeCursor(result); 	// set the adapter's Cursor, the adapter is already set to connect to the Activity's ListView
         										// does list populate now or is something else required?
         databaseConnector.close();
      }
   } //end inner class
      
   
   @Override // create the Activity's menu from a menu resource XML file
   public boolean onCreateOptionsMenu(Menu menu){
      super.onCreateOptionsMenu(menu);
      
      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.addressbook_menu, menu);
      
      return true;
   }
   
   
   @Override // handle choice from options menu - there is only 1 item
   public boolean onOptionsItemSelected(MenuItem item){
	  // launching another Activity in the same App - Easy
      Intent addNewContact = new Intent(AddressBook.this, AddEditContact.class);
      startActivity(addNewContact); // now start the intent we just created and named to actually launch the intent's target activity
      
      return super.onOptionsItemSelected(item); // REQUIRED - start or end of method, usually doesn't matter, Java says first, some community debate
   }

   // event listener that responds to the user touching a contact's name in the ListView
   OnItemClickListener viewContactListener = new OnItemClickListener(){
      @Override
      //params: ref to AdapterView containing clicked View (ListView is an AdapterView), AdapterView: a View whose children are determined by an Adapter
      // 		ref to the view within the AdapterView that was clicked  
      //		item index of View that was clicked 
      //		unique ID (value in column named "_id") of row associated with item that was clicked (here this will be row ID in the cursor i.e. contact ID)
      
      public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3){ // <?> = means unknown
         // create an Intent to launch the ViewContact Activity
         Intent viewContact = new Intent(AddressBook.this, ViewContact.class);
         
         
         viewContact.putExtra(ROW_ID, arg3); // pass the selected contact's row ID between Activities as an extra associated with the Intent
         startActivity(viewContact);
      }
   };
   
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
