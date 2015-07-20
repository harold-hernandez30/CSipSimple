/**
 * Copyright (C) 2010-2012 Regis Montoya (aka r3gis - www.r3gis.fr)
 * This file is part of CSipSimple.
 * <p/>
 * CSipSimple is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * If you own a pjsip commercial license you can also redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public License
 * as an android library.
 * <p/>
 * CSipSimple is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with CSipSimple.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.csipsimple.ui.account;


import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.BaseColumns;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.csipsimple.R;
import com.csipsimple.api.ISipService;
import com.csipsimple.api.SipProfile;
import com.csipsimple.ui.account.AccountsEditListAdapter.AccountRowTag;
import com.csipsimple.ui.account.AccountsEditListAdapter.OnCheckedRowListener;
import com.csipsimple.widgets.CSSListFragment;
import com.csipsimple.wizards.BasePrefsWizard;
import com.csipsimple.wizards.WizardChooser;
import com.csipsimple.wizards.WizardUtils;

public class AccountsEditListFragment extends CSSListFragment implements /*OnQuitListener,*/ OnCheckedRowListener {

    private boolean dualPane;
    private Long curCheckPosition = SipProfile.INVALID_ID;
    //private String curCheckWizard = WizardUtils.EXPERT_WIZARD_TAG;
    private final Handler mHandler = new Handler();
    private AccountStatusContentObserver statusObserver = null;
    private View mHeaderView;
    private AccountsEditListAdapter mAdapter;
    private ISipService service;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            service = ISipService.Stub.asInterface(arg1);
            if (service != null) {

                android.util.Log.d("AccountsEditList", "onServiceConnected");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            service = null;
        }
    };

    class AccountStatusContentObserver extends ContentObserver {

        public AccountStatusContentObserver(Handler h) {
            super(h);
        }

        public void onChange(boolean selfChange) {
            android.util.Log.d("ACCOUNT_LIST_FRAG", "Accounts status.onChange( " + selfChange + ")");
            ((BaseAdapter) getListAdapter()).notifyDataSetChanged();
        }
    }


    private final static String CURRENT_CHOICE = "curChoice";
    //private final static String CURRENT_WIZARD = "curWizard";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(connection != null) {
//            getActivity().unbindService(connection);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ListView lv = getListView();

        //getListView().setSelector(R.drawable.transparent);
        lv.setCacheColorHint(Color.TRANSPARENT);


        // View management
        View detailsFrame = getActivity().findViewById(R.id.details);
        dualPane = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;

//        getActivity().bindService(new Intent(getActivity(), SipService.class), connection, Context.BIND_AUTO_CREATE);

        if (savedInstanceState != null) {
            // Restore last state for checked position.
            curCheckPosition = savedInstanceState.getLong(CURRENT_CHOICE, SipProfile.INVALID_ID);
            //curCheckWizard = savedInstanceState.getString(CURRENT_WIZARD);
        }


//        setEmptyText("Test");
        setListShown(false);
        if (mAdapter == null) {
            if (mHeaderView != null) {
                lv.addHeaderView(mHeaderView, null, true);
            }
            mAdapter = new AccountsEditListAdapter(getActivity(), null);
            mAdapter.setOnCheckedRowListener(this);
            //getListView().setEmptyView(getActivity().findViewById(R.id.progress_container));
            //getActivity().findViewById(android.R.id.empty).setVisibility(View.GONE);
            setListAdapter(mAdapter);
            registerForContextMenu(lv);

            // Prepare the loader.  Either re-connect with an existing one,
            // or start a new one.
            getLoaderManager().initLoader(0, null, this);

            lv.setVerticalFadingEdgeEnabled(true);
        }

        if (dualPane) {
            // In dual-pane mode, the list view highlights the selected item.
            Log.d("lp", "dual pane mode");
            lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            //lv.setVerticalScrollbarPosition(View.SCROLLBAR_POSITION_LEFT);
            lv.setVerticalScrollBarEnabled(false);
            lv.setFadingEdgeLength(50);

            updateCheckedItem();
            // Make sure our UI is in the correct state.
            //showDetails(curCheckPosition, curCheckWizard);
        } else {
            //getListView().setVerticalScrollbarPosition(View.SCROLLBAR_POSITION_RIGHT);
            lv.setVerticalScrollBarEnabled(true);
            lv.setFadingEdgeLength(100);
        }

    }

    private static final int CHOOSE_WIZARD = 0;
    private static final int CHANGE_WIZARD = 1;

    // Menu stuff
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    private static final String THIS_FILE = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.accounts_edit_list, container, false);
        return v;
    }

    private void updateCheckedItem() {
        android.util.Log.d("ACCOUNT_LIST_FRAG", "updateCheckedItem()");
        if (curCheckPosition != SipProfile.INVALID_ID) {
            for (int i = 0; i < getListAdapter().getCount(); i++) {
                long profId = getListAdapter().getItemId(i);
                if (profId == curCheckPosition) {
                    getListView().setItemChecked(i, true);
                    android.util.Log.d("ACCOUNT_LIST_FRAG", "updateCheckedItem()" + "itemchecked: " + i);
                }
            }
        } else {
            android.util.Log.d("ACCOUNT_LIST_FRAG", "updateCheckedItem()" + "invalid sip ID");
            for (int i = 0; i < getListAdapter().getCount(); i++) {
                getListView().setItemChecked(i, false);
            }
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(CURRENT_CHOICE, curCheckPosition);
    }


    @Override
    public void onResume() {
        super.onResume();
        if (statusObserver == null) {
            statusObserver = new AccountStatusContentObserver(mHandler);
            if (isAdded()) {
                getActivity().getContentResolver().registerContentObserver(SipProfile.ACCOUNT_STATUS_URI, true, statusObserver);
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (statusObserver != null && isAdded()) {
            getActivity().getContentResolver().unregisterContentObserver(statusObserver);
            statusObserver = null;
        }
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        Log.d(THIS_FILE, "Checked " + position + " et " + id);
        android.util.Log.d("LIST_ITEM_CHECKED", "list item checked");

        ListView lv = getListView();
        lv.setItemChecked(position, true);

        curCheckPosition = id;
        Cursor c = (Cursor) getListAdapter().getItem(position - lv.getHeaderViewsCount());
//        showDetails(id, c.getString(c.getColumnIndex(SipProfile.FIELD_WIZARD)));
    }

    /**
     * Helper function to show the details of a selected item, either by
     * displaying a fragment in-place in the current UI, or starting a
     * whole new activity in which it is displayed.
     */
    private void showDetails(long profileId, String wizard) {
        //curCheckPosition = index;

        /*
        if (dualPane) {
            // If we are not currently showing a fragment for the new
            // position, we need to create and install a new one.
        	AccountEditFragment df = AccountEditFragment.newInstance(profileId);
            //df.setOnQuitListener(this);
            // Execute a transaction, replacing any existing fragment
            // with this one inside the frame.
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.details, df, null);
          //  ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.setTransition(FragmentTransaction.TRANSIT_NONE);
            //if(profileId != Profile.INVALID_ID) {
            //	ft.addToBackStack(null);
            //}
            ft.commit();
        } else {
        */

        // Otherwise we need to launch a new activity to display
        // the dialog fragment with selected text.
        Intent intent = new Intent();
        //intent.setClass(getActivity(), AccountEdit.class);
        intent.setClass(getActivity(), BasePrefsWizard.class);
        if (profileId != SipProfile.INVALID_ID) {
            intent.putExtra(SipProfile.FIELD_ID, profileId);
        }
        intent.putExtra(SipProfile.FIELD_WIZARD, wizard);
        startActivity(intent);
        	
        	/*
        }
        */
    }

    /*
	@Override
	public void onQuit() {
		curCheckPosition = SipProfile.INVALID_ID;
		if(dualPane) {
			showDetails(curCheckPosition, null);
		}
	}

	@Override
	public void onShowProfile(long profileId) {
		curCheckPosition = profileId;
		updateCheckedItem();
	}
	*/

    @Override
    public void onToggleRow(AccountRowTag tag) {
        ContentValues cv = new ContentValues();
        cv.put(SipProfile.FIELD_ACTIVE, !tag.activated);

        android.util.Log.d("ACCOUNT_LIST_FRAG", "onToggleRow(), tag.accountId: " + tag.accountId);
        getActivity().getContentResolver().update(ContentUris.withAppendedId(SipProfile.ACCOUNT_ID_URI_BASE, tag.accountId), cv, null, null);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        android.util.Log.d("ACCOUNT_LIST_FRAG", "onCreateLoader()" + "id: " + id + "bundle: " + args);
        return new CursorLoader(getActivity(), SipProfile.ACCOUNT_URI, new String[]{
                SipProfile.FIELD_ID + " AS " + BaseColumns._ID,
                SipProfile.FIELD_ID,
                SipProfile.FIELD_DISPLAY_NAME,
                SipProfile.FIELD_WIZARD,
                SipProfile.FIELD_ACTIVE
        }, null, null, null);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        super.onLoadFinished(loader, data);
        // Select correct item if any
        android.util.Log.d("ACCOUNT_LIST_FRAG", "onLoadFinished()" + "loader: " + loader + "data: " + data);
        updateCheckedItem();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        android.util.Log.d("ACCOUNT_LIST_FRAG", "onActivityResult()");
        if (resultCode == Activity.RESULT_OK && data != null && data.getExtras() != null) {

            if (requestCode == CHOOSE_WIZARD) {
                // Wizard has been choosen, now create an account
                String wizardId = data.getStringExtra(WizardUtils.ID);
                if (wizardId != null) {
                    showDetails(SipProfile.INVALID_ID, wizardId);
                }
            } else if (requestCode == CHANGE_WIZARD) {
                // Change wizard done for this account.
                String wizardId = data.getStringExtra(WizardUtils.ID);
                long accountId = data.getLongExtra(Intent.EXTRA_UID, SipProfile.INVALID_ID);

                if (wizardId != null && accountId != SipProfile.INVALID_ID) {
                    ContentValues cv = new ContentValues();
                    cv.put(SipProfile.FIELD_WIZARD, wizardId);
                    getActivity().getContentResolver().update(
                            ContentUris.withAppendedId(SipProfile.ACCOUNT_ID_URI_BASE, accountId),
                            cv, null, null);

                }

            }
        }

    }


    // Context menu stuff
    // Activate / deactive menu
    public static final int MENU_ITEM_ACTIVATE = Menu.FIRST;
    // Modify the account
    public static final int MENU_ITEM_MODIFY = Menu.FIRST + 1;
    // Delete the account
    public static final int MENU_ITEM_DELETE = Menu.FIRST + 2;
    // Change the wizard of the account
    public static final int MENU_ITEM_WIZARD = Menu.FIRST + 3;

    /**
     * Retrieve sip account from a given context menu info pressed
     * @param cmi The context menu info to retrieve infos from
     * @return corresponding sip profile if everything goes well, null if not able to retrieve profile
     */
    private SipProfile profileFromContextMenuInfo(ContextMenuInfo cmi) {
        AdapterView.AdapterContextMenuInfo info;
        try {
            info = (AdapterView.AdapterContextMenuInfo) cmi;
        } catch (ClassCastException e) {
            Log.e(THIS_FILE, "bad menuInfo", e);
            return null;
        }
        Cursor c = (Cursor) getListAdapter().getItem(info.position - getListView().getHeaderViewsCount());
        if (c == null) {
            // For some reason the requested item isn't available, do nothing
            return null;
        }
        return new SipProfile(c);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        final SipProfile account = profileFromContextMenuInfo(item.getMenuInfo());
        if (account == null) {
            // For some reason the requested item isn't available, do nothing
            return super.onContextItemSelected(item);
        }

        switch (item.getItemId()) {
            case MENU_ITEM_DELETE: {
                getActivity().getContentResolver().delete(ContentUris.withAppendedId(SipProfile.ACCOUNT_ID_URI_BASE, account.id), null, null);
                return true;
            }
            case MENU_ITEM_MODIFY: {
                showDetails(account.id, account.wizard);
                return true;
            }
            case MENU_ITEM_ACTIVATE: {
                ContentValues cv = new ContentValues();
                cv.put(SipProfile.FIELD_ACTIVE, !account.active);
                getActivity().getContentResolver().update(ContentUris.withAppendedId(SipProfile.ACCOUNT_ID_URI_BASE, account.id), cv, null, null);
                return true;
            }
            case MENU_ITEM_WIZARD: {
                Intent it = new Intent(getActivity(), WizardChooser.class);
                it.putExtra(Intent.EXTRA_UID, account.id);
                startActivityForResult(it, CHANGE_WIZARD);
                return true;
            }
        }
        return super.onContextItemSelected(item);

    }

    @Override
    public void changeCursor(Cursor c) {
        if (mAdapter != null) {
            mAdapter.changeCursor(c);
        }
    }

}
