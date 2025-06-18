package com.ishikyoo.leavesly.settings;

import com.google.gson.*;
import com.ishikyoo.leavesly.Leavesly;

import java.lang.reflect.Type;

public class BlockData  {
    private BlockData() {

    }

    private Tint tint;
    private BlockSnowLayerData snowLayer;


    public Tint getTint() {
        return tint;
    }
    public BlockSnowLayerData getSnowLayer() {
        return snowLayer;
    }

    public void setTint(Tint tint) {
        this.tint = tint;
    }
    public void setSnowLayer(BlockSnowLayerData snowLayer) {
        this.snowLayer = snowLayer;
    }

    public static BlockData of(Tint tint, BlockSnowLayerData snowLayer) {
        BlockData data = new BlockData();
        data.setTint(tint);
        data.setSnowLayer(snowLayer);
        return data;
    }

    public static class Serializer implements JsonDeserializer<BlockData>, JsonSerializer<BlockData> {
        private static final String JSON_OBJECT_NAME_TINT = "tint";
        private static final String JSON_OBJECT_NAME_SNOW_LAYER = "snow_layer";

        public BlockData deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            JsonElement jsonElementTint = jsonObject.get(JSON_OBJECT_NAME_TINT);
            JsonElement jsonElementSnowLayer = jsonObject.get(JSON_OBJECT_NAME_SNOW_LAYER);
            Tint clientBlockData = getGson().fromJson(jsonElementTint, Tint.class);
            BlockSnowLayerData serverBlockData = getGson().fromJson(jsonElementSnowLayer, BlockSnowLayerData.class);
            return BlockData.of(clientBlockData, serverBlockData);
        }

        public JsonElement serialize(BlockData blockData, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add(JSON_OBJECT_NAME_TINT, getGson().toJsonTree(blockData.getTint()));
            jsonObject.add(JSON_OBJECT_NAME_SNOW_LAYER, getGson().toJsonTree(blockData.getSnowLayer()));
            return jsonObject;
        }
    }

    private static Gson getGson() {
        return Leavesly.getSettings().getGson();
    }
}