package com.ishikyoo.leavesly.settings;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.ishikyoo.leavesly.Leavesly;
import com.ishikyoo.leavesly.block.Blocks;
import com.ishikyoo.leavesly.util.Version;
import net.minecraft.block.Block;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import java.lang.reflect.Type;
import java.util.HashMap;

public class LeaveslySettingsData {
    private LeaveslySettingsData() {

    }

    public static final Logger LOG = Leavesly.LOGGER;

    private Version version;
    private boolean debug;
    private boolean log;
    private SnowLayerData snowLayerData;
    private HashMap<Identifier, BlockData> blockDataHashMap;

    public static LeaveslySettingsData of(Version version, boolean debug, boolean log, SnowLayerData snowLayerData, HashMap<Identifier, BlockData> blockData) {
        LeaveslySettingsData data = new LeaveslySettingsData();
        data.setVersion(version);
        data.setDebug(debug);
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

    public Version getVersion() { return version; }
    public boolean isDebug() { return debug; }
    public boolean shouldLog() { return log; }
    public SnowLayerData getSnowLayer() {
        return snowLayerData;
    }
    public BlockData getBlock(Identifier id) {
        return blockDataHashMap.get(id);
    }
    public BlockData getBlock(Block block) {
        return blockDataHashMap.get(Blocks.getBlockId(block));
    }
    public HashMap<Identifier, BlockData> getBlocks() {
        return blockDataHashMap;
    }

    public void setVersion(Version version) { this.version = version; }
    public void setDebug(boolean value) { debug = value; }
    public void setShouldLog(boolean value) { log = value; }
    public void setSnowLayer(SnowLayerData data) {
        snowLayerData = data;
    }
    public void putBlock(Identifier id, BlockData data) {
        blockDataHashMap.put(id, data);
    }
    public void setBlock(Identifier id, BlockData data) {
        blockDataHashMap.replace(id, data);
    }

    public boolean containsBlock(Identifier id) {
        return blockDataHashMap.containsKey(id);
    }

    public boolean containsBlock(Block block) {
        return containsBlock(Blocks.getBlockId(block));
    }

    public static class Serializer implements JsonDeserializer<LeaveslySettingsData>, JsonSerializer<LeaveslySettingsData> {
        private static final String JSON_OBJECT_NAME_VERSION = "version";
        private static final String JSON_OBJECT_NAME_DEBUG = "debug";
        private static final String JSON_OBJECT_NAME_LOG = "log";
        private static final String JSON_OBJECT_NAME_SNOW_LAYER = "snow_layer";
        private static final String JSON_OBJECT_NAME_BLOCK = "block";

        public LeaveslySettingsData deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            LeaveslySettingsData data = new LeaveslySettingsData();
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            data.setVersion(Version.of(jsonObject.get(JSON_OBJECT_NAME_VERSION).getAsString()));
            data.setDebug(jsonObject.get(JSON_OBJECT_NAME_DEBUG).getAsBoolean());
            data.setShouldLog(jsonObject.get(JSON_OBJECT_NAME_LOG).getAsBoolean());
            data.setSnowLayer(getGson().fromJson(jsonObject.get(JSON_OBJECT_NAME_SNOW_LAYER), SnowLayerData.class));
            JsonElement jsonElementBlock = jsonObject.get(JSON_OBJECT_NAME_BLOCK);
            Type mapType = new TypeToken<HashMap<Identifier, BlockData>>(){}.getType();
            data.blockDataHashMap = getGson().fromJson(jsonElementBlock, mapType);
            return data;
        }

        public JsonElement serialize(LeaveslySettingsData data, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add(JSON_OBJECT_NAME_VERSION, new JsonPrimitive(data.getVersion().toString()));
            jsonObject.add(JSON_OBJECT_NAME_DEBUG, new JsonPrimitive(data.isDebug()));
            jsonObject.add(JSON_OBJECT_NAME_LOG, new JsonPrimitive(data.shouldLog()));
            jsonObject.add(JSON_OBJECT_NAME_SNOW_LAYER, Leavesly.getSettings().getGson().toJsonTree(data.getSnowLayer()));
            jsonObject.add(JSON_OBJECT_NAME_BLOCK, Leavesly.getSettings().getGson().toJsonTree(data.blockDataHashMap));
            return jsonObject;
        }
    }

    private static Gson getGson() {
        return Leavesly.getSettings().getGson();
    }
}
