# 1.安装

- flink version:1.10.0(scala2.12)
- java 1.8
- ssh 免密（包括本机）
- 相同的目录结构

IP | 主机名 | 描述
---|---|---
172.16.32.38 | master | 主节点
172.16.32.39 | slave1 | 工作节点1
172.16.32.40 | slave2 | 工作节点2
172.16.32.41 | slave3 | 工作节点3

安装路径：```/sdg1/flink-1.10.1```
## 1.1 下载



```
curl https://mirror.bit.edu.cn/apache/flink/flink-1.10.1/flink-1.10.1-bin-scala_2.12.tgz 
```
## 1.2 解压

```
# -C 安装到指定目录  --strip-components 跳过压缩包中的第1层目录
tar -xzf flink-1.10.1-bin-scala_2.12.tgz -C /sdg1/flink-1.10.1 --strip-components 1
```

## 1.3 配置Java

ps:可以不指定，使用环境变量，
```
#conf/flink-conf.yaml，配置文件中不存在该属性名,需手动增加
env.java.home: {你的java安装路径}
```

## 1.4 配置Flink

[配置页面](https://ci.apache.org/projects/flink/flink-docs-release-1.10/ops/config.html)
### 

文件`flink-confg.yaml`配置如下内容
```
jobmanager.rpc.address: master
#设置flink master（JobManager / ResourceManager / Dispatcher）的jvm堆大小
jobmanager.heap.size：16g #Flink 1.11.0移除该参数
jobmanager.memory.process.size: 4g  #Flink 1.11.0 新增

#每个TM进程总内存大小（包括JVM metaspace和其他）
taskmanager.memory.process.size: 20g
#Flink自身可用内存，不包括jvm 元空间和开销，和上面参数不建议同时设置
taskmanager.memory.flink.size: 
#每台机器上的CPU数
taskmanager.numberOfTaskSlots: 16
#operators、data sources、data sinks的默认并行性，可以在代码中指定，不指定使用默认
parallelism.default: 1
#临时目录
io.tmp.dirs: /sdg1/flink-1.10.1/temp
```
[FLink内存配置](https://ci.apache.org/projects/flink/flink-docs-release-1.10/zh/ops/memory/mem_setup.html)
## 1.5 HA
```
high-availability: zookeeper
# 保存主节点元数据的路径（HDFS, S3, Ceph, nfs,）
high-availability.storageDir: hdfs:///flink/ha/
# zookeeper路径
high-availability.zookeeper.quorum: localhost:2181
#
high-availability.zookeeper.path.root: /flink
high-availability.cluster-id: /default_ns
```
flink默认使用的是zookeeper 3.4版本，如果使用Zookeeper3.5需要将opt目录下的zookeeper放在lib目录
### 配置工作节点

在文件`conf/slaves`中追加如下内容（规划好的工作节点）
version1.11.0为`conf/workers`
```
slave1
slave2
slave3
```

## 1.5 启动和停止
```
bin/start-cluster.sh
bin/stop-cluster.sh
```

## 1.6 Web页面
http://172.16.32.38:8081
```
#访问端口配置
rest.port: 8081
# 上传job的jars保存目录
web.upload.dir: /sdg1/flink-1.10.1/webupload
```

