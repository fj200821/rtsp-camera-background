package info.idstein.camera.rtsp_background_camera;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import net.majorkernelpanic.streaming.SessionBuilder;
import net.majorkernelpanic.streaming.gl.SurfaceView;
import net.majorkernelpanic.streaming.rtsp.RtspServer;
import net.majorkernelpanic.streaming.video.VideoQuality;

public class BackgroundCameraService extends RtspServer {
    private static final int NOTIFICATION_ID = 42;

    @Override
    public void onCreate() {
        super.onCreate();
        SurfaceView sv;
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        @SuppressLint("InflateParams") RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.live_activity, null);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(1, 1,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_TOAST,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        assert wm != null;
        wm.addView(layout, params);

        sv = layout.findViewById(R.id.surface);
        SurfaceHolder sh = sv.getHolder();
        sv.setZOrderOnTop(true);
        sh.setFormat(PixelFormat.TRANSPARENT);

        // Configures the SessionBuilder
        SessionBuilder.getInstance()
                .setSurfaceView(sv)
                .setContext(getApplicationContext())
                .setVideoQuality(new VideoQuality(1920, 1080, 30, 20000))
                .setVideoEncoder(SessionBuilder.VIDEO_H264)
                .setAudioEncoder(SessionBuilder.AUDIO_AAC)
                .setPreviewOrientation(90)
                /*.setPreviewOrientation(90)
                .setAudioEncoder(SessionBuilder.AUDIO_NONE)
                .setVideoEncoder(SessionBuilder.VIDEO_H264)*/;

        runAsForeground();
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel() {
        NotificationChannel chan = new NotificationChannel("my_service",
                "My Background Service", NotificationManager.IMPORTANCE_NONE);
        NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert service != null;
        service.createNotificationChannel(chan);
        return "my_service";
    }

    private void runAsForeground() {
        Intent notificationIntent = new Intent(this, LiveActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        final Notification.Builder builder;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, createNotificationChannel());
        } else {
            builder = new Notification.Builder(this);
        }
        Notification notification = builder
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setContentIntent(pendingIntent).build();

        startForeground(NOTIFICATION_ID, notification);
    }
}
