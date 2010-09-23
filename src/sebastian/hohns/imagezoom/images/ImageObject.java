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
 * A abstract image object. Implements Runnable to support the thread executor.
 * @author Sebastian Hohns
 */
public abstract class ImageObject implements Runnable{
    protected JImagePyramideProcessor imageProcessor;
    protected OriginalImage props;
    protected int width;
    protected int height;
    protected String path;
    protected int level;
    protected boolean done;

    /**
     * Height of the image.
     * @return height.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Set image height.
     * @param height
     */
    public void setHeight(int height) {
        this.height = height;
    }


    /**
     * Width of the image.
     * @return width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Set image width.
     * @param width
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * Returns the image path.
     * @return path
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets the image path.
     * @param path
     */
    public void setPath(String path) {
        this.path = path;
    }


    /**
     * Return image processor main class.
     * @return imageProcessor
     */
    public JImagePyramideProcessor getImageProcessor() {
        return imageProcessor;
    }

    /**
     * Return the image level.
     * @return level.
     */
    public int getLevel() {
        return level;
    }

    /**
     * Set the image level.
     * @param level
     */
    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * Return the status of the image operation.
     * @return true if finished, else false
     */
    public synchronized boolean isDone() {
        return done;
    }

    
}
