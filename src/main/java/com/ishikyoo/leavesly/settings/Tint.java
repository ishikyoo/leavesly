package com.ishikyoo.leavesly.settings;

import com.google.gson.*;

import java.lang.reflect.Type;

public class Tint {
    private Tint() {

    }

    private ColorType type;
    private ColorBlend blend;
    private int value;

    public static Tint of(int value) {
        return of(ColorType.STATIC, ColorBlend.NONE, value);
    }

    public static Tint of(int value, double brightness) {
        return of(ColorType.STATIC, ColorBlend.NONE, value, brightness);
    }

    public static Tint of(ColorType mode) {
        return switch (mode) {
            case GRASS, FOLIAGE -> of(mode, 1);
            case STATIC -> null;
        };
    }

    public static Tint of(ColorType type, double brightness) {
        return switch (type) {
            case GRASS, FOLIAGE:
                if (brightness < 1)
                    yield of(type, ColorBlend.MULTIPLY, getBrightenedColor(0xffffff, brightness));
                else
                    yield of(type, ColorBlend.SCREEN, getBrightenedColor(0xffffff, brightness - 1));
            case STATIC:
                yield null;
        };
    }

    public static Tint of(ColorType type, ColorBlend blend, int value) {
        Tint tint = new Tint();
        tint.setColorType(type);
        tint.setColorBlend(blend);
        tint.setColorValue(value);
        return tint;
    }

    public static Tint of(ColorType type, ColorBlend blend, int value, double brightness) {
        Tint tint = new Tint();
        tint.setColorType(type);
        tint.setColorBlend(blend);
        tint.setColorValue(getBrightenedColor(value, brightness));
        return tint;
    }

    public static Tint of(Tint tint) {
        Tint _tint = new Tint();
        _tint.setColorType(tint.getColorType());
        _tint.setColorBlend(tint.getColorBlend());
        _tint.setColorValue(tint.getColorValue());
        return _tint;
    }

    public ColorType getColorType() {
        return type;
    }
    public ColorBlend getColorBlend() {
        return blend;
    }
    public int getColorValue() {
        return value;
    }

    public void setColorType(ColorType type) {
        this.type = type;
    }
    public void setColorBlend(ColorBlend blend) {
        this.blend = blend;
    }
    public void setColorValue(int value) {
        this.value = value;
    }

    private static int getBrightenedColor(int color, double brightness) {
        int r = (int) Math.round((color >> 16 & 0xff) * brightness);
        int g = (int) Math.round((color >> 8 & 0xff) * brightness);
        int b = (int) Math.round((color & 0xff) * brightness);
        return r << 16 | g << 8 | b;
    }

    public static class Serializer implements JsonDeserializer<Tint>, JsonSerializer<Tint> {
        private static final String JSON_OBJECT_NAME_TINT_COLOR_TYPE = "color_type";
        private static final String JSON_OBJECT_NAME_TINT_COLOR_BLEND = "color_blend";
        private static final String JSON_OBJECT_NAME_TINT_COLOR_VALUE = "color_value";

        public Tint deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            return Tint.of(ColorType.valueOf(jsonObject.get(JSON_OBJECT_NAME_TINT_COLOR_TYPE).getAsString().toUpperCase()),
                    ColorBlend.valueOf(jsonObject.get(JSON_OBJECT_NAME_TINT_COLOR_BLEND).getAsString().toUpperCase()),
                    Integer.parseInt(jsonObject.get(JSON_OBJECT_NAME_TINT_COLOR_VALUE).getAsString(), 16));
        }

        public JsonElement serialize(Tint tint, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject result = new JsonObject();
            result.add(JSON_OBJECT_NAME_TINT_COLOR_TYPE, new JsonPrimitive(String.valueOf(tint.getColorType()).toLowerCase()));
            result.add(JSON_OBJECT_NAME_TINT_COLOR_BLEND, new JsonPrimitive(String.valueOf(tint.getColorBlend()).toLowerCase()));
            result.add(JSON_OBJECT_NAME_TINT_COLOR_VALUE, new JsonPrimitive(String.format("%06x", tint.getColorValue())));
            return result;
        }
    }
}
