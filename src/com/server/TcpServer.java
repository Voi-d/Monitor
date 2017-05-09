package com.server;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.database.DBHandle;
import com.main.GlobalValues;
import com.main.Station;

public class TcpServer {

	private ServerSocket serverSocket;

	public TcpServer(int port) {
		// TODO Auto-generated constructor stub
		try {
			serverSocket = new ServerSocket(port);
			System.out.println("正在监听端口：" + port);
			DBHandle.getDBInstance();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void waitLink() {
		while (true) {
			try {
				Socket socket = serverSocket.accept();
				System.out.println(socket.getInetAddress() + ":"
						+ socket.getPort() + "连接成功...");
				new Thread(new ClientThread(socket)).start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private boolean verify(String user, String pwd) {
		if (user.equals("void") && pwd.equals("123456")) {
			return true;
		}
		return false;
	}

	class ClientThread implements Runnable {
		private DataInputStream dis;
		private Socket socket;
		private Station station;

		public ClientThread(Socket socket) {
			this.socket = socket;
			try {
				dis = new DataInputStream(socket.getInputStream());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		private void disconnect() {
			try {

				dis.close();
				socket.close();
				System.out.println("断开连接");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public void run() {
			// TODO Auto-generated method stub
			String recv;
			String[] res;
			byte[] buf = new byte[128];
			int len = 0;
			try {
				if ((len = dis.read(buf)) > 0) {// 等待接收客户端发送过来的数据
					recv = new String(buf);
					// System.out.println(recv);
					res = recv.split(":");
					if (verify(res[1], res[2])
							&& !GlobalValues.map.containsKey("station"
									+ Integer.parseInt(res[0]))) { // 身份验证并且未曾登录
						System.out.println("login successful");
						station = new Station(Integer.parseInt(res[0]));
						GlobalValues.map.put(station.getStationName(), station);
						station.initStation();
						while ((len = dis.read(buf)) > 0) { // 收到数据
							recv = new String(buf);
							// System.out.println(recv);
							res = recv.split(":");
							if (res[1].equals("picture")) { // 收到图片数据
								byte[] pictureBuf = new byte[153600];
								byte[] temp = new byte[6400];
								int cursor = 0;
								while ((len = dis.read(temp, 0, 6400)) > 0) {
									for (int i = 0; i < len; i++) {
										pictureBuf[cursor + i] = temp[i];
									}
									cursor = cursor + len;
									// System.out.println(cursor);
									if (cursor >= pictureBuf.length) {
										break;
									}
								}
								station.createImage(pictureBuf);
								continue;
							}
							station.updataData(res);
						}
						station.exitStation();
						GlobalValues.map.remove(station.getStationName());
					}
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				disconnect();
				e1.printStackTrace();
			}

			disconnect();
		}
	}
}
