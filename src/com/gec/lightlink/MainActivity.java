package com.gec.lightlink;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.gec.lightlink.util.Constant;
import com.gec.lightlink.util.NetworkUtil;
import com.gec.lightlink.util.Toaster;
import com.gec.lightlink.widget.AdvancedColorPicker;
import com.gec.lightlink.widget.AdvancedColorPicker.OnHsvSeekBarChangedListener;
import com.gec.lightlink.widget.AdvancedColorPicker.OnWedgeSelectedListener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Spinner;

/**
 * @author sig
 * @version 1.01
 */
public class MainActivity extends Activity {

	// 调试用
	private static final String TAG = MainActivity.class.getCanonicalName();

	// 开启WiFi请求码
	private static final int REQUEST_WIFI = 0x01;
	// 服务器端口
	private static final int PORT = 8001;

	// 下拉选择框组件
	private Spinner mSpinner = null;
	// 复选框组件
	private CheckBox mChBox = null;
	// 颜色选择器（第三方开源组件）
	private AdvancedColorPicker mColorPicker = null;

	// 服务器IP地址
	private static String IP = null;
	// private static String SN = null;

	// 标识设备编号
	private static boolean isWhich = true;

	private Socket mSocket = null;
	// 输出流
	private OutputStream mOutS = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 动态隐藏标题栏（必须在setContentView()之前设置）
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		checkNetworkInfo();

		mSpinner = (Spinner) this.findViewById(R.id.spinner_light);
		// List集合装载数据集
		List<String> dataset = new ArrayList<>(Arrays.asList("No.1", "No.2"));
		// 数组适配器填充数据集
		ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dataset);
		// 设置数组适配器下拉样式
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// 下拉选择框组件关联数组适配器
		mSpinner.setAdapter(adapter);

		// 默认设备选择
		if (isWhich)
			mSpinner.setSelection(0);
		else
			mSpinner.setSelection(1);

		// 设置下拉选择框组件监听器
		mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				switch (position) {
				case 0:
					isWhich = true;
					break;
				case 1:
					isWhich = false;
					break;
				default:
					break;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});

		mChBox = (CheckBox) findViewById(R.id.cb_light_status);
		// 设置复选框组件监听器
		mChBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				lightStatusChanged(isChecked, mChBox);
			}
		});

		mColorPicker = (AdvancedColorPicker) findViewById(R.id.color_picker);
		// 设置颜色选择器组件楔块选择监听器
		mColorPicker.setOnWedgeSelectedListener(new OnWedgeSelectedListener() {

			@Override
			public void onWedgeSelected(int position) {
				if (isWhich) {

					switch (position) {
					case 0:
						writeStream(Constant.FIRST_LIGHT_COLOR_RED);
						break;
					case 1:
						writeStream(Constant.FIRST_LIGHT_COLOR_ORANGE);
						break;
					case 2:
						writeStream(Constant.FIRST_LIGHT_COLOR_YELLOW);
						break;
					case 3:
						writeStream(Constant.FIRST_LIGHT_COLOR_GREEN);
						break;
					case 4:
						writeStream(Constant.FIRST_LIGHT_COLOR_CYAN);
						break;
					case 5:
						writeStream(Constant.FIRST_LIGHT_COLOR_LIGHT_BLUE);
						break;
					case 6:
						writeStream(Constant.FIRST_LIGHT_COLOR_DARK_BLUE);
						break;
					case 7:
						writeStream(Constant.FIRST_LIGHT_COLOR_PURPLE);
						break;
					case 8:
						writeStream(Constant.FIRST_LIGHT_COLOR_GRAY);
						break;
					case 9:
						writeStream(Constant.FIRST_LIGHT_COLOR_GRAY);
						break;
					default:
						break;
					}
				} else {

					switch (position) {
					case 0:
						writeStream(Constant.SECOND_LIGHT_COLOR_RED);
						break;
					case 1:
						writeStream(Constant.SECOND_LIGHT_COLOR_ORANGE);
						break;
					case 2:
						writeStream(Constant.SECOND_LIGHT_COLOR_YELLOW);
						break;
					case 3:
						writeStream(Constant.SECOND_LIGHT_COLOR_GREEN);
						break;
					case 4:
						writeStream(Constant.SECOND_LIGHT_COLOR_CYAN);
						break;
					case 5:
						writeStream(Constant.SECOND_LIGHT_COLOR_LIGHT_BLUE);
						break;
					case 6:
						writeStream(Constant.SECOND_LIGHT_COLOR_DARK_BLUE);
						break;
					case 7:
						writeStream(Constant.SECOND_LIGHT_COLOR_PURPLE);
						break;
					case 8:
						writeStream(Constant.SECOND_LIGHT_COLOR_GRAY);
						break;
					case 9:
						writeStream(Constant.SECOND_LIGHT_COLOR_GRAY);
						break;
					default:
						break;
					}
				}
			}
		});

		// 设置HSV拖动条监听器（Hue：色度 Saturation：饱和度 Value：亮度（明度））
		mColorPicker.setOnHsvSeekBarChangedListener(new OnHsvSeekBarChangedListener() {

			@Override
			public void onValChanged(byte val) {
				Log.d(TAG, "[6] --> val:" + val);

				if (isWhich) {
					Constant.FIRST_LIGHT_VAL[21] = val;
					writeStream(Constant.FIRST_LIGHT_VAL);
				} else {
					Constant.SECOND_LIGHT_VAL[21] = val;
					writeStream(Constant.SECOND_LIGHT_VAL);
				}
			}

			@Override
			public void onSatChanged(byte sat) {
				Log.d(TAG, "[5] --> sat:" + sat);

				if (isWhich) {
					Constant.FIRST_LIGHT_SAT[22] = sat;
					writeStream(Constant.FIRST_LIGHT_SAT);
				} else {
					Constant.SECOND_LIGHT_SAT[22] = sat;
					writeStream(Constant.SECOND_LIGHT_SAT);
				}
			}

			@Override
			public void onHueChanged(byte hue) {
				Log.d(TAG, "[4] --> hue:" + hue);

				if (isWhich) {
					Constant.FIRST_LIGHT_HUE[21] = hue;
					writeStream(Constant.FIRST_LIGHT_HUE);
				} else {
					Constant.SECOND_LIGHT_HUE[21] = hue;
					writeStream(Constant.SECOND_LIGHT_HUE);
				}
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_WIFI)

			// Handler延时3s后再执行UDP广播线程，防止程序崩溃退出
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					new UdpThread("GETIP\r\n").start();
				}
			}, 3 * 1000);

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onBackPressed() {
		close();

		super.onBackPressed();
	}

	/**
	 * UDP广播线程
	 */
	private class UdpThread extends Thread {
		private byte[] data = null;

		public UdpThread(String cmd) {
			// 字符串转化为字节数组
			data = cmd.getBytes();
		}

		@Override
		public void run() {
			DatagramSocket mSocket = null;

			try {
				mSocket = new DatagramSocket();
				// 封装数据包，获取到本地IP，例192.168.1.175，正则表达式取第三位网段1，拼装到广播IP：192.168.1.255
				String netSegment = NetworkUtil.getLocalAddress().split(".")[2];
				DatagramPacket pack = new DatagramPacket(data, data.length,
						InetAddress.getByName("192.168." + netSegment + ".255"), 9090);

				// 发送数据包
				mSocket.send(pack);
			} catch (SocketException e) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						Toaster.shortToastShow(MainActivity.this, "糟糕，找不着服务器君了-_-|||");
						MainActivity.this.finish();
					}
				});
			} catch (IOException e) {
				throw new RuntimeException("输入输出异常", e);
			}

			byte[] dataRecv = new byte[512];
			DatagramPacket pack = new DatagramPacket(dataRecv, dataRecv.length);

			try {
				mSocket.receive(pack);
				String udpResponse = new String(dataRecv, 0, pack.getLength());

				if (udpResponse != null && udpResponse.contains("IP") && udpResponse.contains("SN")) {
					Log.d(TAG, "[2] --> " + udpResponse);

					// SN = udpResponse.split("SN:")[1];
					IP = udpResponse.split("IP:")[1].split("SN:")[0];

					new TcpThread().start();
				}
			} catch (IOException e) {
				throw new RuntimeException("输入输出异常", e);
			} finally {
				mSocket.close();
			}
		}
	}

	private class TcpThread extends Thread {

		@Override
		public void run() {
			InputStream inS = null;

			try {
				mSocket = new Socket(IP, PORT);
				mOutS = mSocket.getOutputStream();
				inS = mSocket.getInputStream();

				byte[] data = new byte[512];
				int len;
				while ((len = inS.read(data)) > 0) {
					String tcpResponse = bytes2HexStr(data, 0, len);
					Log.d(TAG, "[3] -->" + tcpResponse);
				}
			} catch (IOException e) {
				// 屏蔽Log错误信息
				// throw new RuntimeException("输入输出异常",e);
			}
		}
	}

	private String bytes2HexStr(byte[] data, int offset, int byteCount) {
		String ret = "";

		for (int i = offset; i < byteCount; i++) {
			String hex = Integer.toHexString(data[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			ret += hex.toUpperCase(Locale.getDefault());
		}
		return ret;
	}

	/**
	 * 关闭Socket
	 */
	private void close() {
		if (mSocket != null) {
			try {
				mSocket.close();
			} catch (IOException e) {
				throw new RuntimeException("输入输出异常", e);
			}
		}
	}

	/**
	 * 灯状态的改变
	 * 
	 * @param isChecked
	 *            被选中的设备
	 * @param chBox
	 *            复选框组件
	 */
	private void lightStatusChanged(boolean isChecked, CheckBox chBox) {
		if (isChecked) {
			WidgetStyleChanged(chBox, R.drawable.switch_control_pre);

			if (isWhich)
				writeStream(Constant.FIRST_LIGHT_ON);
			else
				writeStream(Constant.SECOND_LIGHT_ON);
		} else {
			WidgetStyleChanged(chBox, R.drawable.switch_control_nor);

			if (isWhich)
				writeStream(Constant.FIRST_LIGHT_OFF);
			else
				writeStream(Constant.SECOND_LIGHT_OFF);
		}
	}

	/**
	 * 发送数据
	 * 
	 * @param data
	 */
	private void writeStream(byte[] data) {
		try {
			if (mOutS != null) {
				// 写数据到输出流
				mOutS.write(data);
				// 调用write()之后数据依然留在缓存中，必须调用flush()，才能将数据真正发送出去
				mOutS.flush();
			}
		} catch (IOException e) {
			// 涉及UI操作的必须在主线程中运行，runOnUiThread()的原理即为Handler消息处理机制
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					Toaster.shortToastShow(MainActivity.this, "连接超时，被服务器君抛弃了::>_<::");
					// 结束程序
					MainActivity.this.finish();
				}
			});
		}
	}

	/**
	 * 组件UI的改变
	 * 
	 * @param chBox
	 *            复选框组件
	 * @param id
	 *            资源ID
	 */
	@SuppressWarnings("deprecation")
	private void WidgetStyleChanged(CheckBox chBox, int id) {
		Drawable topDrawable = getResources().getDrawable(id);
		topDrawable.setBounds(0, 0, topDrawable.getMinimumWidth(), topDrawable.getMinimumHeight());
		chBox.setCompoundDrawables(null, topDrawable, null, null);
	}

	/**
	 * 检测网络状态
	 */
	@SuppressWarnings("deprecation")
	private void checkNetworkInfo() {
		// 获取系统服务
		ConnectivityManager conn = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		// 获取WiFi信号的状态
		State wifiState = conn.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
		Log.d(TAG, "[1] --> WiFi state:" + wifiState.toString());

		if (wifiState == State.CONNECTED || wifiState == State.CONNECTING) { // 判断连接状态
			// 执行UDP广播线程
			new UdpThread("GETIP\r\n").start();
			return;
		}

		Toaster.shortToastShow(this, "都什么年代了，还塞网络o(╯□╰)o");
		// 启动WiFi设置页面并回调onActivityResult()方法
		startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS), REQUEST_WIFI);
	}
}
