package jp.ddo.t2gmon.httpshutter;

import java.util.List;

import jp.ddo.t2gmon.httpshutter.http.HttpServer;
import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
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
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // フルスクリーンにして，タイトルも消す
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		cameraView = new CameraView(this);
		LinearLayout l = new LinearLayout(this);
		l.addView(cameraView);
        setContentView(l);
        server = new HttpServer(this, cameraView);
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

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		int itemId = item.getItemId();
		if (10 <= itemId && itemId < 10 + numOfSupportedPreviewSize) {
			Camera.Size tmpSize = cameraView.getSupportedPreviewSize().get(itemId - 10);
			cameraView.setPreviewSize(tmpSize.width, tmpSize.height);
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