package com.qiniu.config;

import com.qiniu.service.interfaces.IEntryParam;

import java.io.*;
import java.lang.reflect.Method;
import java.util.Properties;

public class PropertiesFile implements IEntryParam {

    private Properties properties;

    public PropertiesFile(String resourceName) throws IOException {
        InputStream inputStream = null;

        try {
            inputStream = new FileInputStream(resourceName);
            properties = new Properties();
            properties.load(new InputStreamReader(new BufferedInputStream(inputStream), "utf-8"));
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    inputStream = null;
                }
            }
        }
    }

    /**
     * 获取属性值，判断是否存在相应的 key，否则抛出异常
     * @param key 属性名
     * @return 属性值字符
     * @throws IOException
     */
    public String getParamValue(String key) throws IOException {
        if (properties.containsKey(key)) {
            return properties.getProperty(key);
        } else {
            throw new IOException("not set " + key + " param.");
        }
    }

    /**
     * 获取属性值，不抛出异常，使用 default 值进行返回
     * @param key
     * @param defaultValue 默认返回值
     * @return 属性值字符
     */
    public String getParamValue(String key, String defaultValue) {
        if (properties.containsKey(key)) {
            return properties.getProperty(key);
        } else {
            return defaultValue;
        }
    }

    /**
     * 获取属性值，通过反射转换成指定类型
     * @param key
     * @param clazz 返回值类型 class
     * @param defaultValue
     * @param <T> 范型
     * @return
     * @throws Exception
     */
    public <T> T getParamValue(String key, Class<T> clazz, T defaultValue) throws Exception {
        Method method = clazz.getMethod("valueOf", clazz.getClasses());
        if (properties.containsKey(key)) {
            return (T) method.invoke(clazz, properties.getProperty(key));
        } else {
            return defaultValue;
        }
    }
}
