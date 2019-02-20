package info.idstein.camera.rtsp_background_camera;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import net.majorkernelpanic.streaming.SessionBuilder;
import net.majorkernelpanic.streaming.gl.SurfaceView;
import net.majorkernelpanic.streaming.rtsp.RtspServer;

public class BackgroundCameraService extends RtspServer {
    private static final int NOTIFICATION_ID = 42;

    @Override
    public void onCreate() {
        super.onCreate();
        SurfaceView sv;
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.live_activity, null);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(1, 1,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);

        wm.addView(layout, params);

        sv = (SurfaceView) layout.findViewById(R.id.surface);
        SurfaceHolder sh = sv.getHolder();
        sv.setZOrderOnTop(true);
        sh.setFormat(PixelFormat.TRANSPARENT);

        // Configures the SessionBuilder
        SessionBuilder.getInstance()
                .setSurfaceView(sv)
                .setPreviewOrientation(90)
                .setContext(getApplicationContext())
                .setAudioEncoder(SessionBuilder.AUDIO_NONE)
                .setVideoEncoder(SessionBuilder.VIDEO_H264);

        runAsForeground();
    }

    private void runAsForeground() {
        Intent notificationIntent = new Intent(this, RtspSettingsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentText("Background recording")
                .setContentIntent(pendingIntent).build();

        startForeground(NOTIFICATION_ID, notification);
    }
}
