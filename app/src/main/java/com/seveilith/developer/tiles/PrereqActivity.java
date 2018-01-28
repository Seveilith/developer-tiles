package com.seveilith.developer.tiles;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootTools.RootTools;

import static com.seveilith.developer.tiles.Utils.LOG_TAG;

public class PrereqActivity extends AppCompatActivity {

    private boolean mIsWorkerThreadAlive;

    private Handler mHandler;

    private Context mContext;

    private RelativeLayout mRootView;

    private FloatingActionMenu mFloatingActionMenu;

    @Override
    public void onBackPressed() {
        mFloatingActionMenu = (FloatingActionMenu) findViewById(R.id.floating_action_menu);

        if (!mFloatingActionMenu.isOpened()) {
            super.onBackPressed();
        } else {
            mFloatingActionMenu.close(true);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mIsWorkerThreadAlive = false;

        mContext = this;

        setContentView(R.layout.activity_prereq);

        mRootView = (RelativeLayout) findViewById(R.id.root_view);
        mFloatingActionMenu = (FloatingActionMenu) findViewById(R.id.floating_action_menu);

        String tileContextMessage = getIntent().getStringExtra("show_snackbar");

        if (tileContextMessage != null) {
            String snackbarMessage = Utils.getStringResource(mContext,
                    R.string.not_system_app_snackbar_message_1) + " '" + tileContextMessage + "' " +
                    Utils.getStringResource(mContext, R.string.not_system_app_snackbar_message_2);

            constructAndShowSnackbar(snackbarMessage,
                    Utils.getStringResource(mContext, android.R.string.ok));
        }

        mHandler = new Handler(Looper.getMainLooper()) {

            @Override
            public void handleMessage(Message message) {

                if (message.getData().getBoolean("show_snackbar", false)) {
                    constructAndShowSnackbar(Utils.getStringResource(mContext, R.string.migration_complete_snackbar_message),
                            Utils.getStringResource(mContext, android.R.string.ok));
                    Log.d(LOG_TAG, "Migration to system app completed");
                }

                String errorMessage = message.getData().getString("command_message");
                if (errorMessage != null) {

                    Toast.makeText(mContext, "Command error: " + errorMessage, Toast.LENGTH_LONG).show();
                    Log.d(LOG_TAG, "Command error: " + errorMessage);
                }
            }
        };

        FloatingActionButton migrateToSystemAppFab = (FloatingActionButton) findViewById(R.id.floating_action_menu_migrate);
        FloatingActionButton playStorePageFab = (FloatingActionButton) findViewById(R.id.floating_action_menu_play_store);

        migrateToSystemAppFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mFloatingActionMenu.close(true);
                migrateToSystemApp();
            }
        });

        playStorePageFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mFloatingActionMenu.close(false);

                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.seveilith.developer.tiles")));
            }
        });


        mRootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mFloatingActionMenu.isOpened()) {
                    mFloatingActionMenu.close(true);
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mIsWorkerThreadAlive) {
            Thread.currentThread().interrupt();
            mIsWorkerThreadAlive = false;
        }
    }

    private void constructAndShowSnackbar(String message, String actionMessage) {
        final Snackbar snackbar = Snackbar.make(mRootView, message, Snackbar.LENGTH_INDEFINITE);

        // Prevent the snackbar text from being truncated
        View snackbarView = snackbar.getView();
        TextView snackbarTextView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        snackbarTextView.setMaxLines(5);

        snackbar.setAction(actionMessage, new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                snackbar.dismiss();
                Log.i(LOG_TAG, "Snackbar dismissed");
            }
        });
        snackbar.show();
    }

    private void migrateToSystemApp() {

        Thread workerThread = new Thread() {
            @Override
            public void run() {

                String packageSourcePath = Utils.getPackageSourcePath(mContext);

                String packageName = Utils.getVersionedPackageName(packageSourcePath);

                Log.d(LOG_TAG, "Migrating to system app: " + packageName);

                String rootScript = "mount -o rw,remount /system\n" +
                        "mv /data/app/" + packageName + " /system/priv-app/" + packageName;

                Command command = new Command(0, rootScript) {

                    @Override
                    public void commandCompleted(int id, int exitCode) {
                        Message snackbarMessage = mHandler.obtainMessage();

                        Bundle bundle = new Bundle();
                        bundle.putBoolean("show_snackbar", true);
                        snackbarMessage.setData(bundle);

                        snackbarMessage.sendToTarget();
                    }

                    @Override
                    public void commandOutput(int id, String line) {
                        prepareAndSendErrorMessage(line);
                    }

                    @Override
                    public void commandTerminated(int i, String s) {
                        prepareAndSendErrorMessage(s);
                    }
                };
                try {
                    RootTools.getShell(true).add(command);
                } catch (Exception e) {
                    e.printStackTrace();
                    prepareAndSendErrorMessage("Device is not rooted!");
                }
            }
        };
        workerThread.start();
        mIsWorkerThreadAlive = true;
    }

    private void prepareAndSendErrorMessage(String error) {
        Message errorMessage = mHandler.obtainMessage();

        Bundle bundle = new Bundle();
        bundle.putString("command_message", error);
        errorMessage.setData(bundle);

        errorMessage.sendToTarget();
    }
}