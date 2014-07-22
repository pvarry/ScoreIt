/*
 * Copyright (c) 2014 SBG Apps
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sbgapps.scoreit;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.faizmalkani.floatingactionbutton.FloatingActionButton;
import com.sbgapps.scoreit.games.Game;
import com.sbgapps.scoreit.games.Lap;
import com.sbgapps.scoreit.games.belote.BeloteLapActivity;
import com.sbgapps.scoreit.games.coinche.CoincheLapActivity;
import com.sbgapps.scoreit.games.tarot.TarotLapActivity;
import com.sbgapps.scoreit.games.universal.UniversalLapActivity;
import com.sbgapps.scoreit.utils.Utils;
import com.sbgapps.scoreit.widget.PlayerInfo;

import org.arasthel.googlenavdrawermenu.views.GoogleNavigationDrawer;

public class ScoreItActivity extends BaseActivity
        implements GoogleNavigationDrawer.OnNavigationSectionSelected {

    public static final String EXTRA_LAP = "com.sbgapps.scoreit.lap";
    public static final String EXTRA_POSITION = "com.sbgapps.scoreit.position";
    private static final int REQ_PICK_CONTACT = 1;
    private static final int REQ_LAP_ACTIVITY = 2;

    private boolean mIsTablet;
    private PlayerInfo mEditedName;
    private ActionBarDrawerToggle mDrawerToggle;
    private GoogleNavigationDrawer mDrawer;
    private ScoreListFragment mScoreListFragment;
    private GraphFragment mGraphFragment;
    private HeaderFragment mHeaderFragment;
    private int mEditedLap = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_scoreit);
        setAccentDecor();
        //mIsTablet = (null != findViewById(R.id.fragment_container_large));

        final FragmentManager fragmentManager = getSupportFragmentManager();

        // Init fragments
        if (null == savedInstanceState) {
            loadFragments(false);
        } else {
            mHeaderFragment = (HeaderFragment) fragmentManager.findFragmentByTag(HeaderFragment.TAG);
            mGraphFragment = (GraphFragment) fragmentManager.findFragmentByTag(GraphFragment.TAG);
            mScoreListFragment = (ScoreListFragment) fragmentManager.findFragmentByTag(ScoreListFragment.TAG);
        }

        // Init drawer
        mDrawer = (GoogleNavigationDrawer) findViewById(R.id.navigation_drawer_container);
        mDrawer.check(mGameHelper.getPlayedGame());
        mDrawerToggle = new ActionBarDrawerToggle(this,
                mDrawer,
                R.drawable.ic_navigation_drawer,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        mDrawer.setDrawerListener(mDrawerToggle);
        mDrawer.setOnNavigationSectionSelected(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // Floating Action Button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLapActivity();
            }
        });
        setTitle();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGameHelper.saveGame();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
//        if (mDrawer.isDrawerOpen()) {
//            menu.clear();
//            return false;
//        } else
        {
            MenuItem item;
            if (0 == mGameHelper.getLaps().size()) {
                if (!mIsTablet) {
                    item = menu.findItem(R.id.menu_view);
                    item.setVisible(false);
                }
                item = menu.findItem(R.id.menu_clear);
                item.setVisible(false);
            }
            item = menu.findItem(R.id.menu_count);
            item.setVisible(Game.UNIVERSAL == mGameHelper.getPlayedGame() ||
                    Game.TAROT == mGameHelper.getPlayedGame());
            return true;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mDrawer != null) {
                    if (mDrawer.isDrawerMenuOpen()) {
                        mDrawer.closeDrawerMenu();
                    } else {
                        mDrawer.openDrawerMenu();
                    }
                }
                return true;

            case R.id.menu_clear:
                showClearDialog();
                return true;

            case R.id.menu_view:
                switchScoreViews();
                return true;

            case R.id.menu_count:
                showPlayerCountDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSectionSelected(View v, int i, long l) {
        switch (i) {
            case 0:
                mGameHelper.setPlayedGame(Game.UNIVERSAL);
                break;
            case 1:
                mGameHelper.setPlayedGame(Game.TAROT);
                break;
            case 2:
                mGameHelper.setPlayedGame(Game.BELOTE);
                break;
            case 3:
                mGameHelper.setPlayedGame(Game.COINCHE);
                break;
            case 4:
                Intent intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                return;
        }
        getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        supportInvalidateOptionsMenu();
        setTitle();
        loadFragments(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String name;
        if (RESULT_OK != resultCode) return;

        switch (requestCode) {
            case REQ_PICK_CONTACT:
                Cursor cursor = getContentResolver().query(data.getData(),
                        new String[]{ContactsContract.Contacts.DISPLAY_NAME}, null, null, null);
                if (cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                    name = cursor.getString(columnIndex);
                    nameEdited(name);
                }
                break;

            case REQ_LAP_ACTIVITY:
                Lap lap = (Lap) data.getSerializableExtra(EXTRA_LAP);
                if (-1 == mEditedLap) {
                    lap.computeScores();
                    mGameHelper.addLap(lap);
                } else {
                    Lap edited = mGameHelper.getLaps().get(mEditedLap);
                    edited.set(lap);
                    mEditedLap = -1;
                }
                updateFragments();
                invalidateOptionsMenu();
                break;
        }
    }

    public void editLap(Lap lap) {
        mEditedLap = mGameHelper.getLaps().indexOf(lap);
        mScoreListFragment.closeOpenedItems();
        showLapActivity();
    }

    public void removeLap(Lap lap) {
        mGameHelper.removeLap(lap);
        mScoreListFragment.closeOpenedItems();
        updateFragments();
    }

    public void editName(View view) {
        mEditedName = (PlayerInfo) view.getParent().getParent();
        showEditNameActionChoices();
    }

    private void updateFragments() {
        if (null != mHeaderFragment)
            mHeaderFragment.update();
        if (null != mScoreListFragment && mScoreListFragment.isVisible())
            mScoreListFragment.update();
        if (null != mGraphFragment && mGraphFragment.isVisible())
            mGraphFragment.traceGraph();
    }

    private void loadFragments(boolean anim) {
        mHeaderFragment = new HeaderFragment();
        mScoreListFragment = new ScoreListFragment();

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (anim)
            ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        ft.replace(R.id.fragment_header, mHeaderFragment, HeaderFragment.TAG)
                .commit();

        ft = getSupportFragmentManager().beginTransaction();
        if (anim)
            ft.setCustomAnimations(R.anim.slide_in_down, R.anim.slide_out_up);
        ft.replace(R.id.fragment_container, mScoreListFragment, ScoreListFragment.TAG)
                .commit();
    }

    private void showLapActivity() {
        Intent intent;
        switch (mGameHelper.getPlayedGame()) {
            default:
            case Game.UNIVERSAL:
                intent = new Intent(this, UniversalLapActivity.class);
                break;
            case Game.BELOTE:
                intent = new Intent(this, BeloteLapActivity.class);
                break;
            case Game.COINCHE:
                intent = new Intent(this, CoincheLapActivity.class);
                break;
            case Game.TAROT:
                intent = new Intent(this, TarotLapActivity.class);
                break;
        }
        intent.putExtra(EXTRA_POSITION, mEditedLap);
        startActivityForResult(intent, REQ_LAP_ACTIVITY);
    }

    private void switchScoreViews() {
        if (null != mGraphFragment && mGraphFragment.isVisible()) {
            getSupportFragmentManager().popBackStack();
            return;
        }

        if (null == mGraphFragment)
            mGraphFragment = new GraphFragment();

        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_down,
                        R.anim.slide_out_up,
                        R.anim.slide_in_up,
                        R.anim.slide_out_down)
                .addToBackStack(null)
                .replace(R.id.fragment_container, mGraphFragment, GraphFragment.TAG)
                .commit();
    }

    private void nameEdited(String name) {
        // TODO
        //mGameHelper.setPlayerName(mEditedName.getPlayer(), name);
        mEditedName.setName(name);
    }

    private void showClearDialog() {
        Dialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.clear_game)
                .setMessage(R.string.sure)
                .setPositiveButton(
                        R.string.yes,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mGameHelper.deleteAll();
                                mHeaderFragment.update();
                                if (null != mScoreListFragment && mScoreListFragment.isVisible())
                                    mScoreListFragment.getListAdapter().removeAll();
                                if (null != mGraphFragment && mGraphFragment.isVisible()) {
                                    mGraphFragment.traceGraph();
                                    if (!mIsTablet) getFragmentManager().popBackStack();
                                }
                                supportInvalidateOptionsMenu();
                            }
                        }
                )
                .setNegativeButton(
                        R.string.no,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Nothing to do!
                            }
                        }
                )
                .create();
        dialog.show();
        Utils.colorizeDialog(dialog);
    }

    private void showEditNameActionChoices() {
        Dialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.edit_name)
                .setItems(R.array.edit_name_action,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent;
                                switch (which) {
                                    default:
                                    case 0:
                                        intent = new Intent(Intent.ACTION_PICK,
                                                ContactsContract.Contacts.CONTENT_URI);
                                        startActivityForResult(intent, REQ_PICK_CONTACT);
                                        break;
                                    case 1:
                                        showEditNameDialog();
                                        break;
                                }
                            }
                        }
                )
                .create();
        dialog.show();
        Utils.colorizeDialog(dialog);
    }

    public void showPlayerCountDialog() {
        String[] players;
        switch (mGameHelper.getPlayedGame()) {
            default:
                players = new String[]{"2", "3", "4", "5"};
                break;
            case Game.TAROT:
                players = new String[]{"3", "4", "5"};
                break;
        }
        Dialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.player_number)
                .setItems(players,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mGameHelper.setPlayerCount(which);
                                loadFragments(true);
                            }
                        }
                )
                .create();
        dialog.show();
        Utils.colorizeDialog(dialog);
    }

    private void showEditNameDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_edit_name, null);
        final EditText editText = (EditText) view.findViewById(R.id.edit_name);
        //editText.setText(mEditedName.getName());
        Dialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.edit_name)
                .setView(view)
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String name = editText.getText().toString();
                                nameEdited(name);
                            }
                        }
                )
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Nothing to do!
                            }
                        }
                )
                .create();
        dialog.show();
        Utils.colorizeDialog(dialog);
    }
}
