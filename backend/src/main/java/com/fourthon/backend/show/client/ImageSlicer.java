package com.fourthon.backend.show.client;

import com.fourthon.backend.ai.client.ImageInput;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

public final class ImageSlicer {

    private static final String JPEG = "image/jpeg";
    private static final String JPEG_FORMAT = "jpg";

    private ImageSlicer() {
    }

    public static List<ImageInput> slice(BufferedImage source, int maxWidth, int sliceHeight, int maxSlices) {
        double scale = Math.min(1.0, (double) maxWidth / source.getWidth());
        int width = Math.max(1, (int) Math.round(source.getWidth() * scale));
        int height = Math.max(1, (int) Math.round(source.getHeight() * scale));
        List<ImageInput> slices = new ArrayList<>();
        for (int top = 0; top < height && slices.size() < maxSlices; top += sliceHeight) {
            int currentHeight = Math.min(sliceHeight, height - top);
            BufferedImage slice = new BufferedImage(width, currentHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = slice.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, width, currentHeight);
            graphics.drawImage(
                    source,
                    0, 0, width, currentHeight,
                    0, (int) Math.round(top / scale), source.getWidth(), (int) Math.round((top + currentHeight) / scale),
                    null
            );
            graphics.dispose();
            slices.add(new ImageInput(JPEG, toJpeg(slice)));
        }
        return slices;
    }

    private static byte[] toJpeg(BufferedImage image) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(image, JPEG_FORMAT, output);
            return output.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
