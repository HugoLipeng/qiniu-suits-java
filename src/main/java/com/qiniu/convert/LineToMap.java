package com.qiniu.convert;

import com.qiniu.util.LineUtils;

import java.io.IOException;
import java.util.*;

public class LineToMap extends ObjectToMap<String> {

    public LineToMap(String parseType, String separator, Map<String, String> indexMap) throws IOException {
        if ("json".equals(parseType)) {
            this.lineParser = line -> LineUtils.getItemMap(line, indexMap, false);
        } else if ("csv".equals(parseType)) {
            this.lineParser = line -> LineUtils.getItemMap(line, ",", indexMap, false);
        } else if ("tab".equals(parseType)) {
            this.lineParser = line -> LineUtils.getItemMap(line, separator, indexMap, false);
        } else {
            throw new IOException("please check your format for line to map.");
        }
    }
}
