# 1.参考 
[https://hub.docker.com/r/wurstmeister/kafka/](https://hub.docker.com/r/wurstmeister/kafka/)

[https://www.github.com/wurstmeister/kafka-docker](https://www.github.com/wurstmeister/kafka-docker)

# 2.zookeeper镜像
```
#拉取镜像
docker pull wurstmeister/kafka
#镜像详情
docker inspect wurstmeister/kafka
```
# 3.单机

```
docker run -d -p 9092:9092 --name kafka   -e "KAFKA_ZOOKEEPER_CONNECT=172.16.193.120:2181" -e "KAFKA_ADVERTISED_HOST_NAME=172.16.193.120" --restart always  wurstmeister/kafka
# 查看运行容器
docker ps
# 访问容器
docker exec -it {容器ID} bash
```

完成后外部访问进行验证
```