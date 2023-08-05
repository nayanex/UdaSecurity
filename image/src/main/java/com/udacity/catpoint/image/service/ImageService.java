package com.udacity.catpoint.image.service;

import java.awt.image.BufferedImage;

public interface ImageService {
    /**
     * Returns true if the provided image contains a cat.
     * @param image Image to scan
     * @param confidenceThreshold Minimum threshold to consider for a cat. For example, 90.0f would require 90% confidence minimum.
     * @return true if the image contains a cat, false otherwise.
     */
    boolean imageContainsCat(BufferedImage image, float confidenceThreshold);
}
