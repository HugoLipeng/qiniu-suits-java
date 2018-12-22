package com.qiniu.service.help;

import com.google.gson.JsonObject;
import com.qiniu.persistence.FileMap;
import com.qiniu.util.JsonConvertUtils;

import java.io.IOException;

public class ProgressRecorder implements Cloneable {

    private String processName;
    private String resultPath;
    private int resultIndex;
    private FileMap fileMap;
    private String[] keys;

    public ProgressRecorder(String processName, String resultPath, int resultIndex, FileMap fileMap, String[] keys) {
        this.processName = processName;
        this.resultPath = resultPath;
        this.resultIndex = resultIndex;
        this.fileMap = fileMap;
        this.keys = keys;
    }

    public ProgressRecorder clone() throws CloneNotSupportedException {
        ProgressRecorder progressRecorder = (ProgressRecorder)super.clone();
        progressRecorder.fileMap = new FileMap();
        try {
            progressRecorder.fileMap.initWriter(resultPath, processName, resultIndex++);
        } catch (IOException e) {
            throw new CloneNotSupportedException("init writer failed.");
        }
        return progressRecorder;
    }

    public void record(String... progress) throws IOException {
        JsonObject jsonObject = new JsonObject();
        for (int i = 0; i < keys.length; i++) {
            if (progress.length < i) break;
            jsonObject.addProperty(keys[i], progress[i]);
        }
        fileMap.writeKeyFile(processName + "_" + resultIndex, JsonConvertUtils.toJsonWithoutUrlEscape(jsonObject));
    }

    public void close() {
        fileMap.closeWriter();
    }
}
