package com.fourthon.backend.ai.domain;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class VectorCodec {

    private VectorCodec() {
    }

    public static byte[] toBytes(float[] values) {
        ByteBuffer buffer = ByteBuffer.allocate(values.length * Float.BYTES).order(ByteOrder.LITTLE_ENDIAN);
        buffer.asFloatBuffer().put(values);
        return buffer.array();
    }

    public static float[] fromBytes(byte[] bytes) {
        float[] values = new float[bytes.length / Float.BYTES];
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer().get(values);
        return values;
    }

    public static double dot(float[] left, float[] right) {
        int length = Math.min(left.length, right.length);
        double sum = 0.0;
        for (int i = 0; i < length; i++) {
            sum += left[i] * right[i];
        }
        return sum;
    }
}
