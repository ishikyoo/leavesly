package com.ishikyoo.leavesly.settings;

import com.google.gson.*;

import java.lang.reflect.Type;

public class SnowLayerData {
    private SnowLayerData() {

    }

    private boolean isEnabled;
    private double transitionSpeed;
    private double minCoverage;
    private double maxCoverage;

    public boolean isEnabled() {
        return isEnabled;
    }
    public double getTransitionSpeed() {
        return transitionSpeed;
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
    public void setTransitionSpeed(double speed) {
        transitionSpeed = speed;
    }
    public void setMinCoverage(double coverage) {
        this.minCoverage = coverage;
    }
    public void setMaxCoverage(double coverage) {
        this.maxCoverage = coverage;
    }

    public static SnowLayerData of(boolean enabled, double transitionSpeed, double minCoverage, double maxCoverage) {
        SnowLayerData data = new SnowLayerData();
        data.setEnabled(enabled);
        data.setTransitionSpeed(transitionSpeed);
        data.setMinCoverage(minCoverage);
        data.setMaxCoverage(maxCoverage);
        return data;
    }

    public static class Serializer implements JsonDeserializer<SnowLayerData>, JsonSerializer<SnowLayerData> {
        private static final String JSON_OBJECT_NAME_SNOW_LAYER_ENABLED = "enabled";
        private static final String JSON_OBJECT_NAME_SNOW_LAYER_TRANSITION_SPEED = "transition_speed";
        private static final String JSON_OBJECT_NAME_SNOW_LAYER_MIN_COVERAGE = "coverage_min";
        private static final String JSON_OBJECT_NAME_SNOW_LAYER_MAX_COVERAGE = "coverage_max";

        public SnowLayerData deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            return SnowLayerData.of(
                    jsonObject.get(JSON_OBJECT_NAME_SNOW_LAYER_ENABLED).getAsBoolean(),
                    jsonObject.get(JSON_OBJECT_NAME_SNOW_LAYER_TRANSITION_SPEED).getAsDouble(),
                    jsonObject.get(JSON_OBJECT_NAME_SNOW_LAYER_MIN_COVERAGE).getAsDouble(),
                    jsonObject.get(JSON_OBJECT_NAME_SNOW_LAYER_MAX_COVERAGE).getAsDouble());
        }

        public JsonElement serialize(SnowLayerData data, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject result = new JsonObject();
            result.add(JSON_OBJECT_NAME_SNOW_LAYER_ENABLED, new JsonPrimitive(data.isEnabled()));
            result.add(JSON_OBJECT_NAME_SNOW_LAYER_TRANSITION_SPEED, new JsonPrimitive(data.getTransitionSpeed()));
            result.add(JSON_OBJECT_NAME_SNOW_LAYER_MIN_COVERAGE, new JsonPrimitive(data.getMinCoverage()));
            result.add(JSON_OBJECT_NAME_SNOW_LAYER_MAX_COVERAGE, new JsonPrimitive(data.getMaxCoverage()));
            return result;
        }
    }
}
