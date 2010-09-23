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

import sebastian.hohns.imagezoom.converter.JImagePyramideProcessor;

/**
 * Represents a single row of a given ImageLevel.
 * @author Sebastian Hohns
 */
public class ImageRow extends ImageObject {

    private ImageLevel imgLevel;
    private int row;

    public ImageRow(JImagePyramideProcessor processor, OriginalImage props, ImageLevel imgLevel, int width, int height, int row) {
        this.imageProcessor = processor;
        this.props = props;
        this.imgLevel = imgLevel;
        this.width = width;
        this.height = height;
        this.row = row;
        this.level = imgLevel.getLevel();
    }

    public void run() {
        path = props.getTempPath() + "row-" + imgLevel.getLevel() + "-" + row + imageProcessor.getImageOperations().tmpFileExtension();
        done = imageProcessor.getImageOperations().cutRow(imgLevel.getPath(), path, imgLevel.getHeight(), row * height, width, imageProcessor.getImageFormat().getTileHeight());
    }

    /**
     * Calculates the number of columns required for this row. Tile width depends on the
     * choosen image format (in most cases 256px).
     * @return number of required columns.
     */
    public int getRequiredCols() {
        return (int) Math.ceil(width / imageProcessor.getImageFormat().getTileWidth());
    }

    /**
     * Return the row index
     * @return row
     */
    public int getRow() {
        return row;
    }
}
