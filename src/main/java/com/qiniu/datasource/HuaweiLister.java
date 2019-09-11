package com.qiniu.datasource;

import com.obs.services.ObsClient;
import com.obs.services.exception.ObsException;
import com.obs.services.model.ListObjectsRequest;
import com.obs.services.model.ObjectListing;
import com.obs.services.model.ObsObject;
import com.qiniu.common.SuitsException;
import com.qiniu.interfaces.ILister;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HuaweiLister implements ILister<ObsObject> {

    private ObsClient obsClient;
    private ListObjectsRequest listObjectsRequest;
    private String endPrefix;
    private List<ObsObject> obsObjects;
    private long count;
    private static final List<ObsObject> defaultList = new ArrayList<>();

    public HuaweiLister(ObsClient obsClient, String bucket, String prefix, String marker, String endPrefix, int max) throws SuitsException {
        this.obsClient = obsClient;
        this.listObjectsRequest = new ListObjectsRequest();
        listObjectsRequest.setBucketName(bucket);
        listObjectsRequest.setPrefix(prefix);
        listObjectsRequest.setMarker("".equals(marker) ? null : marker);
        listObjectsRequest.setMaxKeys(max);
        this.endPrefix = endPrefix;
        doList();
        count += obsObjects.size();
    }

    @Override
    public String getBucket() {
        return listObjectsRequest.getBucketName();
    }

    public String getPrefix() {
        return listObjectsRequest.getPrefix();
    }

    public void setMarker(String marker) {
        listObjectsRequest.setMarker("".equals(marker) ? null : marker);
    }

    public String getMarker() {
        return listObjectsRequest.getMarker();
    }

    @Override
    public void setEndPrefix(String endPrefix) {
        this.endPrefix = endPrefix;
        checkedListWithEnd();
    }

    @Override
    public String getEndPrefix() {
        return endPrefix;
    }

    @Override
    public void setLimit(int limit) {
        listObjectsRequest.setMaxKeys(limit);
    }

    public int getLimit() {
        return listObjectsRequest.getMaxKeys();
    }

    private void checkedListWithEnd() {
        if (endPrefix == null || "".equals(endPrefix)) return;
        String endKey = currentEndKey();
        if (endKey == null) return;
        if (endKey.compareTo(endPrefix) == 0) {
            listObjectsRequest.setMarker(null);
            if (endPrefix.equals(getPrefix() + CloudStorageContainer.firstPoint)) {
                if (obsObjects.size() > 0) {
                    int lastIndex = obsObjects.size() - 1;
                    ObsObject last = obsObjects.get(lastIndex);
                    if (endPrefix.equals(last.getObjectKey())) obsObjects.remove(lastIndex);
                }
            }
        } else if (endKey.compareTo(endPrefix) > 0) {
            listObjectsRequest.setMarker(null);
            int size = obsObjects.size();
            // SDK 中返回的是 ArrayList，使用 remove 操作性能一般较差，同时也为了避免 Collectors.toList() 的频繁 new 操作，根据返
            // 回的 list 为文件名有序的特性，直接从 end 的位置进行截断
            for (int i = 0; i < size; i++) {
                if (obsObjects.get(i).getObjectKey().compareTo(endPrefix) > 0) {
                    obsObjects = obsObjects.subList(0, i);
                    return;
                }
            }
        }
    }

    private void doList() throws SuitsException {
        try {
            ObjectListing objectListing = obsClient.listObjects(listObjectsRequest);
            listObjectsRequest.setMarker(objectListing.getNextMarker());
            obsObjects = objectListing.getObjects();
            checkedListWithEnd();
        } catch (ObsException e) {
            throw new SuitsException(e.getResponseCode(), e.getMessage());
        } catch (NullPointerException e) {
            throw new SuitsException(e, 400000, "lister maybe already closed");
        } catch (Exception e) {
            throw new SuitsException(e, -1, "listing failed");
        }
    }

    @Override
    public synchronized void listForward() throws SuitsException {
        if (hasNext()) {
            doList();
            count += obsObjects.size();
        } else {
            obsObjects = defaultList;
        }
    }

    @Override
    public boolean hasNext() {
        return listObjectsRequest.getMarker() != null && !"".equals(listObjectsRequest.getMarker());
    }

    @Override
    public boolean hasFutureNext() throws SuitsException {
        int expected = listObjectsRequest.getMaxKeys() + 1;
        if (expected <= 10000) expected = 10001;
        int times = 100000 / (obsObjects.size() + 1) + 1;
        times = times > 10 ? 10 : times;
        List<ObsObject> futureList = obsObjects;
        while (hasNext() && times > 0 && futureList.size() < expected) {
            times--;
            doList();
            futureList.addAll(obsObjects);
        }
        obsObjects = futureList;
        count += obsObjects.size();
        return hasNext();
    }

    @Override
    public List<ObsObject> currents() {
        return obsObjects;
    }

    @Override
    public String currentEndKey() {
        if (hasNext()) return getMarker();
        if (obsObjects.size() > 0) return obsObjects.get(obsObjects.size() - 1).getObjectKey();
        return null;
    }

    @Override
    public synchronized String truncate() {
        String truncateMarker = listObjectsRequest.getMarker();
        listObjectsRequest.setMarker(null);
        return truncateMarker;
    }

    @Override
    public long count() {
        return count;
    }

    @Override
    public void close() {
        try {
            obsClient.close();
        } catch (IOException e) {
            obsClient = null;
        }
//        listObjectsRequest = null;
        endPrefix = null;
        obsObjects = defaultList;
    }
}
