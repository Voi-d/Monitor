package com.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JButton;
import javax.swing.SwingConstants;

import com.main.Monitor;

import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

public class LoadJFrame{
	private JFrame frame;
	private JPanel contentPane;
	private JPasswordField passwordField;
	private JTextField txtAmdin;
	
	public JFrame getJFrame(){
		return frame;
	}

	/**
	 * Create the frame.
	 */
	public LoadJFrame() {
		frame = new JFrame();
		frame.setResizable(false);
		frame.setTitle("Ô¶³Ì·çµç¼à¿ØÏµÍ³V1.0");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setBounds(100, 100, 496, 348);
		frame.setLocation(450, 150);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		frame.setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));

		JLabel lblNewLabel = new JLabel((String) null);
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setIcon(new ImageIcon("D:\\MyCode\\Java\\Monitor1.0\\images\\img1.png"));
		contentPane.add(lblNewLabel, BorderLayout.NORTH);

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(5, 20, 5, 5));
		contentPane.add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout(10, 10));

		JLabel lblNewLabel_1 = new JLabel("");
		lblNewLabel_1.setIcon(new ImageIcon("D:\\MyCode\\Java\\Monitor1.0\\images\\img0.png"));
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(lblNewLabel_1, BorderLayout.WEST);

		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new EmptyBorder(15, 0, 15, 60));
		panel.add(panel_2, BorderLayout.CENTER);
		panel_2.setLayout(new GridLayout(2, 2, 10, 20));

		JLabel lblNewLabel_2 = new JLabel("        ÓÃ»§Ãû£º");
		lblNewLabel_2.setFont(new Font("STKaiti", Font.PLAIN, 18));
		lblNewLabel_2.setHorizontalAlignment(SwingConstants.CENTER);
		panel_2.add(lblNewLabel_2);

		txtAmdin = new JTextField();
		txtAmdin.setText("admin");
		panel_2.add(txtAmdin);
		txtAmdin.setColumns(10);

		JLabel lblNewLabel_3 = new JLabel("        ÃÜ    Âë£º");
		lblNewLabel_3.setFont(new Font("STKaiti", Font.PLAIN, 18));
		lblNewLabel_3.setHorizontalAlignment(SwingConstants.CENTER);
		panel_2.add(lblNewLabel_3);

		passwordField = new JPasswordField();
		passwordField.setText("123456");
		panel_2.add(passwordField);

		JPanel panel_1 = new JPanel();
		contentPane.add(panel_1, BorderLayout.SOUTH);

		JButton btnNewButton = new JButton("µÇ  Â¼");
		btnNewButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (txtAmdin.getText().toString().trim().equals("admin")) {
					char[] password1 = passwordField.getPassword();
					char[] password2 = { '1', '2', '3', '4', '5', '6' };
					if (Arrays.equals(password1, password2)) {
						Monitor.loadMainJFrame();
					} else {
						JOptionPane.showMessageDialog(null, "ÃÜÂë´íÎó.", "",
								JOptionPane.WARNING_MESSAGE);
					}
				} else {
					JOptionPane.showMessageDialog(null, "ÕËºÅ´íÎó.", "",
							JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		btnNewButton.setFont(new Font("STKaiti", Font.PLAIN, 15));
		btnNewButton.setPreferredSize(new Dimension(120, 30));
		panel_1.add(btnNewButton);
	}
}
