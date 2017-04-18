package com.main;

import java.awt.EventQueue;

import com.gui.LoadJFrame;
import com.gui.MainJFrame;
import com.server.TcpServer;

public class Monitor {
	private static LoadJFrame loadJFrame;
	private static MainJFrame mainJFrame;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					loadJFrame = new LoadJFrame();
					loadJFrame.getJFrame().setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		TcpServer tcpServer = new TcpServer(8086);
		tcpServer.waitLink();
	}
	
	public static void loadMainJFrame(){
		loadJFrame.getJFrame().dispose();
		mainJFrame = MainJFrame.getUIInstance();
		mainJFrame.setJrameVisible();
	}
}
