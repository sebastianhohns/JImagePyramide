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

package sebastian.hohns.imagezoom.converter;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import sebastian.hohns.imagezoom.exceptions.UnsupportedImageLibaryException;
import sebastian.hohns.imagezoom.imageformats.ImageFormat;
import sebastian.hohns.imagezoom.imageformats.ZoomifyFormat;
import sebastian.hohns.imagezoom.imageops.GraphicsmagickOperations;
import sebastian.hohns.imagezoom.imageops.ImageOperations;
import sebastian.hohns.imagezoom.imageops.ImagemagickOperations;
import sebastian.hohns.imagezoom.images.ImageLevel;
import sebastian.hohns.imagezoom.images.ImageRow;
import sebastian.hohns.imagezoom.images.ImageTile;
import sebastian.hohns.imagezoom.images.OriginalImage;

/**
 * Transforms a given image (or a list of images) to the given image pyramide format using a given image
 * manipulation lib.
 * @author Sebastian Hohns
 */
public class JImagePyramideProcessor {

    private ImageOperations io;
    private ImageFormat format;
    private ExecutorService service;

    /**
     * Set properties.
     * @param imgLib determines which image lib should be used to do the work (im4java-im = Imagemagick, im4java-gm = Graphicsmagick).
     * @param targetFormat target file format, currently only the zoomify format is supported.
     * @param usableThreads number of threads to use to do the calculations.
     * @param tmpImageFormat image format for temporary images. Choose a format (pnm, tif) with compression to speed up calculation for the cost of higher disk usage.
     * @throws UnsupportedImageLibaryException
     */
    public JImagePyramideProcessor(String imgLib, String targetFormat, int usableThreads, String tmpImageFormat, boolean asyncCalculation) throws UnsupportedImageLibaryException {
        format = new ZoomifyFormat();

        if (imgLib.equals("im4java-im")) {
            io = new ImagemagickOperations(tmpImageFormat, asyncCalculation);
        } else if (imgLib.equals("im4java-gm")) {
            io = new GraphicsmagickOperations(tmpImageFormat, asyncCalculation);
        } else {
            throw new UnsupportedImageLibaryException(imgLib + " is not a supported image libary.");
        }

        //Default threadpool size to the number of processors.
        if (usableThreads == -1) {
            usableThreads = Runtime.getRuntime().availableProcessors();
        }
        service = Executors.newFixedThreadPool(usableThreads);
    }

    /**
     * Transform one single image and store the result at targetPath.
     * @param orgImage path to the original image
     * @param targetPath target directory
     * @return true if successful, else false
     */
    public boolean process(String orgImage, String targetPath) {
        File org = new File(orgImage);
        if (org.exists() && org.canRead() && new File(targetPath).canWrite()) {
            OriginalImage p = new OriginalImage(org, format, io, targetPath + "/" + FilenameUtils.getBaseName(org.getName()));

            Future f = service.submit(p);
            buildZoomlevel(p, f);

            try {
                service.shutdown();
                service.awaitTermination(60, TimeUnit.MINUTES);
                format.generateXMLFile(p.getTargetDir(), p.getImageWidth(), p.getImageHeight(), p.getTileCount());
                cleanup(p.getTempPath());
                return true;
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Transform a list of images and store them at targetPath.
     * @param images list of paths to the original images
     * @param targetPath target directory
     * @return true if successful, else false
     */
    public boolean process(List<String> images, String targetPath) {
        List<OriginalImage> props = new ArrayList<OriginalImage>();
        File org;
        for (String orgImage : images) {
            org = new File(orgImage);
            if (org.exists()) {
                OriginalImage p = new OriginalImage(org, format, io, targetPath + "/" + FilenameUtils.getBaseName(org.getName()));
                Future f = service.submit(p);
                buildZoomlevel(p, f);
                props.add(p);
            }
        }

        try {
            service.shutdown();
            service.awaitTermination(60, TimeUnit.MINUTES);
            for (OriginalImage p : props) {
                format.generateXMLFile(p.getTargetDir(), p.getImageWidth(), p.getImageHeight(), p.getTileCount());
                cleanup(p.getTempPath());
            }
            return true;
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        return false;
    }

    /**
     * Transforms a list of images and store them at targetPath.
     * @param imageZip ZipFile with images
     * @param targetPath target directory
     * @return true if successful, else false
     */
    public boolean process(ZipFile imageZip, String targetPath) {

        List<String> images = new ArrayList<String>(imageZip.size());
        //Unzip file to tmp dir
        String tmpPath = System.getProperty("java.io.tmpdir") + "/" + FilenameUtils.getBaseName(imageZip.getName());
        if (!new File(tmpPath).exists()) {
            new File(tmpPath).mkdirs();
        }

        Enumeration<ZipEntry> pics = (Enumeration<ZipEntry>) imageZip.entries();
        ZipEntry entry;
        try {
            while (pics.hasMoreElements()) {
                entry = pics.nextElement();

                //Ignore directory and extract all files to tmpdir
                if (!entry.isDirectory()) {
                    copyInputStream(imageZip.getInputStream(entry), new BufferedOutputStream(new FileOutputStream(FilenameUtils.concat(tmpPath, entry.getName()))));
                    images.add(FilenameUtils.concat(tmpPath, FilenameUtils.getBaseName(entry.getName())));
                }
            }

            //Call List<String> process
            boolean result = process(images, targetPath);

            //Cleanup extracted files
            cleanup(tmpPath);
            return result;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return false;
        }
    }

    /**
     * Build a level of the image pyramide.
     * @param p object representing the original image
     * @param orgPrepared determines if original image is prepared
     */
    public void buildZoomlevel(OriginalImage p, Future orgPrepared) {
        //Check and wait till the image is prepared
        if (orgPrepared != null) {
            try {
                orgPrepared.get();
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            } catch (ExecutionException ee) {
                ee.printStackTrace();
            }
        }

        ImageLevel level;

        //Create all required image levels from the top
        for (int i = p.getRequiredlevels(); i >= 1; i--) {
            level = new ImageLevel(this, p, (p.getRequiredlevels() - i), format.scaleDimension(p.getImageWidth(), i), format.scaleDimension(p.getImageHeight(), i), p.getImagePath());
            Future f = service.submit(level);

            //Build Rows for all but the last image level
            if (i != p.getRequiredlevels()) {
                buildRows(p, level, f);
            }
        }

        //Build rows for the original image
        buildRows(p, new ImageLevel(this, p, (p.getRequiredlevels()), format.scaleDimension(p.getImageWidth(), 0), format.scaleDimension(p.getImageHeight(), 0), p.getImagePath()), null);
    }

    /**
     * Cut rows form a level of the pyramide.
     * @param p original image
     * @param level level of the image pyramide.
     * @param levelDone determines if the level is fully calculated.
     */
    public void buildRows(OriginalImage p, ImageLevel level, Future levelDone) {
        //Check and wait till the ImageLevel is ready
        if (levelDone != null) {
            try {
                levelDone.get();
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            } catch (ExecutionException ee) {
                ee.printStackTrace();
            }
        }

        //Build the required rows
        ImageRow row;
        for (int i = 0; i <= level.getRequiredRows(); i++) {
            row = new ImageRow(this, p, level, level.getWidth(), format.getTileHeight(), i);
            Future f = service.submit(row);
            buildTiles(p, row, f);
        }
    }

    /**
     * Cut tiles from a row of a level.
     * @param p original image
     * @param row row of a level of the image pyramide.
     * @param rowDone determines if the row is fully calculated.
     */
    public void buildTiles(OriginalImage p, ImageRow row, Future rowDone) {
        //Check and wait till the ImageRow is ready
        if (rowDone != null) {
            try {
                rowDone.get();
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            } catch (ExecutionException ee) {
                ee.printStackTrace();
            }
        }

        //Cut tiles from row.
        for (int i = 0; i <= row.getRequiredCols(); i++) {
            service.submit(new ImageTile(this, row, p.getTargetDir() + "/" + format.getTileGroup(p.getTargetDir(), p.incrementTileCounter()), i));
        }
    }

    /**
     * Delete all temporary files inside tmpdir.
     * @param path path to temporary files
     */
    public void cleanup(String path) {
        try {
            FileUtils.deleteDirectory(new File(path));
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * Copy tiles to the target directory.
     * @param p original image
     * @param source source file
     * @param destFilename destination path to copy
     */
    public void moveToTargetDir(OriginalImage p, File source, String destFilename) {
        try {
            File targetFile = new File(p.getTargetDir() + "/" + format.getTileGroup(p.getTargetDir(), p.incrementTileCounter()) + destFilename);
            if (targetFile.exists()) {
                targetFile.delete();
            }

            //apache commons-io FileUtils
            FileUtils.moveFile(source, targetFile);

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public ImageOperations getImageOperations() {
        return io;
    }

    public ImageFormat getImageFormat() {
        return format;
    }

    private static void copyInputStream(InputStream input, OutputStream out) throws IOException {
        byte[] buffer = new byte[4096];
        int len;
               
        while ((len = input.read(buffer)) >= 0) {
            out.write(buffer, 0, len);
        }

        input.close();
        out.close();
    }
}







