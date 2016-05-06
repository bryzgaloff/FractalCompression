import gui.FCFrame;
import logic.MyImage;
import logic.UIMODE;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length > 0) {
            File input = null, output = null;
            int blockSize = 16;
            UIMODE uimode = UIMODE.CONSOLE;
            MODE mode = MODE.UNKNOWN;
            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "--gui":
                        uimode = UIMODE.GUI;
                        break;
                    case "-q":
                        uimode = UIMODE.QUIET;
                        break;
                    case "-c":
                        mode = MODE.COMPRESS;
                        if (++i == args.length) {
                            throw new IOException("�� ������� ��� �������� �����!");
                        }
                        input = new File(args[i]);
                        if (++i == args.length) {
                            throw new IOException("�� ������� ��� ��������� �����!");
                        }
                        output = new File(args[i]);
                        break;
                    case "-d":
                        mode = MODE.DECOMPRESS;
                        if (++i == args.length) {
                            throw new IOException("�� ������� ��� �������� �����!");
                        }
                        input = new File(args[i]);
                        if (++i == args.length) {
                            throw new IOException("�� ������� ��� ��������� �����!");
                        }
                        output = new File(args[i]);
                        break;
                    case "-s":
                        if (++i == args.length) {
                            throw new IOException("�� ������ ������ �����!");
                        }
                        blockSize = Integer.parseInt(args[i]);
                        if (blockSize != 4 && blockSize != 8 && blockSize != 16) {
                            throw new IOException("������ ����� ������ ���� 4, 8 ��� 16!");
                        }
                        break;
                }
            }
            switch (uimode) {
                case GUI:
                    SwingUtilities.invokeLater(FCFrame::new);
                    break;
                case CONSOLE:
                case QUIET:
                    switch (mode) {
                        case DECOMPRESS:
                            new MyImage(input, output);
                            break;
                        case COMPRESS:
                            new MyImage(input, blockSize, output, uimode);
                            break;
                    }
                    break;
            }
        } else {
            System.out.println("������� ��������� ���������, ����� ��������� ���������:\n" +
                    "  --gui: � ������ ������������ ����������\n" +
                    "    (������� ��������� ������ ����� �� ���������);\n" +
                    "  -q: � \"�����\" ������\n" +
                    "    (���������� �����, ������� ���������������\n" +
                    "       ��������� �� ����� ���������);\n" +
                    "  (� ������� ������ � ������� ��������� ���������������\n" +
                    "    �����������: ��������, ����� ��������� ����������\n" +
                    "    ������)\n" +
                    "  -c: � ������ ������ (���������� ������� ������� � ��������\n" +
                    "    ����� � �������� -s);\n" +
                    "  -s: ����� ����������� ������ ����� (16, 8 ��� 4);\n" +
                    "  -d: � ������ ������������ (���������� ������� ������� �\n" +
                    "    �������� �����);\n");
        }
    }

    private enum MODE { UNKNOWN, COMPRESS, DECOMPRESS }
}

