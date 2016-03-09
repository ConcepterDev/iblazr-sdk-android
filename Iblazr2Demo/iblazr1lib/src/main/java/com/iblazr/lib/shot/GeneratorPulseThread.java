package com.iblazr.lib.shot;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.AudioTrack;

public class GeneratorPulseThread extends Thread implements OnAudioFocusChangeListener {
	
	private static final int ID_STATE_IDLE = 0;
	private static final int ID_STATE_START = 1;
	private static final int ID_STATE_GENERATING = 2;
	
	// coeffitients
	private final static int xatCoeff = 4;
	private final static int xbCoeff = 2;
	private final static int xbwCoeff = 4;	
	
	private static final int xsampleRate = 44100;
	private int xbufferSize;
	
	private AudioTrack xtrack;
	
	private boolean xrunFlag;
	private int xfrequency;
	private int xstate;
	private int xduration;
	private long xstartTime;	
	
	private Context xcontext;
	private AudioManager xam;
	private int xoldAudioMode;
	// private int xoldRingerMode;
	private boolean xisSpeakerPhoneOn;
	
	private LibBufferShot xbuffer;	
	
	public GeneratorPulseThread(Context xcontext)
	{
		super("Pulse Generator Thread");
		setPriority(MAX_PRIORITY);
		
		this.xcontext = xcontext;
		xam = (AudioManager) xcontext.getSystemService(Context.AUDIO_SERVICE);
		xoldAudioMode = xam.getMode();
	//	xoldRingerMode = xam.getRingerMode();
		xisSpeakerPhoneOn = xam.isSpeakerphoneOn();
		
		xrunFlag = true;
		xstate = ID_STATE_IDLE;
		
		xbufferSize = AudioTrack.getMinBufferSize(xsampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_8BIT);
		start();
	}
	
	/***
	 * Starts generation for a time
	 * @param frequency - frequency
	 * @param duration - duration in milliseconds
	 */
	public void xgenerate(int xfrequency, int xduration)
	{
		if (ID_STATE_IDLE == xstate){
			this.xfrequency = xfrequency;
			this.xduration = xduration;
			xstate = ID_STATE_START;
		}
	}
	
	public void finish()
	{
		xrunFlag = false;
	}
	
	public boolean xisRunning()
	{
		return this.isAlive();
	}

	@Override
	public void run() {
		xthreadMethod();
	}

	private void xthreadMethod() {
		
		if (null==xtrack){
			xtrack = new AudioTrack(
					AudioManager.STREAM_MUSIC,
					xsampleRate,
					AudioFormat.CHANNEL_OUT_MONO,
					AudioFormat.ENCODING_PCM_16BIT,
					xatCoeff*xbufferSize,
					AudioTrack.MODE_STREAM);
			
			if (null!=xam){
				int xresult = xam.requestAudioFocus(this,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN);
	
				if (xresult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
				//		xam.setRingerMode(AudioManager.RINGER_MODE_SILENT);
				//		xam.setMode(AudioManager.MODE_IN_CALL);
						xam.setSpeakerphoneOn(false);
				} else {
					xrunFlag = false;
				}
			}
		}
		
		while (xrunFlag){
			
			if (ID_STATE_START==xstate){
				xbuffer = new LibBufferShot(xsampleRate, xbufferSize*xbCoeff,xfrequency,xbufferSize/xbwCoeff);
				xbuffer.xapplyFrequency(xfrequency);
				//track.flush();
				xstate=ID_STATE_GENERATING;
				xstartTime = System.currentTimeMillis();
				xtrack.play();
			}
			if (xstate==ID_STATE_GENERATING){
				if (!xbuffer.xwrite(xtrack)) {
					xrunFlag = false;
					break;
				}
				if (System.currentTimeMillis()-xstartTime>=xduration){
					xtrack.pause();
					xtrack.flush();
					xbuffer.finish();
					xstate = ID_STATE_IDLE;
				}
			}
			
			try {
				Thread.sleep(2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		xbuffer.finish();
		
		while (xbuffer.isRunning());
		
		xtrack.pause();
		xtrack.flush();
		xtrack.stop();
		xtrack.release();
		xtrack = null;
		
		if (null!=xam) {
			xam.abandonAudioFocus(this);
			xam.setSpeakerphoneOn(xisSpeakerPhoneOn);
			xam.setMode(xoldAudioMode);
		//	xam.setRingerMode(xoldRingerMode);
		}
	}

	@Override
	public void onAudioFocusChange(int arg0) {
		if (AudioManager.AUDIOFOCUS_LOSS==arg0) {
			xrunFlag = false;
		}
	}
	
}
