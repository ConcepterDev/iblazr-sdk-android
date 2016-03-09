package com.iblazr.lib.shot;

import android.media.AudioTrack;
public class LibBufferShot extends Thread {	
	private int xbuffersize = -1;
	private short[] xbuffer1, xbuffer2, xpcmdata;
	private int xfrequency;
	private int xsampleRate;
	private int xwritesize;
	
	private long xtime;
	
	private boolean xrunFlag = true;
	
	private int xoffset;
	
	private boolean xwaitFlag = false;
	
	public LibBufferShot(int xsampleRate, int xbuffersize, int xstartfreq, int xwritesize)
	{
		this.xsampleRate = xsampleRate;
		this.xbuffersize = xbuffersize;
		xbuffer1 = new short[this.xbuffersize];
		xbuffer2 = new short[this.xbuffersize];
		xpcmdata = xbuffer1;
		this.xfrequency = xstartfreq;
		this.xwritesize = xwritesize;
		
		xoffset = 0;
		xfillBuffer();
		xswapBuffers();
		start();
	}
	
	public void xapplyFrequency(int xnewfreq)
	{
		xfrequency = xnewfreq;		
	}
	
	public boolean xwrite(AudioTrack xtrack)
	{
		int xtowrite = (xbuffersize - xoffset >= xwritesize)?xwritesize:(xbuffersize - xoffset);
		int xwriten = xtrack.write(xpcmdata, xoffset, xtowrite);
		if (xwriten<0){
			return false;
		}
		xoffset+=xwriten;
		if (xoffset>=xbuffersize) 
		{
			xoffset=0;
			xswapBuffers();
		}
		return true;
	}
	
	public boolean isRunning()
	{
		return isAlive()||xrunFlag;
	}
	
	public void finish()
	{
		xrunFlag = false;
		xwaitFlag = false;
	}
	
	private void xswapBuffers()
	{
		xpcmdata = xbuffer1;
		xbuffer1 = xbuffer2;
		xbuffer2 = xpcmdata;
		xwaitFlag = false;
	}
	
	private void xfillBuffer() {
			double xtempSignalValue = Math.sin(2*Math.PI*xfrequency*xtime/xsampleRate);
			double xshift = Math.asin(xtempSignalValue);
			xtime = 0;
			for (int i=0; i<xbuffer1.length; i++){
				xbuffer1[i] = (short) (32767.0*Math.sin(2*Math.PI*xfrequency*xtime/xsampleRate+xshift));
				xtime++;
				if (xtime<0) xtime=0;
			}			
	}

	@Override
	public void run() {
		while (xrunFlag){
			xfillBuffer();
			xwaitFlag = true;
			while (xwaitFlag){
				try {
					Thread.sleep(5);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}	
}
