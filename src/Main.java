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
                            throw new IOException("Не указано имя входного файла!");
                        }
                        input = new File(args[i]);
                        if (++i == args.length) {
                            throw new IOException("Не указано имя выходного файла!");
                        }
                        output = new File(args[i]);
                        break;
                    case "-d":
                        mode = MODE.DECOMPRESS;
                        if (++i == args.length) {
                            throw new IOException("Не указано имя входного файла!");
                        }
                        input = new File(args[i]);
                        if (++i == args.length) {
                            throw new IOException("Не указано имя выходного файла!");
                        }
                        output = new File(args[i]);
                        break;
                    case "-s":
                        if (++i == args.length) {
                            throw new IOException("Не указан размер блока!");
                        }
                        blockSize = Integer.parseInt(args[i]);
                        if (blockSize != 4 && blockSize != 8 && blockSize != 16) {
                            throw new IOException("Размер блока должен быть 4, 8 или 16!");
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
            System.out.println("Укажите следующие параметры, чтобы запустить программу:\n" +
                    "  --gui: в режиме графического интерфейса\n" +
                    "    (никакие параметры больше можно не указывать);\n" +
                    "  -q: в \"тихом\" режиме\n" +
                    "    (консольный режим, никакие диагностические\n" +
                    "       сообщения не будут выводится);\n" +
                    "  (в обычном режиме в консоли выводится диагностическая\n" +
                    "    информациия: например, шкала прогресса выполнения\n" +
                    "    сжатия)\n" +
                    "  -c: в режиме сжатия (необходимо указать входной и выходной\n" +
                    "    файлы и параметр -s);\n" +
                    "  -s: далее указывается размер блока (16, 8 или 4);\n" +
                    "  -d: в режиме декомпрессии (необходимо указать входной и\n" +
                    "    выходной файлы);\n");
        }
    }

    private enum MODE { UNKNOWN, COMPRESS, DECOMPRESS }
}

