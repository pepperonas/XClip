package io.celox.xclip.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import io.celox.xclip.MainActivity;
import io.celox.xclip.R;
import io.celox.xclip.data.ClipboardEntity;
import io.celox.xclip.data.ClipboardRepository;

public class ClipboardService extends Service {

    private static final String CHANNEL_ID = "clipboard_service_channel";
    private static final int NOTIFICATION_ID = 1;
    public static final String ACTION_OPEN_DIALOG = "io.celox.xclip.OPEN_DIALOG";

    private ClipboardManager clipboardManager;
    private ClipboardRepository repository;
    private String lastClipboardText = "";
    private NotificationManager notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();

        repository = new ClipboardRepository(getApplication());
        clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification(0));

        // Clipboard Listener
        clipboardManager.addPrimaryClipChangedListener(() -> {
            ClipData clipData = clipboardManager.getPrimaryClip();
            if (clipData != null && clipData.getItemCount() > 0) {
                ClipData.Item item = clipData.getItemAt(0);
                CharSequence text = item.getText();

                if (text != null && text.length() > 0) {
                    String clipboardText = text.toString();

                    // Verhindere Duplikate
                    if (!clipboardText.equals(lastClipboardText)) {
                        lastClipboardText = clipboardText;

                        // Speichere in Datenbank
                        ClipboardEntity entity = new ClipboardEntity(
                                clipboardText,
                                System.currentTimeMillis()
                        );
                        repository.insert(entity);

                        // Update Notification
                        updateNotification();
                    }
                }
            }
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Clipboard Monitor",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Überwacht deine Zwischenablage kontinuierlich");
            channel.setShowBadge(false);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification(int count) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(ACTION_OPEN_DIALOG);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String contentText = count == 0
                ? "Bereit zum Speichern"
                : count + " " + (count == 1 ? "Eintrag" : "Einträge") + " gespeichert";

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("XClip läuft")
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setOngoing(false)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build();
    }

    private void updateNotification() {
        int count = repository.getCount();
        notificationManager.notify(NOTIFICATION_ID, createNotification(count));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
