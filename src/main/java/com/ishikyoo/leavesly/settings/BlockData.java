package com.ishikyoo.leavesly.settings;

import com.google.gson.*;

import java.lang.reflect.Type;

public class BlockData  {
    private BlockData() {

    }

    private static final Gson GSON = LeaveslySettings.getGson();

    private Tint tint;
    private SnowLayerData snowLayer;

    
    public Tint getTint() {
        return tint;
    }
    public SnowLayerData getSnowLayer() {
        return snowLayer;
    }

    public void setTint(Tint tint) {
        this.tint = tint;
    }
    public void setSnowLayer(SnowLayerData snowLayer) {
        this.snowLayer = snowLayer;
    }

    public static BlockData of(Tint tint, SnowLayerData snowLayer) {
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
            Tint clientBlockData = GSON.fromJson(jsonElementTint, Tint.class);
            SnowLayerData serverBlockData = GSON.fromJson(jsonElementSnowLayer, SnowLayerData.class);
            return BlockData.of(clientBlockData, serverBlockData);
        }

        public JsonElement serialize(BlockData blockData, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add(JSON_OBJECT_NAME_TINT, GSON.toJsonTree(blockData.getTint()));
            jsonObject.add(JSON_OBJECT_NAME_SNOW_LAYER, GSON.toJsonTree(blockData.getSnowLayer()));
            return jsonObject;
        }
    }
}
