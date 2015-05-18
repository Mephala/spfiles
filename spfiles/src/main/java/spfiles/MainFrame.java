package spfiles;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

import org.apache.commons.io.FileUtils;

import service.provider.client.executor.ServiceClient;
import service.provider.common.core.RequestApplication;
import service.provider.common.dto.SPFileDto;
import service.provider.common.request.RequestDtoFactory;
import service.provider.common.request.SPFileRequestDto;
import service.provider.common.response.SPFileResponseDto;

public class MainFrame extends JFrame {

	private JPanel contentPane;
	private JFileChooser fileChooser;
	private static JFrame mainFrame;
	private File chosenFile;
	private final String defaultChosenFileString = "Upload edecek dosya sec";
	private String chosenFileLabelString = defaultChosenFileString;
	private List<String> allFiles;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e1) {
			System.out.println("PisError.");
		} catch (InstantiationException e1) {
			System.out.println("PisError.");
		} catch (IllegalAccessException e1) {
			System.out.println("PisError.");
		} catch (UnsupportedLookAndFeelException e1) {
			System.out.println("PisError.");
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					mainFrame = new MainFrame();
					mainFrame.setVisible(true);
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
		ServiceClient.initialize("http://localhost:8080/");
		setTitle("SPFManager - Gökhanabi");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 451, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		allFiles = getAllFileList();
		final JComboBox<String> comboBox = new JComboBox<String>();
		for (String fileName : allFiles) {
			comboBox.addItem(fileName);
		}
		comboBox.setBounds(10, 21, 423, 22);
		contentPane.add(comboBox);

		JButton btnNewButton = new JButton("Masaustune DL et");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String selectedItem = (String) comboBox.getSelectedItem();
				SPFileRequestDto dlRequest = RequestDtoFactory.createSPFileRequest(RequestApplication.WEB);
				SPFileDto dlFileDto = new SPFileDto();
				dlFileDto.setFileName(selectedItem);
				dlRequest.setSpFileDto(dlFileDto);
				SPFileResponseDto response = ServiceClient.getFileData(dlRequest);
				if (response != null && response.getSpFileDto() != null && response.getSpFileDto().getData() != null && response.getSpFileDto().getData().length > 0) {
					byte[] data = response.getSpFileDto().getData();
					String dlPath = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + selectedItem;
					File dlFile = new File(dlPath);
					try {
						FileUtils.writeByteArrayToFile(dlFile, data);
						JOptionPane.showMessageDialog(null, "Masaustunde secilen dosya olusturuldu!", "Yaptim!", JOptionPane.INFORMATION_MESSAGE);
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(null, "DL OLMADI! Detay:" + e1.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
					}
				} else {
					JOptionPane.showMessageDialog(null, "DL OLMADI!", "Hata", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		btnNewButton.setBounds(10, 64, 147, 23);
		contentPane.add(btnNewButton);

		JLabel lblNewLabel = new JLabel("Upload Edilecek Dosya");
		lblNewLabel.setBounds(10, 98, 132, 14);
		contentPane.add(lblNewLabel);

		final JLabel chosenFileLabel = new JLabel(chosenFileLabelString);
		chosenFileLabel.setBounds(152, 98, 281, 14);
		contentPane.add(chosenFileLabel);

		JButton btnNewButton_1 = new JButton("Upload Dosya Sec");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int fileChooseStatus = fileChooser.showOpenDialog(mainFrame);
				if (fileChooseStatus == JFileChooser.APPROVE_OPTION) {
					chosenFile = fileChooser.getSelectedFile();
					chosenFileLabelString = chosenFile.getAbsolutePath();
					chosenFileLabel.setText(chosenFileLabelString);
				}
			}
		});
		btnNewButton_1.setBounds(10, 123, 147, 23);
		contentPane.add(btnNewButton_1);

		JButton btnNewButton_2 = new JButton("Refresh");
		btnNewButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshFileList(comboBox);
			}
		});
		btnNewButton_2.setBounds(192, 64, 91, 23);
		contentPane.add(btnNewButton_2);

		JButton btnNewButton_3 = new JButton("Resetle");
		btnNewButton_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chosenFileLabel.setText(defaultChosenFileString);
				chosenFile = null;
			}
		});
		btnNewButton_3.setBounds(324, 123, 91, 23);
		contentPane.add(btnNewButton_3);

		JButton btnNewButton_4 = new JButton("Upload Et");
		btnNewButton_4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (chosenFile != null) {
					try {
						String fileName = chosenFile.getName();
						byte[] chosenFileData = FileUtils.readFileToByteArray(chosenFile);
						for (String prevFile : allFiles) {
							if (prevFile.equals(fileName)) {
								JOptionPane.showMessageDialog(null, "Seçtiğiniz dosya isminde başka bir dosya kayıtlı. İsim değişikliği şart.", "Olmadi", JOptionPane.ERROR_MESSAGE);
								return;
							}
						}
						SPFileRequestDto request = RequestDtoFactory.createSPFileRequest(RequestApplication.WEB);
						SPFileDto fileDto = new SPFileDto();
						request.setSpFileDto(fileDto);
						fileDto.setFileName(fileName);
						fileDto.setData(chosenFileData);
						SPFileResponseDto response = ServiceClient.getFileData(request);
						if (response != null) {
							if (response.getError() != null) {
								JOptionPane.showMessageDialog(null, "Hatalarr!! Hata Detayi:" + response.getError(), "HATA!!", JOptionPane.ERROR_MESSAGE);
							} else {
								JOptionPane.showMessageDialog(null, "Islem tamam. Yeni eklenen dosyayi listelenenlar arasinda gorebilirsiniz.", "Tamamdir", JOptionPane.INFORMATION_MESSAGE);
								chosenFile = null;
								chosenFileLabel.setText(defaultChosenFileString);
								refreshFileList(comboBox);
							}
						}
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(null, "Upload Hatasi! Detay:" + e1.getMessage(), "UploadHatasi!", JOptionPane.ERROR_MESSAGE);
					}
				} else {
					JOptionPane.showMessageDialog(null, "Yukleme yapmadan once bir dosya secmelisin!", "Hatalisin", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		btnNewButton_4.setBounds(192, 123, 91, 23);
		contentPane.add(btnNewButton_4);

	}

	private List<String> getAllFileList() {
		SPFileRequestDto spFileRequest = RequestDtoFactory.createSPFileRequest(RequestApplication.WEB);
		SPFileDto fileDto = new SPFileDto();
		spFileRequest.setSpFileDto(fileDto);
		SPFileResponseDto response = ServiceClient.getFileData(spFileRequest);
		List<String> files = response.getSpFileDto().getAllFileNames();
		Collections.sort(files);
		return files;
	}

	private void refreshFileList(final JComboBox<String> comboBox) {
		allFiles = getAllFileList();
		comboBox.removeAllItems();
		for (String file : allFiles) {
			comboBox.addItem(file);
		}
		JOptionPane.showMessageDialog(null, "Liste yenilendi", "Liste yenilendi", JOptionPane.INFORMATION_MESSAGE);
	}
}
