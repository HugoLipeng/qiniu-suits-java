# 导出 ts 列表操作

## 简介
对 m3u8 的资源链接进行读取导出其中的 ts 文件列表。  

## 配置文件选项

### 配置参数
```
process=exportts 
domain=
protocol=
url-index=
rm-prefix=
```  
|参数名|参数值及类型 | 含义|  
|-----|-------|-----|  
|process=exportts| 从 m3u8 导出 ts 时设置为exportts| 表示导出 ts 操作|  
|domain| 域名字符串| 用于拼接文件名生成链接的域名，数据源为 file 且指定 url-index 时无需设置|  
|protocol| http/https| 使用 http 还是 https 访问资源进行抓取（默认 http）|  
|url-index| 字符串| 通过 url 操作时需要设置的 url 索引（下标），需要手动指定才会进行解析|  
|rm-prefix| 字符串| 表示将得到的目标文件名去除存在的指定前缀后再进行exportts操作，用于输入的文件名可能比实际空间的文件名多了前缀的情况，如果设置了另外的 url-index 则该参数无效|  

### 关于 url-index
当使用 file 源且 parse=tab/csv 时下标必须为整数。url-index 表示输入行含 url 形式的源文件地址，未设置的情况下则使用 key 字段加上 domain 的
方式访问源文件地址，key 下标用 indexes 参数设置。  

## 命令行方式
```
-process=avinfo -domain= -protocol=
```