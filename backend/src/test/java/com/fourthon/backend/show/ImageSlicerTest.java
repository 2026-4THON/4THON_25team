package com.fourthon.backend.show;

import com.fourthon.backend.ai.client.ImageInput;
import com.fourthon.backend.show.client.ImageSlicer;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ImageSlicerTest {

    @Test
    void 세로로_긴_이미지를_정해진_높이로_잘라_JPEG로_만든다() throws IOException {
        List<ImageInput> slices = ImageSlicer.slice(new BufferedImage(800, 3500, BufferedImage.TYPE_INT_ARGB), 1000, 1600, 16);

        assertThat(slices).hasSize(3);
        assertThat(slices).allMatch(slice -> slice.mimeType().equals("image/jpeg"));
        assertThat(read(slices.get(0))).satisfies(image -> {
            assertThat(image.getWidth()).isEqualTo(800);
            assertThat(image.getHeight()).isEqualTo(1600);
        });
        assertThat(read(slices.get(2)).getHeight()).isEqualTo(300);
    }

    @Test
    void 폭이_넓으면_비율을_유지해_줄인다() throws IOException {
        List<ImageInput> slices = ImageSlicer.slice(new BufferedImage(2000, 1000, BufferedImage.TYPE_INT_RGB), 1000, 1600, 16);

        assertThat(slices).hasSize(1);
        BufferedImage image = read(slices.get(0));
        assertThat(image.getWidth()).isEqualTo(1000);
        assertThat(image.getHeight()).isEqualTo(500);
    }

    @Test
    void 조각_수_한도를_넘기지_않는다() {
        assertThat(ImageSlicer.slice(new BufferedImage(750, 10000, BufferedImage.TYPE_INT_RGB), 1000, 1600, 4)).hasSize(4);
    }

    private BufferedImage read(ImageInput input) throws IOException {
        return ImageIO.read(new ByteArrayInputStream(input.data()));
    }
}
