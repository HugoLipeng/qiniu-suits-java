package com.qiniu.service.interfaces;

import com.qiniu.service.process.ListFileAntiFilter;
import com.qiniu.service.process.ListFileFilter;
import com.qiniu.common.QiniuException;

import java.util.List;
import java.util.Map;

public interface ILineProcess<T> {

    ILineProcess<T> getNewInstance(int resultFileIndex) throws CloneNotSupportedException;

    default void setBatch(boolean batch) {}

    void setRetryCount(int retryCount);

    default String getProcessName() {
        return "";
    }

    default String getInfo() {
        return "";
    }

    default void setFilter(ListFileFilter listFileFilter, ListFileAntiFilter listFileAntiFilter) {}

    void processLine(List<T> list) throws QiniuException;

    default void setNextProcessor(ILineProcess<Map<String, String>> nextProcessor) {}

    void closeResource();
}
