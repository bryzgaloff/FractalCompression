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
        super("Учебный фрактальный архиватор");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        label = new JLabel();
        JPanel imgPanel = new JPanel();
        imgPanel.add(label);
        final JButton dcmprBtn = new MyButton("Восстановить", "Выполнить одну итерацию восстановления " +
                "(рекомендуется выполнить более одной итерации)");
        dcmprBtn.setEnabled(false);
        JButton saveBtn = new MyButton("Сохранить архив", "Сохранить коэффициенты архивации в файл");
        saveBtn.setEnabled(false);
        switch (JOptionPane.showConfirmDialog(this, "Вы хотите архивировать файл?\nНажмите NO, если хотите выбрать архив " +
                "для деархивации.", "Выберите режим работы программы", JOptionPane.YES_NO_OPTION)) {
            case JOptionPane.YES_OPTION:
                openImage(true);
                break;
            case JOptionPane.NO_OPTION:
                JOptionPane.showMessageDialog(this, "Выберите архив");
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

        JButton cmprBtn = new MyButton("Сжать", "Получить коэффициенты архивации", btnsPanel);
        cmprBtn.addActionListener(actionEvent -> {
            MyDialog dialog;
            img.compress(dialog = new MyDialog(FCFrame.this));
            dialog.dispose();
            JOptionPane.showMessageDialog(FCFrame.this, "Коэффиценты архивации получены!\nТеперь вы можете " +
                            "сохранить архив или восстановить изображение.", "Архивация завершена",
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
                    JOptionPane.showMessageDialog(FCFrame.this, "Произошла ошибка при сохранении файла: " +
                            ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(FCFrame.this, "Произошла ошибка при открытии файла", "Ошибка",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton loadBtn = new MyButton("Открыть архив", "Загрузить коэффиценты архивации из файла", btnsPanel);
        loadBtn.addActionListener(actionEvent -> {
            JOptionPane.showMessageDialog(FCFrame.this, "Выберите архив");
            JFileChooser fileChooser = new JFileChooser(new File("."));
            fileChooser.showOpenDialog(FCFrame.this);
            File file = fileChooser.getSelectedFile();
            try {
                img = new MyImage(file);
                dcmprBtn.setEnabled(true);
                updImg();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(FCFrame.this, "Произошла ошибка при открытии файла: " +
                        ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnsPanel.add(dcmprBtn);
        dcmprBtn.addActionListener(actionEvent -> {
            img.decompress();
            updImg();
        });

        JButton setGrayBtn = new MyButton("Сделать серым",
                "Сделать изображение полностью серым (рекомендуется для восстановления)", btnsPanel);
        setGrayBtn.addActionListener(actionEvent -> {
            img.setGray();
            updImg();
        });

        JButton openBtn = new MyButton("Открыть", "Открыть другое изображение для архивации / восстановления", btnsPanel);
        openBtn.addActionListener(actionEvent -> openImage(false));

        JButton saveImageBtn = new MyButton("Сохранить", "Сохранить изображение в файл", btnsPanel);
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
                JOptionPane.showMessageDialog(FCFrame.this, "Не выбран файл", "Сохранение не удалось",
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
            JOptionPane.showMessageDialog(this, "Выберите файл для архивации");
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
                if (JOptionPane.showConfirmDialog(this, "Возникла ошибка: " + ex.getMessage() + "\nВыберите корректный файл.",
                        "Файл не выбран", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {
                    dispose();
                    System.exit(0);
                }
            }
            if (image != null && image.getHeight() != image.getWidth()) {
                if (JOptionPane.showConfirmDialog(this, "Изображение должно быть квадратным.\nВыберите другой файл.",
                        "Файл не выбран", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {
                    dispose();
                    System.exit(0);
                }
            }
        }
        if (newImage) {
            Integer N[] = {4, 8, 16};
            Integer bs = (Integer) JOptionPane.showInputDialog(this, "Выберите размер блока:", "Размер блока", JOptionPane.QUESTION_MESSAGE,
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