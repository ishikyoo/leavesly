package com.ishikyoo.iyoo.state.property;

import java.util.Objects;

public final class SubProperty<T> {
    private final String name;
    private final int offset;
    private final int bits;
    private final int mask;
    private final BitsmartProperty.Encoder<T> encoder;
    private final BitsmartProperty.Decoder<T> decoder;
    private final Class<T> type;

    SubProperty(String name, int offset, int bits,
                        BitsmartProperty.Encoder<T> encoder, BitsmartProperty.Decoder<T> decoder, Class<T> type) {
        this.name = Objects.requireNonNull(name, "SubProperty name cannot be null");
        this.offset = offset;
        this.bits = bits;
        this.encoder = Objects.requireNonNull(encoder, "Encoder cannot be null");
        this.decoder = Objects.requireNonNull(decoder, "Decoder cannot be null");
        this.type = Objects.requireNonNull(type, "Type cannot be null");
        this.mask = ((1 << bits) - 1) << offset;
    }

    public String name() {
        return name;
    }

    public int offset() {
        return offset;
    }

    public int bits() {
        return bits;
    }

    public int mask() {
        return mask;
    }

    public Class<T> type() {
        return type;
    }

    public boolean isBoolean() {
        return Boolean.class.equals(type);
    }

    public boolean isInteger() {
        return Integer.class.equals(type);
    }

    public boolean isEnum() {
        return type.isEnum();
    }

    public int encode(T value, int bits) {
        int cleared = bits & ~mask;
        int encoded = encoder.encode(value) << offset;
        return cleared | encoded;
    }

    public T decode(int bits) {
        return decoder.decode((bits & mask) >>> offset);
    }

    @Override
    public String toString() {
        return "SubProperty[" + name + ", offset=" + offset + ", bits=" + bits + "]";
    }
}
