/**
 * Copyright (c) 2011, SOCIETIES Consortium (WATERFORD INSTITUTE OF TECHNOLOGY (TSSG), HERIOT-WATT UNIVERSITY (HWU), SOLUTA.NET 
 * (SN), GERMAN AEROSPACE CENTRE (Deutsches Zentrum fuer Luft- und Raumfahrt e.V.) (DLR), Zavod za varnostne tehnologije
 * informacijske družbe in elektronsko poslovanje (SETCCE), INSTITUTE OF COMMUNICATION AND COMPUTER SYSTEMS (ICCS), LAKE
 * COMMUNICATIONS (LAKE), INTEL PERFORMANCE LEARNING SOLUTIONS LTD (INTEL), PORTUGAL TELECOM INOVAÇÃO, SA (PTIN), IBM Corp., 
 * INSTITUT TELECOM (ITSUD), AMITEC DIACHYTI EFYIA PLIROFORIKI KAI EPIKINONIES ETERIA PERIORISMENIS EFTHINIS (AMITEC), TELECOM 
 * ITALIA S.p.a.(TI),  TRIALOG (TRIALOG), Stiftelsen SINTEF (SINTEF), NEC EUROPE LTD (NEC))
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
 * conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 *    disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT 
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.societies.thirdpartyservices.idisaster;

import java.util.ArrayList;

import org.societies.android.api.cis.SocialContract;
import org.societies.thirdpartyservices.idisaster.R;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This activity allows the users to manage the activity feed in 
 * a selected disaster team (a disaster team the user is member of).
 * 
 * @author Jacqueline.Floch@sintef.no
 *
 */
public class FeedListActivity extends ListActivity {
	
	ContentResolver resolver;
	Cursor feedCursor;			// used for the activities in the feed
	int feeds;					// keep track of number of feeds

	
	ArrayAdapter<String> feedAdapter;
	ListView listView;

 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	
    	super.onCreate(savedInstanceState);
    	setContentView (R.layout.feed_list_layout);
    	listView = getListView();
    	resolver = getContentResolver();
    	
    	//TODO: customize the layout for the row is necessary
    	// At the moment a simple string is used as for disaster.

		if (iDisasterApplication.testDataUsed) {			// Test data
															// TODO: check this: The adapter should not be set in onResume since the Android Adapter 
															// mechanism is used for update? See DisasterCreateactivity.
			iDisasterApplication.getInstance().feedAdapter = new ArrayAdapter<String> (this,
					R.layout.disaster_list_item, R.id.disaster_item, iDisasterApplication.getInstance().feedContentList);
	    	// Assign adapter to ListView
	    	listView.setAdapter(iDisasterApplication.getInstance().feedAdapter);
	    }


    	// Add listener for short click.
    	// 
    	listView.setOnItemClickListener(new OnItemClickListener() {
    		public void onItemClick (AdapterView<?> parent, View view,
    			int position, long id) {
    			if (iDisasterApplication.testDataUsed) {
// TODO: Remove code for testing the correct setting of preferences 
    			Toast.makeText(getApplicationContext(),
    				"Click ListItem Number   " + (position+1) + "   " + iDisasterApplication.getInstance().feedContentList.get (position), Toast.LENGTH_LONG)
    				.show();
    			} else {
// TODO: Eventually start new activity if something more to show about the feed.
    				
				}
// The activity is kept on stack (check also that "noHistory" is not set in Manifest)
// Should it be removed?
//    			finish();
    			}
    		});
       	
//TODO:  Add listener for long click
    	// listView.setOnItemLongClickListener(new DrawPopup());

    } // onCreate

/**
 * onResume is called at start of the active lifetime.
 * The list of feeds is retrieved from SocialProvider and assigned to 
 * view.
 * The data are fetched each time the activity becomes visible as these data
 * may be changed by other users (info fetched from the Cloud)
 */
        
	@Override
    protected void onResume() {
		super.onResume();
		
		// Test data are set in onCreate - see explanation above
		
    	if (! iDisasterApplication.testDataUsed) {
    		if (feedAdapter!= null) feedAdapter.clear();
    		getFeed();
    		assignAdapter ();
    			
			Toast.makeText(getApplicationContext(),
    				"Bug when getting data from Social Provider: the implementation of this activity is not complete", Toast.LENGTH_LONG /*Toast.LENGTH_SHORT*/ )
    				.show();
			}

//		// Create dialog if no feed in disaster team						
//  	AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
//  	alertBuilder.setMessage(getString(R.string.feedListDialogCIS))
//  		.setCancelable(false)
//  		.setPositiveButton (getString(R.string.dialogOK), new DialogInterface.OnClickListener() {
//  			public void onClick(DialogInterface dialog, int id) {
//  				// add code
//  				return;
//  			}
//  		});
//	    AlertDialog alert = alertBuilder.create();
//	    alert.show();
//	    return;

    }

/**
 * onPause releases all data.
 * Called when resuming a previous activity (for instance using back button)
 */		       
	@Override
	protected void onPause() {
		
		super.onPause ();
		
// TODO: check the following:
	// When using managedQuery(), the activity keeps a reference to the cursor and close it
    // whenever needed (in onDestroy() for instance.) 
    // When using a contentResolver's query(), the developer has to manage the cursor as a sensitive
    // resource. If you forget, for instance, to close() it in onDestroy(), you will leak 
    // underlying resources (logcat will warn you about it.)
    //

//		feedCursor.close();
	}


/**
 * getFeed retrieves the list of activity feeds for the selected disaster team
 * from Social Provider.
 */
	private void getFeed () {
//		Uri communityActivityUri = SocialContract.CommunityActivity.CONTENT_URI;

//TODO: remove this Uri - waiting for new version of Social Provider
			Uri communityActivityUri = Uri.parse ("content://org.societies.android.SocialProvider/communities_activity");
		
		String[] communityActivityprojection = new String[] {
// TODO: Find out what you be retrieved
			SocialContract.CommunityActivity.GLOBAL_ID_OBJECT	// ?? No content?
//		, SocialContract.CommunityActivity.+++
		};		


//TODO: Add a check on selected community? 
		String communityActivitySelection = SocialContract.CommunityActivity.GLOBAL_ID_FEED_OWNER + "= ?";
		String[] communityActivitySelectionArgs = new String[] {iDisasterApplication.getInstance().
														selectedTeam.globalId};		// The feed belongs to the CIS

//TODO: Classify according to date
		String communityActivitysortOrder = null;

//TODO: Remove comment - currently bug in SOcialProvider
//		feedCursor = resolver.query(communityActivityUri, communityActivityprojection,
//									communityActivitySelection, communityActivitySelectionArgs,
//									communityActivitysortOrder);
		feedCursor =null;

	}


/**
 * assignAdapter assigns data to display to adapter and adapter to view.
 */
	private void assignAdapter () {
			
		feeds= 0;
			
		ArrayList<String> feedList = new ArrayList<String> ();

//		An empty List will be assigned to Adapter
//		if (feedCursor == null) {

//		An empty List will be assigned to Adapter
//			if (feedCursor == null) {
//				if (feedCursor.getCount() == 0) {
			
		if (feedCursor != null) {
			if (feedCursor.getCount() != 0) {
				while (feedCursor.moveToNext()) {
					feeds++;
					String displayName = feedCursor.getString(feedCursor
							.getColumnIndex(SocialContract.CommunityActivity.GLOBAL_ID_OBJECT));
					feedList.add (displayName);
				}
			}
		}
	    feedAdapter = new ArrayAdapter<String> (this,
			R.layout.disaster_list_item, R.id.disaster_item, feedList);
			
		listView.setAdapter(feedAdapter);

	}
	
	
/**
 * onCreateOptionsMenu expands the activity menu for this activity tab.
*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu){

    	//The FIXED menu is set by the TabActivity.
// I am uncertain why the call to the super class leads to the creation
// of the fixed menu set by the TabActivity (DisasterActivity)
    	super.onCreateOptionsMenu(menu);
    	
    	menu.setGroupVisible(R.id.disasterMenuFeed, true);
    	return true;
    }

 /**
  * onOptionsItemSelected handles the selection of an item in the activity menu.
  */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

		// The TabActivity handles items in the FIXED menu.
// I am uncertain why the call to the super class leads to handling
// of a command in the fixed menu by the TabActivity (DisasterActivity)
    	super.onOptionsItemSelected(item);
    	
    	switch (item.getItemId()) {

    		case R.id.disasterMenuAddFeed:    			
//TODO: Remove code for testing the correct setting of preferences 
//    			Toast.makeText(getApplicationContext(),
//    				"Menu item chosen: Add feed", Toast.LENGTH_LONG)
//    				.show();
    			startActivity(new Intent(FeedListActivity.this, FeedAddActivity.class));
    		break;
    		
    		default:
    		break;
    	}
    	return true;
    }

    
    
}
