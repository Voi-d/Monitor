package com.gui;

import javax.swing.JFrame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import com.database.DBHandle;
import com.main.Coordinate;
import com.main.GlobalValues;
import com.main.Station;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import java.awt.FlowLayout;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Vector;

import javax.swing.JButton;

import sun.swing.table.DefaultTableCellHeaderRenderer;

public class MainJFrame implements ActionListener {

	private JFrame frame;
	private JTree tree;
	private DefaultMutableTreeNode node1, node2, node3, node4, root;
	private DefaultTreeModel treeModel;
	private DefaultTableModel realTimeTableModel, historyTableModel;
	private JTable realTimeTable, historyTable;
	private JSplitPane splitPane;
	private JPanel realTimeDataPane, historyDataPanel, historyPicturePanel,
			settingsPanel;
	private JScrollPane treeScrollPane, realTimeDataScrollPane,
			historyDataScrollPane, historyPictureJScrollPane;
	private JLabel realTimePictureLable;
	private JLabel[] jLabels;
	private JSpinner[] jSpinners;
	private ArrayList<String> lists = new ArrayList<String>(); // 异常站点名

	private String staName = "station0";
	private int staNum = 10; // 站点数量
	private Timer timer;

	private String[][] defaultData = { { "1", "0", "0", "0", "0", "0", "0" },
			{ "2", "0", "0", "0", "0", "0", "0" },
			{ "3", "0", "0", "0", "0", "0", "0" } };
	private String[] defaultTitle = { "组号", "温度1", "温度2", "温度3", "温度4", "油液粘度",
			"湿度" };

	ArrayList<Coordinate> coordinateSets = new ArrayList<Coordinate>();

	/**
	 * Create the application.
	 */
	private MainJFrame() {
		initialize();
		timer = new Timer(200, this);
	}

	public void actionPerformed(ActionEvent e) { // 定时刷新界面
		if (!GlobalValues.map.isEmpty()) {
			Station station;
			if (GlobalValues.map.containsKey(staName)) {
				station = GlobalValues.map.get(staName);
			} else { // 选中站点退出时，自动切换到第一个连接的站点
				station = GlobalValues.map.entrySet().iterator().next()
						.getValue();
			}
			if (station.isHaveData()) {
				realTimeTableModel.setDataVector(station.getTableData(),
						station.getTableTitle());
				coordinateSets = station.getCoordinates(); // 获取需要警示的行列坐标集
			}
			if (station.isHaveImage()) {
				Image image = station.getImage();
				BufferedImage tag = new BufferedImage(320, 240,
						BufferedImage.TYPE_INT_RGB);
				tag.getGraphics().drawImage(image, 0, 0, 320, 240, null);
				realTimePictureLable.setIcon(new ImageIcon(tag));
			}
		} else {
			coordinateSets.clear(); // 所有站点断开连接，清楚行列坐标集
			realTimeTableModel.setDataVector(defaultData, defaultTitle); // 显示默认数据
		}

		lists.clear();
		for (Entry<String, Station> entry : GlobalValues.map.entrySet()) {
			Station station = entry.getValue();
			if (station.isAlert()) {
				lists.add(station.getStationName());
			}
		}
		// tree.expandPath(new TreePath(node1.getPath()));
		tree.repaint();
	}

	private static volatile MainJFrame UIInstance;

	public static MainJFrame getUIInstance() {
		if (UIInstance == null) {
			synchronized (MainJFrame.class) {
				if (UIInstance == null) {
					UIInstance = new MainJFrame();
				}
			}
		}
		return UIInstance;
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setResizable(false);
		frame.setBounds(100, 100, 825, 600);
		frame.setLocation(350, 50);
		frame.setTitle("远程风电监测系统V1.0");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new GridLayout(1, 1, 0, 0));

		splitPane = new JSplitPane();
		frame.getContentPane().add(splitPane);

		treeScrollPane = new JScrollPane();
		splitPane.setLeftComponent(treeScrollPane);

		root = new DefaultMutableTreeNode("监测终端");
		node1 = new DefaultMutableTreeNode("实时数据");
		node2 = new DefaultMutableTreeNode("历史数据");
		node3 = new DefaultMutableTreeNode("历史图片");
		node4 = new DefaultMutableTreeNode("设置");
		root.add(node1);
		root.add(node2);
		root.add(node3);
		root.add(node4);

		for (int i = 0; i < staNum; i++) {
			DefaultMutableTreeNode dataNode = new DefaultMutableTreeNode(
					"station" + i);
			DefaultMutableTreeNode pictureNode = new DefaultMutableTreeNode(
					"station" + i);
			node2.add(dataNode);
			node3.add(pictureNode);
		}

		tree = new JTree(root);
		tree.setCellRenderer(new MyTreeCellRenderer(tree.getCellRenderer()));
		tree.setBorder(new EmptyBorder(5, 5, 0, 0));
		tree.setEditable(false);
		treeScrollPane.setViewportView(tree);

		tree.addTreeSelectionListener(new TreeSelectionListener() {

			public void valueChanged(TreeSelectionEvent e) {
				// TODO Auto-generated method stub
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree
						.getLastSelectedPathComponent();
				if (node == null) {
					return;
				}
				String nodeName = node.toString();
				if (!nodeName.equals(root.toString())) {
					String string = nodeName;
					if (nodeName.startsWith("station")) {
						staName = nodeName;
						string = node.getParent().toString();
					}
					// timer.stop();
					if (string.contains("实时数据")) {
						splitPane.setRightComponent(realTimeDataPane);
						timer.start();
					} else if (string.contains("历史数据")) {
						splitPane.setRightComponent(historyDataPanel);
						historyTableModel.setDataVector(
								getHistoryData(null, null), getHistoryTitle());
						TableColumnModel tcm = historyTable.getColumnModel();
						TableColumn tc = tcm.getColumn(1);
						tc.setPreferredWidth(140);
					} else if (string.contains("历史图片")) {
						splitPane.setRightComponent(historyPicturePanel);
						File file = new File("pictures/" + staName);
						if (file.exists()) {
							File fileList[] = file.listFiles();
							for (int i = fileList.length - 1, j = 0; (i >= 0 && j < jLabels.length); i--, j++) {
								if (fileList[i].isFile()
										&& (fileList[i].getName().endsWith(
												".png") || fileList[i]
												.getName().endsWith(".jpg"))) {
									jLabels[j].setIcon(new ImageIcon(
											fileList[i].getPath()));
								}
							}
						}
					} else if (string.contains("设置")) {
						splitPane.setRightComponent(settingsPanel);
					}
				}
			}
		});

		// 实时数据面板
		{
			realTimeTableModel = new DefaultTableModel();
			DefaultTableCellRenderer tcr = new DefaultTableCellRenderer() {
				private static final long serialVersionUID = 3398804151586656997L;

				public Component getTableCellRendererComponent(JTable table,
						Object value, boolean isSelected, boolean hasFocus,
						int row, int column) {
					setBackground(Color.white);
					setForeground(Color.black);
					for (int i = 0; i < coordinateSets.size(); i++) {
						if ((row == coordinateSets.get(i).getX())
								&& (column == coordinateSets.get(i).getY())) {
							setForeground(Color.red);
						}
					}
					return super.getTableCellRendererComponent(table, value,
							isSelected, hasFocus, row, column);
				}
			};
			tcr.setHorizontalAlignment(SwingConstants.CENTER);
			realTimeTableModel.setDataVector(defaultData, defaultTitle);
			realTimeTable = new JTable(realTimeTableModel);
			realTimeTable.setDefaultRenderer(Object.class, tcr);
			realTimeTable.setEnabled(false); // 表格不可编辑
			realTimeTable.setRowHeight(50);
			realTimeTable.setIntercellSpacing(new Dimension(0, 0));
			JTableHeader header = realTimeTable.getTableHeader();
			header.setFont(new Font("STFangsong", Font.PLAIN, 20));

			realTimeDataScrollPane = new JScrollPane(realTimeTable);
			realTimeDataScrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));

			realTimeDataPane = new JPanel();
			realTimeDataPane.setLayout(new BorderLayout(5, 10));
			realTimeDataPane.setBorder(new EmptyBorder(15, 20, 20, 20));
			realTimeDataPane.add(realTimeDataScrollPane, BorderLayout.CENTER);

			JLabel jLabel_0 = new JLabel("实时数据");
			jLabel_0.setHorizontalAlignment(JLabel.CENTER);
			jLabel_0.setFont(new Font("STZhongsong", Font.PLAIN, 26));
			realTimeDataPane.add(jLabel_0, BorderLayout.NORTH);

			JPanel panel_1 = new JPanel();
			panel_1.setLayout(new BorderLayout(0, 10));
			realTimeDataPane.add(panel_1, BorderLayout.SOUTH);

			realTimePictureLable = new JLabel();
			realTimePictureLable.setIcon(new ImageIcon("pictures/Photo.jpg"));
			realTimePictureLable.setHorizontalAlignment(JLabel.CENTER);
			panel_1.add(realTimePictureLable, BorderLayout.CENTER);

			JPanel panel_2 = new JPanel();
			panel_2.setLayout(new FlowLayout());
			panel_1.add(panel_2, BorderLayout.SOUTH);

			JButton jButton_0 = new JButton("手动采集");
			panel_2.add(jButton_0);
		}

		// 历史数据面板
		{
			historyTableModel = new DefaultTableModel();
			DefaultTableCellRenderer tcr = new DefaultTableCellHeaderRenderer();
			tcr.setHorizontalAlignment(SwingConstants.CENTER);
			
			historyTable = new JTable(historyTableModel);
			historyTable.setDefaultRenderer(Object.class, tcr);
			historyTable.setIntercellSpacing(new Dimension(0, 0));

			historyDataScrollPane = new JScrollPane(historyTable);

			historyDataPanel = new JPanel();
			historyDataPanel.setLayout(new BorderLayout(5, 10));
			historyDataPanel.setBorder(new EmptyBorder(15, 10, 10, 10));
			historyDataPanel.add(historyDataScrollPane, BorderLayout.CENTER);

			JLabel jLabel_0 = new JLabel("历史数据");
			jLabel_0.setFont(new Font("STZhongsong", Font.PLAIN, 26));
			jLabel_0.setHorizontalAlignment(JLabel.CENTER);
			historyDataPanel.add(jLabel_0, BorderLayout.NORTH);

			JPanel panel_0 = new JPanel();
			panel_0.setLayout(new GridLayout(2, 1));
			historyDataPanel.add(panel_0, BorderLayout.SOUTH);

			JPanel panel_1 = new JPanel();
			panel_1.setLayout(new FlowLayout());
			panel_0.add(panel_1);

			jSpinners = new JSpinner[6];
			JLabel jLabel_1 = new JLabel("起始时间:");
			jLabel_1.setHorizontalAlignment(JLabel.CENTER);
			jLabel_1.setFont(new Font("STKaiti", Font.PLAIN, 14));
			panel_1.add(jLabel_1);

			jSpinners[0] = new JSpinner();
			jSpinners[0].setModel(new SpinnerNumberModel(2017, 2017, 2017, 1));
			panel_1.add(jSpinners[0]);
			jSpinners[1] = new JSpinner();
			jSpinners[1].setModel(new SpinnerNumberModel(03, 1, 12, 1));
			panel_1.add(jSpinners[1]);
			jSpinners[2] = new JSpinner();
			jSpinners[2].setModel(new SpinnerNumberModel(15, 1, 31, 1));
			panel_1.add(jSpinners[2]);

			JLabel jLabel_3 = new JLabel("          ");
			panel_1.add(jLabel_3);

			JLabel jLabel_2 = new JLabel("终止时间:");
			jLabel_2.setHorizontalAlignment(JLabel.CENTER);
			jLabel_2.setFont(new Font("STKaiti", Font.PLAIN, 14));
			panel_1.add(jLabel_2);

			jSpinners[3] = new JSpinner();
			jSpinners[3].setModel(new SpinnerNumberModel(2017, 2017, 2017, 1));
			panel_1.add(jSpinners[3]);
			jSpinners[4] = new JSpinner();
			jSpinners[4].setModel(new SpinnerNumberModel(03, 1, 12, 1));
			panel_1.add(jSpinners[4]);
			jSpinners[5] = new JSpinner();
			jSpinners[5].setModel(new SpinnerNumberModel(15, 1, 31, 1));
			panel_1.add(jSpinners[5]);

			JLabel jLabel_4 = new JLabel("          ");
			panel_1.add(jLabel_4);

			JButton jButton_1 = new JButton("Search");
			jButton_1.setFont(new Font("Tahoma", Font.PLAIN, 10));
			jButton_1.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent arg0) {
					// TODO Auto-generated method stub
					String startTime = String.format("%04d%02d%02d000000",
							jSpinners[0].getValue(), jSpinners[1].getValue(),
							jSpinners[2].getValue());
					String endTime = String.format("%04d%02d%02d240000",
							jSpinners[3].getValue(), jSpinners[4].getValue(),
							jSpinners[5].getValue());
					historyTableModel.setDataVector(
							getHistoryData(startTime, endTime),
							getHistoryTitle());
					TableColumnModel tcm = historyTable.getColumnModel();
					TableColumn tc = tcm.getColumn(1);
					tc.setPreferredWidth(140);
				}
			});
			panel_1.add(jButton_1);

			JPanel panel_2 = new JPanel();
			panel_2.setLayout(new FlowLayout());
			panel_0.add(panel_2);

			JButton jButton_0 = new JButton("打开数据库");
			jButton_0.setFont(new Font("STKaiti", Font.PLAIN, 16));
			jButton_0.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent arg0) {
					try {
						Runtime.getRuntime()
								.exec("C:/Program Files (x86)/MySQL-Front/MySQL-Front.exe");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			panel_2.add(jButton_0);
		}

		// 历史图片面板
		{
			historyPicturePanel = new JPanel();
			historyPicturePanel.setLayout(new BorderLayout(5, 10));
			historyPicturePanel.setBorder(new EmptyBorder(15, 10, 10, 10));

			JPanel picturePanel = new JPanel();
			picturePanel.setLayout(new FlowLayout());
			picturePanel.setPreferredSize(new Dimension((320 + 5) * 2,
					(240 + 5) * 4));

			jLabels = new JLabel[8];
			for (int i = 0; i < jLabels.length; i++) {
				jLabels[i] = new JLabel();
				jLabels[i].setIcon(new ImageIcon("pictures/Photo.jpg"));
				picturePanel.add(jLabels[i]);
			}

			historyPictureJScrollPane = new JScrollPane(picturePanel);
			historyPicturePanel.add(historyPictureJScrollPane,
					BorderLayout.CENTER);

			JLabel jLabel_0 = new JLabel("历史图片");
			jLabel_0.setFont(new Font("STZhongsong", Font.PLAIN, 26));
			jLabel_0.setHorizontalAlignment(JLabel.CENTER);
			historyPicturePanel.add(jLabel_0, BorderLayout.NORTH);

			JPanel panel_0 = new JPanel();
			panel_0.setLayout(new FlowLayout());
			historyPicturePanel.add(panel_0, BorderLayout.SOUTH);

			JButton jButton_0 = new JButton("打开文件");
			jButton_0.setFont(new Font("STKaiti", Font.PLAIN, 16));
			jButton_0.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent arg0) {
					try {
						Runtime.getRuntime()
								.exec("explorer.exe /e /select, "
										+ "D:\\MyCode\\Java\\Monitor\\pictures");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			panel_0.add(jButton_0);
		}
		splitPane.setRightComponent(realTimeDataPane);

		// 设置面板
		{
			settingsPanel = new JPanel();
			settingsPanel.setBorder(new EmptyBorder(30, 0, 50, 0));
			settingsPanel.setLayout(new BorderLayout());
			// splitPane.setRightComponent(settingsPanel);

			JPanel panel_0 = new JPanel();
			panel_0.setLayout(new FlowLayout());
			settingsPanel.add(panel_0, BorderLayout.CENTER);

			JPanel panel_3 = new JPanel();
			settingsPanel.add(panel_3, BorderLayout.NORTH);

			JLabel jLabel_4 = new JLabel("设置");
			jLabel_4.setFont(new Font("STZhongsong", Font.PLAIN, 26));
			panel_3.add(jLabel_4);

			JPanel panel_1 = new JPanel();
			panel_1.setBorder(new EmptyBorder(10, 10, 10, 10));
			panel_1.setLayout(new GridLayout(4, 5, 25, 65));
			panel_0.add(panel_1);

			JLabel jTable_0 = new JLabel("<html>数据采集<br>间隔时间</html>");
			jTable_0.setFont(new Font("STKaiti", Font.BOLD, 15));
			panel_1.add(jTable_0);

			JRadioButton rButton_5s = new JRadioButton("5s");
			panel_1.add(rButton_5s);
			JRadioButton rButton_10s = new JRadioButton("10s");
			panel_1.add(rButton_10s);
			JRadioButton rButton_15s = new JRadioButton("15s");
			panel_1.add(rButton_15s);
			JRadioButton rButton_20s = new JRadioButton("20s");
			panel_1.add(rButton_20s);

			JLabel jTable_1 = new JLabel("<html>图片采集<br>间隔时间</html>");
			jTable_1.setFont(new Font("STKaiti", Font.BOLD, 15));
			panel_1.add(jTable_1);

			JRadioButton rButton_30min = new JRadioButton("30min");
			panel_1.add(rButton_30min);
			JRadioButton rButton_40min = new JRadioButton("40min");
			panel_1.add(rButton_40min);
			JRadioButton rButton_50min = new JRadioButton("50min");
			panel_1.add(rButton_50min);
			JRadioButton rButton_60min = new JRadioButton("60min");
			panel_1.add(rButton_60min);

			JLabel jTable_2 = new JLabel("<html>xxxx警<br>戒油温</html>");
			jTable_2.setFont(new Font("STKaiti", Font.BOLD, 15));
			panel_1.add(jTable_2);

			JRadioButton rButton_30C0 = new JRadioButton("30°C");
			panel_1.add(rButton_30C0);
			JRadioButton rButton_40C0 = new JRadioButton("40°C");
			panel_1.add(rButton_40C0);
			JRadioButton rButton_50C0 = new JRadioButton("50°C");
			panel_1.add(rButton_50C0);
			JRadioButton rButton_60C0 = new JRadioButton("60°C");
			panel_1.add(rButton_60C0);

			JLabel jTable_3 = new JLabel("<html>xxxx警<br>戒油温</html>");
			jTable_3.setFont(new Font("STKaiti", Font.BOLD, 15));
			panel_1.add(jTable_3);

			JRadioButton rButton_30C1 = new JRadioButton("30°C");
			panel_1.add(rButton_30C1);
			JRadioButton rButton_40C1 = new JRadioButton("40°C");
			panel_1.add(rButton_40C1);
			JRadioButton rButton_50C1 = new JRadioButton("50°C");
			panel_1.add(rButton_50C1);
			JRadioButton rButton_60C1 = new JRadioButton("60°C");
			panel_1.add(rButton_60C1);

			JPanel panel_2 = new JPanel();
			settingsPanel.add(panel_2, BorderLayout.SOUTH);

			JButton button = new JButton("确    认");
			button.setFont(new Font("STKaiti", Font.PLAIN, 16));
			panel_2.add(button);
		}
	}

	public void setJrameVisible() {
		frame.setVisible(true);
	}

	public void insertNode(DefaultMutableTreeNode newNode1) {
		DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
		treeModel.insertNodeInto(newNode1, node1, node1.getChildCount());
		tree.scrollPathToVisible(new TreePath(newNode1.getPath()));
	}

	public void removeNode(DefaultMutableTreeNode newNode) {
		treeModel = (DefaultTreeModel) tree.getModel();
		treeModel.removeNodeFromParent(newNode);
		// treeModel.reload();
	}

	public Vector<Vector<Object>> getHistoryData(String startTime,
			String endTime) {
		DBHandle dbInstance = DBHandle.getDBInstance();
		ResultSet resSet;
		if (startTime == null && endTime == null) {
			resSet = dbInstance.selectDB(staName);
		} else {
			resSet = dbInstance.selectDB(staName, startTime, endTime);
		}

		Vector<Vector<Object>> historyDataVector = new Vector<Vector<Object>>();

		if (resSet == null) {
			return historyDataVector;
		}

		try {
			while (resSet.next()) {
				Vector<Object> vobj = new Vector<Object>();
				for (int i = 1; i <= resSet.getMetaData().getColumnCount(); i++) {
					vobj.add(resSet.getObject(i));
				}
				historyDataVector.add(vobj);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return historyDataVector;
		}
		return historyDataVector;
	}

	public Vector<String> getHistoryTitle() {
		Vector<String> historyTitle = new Vector<String>();

		historyTitle.add("站点" + staName.charAt(staName.length() - 1));
		historyTitle.add("时间");
		historyTitle.add("温度1");
		historyTitle.add("温度2");
		historyTitle.add("温度3");
		historyTitle.add("温度4");
		historyTitle.add("油液粘度");
		historyTitle.add("湿度");

		return historyTitle;
	}

	class MyTreeCellRenderer extends DefaultTreeCellRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final TreeCellRenderer renderer;

		public MyTreeCellRenderer(TreeCellRenderer renderer) {
			this.renderer = renderer;
		}

		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean isSelected, boolean expanded, boolean leaf, int row,
				boolean hasFocus) {
			JComponent c = (JComponent) renderer.getTreeCellRendererComponent(
					tree, value, isSelected, expanded, leaf, row, hasFocus);

			c.setOpaque(true);
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
			if (node.isLeaf() && node.getParent().toString().equals("实时数据")
					&& lists.contains(value.toString())) {
				c.setForeground(Color.RED);
				// c.setForeground(getTextNonSelectionColor());
				c.setBackground(getBackgroundNonSelectionColor());
			} else {
				c.setForeground(getTextNonSelectionColor());
				c.setBackground(getBackgroundNonSelectionColor());
			}

			if (isSelected) {
				c.setOpaque(false);
			}

			return c;
		}
	}
}
