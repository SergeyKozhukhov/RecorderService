package ru.sergeykozhukhov.recorderservice;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

/**
 * Сервис записи звуковых файлов
 */
public class RecorderService extends Service {

    /**
     * Идентификатор канала уведомлений
     */
    private static final String CHANNEL_ID = "CHANNEL_ID_3";

    /**
     * Шаг таймера
     */
    private static final long TIMER_PERIOD = 1000L;

    /**
     * Идентификатор уведомления
     */
    private static final int NOTIFICATION_ID = 3;

    /**
     * Максимальное время записи
     */
    private static long TIMER_MAX_VALUE = 5L*60L*60L*1000L;

    /**
     * Действие интента, по которому производится остановка записи
     */
    public static final String ACTION_STOP = "TIMER_SERVICE_ACTION_STOP";

    /**
     * Диктофон
     */
    private VoiceRecorder voiceRecorder;

    /**
     * Счетчик обратного отсчета (таймер)
     */
    private CountDownTimer mCountDownTimer;


    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();
        initVoiceRecorder();

    }

    private void initVoiceRecorder() {
        voiceRecorder = new VoiceRecorder();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (ACTION_STOP.equals(intent.getAction())) {
            stopCountdownTimer();
            stopForeground( true );
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        stopCountdownTimer();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return recorderServiceAIDL.asBinder();
    }


    @Override
    public boolean onUnbind(Intent intent) {
        stopCountdownTimer();
        return super.onUnbind(intent);
    }


    private Notification createNotification(long currentTime) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Intent intentCloseService = new Intent(this, RecorderService.class);
        intentCloseService.setAction(ACTION_STOP);
        PendingIntent pendingIntentCloseService = PendingIntent.getService(this, 0, intentCloseService, 0);


        long timeFromStart = millsToSeconds(TIMER_MAX_VALUE) - currentTime;

        builder.setContentTitle(getString(R.string.notif_recorder_service_title))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText(getString(R.string.notif_recorder_service_text) + timeFromStart)
                .setOnlyAlertOnce(true)
                .addAction(R.drawable.ic_launcher_background, getString(R.string.notif_recorder_service_action_title), pendingIntentCloseService)
                .setContentIntent(pendingIntent);

        return builder.build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_recorder_service_name);
            String description = getString(R.string.channel_recorder_service_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    private void startCountdownTimer(long time, long period) {

        mCountDownTimer = new CountDownTimer(time, period) {
            @Override
            public void onTick(long millisUntilFinished) {
                updateNotification(createNotification(millsToSeconds(millisUntilFinished)));
            }

            @Override
            public void onFinish() {
                voiceRecorder.stop();
                stopSelf();
            }
        };

        mCountDownTimer.start();
    }


    private void updateNotification(@NonNull Notification notification) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, notification);


    }

    /**
     * Остановка таймера.
     */
    private void stopCountdownTimer() {
        voiceRecorder.stop();
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }
    }

    /**
     * Перевод миллисекунд в секунды.
     *
     * @param time - время в милиссекундах.
     * @return время в секундах.
     */
    private long millsToSeconds(long time) {
        return time / 1000L;
    }


    private Handler mHandler = new Handler();

    private IRecorderServiceAIDL.Stub recorderServiceAIDL = new IRecorderServiceAIDL.Stub() {

        @Override
        public void setDirectory(String directory) throws RemoteException {
            voiceRecorder.setFilePath(directory);
        }

        @Override
        public void startRecord() throws RemoteException {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    startCountdownTimer(TIMER_MAX_VALUE, TIMER_PERIOD);
                    startForeground(NOTIFICATION_ID, createNotification(1000));
                    Toast.makeText(getApplicationContext(), R.string.toast_start_record, Toast.LENGTH_SHORT).show();
                    voiceRecorder.start();
                }
            });
        }

        @Override
        public void stopRecord() throws RemoteException {
            stopCountdownTimer();
            Toast.makeText(getApplicationContext(), R.string.toast_stop_record, Toast.LENGTH_SHORT).show();
            stopForeground( true );
        }
    };




}
