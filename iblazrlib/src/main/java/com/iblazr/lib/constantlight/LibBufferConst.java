package com.iblazr.lib.constantlight;

import android.media.AudioTrack;
public class LibBufferConst extends Thread {	
	private int buffersize = -1;
	private short[] buffer1, buffer2, pcmdata;
	private int frequency, prevfrequency;
	private int sampleRate;
	private int writesize;
	
	private short lastSignalValue;
	
	private long time;
	
	private boolean runFlag = true;
	
	private int offset;
	
	private boolean waitFlag = false;
	
	public LibBufferConst(int sampleRate, int buffersize, int startfreq, int writesize)
	{
		this.sampleRate = sampleRate;
		this.buffersize = buffersize;
		buffer1 = new short[this.buffersize];
		buffer2 = new short[this.buffersize];
		pcmdata = buffer1;
		this.frequency = startfreq;
		prevfrequency = this.frequency;
		this.writesize = writesize;
		
		lastSignalValue = (short)0;
		offset = 0;
		fillBuffer();
		swapBuffers();
		start();
	}
	
	public void applyFrequency(int newfreq)
	{
		frequency = newfreq;		
	}
	
	public boolean write(AudioTrack track)
	{
		int towrite = (buffersize - offset >= writesize)?writesize:(buffersize - offset);
		int writen = track.write(pcmdata, offset, towrite);
		if (writen<0){
			return false;
		}
		offset+=writen;
		if (offset>=buffersize) 
		{
			offset=0;
			swapBuffers();
		}
		return true;
	}
	
	public boolean isRunning()
	{
		return isAlive()||runFlag;
	}
	
	public void finish()
	{
		runFlag = false;
		waitFlag = false;
	}
	
	private void swapBuffers()
	{
		pcmdata = buffer1;
		buffer1 = buffer2;
		buffer2 = pcmdata;
		waitFlag = false;
	}
	
	private void fillBuffer() {
		int curfreq = frequency;
		if (curfreq<1300) curfreq = 1300;
		if (curfreq>9400) curfreq = 9400;
		
		if (curfreq!=prevfrequency){
			double tempSignalValue = Math.sin(2*Math.PI*prevfrequency*time/sampleRate);
			double shift = Math.asin(tempSignalValue);
			if ((short)(32767.0*tempSignalValue)<lastSignalValue) {
				shift = Math.PI - shift;
			}
			time = 0;
			for (int i=0; i<buffer1.length; i++){
				buffer1[i] = (short) (32767.0*Math.sin(2*Math.PI*curfreq*time/sampleRate+shift));
				time++;
				if (time<0) time=0;
			}			
		} else {
			for (int i=0; i<buffer1.length; i++){
				buffer1[i] = (short) (32767.0*Math.sin(2*Math.PI*curfreq*time/sampleRate));
				time++;
				if (time<0) time=0;
			}
		}
		lastSignalValue = buffer1[buffer1.length-1];
		prevfrequency = curfreq;
	}

	@Override
	public void run() {
		while (runFlag){
			fillBuffer();
			waitFlag = true;
			while (waitFlag){
				try {
					Thread.sleep(5);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}	
}
