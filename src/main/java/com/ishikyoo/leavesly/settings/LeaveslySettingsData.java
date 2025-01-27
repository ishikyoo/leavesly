package com.ishikyoo.leavesly.settings;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.ishikyoo.leavesly.LeaveslyBlockRegistry;
import net.minecraft.block.Block;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.HashMap;

public class LeaveslySettingsData {
    private LeaveslySettingsData() {

    }

    public static final Logger LOGGER = LoggerFactory.getLogger("Leavesly");
    private static final Gson GSON = LeaveslySettings.getGson();

    private int version = 1;
    private SnowLayerData snowLayerData;
    private HashMap<Identifier, BlockData> blockDataHashMap;

    public static LeaveslySettingsData of(int version, SnowLayerData snowLayerData, HashMap<Identifier, BlockData> blockData) {
        LeaveslySettingsData data = new LeaveslySettingsData();
        data.setVersion(version);
        data.setSnowLayer(snowLayerData);
        data.blockDataHashMap = new HashMap<>(blockData);
        return data;
    }

    public static LeaveslySettingsData of(LeaveslySettingsData settings) {
        LeaveslySettingsData data = new LeaveslySettingsData();
        data.setVersion(settings.version);
        data.setSnowLayer(settings.snowLayerData);
        data.blockDataHashMap = new HashMap<>(settings.blockDataHashMap);
        return data;
    }

    public int getVersion() {
        return version;
    }
    public SnowLayerData getSnowLayer() {
        return snowLayerData;
    }
    public BlockData getBlock(Identifier id) {
        return blockDataHashMap.get(id);
    }
    public BlockData getBlock(Block block) {
        return blockDataHashMap.get(LeaveslyBlockRegistry.getBlock(block));
    }
    public HashMap<Identifier, BlockData> getBlocks() {
        return blockDataHashMap;
    }

    private void setVersion(int version) {
        this.version = version;
    }
    public void setSnowLayer(SnowLayerData data) {
        snowLayerData = data;
    }
    public void putBlock(Identifier id, BlockData data) {
        blockDataHashMap.put(id, data);
    }
    public void setBlock(Identifier id, BlockData data) {
        blockDataHashMap.replace(id, data);
    }

    public static class Serializer implements JsonDeserializer<LeaveslySettingsData>, JsonSerializer<LeaveslySettingsData> {
        private static final String JSON_OBJECT_NAME_VERSION = "version";
        private static final String JSON_OBJECT_NAME_SNOW_LAYER = "snow_layer";
        private static final String JSON_OBJECT_NAME_BLOCK = "block";

        public LeaveslySettingsData deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            LeaveslySettingsData data = new LeaveslySettingsData();
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            data.setVersion(jsonObject.get(JSON_OBJECT_NAME_VERSION).getAsInt());
            data.setSnowLayer(GSON.fromJson(jsonObject.get(JSON_OBJECT_NAME_SNOW_LAYER), SnowLayerData.class));
            JsonElement jsonElementBlock = jsonObject.get(JSON_OBJECT_NAME_BLOCK);
            Type mapType = new TypeToken<HashMap<Identifier, BlockData>>(){}.getType();
            data.blockDataHashMap = GSON.fromJson(jsonElementBlock, mapType);
            return data;
        }

        public JsonElement serialize(LeaveslySettingsData data, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add(JSON_OBJECT_NAME_VERSION, new JsonPrimitive(data.getVersion()));
            jsonObject.add(JSON_OBJECT_NAME_SNOW_LAYER, LeaveslySettings.getGson().toJsonTree(data.getSnowLayer()));
            jsonObject.add(JSON_OBJECT_NAME_BLOCK, LeaveslySettings.getGson().toJsonTree(data.blockDataHashMap));
            return jsonObject;
        }
    }
}
