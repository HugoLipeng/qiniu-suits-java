package com.qiniu.datasource;

import com.qiniu.convert.LineToMap;
import com.qiniu.convert.MapToString;
import com.qiniu.interfaces.IReader;
import com.qiniu.interfaces.ITypeConvert;
import com.qiniu.persistence.FileSaveMapper;
import com.qiniu.interfaces.IResultOutput;
import com.qiniu.util.FileUtils;

import java.io.*;
import java.util.*;

public class LocalFileContainer extends FileContainer<BufferedReader, BufferedWriter, Map<String, String>> {

    public LocalFileContainer(String filePath, String parseFormat, String separator, String addKeyPrefix,
                              String rmKeyPrefix, Map<String, String> linesMap, Map<String, String> indexMap,
                              List<String> fields, int unitLen, int threads) throws IOException {
        super(filePath, parseFormat, separator, addKeyPrefix, rmKeyPrefix, linesMap, indexMap, fields, unitLen, threads);
    }

    @Override
    protected ITypeConvert<String, Map<String, String>> getNewConverter() throws IOException {
        return new LineToMap(parse, separator, addKeyPrefix, rmKeyPrefix, indexMap);
    }

    @Override
    protected ITypeConvert<Map<String, String>, String> getNewStringConverter() throws IOException {
        return new MapToString(saveFormat, saveSeparator, fields);
    }

    @Override
    public String getSourceName() {
        return "local";
    }

    @Override
    protected IResultOutput<BufferedWriter> getNewResultSaver(String order) throws IOException {
        return order != null ? new FileSaveMapper(savePath, getSourceName(), order) : new FileSaveMapper(savePath);
    }

    @Override
    protected List<IReader<BufferedReader>> getFileReaders(String path) throws IOException {
        List<IReader<BufferedReader>> fileReaders = new ArrayList<>();
        if (linesMap != null && linesMap.size() > 0) {
            boolean pathIsValid = true;
            try { path = FileUtils.convertToRealPath(path); } catch (IOException ignored) { pathIsValid = false; }
            String type;
            File file;
            for (Map.Entry<String, String> entry : linesMap.entrySet()) {
                if (pathIsValid) {
                    file = new File(path, entry.getKey());
                    if (!file.exists()) file = new File(entry.getKey());
                } else {
                    file = new File(entry.getKey());
                }
                if (!file.exists()) throw new IOException("the filename not exists: " + entry.getKey());
                if (file.isDirectory()) throw new IOException("the filename defined in lines map can not be directory: "
                        + entry.getKey());
                else {
                    type = FileUtils.contentType(file);
                    if (type.startsWith("text") || type.equals("application/octet-stream")) {
                        fileReaders.add(new LocalFileReader(file, linesMap.get(file.getPath()), unitLen));
                    } else {
                        throw new IOException("please provide the \'text\' file. The current path you gave is: " + path);
                    }
                }
            }
        } else {
            path = FileUtils.convertToRealPath(path);
            File sourceFile = new File(path);
            if (sourceFile.isDirectory()) {
                List<File> files = FileUtils.getFiles(sourceFile, true);
                for (File file : files) {
                    if (file.getPath().contains(FileUtils.pathSeparator + ".")) continue;
                    fileReaders.add(new LocalFileReader(file, linesMap.get(file.getPath()), unitLen));
                }
            } else {
                String type = FileUtils.contentType(sourceFile);
                if (type.startsWith("text") || type.equals("application/octet-stream")) {
                    fileReaders.add(new LocalFileReader(sourceFile, linesMap.get(sourceFile.getPath()), unitLen));
                } else {
                    throw new IOException("please provide the \'text\' file. The current path you gave is: " + path);
                }
            }
        }
        if (fileReaders.size() == 0) throw new IOException("please provide the \'text\' file in the directory. " +
                "The current path you gave is: " + path);
        return fileReaders;
    }
}
