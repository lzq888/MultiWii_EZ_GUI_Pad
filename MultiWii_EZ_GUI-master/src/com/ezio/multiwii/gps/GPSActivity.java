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
package com.ezio.multiwii.gps;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.ezio.multiwii.R;
import com.ezio.multiwii.app.App;
import com.ezio.multiwii.waypoints.Waypoint;

public class GPSActivity extends SherlockActivity {

	private boolean killme = false;

	App app;
	Handler mHandler = new Handler();

	TextView GPS_distanceToHomeTV;
	TextView GPS_directionToHomeTV;
	TextView GPS_numSatTV;
	TextView GPS_fixTV;
	TextView GPS_updateTV;

	TextView GPS_altitudeTV;
	TextView GPS_speedTV;
	TextView GPS_latitudeTV;
	TextView GPS_longitudeTV;

	TextView PhoneLatitudeTV;
	TextView PhoneLongtitudeTV;
	TextView PhoneAltitudeTV;
	TextView PhoneSpeedTV;
	TextView PhoneNumSatTV;
	TextView DeclinationTV;
	TextView PhoneAccuracyTV;

	CheckBox CheckBoxInjectGPS;
	CheckBox CheckBoxFollowMe;
	CheckBox CheckBoxFollowHeading;

	TextView FollowMeInfoTV;

	TextView PhoneHeadingTV;
	TextView HeadingTV;

	private Runnable update = new Runnable() {
		@Override
		public void run() {

			app.mw.ProcessSerialData(app.loggingON);
			app.frskyProtocol.ProcessSerialData(false);

			float lat = (float) (app.mw.GPS_latitude / Math.pow(10, 7));
			float lon = (float) (app.mw.GPS_longitude / Math.pow(10, 7));

			GPS_distanceToHomeTV.setText(String.valueOf(app.mw.GPS_distanceToHome));
			GPS_directionToHomeTV.setText(String.valueOf(app.mw.GPS_directionToHome));
			GPS_numSatTV.setText(String.valueOf(app.mw.GPS_numSat));
			GPS_fixTV.setText(String.valueOf(app.mw.GPS_fix));
			// GPS_updateTV.setText(String.valueOf(app.mw.GPS_update));

			if (app.mw.GPS_update % 2 == 0) {
				GPS_updateTV.setBackgroundColor(Color.GREEN);
			} else {
				GPS_updateTV.setBackgroundColor(Color.TRANSPARENT);
			}

			GPS_altitudeTV.setText(String.valueOf(app.mw.GPS_altitude));
			GPS_speedTV.setText(String.valueOf(app.mw.GPS_speed));

			GPS_latitudeTV.setText(String.valueOf(lat));
			GPS_longitudeTV.setText(String.valueOf(lon));

			PhoneLatitudeTV.setText(String.valueOf((float) app.sensors.PhoneLatitude));
			PhoneLongtitudeTV.setText(String.valueOf((float) app.sensors.PhoneLongitude));
			PhoneAltitudeTV.setText(String.valueOf((int) app.sensors.PhoneAltitude));
			PhoneSpeedTV.setText(String.valueOf(app.sensors.PhoneSpeed));
			PhoneNumSatTV.setText(String.valueOf(app.sensors.PhoneNumSat));
			PhoneAccuracyTV.setText(String.valueOf(app.sensors.PhoneAccuracy));
			DeclinationTV.setText(String.valueOf(app.sensors.Declination));
			PhoneHeadingTV.setText(String.valueOf(app.sensors.Heading));
			HeadingTV.setText(String.valueOf(app.mw.head));

			FollowMeInfoTV.setText("WayPointsDebug:\n");
			Waypoint w = app.mw.Waypoints[0];
			FollowMeInfoTV.append("No:" + String.valueOf(w.Number) + " Lat:" + String.valueOf(w.Lat) + " Lon:" + String.valueOf(w.Lon) + " Alt:" + String.valueOf(w.Alt) + " NavFlag:" + String.valueOf(w.NavFlag) + "\n");
			w = app.mw.Waypoints[16];
			FollowMeInfoTV.append("No:" + String.valueOf(w.Number) + " Lat:" + String.valueOf(w.Lat) + " Lon:" + String.valueOf(w.Lon) + " Alt:" + String.valueOf(w.Alt) + " NavFlag:" + String.valueOf(w.NavFlag) + "\n");

			if (app.FollowMeBlinkFlag) {
				CheckBoxFollowMe.setBackgroundColor(Color.GREEN);
			} else {
				CheckBoxFollowMe.setBackgroundColor(Color.TRANSPARENT);
			}
			if (app.InjectGPSBlinkFlag) {
				CheckBoxInjectGPS.setBackgroundColor(Color.GREEN);
			} else {
				CheckBoxInjectGPS.setBackgroundColor(Color.TRANSPARENT);
			}
			if (app.FollowHeadingBlinkFlag) {
				CheckBoxFollowHeading.setBackgroundColor(Color.GREEN);
			} else {
				CheckBoxFollowHeading.setBackgroundColor(Color.TRANSPARENT);
			}

			app.Frequentjobs();

			app.mw.SendRequest();
			// app.mw.SendRequestGetWayPoint(0);
			if (!killme)
				mHandler.postDelayed(update, app.RefreshRate);

			Log.d(app.TAG, "loop " + this.getClass().getName());

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.gps_layout);

		app = (App) getApplication();

		GPS_distanceToHomeTV = (TextView) findViewById(R.id.TextViewGPS_distanceToHome);
		GPS_directionToHomeTV = (TextView) findViewById(R.id.TextViewGPS_directionToHome);
		GPS_numSatTV = (TextView) findViewById(R.id.TextViewGPS_numSat);
		GPS_fixTV = (TextView) findViewById(R.id.TextViewGPS_fix);
		GPS_updateTV = (TextView) findViewById(R.id.TextViewGPS_update);

		GPS_altitudeTV = (TextView) findViewById(R.id.TextViewGPS_altitude);
		GPS_speedTV = (TextView) findViewById(R.id.TextViewGPS_speed);
		GPS_latitudeTV = (TextView) findViewById(R.id.TextViewGPS_latitude);
		GPS_longitudeTV = (TextView) findViewById(R.id.TextViewGPS_longitude);

		PhoneLatitudeTV = (TextView) findViewById(R.id.textViewPhoneLatitude);
		PhoneLongtitudeTV = (TextView) findViewById(R.id.textViewPhoneLongitude);
		PhoneAltitudeTV = (TextView) findViewById(R.id.TextViewPhoneAltitude);
		PhoneSpeedTV = (TextView) findViewById(R.id.textViewPhoneSpeed);
		PhoneNumSatTV = (TextView) findViewById(R.id.textViewPhoneNumSat);
		PhoneAccuracyTV = (TextView) findViewById(R.id.textViewPhoneAccuracy);

		CheckBoxInjectGPS = (CheckBox) findViewById(R.id.checkBoxInjectGPS);
		CheckBoxFollowMe = (CheckBox) findViewById(R.id.checkBoxFollowMe);
		CheckBoxFollowHeading = (CheckBox) findViewById(R.id.checkBoxFollowHeading);
		DeclinationTV = (TextView) findViewById(R.id.textViewDeclination);
		FollowMeInfoTV = (TextView) findViewById(R.id.textViewFollowMeInfo);
		PhoneHeadingTV = (TextView) findViewById(R.id.TextViewPhoneHead);
		HeadingTV = (TextView) findViewById(R.id.TextViewHeading);
		if (!app.AdvancedFunctions) {
			CheckBoxInjectGPS.setVisibility(View.GONE);
			//CheckBoxFollowHeading.setVisibility(View.GONE);
			//((LinearLayout) findViewById(R.id.MockLayout)).setVisibility(View.GONE);
		}

	}

	public void SHOWHIDDENOncLick(View v) {
		CheckBoxInjectGPS.setVisibility(View.VISIBLE);
		CheckBoxFollowMe.setVisibility(View.VISIBLE);
		FollowMeInfoTV.setVisibility(View.VISIBLE);
		CheckBoxFollowHeading.setVisibility(View.VISIBLE);
		((LinearLayout) findViewById(R.id.MockLayout)).setVisibility(View.VISIBLE);

		app.AdvancedFunctions = true;

		Toast.makeText(getApplicationContext(), "Not tested features activated", Toast.LENGTH_LONG).show();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mHandler.removeCallbacks(update);
		killme = true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		app.ForceLanguage();

		try {
			stopService(new Intent(getApplicationContext(), MOCK_GPS_Service.class));
		} catch (Exception e) {
			// TODO: handle exception
		}

		killme = false;
		mHandler.postDelayed(update, app.RefreshRate);

		CheckBoxFollowMe.setChecked(app.FollowMeEnable);
		CheckBoxInjectGPS.setChecked(app.InjectGPSEnable);

		app.Say(getString(R.string.GPS));

	}

	public void FollowMeCheckBoxOnClick(View v) {
		app.FollowMeEnable = CheckBoxFollowMe.isChecked();
	}

	public void InjectGPSCheckBoxOnClick(View v) {
		app.InjectGPSEnable = CheckBoxInjectGPS.isChecked();
	}

	public void FollowHeadingCheckBoxOnClick(View v) {
		app.FollowHeading = CheckBoxFollowHeading.isChecked();
	}

	public void StartMOCKLocationServiceOnClick(View v) {
		Intent service = new Intent(getApplicationContext(), MOCK_GPS_Service.class);
		startService(service);

		Intent startMain = new Intent(Intent.ACTION_MAIN);
		startMain.addCategory(Intent.CATEGORY_HOME);
		startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(startMain);
	}

}
