package com.jackpocket.scratchoff.tools;

import ohos.media.image.PixelMap;
import ohos.media.image.common.Rect;

import java.util.ArrayList;
import java.util.List;

public class ThresholdCalculator {

    private final int unscratchedColor;

    public ThresholdCalculator(int unscratchedColor) {
        this.unscratchedColor = unscratchedColor;
    }

    public float calculate(PixelMap bitmap, List<Rect> regions) {
        float matchesSum = 0F;

        for (Rect region : regions) {
            matchesSum += calculate(
                    countNotMatching(bitmap, region),
                    region.width,
                    region.height);
        }

        return matchesSum / regions.size();
    }

    public float calculate(int scratchedCount, int width, int height) {
        return Math.min(1, Math.max(0, ((float) scratchedCount) / (width * height)));
    }

    public int countNotMatching(PixelMap bitmap, Rect region) {
        int pixelCount = region.width * region.height;
        int[] pixels = new int[pixelCount];

        bitmap.readPixels(
                pixels,
                0,
                region.height,
                region);

        return pixelCount - countMatching(pixels);
    }

    int countMatching(int[] pixels) {
        int scratched = 0;

        for (int pixel : pixels) {
            if (pixel == unscratchedColor)
                scratched++;
        }

        return scratched;
    }

    public static List<Rect> createFullSizeThresholdRegion(PixelMap source) {
        ArrayList<Rect> regions = new ArrayList<Rect>();
        regions.add(new Rect(0, 0, source.getImageInfo().size.width,source.getImageInfo().size.height));

        return regions;
    }
}
