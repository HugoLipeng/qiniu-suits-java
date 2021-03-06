# 资源复制

## 简介
对空间中的资源进行**复制**到另一个目标空间。参考：[七牛空间资源复制](https://developer.qiniu.com/kodo/api/1254/copy)/[批量复制](https://developer.qiniu.com/kodo/api/1250/batch)  
1. **操作需要指定数据源，默认表示从七牛空间列举文件执行操作，如非默认或需更多条件，请先[配置数据源](datasource.md)**  
2. 支持通过 `-a=<account-name>`/`-d` 使用已设置的账号，则不需要再直接设置密钥，参考：[账号设置](../README.md#账号设置)  
3. 单次复制一个文件请参考[ single 操作](single.md)  
4. 交互式操作随时输入 key 进行复制请参考[ interactive 操作](interactive.md)  

## 配置
```
process=copy 
ak=<ak> 
sk=<sk> 
bucket=<bucket> 
indexes=
to-bucket=<to-bucket>
toKey-index=
add-prefix=
rm-prefix=
```  
|参数名|参数值及类型 | 含义|  
|-----|-------|-----|  
|process=copy| 复制资源时设置为copy| 表示复制操作|  
|ak、sk|长度40的字符串|七牛账号的ak、sk，通过七牛控制台个人中心获取，当数据源为 qiniu 时无需再设置|  
|bucket| 字符串| 操作的资源原空间，当数据源为 qiniu 时无需再设置|  
|indexes|字符串| 设置输入行中 key 字段的下标（有默认值），参考[数据源 indexes 设置](datasource.md#1-公共参数)|  
|to-bucket| 字符串| 复制资源保存的目标空间|  
|toKey-index| 字符串| copy 操作可选择设置的目标文件名索引（下标），需要手动指定才会进行解析|  
|add-prefix| 字符串| 表示为保存的文件名添加指定前缀|  
|rm-prefix| 字符串| 表示将原文件名去除存在的指定前缀后作为 copy 之后保存的文件名|  

### 关于 toKey-index
指定输入行中对应修改之后的文件名字段下标，不设置则无法进行解析，当使用 file 源且 parse=tab/csv 时下标必须为整数，但未设置且 add-prefix 不为空
时需要强制指定 prefix-force=true，表明该次重命名操作只添加文件名前缀。由于 rename 操作既需要原始文件名字段也需要新文件名字段，因此 toKey 下
标和 key 字段下标不可相同，key 下标用 indexes 参数设置，。  
**注意**：七牛存储空间不支持文件名以 `../`, `./` 开头或者包含 `/../`, `/./` 这种情况，会造成无法访问，因此设置文件名时请注意。  

### 命令行方式
```
-process=copy -ak= -sk= -bucket= -to-bucket= -toKey-index= -add-prefix= -rm-prefix=
```
