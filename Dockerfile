# ==========================================
# 🐳 Dockerfile - Spring Boot 应用
# ==========================================
# Dockerfile 是构建镜像的"菜谱"
# 每一行指令都会创建一个新的"层"(layer)

# -----------------------------------------
# 阶段1: 构建阶段 (Build Stage)
# -----------------------------------------
# FROM: 指定基础镜像（相当于选择"操作系统"）
# AS builder: 给这个阶段起名叫 "builder"
# 使用支持 ARM64 (Apple Silicon) 的镜像
FROM maven:3.9-eclipse-temurin-17 AS builder

# WORKDIR: 设置工作目录（相当于 cd 到这个目录）
WORKDIR /app

# COPY: 复制文件到镜像中
# 先复制 pom.xml，利用 Docker 的缓存机制
# 如果 pom.xml 没变，下次构建就会跳过依赖下载
COPY pom.xml .

# RUN: 执行命令
# 下载所有依赖（这一层会被缓存）
RUN mvn dependency:go-offline -B

# 复制源代码
COPY src ./src

# 打包应用（跳过测试以加快构建速度）
RUN mvn clean package -DskipTests

# -----------------------------------------
# 阶段2: 运行阶段 (Runtime Stage)
# -----------------------------------------
# 使用更小的基础镜像运行应用
# 使用支持 ARM64 (Apple Silicon) 的镜像
FROM eclipse-temurin:17-jre

# 添加描述性标签
LABEL maintainer="your-email@example.com"
LABEL description="JPA Demo Spring Boot Application"

# 创建非 root 用户（安全最佳实践）
# 注意：非 Alpine 镜像使用不同的命令
RUN groupadd -r spring && useradd -r -g spring spring

# 设置工作目录
WORKDIR /app

# 从构建阶段复制打包好的 jar 文件
# --from=builder: 从名为 builder 的阶段复制
COPY --from=builder /app/target/*.jar app.jar

# 更改文件所有者
RUN chown spring:spring app.jar

# 切换到非 root 用户
USER spring

# EXPOSE: 声明容器将监听的端口（文档作用）
EXPOSE 8081

# ENTRYPOINT: 容器启动时执行的命令
# 这里启动 Spring Boot 应用
ENTRYPOINT ["java", "-jar", "app.jar"]

