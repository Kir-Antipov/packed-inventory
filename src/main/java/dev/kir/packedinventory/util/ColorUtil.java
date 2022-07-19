package dev.kir.packedinventory.util;

import net.minecraft.util.math.MathHelper;

public final class ColorUtil {
    public static int fromRGB(float r, float g, float b) {
        return ColorUtil.fromRGBA(r, g, b, 1);
    }

    public static int fromRGBA(float r, float g, float b, float a) {
        return ((byte)(a * 255) << 24) + ((byte)(r * 255) << 16) + ((byte)(g * 255) << 8) + (byte)(b * 255);
    }

    public static float[] toRGB(int color) {
        float r = ((color >> 16) & 255) / 255F;
        float g = ((color >> 8) & 255) / 255F;
        float b = (color & 255) / 255F;

        return new float[] { r, g, b };
    }

    public static float[] toRGBA(int color) {
        float a = ((color >> 24) & 255) / 255F;
        float r = ((color >> 16) & 255) / 255F;
        float g = ((color >> 8) & 255) / 255F;
        float b = (color & 255) / 255F;

        return new float[] { r, g, b, a };
    }

    private static float normalizeRGBChannel(float channel) {
        return 100F * (channel > 0.04045F ? (float)Math.pow((channel + 0.055F) / 1.055F, 2.4F) : channel / 12.92F);
    }

    private static float normalizeXYZChannel(float channel) {
        return (channel > 0.008856F) ? (float)Math.pow(channel, 1 / 3F) : (7.787F * channel) + (16 / 116F);
    }

    public static float[] RGB2LAB(float[] rgb) {
        float r = normalizeRGBChannel(rgb[0]);
        float g = normalizeRGBChannel(rgb[1]);
        float b = normalizeRGBChannel(rgb[2]);

        float x = r * 0.4124F + g * 0.3576F + b * 0.1805F;
        float y = r * 0.2126F + g * 0.7152F + b * 0.0722F;
        float z = r * 0.0193F + g * 0.1192F + b * 0.9505F;

        x = normalizeXYZChannel(x / 95.0470F);
        y = normalizeXYZChannel(y / 100.0F);
        z = normalizeXYZChannel(z / 108.883F);

        float _l = (116 * y) - 16;
        float _a = 500 * (x - y);
        float _b = 200 * (y - z);
        return new float[] { _l, _a, _b };
    }

    public static float computeXYZDistance(float[] left, float[] right) {
        return MathHelper.sqrt(MathHelper.square(left[0] - right[0]) + MathHelper.square(left[1] - right[1]) + MathHelper.square(left[2] - right[2]));
    }

    private ColorUtil() { }
}
