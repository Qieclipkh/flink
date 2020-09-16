# 1.安装

- Hadoop version:2.8.3
- java 1.8
- ssh 免密（包括本机）
- 相同的目录结构

IP | 主机名 | 描述
---|---|---
172.16.32.38 | master | 主节点
172.16.32.39 | slave1 | 工作节点1
172.16.32.40 | slave2 | 工作节点2
172.16.32.41 | slave3 | 工作节点3

安装路径：```/sdg1/hadoop-2.8.3```
参考文档：[https://hadoop.apache.org/docs/r2.8.3/](https://hadoop.apache.org/docs/r2.8.3/)


[Hadoop和java版本对应关系](https://cwiki.apache.org/confluence/display/hadoop/HadoopJavaVersions)

## 1.1 解压

```
# -C 安装到指定目录  --strip-components 跳过压缩包中的第1层目录
tar -zxvf hadoop-2.8.3.tar.gz
```

## 1.2 配置

1. JAVA环境
* 文件：etc/hadoop/hadoop-env.sh
```
#默认使用环境变量JAVA_HOME
 export JAVA_HOME=/usr/java/latest
```
2. HADOOP配置目录
* 文件：etc/hadoop/hadoop-env.sh
```
#环境变量存在使用HADOOP_CONF_DIR，否则使用{安装目录下}/etc/hadoop
 export HADOOP_CONF_DIR=/usr/java/latest
```
3. HADOOP日志
* 文件：etc/hadoop/hadoop-env.sh
```
#环境变量存在使用HADOOP_LOG_DIR，否则使用$HADOOP_HOME/logs
 export HADOOP_LOG_DIR=/usr/java/latest
```
4.修改环境变量
在目录`/etc/profile.d`新建`hadoop-2.8.3.sh`文件
```
export HADOOP_HOME=/sdg1/hadoop-2.8.3
export HADOOP_PREFIX=$HADOOP_HOME
export HADOOP_CONF_DIR=$HADOOP_PREFIX/etc/hadoop

export LD_LIBRARY_PATH=$HADOOP_PREFIX/lib/native
export PATH=$HADOOP_HOME/bin:$HADOOP_HOME/sbin:$PATH
```


## 1.3 Local模式

```
mkdir input
cp etc/hadoop/*.xml input
bin/hadoop jar share/hadoop/mapreduce/hadoop-mapreduce-examples-2.8.3.jar grep input output 'dfs[a-z.]+'
cat output/*
```

## 1.4 伪分布式

### 1.4.1 配置
* 文件：`etc/hadoop/core-site.xml`
```
    <property>
        <name>fs.defaultFS</name>
        <value>hdfs://localhost:9000</value>
        <description>默认文件系统的名称。通常指定namenode的地址</description>
    </property>
```
* 文件：`etc/hadoop/hdfs-site.xml`
```
    <property>
        <name>dfs.replication</name>
        <value>1</value>
        <description>数据块备份量</description>
    </property>
```


### 1.4.2 本地运行MapReduce作业

1. 格式化文件系统 `
bin/hdfs namenode -format`
2. 启动NameNode和DataNode `sbin/start-dfs.sh`
3. 访问`http://172.16.32.38:50070`
4. 执行MapReduce作业
```
bin/hdfs dfs -mkdir /user
bin/hdfs dfs -mkdir /user/root
bin/hdfs dfs -put etc/hadoop input
bin/hadoop jar share/hadoop/mapreduce/hadoop-mapreduce-examples-2.8.3.jar grep input output 'dfs[a-z.]+'

bin/hdfs dfs -get output output
cat output/*
# 和上边2行效果一样
bin/hdfs dfs -cat output/*
```
5. 停止 `sbin/stop-dfs.sh`
### 1.4.3 Yarn运行MapReduce作业
1. 配置`etc/hadoop/mapred-site.xml`

创建文件`cp etc/hadoop/mapred-site.xml.template etc/hadoop/mapred-site.xml`
```
    <property>
        <name>mapreduce.framework.name</name>
        <value>yarn</value>
    </property>
```
2. 配置`etc/hadoop/yarn-site.xml`
```
    <property>
        <name>yarn.nodemanager.aux-services</name>
        <value>mapreduce_shuffle</value>
    </property>
```
3. 启动 `sbin/start-yarn.sh`
4. 访问 `http://localhost:8088/`
5. 运行MapReduce作业（同1.4.2）
```
# 运行之前删除 上次运行的输出目录
bin/hdfs dfs -rm -r -f /user/root/output
```
6. 停止 `sbin/stop-yarn.sh`
## 1.5 完全分布式

[core-default.xml](https://hadoop.apache.org/docs/r2.8.3/hadoop-project-dist/hadoop-common/core-default.xml)

[hdfs-default.xml](https://hadoop.apache.org/docs/r2.8.3/hadoop-project-dist/hadoop-hdfs/hdfs-default.xml)

[mapred-default.xml](https://hadoop.apache.org/docs/r2.8.3/hadoop-mapreduce-client/hadoop-mapreduce-client-core/mapred-default.xml)

[yarn-default.xml](https://hadoop.apache.org/docs/r2.8.3/hadoop-yarn/hadoop-yarn-common/yarn-default.xml)

### 1.5.1 配置`core-site.xml`
```
    <!-- 默认文件系统的名称。通常指定namenode的地址 -->
    <property>
        <name>fs.defaultFS</name>
        <value>hdfs://master:9000</value>
    </property>
    <!-- 在序列文件中使用的缓冲区大小。这个缓冲区的大小应该是页大小（英特尔x86上为4096）的倍数，它决定读写操作中缓冲了多少数据。 -->
    <property>
        <name>io.file.buffer.size</name>
        <value>131072</value>
    </property>
    <!-- 临时数据存储目录，默认/tmp/hadoop-${user.name} -->
    <property>
        <name>hadoop.tmp.dir</name>
        <value>/sdg1/data/hadoop/tmp</value>
    </property>
```
### 1.5.2 配置`hdfs-site.xml`
```
    <!-- 配置副本数 -->
    <property>
        <name>dfs.replication</name>
        <value>1</value>
    </property>
    <!-- HDFS的块大小，134217728 or 128m-->
    <property>
        <name>dfs.blocksize</name>
        <value>128m</value>
    </property>
    <!-- hdfs的元数据存储位置，逗号分隔，会复制到所有目录中，实现冗余备份 -->
    <property>
        <name>dfs.namenode.name.dir</name>
        <value>/sdg1/data/hadoop/name</value>
    </property>
    <!-- hdfs的数据存储位置 ，逗号分隔-->
    <property>
        <name>dfs.datanode.data.dir</name>
        <value>/sdg1/data/hadoop/data</value>
    </property>
    <!-- hdfs的namenode的web ui 地址 -->
    <property>
        <name>dfs.http.address</name>
        <value>master:50070</value>
    </property>
    <!-- hdfs的snn的web ui 地址 -->
    <property>
        <name>dfs.secondary.http.address</name>
        <value>master:50090</value>
    </property>
    <!-- 是否开启web操作hdfs -->
    <property>
        <name>dfs.webhdfs.enabled</name>
        <value>true</value>
    </property>
    <!-- 是否启用hdfs权限（acl） -->
    <property>
        <name>dfs.permissions</name>
        <value>false</value>
    </property>
```
### 1.5.3 配置`mapred-site.xml`
```
    <!-- 指定maoreduce运行框架 -->
    <property>
        <name>mapreduce.framework.name</name>
        <value>yarn</value>
    </property>
    <!-- 历史服务的通信地址 -->
    <property>
        <name>mapreduce.jobhistory.address</name>
        <value>master:10020</value>
    </property>
    <!-- 历史服务的web ui地址 -->
    <property>
        <name>mapreduce.jobhistory.webapp.address</name>
        <value>master:19888</value>
    </property>
```
### 1.5.4 配置`yarn-site.xml`
```
    <!--指定resourcemanager所启动的服务器主机名-->
    <property>
        <name>yarn.resourcemanager.hostname</name>
        <value>master</value>
    </property>
    <!-- 指定mapreduce的shuffle -->
    <property>
        <name>yarn.nodemanager.aux-services</name>
        <value>mapreduce_shuffle</value>
    </property>
    <!-- 指定resourcemanager的内部通讯地址 -->
    <property>
        <name>yarn.resourcemanager.address</name>
        <value>master:8032</value>
    </property>
    <!-- 指定scheduler的内部通讯地址 -->
    <property>
        <name>yarn.resourcemanager.scheduler.address</name>
        <value>master:8030</value>
    </property>
    <!-- 指定resource-tracker的内部通讯地址 -->
    <property>
        <name>yarn.resourcemanager.resource-tracker.address</name>
        <value>master:8031</value>
    </property>
    <!-- 指定resourcemanager.admin的内部通讯地址 -->
    <property>
        <name>yarn.resourcemanager.admin.address</name>
        <value>master:8033</value>
    </property>
    <!--指定resourcemanager.webapp的ui监控地址，默认${yarn.resourcemanager.hostname}:8088,建议修改，避免端口冲突--> 
    <property> 
        <name>yarn.resourcemanager.webapp.address</name>  
        <value>master:10086</value>
    </property>
    <!-- 资源管理 Scheduler类（CapacityScheduler、FairScheduler、FifoScheduler）--> 
    <property> 
        <name>yarn.resourcemanager.scheduler.class</name>      
        <value>org.apache.hadoop.yarn.server.resourcemanager.scheduler.capacity.CapacityScheduler</value>
    </property>
    <!-- 分配给每一个容器请求的最小内存限制，单位：MB --> 
    <property> 
        <name>yarn.scheduler.minimum-allocation-mb</name>  
        <value>1024</value>
    </property>
    <!-- 分配给每一个容器请求的最大内存限制，单位：MB --> 
    <property> 
        <name>yarn.scheduler.maximum-allocation-mb</name>  
        <value>8192</value>
    </property>
    <!-- 每个容器请求的最小虚拟CPU核数 --> 
    <property> 
        <name>yarn.scheduler.minimum-allocation-vcores</name>  
        <value>1</value>
    </property>
    <!-- 每个容器请求的最大虚拟CPU核数 --> 
    <property> 
        <name>yarn.scheduler.maximum-allocation-vcores</name>  
        <value>24</value>
    </property>
    <!-- 虚拟内存与物理内存之间的比例：默认2.1 --> 
    <property> 
        <name>yarn.nodemanager.vmem-pmem-ratio</name>  
        <value>3</value>
    </property>
    <!-- 应用程序尝试的最大次数：默认2 --> 
    <property> 
        <name>yarn.resourcemanager.am.max-attempts</name>  
        <value>3</value>
    </property>
```
### 1.5.5 配置`slaves`
```
slave1
slave2
slave3
```

### 1.5.6 配置`hadoop-env.sh`
```
 export JAVA_HOME=/usr/java/jdk1.8.0_191-amd64
```
### 1.5.7 配置`yarn-env.sh`
```
export JAVA_HOME=/usr/java/jdk1.8.0_191-amd64
```
### 1.5.8 配置`mapred-env.sh`
```
export JAVA_HOME=/usr/java/jdk1.8.0_191-amd64
```

### 1.5.9 命令

#### 1.5.9.1 格式化namenode

```
#第一次启动集群执行
bin/hdfs namenode -format <cluster_name>
```

#### 1.5.9.2 启动

* 启动HDFS
```
# 启动NameNode，只需在NameNode节点执行
sbin/hadoop-daemon.sh --config $HADOOP_CONF_DIR --script hdfs start namenode
# 启动DataNode,在每个DataNode节点执行
sbin/hadoop-daemons.sh --config $HADOOP_CONF_DIR --script hdfs start datanode
```
OR 
```
# 如果配置了ssh免密以及etc/hadoop/slaves,启动所有HDFS进程
sbin/start-dfs.sh
```
* 启动Yarn
```
# 启动Resourcemanager，在yarn-site.xml中属性yarn.resourcemanager.hostname指定的机器上执行
sbin/yarn-daemon.sh --config $HADOOP_CONF_DIR start resourcemanager
# 启动Nodemanager，在每一个上执行
sbin/yarn-daemons.sh --config $HADOOP_CONF_DIR start nodemanager
# 启动独立的WebAppProxy 服务
sbin/yarn-daemon.sh --config $HADOOP_CONF_DIR start proxyserver
```
OR
```
# 如果配置了ssh免密以及etc/hadoop/slaves,启动所有Yarn进程
sbin/start-yarn.sh
```
* 启动 MapReduce JobHistory服务
```
#在mapred-site.xml中指定的机器上执行
sbin/mr-jobhistory-daemon.sh --config $HADOOP_CONF_DIR start historyserver
```

#### 1.5.9.3 停止
* 停止HDFS
```
sbin/hadoop-daemon.sh --config $HADOOP_CONF_DIR --script hdfs stop namenode
sbin/hadoop-daemons.sh --config $HADOOP_CONF_DIR --script hdfs stop datanode
```
OR
```
sbin/stop-dfs.sh
```
* 停止Yarn
```
sbin/yarn-daemon.sh --config $HADOOP_CONF_DIR stop resourcemanager
sbin/yarn-daemons.sh --config $HADOOP_CONF_DIR stop nodemanager
sbin/yarn-daemon.sh --config $HADOOP_CONF_DIR stop proxyserver
```
OR
```
sbin/stop-yarn.sh
```
* 停止MapReduce JobHistory服务
```
sbin/mr-jobhistory-daemon.sh --config $HADOOP_CONF_DIR stop historyserver
```

### 1.5.10 Web UI

* NameNode [http://master:50070/](http://localhost:50070/)
* ResourceManager [http://master:8088/](http://localhost:10086/)
* MapReduce JobHistory Server ${mapreduce.jobhistory.webapp.address}
 [http://master:19888/](http://localhost:19888/)

# 2. 附件

| 属性名称 | 属性值 | 描述 |
| --- | --- | --- |
| hadoop.common.configuration.version |0.23.0| 配置文件的版本。|
|hadoop.tmp.dir|/tmp/hadoop-${user.name}|其它临时目录的父目录，会被其它临时目录用到。|
|io.native.lib.available|TRUE|是否使用本地库进行bz2和zlib的文件压缩及编解码。|
|hadoop.http.filter.initializers|org.apache.hadoop.http.lib.StaticUserWebFilter|一个逗号分隔的类名列表，他们必须继承于org.apache.hadoop.http.FilterInitializer，相应的过滤器被初始化后，将应用于所有的JSP和Servlet网页。列表的排序即为过滤器的排序。|
|hadoop.security.authorization|FALSE|是否启用service级别的授权。|
|hadoop.security.instrumentation.requires.admin|FALSE|访问servlets (JMX, METRICS, CONF, STACKS)是否需要管理员ACL(访问控制列表)的授权。|
|hadoop.security.authentication|simple|有两个选项，simple和kerberos，两个的详细区别就自己百度吧。|
|hadoop.security.group.mapping|org.apache.hadoop.security.JniBasedUnixGroupsMappingWithFallback|用于ACL用户组映射的类，默认的实现类是 org.apache.hadoop.security.JniBasedUnixGroupsMappingWithFallback，定义了JNI是否可用，如果可用，将使用hadoop中的API来实现访问用户组解析，如果不可用，将使用ShellBasedUnixGroupsMapping来实现。|
|hadoop.security.dns.interface||用于确定Kerberos登录主机的网络接口的名称。|
|hadoop.security.dns.nameserver||用于确定Kerberos登录主机的地址。|
|hadoop.security.dns.log-slow-lookups.enabled|FALSE|当查询名称时间超过阈值时是否进行记录。|
|hadoop.security.dns.log-slow-lookups.threshold.ms|1000|接上一个属性，这个属性就是设置阈值的。|
|hadoop.security.groups.cache.secs|300|配置用户组映射缓存时间的，当过期时重新获取并缓存。|
|hadoop.security.groups.negative-cache.secs|30|当无效用户频繁访问，用于设置缓存锁定时间。建议设置为较小的值，也可以通过设置为0或负数来禁用此属性。|
|hadoop.security.groups.cache.warn.after.ms|5000|当查询用户组时间超过设置的这个阈值时，则作为警告信息进行记录。||hadoop.security.groups.cache.background.reload|FALSE|是否使用后台线程池重新加载过期的用户组映射。|
|hadoop.security.groups.cache.background.reload.threads|3|接上一个属性，当上个属性为true时，通过此属性控制后台线程的数量。|
|hadoop.security.groups.shell.command.timeout|0s|设置shell等待命令执行时间，如果超时，则命令中止，如果设置为0，表示无限等待。|
|hadoop.security.group.mapping.ldap.connection.timeout.ms|60000|设置LDAP的连接超时时间，如果为0或负数，表示无限等待。|
|hadoop.security.group.mapping.ldap.read.timeout.ms|60000|设置LDAP的读取超时时间，如果为0或负数，表示无限等待。||hadoop.security.group.mapping.ldap.url||LDAP服务器的地址。|
|hadoop.security.group.mapping.ldap.ssl|FALSE|是否使用SSL连接LDAP服务器。|
|hadoop.security.group.mapping.ldap.ssl.keystore||包含SSL证书的SSL密钥文件的存储路径。|
|hadoop.security.group.mapping.ldap.ssl.keystore.password.file||包括SSL密钥文件访问密码的文件路径，如果此属性没有设置，并且hadoop.security.group.mapping.ldap.ssl.keystore.password属性也没有设置，则直接从LDAP指定文件读取密码（注意：此文件只能由运行守护进程的unix用户读取，并且应该是本地文件）。|
|hadoop.security.group.mapping.ldap.ssl.keystore.password||保存SSL密钥文件访问密码的别名，如果此属性为空，并且hadoop.security.credential.clear-text-fallback属性为true时，则通过后者获取密码。||hadoop.security.credential.clear-text-fallback|TRUE|是否将密码保存为明文。|
|hadoop.security.credential.provider.path||包含证书类型和位置的文件地址列表。|
|hadoop.security.credstore.java-keystore-provider.password-file||包含用户自定义密码的文件路径。|
|hadoop.security.group.mapping.ldap.bind.user||连接到LDAP服务器时的用户别名，如果LDAP服务器支持匿名绑定，则此属性可以为空值。|
|hadoop.security.group.mapping.ldap.bind.password.file||包含绑定用户密码的文件的路径。如果在证书提供程序中没有配置密码，并且属性hadoop.security.group.mapping.ldap.bind.password没有设置，则从文件读取密码。注意：此文件只能由运行守护进程的UNIX用户读取，并且应该是本地文件。|
|hadoop.security.group.mapping.ldap.bind.password||绑定用户的密码。此属性名用作从凭据提供程序获取密码的别名。如果无法找到密码，hadoop.security.credential.clear-text-fallback是真的，则使用此属性的值作为密码。||hadoop.security.group.mapping.ldap.base||LDAP连接时搜索的根目录。||hadoop.security.group.mapping.ldap.userbase||指定用户LDAP连接时搜索的根目录。如果不设置此属性，则使用hadoop.security.group.mapping.ldap.base属性的值。|
|hadoop.security.group.mapping.ldap.groupbase||指定用户组LDAP连接时搜索的根目录。如果不设置此属性，则使用hadoop.security.group.mapping.ldap.base属性的值。|
|hadoop.security.group.mapping.ldap.search.filter.user|(&(objectClass=user)(sAMAccountName={0}))|搜索LDAP用户时提供的额外的筛选器。|
|hadoop.security.group.mapping.ldap.search.filter.group|(objectClass=group)|搜索LDAP用户组时提供的额外的筛选器。|
|hadoop.security.group.mapping.ldap.search.attr.memberof||用户对象的属性，用于标识其组对象。|
|hadoop.security.group.mapping.ldap.search.attr.member|member|用户组对象的属性，用于标识其有哪些组成员。||hadoop.security.group.mapping.ldap.search.attr.group.name|cn|用户组对象的属性，用于标识用户组的名称。|
|hadoop.security.group.mapping.ldap.search.group.hierarchy.levels|0|当要确定用户所属的用户组时，此属性用于指定向上查找的层级数目。如果为0，则表示只查询当前用户所属的直接用户组，不再向上查找。|
|hadoop.security.group.mapping.ldap.posix.attr.uid.name|uidNumber| posixAccount的属性，用于成员分组。|
|hadoop.security.group.mapping.ldap.posix.attr.gid.name|gidNumber|posixAccount的属性，用户标识组ID。|
|hadoop.security.group.mapping.ldap.directory.search.timeout|10000| LDAP SearchControl的属性，用于在搜索和等待结果时设置最大时间限制。如果需要无限等待时间，设置为0。默认值为10秒。单位为毫秒。|
|hadoop.security.group.mapping.providers||逗号分隔的提供商名称，用于用户组映射。|
|hadoop.security.group.mapping.providers.combined|TRUE|标识提供商提供的级是否可以被组合。|
|hadoop.security.service.user.name.key||此属性用于指定RPC调用的服务主名称，适用于相同的RPC协议由多个服务器实现的情况。|
|fs.azure.user.agent.prefix|unknown|WASB提供给Azure的前缀，默认包括WASB版本、JAVA运行时版本、此属性的值等。|
|hadoop.security.uid.cache.secs|14400|控制缓存的过期时间。|
|hadoop.rpc.protection|authentication|一个逗号分隔的安全SASL连接的保护值列表。|
|hadoop.security.saslproperties.resolver.class||用于连接时解决QOP的SaslPropertiesResolver。|
|hadoop.security.sensitive-config-keys|secret$ password$ ssl.keystore.pass$ fs.s3.\*[Ss]ecret.?[Kk]ey fs.s3a.\*.server-side-encryption.key fs.azure.account.key.* credential$oauth.\*token$ hadoop.security.sensitive-config-keys|一个逗号分隔的或多行的正则表达式列表。|
|hadoop.workaround.non.threadsafe.getpwuid|TRUE|一些系统已知在调用getpwuid_r和getpwgid_r有问题，这些调用是非线程安全的。这个问题的主要表现特征是JVM崩溃。如果你的系统有这些问题，开启这个选项。默认是关闭的。|
|hadoop.kerberos.kinit.command|kinit|用于Kerberos证书的定时更新。|
|hadoop.kerberos.min.seconds.before.relogin|60|重新尝试登录Kerberos的最小时间间隔，单位为秒。|
|hadoop.security.auth_to_local||将Kerberos主体映射到本地用户名。|
|hadoop.token.files||具有Hadoop服务授权令牌的令牌缓存文件列表。|
|io.file.buffer.size|4096|在序列文件中使用的缓冲区大小。这个缓冲区的大小应该是页大小（英特尔x86上为4096）的倍数，它决定读写操作中缓冲了多少数据。|
|io.bytes.per.checksum|512|每个检验和的字节数，不能大于 io.file.buffer.size属性的值。|
|io.skip.checksum.errors|FALSE|如果为true，当读取序列文件时遇到校验和错误，则跳过条目，而不是抛出异常。|
|io.compression.codecs||一组可用于压缩/解压缩的表列表，使用逗号进行分隔。|
|io.compression.codec.bzip2.library|system-native|用于bzip2编解码的本地代码库，可以通过名称或全路径来指定该库。|
|io.serializations|org.apache.hadoop.io.serializer.WritableSerialization, org.apache.hadoop.io.serializer.avro.AvroSpecificSerialization, org.apache.hadoop.io.serializer.avro.AvroReflectSerialization|可用于获取序列化和反序列化的序列化类的列表。|
|io.seqfile.local.dir|${hadoop.tmp.dir}/io/local|存储中间数据文件的本地目录。|
|io.map.index.skip|0|跳过索引的数量。|
|io.map.index.interval|128|MapFile由两部分组成：数据文件和索引文件。在每个设置的时间间隔后，会根据写入的数据文件内容，创建索引对应的索引文件内容。|
|fs.defaultFS|file:///|默认文件系统的名称。通常指定namenode的URI地址，包括主机和端口。|
|fs.default.name|file:///|不建议使用此属性，建议用fs.defaultFS属性代替。|
|fs.trash.interval|0|检查点被删除的时间间隔，单位为分钟。此属性可以在服务器和客户端上配置。如果服务器上被禁用，则检查客户端配置，如果服务器上被启用，则忽略客户端配置。|
|fs.trash.checkpoint.interval|0|检查点之间的时间间隔，此属性的值应该小于fs.trash.interval属性的值。每次检查指针运行时，它都会创建一个新的检查点，并移除在几分钟前创建的检查点。|
|fs.protected.directories||一个逗号分隔的目录列表，即使是空的，也不能被超级用户删除。此设置可用于防止重要系统目录因管理员错误而意外删除。|
|fs.AbstractFileSystem.file.impl|org.apache.hadoop.fs.local.LocalFs|file的抽象文件类。|
|fs.AbstractFileSystem.har.impl|org.apache.hadoop.fs.HarFs|har的抽象文件类。|
|fs.AbstractFileSystem.hdfs.impl|org.apache.hadoop.fs.Hdfs|hdfs的抽象文件类。|
|fs.AbstractFileSystem.viewfs.impl|org.apache.hadoop.fs.viewfs.ViewFs|viewfs的抽象文件类。|
|fs.viewfs.rename.strategy|SAME_MOUNTPOINT|允许在多个挂载点间重命名。|
|fs.AbstractFileSystem.ftp.impl|org.apache.hadoop.fs.ftp.FtpFs|ftp的抽象文件类。|
|fs.AbstractFileSystem.webhdfs.impl|org.apache.hadoop.fs.WebHdfs|webhdfs的抽象文件类。|
|fs.AbstractFileSystem.swebhdfs.impl|org.apache.hadoop.fs.SWebHdfs|swebhdfs的抽象文件类。|
|fs.ftp.host|0.0.0.0|ftp的连接服务器。|
|fs.ftp.host.port|21|ftp的连接服务器端口。|
|fs.ftp.data.connection.mode|ACTIVE_LOCAL_DATA_CONNECTION_MODE|ftp客户端的数据连接模式，有如下选项ACTIVE_LOCAL_DATA_CONNECTION_MODE，PASSIVE_LOCAL_DATA_CONNECTION_MODE 和PASSIVE_REMOTE_DATA_CONNECTION_MODE。|
|fs.ftp.transfer.mode|BLOCK_TRANSFER_MODE|ftp的数据传输模式，有如下选项 STREAM_TRANSFER_MODE，BLOCK_TRANSFER_MODE 和COMPRESSED_TRANSFER_MODE。|
|fs.df.interval|60000|磁盘使用统计情况的刷新时间间隔。|
|fs.du.interval|600000|文件空间使用统计情况的刷新时间间隔。||fs.s3.awsAccessKeyId||S3使用的AWS访问密钥ID。|
|fs.s3.awsSecretAccessKey||S3使用的AWS密钥。|
|fs.s3.block.size|67108864|S3使用的块大小。|
|fs.s3.buffer.dir|${hadoop.tmp.dir}/s3|该目录用于发送S3前的临时本地目录。|
|fs.s3.maxRetries|4|在向应用程序发出故障之前，读取或写入文件到S3的最大重试次数。|
|fs.s3.sleepTimeSeconds|10|在每次S3重试之间的睡眠时间间隔。|
|fs.swift.impl|org.apache.hadoop.fs.swift.snative.SwiftNativeFileSystem|OpenStack Swift Filesystem的实现类。|
|fs.automatic.close|TRUE|当为true时，FileSystem的实例会在程序退出时关闭，为false时，不自动退出。|
|fs.s3n.awsAccessKeyId||S3本地文件系统使用的AWS访问密钥ID。|
|fs.s3n.awsSecretAccessKey||S3本地文件系统使用的AWS密钥。|
|fs.s3n.block.size|67108864|S3本地文件系统使用的块大小。|
|fs.s3n.multipart.uploads.enabled|FALSE|为true时，允许多个上传到本地S3。当上传一个的大小超过fs.s3n.multipart.uploads.block.size属性的大小，则将其分割成块。|
|fs.s3n.multipart.uploads.block.size|67108864|多上传到本地S3时的块大小，默认大小为64MB。|
|fs.s3n.multipart.copy.block.size|5368709120|多拷贝时的块大小，默认大小为5GB。|
|fs.s3n.server-side-encryption-algorithm||为S3指定服务器端加密算法。默认情况下未设置，而当前唯一允许的值是AES256。|
|fs.s3a.access.key||S3A文件系统使用的AWS访问密钥ID。|
|fs.s3a.secret.key||S3A文件系统使用的AWS密钥。|
|fs.s3a.aws.credentials.provider||一组com.amazonaws.auth.AWSCredentialsProvider的实现类，按照顺序加载和查询。|
|fs.s3a.session.token||当使用org.apache.hadoop.fs.s3a.TemporaryAWSCredentialsProvider时的会话令牌。|
|fs.s3a.security.credential.provider.path||hadoop.security.credential.provider.path属性的一个子集。|
|fs.s3a.connection.maximum|15|S3A的最大连接数。|
|fs.s3a.connection.ssl.enabled|TRUE|是否启用SSL连接到S3A。|
|fs.s3a.endpoint||AWS S3 连接终端。|
|fs.s3a.path.style.access|FALSE|启用S3A path style访问，即禁用默认虚拟的互联网行为。|
|fs.s3a.proxy.host||S3A连接代理的主机名。|
|fs.s3a.proxy.port||S3A连接代理的端口，如果未设置，默认为80或443。|
|fs.s3a.proxy.username||S3A连接代理的用户名。|
|fs.s3a.proxy.password||S3A连接代理的密码。|
|fs.s3a.proxy.domain||S3A连接代理的域。|
|fs.s3a.proxy.workstation||S3A连接代理的工作站。|
|fs.s3a.attempts.maximum|20|当出现错误时的最大重试次数。|
|fs.s3a.connection.establish.timeout|5000|Socket连接建立超时时间，单位为毫秒。|
|fs.s3a.connection.timeout|200000|Socket连接保持时间，单位为毫秒。|
|fs.s3a.socket.send.buffer|8192|Socket 发送缓冲大小，单位为字节。|
|fs.s3a.socket.recv.buffer|8192|Socket 接收缓冲大小，单位为字节。|
|fs.s3a.paging.maximum|5000|在读取目录列表时，从S3A同时请求的密钥最大数量。|
|fs.s3a.threads.max|10|文件请求的最大并发线程数。|
|fs.s3a.threads.keepalivetime|60|线程空间多长时间后，即终止。单位为秒。|
|fs.s3a.max.total.tasks|5|可以并发执行的操作数。|
|fs.s3a.multipart.size|100M|upload或copy操作，当文件超过多大时，即拆分。|单位可以为K/M/G/T/P。|
|fs.s3a.multipart.threshold|2147483647|upload或copy或rename操作，当文件超过多大时，即拆分。单位可以为K/M/G/T/P，不写表示字节。|
|fs.s3a.multiobjectdelete.enable|TRUE|当启用时，多个单对象的删除，被单个多对象的删除替代，以减少请求数。|
|fs.s3a.acl.default||选项有Private、PublicRead,、PublicReadWrite、 AuthenticatedRead、LogDeliveryWrite、 BucketOwnerRead、 or BucketOwnerFullControl。|
|fs.s3a.multipart.purge|FALSE|当为true时，清除多文件上传失败时的文件。|
|fs.s3a.multipart.purge.age|86400|清理多文件上传的最小秒数。|
|fs.s3a.server-side-encryption-algorithm||为S3A指定服务器端加密算法，可以为 'AES256' (for SSE-S3)、 'SSE-KMS' 或 'SSE-C'。|
|fs.s3a.server-side-encryption.key||如果 fs.s3a.server-side-encryption-algorithm属性值为'SSE-KMS' or 'SSE-C'，则使用特定的加密密钥。在SSE-C的情况下，这个属性的值应该是Base64编码的密钥，在SSE-KMS的情况下，如果该属性为空，则使用默认的S3KMS密钥，否则应将该属性设置为特定的KMS密钥ID。|
|fs.s3a.signing-algorithm||重写默认签名算法。|
|fs.s3a.block.size|32M|S3A的块大小。|
|fs.s3a.buffer.dir|${hadoop.tmp.dir}/s3a|用于缓冲上传文件的目录。|
|fs.s3a.fast.upload|FALSE|是否启用基于增量块的快速上传机制。|
|fs.s3a.fast.upload.buffer|disk|选项可以为disk/array/bytebuffer。|
|fs.s3a.fast.upload.active.blocks|4|单个输出流可以激活的最大块数。|
|fs.s3a.readahead.range|64K|在关闭和重新打开S3 HTTP连接之前在seek()提前读取的字节。|
|fs.s3a.user.agent.prefix||设置一个自定义值，作为发送到S3的HTTP请求的头部。|
|fs.s3a.metadatastore.authoritative|FALSE|当为true时，允许元数据作为真实的数据源。|
|fs.s3a.metadatastore.impl|org.apache.hadoop.fs.s3a.s3guard.NullMetadataStore|实现S3A的元数据存储类的完全限定名。|
|fs.s3a.s3guard.cli.prune.age|86400000|删除命令执行后，元数据在设定时间后被删除，单位为毫秒。|
|fs.s3a.impl|org.apache.hadoop.fs.s3a.S3AFileSystem|S3A文件系统的实现类 。|
|fs.s3a.s3guard.ddb.region||AWS DynamoDB连接域。|
|fs.s3a.s3guard.ddb.table||DynamoDB操作表名，如果此属性没有被设置，则使用S3的桶名。|
|fs.s3a.s3guard.ddb.table.create|FALSE|当为true时，S3A客户端将允许创建不存在的表。|
|fs.s3a.s3guard.ddb.table.capacity.read|500|读操作的吞吐量设置。|
|fs.s3a.s3guard.ddb.table.capacity.write|100|写操作的吞吐量设置。|
|fs.s3a.s3guard.ddb.max.retries|9|批量DynamoDB操作报错或取消前的最大重试次数。|
|fs.s3a.s3guard.ddb.background.sleep|25|批量删除时，每个删除间的时间间隔，单位为毫秒。|
|fs.AbstractFileSystem.s3a.impl|org.apache.hadoop.fs.s3a.S3A|S3A抽象文件系统的实现类。|
|fs.wasb.impl|org.apache.hadoop.fs.azure.NativeAzureFileSystem|原生Azure文件系统的实现类。|
|fs.wasbs.impl|org.apache.hadoop.fs.azure.NativeAzureFileSystem$Secure|安全原生Azure文件系统的实现类。|
|fs.azure.secure.mode|FALSE|当为true时，允许 fs.azure.NativeAzureFileSystem使用SAS密钥与Azure存储进行通信。|
|fs.azure.local.sas.key.mode|FALSE|当为true时，fs.azure.NativeAzureFileSystem使用本地SAS密钥生成，当为false，此属性无意义。|
|fs.azure.sas.expiry.period|90d|生成的SAS密钥过期时间，单位可以是ms(millis)， s(sec)， m(min)， h(hour)， d(day) 。|
|fs.azure.authorization|FALSE|当为true时，启用WASB的授权支持。|
|fs.azure.authorization.caching.enable|TRUE|当为true时，开户授权结果的缓存。|
|fs.azure.saskey.usecontainersaskeyforallaccess|TRUE|当为true时，使用容器内的SAS密钥访问blob，专用密钥无效。|
|fs.adl.impl|org.apache.hadoop.fs.adl.AdlFileSystem||
|fs.AbstractFileSystem.adl.impl|org.apache.hadoop.fs.adl.Adl||
|io.seqfile.compress.blocksize|1000000|块压缩序列文件中压缩的最小块大小。|
|io.mapfile.bloom.size|1048576|BloomMapFile中的bloom过滤器大小。|
|io.mapfile.bloom.error.rate|0.005|BloomMapFile中的bloom过滤器的假负率，默认是0.5%。|
|hadoop.util.hash.type|murmur|Hash的默认实现，有两个选项murmur和jenkins。|
|ipc.client.idlethreshold|4000|定义连接的阈值数量，之后将检查连接是否空闲。|
|ipc.client.kill.max|10|定义一次断开的客户端的最大数量。|
|ipc.client.connection.maxidletime|10000|空间连接断开时间，单位为毫秒。|
|ipc.client.connect.max.retries|10|客户端重新建立服务器连接的重试次数。|
|ipc.client.connect.retry.interval|1000|两次重新建立连接之间的时间间隔，单位为毫秒。|
|ipc.client.connect.timeout|20000|客户端通过socket连接到服务器的超时时间。|
|ipc.client.connect.max.retries.on.timeouts|45|客户端通过socket重新连接到服务器的重试次数。|
|ipc.client.tcpnodelay|TRUE|当为true时，使用TCP_NODELAY标志绕过Nagle的算法传输延迟。|
|ipc.client.low-latency|FALSE|当为true时，使用低延迟在QoS标记。|
|ipc.client.ping|TRUE|当为true时，如果读取响应超时，则向服务器发送ping命令。|
|ipc.ping.interval|60000|等待服务器响应的超时时间，单位为毫秒。当ipc.client.ping属性为true时，客户端将在不接收字节的情况下发送Ping命令。|
|ipc.client.rpc-timeout.ms|0|等待服务器响应的超时时间，单位为毫秒。当ipc.client.ping属性为true，并且这个属性的时间比 ipc.ping.interval属性的值大时，这个属性的时间将被修改为 ipc.ping.interval的最大倍数。|
|ipc.server.listen.queue.size|128|接受客户端连接的服务器的侦听队列的长度。|
|ipc.server.log.slow.rpc|FALSE|此设置有助于排除各种服务的性能问题。如果这个值设置为true，将被记录请求。|
|ipc.maximum.data.length|67108864|服务器可以接受的最大IPC消息长度（字节）。|
|ipc.maximum.response.length|134217728|服务器可以接受的最大IPC消息长度（字节）。设置为0禁用。|
|hadoop.security.impersonation.provider.class||ImpersonationProvider接口的实现类，用于授权一个用户是否可以模拟特定用户。如果未指定，则使用DefaultImpersonationProvider实现。|
|hadoop.rpc.socket.factory.class.default|org.apache.hadoop.net.StandardSocketFactory|默认使用SocketFactory，参数格式为package.FactoryClassName。|
|hadoop.rpc.socket.factory.class.ClientProtocol||连接到DFS的SocketFactory，如果为空，则使用 hadoop.rpc.socket.class.default属性的值。|
|hadoop.socks.server||SocksSocketFactory使用的SOCKS服务器的地址（主机：端口）。|
|net.topology.node.switch.mapping.impl|org.apache.hadoop.net.ScriptBasedMapping|DNSToSwitchMapping的默认实现，其调用net.topology.script.file.name属性的值来解析节点名称。|
|net.topology.impl|org.apache.hadoop.net.NetworkTopology|NetworkTopology的默认实现，它是典型的三层拓扑结构。|
|net.topology.script.file.name||该脚本被用于解析DNS的名称，例如，脚本将接收host.foo.bar，然后返回 /rack1。|
|net.topology.script.number.args|100|net.topology.script.file.name属性中参数的最大数量。|
|net.topology.table.file.name||当net.topology.node.switch.mapping.impl属性的值为 org.apache.hadoop.net.TableMapping时适用，表示一个拓扑文件。该文件格式是两列文本，列由空白分隔。第一列是DNS或IP地址，第二列指定地址映射的机架。如果没有找到对应于集群中的主机的条目，则假设默认机架。|
|file.stream-buffer-size|4096|流文件的缓冲区大小，这个大小应该是页大小的位数（X86为4096）。|
|file.bytes-per-checksum|512|每个校验和的字节数。|
|file.client-write-packet-size|65536|客户机写入的数据包大小。|
|file.blocksize|67108864|块大小。|
|file.replication|1|复制因子。|
|s3.stream-buffer-size|4096|流文件的缓冲区大小，这个大小应该是页大小的位数（X86为4096）。|
|s3.bytes-per-checksum|512|每个校验和的字节数，该数值不能大于 
s3.stream-buffer-size属性的值。|
|s3.client-write-packet-size|65536|客户机写入的数据包大小。||s3.blocksize|67108864|块大小。|
|s3.replication|3|复制因子。|
|s3native.stream-buffer-size|4096|流文件的缓冲区大小，这个大小应该是页大小的位数（X86为4096）。|
|s3native.bytes-per-checksum|512|每个校验和的字节数，该数值不能大于 s3native.stream-buffer-size属性的值。|
|s3native.client-write-packet-size|65536|客户机写入的数据包大小。|
|s3native.blocksize|67108864|块大小。|
|s3native.replication|3|复制因子。|
|ftp.stream-buffer-size|4096|流文件的缓冲区大小，这个大小应该是页大小的位数（X86为4096）。|
|ftp.bytes-per-checksum|512|每个校验和的字节数，该数值不能大于ftp.stream-buffer-size属性的值。|
|ftp.client-write-packet-size|65536|客户机写入的数据包大小。|
|ftp.blocksize|67108864|块大小。|
|ftp.replication|3|复制因子。|
|tfile.io.chunk.size|1048576|chunk大小，单位为字节，默认为1MB。|
|tfile.fs.output.buffer.size|262144|FSDataOutputStream中使用的缓冲区大小。|
|tfile.fs.input.buffer.size|262144|FSDataInputStream使用的缓冲区大小。|
|hadoop.http.authentication.type|simple|定义了Oozie HTTP终端的认证方式，支持simple和kerberos。|
|hadoop.http.authentication.token.validity|36000|验证令牌的有效时长，单位为秒。|
|hadoop.http.authentication.signature.secret.file|${user.home}/hadoop-http-auth-signature-secret|签署认证令牌的签名秘密。同样的秘密应该用于JT/NN/DN/TT配置。|
|hadoop.http.authentication.cookie.domain||用于存储身份验证令牌的HTTP Cookie域。为了授权在所有Hadoop节点Web控制台上正确工作，必须正确设置域。重要事项：当使用IP地址时，浏览器忽略具有域设置的Cookie。为了使该设置正常工作，集群中的所有节点必须配置为具有主机名的URL。|
|hadoop.http.authentication.simple.anonymous.allowed|TRUE|当使用'simple'认证时，是否允许匿名请求。|
|hadoop.http.authentication.kerberos.principal|HTTP/\_HOST@LOCALHOST|HTTP终端中使用的Kerberos principal，该principal必须以 'HTTP/'开头。|
|hadoop.http.authentication.kerberos.keytab|${user.home}/hadoop.keytab|keytab文件的位置。|
|hadoop.http.cross-origin.enabled|FALSE|是否启用cross-origin (CORS)过滤器。|
|hadoop.http.cross-origin.allowed-origins|\*|需要cross-origin (CORS)支持的web服务的来源列表，用逗号分隔。|
|hadoop.http.cross-origin.allowed-methods|GET,POST,HEAD|需要cross-origin (CORS)支持的方法列表，用逗号分隔。|
|hadoop.http.cross-origin.allowed-headers|X-Requested-With,Content-Type,Accept,Origin|需要cross-origin (CORS)支持的web服务的的头部，用逗号分隔。|
|hadoop.http.cross-origin.max-age|1800|需要cross-origin (CORS)支持的web服务缓存支持秒数。|
|dfs.ha.fencing.methods||fencing方法列表。|
|dfs.ha.fencing.ssh.connect-timeout|30000|SSH连接超时时长，单位为毫秒。|
|dfs.ha.fencing.ssh.private-key-files||SSH私钥文件。|
|hadoop.http.staticuser.user|dr.who|呈现内容时在静态Web筛选器上进行过滤的用户名，比如在HDFS web UI中的过滤。|
|ha.zookeeper.quorum||ZooKeeper服务器地址列表，用逗号分隔，可以被ZKFailoverController用于自动故障转移。|
|ha.zookeeper.session-timeout.ms|5000|ZKFC连接到ZooKeeper的超时时长，将该值设置为较低的值意味着服务器崩溃将被更快地检测到，但在瞬态错误或网络错误的情况下，就会使故障转移过于激进。|
|ha.zookeeper.parent-znode|/hadoop-ha|ZKFC下的存储信息的znode。|
|ha.zookeeper.acl|world:anyone:rwcda|znode使用的ZooKeeper ACL列表，用逗号分隔。格式同ZooKeeper CLI。如果ACL本身包含秘密，那么您可以指定一个文件的路径，用“@”符号前缀，并且该配置的值将从内部加载。|
|ha.zookeeper.auth||连接到ZooKeeper时，将该列表加入到认证列表，此列表用逗号分隔。|
|hadoop.ssl.keystores.factory.class|org.apache.hadoop.security.ssl.FileBasedKeyStoresFactory|用于检索证书的密钥存储工厂。|
|hadoop.ssl.require.client.cert|FALSE|是否需要客户端证书。|
|hadoop.ssl.hostname.verifier|DEFAULT|提供HttpsURL连接主机名验证器。有以下选项：DEFAULT， STRICT， STRICT_IE6，DEFAULT_AND_LOCALHOST 和 ALLOW_ALL。|
|hadoop.ssl.server.conf|ssl-server.xml|提取SSL服务器密钥存储信息的资源文件，这个文件通过在classpath中查询。默认为hadoop下的conf/ 目录。|
|hadoop.ssl.client.conf|ssl-client.xml|提取SSL客户端密钥存储信息的资源文件，这个文件通过在classpath中查询。默认为hadoop下的conf/ 目录。|
|hadoop.ssl.enabled|FALSE|不建议使用，建议用dfs.http.policy and yarn.http.policy代替。|
|hadoop.ssl.enabled.protocols|TLSv1,SSLv2Hello,TLSv1.1,TLSv1.2|支持的SSL协议列表。|
|hadoop.jetty.logs.serve.aliases|TRUE|对于jetty的服务是否启用别名。|
|fs.permissions.umask-mode|22|创建文件或目录时的umask。例如"022" (符号表示就是 u=rwx,g=r-x,o=r-x )，或者 "u=rwx,g=rwx,o=" (用八进制表示就是007)。|
|ha.health-monitor.connect-retry-interval.ms|1000|重试连接到服务的频率。|
|ha.health-monitor.check-interval.ms|1000|多久检查一次服务。|
|ha.health-monitor.sleep-after-disconnect.ms|1000|在异常RPC错误之后，休眠多长时间。|
|ha.health-monitor.rpc-timeout.ms|45000|实际 monitorHealth() 调用超时时间。|
|ha.failover-controller.new-active.rpc-timeout.ms|60000|FC等待新任务的超时时间，在设置时间内有新任务，即重新进入激活状态。|
|ha.failover-controller.graceful-fence.rpc-timeout.ms|5000|FC等待旧任务的超时时间，然后进入待机。|
|ha.failover-controller.graceful-fence.connection.retries|1|graceful fencing中FC连接的重试次数。|
|ha.failover-controller.cli-check.rpc-timeout.ms|20000|CLI (manual) FC等待monitorHealth, getServiceState的超时时间。|
|ipc.client.fallback-to-simple-auth-allowed|FALSE|当客户端被配置为尝试安全连接，但尝试连接到不安全的服务器时，该服务器可以指示客户端切换到SASL SIMPLE（非安全）认证。此设置控制客户端是否将接受来自服务器的此指令。当FALSE（默认）时，客户端将不允许退回到简单的身份验证，并将中止连接。|
|fs.client.resolve.remote.symlinks|TRUE|在访问远程Hadoop文件系统时，是否解析符号连接。当为false时，如果遇到符号连接，则触发异常。此设置对于本地文件系统不适用，对于本地文件系统，会自动解析符号连接。|
|nfs.exports.allowed.hosts|* rw|默认情况下，所有客户端都可以导出。该属性的值包含机构号和访问权限，由空格分隔。机器名称的格式可以是一个单一的主机，一个java正则表达式，或一个IPv4地址。访问特权使用RW或RO来指定机器的读/写权限。如果未提供访问特权，则默认为只读。条目由“；”分隔。例如：“192.1680.0/22RW；主机。*.Stase\.com；Hoo1.Test.Org Ro；”。只有更新了NFS网关之后，才能重新启动该属性。|
|hadoop.user.group.static.mapping.overrides|dr.who=;|用户到组的静态映射。如果指定的用户在系统中可用，则这将覆盖组。换句话说，这些用户不会出现组查找，而是使用在这个配置中映射的组。映射应采用这种格式。USER1＝GROMP1，GROP2；USER2=；USER3= GROP2；默认“DR.WH=”将考虑“D.WHO”作为没有组的用户。|
|rpc.metrics.quantile.enable|FALSE|当为true，并且rpc.metrics.percentiles.intervals属性为一组逗号分隔的度量时，将在百分位50/75/90/95/99时，加入rpc metrics。|
|rpc.metrics.percentiles.intervals||接上一属性，和rpc.metrics.quantile.enable配合使用。|
|hadoop.security.crypto.codec.classes.EXAMPLECIPHERSUITE||对于给定的加密编解码器的前缀，包含一个逗号分隔的给定密码编解码器（例如EXAMPLECIPHERSUITE）的实现类。如果可用的话，第一个实现将被使用，其他的则是回退。|
|hadoop.security.crypto.codec.classes.aes.ctr.nopadding|org.apache.hadoop.crypto.OpensslAesCtrCryptoCodec, org.apache.hadoop.crypto.JceAesCtrCryptoCodec|AES/CTR/NopAudio的加密编解码器实现类，用逗号分隔。如果可用的话，第一个实现将被使用，其他的则是回退。|
|hadoop.security.crypto.cipher.suite|AES/CTR/NoPadding|用于加密编解码器的密码套件。|
|hadoop.security.crypto.jce.provider||CryptoCodec中使用的JCE提供程序名称。|
|hadoop.security.crypto.buffer.size|8192|CryptoInputStream和CryptoOutputStream使用的缓冲区大小。|
|hadoop.security.java.secure.random.algorithm|SHA1PRNG|java安全随机算法。|
|hadoop.security.secure.random.impl||安全随机的实现。|
|hadoop.security.random.device.file.path|/dev/urandom|OS安全随机设备文件路径。|
|hadoop.security.key.provider.path||在管理区域密钥时使用的密钥提供程序。对于HDFS客户端，提供程序路径将与NAMENODE的提供程序路径相同。|
|fs.har.impl.disable.cache|TRUE|当为true时，不缓存“HAR”文件系统实例。|
hadoop.security.kms.client.authentication.retry-count|1|在认证失败时重试连接到KMS的次数。|
|hadoop.security.kms.client.encrypted.key.cache.size|500|EncryptedKeyVersion缓存队列的大小。|
|hadoop.security.kms.client.encrypted.key.cache.low-watermark|0.3f|如果EncryptedKeyVersion缓存队列大小低于watermark，队列将被重新调度填充。|
|hadoop.security.kms.client.encrypted.key.cache.num.refill.threads|2|重新填充EncryptedKeyVersion缓存队列的线程数。|
|hadoop.security.kms.client.encrypted.key.cache.expiry|43200000|密钥过期时间，默认为12小时。|
|hadoop.security.kms.client.timeout|60|KMS连接超时时间。|
|hadoop.security.kms.client.failover.sleep.base.millis|100|在故障转移尝试之间以指数形式增加时长，这是迄今为止尝试的数目的函数，具有+/- 50%的随机因子。此选项指定在故障转移计算中使用的基值。第一次故障转移将立即重试。第二次故障转移尝试将延迟至少hadoop.security.client.failover.sleep.base.millis属性的值之后……单位为毫秒。|
|hadoop.security.kms.client.failover.sleep.max.millis|2000|在故障转移尝试之间以指数形式增加时长，这是迄今为止尝试的数目的函数，具有+/- 50%的随机因子。此选项指定在故障转移之间等待的最大值。具体来说，两个故障转移尝试之间的时间将不超过 hadoop.security.client.failover.sleep.max.millis属性的值，单位为毫秒。|
|ipc.server.max.connections|0|服务器接受的最大并发连接数。|
|hadoop.registry.rm.enabled|FALSE|是否在YARN Resource Manager中启用注册表。|
|hadoop.registry.zk.root|/registry|注册表的根zookeeper节点。|
|hadoop.registry.zk.session.timeout.ms|60000|Zookeeper会话超时时间，单位为毫秒。|
|hadoop.registry.zk.connection.timeout.ms|15000|Zookeeper连接超时时间，单位为毫秒。|
|hadoop.registry.zk.retry.times|5|Zookeeper连接重试最大次数。|
|hadoop.registry.zk.retry.interval.ms|1000|Zookeeper连接重试间隔。|
|hadoop.registry.zk.retry.ceiling.ms|60000|Zookeeper重试的时长限制，单位为毫秒。|
|hadoop.registry.zk.quorum|localhost:2181|绑定注册表的zookeeper的主机名列表。|
|hadoop.registry.secure|FALSE|注册表是否是安全的。|
|hadoop.registry.system.acls|sasl:yarn@, sasl:mapred@, sasl:hdfs@|可以安全访问注册表的 zookeeper ACL列表。|
|hadoop.registry.kerberos.realm||Kerberos域。|
|hadoop.registry.jaas.context|Client|定义 JAAS上下文的密钥，用于安全模式中。|
|hadoop.shell.missing.defaultFs.warning|FALSE|如果fs.defaultFS属性未设置，则在hdfs中启用shell命令打印警告信息。|
|hadoop.shell.safely.delete.limit.num.files|100|使用hadoop fs -rm的-safe选项，以避免意外删除大目录。 当启用时，如果要删除的文件数量大于该限制，则-RM命令需要确认。默认的限制是100个文件。如果限制为0或在-RM命令中未指定安全性，则禁用警告。|
|fs.client.htrace.sampler.classes||hadoop文件系统客户端使用的HTrace Samplers类名。|
|hadoop.htrace.span.receiver.classes||hadoop中使用的Span Receivers类名。|
|hadoop.http.logs.enabled|TRUE|当为true时，启用hadoop守护进程上的/logs终端。|
|fs.client.resolve.topology.enabled|FALSE|是否使用net.topology.node.switch.mapping.impl属性的值来计算客户端到远程机器之间的网络距离。|
|fs.adl.impl|org.apache.hadoop.fs.adl.AdlFileSystem||
|fs.AbstractFileSystem.adl.impl|org.apache.hadoop.fs.adl.Adl||
|adl.feature.ownerandgroup.enableupn|FALSE|为了获得最佳性能，建议使用FALSE。|
|fs.adl.oauth2.access.token.provider.type|ClientCredential|定义了Azure Active Directory OAuth2访问令牌提供程序类型。|
|fs.adl.oauth2.client.id||OAuth2客户端ID。|
|fs.adl.oauth2.credential||OAuth2访问密钥。|
|fs.adl.oauth2.refresh.url||OAuth2令牌终端。|
|fs.adl.oauth2.refresh.token||OAuth2刷新令牌。|
|fs.adl.oauth2.access.token.provider||OAuth2访问令牌提供程序的类名。|
|fs.adl.oauth2.msi.port||MSI令牌服务的本地端口，端口是在创建Azure VM时被指定的。如果未被指定，则用默认的50342。|
|fs.adl.oauth2.devicecode.clientapp.id||ADD本地app的ID。|
|hadoop.caller.context.enabled|FALSE|当为true时，附加的内容会被写入到namenode的log。|
|hadoop.caller.context.max.size|128|调用内容的最大字节数。|
|hadoop.caller.context.signature.max.size|40|服务器中允许签名的最大字节。|
|seq.io.sort.mb|100|当使用SequenceFile.Sorter时，可以用于排序的缓冲区总大小。单位为兆字节。默认情况下，每个合并流为1MB。|
|seq.io.sort.factor|100|当使用SequenceFile.Sorter时，允许同时合并的流数量。|
|hadoop.zk.address||ZooKeeper服务器地址。|
|hadoop.zk.num-retries|1000|尝试连接到ZooKeeper的数量。|
|hadoop.zk.retry-interval-ms|1000|连接到ZooKeeper的重试时间间隔，单位为毫秒。|
|hadoop.zk.timeout-ms|10000|ZooKeeper会话超时时间，单位为毫秒。|
|hadoop.zk.acl|world:anyone:rwcda|用于ZooKeeper znode的ACL。|
|hadoop.zk.auth||为hadoop.zk.acl属性中的ACL指定认证方式。|

