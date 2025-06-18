package com.ishikyoo.leavesly.settings;

import com.google.gson.*;

import java.lang.reflect.Type;

public class BlockSnowLayerData {
    private BlockSnowLayerData() {

    }

    private boolean isEnabled;
    private double minCoverage;
    private double maxCoverage;

    public boolean isEnabled() {
        return isEnabled;
    }
    public double getMinCoverage() {
        return minCoverage;
    }
    public double getMaxCoverage() {
        return maxCoverage;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }
    public void setMinCoverage(double coverage) {
        this.minCoverage = coverage;
    }
    public void setMaxCoverage(double coverage) {
        this.maxCoverage = coverage;
    }

    public static BlockSnowLayerData of(boolean enabled, double minCoverage, double maxCoverage) {
        BlockSnowLayerData data = new BlockSnowLayerData();
        data.setEnabled(enabled);
        data.setMinCoverage(minCoverage);
        data.setMaxCoverage(maxCoverage);
        return data;
    }

    public static class Serializer implements JsonDeserializer<BlockSnowLayerData>, JsonSerializer<BlockSnowLayerData> {
        private static final String JSON_OBJECT_NAME_SNOW_LAYER_ENABLED = "enabled";
        private static final String JSON_OBJECT_NAME_SNOW_LAYER_MIN_COVERAGE = "coverage_min";
        private static final String JSON_OBJECT_NAME_SNOW_LAYER_MAX_COVERAGE = "coverage_max";

        public BlockSnowLayerData deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            return BlockSnowLayerData.of(
                    jsonObject.get(JSON_OBJECT_NAME_SNOW_LAYER_ENABLED).getAsBoolean(),
                    jsonObject.get(JSON_OBJECT_NAME_SNOW_LAYER_MIN_COVERAGE).getAsDouble(),
                    jsonObject.get(JSON_OBJECT_NAME_SNOW_LAYER_MAX_COVERAGE).getAsDouble());
        }

        public JsonElement serialize(BlockSnowLayerData data, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject result = new JsonObject();
            result.add(JSON_OBJECT_NAME_SNOW_LAYER_ENABLED, new JsonPrimitive(data.isEnabled()));
            result.add(JSON_OBJECT_NAME_SNOW_LAYER_MIN_COVERAGE, new JsonPrimitive(data.getMinCoverage()));
            result.add(JSON_OBJECT_NAME_SNOW_LAYER_MAX_COVERAGE, new JsonPrimitive(data.getMaxCoverage()));
            return result;
        }
    }
}
