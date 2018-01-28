package com.seveilith.developer.tiles.tiles;

import android.content.ContentResolver;
import android.content.Intent;
import android.provider.Settings;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import com.seveilith.developer.tiles.PrereqActivity;
import com.seveilith.developer.tiles.Utils;

public class AdbSettingsTile extends TileService {

    private Tile mTile;

    private ContentResolver mContentResolver;

    private boolean mAdbEnabled;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onTileAdded() {
        super.onTileAdded();
    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        refresh();
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
    }

    @Override
    public void onClick() {
        super.onClick();

        if (Utils.isSystemApp(this)) {

            updateQsTile();

        } else {
            Intent launchPrereqs = new Intent(this, PrereqActivity.class);
            launchPrereqs.putExtra("show_snackbar", "ADB Settings");
            startActivity(launchPrereqs);
        }
    }

    private void updateQsTile() {

        mTile = getQsTile();

        mContentResolver = getContentResolver();
        mAdbEnabled = (Settings.Global.getInt(mContentResolver, Settings.Global.ADB_ENABLED, 0) > 0);

        if (mAdbEnabled) {
            Settings.Global.putInt(mContentResolver, Settings.Global.ADB_ENABLED, 0);
            mTile.setState(Tile.STATE_INACTIVE);
        } else {
            Settings.Global.putInt(mContentResolver, Settings.Global.ADB_ENABLED, 1);
            mTile.setState(Tile.STATE_ACTIVE);
        }
        mTile.updateTile();
    }

    private void refresh() {

        mTile = getQsTile();

        mContentResolver = getContentResolver();
        mAdbEnabled = (Settings.Global.getInt(mContentResolver, Settings.Global.ADB_ENABLED, 0) > 0);

        if (mAdbEnabled) {
            mTile.setState(Tile.STATE_ACTIVE);
        } else {
            mTile.setState(Tile.STATE_INACTIVE);
        }
        mTile.updateTile();
    }
}