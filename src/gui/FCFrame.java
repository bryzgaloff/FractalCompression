package gui;

import logic.MyImage;
import logic.UIMODE;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;

public class FCFrame extends JFrame {
    public FCFrame() {
        super("������� ����������� ���������");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        label = new JLabel();
        JPanel imgPanel = new JPanel();
        imgPanel.add(label);
        final JButton dcmprBtn = new MyButton("������������", "��������� ���� �������� �������������� " +
                "(������������� ��������� ����� ����� ��������)");
        dcmprBtn.setEnabled(false);
        JButton saveBtn = new MyButton("��������� �����", "��������� ������������ ��������� � ����");
        saveBtn.setEnabled(false);
        switch (JOptionPane.showConfirmDialog(this, "�� ������ ������������ ����?\n������� NO, ���� ������ ������� ����� " +
                "��� �����������.", "�������� ����� ������ ���������", JOptionPane.YES_NO_OPTION)) {
            case JOptionPane.YES_OPTION:
                openImage(true);
                break;
            case JOptionPane.NO_OPTION:
                JOptionPane.showMessageDialog(this, "�������� �����");
                JFileChooser fileChooser = new JFileChooser(new File("."));
                fileChooser.showOpenDialog(this);
                File file = fileChooser.getSelectedFile();
                if (file != null) {
                    try {
                        img = new MyImage(file);
                        dcmprBtn.setEnabled(true);
                        updImg();
                    } catch (IOException e) {
                        e.printStackTrace();
                        // TODO handler
                    }
                    break;
                }
            default:
                dispose();
                System.exit(0);
        }
        JPanel btnsPanel = new JPanel();

        JButton cmprBtn = new MyButton("�����", "�������� ������������ ���������", btnsPanel);
        cmprBtn.addActionListener(actionEvent -> {
            MyDialog dialog;
            img.compress(dialog = new MyDialog(FCFrame.this));
            dialog.dispose();
            JOptionPane.showMessageDialog(FCFrame.this, "����������� ��������� ��������!\n������ �� ������ " +
                            "��������� ����� ��� ������������ �����������.", "��������� ���������",
                    JOptionPane.INFORMATION_MESSAGE);
            dcmprBtn.setEnabled(true);
            saveBtn.setEnabled(true);
        });

        btnsPanel.add(saveBtn);
        saveBtn.addActionListener(actionEvent -> {
            JFileChooser fileChooser = new JFileChooser(new File("."));
            fileChooser.showSaveDialog(FCFrame.this);
            File file = fileChooser.getSelectedFile();
            if (file != null) {
                try {
                    img.save(file);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(FCFrame.this, "��������� ������ ��� ���������� �����: " +
                            ex.getMessage(), "������", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(FCFrame.this, "��������� ������ ��� �������� �����", "������",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton loadBtn = new MyButton("������� �����", "��������� ����������� ��������� �� �����", btnsPanel);
        loadBtn.addActionListener(actionEvent -> {
            JOptionPane.showMessageDialog(FCFrame.this, "�������� �����");
            JFileChooser fileChooser = new JFileChooser(new File("."));
            fileChooser.showOpenDialog(FCFrame.this);
            File file = fileChooser.getSelectedFile();
            try {
                img = new MyImage(file);
                dcmprBtn.setEnabled(true);
                updImg();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(FCFrame.this, "��������� ������ ��� �������� �����: " +
                        ex.getMessage(), "������", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnsPanel.add(dcmprBtn);
        dcmprBtn.addActionListener(actionEvent -> {
            img.decompress();
            updImg();
        });

        JButton setGrayBtn = new MyButton("������� �����",
                "������� ����������� ��������� ����� (������������� ��� ��������������)", btnsPanel);
        setGrayBtn.addActionListener(actionEvent -> {
            img.setGray();
            updImg();
        });

        JButton openBtn = new MyButton("�������", "������� ������ ����������� ��� ��������� / ��������������", btnsPanel);
        openBtn.addActionListener(actionEvent -> openImage(false));

        JButton saveImageBtn = new MyButton("���������", "��������� ����������� � ����", btnsPanel);
        saveImageBtn.addActionListener(actionEvent -> {
            JFileChooser fileChooser = new JFileChooser(new File("."));
            fileChooser.showSaveDialog(FCFrame.this);
            File file = fileChooser.getSelectedFile();
            if (file != null) {
                try {
                    ImageIO.write(img.getBufferedImage(), "bmp", file);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(FCFrame.this, "�� ������ ����", "���������� �� �������",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        getContentPane().add(imgPanel, BorderLayout.CENTER);
        getContentPane().add(btnsPanel, BorderLayout.SOUTH);
        setVisible(true);
        pack();
    }

    private void update() {
        update(getGraphics());
    }

    public void updImg() {
        BufferedImage bufferedImage = img.getBufferedImage();
        label.setIcon(new ImageIcon(bufferedImage));
        update();
    }

    private void openImage(boolean newImage) {
        JFileChooser fileChooser = new JFileChooser(new File("."));
        BufferedImage image = null;
        if (newImage) {
            JOptionPane.showMessageDialog(this, "�������� ���� ��� ���������");
        }
        while (image == null) {
            File file;
            fileChooser.showOpenDialog(this);
            file = fileChooser.getSelectedFile();
            if (file == null) {
                dispose();
                System.exit(0);
            }
            try {
                image = ImageIO.read(file);
            } catch (IOException ex) {
                ex.printStackTrace();
                image = null;
                if (JOptionPane.showConfirmDialog(this, "�������� ������: " + ex.getMessage() + "\n�������� ���������� ����.",
                        "���� �� ������", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {
                    dispose();
                    System.exit(0);
                }
            }
            if (image != null && image.getHeight() != image.getWidth()) {
                if (JOptionPane.showConfirmDialog(this, "����������� ������ ���� ����������.\n�������� ������ ����.",
                        "���� �� ������", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {
                    dispose();
                    System.exit(0);
                }
            }
        }
        if (newImage) {
            Integer N[] = {4, 8, 16};
            Integer bs = (Integer) JOptionPane.showInputDialog(this, "�������� ������ �����:", "������ �����", JOptionPane.QUESTION_MESSAGE,
                    null, N, 16);
            if (bs == null) {
                dispose();
                System.exit(0);
            }
            img = new MyImage(image, bs);
        } else {
            img.setRGB(image);
        }
        updImg();
    }

    private MyImage img;
    private final JLabel label;
}

class MyButton extends JButton {
    public MyButton(String title, String tooltip) {
        super(title);
        createToolTip();
        setToolTipText(tooltip);
    }

    public MyButton(String title, String tooltip, JPanel btns) {
        this(title, tooltip);
        btns.add(this);
    }
}