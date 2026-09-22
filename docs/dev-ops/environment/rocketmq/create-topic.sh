# 命令创建主题 Topic ticket-catalog-mq，可按需更换
docker exec -it rocketmq sh /home/app/rocketmq/bin/mqadmin updateTopic -n localhost:9876 -c DefaultCluster -t ticket-catalog-mq
