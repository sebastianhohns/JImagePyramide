/*
 * Copyright 2009,2010 Sebastian Hohns.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sebastian.hohns.imagezoom.images;

import sebastian.hohns.imagezoom.imageops.ImageOperations;
import sebastian.hohns.imagezoom.exceptions.ImageSizeNotFoundException;
import sebastian.hohns.imagezoom.imageformats.ImageFormat;
import java.io.File;
import org.apache.commons.io.FilenameUtils;

/**
 * Represents the orginal image.
 * @author Sebastian Hohns
 */
public class OriginalImage extends ImageObject {

    private ImageFormat format;
    private File image;
    private int imageWidth;
    private int imageHeight;
    private int requiredlevels;
    private int tileGroupCounter;
    private ImageOperations io;
    private String targetDir;

    public OriginalImage(File image, ImageFormat format, ImageOperations io, String targetDir) {
        this.image = image;
        this.io = io;
        this.targetDir = targetDir;

        new File(getTempPath()).mkdirs();
        new File(targetDir).mkdirs();

        this.format = format;
        tileGroupCounter = 0;

        try {
            String size[] = io.determineImageDimension(image.getAbsolutePath()).split("x");

            if (size.length == 2) {
                imageWidth = Integer.parseInt(size[0]);
                imageHeight = Integer.parseInt(size[1]);
                requiredlevels = determineRequiredLevels();
            } else {
                throw new ImageSizeNotFoundException("Image size couldn't be determined!");
            }
        } catch (ImageSizeNotFoundException isnfe) {
            isnfe.printStackTrace();
        }
    }

    /**
     * Prepare the original image. For example transform it to a compatible image format.
     */
    public void run() {
        image = io.prepareOriginalImage(getImagePath(), getTempPath(), getImageName());
        done = true;
    }

    /**
     * Calculates the required level of the pyramide.
     * @return number of levels
     */
    public int determineRequiredLevels() {
        return (int) Math.ceil(Math.log((double) Math.max(imageHeight, imageWidth) / (format.getTileWidth() * 1.0)) / Math.log(2.0));
    }

    /**
     * Calculates the required rows of a level for the given image format.
     * @return number of rows
     */
    public int getRequiredRows() {
        return (int) Math.ceil(imageHeight / format.getTileHeight());
    }

    /**
     * Calculates the required columns of a row for the given image format.
     * @return number of rows
     */
    public int getRequiredCols() {
        return (int) Math.ceil(imageWidth / format.getTileWidth());
    }

    /**
     * Return imageHeight.
     * @return imageHeight
     */
    public int getImageHeight() {
        return imageHeight;
    }

    /**
     * Return imageWidth.
     * @return imageWidth
     */
    public int getImageWidth() {
        return imageWidth;
    }

    /**
     * Return number of required levels.
     * @return requiredlevels
     */
    public int getRequiredlevels() {
        return requiredlevels;
    }

    /**
     * Return the current tileCount
     * @return tileGroupCounter
     */
    public synchronized int getTileCount() {
        return tileGroupCounter;
    }

    /**
     * Returns the absolutepath of the original image.
     * @return absolute imagepath
     */
    public synchronized String getImagePath() {
        return image.getAbsolutePath();
    }

    /**
     * Returns the name of the image.
     * @return imagename
     */
    public synchronized String getImageName() {
        return image.getName();
    }

    /**
     * Returns the directory for temporary files.
     * @return path
     */
    public synchronized String getTempPath() {
        return System.getProperty("java.io.tmpdir") + "/" + FilenameUtils.getBaseName(getImageName()) + "/";
    }

    /**
     * Increment the tile counter (needed to determine the TileGroup.
     * @return tileGroupCounter
     */
    public synchronized int incrementTileCounter() {
        return tileGroupCounter++;
    }

    /**
     * Returns the target directory.
     * @return targetDir
     */
    public synchronized String getTargetDir() {
        return targetDir;
    }
}
