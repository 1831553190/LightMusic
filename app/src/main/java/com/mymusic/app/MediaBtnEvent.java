package com.mymusic.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;

import com.mymusic.app.bean.DataBean;

public class MediaBtnEvent extends BroadcastReceiver {
	static long lastClickTime=0;
	private static final int DOUBLE_CLICK = 400;
	private static int clickCount=0;
	private static final int MSG_HEADSET_DOUBLE_CLICK_TIMEOUT = 2;
	private String command;
	
	static Handler handler=new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what){
				case MSG_HEADSET_DOUBLE_CLICK_TIMEOUT:
					final int clickCount=msg.arg1;
					final String cmd ;
					switch (clickCount){
						case 1:
							cmd= DataBean.ACTION_PLAYANDPASE;
							break;
						case 2:
							cmd= DataBean.ACTION_PLAYNEXT;
							break;
						case 3:
							cmd=DataBean.ACTION_PLAYPRIVAOUS;
							break;
							default:
								cmd=null;
					}
					if (cmd != null) {
						final Context context = (Context) msg.obj;
						Intent intent = new Intent(context, MediaService.class);
						intent.setAction(cmd);
						context.startService(intent);
					}
					break;
			}
		}
	};

	public static boolean handleEvent(Context context,Intent intent){
		String action=intent.getAction();
		if (action!=null){
			if (action.equals(Intent.ACTION_MEDIA_BUTTON)){
				final KeyEvent keyEvent=intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
				if (keyEvent==null)
					return false;
				final long eventTime=keyEvent.getEventTime();
				final int keycode = keyEvent.getKeyCode();
				final int eventAction=keyEvent.getAction();

				if (eventAction==KeyEvent.ACTION_DOWN){
					if (keyEvent.getRepeatCount()==0){
						if (keycode==KeyEvent.KEYCODE_HEADSETHOOK){
							if (eventTime-lastClickTime>=DOUBLE_CLICK){
								clickCount=0;
							}
							clickCount++;
							handler.removeMessages(MSG_HEADSET_DOUBLE_CLICK_TIMEOUT);
							Message msg=handler.obtainMessage(MSG_HEADSET_DOUBLE_CLICK_TIMEOUT,clickCount,0,context);
							long delay=clickCount<3?DOUBLE_CLICK:0;
							if (clickCount>3){
								clickCount=2;
							}
							lastClickTime=eventTime;
							handler.sendMessageDelayed(msg,delay);
						}
						return true;
					}
				}
			}

		}

		return false;

	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if (handleEvent(context,intent)&&isOrderedBroadcast()){
			abortBroadcast();
		}
	}
}
