package logic;

import gui.MyDialog;

import javax.imageio.ImageIO;

import static java.lang.Math.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

public class MyImage {
    public MyImage(BufferedImage image, int BLOCK_SIZE) {
        this.mode = UIMODE.GUI;
        N = image.getWidth();
        this.BLOCK_SIZE = BLOCK_SIZE;
        rgb = new int[N][N];
        Y = new ImageMap(N, BLOCK_SIZE);
        setRGB(image);
    }

    public MyImage(File in, File... out) throws IOException {
        if (out.length == 0) {
            mode = UIMODE.GUI;
        }
        FileInputStream is = new FileInputStream(in);
        int meta = is.read();
        grayScale = (meta & 1) != 0;
        meta &= ~1;
        BLOCK_SIZE = (meta & 0xF) * 2;
        N = ((meta & 0x10) != 0) ? 512 : 256;
        rgb = new int[N][N];
        Y = new ImageMap(N, BLOCK_SIZE);
        Y.load(is);
        if (!grayScale) {
            U = new ImageMap(N / 2, BLOCK_SIZE);
            U.load(is);
            V = new ImageMap(N / 2, BLOCK_SIZE);
            V.load(is);
        }
        setGray();
        if (mode != UIMODE.GUI) {
            for (int i = 0; i < 20; i++) {
                decompress();
            }
            ImageIO.write(getBufferedImage(), "bmp", out[0]);
        }
        is.close();
    }

    public MyImage(File in, int BLOCK_SIZE, File out, UIMODE mode) throws IOException {
        this(ImageIO.read(in), BLOCK_SIZE);
        this.mode = mode;
        compress();
        save(out);
    }

    public void compress(MyDialog... dialog) {
        print("Сжатие Y", dialog);
        Y.compress(mode, dialog);
        if (!isGrayScale()) {
            print("Сжатие U", dialog);
            U.compress(mode, dialog);
            print("Сжатие V", dialog);
            V.compress(mode, dialog);
        }
    }

    private void print(String msg, MyDialog... dialog) {
        if (mode == UIMODE.CONSOLE) {
            System.out.println(msg);
        } else if (mode == UIMODE.GUI) {
            dialog[0].setLabelText(msg);
        }
    }

    public void decompress() {
        Y.decompress();
        if (!isGrayScale()) {
            U.decompress();
            V.decompress();
        }
    }

    public void updateYUV() {
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                int r = (rgb[i][j] & 0xFF0000) >> 16;
                int g = (rgb[i][j] & 0xFF00) >> 8;
                int b = (rgb[i][j] & 0xFF);
                int y = max(min((int) (0.299 * r + 0.587 * g + 0.114 * b), 255), 0);
                Y.map[i][j] = y;
            }
        }
        if (!isGrayScale()) {
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    int r = (rgb[i][j] & 0xFF0000) >> 16;
                    int b = (rgb[i][j] & 0xFF);
                    int y = Y.map[i][j];
                    U.map[i / 2][j / 2] = max(min((int) (0.56 * (b - y) + 128), 255), 0);
                    V.map[i / 2][j / 2] = max(min((int) (0.713 * (r - y) + 128), 255), 0);
                }
            }
        }
    }

    public void updateRGB() {
        if (isGrayScale()) {
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    int y = Y.map[i][j];
                    rgb[i][j] = (y << 16) + (y << 8) + y;
                }
            }
        } else {
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    int y = Y.map[i][j];
                    int u = U.map[i / 2][j / 2] - 128;
                    int v = V.map[i / 2][j / 2] - 128;
                    int r = max(min((int) (y + 1.402 * v), 255), 0);
                    int g = max(min((int) (y - 0.71413 * v - 0.3468 * u), 255), 0);
                    int b = max(min((int) (y + 1.786 * u), 255), 0);
                    rgb[i][j] = (r << 16) + (g << 8) + b;
                }
            }
        }
    }

    public BufferedImage getBufferedImage() {
        updateRGB();
        BufferedImage img = new BufferedImage(N, N, BufferedImage.TYPE_3BYTE_BGR);
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                img.setRGB(i, j, rgb[i][j]);
            }
        }
        return img;
    }

    public void setGray() {
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                rgb[i][j] = 0x7F7F7F;
            }
        }
        updateYUV();
    }

    public void setRGB(BufferedImage img) {
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                rgb[i][j] = img.getRGB(i, j);
            }
        }
        grayScale = null;
        if (!isGrayScale()) {
            if (U == null) {
                U = new ImageMap(N / 2, BLOCK_SIZE);
            }
            if (V == null) {
                V = new ImageMap(N / 2, BLOCK_SIZE);
            }
        }
        updateYUV();
    }

    public boolean isGrayScale() {
        if (grayScale != null) {
            return grayScale;
        }
        grayScale = true;
        for (int i = 0; i < N && grayScale; i++) {
            for (int j = 0; j < N && grayScale; j++) {
                int r = (rgb[i][j] & 0xFF0000) >> 16;
                int g = (rgb[i][j] & 0xFF00) >> 8;
                int b = (rgb[i][j] & 0xFF);
                grayScale = (r == g) && (g == b);
            }
        }
        return grayScale;
    }

    public void save(File file) throws IOException {
        FileOutputStream out = new FileOutputStream(file);
        byte meta = (byte)(((N == 256 ? 0 : 1) << 4) + BLOCK_SIZE / 2 + (isGrayScale() ? 1 : 0));
        try {
            out.write(meta);
            Y.save(out);
            if (!isGrayScale()) {
                U.save(out);
                V.save(out);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        out.close();
    }

    private int[][] rgb;
    Boolean grayScale = null;
    private final int N, BLOCK_SIZE;
    private final ImageMap Y;
    private ImageMap U, V;
    UIMODE mode;

    private class ImageMap {
        public ImageMap(int N, int BLOCK_SIZE) {
            this.N = N;
            this.BLOCK_SIZE = BLOCK_SIZE;
            map = new int[N][N];
            final int BLOCK_NUM = N / BLOCK_SIZE;
            x = new short[BLOCK_NUM][BLOCK_NUM];
            y = new short[BLOCK_NUM][BLOCK_NUM];
            k = new byte[BLOCK_NUM][BLOCK_NUM];
            q = new short[BLOCK_NUM][BLOCK_NUM];
            sumr = new long[BLOCK_NUM][BLOCK_NUM];
            sumsqrr = new long[BLOCK_NUM][BLOCK_NUM];
            p = new short[BLOCK_NUM][BLOCK_NUM];
            sumd = new long[BLOCK_NUM / 2][BLOCK_NUM / 2];
            sumsqrd = new long[BLOCK_NUM / 2][BLOCK_NUM / 2];
        }

        public void compress(UIMODE mode, MyDialog... dialog) {
            final int BLOCK_NUM = N / BLOCK_SIZE;
            // подсчет сумм Rij и Rij ^ 2
            for (int i = 0; i < BLOCK_NUM; i++) {
                for (int j = 0; j < BLOCK_NUM; j++) {
                    sumr[i][j] = 0;
                    sumsqrr[i][j] = 0;
                    int ib = i * BLOCK_SIZE;
                    int jb = j * BLOCK_SIZE;
                    for (int s = 0; s < BLOCK_SIZE; s++) {
                        for (int t = 0; t < BLOCK_SIZE; t++) {
                            sumr[i][j] += map[ib + s][jb + t];
                            sumsqrr[i][j] += pow(map[ib + s][jb + t], 2);
                        }
                    }
                }
            }
            // подсчет сумм Dij и Dij ^ 2
            for (int i = 0; i < BLOCK_NUM / 2; i++) {
                for (int j = 0; j < BLOCK_NUM / 2; j++) {
                    sumd[i][j] = 0;
                    sumsqrd[i][j] = 0;
                    int ib = i * 2 * BLOCK_SIZE;
                    int jb = j * 2 * BLOCK_SIZE;
                    for (int s = 0; s < 2 * BLOCK_SIZE; s += 2) {
                        for (int t = 0; t < 2 * BLOCK_SIZE; t += 2) {
                            int tmp = (map[ib + s][jb + t] +
                                    map[ib + s][jb + t + 1] +
                                    map[ib + s + 1][jb + t] +
                                    map[ib + s + 1][jb + t + 1]) / 4;
                            sumd[i][j] += tmp;
                            sumsqrd[i][j] += pow(tmp, 2);
                        }
                    }
                }
            }
            int pr = -1;
            for (int i = 0; i < BLOCK_NUM; i++) {
                for (int j = 0; j < BLOCK_NUM; j++) {
                    double best = Double.MAX_VALUE;
                    for (short s = 0; s < BLOCK_NUM; s += 2) {
                        for (short t = 0; t < BLOCK_NUM; t += 2) {
                            for (byte w = 0; w < 8; w++) {
                                double tmp;
                                if ((tmp = diff(i, j, transform(s, t, w, false), s / 2, t / 2, best)) < best) {
                                    best = tmp;
                                    k[i][j] = w;
                                }
                            }
                        }
                    }
                    if ((int)(100.0 * (i * BLOCK_NUM + j) / (BLOCK_NUM * BLOCK_NUM)) > pr) {
                        pr = (int) ((double) (i * BLOCK_NUM + j) / (BLOCK_NUM * BLOCK_NUM) * 100);
                        if (mode == UIMODE.CONSOLE) {
                            System.out.print("|");
                            int k = pr / 10;
                            for (int p = 0; p < k; p++) {
                                System.out.print("=");
                            }
                            for (int p = k; p < 10; p++) {
                                System.out.print(" ");
                            }
                            System.out.print("| " + pr + "%\r");
                        } else if (mode == UIMODE.GUI) {
                            dialog[0].setProgressBarValue(pr);
                        }
                    }
                }
            }
            if (mode == UIMODE.CONSOLE) {
                System.out.println("Успешно    | 100%");
            }
        }

        private double diff(int i, int j, int[][] dom, int s, int t, double best) {
            int ib = i * BLOCK_SIZE;
            int jb = j * BLOCK_SIZE;
            int[][] d = new int[BLOCK_SIZE][BLOCK_SIZE];
            long rd = 0;
            for (int m = 0; m < BLOCK_SIZE; m++) {
                for (int k = 0; k < BLOCK_SIZE; k++) {
                    int tmp = (dom[2 * m][2 * k] +
                            dom[2 * m][2 * k + 1] +
                            dom[2 * m + 1][2 * k] +
                            dom[2 * m + 1][2 * k + 1]) / 4;
                    d[m][k] = tmp;
                    rd += map[ib + m][jb + k] * d[m][k];
                }
            }
            double tmp = (BLOCK_SIZE * BLOCK_SIZE * sumsqrd[s][t] - sumd[s][t] * sumd[s][t]), p = 0;
            if (tmp != 0) {
                p = 1.0 * (BLOCK_SIZE * BLOCK_SIZE * rd - sumr[i][j] * sumd[s][t]) / tmp;
            }
            p = max(min(p, 191.0 / 255), 64.0 / 255); // ~ [0.25 .. 0.75]
            p = (int)(p * 255) / 255.0;
            double q = (sumr[i][j] - p * sumd[s][t]) / (BLOCK_SIZE * BLOCK_SIZE);
            double sum = p * p * sumsqrd[s][t] + BLOCK_SIZE * BLOCK_SIZE * q * q + sumsqrr[i][j] -
                    2 * p * rd + 2 * p * q * sumd[s][t] - 2 * q * sumr[i][j];
            if (sum < best) {
                this.q[i][j] = (short)(max(min(q, 255), -255) + 255);
                this.p[i][j] = (short)(p * 255 - 64);
                x[i][j] = (byte)s;
                y[i][j] = (byte)t;
            }
            return sum;
        }

        public void decompress() {
            final int[][] res = new int[N][N];
            for (int i = 0; i < N; i += BLOCK_SIZE) {
                for (int j = 0; j < N; j += BLOCK_SIZE) {
                    int x = this.x[i / BLOCK_SIZE][j / BLOCK_SIZE] * 2;
                    int y = this.y[i / BLOCK_SIZE][j / BLOCK_SIZE] * 2;
                    int[][] dom = transform(x, y, k[i / BLOCK_SIZE][j / BLOCK_SIZE], false);
                    int q = this.q[i / BLOCK_SIZE][j / BLOCK_SIZE] - 255;
                    double p = (this.p[i / BLOCK_SIZE][j / BLOCK_SIZE] + 64) / 255.0;
                    for (int s = 0; s < BLOCK_SIZE; s++) {
                        for (int t = 0; t < BLOCK_SIZE; t++) {
                            int tmp = (dom[2 * s][2 * t] +
                                    dom[2 * s][2 * t + 1] +
                                    dom[2 * s + 1][2 * t] +
                                    dom[2 * s + 1][2 * t + 1]) / 4;
                            res[i + s][j + t] = (int)(p * tmp + q);
                        }
                    }
                }
            }
            map = res;
        }

        private int[][] transform(int m, int k, int w, boolean resident) {
            int mul = resident ? 1 : 2;
            m *= BLOCK_SIZE;
            k *= BLOCK_SIZE;
            int[][] tr = new int[mul * BLOCK_SIZE][mul * BLOCK_SIZE];
            final int BS = mul * BLOCK_SIZE - 1;
            switch (w) {
                case 0: // E
                    for (int i = 0; i < mul * BLOCK_SIZE; i++) {
                        System.arraycopy(map[m + i], k, tr[i], 0, mul * BLOCK_SIZE);
                    }
                    break;
                case 1: // 90
                    for (int i = 0; i < mul * BLOCK_SIZE; i++) {
                        for (int j = 0; j < mul * BLOCK_SIZE; j++) {
                            tr[i][j] = map[m + BS - j][k + i];
                        }
                    }
                    break;
                case 2: // 180
                    for (int i = 0; i < mul * BLOCK_SIZE; i++) {
                        for (int j = 0; j < mul * BLOCK_SIZE; j++) {
                            tr[i][j] = map[m + BS - i][k + BS - j];
                        }
                    }
                    break;
                case 3: // 270
                    for (int i = 0; i < mul * BLOCK_SIZE; i++) {
                        for (int j = 0; j < mul * BLOCK_SIZE; j++) {
                            tr[i][j] = map[m + j][k + BS - i];
                        }
                    }
                    break;
                case 4: // inverse horizontal
                    for (int i = 0; i < mul * BLOCK_SIZE; i++) {
                        System.arraycopy(map[m + BS - i], k, tr[i], 0, mul * BLOCK_SIZE);
                    }
                    break;
                case 5: // inverse vertical
                    for (int i = 0; i < mul * BLOCK_SIZE; i++) {
                        for (int j = 0; j < mul * BLOCK_SIZE; j++) {
                            tr[i][j] = map[m + i][k + BS - j];
                        }
                    }
                    break;
                case 6: // inverse horizontal + 90 == transposition
                    for (int i = 0; i < mul * BLOCK_SIZE; i++) {
                        for (int j = 0; j < mul * BLOCK_SIZE; j++) {
                            tr[i][j] = map[m + j][k + i];
                        }
                    }
                    break;
                case 7: // inverse vertical + 90 == reverse transposition
                    for (int i = 0; i < mul * BLOCK_SIZE; i++) {
                        for (int j = 0; j < mul * BLOCK_SIZE; j++) {
                            tr[i][j] = map[m + BS - j][k + BS - i];
                        }
                    }
                    break;
            }
            return tr;
        }

        public void save(FileOutputStream out) throws IOException {
            final int BLOCK_NUM = N / BLOCK_SIZE;
            int sz = log2(N / BLOCK_SIZE / 2); // sizeof(x)
            byte[] data = new byte[BLOCK_NUM * BLOCK_NUM * (9 + 7 + 3 + 2 * sz) / 8]; // q + p + k + (x + y)
            int t = 0;
            for (int i = 0; i < BLOCK_NUM; i++) {
                for (int j = 0; j < BLOCK_NUM; j++) {
                    int q = this.q[i][j];
                    int p = this.p[i][j];
                    int k = this.k[i][j];
                    int x = this.x[i][j];
                    int y = this.y[i][j];
                    for (int m = 0; m < 9; m++) {
                        data[t / 8] |= ((q & (1 << m)) >> m) << t % 8;
                        ++t;
                    }
                    for (int m = 0; m < 7; m++) {
                        data[t / 8] |= ((p & (1 << m)) >> m) << t % 8;
                        ++t;
                    }
                    for (int m = 0; m < 3; m++) {
                        data[t / 8] |= ((k & (1 << m)) >> m) << t % 8;
                        ++t;
                    }
                    for (int m = 0; m < sz; m++) {
                        data[t / 8] |= ((x & (1 << m)) >> m) << t % 8;
                        ++t;
                    }
                    for (int m = 0; m < sz; m++) {
                        data[t / 8] |= ((y & (1 << m)) >> m) << t % 8;
                        ++t;
                    }
                }
            }
            out.write(data);
        }

        private int log2(int a) {
            return (int)(log(a) / log(2));
        }

        public void load(InputStream in) throws IOException {
            final int BLOCK_NUM = N / BLOCK_SIZE;
            int sz = log2(N / 2 / BLOCK_SIZE); // sizeof(x)
            int size = BLOCK_NUM * BLOCK_NUM * (9 + 7 + 3 + 2 * sz) / 8; // q + p + k + (x + y)
            byte[] data = new byte[size];
            if (in.read(data) < size) {
                throw new IOException("Недостаточно данных");
            }
            int t = 0;
            for (int i = 0; i < BLOCK_NUM; i++) {
                for (int j = 0; j < BLOCK_NUM; j++) {
                    int q = 0;
                    for (int m = 0; m < 9; m++) {
                        q |= ((data[t / 8] & (1 << t % 8)) >> t % 8) << m;
                        ++t;
                    }
                    this.q[i][j] = (short)q;
                    int p = 0;
                    for (int m = 0; m < 7; m++) {
                        p |= ((data[t / 8] & (1 << t % 8)) >> t % 8) << m;
                        ++t;
                    }
                    this.p[i][j] = (short)p;
                    int k = 0;
                    for (int m = 0; m < 3; m++) {
                        k |= ((data[t / 8] & (1 << t % 8)) >> t % 8) << m;
                        ++t;
                    }
                    this.k[i][j] = (byte)k;
                    int x = 0;
                    for (int m = 0; m < sz; m++) {
                        x |= ((data[t / 8] & (1 << t % 8)) >> t % 8) << m;
                        ++t;
                    }
                    this.x[i][j] = (short)x;
                    int y = 0;
                    for (int m = 0; m < sz; m++) {
                        y |= ((data[t / 8] & (1 << t % 8)) >> t % 8) << m;
                        ++t;
                    }
                    this.y[i][j] = (short)y;
                }
            }
        }

        private int[][] map;
        private final short[][] q;
        private final int N, BLOCK_SIZE;
        private final short[][] x, y, p;
        private final byte[][] k;
        private final long[][] sumr, sumsqrr, sumd, sumsqrd;
    }
}
