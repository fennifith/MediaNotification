package james.medianotification;

import android.graphics.Bitmap;
import android.support.v7.graphics.Palette;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PaletteUtils {

    public static Palette.Swatch getBestPaletteSwatchFrom(Bitmap bitmap) {
        return getBestPaletteSwatchFrom(Palette.from(bitmap).generate());
    }

    public static Palette.Swatch getBestPaletteSwatchFrom(Palette palette) {
        if (palette != null) {
            if (palette.getDominantSwatch() != null)
                return palette.getDominantSwatch();
            else if (palette.getVibrantSwatch() != null)
                return palette.getVibrantSwatch();
            else if (palette.getMutedSwatch() != null)
                return palette.getMutedSwatch();
            else if (palette.getDarkVibrantSwatch() != null)
                return palette.getDarkVibrantSwatch();
            else if (palette.getDarkMutedSwatch() != null)
                return palette.getDarkMutedSwatch();
            else if (palette.getLightVibrantSwatch() != null)
                return palette.getLightVibrantSwatch();
            else if (palette.getLightMutedSwatch() != null)
                return palette.getLightMutedSwatch();
            else if (!palette.getSwatches().isEmpty())
                return getBestPaletteSwatchFrom(palette.getSwatches());
        }
        return null;
    }

    private static Palette.Swatch getBestPaletteSwatchFrom(List<Palette.Swatch> swatches) {
        if (swatches == null) return null;
        return Collections.max(swatches, new Comparator<Palette.Swatch>() {
            @Override
            public int compare(Palette.Swatch opt1, Palette.Swatch opt2) {
                int a = opt1 == null ? 0 : opt1.getPopulation();
                int b = opt2 == null ? 0 : opt2.getPopulation();
                return a - b;
            }
        });
    }
}