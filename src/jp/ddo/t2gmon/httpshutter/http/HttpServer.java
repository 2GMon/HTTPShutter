package jp.ddo.t2gmon.httpshutter.http;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import android.content.Context;
import android.util.Log;

public class HttpServer extends Thread {
	private Context context;
	private ServerSocket serverSocket = null;

	public HttpServer(Context context) {
		this.context = context;
	}
	
	public void run() {
		Socket socket = null;
		
		try {
			Log.v("httpshutter_server", "Server start.");
			serverSocket = new ServerSocket();
			serverSocket.setReuseAddress(true);
			serverSocket.bind(new InetSocketAddress(8080));
			
			while (true) {
				socket = serverSocket.accept();
				new ServerProcess(socket).start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// HttpServer.close()が実行されたあともう一回ServerSocket.closeが実行されるのを防ぐために
			// 少し待つ
			try {
				HttpServer.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (serverSocket != null) {
				try {
					serverSocket.close();
					serverSocket = null;
					Log.v("httpshutter_server", "ServerSocket is closed. (finally)");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		Log.v("httpshutter_server", "Server end");
	}
	
	// 外部からサーバーを終了できるようにする
	public void close() {
		if (serverSocket == null) {
			return;
		}
		else {
			try {
				serverSocket.close();
				serverSocket = null;
				Log.v("httpshutter_server", "ServerSocket is closed. (close())");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private class ServerProcess extends Thread {
		private final int bufferSize = 2000;
		private Socket socket;
		
		public ServerProcess(Socket socket) {
			this.socket = socket;
		}
	
		public void run() {
			InputStream stream = null;
			byte buffer[] = new byte[bufferSize];
			int length;
			StringBuffer recvMessage = new StringBuffer();

			Log.v("httpshutter_ServerProcess", "ServerProcess start.");
			try {
				stream = socket.getInputStream();
				while (true) {
					length = stream.read(buffer);
					if (length > 0)
						recvMessage.append(buffer.toString());
					else
						break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			Log.v("httpshutther_server", "Recived: " + recvMessage);
			Log.v("httpshutter_ServerProcess", "ServerProcess end.");
		}

	}

}
