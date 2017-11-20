// ViewContact.java
// Activity for viewing a single contact.
package au.edu.monash.fit2081.addressbook;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.TextView;

public class ViewContact extends Activity{
   private long rowID; // selected contact's name
   private TextView nameTextView; // displays contact's name 
   private TextView phoneTextView; // displays contact's phone
   private TextView emailTextView; // displays contact's email
   private CheckBox favouriteCheckBox;
   private TextView streetTextView; // displays contact's street
   private TextView cityTextView; // displays contact's city/state/zip

   // called when the activity is first created
   @Override
   public void onCreate(Bundle savedInstanceState){
      super.onCreate(savedInstanceState);
      
      setContentView(R.layout.view_contact);

      // get the TextView refs
      nameTextView 		= (TextView) findViewById(R.id.nameTextView);
      phoneTextView 	= (TextView) findViewById(R.id.phoneTextView);
      emailTextView 	= (TextView) findViewById(R.id.emailTextView);
      favouriteCheckBox =(CheckBox) findViewById(R.id.favouriteCheckBox);
      streetTextView 	= (TextView) findViewById(R.id.streetTextView);
      cityTextView 		= (TextView) findViewById(R.id.cityTextView);
      
      // get the selected contact's unique row ID passed with the Intent that launched this Activity
      Bundle extras = getIntent().getExtras();
      rowID = extras.getLong(AddressBook.ROW_ID); 
   }

   
   //executes each time the Activity regains the focus (including the first time) after being partially or fully hidden
   @Override
   protected void onResume(){
      super.onResume();
      
      // instantiate a new anonymous LoadContactTask (it's an AsyncTask) and execute it
      new LoadContactTask().execute(rowID); //rowID is passed and becomes input parameter to doInBackground
   }
   
   
   // REFER to notes for AsyncTask in AddressBook.java
   // performs database query outside GUI thread
   // first generic type is long since execute passes a long to doInBackground, second not used, third Cursor again
   private class LoadContactTask extends AsyncTask<Long, Object, Cursor>{
      DatabaseConnector databaseConnector = new DatabaseConnector(ViewContact.this);

      // perform the database access
      @Override
      protected Cursor doInBackground(Long... params){ //any number of longs
         databaseConnector.open();
         
         // get a cursor containing all data on given entry
         return databaseConnector.getOneContact(params[0]); //first and only long passed
      }

      // use the Cursor returned from the doInBackground method
      @Override
      protected void onPostExecute(Cursor result){ // executes on the GUI thread after doInBackground completes
         super.onPostExecute(result);
   
         result.moveToFirst(); // move to the first (and only) row, cursor is before the first row initially
   
         // get the column index for each data item
         int nameIndex 		= result.getColumnIndex("name");
         int phoneIndex 	= result.getColumnIndex("phone");
         int emailIndex 	= result.getColumnIndex("email");
         int favouriteIndex = result.getColumnIndex("favourite");
         int streetIndex 	= result.getColumnIndex("street");
         int cityIndex 		= result.getColumnIndex("city");
         boolean checked = (result.getInt(favouriteIndex)==1);
         // fill TextViews with the retrieved data
         nameTextView.setText(result.getString(nameIndex));
         phoneTextView.setText(result.getString(phoneIndex));
         emailTextView.setText(result.getString(emailIndex));
//         if(checked==true)
//            favouriteCheckBox.setChecked(!favouriteCheckBox.isChecked());
//         else
//            favouriteCheckBox.setChecked(false);
         favouriteCheckBox.setChecked(checked);

         streetTextView.setText(result.getString(streetIndex));
         cityTextView.setText(result.getString(cityIndex));
   
         result.close(); // close the result cursor
         databaseConnector.close();
      }
   } // end inner class
      
   // create the Activity's menu from a menu resource XML file
   @Override
   public boolean onCreateOptionsMenu(Menu menu){
      super.onCreateOptionsMenu(menu);
      
      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.view_contact_menu, menu);
      
      return true;
   }
   
   // handle choice from options menu
   // code style is poor, switch should contain breaks and a retVal variable should be used so there is only 1 return statement
   @Override
   public boolean onOptionsItemSelected(MenuItem item){
      switch (item.getItemId()){
      
         case R.id.editItem:
            // create an Intent to launch the AddEditContact Activity
            Intent addEditContact = new Intent(this, AddEditContact.class);
            
            // pass the selected contact's data as extras with the Intent
            addEditContact.putExtra(AddressBook.ROW_ID, rowID); //ROW_ID is public, rowID is class level in this class and set during onCreate
            
            addEditContact.putExtra("name", nameTextView.getText());
            addEditContact.putExtra("phone", phoneTextView.getText());
            addEditContact.putExtra("email", emailTextView.getText());
            addEditContact.putExtra("favourite",favouriteCheckBox.isChecked());
            addEditContact.putExtra("street", streetTextView.getText());
            addEditContact.putExtra("city", cityTextView.getText());
            
            startActivity(addEditContact); // start the Activity
            
            return true; // handled the event
         case R.id.deleteItem:
            deleteContact();
            return true; // handled the event
            
         default:
            return super.onOptionsItemSelected(item); // because we didn't handle the event
      }
   }
   
   // delete a contact
   private void deleteContact(){
      // create a new AlertDialog Builder
      AlertDialog.Builder builder = new AlertDialog.Builder(ViewContact.this);

      builder.setTitle(R.string.confirmTitle);
      builder.setMessage(R.string.confirmMessage);

      // provide an OK button that deletes asynchronously while dismissing the dialog
      builder.setPositiveButton(R.string.button_delete, new DialogInterface.OnClickListener()
         {
            @Override
            public void onClick(DialogInterface dialog, int button){
               // get a DatabaseConnector reference for use in AsyncTask (lack of closure in Java requires final) 
               final DatabaseConnector databaseConnector = new DatabaseConnector(ViewContact.this);

               // create an AsyncTask that deletes the contact in another thread, then calls finish after the deletion to return to Activity the "intented" this one 
               // here for the first time we are declaring and instantiating an AsyncTask object in the same statement
               AsyncTask<Long, Object, Object> deleteTask = new AsyncTask<Long, Object, Object>(){
                     @Override
                     protected Object doInBackground(Long... params){
                        databaseConnector.deleteContact(params[0]); 
                        return null;
                     }

                     @Override
                     protected void onPostExecute(Object result){
                        finish(); // finish this Activity and return to the Activity which Intent(ed) this one i.e. the AddressBook Activity
                     }
                     
                  }; // end AsyncTask subclass definition/instantiation (JLS SE7 15.9.1)

               // execute the AsyncTask defined and instantiated above to delete contact at rowID
               deleteTask.execute(rowID); //one and only long. rowID is class level and set in onCreate               
            }
         } // end anonymous inner class that defines an instantiates the listener for positive button clicks
      
      ); // end listener ref declaration
      
      builder.setNegativeButton(R.string.button_cancel, null); //just dismiss dialog box (no delete)
      
      builder.show();
   } // end method deleteContact
   
} // end class ViewContact


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
