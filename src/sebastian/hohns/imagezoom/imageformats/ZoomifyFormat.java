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

package sebastian.hohns.imagezoom.imageformats;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * Format properties for the zoomify format. This format is support in all OpenZoom and, of
 * course, Zoomify players.
 * see: http://www.zoomify.com/
 * @author Sebastian Hohns
 */
public class ZoomifyFormat implements ImageFormat {

    private static final int MAX_FILES_IN_TILEGROUP = 256;
    private static final int TILE_WIDTH = 256;
    private static final int TILE_HEIGHT = 256;
    private static final int SCALE_FACTOR = 2;

    public int getTileWidth() {
        return TILE_WIDTH;
    }

    public int getTileHeight() {
        return TILE_HEIGHT;
    }

    public int getScaleFactor() {
        return SCALE_FACTOR;
    }

    /**
     * Calculates a dimension (x or y) for a given level.
     * @param dim a width or heigt
     * @param level level of the pyramide
     * @return new dimension length
     */
    public int scaleDimension(int dim, int level) {
        return (int) (dim / scaleFactor(level));
    }

    /**
     * Calculates the scale factor for a level of the image pyramide.
     * @param level
     * @return scale factor
     */
    public int scaleFactor(int level) {
        return (int) Math.pow(SCALE_FACTOR, level);
    }

    /**
     * Generates a filename based on level, col and row of the tile.
     * @param level level in the image pyramide
     * @param col column
     * @param row row
     * @return filename for a tile
     */
    public synchronized String generateFilename(int level, int col, int row) {
        return level + "-" + row + "-" + col + ".jpg";
    }

    /**
     * Calculates in which tilegroup a tile belongs. Creates the directory if required.
     * @param basePath path to target directory
     * @param counter number already available tiles
     * @return tilegroup
     */
    public synchronized String getTileGroup(String basePath, int counter) {
        //Create a new TileGroup if MAX_FILES_IN_TILEGROUP is reached
        if (counter % MAX_FILES_IN_TILEGROUP == 0) {
            new File(basePath + File.separator+ "TileGroup" + (int) Math.floor(counter / MAX_FILES_IN_TILEGROUP)).mkdirs();
        }
        return "TileGroup" + (int) Math.floor(counter / MAX_FILES_IN_TILEGROUP) + File.separator;
    }

    /**
     * Generates a xml file specified by the image format.
     * @param targetDir target directory
     * @param width width of the original image
     * @param height height of the original image
     * @param tileCount number of tiles
     */
    public void generateXMLFile(String targetDir, int width, int height, int tileCount) {
        Writer writer =null;
        try {
            writer = new FileWriter(targetDir + File.separator +"ImageProperties.xml");
            writer.write("<IMAGE_PROPERTIES WIDTH=\"" + width + "\" HEIGHT=\"" + height + "\" NUMTILES=\"" + tileCount + "\" NUMIMAGES=\"1\" VERSION=\"1.8\" TILESIZE=\"" + MAX_FILES_IN_TILEGROUP + "\" />");
        } catch (IOException io) {
            io.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Determines the image format used for the final tiles.
     * @return tile image format
     */
    public String getTileFileExtension() {
        return ".jpg";
    }
}
