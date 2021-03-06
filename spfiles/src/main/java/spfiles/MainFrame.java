package spfiles;

import org.apache.commons.io.FileUtils;
import service.provider.client.executor.ServiceClient;
import service.provider.common.core.RequestApplication;
import service.provider.common.core.ResponseStatus;
import service.provider.common.dto.SPFileDto;
import service.provider.common.request.RequestDtoFactory;
import service.provider.common.request.SPFileRequestDto;
import service.provider.common.response.SPFileResponseDto;
import service.provider.common.util.CommonUtils;
import service.provider.common.util.FileTransferStatusTracker;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MainFrame extends JFrame {

    private static JFrame mainFrame;
    private final String defaultChosenFileString = "Upload edecek dosya sec";
    private final String serverIp;
    private final String serverDlPath;
    private final boolean isTest = false;
    private final JCheckBox isAkbank;
    private JPanel contentPane;
    private JFileChooser fileChooser;
    private File chosenFile;
    private String chosenFileLabelString = defaultChosenFileString;
    private List<String> allFiles;
    private Map<String, String> fileNameToDLUUIDPath;

    /**
     * Create the frame.
     */
    public MainFrame() {
        if (isTest) {
            this.serverDlPath = "http://localhost:8080/";
            this.serverIp = "localhost";
        } else {
            this.serverDlPath = "http://sert-yapi.com/serviceProvider/";
            this.serverIp = "sert-yapi.com";
        }
        ServiceClient.initialize(serverDlPath);
        setTitle("SPFManager - Gökhanabi");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 525, 300);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);
        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        final JComboBox<String> comboBox = new JComboBox<String>();
        Thread comboBoxFetcher = new Thread(new Runnable() {

            public void run() {
                createComboBox(comboBox);

            }
        });
        comboBoxFetcher.start();
        JButton btnNewButton = new JButton("Masaustune DL et");
        btnNewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String selectedItem = (String) comboBox.getSelectedItem();
                String dlPath = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + selectedItem;
                File downloadFile = new File(dlPath);
                if (downloadFile.exists())
                    downloadFile.delete();
                try {
                    String dlUUID = fileNameToDLUUIDPath.get(selectedItem);
                    if (CommonUtils.isEmpty(dlUUID))
                        dlUUID = selectedItem;
                    FileUtils.copyURLToFile(new URL(serverDlPath + dlUUID + "/downloadFile.do"), downloadFile);
                    JOptionPane.showMessageDialog(null, "Download basarili", "Basari", JOptionPane.INFORMATION_MESSAGE);
                } catch (Throwable t) {
                    t.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Olmadi arkadas.", "Basarisizlik, Maglubiyet...", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        btnNewButton.setBounds(10, 64, 181, 23);
        contentPane.add(btnNewButton);

        JLabel lblNewLabel = new JLabel("Upload Edilecek Dosya");
        lblNewLabel.setBounds(10, 98, 169, 14);
        contentPane.add(lblNewLabel);

        final JLabel chosenFileLabel = new JLabel(chosenFileLabelString);
        chosenFileLabel.setBounds(191, 98, 314, 14);
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
        btnNewButton_1.setBounds(10, 123, 181, 23);
        contentPane.add(btnNewButton_1);

        JButton btnNewButton_2 = new JButton("Refresh");
        btnNewButton_2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                refreshFileList(comboBox);
            }
        });
        btnNewButton_2.setBounds(222, 64, 125, 23);
        contentPane.add(btnNewButton_2);

        JButton btnNewButton_3 = new JButton("Resetle");
        btnNewButton_3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                chosenFileLabel.setText(defaultChosenFileString);
                chosenFile = null;
            }
        });
        btnNewButton_3.setBounds(380, 124, 125, 23);
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
                                JOptionPane.showMessageDialog(null, "Seçtiğiniz dosya isminde başka bir dosya kayıtlı. İsim değişikliği şart.", "Olmadi",
                                        JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        }
                        boolean sendResult = false;
                        //akbankta http protokolu disina firewall uygulaniyor.
                        if (isAkbank.isSelected()) {
                            SPFileRequestDto fileRequestDto = RequestDtoFactory.createSPFileRequest(RequestApplication.PCA);
                            SPFileDto fileDto = new SPFileDto();
                            fileDto.setData(FileUtils.readFileToByteArray(chosenFile));
                            fileDto.setFileName(chosenFile.getName());
                            fileRequestDto.setSpFileDto(fileDto);
                            SPFileResponseDto response = ServiceClient.getFileData(fileRequestDto);
                            if (!(response == null || !ResponseStatus.OK.equals(response.getResponseStatus())))
                                sendResult = true;
                        } else {
                            sendResult = uploadViaLLSocket();
                        }
                        if (sendResult)
                            JOptionPane.showMessageDialog(null, "Upload ettim.", "Basarili", JOptionPane.INFORMATION_MESSAGE);
                        else
                            JOptionPane.showMessageDialog(null, "Upload edemedim, zivadim.", "Yakismadi.", JOptionPane.ERROR_MESSAGE);
                        refreshFileList(comboBox);
                    } catch (IOException e1) {
                        JOptionPane.showMessageDialog(null, "Upload Hatasi! Detay:" + e1.getMessage(), "UploadHatasi!", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Yukleme yapmadan once bir dosya secmelisin!", "Hatalisin", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        btnNewButton_4.setBounds(222, 123, 125, 23);
        contentPane.add(btnNewButton_4);
        isAkbank = new JCheckBox("akbank");
        isAkbank.setBounds(222, 153, 125, 25);
        isAkbank.setSelected(false);
        contentPane.add(isAkbank);

        JButton btnNewButton_5 = new JButton("Sil");
        btnNewButton_5.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int areYouSure = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this file?");
                if (areYouSure == 0) {
                    // TODO Deletion process.
                    String fileName = (String) comboBox.getSelectedItem();
                    SPFileRequestDto spFileRequest = RequestDtoFactory.createSPFileRequest(RequestApplication.WEB);
                    SPFileDto fileDto = new SPFileDto();
                    fileDto.setFileName(fileName);
                    spFileRequest.setDeleteFile(Boolean.TRUE);
                    spFileRequest.setSpFileDto(fileDto);
                    SPFileResponseDto response = ServiceClient.getFileData(spFileRequest);
                    if (response == null) {
                        JOptionPane.showMessageDialog(null, "Failed to communicate with server", "Technical Failure", JOptionPane.ERROR_MESSAGE);
                        return;
                    } else if (ResponseStatus.ERROR.equals(response.getResponseStatus())) {
                        JOptionPane.showMessageDialog(null, "Failed to delete file at server side....", "Technical Failure", JOptionPane.ERROR_MESSAGE);
                        return;
                    } else if (ResponseStatus.OK.equals(response.getResponseStatus())) {
                        JOptionPane.showMessageDialog(null, "Dosya basariyla silindi", "Success", JOptionPane.INFORMATION_MESSAGE);
                        refreshFileList(comboBox);
                    }
                }
            }
        });
        btnNewButton_5.setBounds(380, 63, 125, 25);
        contentPane.add(btnNewButton_5);

    }

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

    private boolean uploadViaLLSocket() {
        FileTransferStatusTracker ftst = new FileTransferStatusTracker();
        return ServiceClient.sendFileToSPS(chosenFile, 2005, serverIp, ftst);
    }

    private void createComboBox(final JComboBox<String> comboBox) {
        allFiles = getAllFileList();
        System.out.println("Files fetched.");
        comboBox.removeAllItems();
        for (String fileName : allFiles) {
            comboBox.addItem(fileName);
        }
        comboBox.repaint();
        comboBox.setBounds(10, 21, 423, 22);
        contentPane.add(comboBox);
        contentPane.repaint();
        System.out.println("Content pane repainted.");
    }

    private List<String> getAllFileList() {
        SPFileRequestDto spFileRequest = RequestDtoFactory.createSPFileRequest(RequestApplication.WEB);
        SPFileDto fileDto = new SPFileDto();
        spFileRequest.setSpFileDto(fileDto);
        spFileRequest.setUserName("gokhanabi");
        spFileRequest.setPassword("gerebic");
        SPFileResponseDto response = ServiceClient.getFileData(spFileRequest);
        List<String> files = response.getSpFileDto().getAllFileNames();
        fileNameToDLUUIDPath = response.getSpFileDto().getFileNameToUUIDMap();
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