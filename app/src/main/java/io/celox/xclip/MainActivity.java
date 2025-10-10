package io.celox.xclip;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import io.celox.xclip.service.ClipboardService;
import io.celox.xclip.ui.ClipboardDialog;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check for notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        PERMISSION_REQUEST_CODE);
            } else {
                startClipboardService();
            }
        } else {
            startClipboardService();
        }

        // Check if started from notification
        if (ClipboardService.ACTION_OPEN_DIALOG.equals(getIntent().getAction())) {
            openClipboardDialog();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (ClipboardService.ACTION_OPEN_DIALOG.equals(intent.getAction())) {
            openClipboardDialog();
        }
    }

    private void startClipboardService() {
        Intent serviceIntent = new Intent(this, ClipboardService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    private void openClipboardDialog() {
        ClipboardDialog dialog = new ClipboardDialog();
        dialog.show(getSupportFragmentManager(), "clipboard_dialog");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startClipboardService();
            } else {
                Toast.makeText(this, "Benachrichtigungsberechtigung erforderlich", Toast.LENGTH_LONG).show();
            }
        }
    }
}