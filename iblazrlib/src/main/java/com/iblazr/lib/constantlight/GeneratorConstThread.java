package com.iblazr.lib.constantlight;



import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.AudioTrack;
import android.util.Log;

public class GeneratorConstThread extends Thread implements OnAudioFocusChangeListener {
	// coeffitients
	private final static int atCoeff = 4;
	private final static int bCoeff = 1;
	private final static int bwCoeff = 2;	
	
	private static final int sampleRate = 44100;
	private static final int writelimit = 5000;
	private int bufferSize;
	private static final String tag = "generatorthread";
	
	private AudioTrack track;
	
	private AtomicBoolean runFlag;
	private int frequency;
	private boolean freqUpdated = false;
	
	private Context context;
	private AudioManager am;
	
	private LibBufferConst buffer;
	
	
	public GeneratorConstThread(Context context, int defFreq)
	{
		super("Generator Thread");
		//setPriority(MAX_PRIORITY);
		
		this.context = context;
		am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		am.setSpeakerphoneOn(false);
		//am.setRouting(AudioManager.MODE_NORMAL, AudioManager.ROUTE_HEADSET, 0);
		
		
		runFlag = new AtomicBoolean(true);
		freqUpdated = true;
		frequency = defFreq;
		
		bufferSize = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_8BIT);
		buffer = new LibBufferConst(sampleRate, bufferSize*bCoeff,frequency,bufferSize/bwCoeff);
		start();
		Log.d(tag, "============ Generator Thread has been created ============");
	}
	
	public void setFrequency(int freq)
	{
		frequency = freq;
		freqUpdated = true;
	}
	
	public void finish()
	{
		runFlag.set(false);
        if (buffer.isRunning()){
            buffer.finish();
        }
        this.interrupt();
	}
	
	public boolean isRunning()
	{
		return this.isAlive();
	}

	@Override
	public void run() {
		Log.d(tag, "Starting thread");
		threadMethod();
	}

	private void threadMethod() {
		
		if (null==track){
			track = new AudioTrack(
					AudioManager.STREAM_MUSIC,
					sampleRate,
					AudioFormat.CHANNEL_OUT_MONO,
					AudioFormat.ENCODING_PCM_16BIT,
					atCoeff*bufferSize,
					AudioTrack.MODE_STREAM);
			
			if (null!=am){
				int result = am.requestAudioFocus(this,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN);
	
				if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
						Log.d(tag, "Audiofocus granted");
						track.play();
				} else {
					runFlag.set(false);
					
					
				}
				
				Log.d(tag, "Track has been created");
			}
		}
		
		while (runFlag.get()){
			
			if (freqUpdated){
				Log.d(tag, "Setting new value of frequency: "+frequency);
				buffer.applyFrequency(frequency);
				//track.flush();
				freqUpdated = false;
			}
			
			if (!buffer.write(track)) {
				runFlag.set(false);
				Log.e(tag, "Finishibg due some error!");
				break;
			}
			
			/*try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				Log.e(tag, "Exception: "+e);
				e.printStackTrace();
			}*/
		}
		
		buffer.finish();
		
		while (buffer.isRunning());
		
		track.pause();
		track.flush();
		track.stop();
		track.release();
		track = null;
		
		if (null!=am) {
			am.abandonAudioFocus(this);
			am.setSpeakerphoneOn(true);
		}
		
		Log.d(tag, "Finishing thread");
	}



	@Override
	public void onAudioFocusChange(int arg0) {
		if (AudioManager.AUDIOFOCUS_LOSS==arg0) {
			runFlag.set(false);
			Log.d(tag, "Audiofocus loss");
		}
	}
}
