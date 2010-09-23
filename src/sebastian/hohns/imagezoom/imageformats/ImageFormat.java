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

/**
 * Format properties. Represents a image format.
 * @author Sebastian Hohns
 */
public interface ImageFormat {

    /**
     * Width of a single tile.
     * @return tileWidth
     */
    public int getTileWidth();

    /**
     * Height of a single tile.
     * @return tileHeight
     */
    public int getTileHeight();

    /**
     * Returns the current scale factor.
     */
    public int getScaleFactor();

    /**
     * Calculates the scale factor for a level of the image pyramide.
     * @param level
     * @return scale factor
     */
    public int scaleDimension(int dim, int level);

    /**
     * Calculates the scale factor for a level of the image pyramide.
     * @param level
     * @return scale factor
     */
    public int scaleFactor(int level);

    /**
     * Generates a filename based on level, col and row of the tile.
     * @param level level in the image pyramide
     * @param col column
     * @param row row
     * @return filename for a tile
     */
    public String generateFilename(int level, int col, int row);

    /**
     * Calculates in which tilegroup a tile belongs.
     * @param basePath
     * @param counter
     * @return tilegroup
     */
    public String getTileGroup(String basePath, int counter);

    /**
     * Generates a xml file specified by the image format.
     * @param targetDir
     * @param width
     * @param height
     * @param tileCount
     */
    public void generateXMLFile(String targetDir, int width, int height, int tileCount);

    /**
     * Determines the image format used for the final tiles.
     * @return tile image format
     */
    public String getTileFileExtension();
}
