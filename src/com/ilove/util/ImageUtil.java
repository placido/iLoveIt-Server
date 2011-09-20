package com.ilove.util;

/*
 * Copyright 2010-2011 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;


public class ImageUtil {

    private static Logger logger = Logger.getLogger(ImageUtil.class.getName());

    private ImageUtil () {}

    public static byte [] cropPhoto (byte [] photoData) throws IOException {

        ByteArrayInputStream dataStream = new ByteArrayInputStream(photoData);
        ImageInputStream iis = ImageIO.createImageInputStream(dataStream);
        Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);

        // Determine image format
        final String formatName;
        if (readers.hasNext()) {
            ImageReader reader = readers.next();
            formatName = reader.getFormatName();
        }
        else {
            logger.log(Level.SEVERE,"Unsupported image type");
            return null;
        }

        BufferedImage image = ImageIO.read(iis);

        int baseWidth = image.getWidth();
        int baseHeight = image.getHeight();

        int x0, y0, w, h;
        if (baseHeight < baseWidth) {
        	x0 = (int)Math.floor((baseWidth - baseHeight)/2); y0 = 0; 
        	w = baseHeight; h = baseHeight;
        } else {
    		x0 = 0; y0 = (int)Math.floor((baseHeight - baseWidth)/2); 
        	w = baseWidth; h = baseWidth;
        }
        BufferedImage cropped = image.getSubimage(x0, y0, w, h);
        ByteArrayOutputStream imageOut = new ByteArrayOutputStream();
        ImageIO.write(cropped, formatName, imageOut);
        return (imageOut.toByteArray());       
    }

    /**
     * This method will scale the photo data to the specified size.
     * It will only decrease the size of the photo, not
     * increase it.
     *
     * @param longEdgeSize
     * @param photoData the raw binary photo data
     * @return the resized photo data
     * @throws IOException
     */
    public static byte [] scalePhoto (int longEdgeSize, byte [] photoData) throws IOException {

        ByteArrayInputStream dataStream = new ByteArrayInputStream(photoData);
        ImageInputStream iis = ImageIO.createImageInputStream(dataStream);
        Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);

        // Determine image format
        final String formatName;
        if (readers.hasNext()) {
            ImageReader reader = readers.next();
            formatName = reader.getFormatName();
        }
        else {
            logger.log(Level.SEVERE,"Unsupported image type");
            return null;
        }

        BufferedImage image = ImageIO.read(iis);

        int baseWidth = image.getWidth();
        int baseHeight = image.getHeight();

        float aspectRatio = (float)baseHeight/(float)baseWidth;

        final int modWidth;
        final int modHeight;

        if (baseWidth>baseHeight) {
            // width is long edge so scale based on that
            modWidth = longEdgeSize;
            modHeight = Math.round(aspectRatio*longEdgeSize);
        }
        else {
            modHeight = longEdgeSize;
            modWidth = Math.round(aspectRatio*longEdgeSize);
        }

        return scalePhoto(modWidth,modHeight, image, formatName);
    }


    /**
     * This method will scale the photo data passed in through the constructor
     * to the specified size.  It will only decrease the size of the photo, not
     * increase it.
     *
     * @param targetWidth width to scale to
     * @param targetHeight height to scale to
     * @param image the BufferedImage that contains the raw image data
     * @param formatName the name of the format for the image (typically "jpeg")
     * @return the resized photo data
     * @throws IOException
     */
    private static byte [] scalePhoto (int targetWidth, int targetHeight, BufferedImage image, String formatName) throws IOException {

        ByteArrayOutputStream imageOut = new ByteArrayOutputStream();
        if (targetWidth>image.getWidth() || targetHeight>image.getHeight()) {
            // we don't want to scale up.  If it's smaller just leave it alone
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, formatName,out);
            return out.toByteArray();
        }
        else {
            BufferedImage scaledImage = scalePhoto(image,targetWidth,targetHeight);
            ImageIO.write(scaledImage,formatName,imageOut);
            return imageOut.toByteArray();
        }
    }

    /**
     * private method that does the heavy lifting for scaling photos.
     * It doesn't make sense to call directly as we need to specify the
     * jpeg encoding details, etc, before we render the new image.
     *
     * @param img the BufferedImage object containing the photo to be scaled
     * @param targetWidth width to scale to
     * @param targetHeight height to scale to
     * @return the BufferedImage object that contains the resized photo data
     */
    private static BufferedImage scalePhoto (BufferedImage img, int targetWidth,
            int targetHeight) {
        int type = (img.getTransparency() == Transparency.OPAQUE) ? BufferedImage.TYPE_INT_RGB
                : BufferedImage.TYPE_INT_ARGB;
        BufferedImage ret = (BufferedImage) img;

        int width = img.getWidth();
        int height = img.getHeight();

        /**
         * A quirk of the image scaling algorithm is that if you scale from a very large
         * image to a small image in one step, the quality of the scaling degrades quickly
         * leading to a noisy photo.  This process instead scales a photo down in smaller
         * increments in a loop until the right size is achieved.
         */
        while (width != targetWidth || height != targetHeight) {
            if (width > targetWidth) {
                width = width/2;
                if (width < targetWidth) {
                    width = targetWidth;
                }
            }

            if ( height > targetHeight) {
                height = height/2;
                if (height< targetHeight) {
                    height = targetHeight;
                }
            }

            BufferedImage tmp = new BufferedImage(width, height, type);
            Graphics2D graphics = tmp.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.drawImage(ret, 0, 0, width, height, null);
            graphics.dispose();

            ret = tmp;
        }

        return ret;
    }

}
