package com.qiniu.process.qiniu;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.process.Base;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.BucketManager.*;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.model.StorageType;
import com.qiniu.util.Auth;
import com.qiniu.util.HttpRespUtils;
import com.qiniu.util.CloudApiUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChangeType extends Base<Map<String, String>> {

    private StorageType storageType;
    private BatchOperations batchOperations;
    private List<Map<String, String>> lines;
    private Configuration configuration;
    private BucketManager bucketManager;

    public ChangeType(String accessKey, String secretKey, Configuration configuration, String bucket, int type)
            throws IOException {
        super("type", accessKey, secretKey, bucket);
        storageType = type == 0 ? StorageType.COMMON : StorageType.INFREQUENCY;
        this.configuration = configuration;
        this.bucketManager = new BucketManager(Auth.create(accessKey, secretKey), configuration.clone());
        CloudApiUtils.checkQiniu(bucketManager, bucket);
    }

    public ChangeType(String accessKey, String secretKey, Configuration configuration, String bucket, int type,
                      String savePath, int saveIndex) throws IOException {
        super("type", accessKey, secretKey, bucket, savePath, saveIndex);
        storageType = type == 0 ? StorageType.COMMON : StorageType.INFREQUENCY;
        this.batchSize = 1000;
        this.batchOperations = new BatchOperations();
        this.lines = new ArrayList<>();
        this.configuration = configuration;
        this.bucketManager = new BucketManager(Auth.create(accessKey, secretKey), configuration.clone());
        CloudApiUtils.checkQiniu(bucketManager, bucket);
    }

    public ChangeType(String accessKey, String secretKey, Configuration configuration, String bucket, int type,
                      String savePath) throws IOException {
        this(accessKey, secretKey, configuration, bucket, type, savePath, 0);
    }

    @Override
    public ChangeType clone() throws CloneNotSupportedException {
        ChangeType changeType = (ChangeType)super.clone();
        changeType.bucketManager = new BucketManager(Auth.create(accessId, secretKey), configuration.clone());
        changeType.batchOperations = new BatchOperations();
        changeType.lines = new ArrayList<>();
        return changeType;
    }

    @Override
    protected String resultInfo(Map<String, String> line) {
        return line.get("key");
    }

    @Override
    protected List<Map<String, String>> putBatchOperations(List<Map<String, String>> processList) throws IOException {
        batchOperations.clearOps();
        lines.clear();
        String key;
        for (Map<String, String> map : processList) {
            key = map.get("key");
            if (key != null) {
                lines.add(map);
                batchOperations.addChangeTypeOps(bucket, storageType, key);
            } else {
                fileSaveMapper.writeError("key is not exists or empty in " + map, false);
            }
        }
        return lines;
    }

    @Override
    protected String batchResult(List<Map<String, String>> lineList) throws IOException {
        return HttpRespUtils.getResult(bucketManager.batch(batchOperations));
    }

    @Override
    protected String singleResult(Map<String, String> line) throws IOException {
        String key = line.get("key");
        if (key == null) throw new IOException("key is not exists or empty in " + line);
        Response response = bucketManager.changeType(bucket, key, storageType);
        if (response.statusCode != 200) throw new QiniuException(response);
        response.close();
        return String.join("\t", key, "200");
    }

    @Override
    public void closeResource() {
        super.closeResource();
        batchOperations = null;
        lines = null;
        configuration = null;
        bucketManager = null;
    }
}
