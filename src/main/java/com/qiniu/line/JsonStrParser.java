package com.qiniu.line;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.qiniu.interfaces.ILineParser;

import java.io.IOException;
import java.util.*;

public class JsonStrParser implements ILineParser<String> {

    private JsonObjParser jsonObjParser;

    public JsonStrParser(HashMap<String, String> indexMap) throws IOException {
        this.jsonObjParser = new JsonObjParser(indexMap, false);
    }

    public Map<String, String> getItemMap(String line) throws IOException {
        JsonObject parsed = new JsonParser().parse(line).getAsJsonObject();
        return jsonObjParser.getItemMap(parsed);
    }
}