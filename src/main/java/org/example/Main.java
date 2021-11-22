package org.example;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;

import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException, ImageWriteException, ImageReadException {

        new WriteExifMetadataExample().changeExifMetadata(new File("IMG_0996.jpeg"), new File("result_test.jpg"));
        System.out.println(WriteExifMetadataExample.getExifDate(new File("result_test.jpg")));
        MetadataExample.metadataExample(new File("IMG_0996.jpeg"));
    }
}
