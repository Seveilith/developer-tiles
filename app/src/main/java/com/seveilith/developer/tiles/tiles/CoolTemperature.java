package com.seveilith.developer.tiles.tiles;

import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.widget.Toast;

import com.seveilith.developer.tiles.R;
import com.seveilith.developer.tiles.SystemPropertiesProxy;

public class CoolTemperature extends TileService {

    private Tile mTile;

    private boolean mUseCoolColorTemp;

    private final String COLOR_TEMPERATURE_PROPERTY = "persist.sys.debug.color_temp";

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
        updateQsTile();
    }

    private void updateQsTile() {

        mTile = getQsTile();

        mUseCoolColorTemp = SystemPropertiesProxy.getBoolean(COLOR_TEMPERATURE_PROPERTY, false);

        if (mUseCoolColorTemp) {
            SystemPropertiesProxy.set(COLOR_TEMPERATURE_PROPERTY, "0");
            mTile.setState(Tile.STATE_INACTIVE);
        } else {
            SystemPropertiesProxy.set(COLOR_TEMPERATURE_PROPERTY, "1");
            mTile.setState(Tile.STATE_ACTIVE);
        }
        Toast.makeText(this, R.string.color_temperature_toast, Toast.LENGTH_LONG).show();
        mTile.updateTile();
    }

    private void refresh() {

        mTile = getQsTile();

        mUseCoolColorTemp = SystemPropertiesProxy.getBoolean(COLOR_TEMPERATURE_PROPERTY, false);

        if (mUseCoolColorTemp) {
            mTile.setState(Tile.STATE_ACTIVE);
        } else {
            mTile.setState(Tile.STATE_INACTIVE);
        }
        mTile.updateTile();
    }
}