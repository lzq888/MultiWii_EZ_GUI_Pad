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
package com.ezio.multiwii.graph;

import java.util.ArrayList;
import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;

import com.actionbarsherlock.app.SherlockActivity;
//import com.actionbarsherlock.view.Menu;
//import com.actionbarsherlock.view.MenuInflater;
//import com.actionbarsherlock.view.MenuItem;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;

import com.ezio.multiwii.R;
import com.ezio.multiwii.app.App;
import com.ezio.multiwii.graph.GraphView.GraphViewData;
import com.ezio.multiwii.graph.GraphView.LegendAlign;
import com.ezio.multiwii.graph.GraphViewSeries.GraphViewSeriesStyle;

public class PadGraphsActivity {

	private boolean killme = false;
    public Activity mContext;
    public App app;
    public Handler mHandler;

    LineGraphView graphView;
	ArrayList<GraphViewSeries> series = new ArrayList<GraphViewSeries>();

	Random rnd = new Random();

    final static int REFRESH_RATE = 5000;
	int CurentPosition = 0;
	int NextLimit = REFRESH_RATE;
	
	boolean pause = false;

	private Runnable update = new Runnable() {
		@Override
		public void run() {

			app.mw.ProcessSerialData(app.loggingON);
			app.frskyProtocol.ProcessSerialData(false);
			app.Frequentjobs();

			update();
			
			app.mw.SendRequest();
			if (!killme)
				mHandler.postDelayed(update, 100);
			
			if(app.D)	Log.d(app.TAG, "loop "+this.getClass().getName());

		}
	};

	public void update() {
		if (!pause) {
			CurentPosition++;
			if (CurentPosition == NextLimit) {
				for (GraphViewSeries s : series) {
					s.resetData(new GraphViewData[] { new GraphViewData(CurentPosition, 0) });
				}
				NextLimit = CurentPosition + REFRESH_RATE;
			}

			// debug
			// app.mw.ax = rnd.nextFloat();
			// app.mw.ay = rnd.nextFloat();
			// app.mw.az = rnd.nextFloat();
			//
			// app.mw.gx = rnd.nextFloat();
			// app.mw.gy = rnd.nextFloat();
			// app.mw.gz = rnd.nextFloat();
			//
			// app.mw.magx = rnd.nextFloat();
			// app.mw.magy = rnd.nextFloat();
			// app.mw.magz = rnd.nextFloat();
			//
			// app.mw.alt = rnd.nextFloat();
			// app.mw.head = rnd.nextFloat();
			// //////

			for (GraphViewSeries s : series) {

				if (s.key.equals(app.ACCROLL))
					s.appendData(new GraphViewData(CurentPosition, app.mw.ax), true);

				if (s.key.equals(app.ACCPITCH))
					s.appendData(new GraphViewData(CurentPosition, app.mw.ay), true);

				if (s.key.equals(app.ACCZ))
					s.appendData(new GraphViewData(CurentPosition, app.mw.az), true);

				// /
				if (s.key.equals(app.GYROROLL))
					s.appendData(new GraphViewData(CurentPosition, app.mw.gx), true);
				if (s.key.equals(app.GYROPITCH))
					s.appendData(new GraphViewData(CurentPosition, app.mw.gy), true);
				if (s.key.equals(app.GYROYAW))
					s.appendData(new GraphViewData(CurentPosition, app.mw.gz), true);
				// /
				if (s.key.equals(app.MAGROLL))
					s.appendData(new GraphViewData(CurentPosition, app.mw.magx), true);
				if (s.key.equals(app.MAGPITCH))
					s.appendData(new GraphViewData(CurentPosition, app.mw.magy), true);
				if (s.key.equals(app.MAGYAW))
					s.appendData(new GraphViewData(CurentPosition, app.mw.magz), true);
				// /
				if (s.key.equals(app.ALT))
					s.appendData(new GraphViewData(CurentPosition, app.mw.alt), true);

				if (s.key.equals(app.HEAD))
					s.appendData(new GraphViewData(CurentPosition, app.mw.head), true);
			}
		}		
	}
	
	protected void onCreate(Bundle savedInstanceState) {

	}

	void graphInit() {

		CurentPosition = 0;
		NextLimit = REFRESH_RATE;

		if (graphView == null) {
			graphView = new LineGraphView(mContext.getApplicationContext(), mContext.getString(R.string.Graphs));
			graphView.setViewPort(1, 100);
			graphView.setScalable(true);
			graphView.setShowLegend(true);
			graphView.setLegendAlign(LegendAlign.BOTTOM);
			LinearLayout a = (LinearLayout) mContext.findViewById(R.id.graphy);
			a.addView(graphView);
		}
		
		for (GraphViewSeries s : series) {
			graphView.removeSeries(s);
		}
		
		series = new ArrayList<GraphViewSeries>();
		
		String gr = app.GraphsToShow;

		if (gr.contains(app.ACCROLL))
			series.add(new GraphViewSeries(app.ACCROLL, mContext.getString(R.string.ACCROLL), new GraphViewSeriesStyle(Color.RED, 3), new GraphViewData[] { new GraphViewData(0, 0) }));

		if (gr.contains(app.ACCPITCH))
			series.add(new GraphViewSeries(app.ACCPITCH, mContext.getString(R.string.ACCPITCH), new GraphViewSeriesStyle(Color.GREEN, 3), new GraphViewData[] { new GraphViewData(0, 0) }));
		if (gr.contains(app.ACCZ))
			series.add(new GraphViewSeries(app.ACCZ, mContext.getString(R.string.ACCZ), new GraphViewSeriesStyle(Color.BLUE, 3), new GraphViewData[] { new GraphViewData(0, 0) }));

		if (gr.contains(app.GYROROLL))
			series.add(new GraphViewSeries(app.GYROROLL, mContext.getString(R.string.GYROROLL), new GraphViewSeriesStyle(Color.rgb(196, 201, 0), 3), new GraphViewData[] { new GraphViewData(0, 0) }));
		if (gr.contains(app.GYROPITCH))
			series.add(new GraphViewSeries(app.GYROPITCH, mContext.getString(R.string.GYROPITCH), new GraphViewSeriesStyle(Color.rgb(0, 255, 255), 3), new GraphViewData[] { new GraphViewData(0, 0) }));
		if (gr.contains(app.GYROYAW))
			series.add(new GraphViewSeries(app.GYROYAW, mContext.getString(R.string.GYROYAW), new GraphViewSeriesStyle(Color.rgb(255, 0, 255), 3), new GraphViewData[] { new GraphViewData(0, 0) }));

		if (gr.contains(app.MAGROLL))
			series.add(new GraphViewSeries(app.MAGROLL, mContext.getString(R.string.MAGROLL), new GraphViewSeriesStyle(Color.rgb(52, 101, 144), 3), new GraphViewData[] { new GraphViewData(0, 0) }));
		if (gr.contains(app.MAGPITCH))
			series.add(new GraphViewSeries(app.MAGPITCH, mContext.getString(R.string.MAGPITCH), new GraphViewSeriesStyle(Color.rgb(98, 51, 149), 3), new GraphViewData[] { new GraphViewData(0, 0) }));
		if (gr.contains(app.MAGYAW))
			series.add(new GraphViewSeries(app.MAGYAW, mContext.getString(R.string.MAGYAW), new GraphViewSeriesStyle(Color.rgb(150, 100, 49), 3), new GraphViewData[] { new GraphViewData(0, 0) }));

		if (gr.contains(app.ALT))
			series.add(new GraphViewSeries(app.ALT, mContext.getString(R.string.ALT), new GraphViewSeriesStyle(Color.rgb(130, 122, 125), 3), new GraphViewData[] { new GraphViewData(0, 0) }));
		if (gr.contains(app.HEAD))
			series.add(new GraphViewSeries(app.HEAD, mContext.getString(R.string.HEAD), new GraphViewSeriesStyle(Color.rgb(255, 226, 124), 3), new GraphViewData[] { new GraphViewData(0, 0) }));

		for (GraphViewSeries s : series) {
			graphView.addSeries(s);
		}

		//setContentView(graphView);

	}

	protected void onResume() {
		graphInit();
		//killme = false;
		//mHandler.postDelayed(update, 100);

	}

	protected void onPause() {
		mHandler.removeCallbacks(null);
		//killme = true;
	}

	// /////menu////////
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = mContext.getMenuInflater();
		inflater.inflate(R.menu.menu_graphs, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.MenuGraphsShow) {
			mContext.startActivity(new Intent(mContext.getApplicationContext(), SelectToShowActivity.class));
			return true;
		}

		if (item.getItemId() == R.id.MenuGraphsPause) {
			pause = !pause;
		}
		return false;
	}
	// ///menu end//////
	
	public void PauseOnClick() {
		pause = !pause;
		Button b1 = (Button)mContext.findViewById(R.id.graphy_bt1);
		b1.setText(pause?R.string.Started:R.string.Pause);
	}
	
	public void ShowOnClick() {
		mContext.startActivity(new Intent(mContext.getApplicationContext(), SelectToShowActivity.class));
	}
	
    public void show() {
    	onCreate(null);
    	onResume();
    }
}
