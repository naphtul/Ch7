package il.co.gilead.ch7;

import java.io.IOException;

import com.google.ads.Ad;
import com.google.ads.AdListener;
import com.google.ads.AdRequest;
import com.google.ads.AdRequest.ErrorCode;
import com.google.ads.InterstitialAd;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements OnPreparedListener, OnBufferingUpdateListener{
	private MediaPlayer mediaPlayer;
	private TextView status;
	private AudioManager mAudioManager;
	private int current_volume;
	private int saved_volume = 5;
	private InterstitialAd interstitial;
	private boolean isMuted = false;
	private int state; // 1=playing, 2=paused, 3=stopped

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
	    status = (TextView) findViewById(R.id.textView);

		mediaPlayer = new MediaPlayer();
		mediaPlayer.setOnPreparedListener(this);
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		try {
			mediaPlayer.setDataSource("http://media.varnish.inn.co.il/a7live");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		prepare((View) findViewById(R.layout.activity_main));
		ch7website();
	}

	@SuppressLint("SetJavaScriptEnabled")
	private void ch7website(){
		WebView webView = (WebView) findViewById(R.id.webView);
		webView.setWebViewClient(new WebViewClient());
		WebSettings webSettings = webView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webView.loadUrl("http://www.inn.co.il/wap/");
	}

	private void interstitAd(){
		interstitial = new InterstitialAd(this, getString(R.string.admob_id));
		AdRequest adRequest = new AdRequest();
		interstitial.loadAd(adRequest);
		interstitial.setAdListener(new AdListener() {
			
			@Override
			public void onReceiveAd(Ad arg0) {
				if (arg0 == interstitial) {
					interstitial.show();
				}
			} 
			
			@Override
			public void onPresentScreen(Ad arg0) {
			}
			
			@Override
			public void onLeaveApplication(Ad arg0) {
			}
			
			@Override
			public void onFailedToReceiveAd(Ad arg0, ErrorCode arg1) {
			}
			
			@Override
			public void onDismissScreen(Ad arg0) {
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
    	switch(item.getItemId()){
    	case R.id.action_exit:
    		interstitAd();
			onDestroy();
			finish();
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    }

	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		if (!isMuted){
			if (!mp.isPlaying())
				status.setText(R.string.buffering);
			else
				state=1;
		}
		switch (state){
		case 1:
			status.setText(R.string.playing);
			break;
		case 2:
			status.setText(R.string.paused);
			break;
		case 3:
			status.setText(R.string.stopped);
			break;
		}
	}

	public void prepare(View view){
		status.setText(R.string.buffering);
		try {
			mediaPlayer.prepareAsync();
			mediaPlayer.setOnBufferingUpdateListener(this);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}

	public void onPrepared(MediaPlayer mp) {
		state=1;
		mp.start();
	}

	public void start(View view){
		Button btnPlayPause = (Button) findViewById(R.id.playPauseButton); 
		switch (state){
		case 1:
			btnPlayPause.setBackgroundResource(R.drawable.ic_action_play);
			mediaPlayer.pause();
			state=2;
			break;
		case 2:
			btnPlayPause.setBackgroundResource(R.drawable.ic_action_pause);
			mediaPlayer.start();
			state=1;
			break;
		case 3:
			prepare(view);
			break;
		}
	}

	public void stop(View view){
		switch (state){
		case 1:
			mediaPlayer.stop();
			state=3;
			break;
		case 2:
			mediaPlayer.stop();
			state=3;
			break;
		case 3:
			break;
		}
	}

	public void mute(View view){
		mAudioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
		current_volume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		Button btnMute = (Button) findViewById(R.id.muteButton); 
		if (current_volume > 0){
			saved_volume = current_volume;
			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
			btnMute.setBackgroundResource(R.drawable.ic_action_volume_muted);
			status.setText(R.string.mute);
			isMuted = true;
		}else{
			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, saved_volume, 0);
			btnMute.setBackgroundResource(R.drawable.ic_action_volume_on);
			status.setText(R.string.playing);
			isMuted = false;
		}		
	}

	@Override public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);     
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); 
	}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
        	mediaPlayer.release();
        	mediaPlayer = null;
        }
    }

}
