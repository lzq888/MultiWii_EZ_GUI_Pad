/*  MultiWii EZ-GUI
    Copyright (C) <2012>  Bartosz Szczygiel (eziosoft)

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.ezio.multiwii.app;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.ezio.multiwii.R;
import com.ezio.multiwii.frsky.FrskyProtocol;
import com.ezio.multiwii.helpers.Functions;
import com.ezio.multiwii.helpers.Notifications;
import com.ezio.multiwii.helpers.Sensors;
import com.ezio.multiwii.helpers.SoundManager;
import com.ezio.multiwii.helpers.TTS;
import com.ezio.multiwii.mw.MultiWii210;
import com.ezio.multiwii.mw.MultirotorData;
import com.ezio.multiwii.waypoints.Waypoint;
import com.ezio.sec.Sec;
import com.ezio.multiwii.helpers.RootCmd;
import com.ezio.multiwii.helpers.SystemPartition;

import communication.BT;
import communication.Communication;
import communication.SerialCDC_ACM;
import communication.SerialFTDI;

public class App extends Application implements Sensors.Listener {

	// debug
	public boolean D = false; // debug
	public String TAG = "EZGUI";
	// end debug/////////////////

	private static String REFRESHRATE = "REFRESHRATE";
	public int RefreshRate = 100; // this means wait 100ms after everything is
									// done

	public Communication commMW;
	public Communication commFrsky;
	public MultirotorData mw;
	public Sensors sensors;

	public boolean FollowMeEnable = false;
	public boolean FollowMeBlinkFlag = false;
	public boolean InjectGPSEnable = false;
	public boolean InjectGPSBlinkFlag = false;
	public boolean FollowHeading = false;
	public boolean FollowHeadingBlinkFlag = false;

	public FrskyProtocol frskyProtocol;

	private SharedPreferences prefs;
	private Editor editor;
	public TTS tts;
	public SoundManager soundManager;

	// variables used in FrequentJobs
	private boolean[] oldActiveModes;
	private long timer1 = 0; // Say battery level every xx seconds;
								// PeriodicSpeaking is the frequency in ms
	private long timer2 = 0;
	int timer2Freq = 8000;// bip when low battery
	private long timer3 = 0;
	int timer3Freq = 1000; // timer every 1sek
	private long timer4 = 0;
	int timer4Freq = 5000; // timer every 5sek

	public boolean loggingON = false;
	// ----settings-----
	private static String COPYFRSKYTOMW = "COPYFRSKYTOMW";
	public boolean CopyFrskyToMW;

	private static String COMMUNICATION_TYPE_MW = "CommunicationTypeMW";
	public static int COMMUNICATION_TYPE_BT = 0;
	public static int COMMUNICATION_TYPE_SERIAL_FTDI = 1;
	public static int COMMUNICATION_TYPE_SERIAL_OTHERCHIPS = 2;
	public int CommunicationTypeMW = COMMUNICATION_TYPE_BT;

	public static String SERIAL_PORT_BAUD_RATE_MW = "SerialPortBaudRateMW";
	public String SerialPortBaudRateMW = "115200";

	public static String SERIAL_PORT_BAUD_RATE_FRSKY = "SerialPortBaudRateFrSky";
	public String SerialPortBaudRateFrSky = "9600";

	private static String COMMUNICATION_TYPE_FRSKY = "CommunicationTypeFrSky";
	public int CommunicationTypeFrSky = COMMUNICATION_TYPE_BT;

	private static String RADIOMODE = "RadioMode";
	public int RadioMode;

	private static String PROTOCOL = "PROTOCOL1";
	public int Protocol;

	private static String MAGMODE = "MAGMODE";
	public int MagMode;

	private static String TEXTTOSPEACH = "TEXTTOSPEACH1";
	public boolean TextToSpeach = true;

	private static String MACADDERSS = "MACADDERSS";
	public String MacAddress = "";

	private static String MACADDERSSFRSKY = "MACADDERSSFRSKY";
	public String MacAddressFrsky = "";

	private static String CONNECTONSTART = "CONNECTONSTART";
	public boolean ConnectOnStart = false;

	private static String ALTCORRECTION = "ALTCORRECTION";
	public boolean AltCorrection = false;

	private static String ADVANCEDFUNCTIONS = "ADVANCEDFUNCTIONS";
	public boolean AdvancedFunctions = false;

	private static String DISABLEBTONEXIT = "DISABLEBTONEXIT";
	public boolean DisableBTonExit = true;

	// private static String G1 = "G1";
	// private int _1Gtemp; // 1g value, used for g-force display

	private static String FORCELANGUAGE = "FORCELANGUAGE";
	public String ForceLanguage = "";

	private static String PERIODICSPEAKING = "PERIODICSPEAKING";
	public int PeriodicSpeaking = 20000; // in ms

	private static String VOLTAGEALARM = "VOLTAGEALARM";
	public float VoltageAlarm = 0;

	private static String USEOFFLINEMAPS = "USEOFFLINEMAPS";
	//public boolean UseOfflineMaps = true;

	private static String APPSTARTCOUNTER = "APPSTARTCOUNTER";
	public int AppStartCounter = 0;

	private static String DONATEBUTTONPRESSED = "DONATEBUTTONPRESSED";
	public int DonateButtonPressed = 0;

	private static String REVERSEROLL = "REVERSEROLL";
	public boolean ReverseRoll = false;

	private static String MAPZOOMLEVEL = "MAPZOOMLEVEL";
	public int MapZoomLevel = 9;

	private static String MAPCENTERPERIOD = "MAPCENTERPERIOD";
	public int MapCenterPeriod = 3;

	// graphs
	public String ACCROLL = "ACC ROLL";
	public String ACCPITCH = "ACC PITCH";
	public String ACCZ = "ACC Z";

	public String GYROROLL = "GYRO ROLL";
	public String GYROPITCH = "GYRO PITCH";
	public String GYROYAW = "GYRO YAW";

	public String MAGROLL = "MAG ROLL";
	public String MAGPITCH = "MAG PITCH";
	public String MAGYAW = "MAG YAW";

	public String ALT = "ALT";
	public String HEAD = "HEAD";

	private static String GRAPHSTOSHOW = "GRAPHSTOSHOW";
	public String GraphsToShow = ACCROLL + ";" + ACCZ + ";" + ALT + ";" + GYROPITCH;

	// graphs end

	public Notifications notifications;

	private int tempLastI2CErrorCount = 0;
	
	public final static int MAP_OSM = 0;
	public final static int MAP_GOOGLE = 1;
	public final static int MAP_BAIDU = 2;
	public int mMapEngine = MAP_OSM; //0 osm; 1 google; 2 baidu; -1 no any engine installed
	private boolean isGoogleLibInstalled = false;

	@Override
	public void onCreate() {

		Log.d("aaa", "APP ON CREATE");
		super.onCreate();

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		editor = prefs.edit();
		Init();
		try {
			Class.forName("com.google.android.maps.MapActivity");
			isGoogleLibInstalled = true;
		} catch (Exception e) {}
		tts = new TTS(getApplicationContext());

		prepareSounds();

		Say(getString(R.string.Started));

		soundManager.playSound(2);

		notifications = new Notifications(getApplicationContext());

		sensors = new Sensors(getApplicationContext());
		sensors.registerListener(this);
		sensors.start();
	}

	public void Init() {
		ReadSettings();
		ForceLanguage();

		if (CommunicationTypeMW == COMMUNICATION_TYPE_BT) {
			commMW = new BT(getApplicationContext());
		}

		if (CommunicationTypeMW == COMMUNICATION_TYPE_SERIAL_FTDI) {
			commMW = new SerialFTDI(getApplicationContext());
		}

		if (CommunicationTypeMW == COMMUNICATION_TYPE_SERIAL_OTHERCHIPS) {
			commMW = new SerialCDC_ACM(getApplicationContext());
		}

		if (CommunicationTypeFrSky == COMMUNICATION_TYPE_BT) {
			commFrsky = new BT(getApplicationContext());
		}

		if (CommunicationTypeFrSky == COMMUNICATION_TYPE_SERIAL_FTDI) {
			commFrsky = new SerialFTDI(getApplicationContext());
		}

		if (CommunicationTypeFrSky == COMMUNICATION_TYPE_SERIAL_OTHERCHIPS) {
			commFrsky = new SerialCDC_ACM(getApplicationContext());
		}

		SelectProtocol();

	}

    boolean copyFileFromApkToLocal(String src, String dest) {
        AssetManager am = getAssets();
        InputStream ins = null;

        try {
            ins = am.open(src);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        
        FileOutputStream outs = null;
        byte buf[] = new byte[1024];
        int len =0;
        try {
            outs = new FileOutputStream(dest);
            while((len = ins.read(buf)) > 0) {
                outs.write(buf, 0, len);
            }
        } catch (Exception e) {
            Log.e(TAG, "test fail, " + e);
            return false;
        } finally {
            try {
                ins.close();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            try {
                outs.close();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

	static boolean googleMapInstalledAndNeedReboot = false;
	public boolean checkAndGuideGoogleMapLibInstall(Context context) {
		if (mMapEngine != MAP_GOOGLE || isGoogleLibInstalled) return true;

		if (googleMapInstalledAndNeedReboot) {
			Toast.makeText(getApplicationContext(), R.string.googleMapInstalled, Toast.LENGTH_LONG).show();
			return false;
		}

		AlertDialog diag = null;
		final AlertDialog.Builder diagbuilder = new AlertDialog.Builder(context);
		// .setTitle("test")
		diagbuilder.setMessage(R.string.installGoogleMapTip);
		diagbuilder.setNegativeButton(android.R.string.cancel, null);
		diagbuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				boolean isRoot = RootCmd.haveRoot();
				if (!isRoot) {
					Toast.makeText(getApplicationContext(), R.string.notRootTip, Toast.LENGTH_LONG).show();
					return;
				}
				String sysmount = SystemPartition.getSystemMountPiont();
				boolean isWritable = SystemPartition.isWriteable();
				Log.e(TAG, "test sysmount=" + sysmount + ", isWritable=" + isWritable);
				SystemPartition.remountSystem(true);
				isWritable = SystemPartition.isWriteable();
				Log.e(TAG, "test remount, isWritable=" + isWritable);
				String rootDir = "/data/data/" + getPackageName() + "/";
				String file1 = "com.google.android.maps.jar";
				String file2 = "com.google.android.maps.xml";
				boolean ok = copyFileFromApkToLocal(file1, rootDir + file1);
				Log.e(TAG, "test copy " + file1 + ", result=" + ok);
				ok = copyFileFromApkToLocal(file2, rootDir + file2);
				Log.e(TAG, "test copy " + file2 + ", result=" + ok);

				int res = RootCmd.execRootCmdSilent("cp " + rootDir + file1 + " /system/framework/");
				Log.e(TAG, "test copy to system/framework/, result=" + res);
				res += RootCmd.execRootCmdSilent("chmod 777 " + " /system/framework/" + file1);

				res += RootCmd.execRootCmdSilent("cp " + rootDir + file2 + " /system/etc/permissions/");
				res += RootCmd.execRootCmdSilent("chmod 777 " + " /system/etc/permissions/" + file2);
				Log.e(TAG, "test copy to system/etc/permissions/, result=" + res);
				if (res == 0) {
					googleMapInstalledAndNeedReboot = true;
					Toast.makeText(getApplicationContext(), R.string.googleMapInstalled, Toast.LENGTH_LONG).show();
				}
			}
		});
		diagbuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
				// finish();
			}
		});
		diag = diagbuilder.create();
		diag.show();
		return false;
	}

   public void alertInstallEyesFreeTTSData(final Context context) {
	   AlertDialog.Builder alertInstall = new AlertDialog.Builder(context).setTitle(
                            R.string.Warning).setMessage(R.string.noTTSData).setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int which) {
                                            // ����eyes-free���������ݰ�
                                           String ttsDataUrl = "http://eyes-free.googlecode.com/files/com.googlecode.eyesfree.espeak-v1.46.02_r8.apk";
                                            Uri ttsDataUri = Uri.parse(ttsDataUrl);
                                            Intent ttsIntent = new Intent(Intent.ACTION_VIEW,
                                                            ttsDataUri);
                                            context.startActivity(ttsIntent);
                                            Toast.makeText(getApplicationContext(), R.string.ttsDataUseTip, Toast.LENGTH_LONG).show();
                                    }
                            }).setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                            });
            alertInstall.create().show();

    }

	public void SelectProtocol() {

		// if (Protocol == 200) {
		// mw = new MultiWii200(bt);
		// }

		Protocol = 210;

		if (Protocol == 210) {
			mw = new MultiWii210(commMW);
		}

		frskyProtocol = new FrskyProtocol(commFrsky);

		oldActiveModes = new boolean[20];// not the best method
		// mw._1G = _1Gtemp;

	}

	public void ReadSettings() {
		RadioMode = prefs.getInt(RADIOMODE, 2);
		Protocol = prefs.getInt(PROTOCOL, 210);
		MagMode = prefs.getInt(MAGMODE, 1);
		TextToSpeach = prefs.getBoolean(TEXTTOSPEACH, true);
		MacAddress = prefs.getString(MACADDERSS, "");
		MacAddressFrsky = prefs.getString(MACADDERSSFRSKY, "");
		ConnectOnStart = prefs.getBoolean(CONNECTONSTART, false);
		AltCorrection = prefs.getBoolean(ALTCORRECTION, false);
		// AdvancedFunctions = prefs.getBoolean(ADVANCEDFINCTIONS, false);

		AdvancedFunctions = true;//(Sec.VerifyDeveloperID(Sec.GetDeviceID(getApplicationContext()), Sec.TestersIDs));
		if (AdvancedFunctions)
			Toast.makeText(getApplicationContext(), "You are a tester", Toast.LENGTH_SHORT).show();

		DisableBTonExit = prefs.getBoolean(DISABLEBTONEXIT, true);
		// _1Gtemp = prefs.getInt(G1, 256);
		ForceLanguage = prefs.getString(FORCELANGUAGE, "");
		PeriodicSpeaking = prefs.getInt(PERIODICSPEAKING, 20000);
		VoltageAlarm = prefs.getFloat(VOLTAGEALARM, 9.9f);
		GraphsToShow = prefs.getString(GRAPHSTOSHOW, GraphsToShow);
		try {
			mMapEngine = prefs.getInt(USEOFFLINEMAPS, MAP_OSM);
		} catch(Exception e){}
		RefreshRate = prefs.getInt(REFRESHRATE, 100);
		CopyFrskyToMW = prefs.getBoolean(COPYFRSKYTOMW, false);
		AppStartCounter = prefs.getInt(APPSTARTCOUNTER, 0);
		DonateButtonPressed = prefs.getInt(DONATEBUTTONPRESSED, 0);
		ReverseRoll = prefs.getBoolean(REVERSEROLL, false);
		MapZoomLevel = prefs.getInt(MAPZOOMLEVEL, 9);
		MapCenterPeriod = prefs.getInt(MAPCENTERPERIOD, 3);
		CommunicationTypeMW = prefs.getInt(COMMUNICATION_TYPE_MW, COMMUNICATION_TYPE_BT);
		CommunicationTypeFrSky = prefs.getInt(COMMUNICATION_TYPE_FRSKY, COMMUNICATION_TYPE_BT);
		SerialPortBaudRateMW = prefs.getString(SERIAL_PORT_BAUD_RATE_MW, "115200");
		SerialPortBaudRateFrSky = prefs.getString(SERIAL_PORT_BAUD_RATE_FRSKY, "9600");



	}

	public void SaveSettings(boolean quiet) {
		editor.putInt(RADIOMODE, RadioMode);
		editor.putInt(PROTOCOL, Protocol);
		editor.putInt(MAGMODE, MagMode);
		editor.putBoolean(TEXTTOSPEACH, TextToSpeach);
		editor.putString(MACADDERSS, MacAddress);
		editor.putString(MACADDERSSFRSKY, MacAddressFrsky);
		editor.putBoolean(CONNECTONSTART, ConnectOnStart);
		editor.putBoolean(ALTCORRECTION, AltCorrection);
		// editor.putBoolean(ADVANCEDFINCTIONS, AdvancedFunctions);
		editor.putBoolean(DISABLEBTONEXIT, DisableBTonExit);
		// editor.putInt(G1, mw._1G);
		editor.putString(FORCELANGUAGE, ForceLanguage);
		editor.putInt(PERIODICSPEAKING, PeriodicSpeaking);
		editor.putFloat(VOLTAGEALARM, VoltageAlarm);
		editor.putString(GRAPHSTOSHOW, GraphsToShow);
		editor.putInt(USEOFFLINEMAPS, mMapEngine);
		editor.putInt(REFRESHRATE, RefreshRate);
		editor.putBoolean(COPYFRSKYTOMW, CopyFrskyToMW);
		editor.putInt(APPSTARTCOUNTER, AppStartCounter);
		editor.putInt(DONATEBUTTONPRESSED, DonateButtonPressed);
		editor.putBoolean(REVERSEROLL, ReverseRoll);
		editor.putInt(MAPZOOMLEVEL, MapZoomLevel);
		editor.putInt(MAPCENTERPERIOD, MapCenterPeriod);
		editor.putInt(COMMUNICATION_TYPE_MW, CommunicationTypeMW);
		editor.putString(SERIAL_PORT_BAUD_RATE_MW, SerialPortBaudRateMW);
		editor.commit();

		if (!quiet) {
			Toast.makeText(getApplicationContext(), getString(R.string.Settingssaved), Toast.LENGTH_LONG).show();
			Say(getString(R.string.Settingssaved));
		}
	}

	@Override
	public void onTerminate() {
		sensors.stop();
		mw.CloseLoggingFile();
		super.onTerminate();

	}

	public void Say(String text) {
		if (TextToSpeach)
			tts.Speak(text);
	}

	public void Frequentjobs() {

		// rssi
		if (!commFrsky.Connected && commMW.Connected) {
			frskyProtocol.TxRSSI = Functions.map(mw.rssi, 0, 1024, 0, 110);
		}

		// Copy data from FrSky
		if (CopyFrskyToMW && commFrsky.Connected && !commMW.Connected)
			FrskyToMW();

		// Say battery level every xx seconds
		if (PeriodicSpeaking > 0 && (commMW.Connected || commFrsky.Connected) && timer1 < System.currentTimeMillis()) {
			timer1 = System.currentTimeMillis() + PeriodicSpeaking;
			if (mw.bytevbat > 10) {
				Say(getString(R.string.BatteryLevelIs) + " " + String.valueOf((float) (mw.bytevbat / 10f)) + " " + getString(R.string.TTS_Volts));
			}

			if (mw.alt != 0) {
				Say(getString(R.string.TTS_Altitude) + " " + String.valueOf((int) mw.alt) + " " + getString(R.string.TTS_Meters));
			}
		}

		// beep when low battery
		if (mw.bytevbat > 10 && VoltageAlarm > 0 && (commMW.Connected || commFrsky.Connected) && timer2 < System.currentTimeMillis() && (float) (mw.bytevbat / 10f) < VoltageAlarm) {
			timer2 = System.currentTimeMillis() + timer2Freq;
			soundManager.playSound(0);
		}

		// ===================timer every 1sek===============================
		if (timer3 < System.currentTimeMillis()) {
			timer3 = System.currentTimeMillis() + timer3Freq;
			
			
			// Notifications
			if (mw.i2cError != tempLastI2CErrorCount) {
				notifications.displayNotification(getString(R.string.Warning), "I2C Error=" + String.valueOf(mw.i2cError), true, 1, false);
				tempLastI2CErrorCount = mw.i2cError;
			}

			if (mw.DataFlow < 0) {
				notifications.displayNotification(getString(R.string.Warning), getString(R.string.NoDataRecieved) + " " + String.valueOf(mw.DataFlow), true, 1, false);

			}

			// Checkboxes speaking; ON OFF
			for (int i = 0; i < mw.CHECKBOXITEMS; i++) {
				if (mw.ActiveModes[i] != oldActiveModes[i]) {
					String s = "";
					if (mw.ActiveModes[i]) {
						s = getString(R.string.isON);
						soundManager.playSound(2);
					} else {
						s = getString(R.string.isOFF);
					}

					Say((mw.buttonCheckboxLabel[i] + s).toLowerCase());

					if (mw.buttonCheckboxLabel[i].equals("ARM")) {
						soundManager.playSound(1);
					}

					if (mw.buttonCheckboxLabel[i].equals("ARM") && AltCorrection) {
						mw.AltCorrection = mw.alt;
					}

					if (!AltCorrection)
						mw.AltCorrection = 0;
				}
				oldActiveModes[i] = mw.ActiveModes[i];
			}

			// followHeading
			if (FollowHeading) {
				mw.SendRequestMSP_SET_HEAD((int) sensors.Heading);
				FollowHeadingBlinkFlag = !FollowHeadingBlinkFlag;
			}

		}
		// --------------------END timer every 1sek---------------------------

		// ===================timer every 5sek===============================
		if (timer4 < System.currentTimeMillis()) {
			timer4 = System.currentTimeMillis() + timer4Freq;

			// Reconecting
			if (commMW.ConnectionLost) {
				if (commMW.ReconnectTry < 1) {
					tts.Speak(getString(R.string.Reconnecting));
					commMW.Connect(MacAddress);
					commMW.ReconnectTry++;
				}
			}

			// update Home position
			if (commMW.Connected) {
				mw.SendRequestMSP_WP(0);

				for (int i = 0; i < mw.CHECKBOXITEMS; i++) {
					if (mw.buttonCheckboxLabel[i].equals("GPS HOLD")) {
						if (mw.ActiveModes[i]) {
							// update Position hold
							mw.SendRequestMSP_WP(16);
						} else {
							mw.Waypoints[16].Lat = 0;
							mw.Waypoints[16].Lon = 0;
						}
					}
				}
			}

			String t = new String();
			if (sensors.MockLocationWorking)
				t += getString(R.string.MockLocationIsWorking) + ";";
			if (FollowMeEnable)
				t += getString(R.string.Follow_Me) + ";";
			if (InjectGPSEnable)
				t += "InjectGPS" + ";";
			if (FollowHeading)
				t += getString(R.string.Follow_Heading);

			notifications.displayNotification("Status", t, false, 99, false);

		}
		// --------------------END timer every 5sek---------------------------
	}

	private void prepareSounds() {
		soundManager = new SoundManager(getApplicationContext());
		soundManager.addSound(0, R.raw.alarma);
		soundManager.addSound(1, R.raw.alert1);
		soundManager.addSound(2, R.raw.blip);
	}

	public void ForceLanguage() {
		if (!ForceLanguage.equals("")) {
			String languageToLoad = ForceLanguage;
			Locale locale = new Locale(languageToLoad);
			Locale.setDefault(locale);
			Configuration config = new Configuration();
			config.locale = locale;
			getBaseContext().getResources().updateConfiguration(config, null);
		}
	}

	public void ConnectionBug() { // autoconnect again when new activity is
									// started
		if (ConnectOnStart && !commMW.Connected) {
			commMW.Connect(MacAddress);
			Say(getString(R.string.menu_connect));
		}
	}

	private void FrskyToMW() {
		mw.angx = frskyProtocol.frskyHubProtocol.angX;
		mw.angy = frskyProtocol.frskyHubProtocol.angY;

		mw.ax = frskyProtocol.frskyHubProtocol.Acc_X;
		mw.ay = frskyProtocol.frskyHubProtocol.Acc_Y;
		mw.az = frskyProtocol.frskyHubProtocol.Acc_Z;

		mw.head = frskyProtocol.frskyHubProtocol.Heading;
		mw.GPS_numSat = frskyProtocol.frskyHubProtocol.Temperature_1;
		mw.GPS_speed = frskyProtocol.frskyHubProtocol.GPS_Speed;

		mw.GPS_latitude = (int) frskyProtocol.frskyHubProtocol.GPS_Latitude;
		mw.GPS_longitude = (int) frskyProtocol.frskyHubProtocol.GPS_Longtitude;

		mw.alt = frskyProtocol.frskyHubProtocol.Altitude;

		mw.bytevbat = (byte) frskyProtocol.frskyHubProtocol.Voltage;
	}

	@Override
	public void onSensorsStateChangeMagAcc() {
	}

	@Override
	public void onSensorsStateGPSLocationChange() {
		if (FollowMeEnable) {
			// TODO needs more work here
			mw.SendRequestMSP_SET_WP(new Waypoint(0, (int) (sensors.geopointOfflineMapCurrentPosition.getLatitudeE6() * 10), (int) (sensors.getNextPredictedLocationOfflineMap().getLongitudeE6() * 10), 0, 0, 0, 0));
			mw.SendRequestMSP_SET_WP(new Waypoint(16, (int) (sensors.geopointOfflineMapCurrentPosition.getLatitudeE6() * 10), (int) (sensors.getNextPredictedLocationOfflineMap().getLongitudeE6() * 10), 0, 0, 0, 0));

			FollowMeBlinkFlag = !FollowMeBlinkFlag;
		}

		if (InjectGPSEnable) {
			mw.SendRequestMSP_SET_RAW_GPS((byte) sensors.PhoneFix, (byte) sensors.PhoneNumSat, (int) (sensors.PhoneLatitude * 1e7), (int) (sensors.PhoneLongitude * 1e7), (int) sensors.PhoneAltitude, (int) sensors.PhoneSpeed);
			InjectGPSBlinkFlag = !InjectGPSBlinkFlag;
		}

	}

	@Override
	public void onSensorsStateGPSStatusChange() {

	}
	
	public void OpenInfoOnClick(View v) {
		{
			Log.d("aaa", "OpenInfoOnClick " + v.getTag().toString());
			final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(v.getTag().toString()));
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		}
	}
	
	

}
