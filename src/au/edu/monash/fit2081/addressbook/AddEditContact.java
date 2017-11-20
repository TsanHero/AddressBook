// AddEditContact.java
// Activity for adding a new entry to or  
// editing an existing entry in the address book.
package au.edu.monash.fit2081.addressbook;


import android.app.Activity;
import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import au.edu.monash.fit2081.addressbook.R;

public class AddEditContact extends Activity{
   private long rowID; // id of contact being edited, not set if adding
   
   // EditTexts references for contact information
   private EditText nameEditText;
   private EditText phoneEditText;
   private EditText emailEditText;
   private EditText streetEditText;
   private EditText cityEditText;
   private CheckBox favouriteEdit;
   
   // called when the Activity is first started
   @Override
   public void onCreate(Bundle savedInstanceState){
      super.onCreate(savedInstanceState);
      
      setContentView(R.layout.add_contact); // inflate the UI for ADD (i.e. with hints in EditTexts (overwrite with field values if this is an EDIT)

      // set EditText references
      nameEditText 		= (EditText) findViewById(R.id.nameEditText);
      emailEditText 	= (EditText) findViewById(R.id.emailEditText);
      phoneEditText 	= (EditText) findViewById(R.id.phoneEditText);
      streetEditText 	= (EditText) findViewById(R.id.streetEditText);
      cityEditText 		= (EditText) findViewById(R.id.cityEditText);
      favouriteEdit     =(CheckBox) findViewById(R.id.favouriteEditCheckBox);
      
      Bundle extras = getIntent().getExtras();

      if (extras != null){ // we came to edit not to add (add sends no intent extras, edit sends rowID + field values)
         rowID = extras.getLong("row_id");
         
         nameEditText.setText(extras.getString("name"));  
         emailEditText.setText(extras.getString("email"));
         favouriteEdit.setChecked(extras.getBoolean("favourite"));
         phoneEditText.setText(extras.getString("phone"));  
         streetEditText.setText(extras.getString("street"));  
         cityEditText.setText(extras.getString("city"));  
      }
      // else we came to add not edit
      	 // without data inserted fields will automatically show hints for an add
      
      // set event listener for the Save Contact Button
      Button saveContactButton = (Button) findViewById(R.id.saveContactButton);
      saveContactButton.setOnClickListener(saveContactButtonClicked);
   }

   
   // responds to event generated when user clicks the Save Contact button
   OnClickListener saveContactButtonClicked = new OnClickListener() 
   {
      @Override
      public void onClick(View v){
         if (nameEditText.getText().length() != 0){ // will only save if name at least is present
        	// none of AsyncTask's generic parameters are used
        	// nothing passed to doInBackground or returned from onPostExecute and no progress tracked
        	// why? a Save for ADD needs nothing passed and a Save for EDIT uses the class level rowID set in onCreate
            AsyncTask<Object, Object, Object> saveContactTask = new AsyncTask<Object, Object, Object>() 
               {
                  @Override
                  protected Object doInBackground(Object... params){
                     saveContact(); // save contact to the database NOT on main/GUI thread
                     return null; 	//see notes immediately above
                  }
      
                  @Override
                  protected void onPostExecute(Object result){
                	 makeSomeToast(); // just for a Toast example
                     finish(); // return to the previous Activity
                  }
               }; // end AsyncTask
               
            // save the contact to the database using a separate thread
            saveContactTask.execute((Object[]) null);            
         }
         else{
            // create a new AlertDialog Builder
            AlertDialog.Builder builder = new AlertDialog.Builder(AddEditContact.this); // need fully qualified Activity ref since in an inner class
      
            // set dialog title & message, and provide Button to dismiss
            builder.setTitle(R.string.errorTitle); 
            builder.setMessage(R.string.errorMessage);
            builder.setPositiveButton(R.string.errorButton, null); 
            builder.show(); // display the Dialog
         }
      }
   }; // end declaration of listener reference 

   
   // saves contact information to the database
   // no parameters required as ADD doesn't need them, and EDIT uses the class level rowID set in onCreate
   private void saveContact(){ //NOT on main/GUI thread
      // get DatabaseConnector to interact with the SQLite database
      DatabaseConnector databaseConnector = new DatabaseConnector(this);

      // db open and close for insert (add) and update (edit) done in called methods in DatabaseConnector class
      if (getIntent().getExtras() == null){  // this is an ADD (_id is an autoincrement field see DatabaseConnector class)
         databaseConnector.insertContact(
            nameEditText.getText().toString(),
            emailEditText.getText().toString(),
            ((favouriteEdit.isChecked()?"*":" ")),
            phoneEditText.getText().toString(), 
            streetEditText.getText().toString(),
            cityEditText.getText().toString());
      }
      else{ 								// this is an EDIT (need to pass a primary key - rowID)
         databaseConnector.updateContact(rowID,
            nameEditText.getText().toString(),
            emailEditText.getText().toString(),
            ((favouriteEdit.isChecked()?"*":" ")),
            phoneEditText.getText().toString(), 
            streetEditText.getText().toString(),
            cityEditText.getText().toString());
      }
      
   }
   
   private void makeSomeToast(){
	   
       // display a message indicating that the contact was saved
       Toast message = Toast.makeText(this, R.string.contact_saved, Toast.LENGTH_SHORT);
       // gravity centres top left hand of message so offsets required
       message.setGravity(Gravity.CENTER, message.getXOffset() / 2, message.getYOffset() / 2);
       message.show(); // display the Toast 	   
	   
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
