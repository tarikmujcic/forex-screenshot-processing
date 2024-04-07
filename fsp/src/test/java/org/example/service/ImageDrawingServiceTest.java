package org.example.service;

import junit.framework.TestCase;

public class ImageDrawingServiceTest extends TestCase {

    public void testUpdateUnprocessedDirectoryWith9to10Lines() {
        ImageDrawingService.UNPROCESSED_DIRECTORY_PATH = "D:\\Desktop\\ImagesBefore";
        ImageDrawingService.updateUnprocessedDirectoryWith9to10Lines();
    }
}