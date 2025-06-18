package com.ishikyoo.iyoo.state.property;

import net.minecraft.state.property.IntProperty;
import net.minecraft.util.Identifier;

import java.util.*;

public final class BitsmartProperty {
    private final Identifier id;
    private final String name;
    private final int capacity;
    private final IntProperty property;
    private final Map<String, SubProperty<?>> subPropertyMap = new HashMap<>();
    private int nextBitIndex = 0;
    private boolean locked = false;

    BitsmartProperty(Identifier id, int capacity) {
        Objects.requireNonNull(id, "BitsmartProperty id cannot be null");
        if (capacity < 1 || capacity > 31) {
            throw new IllegalArgumentException("BitsmartProperty capacity must be between 1 and 31");
        }

        this.id = id;
        this.name = id.getNamespace() + "_" + id.getPath();
        this.capacity = capacity;
        this.property = IntProperty.of(this.name, 0, (1 << capacity) - 1);
    }

    public static BitsmartProperty of(Identifier id, int capacity) {
        Objects.requireNonNull(id, "Identifier cannot be null");
        return new BitsmartProperty(id, capacity);
    }

    public Identifier identifier() {
        return id;
    }

    public String name() {
        return name;
    }

    public int capacity() {
        return capacity;
    }

    public IntProperty property() {
        return property;
    }

    public int getReservedBits() {
        return nextBitIndex;
    }

    public int getAvailableBits() {
        return capacity - nextBitIndex;
    }

    public boolean isLocked() {
        return locked;
    }

    public void lock() {
        this.locked = true;
    }

    public boolean hasSubProperty(String subName) {
        return subPropertyMap.containsKey(normalize(subName));
    }

    @SuppressWarnings("unchecked")
    public <T> SubProperty<T> getSubProperty(String subName, Class<T> type) {
        SubProperty<?> prop = subPropertyMap.get(normalize(subName));
        if (prop == null) {
            throw new NoSuchElementException("No sub-property registered with name: " + subName);
        }
        if (!type.isAssignableFrom(prop.type())) {
            throw new ClassCastException("Sub-property '" + subName + "' is not of expected type " + type.getSimpleName());
        }
        return (SubProperty<T>) prop;
    }

    public Collection<SubProperty<?>> getSubProperties() {
        return Collections.unmodifiableCollection(subPropertyMap.values());
    }

    public SubProperty<Boolean> registerBool(String name) {
        return registerSubProperty(name, 1, val -> val ? 1 : 0, i -> i != 0, Boolean.class);
    }

    public SubProperty<Integer> registerInt(String name, int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("Invalid integer range: min > max");
        }

        int range = max - min;
        if (range < 1) {
            throw new IllegalArgumentException("Integer range must include at least two distinct values");
        }

        int bits = Integer.SIZE - Integer.numberOfLeadingZeros(range);
        return registerSubProperty(name, bits, i -> i - min, i -> i + min, Integer.class);
    }

    public <E extends Enum<E>> SubProperty<E> registerEnum(String name, Class<E> enumClass) {
        Objects.requireNonNull(enumClass, "Enum class cannot be null");
        E[] values = enumClass.getEnumConstants();

        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("Enum class must have at least one constant");
        }

        int bits = Integer.SIZE - Integer.numberOfLeadingZeros(values.length - 1);
        return registerSubProperty(name, bits, Enum::ordinal, i -> values[i], enumClass);
    }

    private <T> SubProperty<T> registerSubProperty(
            String subName, int bitCount,
            Encoder<T> encoder, Decoder<T> decoder, Class<T> type
    ) {
        if (locked) {
            throw new IllegalStateException("Cannot register sub-property after BitsmartProperty is sealed");
        }

        String key = normalize(subName);
        if (key.isEmpty()) {
            throw new IllegalArgumentException("Sub-property name cannot be null or empty");
        }

        if (bitCount < 1 || nextBitIndex + bitCount > capacity) {
            throw new IllegalStateException("Not enough bits to register sub-property '" + subName +
                    "'. Required: " + bitCount + ", Available: " + (capacity - nextBitIndex));
        }

        if (subPropertyMap.containsKey(key)) {
            throw new IllegalArgumentException("Sub-property '" + subName + "' already exists");
        }

        SubProperty<T> prop = new SubProperty<>(subName, nextBitIndex, bitCount, encoder, decoder, type);
        subPropertyMap.put(key, prop);
        nextBitIndex += bitCount;

        return prop;
    }

    private String normalize(String name) {
        return name.toLowerCase(Locale.ROOT).trim();
    }

    @Override
    public String toString() {
        return "BitsmartProperty[" + id + ", capacity=" + capacity + ", locked=" + locked +
                ", usedBits=" + nextBitIndex + ", subProperties=" + subPropertyMap.size() + "]";
    }

    @FunctionalInterface
    public interface Encoder<T> {
        int encode(T value);
    }

    @FunctionalInterface
    public interface Decoder<T> {
        T decode(int bits);
    }
}
