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

package sebastian.hohns.imagezoom.imageops;

import java.io.File;
import java.io.IOException;
import org.im4java.core.GMOperation;
import org.im4java.core.GraphicsMagickCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.Info;

/**
 * Implementation for Graphicsmagick. Make sure your imagemagick installation is accessible.
 * @author Sebastian Hohns
 */
public class GraphicsmagickOperations implements ImageOperations {

    private final GraphicsMagickCmd convert = new GraphicsMagickCmd("convert");
    private String tmpImageFormat;
    private boolean async;

    public GraphicsmagickOperations(String tmpImageFormat, boolean async) {
        this.tmpImageFormat = tmpImageFormat;
        this.async = async;
    }

    /**
     * Scales the image down to a new size.
     * @param path path to original image
     * @param target target path
     * @param newWidth
     * @param newHeight
     * @param scaleFactor
     * @return true if successful, false else
     */
    public boolean scaleImage(String path, String target, int newWidth, int newHeight, int scaleFactor) {
        GMOperation op = new GMOperation();
        op.addImage(path);
        op.resize(newWidth, newHeight);
        op.addImage(target);

        try {
            convert.setAsyncMode(async);
            convert.run(op);
            return true;
        } catch (IOException io) {
            io.printStackTrace();
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        } catch (IM4JavaException im4) {
            im4.printStackTrace();
        }
        return false;
    }

    /**
     * Cuts a row from a given image level.
     * @param path path to the image level
     * @param target target path
     * @param yOffset offset
     * @param maxHeight max hight of the row (see ImageFormat)
     * @param width with of the row
     * @param tileHeight heigh of the row
     * @return true if successful, false else
     */
    public boolean cutRow(String path, String target, int yOffset, int maxHeight, int width, int tileHeight) {
        if (yOffset + 256 > maxHeight) {
            tileHeight = yOffset - maxHeight;
        }

        GMOperation op = new GMOperation();
        op.addImage(path);
        op.crop(width, 256, 0, yOffset - tileHeight);
        op.addImage(target);

        try {
            convert.setAsyncMode(async);
            convert.run(op);
            return true;
        } catch (IOException io) {
            io.printStackTrace();
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        } catch (IM4JavaException im4) {
            im4.printStackTrace();
        }
        return false;
    }

    /**
     * Cuts a tile from a image row.
     * @param path path to the image row
     * @param target target path
     * @param maxWidth max width of the tile (see ImageFormat)
     * @param rowHeight heigh of the row
     * @param xOffset offset
     * @return true if successful, false else
     */
    public boolean cutTile(String path, String target, int maxWidth, int rowHeight, int xOffset) {
        GMOperation op = new GMOperation();
        op.addImage(path);
        op.crop(256, rowHeight, xOffset * 256, 0);
        op.addImage(target);
        //System.out.println("convert "+path+" -crop 256x"+rowHeight+"+"+(xOffset*256)+"+0 "+target);

        try {
            convert.setAsyncMode(async);
            convert.run(op);
            return true;
        } catch (IOException io) {
            io.printStackTrace();
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        } catch (IM4JavaException im4) {
            im4.printStackTrace();
        }
        return false;
    }

    /**
     * Prepares the original image according to the settings of the choosen image format. For example
     * to change the image format from tiff to jpeg.
     * @param path path to the original image
     * @param tmpPath path to the tmp folder
     * @param filename target filename
     * @return prepared file
     */
    public File prepareOriginalImage(String path, String tmpPath, String filename) {
        return new File(path);
    }

      /**
     * Returns the file extension used for temporary files like a image row. You get a
     * performance boost if you choose a uncompressed format for the cost of disk space.
     * @return file extension, for example ".jpg"
     */
    public String tmpFileExtension() {
        return tmpImageFormat;
    }

     /**
     * Returnn the dimensions of a image.
     * @param path path to the image
     * @return image dimension in format w x h.
     */
    public String determineImageDimension(String path) {
        try {
            Info info = new Info(path);
            String size = info.getProperty("Geometry");
            return size.substring(0, size.indexOf("+"));
        } catch (IM4JavaException im4) {
            im4.printStackTrace();
        }
        return null;
    }

    /**
     * Transforms a given image to a different file format.
     * @param path path to the image
     * @param targetExtension new target extension
     * @return true if successful else false
     */
    public boolean transformToTargetFormat(String path, String targetExtension) {
        String target = path.substring(0, path.lastIndexOf(".")) + targetExtension;
        GMOperation op = new GMOperation();
        op.addImage(path);
        op.addImage(target);
     
        try {
            convert.setAsyncMode(async);
            convert.run(op);
            return true;
        } catch (IOException io) {
            io.printStackTrace();
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        } catch (IM4JavaException im4) {
            im4.printStackTrace();
        }
        return false;
    }
}
