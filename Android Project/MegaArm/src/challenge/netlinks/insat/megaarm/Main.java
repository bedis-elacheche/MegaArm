package challenge.netlinks.insat.megaarm;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageButton;

public class Main extends Activity implements OnTouchListener, OnClickListener {

	// Declaring variables
	private static ImageButton rotation_base, engine_base, engine_arm, hand;
	private static Button base_f, base_b, arm_f, arm_b, hand_f, hand_b;
	private String signal = null;
	private BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
	private OutputStream outStream = null;
	private BluetoothDevice mmDevice = null;
	private BluetoothSocket mmSocket = null;

	// SPP UUID service
	private static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");
	// MAC-address of BT module
	private static String address = "XX:XX:XX:XX:XX:XX";

	// Main method
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Testing Bluetooth's availability
		if (btAdapter == null) {
			setContentView(R.layout.no_bt);
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					Main.this.finish();
				}
			}, 3000);
		} else {
			setContentView(R.layout.main);
			// Enabling BT if it is disabled
			openBT();
			// Establishing connection
			connectBT();
			// Assigning views
			arm_f = (Button) findViewById(R.id.arm_f);
			arm_b = (Button) findViewById(R.id.arm_b);
			base_f = (Button) findViewById(R.id.base_f);
			base_b = (Button) findViewById(R.id.base_b);
			hand_f = (Button) findViewById(R.id.hand_f);
			hand_b = (Button) findViewById(R.id.hand_b);
			rotation_base = (ImageButton) findViewById(R.id.rotation_base);
			engine_base = (ImageButton) findViewById(R.id.engine_base);
			engine_arm = (ImageButton) findViewById(R.id.engine_arm);
			hand = (ImageButton) findViewById(R.id.hand);
			// Assigning actions
			arm_f.setOnTouchListener(this);
			arm_b.setOnTouchListener(this);
			base_f.setOnTouchListener(this);
			base_b.setOnTouchListener(this);
			hand_f.setOnClickListener(this);
			hand_b.setOnClickListener(this);
		}
	}

	// Called when application shutdown
	@Override
	protected void onDestroy() {
		// Close BT
		closeBT();
		super.onDestroy();
	}

	// OnTouch method: called when arm/base command buttons are pushed
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			switch (v.getId()) {
			case R.id.base_f:
			case R.id.arm_f:
				signal = signal.charAt(0) + "F";
				break;
			case R.id.base_b:
			case R.id.arm_b:
				signal = signal.charAt(0) + "B";
				break;
			}
			break;
		case MotionEvent.ACTION_UP:
			signal = signal.charAt(0) + "S";
			break;
		}
		sendData(signal);
		return false;
	}

	// OnClick method: called when hand command buttons are clicked
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.hand_f:
			signal = "4F";
			hand_b.setEnabled(true);
			hand_f.setEnabled(false);
			break;
		case R.id.hand_b:
			signal = "4B";
			hand_f.setEnabled(true);
			hand_b.setEnabled(false);
			break;
		}
		sendData(signal);
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				sendData("4S");
			}
		}, 500);
	}

	// Called when engine's button is chosen
	public void onChooseEngine(View v) {
		switch (v.getId()) {
		case R.id.rotation_base:
			signal = "1";
			disable_controls();
			enable_menu_except(rotation_base);
			base_f.setEnabled(true);
			base_b.setEnabled(true);
			break;
		case R.id.engine_base:
			signal = "2";
			disable_controls();
			enable_menu_except(engine_base);
			arm_f.setEnabled(true);
			arm_b.setEnabled(true);
			break;
		case R.id.engine_arm:
			signal = "3";
			disable_controls();
			enable_menu_except(engine_arm);
			arm_f.setEnabled(true);
			arm_b.setEnabled(true);
			break;
		case R.id.hand:
			signal = "4";
			disable_controls();
			enable_menu_except(hand);
			hand_f.setEnabled(true);
			break;
		}
	}

	// Disable all activity's buttons
	private void disable_controls() {
		Button[] btnArray = { base_f, base_b, arm_f, arm_b, hand_f, hand_b };
		for (Button button : btnArray) {
			button.setEnabled(false);
		}
	}

	// Enable menu's buttons except b
	private void enable_menu_except(ImageButton b) {
		ImageButton[] btnArray = { rotation_base, engine_base, engine_arm, hand };
		for (ImageButton button : btnArray) {
			if (b == button)
				button.setEnabled(false);
			else
				button.setEnabled(true);
		}
	}

	// Open BT
	private void openBT() {
		AsyncTask<Void, Void, Void> async = new AsyncTask<Void, Void, Void>() {
			ProgressDialog dialog = new ProgressDialog(Main.this);
			protected void onPreExecute() {
				super.onPreExecute();
				dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				dialog.setMessage("Enabeling Bluetooth, please wait...");
				dialog.setCancelable(false);
				dialog.show();
			}

			@Override
			protected Void doInBackground(Void... params) {
				while (!btAdapter.isEnabled())
					btAdapter.enable();
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				dialog.dismiss();
				dialog = null;
			}

		};
		async.execute();
	}

	// Close BT
	private void closeBT() {
		if (btAdapter.isEnabled())
			btAdapter.disable();
	}

	// Establishing connection
	private void connectBT() {
		Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
		if (pairedDevices.size() > 0) {
			for (BluetoothDevice device : pairedDevices) {
				if (device.getAddress().equals(address)) {
					mmDevice = device;
					break;
				}
			}
		}
		try {
			mmSocket = InsecureBluetooth.createRfcommSocketToServiceRecord(
					mmDevice, MY_UUID, true);
		} catch (NullPointerException e) {
		} catch (IOException e) {
		}
		try {
			mmSocket.connect();
		} catch (NullPointerException e) {
		} catch (IOException e) {
			try {
				mmSocket.close();
			} catch (IOException e1) {
			}
		}
		try {
			outStream = mmSocket.getOutputStream();
		} catch (NullPointerException e) {
		} catch (IOException e) {
		}
	}

	// Send data to Arduino
	private void sendData(String message) {
		byte[] msgBuffer = message.getBytes();

		try {
			outStream.write(msgBuffer);
		} catch (NullPointerException e) {
		} catch (IOException e) {
		}
		try {
			outStream.flush();
		} catch (NullPointerException e) {
		} catch (IOException e) {
		}
	}

}

