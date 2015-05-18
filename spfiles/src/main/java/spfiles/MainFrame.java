package spfiles;

import java.awt.EventQueue;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class MainFrame extends JFrame {

	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainFrame frame = new MainFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public MainFrame() {
		setTitle("SPFManager");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 451, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JComboBox comboBox = new JComboBox();
		comboBox.setBounds(10, 21, 423, 22);
		contentPane.add(comboBox);

		JButton btnNewButton = new JButton("Masaustune DL et");
		btnNewButton.setBounds(10, 64, 147, 23);
		contentPane.add(btnNewButton);

		JLabel lblNewLabel = new JLabel("Upload Edilecek Dosya");
		lblNewLabel.setBounds(10, 98, 132, 14);
		contentPane.add(lblNewLabel);

		JButton btnNewButton_1 = new JButton("Upload Dosya Sec");
		btnNewButton_1.setBounds(10, 123, 132, 23);
		contentPane.add(btnNewButton_1);

		JButton btnNewButton_2 = new JButton("Refresh");
		btnNewButton_2.setBounds(192, 64, 91, 23);
		contentPane.add(btnNewButton_2);

		JButton btnNewButton_3 = new JButton("Resetle");
		btnNewButton_3.setBounds(192, 123, 91, 23);
		contentPane.add(btnNewButton_3);
	}
}
