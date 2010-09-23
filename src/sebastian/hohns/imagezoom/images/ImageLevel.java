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

import java.io.File;
import sebastian.hohns.imagezoom.converter.JImagePyramideProcessor;

/**
 * Represents a zoom level of the image pyramide.
 * @author Sebastian Hohns
 */
public class ImageLevel extends ImageObject {
    
    public ImageLevel(JImagePyramideProcessor processor, OriginalImage props, int level, int width, int height) {
        this.imageProcessor = processor;
        this.width = width;
        this.height = height;
        this.props = props;
        this.level = level;
    }

    public ImageLevel(JImagePyramideProcessor processor, OriginalImage props, int level, int width, int height, String path) {
        this.imageProcessor = processor;
        this.width = width;
        this.height = height;
        this.props = props;
        this.level = level;
        this.path = path;
    }

    /**
     * Calls the converter thread. Note that the last zoom level needs to be copied to
     * the first tile group.
     */
    public void run() {
        if(level==0) {
           setPath(props.getTempPath() + "level-" + level + imageProcessor.getImageFormat().getTileFileExtension());
        } else {
            setPath(props.getTempPath() + "level-" + level + imageProcessor.getImageOperations().tmpFileExtension());
        }
     
        done = imageProcessor.getImageOperations().scaleImage(props.getImagePath(), path, width, height,  imageProcessor.getImageFormat().scaleFactor(level));
        //copy last level to tilegroup
        if (level == 0) {          
            imageProcessor.moveToTargetDir(props, new File(path), imageProcessor.getImageFormat().generateFilename(0, 0, 0));
        }
    }

    /**
     * Calculates the number of rows required for this level. Tile height depends on the
     * choosen image format (in most cases 256px).
     * @return number of required rows.
     */
    public int getRequiredRows() {
        return (int) Math.ceil(height / imageProcessor.getImageFormat().getTileHeight());
    }
}
