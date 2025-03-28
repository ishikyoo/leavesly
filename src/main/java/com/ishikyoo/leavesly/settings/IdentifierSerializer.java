package com.ishikyoo.leavesly.settings;

import com.google.gson.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.lang.reflect.Type;

public class IdentifierSerializer implements JsonDeserializer<Identifier>, JsonSerializer<Identifier> {
    public Identifier deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return Identifier.of(JsonHelper.asString(jsonElement, "location"));
    }

    public JsonElement serialize(Identifier identifier, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(identifier.toString());
    }
}
