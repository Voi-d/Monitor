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
			System.out.println("���ڼ����˿ڣ�" + port);
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
				System.err.println(socket.getInetAddress() + "���ӳɹ�...");
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

		public void run() {
			// TODO Auto-generated method stub
			String recv;
			String[] res;
			byte[] buf = new byte[128];
			int len = 0;
			try {
				if ((len = dis.read(buf)) > 0) {// �ȴ����տͻ��˷��͹���������
					recv = new String(buf);
					res = recv.split(":");
					if (verify(res[1], res[2])
							&& !GlobalValues.map.containsKey("station"
									+ Integer.parseInt(res[0]))) { // �����֤����δ�ɵ�¼
						station = new Station(Integer.parseInt(res[0]));
						GlobalValues.map.put(station.getStationName(), station);
						station.initStation();
						while ((len = dis.read(buf)) > 0) { // �յ�����
							recv = new String(buf);
							res = recv.split(":");
							if (res[1].equals("picture")) { // �յ�ͼƬ����
								byte[] pictureBuf = new byte[153600];
								int cursor = 0;
								while ((len = dis.read(pictureBuf, cursor, 640)) > 0) {
									cursor = cursor + len;
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
				e1.printStackTrace();
			}

			try {

				dis.close();
				socket.close();
				System.out.println("�Ͽ�����");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
