# 1. 环境

[https://ci.apache.org/projects/flink/flink-docs-release-1.11/zh/ops/deployment/docker.html](https://ci.apache.org/projects/flink/flink-docs-release-1.11/zh/ops/deployment/docker.html)

[https://hub.docker.com/_/flink/](https://hub.docker.com/_/flink/)


# 2.镜像

```
docker pull flink:1.11.2-scala_2.11-java8

```
# 3. 启动session cluster

```
# 创建单独的网络，可以进行内部通信(使用容器名称如jobmanager.rpc.address: jobmanager)
docker network create flink-network
# -rm容器退出自动移除容器  最后的jobmanager，启动flink的jobmanager节点
docker run -d --rm --name=jobmanager --network flink-network -p 8081:8081 --env FLINK_PROPERTIES="jobmanager.rpc.address: jobmanager" flink:1.11.2-scala_2.11-java8 jobmanager
# 最后的taskmanager，启动flink的taskmanager节点
docker run -d --rm --name=taskmanager2 --network flink-network --env FLINK_PROPERTIES="jobmanager.rpc.address: jobmanager" flink:1.11.2-scala_2.11-java8 taskmanager
```
# 4. 启动job cluster


```
docker pull flink:1.11.2-scala_2.11-java8

FLINK_PROPERTIES="jobmanager.rpc.address: jobmanager"
docker network create flink-network
docker run -d --rm --name=jobmanager --network flink-network -p 8081:8081 --env FLINK_PROPERTIES="jobmanager.rpc.address: jobmanager" flink:1.11.2-scala_2.11-java8 jobmanager

docker run -d --rm --name=taskmanager2 --network flink-network --env FLINK_PROPERTIES="jobmanager.rpc.address: jobmanager" flink:1.11.2-scala_2.11-java8 taskmanager

```