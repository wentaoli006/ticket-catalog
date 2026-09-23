# 基础镜像
FROM eclipse-temurin:25-jre

# 维护者
LABEL maintainer="system"

# 配置
ENV PARAMS=""

# 时区
ENV TZ=PRC
# 文件 JSON 日志目录，需与 compose volume 一致，供 Promtail 采集
ENV LOGGING_FILE_PATH=/data/log
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 添加应用
ADD target/ticket-catalog-start.jar /ticket-catalog-start.jar

ENTRYPOINT ["sh","-c","java -jar $JAVA_OPTS /ticket-catalog-start.jar $PARAMS"]
