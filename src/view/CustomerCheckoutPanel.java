package view;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;

public class CustomerCheckoutPanel extends JFrame {

	private JPanel contentPane;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					CustomerCheckoutPanel frame = new CustomerCheckoutPanel();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public CustomerCheckoutPanel() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1200, 750);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JPanel mainPanel = new JPanel();
		mainPanel.setBackground(new Color(30, 144, 255));
		mainPanel.setBounds(0, 0, 1200, 750);
		contentPane.add(mainPanel);
		mainPanel.setLayout(null);

		JLabel grocerySystemTitle = new JLabel("Kostco Market");
		grocerySystemTitle.setForeground(new Color(255, 255, 255));
		grocerySystemTitle.setFont(new Font("Lucida Grande", Font.BOLD, 25));
		grocerySystemTitle.setBounds(842, 96, 211, 70);
		mainPanel.add(grocerySystemTitle);

		JButton selectItem = new JButton("Select Item");
		selectItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
				LoginScreen login = new LoginScreen();
				login.setVisible(true);
				validate();
				revalidate();

			}
		});
		selectItem.setBounds(803, 390, 298, 43);
		mainPanel.add(selectItem);

		JPanel dateTimePanel = new JPanel();
		dateTimePanel.setBackground(new Color(128, 128, 128));
		dateTimePanel.setForeground(new Color(128, 128, 128));
		dateTimePanel.setBounds(0, 0, 1200, 43);
		mainPanel.add(dateTimePanel);
		dateTimePanel.setLayout(null);

		JLabel welcomeLbl = new JLabel("Welcome [getID]");
		welcomeLbl.setFont(new Font("Lucida Grande", Font.BOLD, 20));
		welcomeLbl.setBounds(18, 7, 234, 31);
		dateTimePanel.add(welcomeLbl);
//Need to fix so the time changes value overtime. Integrate ClockPanel if you know how
		String date = new SimpleDateFormat("[dd/MM/yyyy] [hh:mm:ss]").format(new Date());
        JLabel labelTime = new JLabel(date);
        labelTime.setFont(new Font("Lucida Grande", Font.BOLD, 16));
        labelTime.setBounds(952, 7, 242, 35);
 		dateTimePanel.add(labelTime);


		JPanel assisstancePanel = new JPanel();
		assisstancePanel.setForeground(Color.GRAY);
		assisstancePanel.setBackground(Color.GRAY);
		assisstancePanel.setBounds(0, 678, 1200, 50);
		mainPanel.add(assisstancePanel);
		assisstancePanel.setLayout(null);

		JButton requireAssistanceBtn = new JButton("I NEED ASSISTANCE");
//		requireAssistanceBtn.setBackground(new Color(255, 0, 0));
		requireAssistanceBtn.setBackground(new Color(128, 128, 128));
//		requireAssistanceBtn.setForeground(new Color(255, 0, 0));
		requireAssistanceBtn.setOpaque(true);
		requireAssistanceBtn.setBounds(926, 6, 219, 38);
		assisstancePanel.add(requireAssistanceBtn);

		JButton cancelOrderBtn = new JButton("Cancel Order");
		cancelOrderBtn.setOpaque(true);
		cancelOrderBtn.setBackground(Color.GRAY);
		cancelOrderBtn.setBounds(695, 6, 187, 38);
		assisstancePanel.add(cancelOrderBtn);

		JPanel imagePanel = new JPanel();
		imagePanel.setBounds(66, 83, 471, 158);
		mainPanel.add(imagePanel);

		JButton enterItemBtn = new JButton("Enter Item");
		enterItemBtn.setBounds(803, 460, 298, 43);
		mainPanel.add(enterItemBtn);

		JButton btnFinishAndPay = new JButton("Finish and Pay");
		btnFinishAndPay.setBounds(803, 539, 298, 43);
		mainPanel.add(btnFinishAndPay);

		JPanel panel = new JPanel();
		panel.setBounds(66, 280, 471, 357);
		mainPanel.add(panel);
		panel.setLayout(null);

		JLabel totalLabel = new JLabel("Total");
		totalLabel.setBounds(6, 335, 61, 16);
		panel.add(totalLabel);

				JSeparator separator = new JSeparator();
				separator.setBounds(0, 311, 471, 12);
				panel.add(separator);
				separator.setBackground(Color.BLACK);
				separator.setForeground(Color.BLACK);
	}
}