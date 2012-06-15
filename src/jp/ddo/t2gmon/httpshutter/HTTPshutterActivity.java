package jp.ddo.t2gmon.httpshutter;

import jp.ddo.t2gmon.httpshutter.http.HttpServer;
import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class HTTPshutterActivity extends Activity {
	private HttpServer server = null;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // フルスクリーンにして，タイトルも消す
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		LinearLayout l = new LinearLayout(this);
		l.addView(new CameraView(this));
        setContentView(l);
        server = new HttpServer(this);
        server.start();
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