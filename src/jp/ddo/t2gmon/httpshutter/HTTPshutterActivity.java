/*
 * HttpShutter - Take a photo via http access
 * Copyright (C) 2012 TSUJIMOTO Takaaki
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses/>.
 */

package jp.ddo.t2gmon.httpshutter;

import java.util.List;

import jp.ddo.t2gmon.httpshutter.http.HttpServer;
import android.app.Activity;
import android.hardware.Camera;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class HTTPshutterActivity extends Activity {
	private HttpServer server = null;
	private CameraView cameraView = null;
	private int numOfSupportedPreviewSize;
	private int numOfSupportedPictureSize;
	private int numOfSupportedFlashMode;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // フルスクリーンにして，タイトルも消す
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		cameraView = new CameraView(this);
		LinearLayout l = new LinearLayout(this);
		l.addView(cameraView);
        setContentView(l);
        server = new HttpServer(cameraView);
        server.start();
    }

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		super.onPrepareOptionsMenu(menu);

		SubMenu previewSize = menu.addSubMenu(Menu.NONE, 1, 0, "Preview Size");
		List<Camera.Size> supportedPreviewSize = cameraView.getSupportedPreviewSize();
		numOfSupportedPreviewSize = supportedPreviewSize.size();
		for (int i = 0; i < supportedPreviewSize.size(); i++) {
			Camera.Size tmpSize = supportedPreviewSize.get(i);
			previewSize.add(Menu.NONE, 10 + i, 0, tmpSize.width + " x " + tmpSize.height);
		}
		SubMenu pictureSize = menu.addSubMenu(Menu.NONE, 2, 0, "Picture Size");
		List<Camera.Size> supportedPictureSize = cameraView.getSupportedPictureSize();
		numOfSupportedPictureSize = supportedPictureSize.size();
		for (int i = 0; i < supportedPictureSize.size(); i++) {
			Camera.Size tmpSize = supportedPictureSize.get(i);
			pictureSize.add(Menu.NONE, 10 + numOfSupportedPreviewSize + i, 0, tmpSize.width + " x " + tmpSize.height);
		}
		SubMenu flashMode = menu.addSubMenu(Menu.NONE, 3, 0, "Flash Mode");
		List<String> supportedFlashMode = cameraView.getSupportedFlashMode();
		numOfSupportedFlashMode = supportedFlashMode.size();
		for (int i = 0; i < supportedFlashMode.size(); i++) {
			String tmpFlashMode = supportedFlashMode.get(i);
			flashMode.add(Menu.NONE, 10 + numOfSupportedPreviewSize + numOfSupportedPictureSize + i,
					0, tmpFlashMode);
		}
		menu.add(Menu.NONE, 0, 0, "Auto Focus");

		// 現在の設定情報
		WifiManager wifiManager = (WifiManager)getSystemService(WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int ipAddress = wifiInfo.getIpAddress();
		SubMenu preferences = menu.addSubMenu(Menu.NONE, 4, 0, "Preferences");
		String strIPAddess = ((ipAddress >> 0) & 0xFF) + "." + ((ipAddress >> 8) & 0xFF) + "." +
			    ((ipAddress >> 16) & 0xFF) + "." + ((ipAddress >> 24) & 0xFF);
		preferences.add(Menu.NONE, 97, 0, strIPAddess + ":8080");
		Camera.Size cameraSize = cameraView.getPreviewSize();
		preferences.add(Menu.NONE, 98, 0, "Preview Size: " + cameraSize.width + "x" + cameraSize.height);
		cameraSize = cameraView.getPictureSize();
		preferences.add(Menu.NONE, 99, 0, "Picture Size: " + cameraSize.width + "x" + cameraSize.height);

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		int itemId = item.getItemId();
		if (10 <= itemId && itemId < 10 + numOfSupportedPreviewSize) {
			Camera.Size tmpSize = cameraView.getSupportedPreviewSize().get(itemId - 10);
			cameraView.setPreviewSize(tmpSize.width, tmpSize.height);
		}
		else if (10 + numOfSupportedPreviewSize <= itemId
				&& itemId < 10 + numOfSupportedPreviewSize + numOfSupportedPictureSize) {
			Camera.Size tmpSize = cameraView.getSupportedPictureSize().get(itemId - 10 - numOfSupportedPreviewSize);
			cameraView.setPictureSize(tmpSize.width, tmpSize.height);
		}
		else if (10 + numOfSupportedPreviewSize + numOfSupportedPictureSize <= itemId
				&& itemId < 10 + numOfSupportedPreviewSize + numOfSupportedPictureSize + numOfSupportedFlashMode) {
			String tmpFlashMode = cameraView.getSupportedFlashMode().get(itemId - 10
					- numOfSupportedPreviewSize - numOfSupportedPictureSize);
			cameraView.setFlashMode(tmpFlashMode);
		}
		else if (itemId == 0) {
			cameraView.doAutofocus();
		}
		return super.onMenuItemSelected(featureId, item);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		server.close();
	}
}