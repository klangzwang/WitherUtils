package geni.witherutils.core.common.helper;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

@SuppressWarnings({ "rawtypes", "unused", "unchecked" })
public class GifDecoder {
	
    public static final int STATUS_OK = 0;
    public static final int STATUS_FORMAT_ERROR = 1;
    public static final int STATUS_OPEN_ERROR = 2;
    protected BufferedInputStream in;
    protected int status;
    protected int width;
    protected int height;
    protected boolean gctFlag;
    protected int gctSize;
    protected int loopCount = 1;
    protected int[] gct;
    protected int[] lct;
    protected int[] act;
    protected int bgIndex;
    protected int bgColor;
    protected int lastBgColor;
    protected int pixelAspect;
    protected boolean lctFlag;
    protected boolean interlace;
    protected int lctSize;
    protected int ix;
    protected int iy;
    protected int iw;
    protected int ih;
    protected Rectangle lastRect;
    protected BufferedImage image;
    protected BufferedImage lastImage;
    protected byte[] block = new byte[256];
    protected int blockSize = 0;
    protected int dispose = 0;
    protected int lastDispose = 0;
    protected boolean transparency = false;
    protected int delay = 0;
    protected int transIndex;
    protected static final int MaxStackSize = 4096;
    protected short[] prefix;
    protected byte[] suffix;
    protected byte[] pixelStack;
    protected byte[] pixels;

	protected ArrayList frames;
    protected int frameCount;

    public int getDelay(int n) {
        this.delay = -1;
        if (n >= 0 && n < this.frameCount) {
            this.delay = ((GifFrame)this.frames.get((int)n)).delay;
        }
        return this.delay;
    }

    public int getFrameCount() {
        return this.frameCount;
    }

    public BufferedImage getImage() {
        return this.getFrame(0);
    }

    public int getLoopCount() {
        return this.loopCount;
    }

    protected void setPixels() {
        int[] dest = ((DataBufferInt)this.image.getRaster().getDataBuffer()).getData();
        if (this.lastDispose > 0) {
            if (this.lastDispose == 3) {
                int n = this.frameCount - 2;
                this.lastImage = n > 0 ? this.getFrame(n - 1) : null;
            }
            if (this.lastImage != null) {
                int[] prev = ((DataBufferInt)this.lastImage.getRaster().getDataBuffer()).getData();
                System.arraycopy(prev, 0, dest, 0, this.width * this.height);
                if (this.lastDispose == 2) {
                    Graphics2D g = this.image.createGraphics();
                    Color c = null;
                    c = this.transparency ? new Color(0, 0, 0, 0) : new Color(this.lastBgColor);
                    g.setColor(c);
                    g.setComposite(AlphaComposite.Src);
                    g.fill(this.lastRect);
                    g.dispose();
                }
            }
        }
        int pass = 1;
        int inc = 8;
        int iline = 0;
        for (int i = 0; i < this.ih; ++i) {
            int line = i;
            if (this.interlace) {
                if (iline >= this.ih) {
                    switch (++pass) {
                        case 2: {
                            iline = 4;
                            break;
                        }
                        case 3: {
                            iline = 2;
                            inc = 4;
                            break;
                        }
                        case 4: {
                            iline = 1;
                            inc = 2;
                        }
                    }
                }
                line = iline;
                iline += inc;
            }
            if ((line += this.iy) >= this.height) continue;
            int k = line * this.width;
            int dx = k + this.ix;
            int dlim = dx + this.iw;
            if (k + this.width < dlim) {
                dlim = k + this.width;
            }
            int sx = i * this.iw;
            while (dx < dlim) {
                int index;
                int c;
                if ((c = this.act[index = this.pixels[sx++] & 0xFF]) != 0) {
                    dest[dx] = c;
                }
                ++dx;
            }
        }
    }

    public BufferedImage getFrame(int n) {
        BufferedImage im = null;
        if (n >= 0 && n < this.frameCount) {
            im = ((GifFrame)this.frames.get((int)n)).image;
        }
        return im;
    }

    public Dimension getFrameSize() {
        return new Dimension(this.width, this.height);
    }

    public int read(BufferedInputStream is) {
        this.init();
        if (is != null) {
            this.in = is;
            this.readHeader();
            if (!this.err()) {
                this.readContents();
                if (this.frameCount < 0) {
                    this.status = 1;
                }
            }
        } else {
            this.status = 2;
        }
        try {
            is.close();
        }
        catch (IOException iOException) {
            // empty catch block
        }
        return this.status;
    }

    public int read(InputStream is) {
        this.init();
        if (is != null) {
            if (!(is instanceof BufferedInputStream)) {
                is = new BufferedInputStream(is);
            }
            this.in = (BufferedInputStream)is;
            this.readHeader();
            if (!this.err()) {
                this.readContents();
                if (this.frameCount < 0) {
                    this.status = 1;
                }
            }
        } else {
            this.status = 2;
        }
        try {
            is.close();
        }
        catch (IOException iOException) {
            // empty catch block
        }
        return this.status;
    }

    @SuppressWarnings("deprecation")
	public int read(String name) {
        this.status = 0;
        try {
            name = name.trim().toLowerCase();
            if (name.indexOf("file:") >= 0 || name.indexOf(":/") > 0) {
                URL url = new URL(name);
                this.in = new BufferedInputStream(url.openStream());
            } else {
                this.in = new BufferedInputStream(new FileInputStream(name));
            }
            this.status = this.read(this.in);
        }
        catch (IOException e) {
            this.status = 2;
        }
        return this.status;
    }

    protected void decodeImageData() {
        int bi;
        int pi;
        int top;
        int first;
        int count;
        int bits;
        int code;
        int NullCode = -1;
        int npix = this.iw * this.ih;
        if (this.pixels == null || this.pixels.length < npix) {
            this.pixels = new byte[npix];
        }
        if (this.prefix == null) {
            this.prefix = new short[4096];
        }
        if (this.suffix == null) {
            this.suffix = new byte[4096];
        }
        if (this.pixelStack == null) {
            this.pixelStack = new byte[4097];
        }
        int data_size = this.read();
        int clear = 1 << data_size;
        int end_of_information = clear + 1;
        int available = clear + 2;
        int old_code = NullCode;
        int code_size = data_size + 1;
        int code_mask = (1 << code_size) - 1;
        for (code = 0; code < clear; ++code) {
            this.prefix[code] = 0;
            this.suffix[code] = (byte)code;
        }
        int datum = bits = (count = (first = (top = (pi = (bi = 0)))));
        int i = 0;
        while (i < npix) {
            if (top == 0) {
                if (bits < code_size) {
                    if (count == 0) {
                        count = this.readBlock();
                        if (count <= 0) break;
                        bi = 0;
                    }
                    datum += (this.block[bi] & 0xFF) << bits;
                    bits += 8;
                    ++bi;
                    --count;
                    continue;
                }
                code = datum & code_mask;
                datum >>= code_size;
                bits -= code_size;
                if (code > available || code == end_of_information) break;
                if (code == clear) {
                    code_size = data_size + 1;
                    code_mask = (1 << code_size) - 1;
                    available = clear + 2;
                    old_code = NullCode;
                    continue;
                }
                if (old_code == NullCode) {
                    this.pixelStack[top++] = this.suffix[code];
                    old_code = code;
                    first = code;
                    continue;
                }
                int in_code = code;
                if (code == available) {
                    this.pixelStack[top++] = (byte)first;
                    code = old_code;
                }
                while (code > clear) {
                    this.pixelStack[top++] = this.suffix[code];
                    code = this.prefix[code];
                }
                first = this.suffix[code] & 0xFF;
                if (available >= 4096) {
                    this.pixelStack[top++] = (byte)first;
                    continue;
                }
                this.pixelStack[top++] = (byte)first;
                this.prefix[available] = (short)old_code;
                this.suffix[available] = (byte)first;
                if ((++available & code_mask) == 0 && available < 4096) {
                    ++code_size;
                    code_mask += available;
                }
                old_code = in_code;
            }
            this.pixels[pi++] = this.pixelStack[--top];
            ++i;
        }
        for (i = pi; i < npix; ++i) {
            this.pixels[i] = 0;
        }
    }

    protected boolean err() {
        return this.status != 0;
    }

    protected void init() {
        this.status = 0;
        this.frameCount = 0;
        this.frames = new ArrayList();
        this.gct = null;
        this.lct = null;
    }

    protected int read() {
        int curByte = 0;
        try {
            curByte = this.in.read();
        }
        catch (IOException e) {
            this.status = 1;
        }
        return curByte;
    }

    protected int readBlock() {
        int n = 0;
        this.blockSize = this.read();
        if (this.blockSize > 0) {
            try {
                int count = 0;
                for (n = 0; n < this.blockSize && (count = this.in.read(this.block, n, this.blockSize - n)) != -1; n += count) {
                }
            }
            catch (IOException iOException) {
                // empty catch block
            }
            if (n < this.blockSize) {
                this.status = 1;
            }
        }
        return n;
    }

    protected int[] readColorTable(int ncolors) {
        int nbytes = 3 * ncolors;
        int[] tab = null;
        byte[] c = new byte[nbytes];
        int n = 0;
        try {
            n = this.in.read(c);
        }
        catch (IOException iOException) {
            // empty catch block
        }
        if (n < nbytes) {
            this.status = 1;
        } else {
            tab = new int[256];
            int i = 0;
            int j = 0;
            while (i < ncolors) {
                int r = c[j++] & 0xFF;
                int g = c[j++] & 0xFF;
                int b = c[j++] & 0xFF;
                tab[i++] = 0xFF000000 | r << 16 | g << 8 | b;
            }
        }
        return tab;
    }

    protected void readContents() {
        boolean done = false;
        block10: while (!done && !this.err()) {
            int code = this.read();
            switch (code) {
                case 44: {
                    this.readImage();
                    continue block10;
                }
                case 33: {
                    code = this.read();
                    switch (code) {
                        case 249: {
                            this.readGraphicControlExt();
                            continue block10;
                        }
                        case 255: {
                            this.readBlock();
                            Object app = "";
                            for (int i = 0; i < 11; ++i) {
                                app = (String)app + (char)this.block[i];
                            }
                            if (((String)app).equals("NETSCAPE2.0")) {
                                this.readNetscapeExt();
                                continue block10;
                            }
                            this.skip();
                            continue block10;
                        }
                    }
                    this.skip();
                    continue block10;
                }
                case 59: {
                    done = true;
                    continue block10;
                }
                case 0: {
                    continue block10;
                }
            }
            this.status = 1;
        }
    }

    protected void readGraphicControlExt() {
        this.read();
        int packed = this.read();
        this.dispose = (packed & 0x1C) >> 2;
        if (this.dispose == 0) {
            this.dispose = 1;
        }
        this.transparency = (packed & 1) != 0;
        this.delay = this.readShort() * 10;
        this.transIndex = this.read();
        this.read();
    }

    protected void readHeader() {
        Object id = "";
        for (int i = 0; i < 6; ++i) {
            id = (String)id + (char)this.read();
        }
        if (!((String)id).startsWith("GIF")) {
            this.status = 1;
            return;
        }
        this.readLSD();
        if (this.gctFlag && !this.err()) {
            this.gct = this.readColorTable(this.gctSize);
            this.bgColor = this.gct[this.bgIndex];
        }
    }

    protected void readImage() {
        this.ix = this.readShort();
        this.iy = this.readShort();
        this.iw = this.readShort();
        this.ih = this.readShort();
        int packed = this.read();
        this.lctFlag = (packed & 0x80) != 0;
        this.interlace = (packed & 0x40) != 0;
        this.lctSize = 2 << (packed & 7);
        if (this.lctFlag) {
            this.lct = this.readColorTable(this.lctSize);
            this.act = this.lct;
        } else {
            this.act = this.gct;
            if (this.bgIndex == this.transIndex) {
                this.bgColor = 0;
            }
        }
        int save = 0;
        if (this.transparency) {
            save = this.act[this.transIndex];
            this.act[this.transIndex] = 0;
        }
        if (this.act == null) {
            this.status = 1;
        }
        if (this.err()) {
            return;
        }
        this.decodeImageData();
        this.skip();
        if (this.err()) {
            return;
        }
        ++this.frameCount;
        this.image = new BufferedImage(this.width, this.height, 3);
        this.setPixels();
        this.frames.add(new GifFrame(this.image, this.delay));
        if (this.transparency) {
            this.act[this.transIndex] = save;
        }
        this.resetFrame();
    }

    protected void readLSD() {
        this.width = this.readShort();
        this.height = this.readShort();
        int packed = this.read();
        this.gctFlag = (packed & 0x80) != 0;
        this.gctSize = 2 << (packed & 7);
        this.bgIndex = this.read();
        this.pixelAspect = this.read();
    }

    protected void readNetscapeExt() {
        do {
            this.readBlock();
            if (this.block[0] != 1) continue;
            int b1 = this.block[1] & 0xFF;
            int b2 = this.block[2] & 0xFF;
            this.loopCount = b2 << 8 | b1;
        } while (this.blockSize > 0 && !this.err());
    }

    protected int readShort() {
        return this.read() | this.read() << 8;
    }

    protected void resetFrame() {
        this.lastDispose = this.dispose;
        this.lastRect = new Rectangle(this.ix, this.iy, this.iw, this.ih);
        this.lastImage = this.image;
        this.lastBgColor = this.bgColor;
        this.lct = null;
    }

    protected void skip() {
        do {
            this.readBlock();
        } while (this.blockSize > 0 && !this.err());
    }

    static class GifFrame {
        public BufferedImage image;
        public int delay;

        public GifFrame(BufferedImage im, int del) {
            this.image = im;
            this.delay = del;
        }
    }
}

