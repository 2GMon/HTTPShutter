package jp.ddo.t2gmon.httpshutter.http;

import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import jp.ddo.t2gmon.httpshutter.CameraView;

import android.content.Context;
import android.util.Log;

public class HttpServer extends Thread {
	private Context context;
	private ServerSocket serverSocket = null;
	private CameraView cameraView = null;

	public HttpServer(Context context, CameraView cameraView) {
		this.context = context;
		this.cameraView = cameraView;
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
		
		// リクエストされたPATHを返す
		private String getReceivedPath(InputStream inputStream) {
			byte buffer[] = new byte[bufferSize];
			StringBuffer recvMessage = new StringBuffer();
			StringBuffer path = new StringBuffer();
			
			// headerの最後はCR/LF/CR/CFのはずなので，一文字ずつ見ていく
			int i = 0;
			while (true) {
				try {
					int c = inputStream.read();
					if (c < 0) {
						throw new Exception();
					}
					buffer[i] = (byte)c;
					if (i > 3 &&buffer[i - 3] == '\r' && buffer[i - 2] == '\n' && buffer[i - 1] == '\r' && buffer[i] == '\n') {
						recvMessage.append(new String(buffer, "UTF-8"));
						break;
					}
					else if (i == bufferSize - 1) {
						throw new Exception();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				i++;
			}

			// recvMessageからPATHのみ取り出す
			i = 0;
			while (true) {
				if (recvMessage.charAt(i) == '\r' && recvMessage.charAt(i + 1) == '\n') {
					break;
				}
				else {
					path.append(recvMessage.charAt(i));
				}
				i++;
			}
			
			return path.toString().split(" ")[1];
		}

		// ヘッダのみのレスポンスを返す
		private void responseHeader(int status, String message, PrintStream printStream) {
			printStream.println("HTTP/1.0 " + status + " " + message);
			printStream.println("");
			printStream.flush();
		}

		public ServerProcess(Socket socket) {
			this.socket = socket;
		}
	
		public void run() {
			InputStream inputStream = null;
			PrintStream printStream = null;

			Log.v("httpshutter_ServerProcess", "ServerProcess start.");
			try {
				inputStream = socket.getInputStream();
				
				String path = getReceivedPath(inputStream);
				if (path.equals("/photo.jpg")) {
					cameraView.httpShutter();
				}
				Log.v("httpshutther_server", "Received Path: " + path);

				printStream = new PrintStream(socket.getOutputStream());
				responseHeader(201,"OKOK", printStream);
                socket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			Log.v("httpshutter_ServerProcess", "ServerProcess end.");
		}

	}

}
