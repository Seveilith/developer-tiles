package com.seveilith.developer.tiles;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

import java.io.File;

import static com.seveilith.developer.tiles.Utils.LOG_TAG;

public class SystemAppActivity extends AppCompatActivity {

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

        setContentView(R.layout.activity_system_app);

        mRootView = (RelativeLayout) findViewById(R.id.root_view);
        mFloatingActionMenu = (FloatingActionMenu) findViewById(R.id.floating_action_menu);

        mHandler = new Handler(Looper.getMainLooper()) {

            @Override
            public void handleMessage(Message message) {

                if (message.getData().getBoolean("show_uninstall_snackbar", false)) {
                    constructAndShowSnackbar(Utils.getStringResource(mContext, R.string.uninstall_complete_snackbar_message),
                            Utils.getStringResource(mContext, android.R.string.ok));
                    Log.d(LOG_TAG, "Uninstall completed");
                } else if (message.getData().getBoolean("show_migration_snackbar", false)) {
                    constructAndShowSnackbar(Utils.getStringResource(mContext, R.string.migration_complete_snackbar_message),
                            Utils.getStringResource(mContext, android.R.string.ok));
                    Log.d(LOG_TAG, "Migration to user app completed");
                }

                String errorMessage = message.getData().getString("command_message");
                if (errorMessage != null) {

                    Toast.makeText(mContext, "Command error: " + errorMessage, Toast.LENGTH_LONG).show();
                    Log.d(LOG_TAG, "Command error: " + errorMessage);
                }
            }
        };

        FloatingActionButton uninstallSystemAppFab = (FloatingActionButton) findViewById(R.id.floating_action_menu_uninstall);
        FloatingActionButton playStorePageFab = (FloatingActionButton) findViewById(R.id.floating_action_menu_play_store);

        uninstallSystemAppFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showUninstallDialog();
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

    private void showUninstallDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.DialogStyle);

        builder.setTitle(R.string.uninstall_dialog_title)
                .setMessage(R.string.uninstall_dialog_message)
                .setPositiveButton(R.string.uninstall_dialog_positive_action, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                       mFloatingActionMenu.close(true);
                        uninstallApp();
                    }
                })
                .setNegativeButton(R.string.uninstall_dialog_negative_action, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mFloatingActionMenu.close(true);
                        migrateToUserApp();
                    }
                })
                .setNeutralButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mFloatingActionMenu.close(true);
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
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

    private void uninstallApp() {

        Thread workerThread = new Thread() {

            @Override
            public void run() {

                String packageSourcePath = Utils.getPackageSourcePath(mContext);

                String packageName = Utils.getVersionedPackageName(packageSourcePath);

                Log.d(LOG_TAG, "Begin uninstall for system app: " + packageName);

                File directory = new File("/system/priv-app/" + packageName);

                if (!directory.exists()) {
                    // Package ID might be wrong

                    Log.d(LOG_TAG, "Package name does not exist in priv-app. Check for another versionId.");

                    int versionId = 0;
                    String filePath = "/system/priv-app/" + getPackageName() + "-";
                    File newDirectory = new File(filePath + versionId);
                    while (!newDirectory.exists()) {
                        versionId++;
                        filePath = filePath + versionId;
                    }
                    packageName = getPackageName() + "-" + versionId;

                    Log.d(LOG_TAG, "App's new package name in priv-app found: " + packageName);
                }

                String rootScript = "mount -o rw,remount /system\n" +
                        "rm -r /system/priv-app/" + packageName;

                Command command = new Command(0, rootScript) {

                    @Override
                    public void commandCompleted(int id, int exitCode) {
                        Message snackbarMessage = mHandler.obtainMessage();

                        Bundle bundle = new Bundle();
                        bundle.putBoolean("show_uninstall_snackbar", true);
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
                }
            }
        };
        workerThread.start();
        mIsWorkerThreadAlive = true;
    }

    private void migrateToUserApp() {

        Thread workerThread = new Thread() {
            @Override
            public void run() {

                String packageSourcePath = Utils.getPackageSourcePath(mContext);

                String packageName = Utils.getVersionedPackageName(packageSourcePath);

                Log.d(LOG_TAG, "Begin migration to user app for: " + packageName);

                String rootScript = "mount -o rw,remount /system\n" +
                        "mv /system/priv-app/" + packageName + " /data/app/" + packageName;

                Command command = new Command(0, rootScript) {

                    @Override
                    public void commandCompleted(int id, int exitCode) {
                        Message snackbarMessage = mHandler.obtainMessage();

                        Bundle bundle = new Bundle();
                        bundle.putBoolean("show_migration_snackbar", true);
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