// $Id$
/*
 * ====================================================================
 * Copyright (c) 2002-2004, Christophe Labouisse All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package net.ggtools.grand.ui.image;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;

/**
 * A class to save SWT Image to disk.
 * 
 * @author Christophe Labouisse
 */
public class ImageSaver {

    private static class ColorCounter implements Comparable<ColorCounter> {
        /**
         * Logger for this class
         */
        @SuppressWarnings("unused")
        private static final Log log = LogFactory.getLog(ColorCounter.class);

        int count;

        RGB rgb;

        public int compareTo(final ColorCounter o) {
            return o.count - count;
        }
    }

    /**
     * 
     * @author Christophe Labouisse
     */
    private static class ImageFormat {

        @SuppressWarnings("unused")
        public final String name;
        public final int swtId;
        public final boolean needDownsampling;

        public ImageFormat(final String name, final int swtId, final boolean needDownsampling) {
            this.name = name;
            this.swtId = swtId;
            this.needDownsampling = needDownsampling;
        }
    }

    private static boolean formatInitDone = false;

    private static final Map<String, ImageFormat> formatRegistry =
            new HashMap<String, ImageFormat>();

    /**
     * Logger for this class
     */
    private static final Log log = LogFactory.getLog(ImageSaver.class);

    private static String[] supportedExtensions;

    private static int closest(final RGB[] rgbs, final int n, final RGB rgb) {
        int minDist = 256 * 256 * 3;
        int minIndex = 0;
        for (int i = 0; i < n; ++i) {
            final RGB rgb2 = rgbs[i];
            final int da = rgb2.red - rgb.red;
            final int dg = rgb2.green - rgb.green;
            final int db = rgb2.blue - rgb.blue;
            final int dist = da * da + dg * dg + db * db;
            if (dist < minDist) {
                minDist = dist;
                minIndex = i;
            }
        }
        return minIndex;
    }

    private static ImageData downSample(final Image image) {
        final ImageData data = image.getImageData();
        if (!data.palette.isDirect && (data.depth <= 8)) {
            return data;
        }

        // compute a histogram of color frequencies
        final HashMap<RGB, ColorCounter> freq = new HashMap<RGB, ColorCounter>();
        final int width = data.width;
        final int[] pixels = new int[width];
        final int[] maskPixels = new int[width];
        for (int y = 0, height = data.height; y < height; ++y) {
            data.getPixels(0, y, width, pixels, 0);
            for (int x = 0; x < width; ++x) {
                final RGB rgb = data.palette.getRGB(pixels[x]);
                ColorCounter counter = freq.get(rgb);
                if (counter == null) {
                    counter = new ColorCounter();
                    counter.rgb = rgb;
                    freq.put(rgb, counter);
                }
                counter.count++;
            }
        }

        // sort colors by most frequently used
        final ColorCounter[] counters = new ColorCounter[freq.size()];
        freq.values().toArray(counters);
        Arrays.sort(counters);

        // pick the most frequently used 256 (or fewer), and make a palette
        ImageData mask = null;
        if ((data.transparentPixel != -1) || (data.maskData != null)) {
            mask = data.getTransparencyMask();
        }
        final int n = Math.min(256, freq.size());
        final RGB[] rgbs = new RGB[n + ((mask != null) ? 1 : 0)];
        for (int i = 0; i < n; ++i) {
            rgbs[i] = counters[i].rgb;
        }
        if (mask != null) {
            rgbs[rgbs.length - 1] = (data.transparentPixel != -1) ? data.palette
                    .getRGB(data.transparentPixel) : new RGB(255, 255, 255);
        }
        final PaletteData palette = new PaletteData(rgbs);

        // create a new image using the new palette:
        // for each pixel in the old image, look up the best matching
        // index in the new palette
        final ImageData newData = new ImageData(width, data.height, 8, palette);
        if (mask != null) {
            newData.transparentPixel = rgbs.length - 1;
        }
        for (int y = 0, height = data.height; y < height; ++y) {
            data.getPixels(0, y, width, pixels, 0);
            if (mask != null) {
                mask.getPixels(0, y, width, maskPixels, 0);
            }
            for (int x = 0; x < width; ++x) {
                if ((mask != null) && (maskPixels[x] == 0)) {
                    pixels[x] = rgbs.length - 1;
                }
                else {
                    final RGB rgb = data.palette.getRGB(pixels[x]);
                    pixels[x] = closest(rgbs, n, rgb);
                }
            }
            newData.setPixels(0, y, width, pixels, 0);
        }
        return newData;
    }

    private static final void initFormats() {
        if (!formatInitDone) {
            final ImageFormat jpegImageFormat = new ImageFormat("jpeg", SWT.IMAGE_JPEG, false);
            formatRegistry.put("jpg", jpegImageFormat);
            formatRegistry.put("jpeg", jpegImageFormat);
            formatRegistry.put("gif", new ImageFormat("gif", SWT.IMAGE_GIF, true));
            formatRegistry.put("png", new ImageFormat("png", SWT.IMAGE_PNG, false));
            formatRegistry.put("bmp", new ImageFormat("bmp", SWT.IMAGE_BMP, false));
            supportedExtensions = formatRegistry.keySet().toArray(
                    new String[formatRegistry.keySet().size()]);
            formatInitDone = true;
        }
    }

    public ImageSaver() {
        initFormats();
    }

    public final String[] getSupportedExtensions() {
        return supportedExtensions;
    }

    public final void saveImage(final Image image, final String fileName)
            throws IOException, IllegalArgumentException {
        final int lastDotPosition = fileName.lastIndexOf('.');
        final String extension = fileName.substring(lastDotPosition + 1).toLowerCase();

        if (!formatRegistry.containsKey(extension)) {
            final String message = "Unknow extension " + extension;
            log.error(message);
            throw new IllegalArgumentException(message);
        }

        if (log.isDebugEnabled()) {
            log.debug("Saving image to " + fileName + " as " + extension);
        }

        final ImageFormat format = formatRegistry.get(extension);

        FileOutputStream result = null;
        try {
            /*
             * BufferedImage bi = new
             * BufferedImage(imageData.width,imageData.height,BufferedImage.TYPE_INT_RGB);
             * bi.setData(new ImageDataRaster(imageData));
             * ImageIO.write(bi,"png",new File("/tmp/image.png"));
             */

            result = new FileOutputStream(fileName);
            ImageData imageData = image.getImageData();
            if (format.needDownsampling && (imageData.depth > 8)) {
                if (log.isDebugEnabled()) {
                    log.debug("Downsampling image");
                }
                imageData = downSample(image);
            }

            final ImageLoader imageLoader = new ImageLoader();
            imageLoader.data = new ImageData[]{imageData};
            imageLoader.save(result, format.swtId);
        } catch (final FileNotFoundException e) {
            throw e;
        } finally {
            if (result != null) {
                try {
                    result.close();
                } catch (final IOException e) {
                    log.warn("Got exception saving image", e);
                }
            }
        }
    }

}
