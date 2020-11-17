# Oauth

![Maven](https://img.shields.io/badge/maven-v4.0.0-blue)
![License](https://img.shields.io/badge/license-MIT-green)

## 1 简介

基于spring-boot开发的安全认证框架，支持redis和jdbc等多种存储方式，配置简单，使用方便、易于扩展。

## 2 使用

### 2.1 SpringBoot集成

#### 2.1.1 导入
```xml
<dependency>
  <groupId>io.github.tunkko</groupId>
  <artifactId>security</artifactId>
  <version>1.0.0</version>
</dependency>
```

#### 2.1.2 配置
```text
oauth:
  # 令牌存储方式(redis|java)
  store-type: redis
  # 拦截路径
  paths: /**
  # 可认证路径
  include-paths: /*.htmls
  # 放行路径
  exclude-paths: /login.htmls
```
