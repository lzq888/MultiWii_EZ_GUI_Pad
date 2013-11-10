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
package com.ezio.multiwii.about;

import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.ezio.multiwii.R;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class AboutActivity extends SherlockActivity {

	TextView TextViewCopyRights;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_layout);

		TextViewCopyRights = (TextView) findViewById(R.id.textViewCopyRights);
		String s = "<b>MultiWii EZ-GUI</b><br>" + "Copyright (C) <2012>  Bartosz Szczygiel (eziosoft)<br><br>" + " This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version." + "<br>" + "This program is distributed in the hope that it will be useful,  but <b>WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE</b>." + "<br>See the GNU General Public License for more details.<br><br>" + "You should have received a copy of the GNU General Public License along with this program.  If not, see http://www.gnu.org/licenses/" + "<br><br><b>Libraries:</b><br>ActionBarSherlock,<br>ViewPagerIndicator,<br>Osmdroid,<br>slf4j,<br>GraphView by jjoe64,<br>FTDI Driver by @ksksue,<br>china feature by sufferpriest(qq:110671958)";

		TextViewCopyRights.setText(Html.fromHtml(s));
		String gms = GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(this);
		if (gms != null)
		TextViewCopyRights.append(gms);
	}
}
