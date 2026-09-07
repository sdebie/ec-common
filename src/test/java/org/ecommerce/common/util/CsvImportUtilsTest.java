package org.ecommerce.common.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CsvImportUtilsTest
{
    @Test
    void stripsLeadingSlashFromStorageRelativeImagePath()
    {
        assertEquals("02/K-MSK-FFP1-Dro.jpg", CsvImportUtils.normalizeStorageRelativePath("/02/K-MSK-FFP1-Dro.jpg"));
    }

    @Test
    void keepsCommaInsideASingleImageFilename()
    {
        assertEquals(
                java.util.List.of("02/DW-ARC9,6-SS-S-SKB.jpg"),
                CsvImportUtils.splitImageNames("/02/DW-ARC9,6-SS-S-SKB.jpg"));
    }

    @Test
    void stillSplitsSeparateImagePaths()
    {
        assertEquals(
                java.util.List.of("02/front.jpg", "02/back.jpg"),
                CsvImportUtils.splitImageNames("/02/front.jpg,/02/back.jpg"));
    }

    @Test
    void splitsBareFilenamesWithoutDirectories()
    {
        assertEquals(
                java.util.List.of("front.jpg", "back.jpg"),
                CsvImportUtils.splitImageNames("front.jpg,back.jpg"));
    }
}
