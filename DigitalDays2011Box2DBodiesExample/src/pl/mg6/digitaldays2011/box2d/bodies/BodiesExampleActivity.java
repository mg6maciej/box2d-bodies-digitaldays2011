package pl.mg6.digitaldays2011.box2d.bodies;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class BodiesExampleActivity extends Activity implements Handler.Callback, OnTouchListener {
	
	private static final String TAG = BodiesExampleActivity.class.getSimpleName();
	
	private BodiesExampleModel model;
	private BodiesExampleView view;
	
	private Handler handler;
	private static final int MESSAGE_ID = 666;
	
	private long lastUpdateTime;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		model = (BodiesExampleModel) getLastNonConfigurationInstance();
		if (model == null) {
			model = new BodiesExampleModel();
		}
		view = new BodiesExampleView(this);
		view.setOnTouchListener(this);
		view.setModel(model);
		setContentView(view);
		
		handler = new Handler(this);
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
		return model;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		handler.sendEmptyMessage(MESSAGE_ID);
		lastUpdateTime = SystemClock.uptimeMillis();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		handler.removeMessages(MESSAGE_ID);
	}

	public boolean handleMessage(Message msg) {
		if (msg.what == MESSAGE_ID) {
			update();
			handler.sendEmptyMessageDelayed(MESSAGE_ID, 10);
			return true;
		}
		return false;
	}
	
	private void update() {
		long currentTime = SystemClock.uptimeMillis();
		long elapsed = currentTime - lastUpdateTime;
		lastUpdateTime = currentTime;
		// updating model on UI thread (never do that at home)
		model.update(elapsed);
		view.invalidate();
	}
	
	public boolean onTouch(View v, MotionEvent event) {
		float viewportSize = BodiesExampleView.VIEWPORT_SIZE;
		int action = event.getAction() & MotionEvent.ACTION_MASK;
		int pointerIndex = event.getAction() >> MotionEvent.ACTION_POINTER_ID_SHIFT;
		float x = event.getX(pointerIndex) * viewportSize / v.getWidth();
		float y = (v.getHeight() - event.getY(pointerIndex)) * viewportSize / v.getWidth();
		int pointerId = event.getPointerId(pointerIndex);
		if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
			Log.i(TAG, "down: " + pointerId + " " + x + " " + y);
			model.userActionStart(pointerId, x, y);
		}
		if (event.getAction() == MotionEvent.ACTION_MOVE) {
			for (int i = 0; i < event.getPointerCount(); i++) {
				x = event.getX(i) * viewportSize / v.getWidth();
				y = (v.getHeight() - event.getY(i)) * viewportSize / v.getWidth();
				pointerId = event.getPointerId(i);
				//Log.i(TAG, "move: " + pointerId + " " + x + " " + y);
				model.userActionUpdate(pointerId, x, y);
			}
		}
		if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
			Log.i(TAG, "up: " + pointerId + " " + x + " " + y);
			model.userActionEnd(pointerId, x, y);
		}
		return true;
	}
}
