package com.qiniu.datasource;

import com.qiniu.common.SuitsException;
import com.qiniu.interfaces.ITypeConvert;
import com.qiniu.persistence.IResultOutput;
import com.qiniu.sdk.FolderItem;
import com.qiniu.sdk.UpYunClient;
import com.qiniu.sdk.UpYunConfig;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class UpYunOssContainer extends OssContainer<FolderItem, BufferedWriter, Map<String, String>> {

    private String username;
    private String password;
    private UpYunConfig configuration;

    public UpYunOssContainer(String username, String password, UpYunConfig configuration, String bucket,
                             List<String> antiPrefixes, Map<String, String[]> prefixesMap, boolean prefixLeft,
                             boolean prefixRight, Map<String, String> indexMap, int unitLen, int threads) {
        super(bucket, antiPrefixes, prefixesMap, prefixLeft, prefixRight, indexMap, unitLen, threads);
        this.username = username;
        this.password = password;
        this.configuration = configuration;
    }

    @Override
    public String getSourceName() {
        return "upyun";
    }

    @Override
    protected ITypeConvert<FolderItem, Map<String, String>> getNewConverter() {
        return null;
    }

    @Override
    protected ITypeConvert<FolderItem, String> getNewStringConverter() throws IOException {
        return null;
    }

    @Override
    protected IResultOutput<BufferedWriter> getNewResultSaver(String order) throws IOException {
        return null;
    }

    @Override
    protected ILister<FolderItem> getLister(String prefix, String marker, String end) throws SuitsException {
        try {
            return new UpLister(new UpYunClient(configuration, username, password), bucket, prefix, marker, end, unitLen);
        } catch (Exception e) {
            throw new SuitsException(40000, e.getMessage());
        }
    }
}
