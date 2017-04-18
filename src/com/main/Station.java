package com.main;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;

import com.database.DBHandle;
import com.gui.MainJFrame;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

public class Station {
	private int stationNum;
	private String stationName;
	private Vector<String> title;
	private Vector<Vector<Object>> data;

	private Image image;
	private boolean hasImage;
	private boolean hasData;

	private DBHandle dbInstance;
	private MainJFrame UIInstance;
	DefaultMutableTreeNode node;

	public Station(int stationNum) {
		this.stationNum = stationNum;
		stationName = "station" + stationNum;
		dbInstance = DBHandle.getDBInstance();
		UIInstance = MainJFrame.getUIInstance();
	}

	public void initStation() {
		title = new Vector<String>();
		data = new Vector<Vector<Object>>();
		node = new DefaultMutableTreeNode(stationName);

		hasImage = false;
		hasData = false;

		title.add("站点" + stationNum);
		title.add("温度1");
		title.add("温度2");
		title.add("温度3");
		title.add("温度4");
		title.add("油液粘度");
		title.add("湿度");

		dbInstance.createTable(stationName);

		for (int i = 0; i < 3; i++) {
			data.add(new Vector<Object>());
		}
		UIInstance.insertNode(node);
	}

	public String getStationName() {
		return stationName;
	}

	public Vector<String> getTableTitle() {
		return title;
	}

	public Vector<Vector<Object>> getTableData() {
		return data;
	}

	public void updataData(String[] string) {
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		for (int i = 0; i < 3; i++) {
			Vector<Object> vobj = new Vector<Object>();
			for (int j = 0; j < 7; j++) {
				vobj.add(string[i * 7 + j]);
			}
			data.setElementAt(vobj, i);
			dbInstance.insertDB(stationName,
					Integer.parseInt((String) vobj.get(0)),
					df.format(new Date()),
					Integer.parseInt((String) vobj.get(1)),
					Integer.parseInt((String) vobj.get(2)),
					Integer.parseInt((String) vobj.get(3)),
					Integer.parseInt((String) vobj.get(4)),
					Integer.parseInt((String) vobj.get(5)),
					Integer.parseInt((String) vobj.get(6)));
		}
		hasData = true;
	}

	public void createImage(byte[] buf) throws IOException {
		int[] ndata = new int[240 * 320];
		for (int i = 0; i < 240; i++) {
			for (int j = 0; j < 320; j++) {
				short read_data = 0;
				int ret = 0xff;
				read_data = (short) (read_data | ((short) (buf[(320 * i + j) * 2]) & 0x00ff));
				read_data = (short) (read_data << 8);
				read_data = (short) (read_data | ((short) (buf[(320 * i + j) * 2 + 1]) & 0x00ff));
				buf[2] = (byte) ((read_data >>> 11) << 3);
				buf[1] = (byte) (((read_data & 0x7ff) >>> 5) << 2);
				buf[0] = (byte) (((read_data & 0x1f) << 3));
				ret = (ret << 8) | ((int) buf[2] & 0xff);
				ret = (ret << 8) | ((int) buf[1] & 0xff);
				ret = (ret << 8) | ((int) buf[0] & 0xff);
				ndata[i * 320 + j] = ret;
			}
		}
		image = Toolkit.getDefaultToolkit().createImage(
				new MemoryImageSource(320, 240, ndata, 0, 320));
		hasImage = true;

		BufferedImage tag = new BufferedImage(320, 240,
				BufferedImage.TYPE_INT_RGB);
		tag.getGraphics().drawImage(image, 0, 0, 320, 240, null);
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		String dateString = df.format(new Date());
		String path = "pictures/" + stationName + '/';
		File fp = new File(path);
		if (!fp.exists()) {
			fp.mkdirs();
		}
		FileOutputStream out = new FileOutputStream(path + dateString + ".jpg");
		JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
		encoder.encode(tag);
		out.close();
	}

	public boolean isHaveImage() {
		return hasImage;
	}

	public boolean isHaveData() {
		return hasData;
	}

	public Image getImage() {
		return image;
	}

	public boolean isAlert() {
		return true;
	}

	public void exitStation() {
		UIInstance.removeNode(node);
	}
}
