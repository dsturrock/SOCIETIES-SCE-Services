/*
 * 
 */
package com.asocom.activities;

import com.asocom.components.Menu;
import com.asocom.components.Options;
import com.asocom.components.CommunityProfile;
import com.asocom.model.Manager;
import com.asocom.tools.NonExistentCommunityException;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;

// TODO: Auto-generated Javadoc
/**
 * The Class CommunityProfileActivity.
 */
public class CommunityProfileActivity extends Activity implements
		View.OnClickListener {

	/** The menu. */
	private Menu menu;
	
	/** The CONTEX t_ men u_ id. */
	private final int CONTEXT_MENU_ID = 7;

	//
	/** The Constant ACTIVITY_NAME. */
	private static final String ACTIVITY_NAME = "CommunityProfileActivity";

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_void);

		LinearLayout topLayout = (LinearLayout) findViewById(R.id.main_void_topLayout);
		LinearLayout layout = (LinearLayout) findViewById(R.id.main_void_layout);
		//
		menu = new Menu(this);
		topLayout.addView(menu);

		try {

			Options options = new Options(this);
			options.setImageStatus(Manager.getCurrentStatus());
			options.setTextTop(Manager.getCommunities()
					.get(Manager.getCurrentCommunityPos()).getCommunityName());
			options.setImageDescription(Manager.getCommunities()
					.get(Manager.getCurrentCommunityPos()).getImage());
			options.setTextDescription(Manager.getCommunities()
					.get(Manager.getCurrentCommunityPos()).getDescription());
			options.getButton03()
					.setBackgroundResource(R.drawable.list_menu_02);
			options.getButton01().setBackgroundResource(R.drawable.list_menu);
			options.getButton01().setOnClickListener(this);
			topLayout.addView(options);

			CommunityProfile pc = new CommunityProfile(this);
			pc.setName(Manager.getCommunities()
					.get(Manager.getCurrentCommunityPos()).getCommunityName());
			pc.setDescription(Manager.getCommunities()
					.get(Manager.getCurrentCommunityPos()).getDescription());
			pc.setAdministrator(Manager.getCommunities()
					.get(Manager.getCurrentCommunityPos())
					.getNameAdministrator());
			pc.setCategory(Manager.getCommunities()
					.get(Manager.getCurrentCommunityPos()).getProfile());
			pc.setVisibility(Manager.getCommunities()
					.get(Manager.getCurrentCommunityPos()).getVisibility());
			pc.setDate(Manager.getCommunities()
					.get(Manager.getCurrentCommunityPos()).getDateOfCreation());
			pc.setMembers(Manager.getCommunities()
					.get(Manager.getCurrentCommunityPos()).getUserList().size()
					+ "");
			layout.addView(pc);
		} catch (NonExistentCommunityException e) {
			finish();
			Intent i = new Intent(this, HomeMenu.class);
			startActivity(i);
		}
		//
		Manager.setNameCurrentActivity(ACTIVITY_NAME);
		Manager.setCurrentActivity(this);
	}

	/* (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	public void onClick(View v) {
		if (v.getId() == R.id.options_button_01) {
			Intent i = new Intent(this, CommunityOptions.class);
			startActivity(i);
			finish();
			return;
		}
		menu.onClick(v);
	}

	/**
	 * create context menu.
	 *
	 * @param id the id
	 * @return the dialog
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == CONTEXT_MENU_ID)
			return menu.getIconContextMenu().createMenu("Select status");
		return super.onCreateDialog(id);
	}

	//
	/* (non-Javadoc)
	 * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
	 */
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent homeMenu = new Intent(this, HomeMenu.class);
			startActivity(homeMenu);
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	/* (non-Javadoc)
	 * @see android.content.ContextWrapper#clearWallpaper()
	 */
	public void clearWallpaper() {

	}

	//
	/**
	 * Gets the activity name.
	 *
	 * @return the activity name
	 */
	public static String getActivityName() {
		return ACTIVITY_NAME;
	}

}