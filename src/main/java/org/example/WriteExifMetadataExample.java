package org.example;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.common.RationalNumber;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;
import org.apache.commons.imaging.formats.tiff.fieldtypes.FieldType;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfo;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputField;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class WriteExifMetadataExample {

    public static final int DATE_TIME_ORIGINAL = 0x9003;

    public void changeExifMetadata(final File jpegImageFile, final File dst)
            throws IOException, ImageReadException, ImageWriteException {

        try (FileOutputStream fos = new FileOutputStream(dst);
             OutputStream os = new BufferedOutputStream(fos)) {

            TiffOutputSet outputSet = null;

            // note that metadata might be null if no metadata is found.
            final ImageMetadata metadata = Imaging.getMetadata(jpegImageFile);
            final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
            if (null != jpegMetadata) {
                // note that exif might be null if no Exif metadata is found.
                final TiffImageMetadata exif = jpegMetadata.getExif();

                if (null != exif) {
                    // TiffImageMetadata class is immutable (read-only).
                    // TiffOutputSet class represents the Exif data to write.
                    //
                    // Usually, we want to update existing Exif metadata by
                    // changing
                    // the values of a few fields, or adding a field.
                    // In these cases, it is easiest to use getOutputSet() to
                    // start with a "copy" of the fields read from the image.
                    outputSet = exif.getOutputSet();
                }
            }

            // if file does not contain any exif metadata, we create an empty
            // set of exif metadata. Otherwise, we keep all of the other
            // existing tags.
            if (null == outputSet) {
                outputSet = new TiffOutputSet();
            }

            {
                // Example of how to add a field/tag to the output set.
                //
                // Note that you should first remove the field/tag if it already
                // exists in this directory, or you may end up with duplicate
                // tags. See above.
                //
                // Certain fields/tags are expected in certain Exif directories;
                // Others can occur in more than one directory (and often have a
                // different meaning in different directories).
                //
                // TagInfo constants often contain a description of what
                // directories are associated with a given tag.
                //
                final TiffOutputDirectory exifDirectory = outputSet.getOrCreateExifDirectory();
                // make sure to remove old value if present (this method will
                // not fail if the tag does not exist).

                SimpleDateFormat SDF = new SimpleDateFormat("yyyy:MM:dd kk:mm:ss");
                Calendar calendar = Calendar.getInstance();
                String updatedDateString = SDF.format(calendar.getTime());
                System.out.println(updatedDateString);

                FieldType originalFieldType = null;
                TagInfo originalTagInfo = null;
                TiffOutputField dateTimeFieldOriginal = exifDirectory.findField(DATE_TIME_ORIGINAL); // DateTimeOriginal
                if (dateTimeFieldOriginal != null) {
                    originalFieldType = dateTimeFieldOriginal.fieldType;
                    originalTagInfo = dateTimeFieldOriginal.tagInfo;
                }
                final TiffOutputField dateTimeOutputField = new TiffOutputField(originalTagInfo, originalFieldType, updatedDateString.length(), updatedDateString.getBytes());
                exifDirectory.removeField(ExifTagConstants.EXIF_TAG_APERTURE_VALUE);
                exifDirectory.removeField(306);
                exifDirectory.removeField(DATE_TIME_ORIGINAL);
                exifDirectory.add(dateTimeOutputField);

                exifDirectory.add(ExifTagConstants.EXIF_TAG_APERTURE_VALUE,
                        new RationalNumber(3, 10));
            }

            {
                // Example of how to add/update GPS info to output set.

                // New York City
                final double longitude = -74.0; // 74 degrees W (in Degrees East)
                final double latitude = 40 + 43 / 60.0; // 40 degrees N (in Degrees
                // North)

                outputSet.setGPSInDegrees(longitude, latitude);
            }

            // printTagValue(jpegMetadata, TiffConstants.TIFF_TAG_DATE_TIME);

            new ExifRewriter().updateExifMetadataLossless(jpegImageFile, os,
                    outputSet);
        }
    }


    private static void printTagValue(final JpegImageMetadata jpegMetadata,
                                      final TagInfo tagInfo) {
        final TiffField field = jpegMetadata.findEXIFValueWithExactMatch(tagInfo);
        if (field == null) {
            System.out.println(tagInfo.name + ": " + "Not Found.");
        } else {
            System.out.println(tagInfo.name + ": "
                    + field.getValueDescription());
        }
    }

    public static String getExifDate(final File imageFile) throws ImageReadException, IOException {
        final ImageMetadata metadata = Imaging.getMetadata(imageFile);
        TiffImageMetadata tiffImageMetadata;
        if (metadata instanceof JpegImageMetadata) {
            tiffImageMetadata = ((JpegImageMetadata) metadata).getExif();
        }
        else if (metadata instanceof TiffImageMetadata) {
            tiffImageMetadata = (TiffImageMetadata) metadata;
        }
        else {
            return null;
        }

        TiffField dateTime = tiffImageMetadata.findField(TiffTagConstants.TIFF_TAG_DATE_TIME);
        if (dateTime == null) {
            return null;
        }
        else {
            return dateTime.getStringValue();
        }
    }

}
