package gui;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class MyDialog extends JDialog {
    public MyDialog(JFrame owner) {
        super(owner, "Производится сжатие", false);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(240, 100);
        label = new JLabel("Компрессия начата");
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        JPanel lPanel = new JPanel();
        JPanel pbPanel = new JPanel();
        lPanel.add(label);
        pbPanel.add(progressBar);
        add(lPanel, BorderLayout.NORTH);
        add(pbPanel, BorderLayout.CENTER);
        setVisible(true);
    }

    public void setProgressBarValue(int val) {
        progressBar.setValue(val);
        progressBar.update(progressBar.getGraphics());
    }

    public void setLabelText(String text) {
        label.setText(text);
        label.update(label.getGraphics());
    }

    private JProgressBar progressBar;
    private JLabel label;
}
