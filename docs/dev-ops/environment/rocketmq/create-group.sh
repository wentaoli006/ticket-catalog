# 命令创建消费者组 Group ticket-catalog-group，可按需更换
docker exec -it rocketmq sh /home/app/rocketmq/bin/mqadmin updateSubGroup -n localhost:9876 -c DefaultCluster -g ticket-catalog-group
