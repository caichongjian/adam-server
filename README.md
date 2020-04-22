# adam-server: 从零开始学习HTTP服务器的开发(Java版)
adam-server现在还很弱小，没法兼容现代浏览器的一些较新的特性，动不动就因各种问题报个错。
建议各位小伙伴仅用作学习材料，不要用在生产环境。

## 环境准备
+ JDK 11+
+ Apache Maven (版本不明确)
+ 浏览器 (版本不明确)

## 快速开始
```bash
# clone the project
git clone https://github.com/caichongjian/adam-server.git

# enter the project directory
cd adam-server

# install dependency
mvn install

# run
cd adam-server-example/target
java -jar adam-server-example-0.1-SNAPSHOT.jar
```

## 本人的开发环境
排序不分先后。因为adam-server经常报错，提供出来方便各位小伙伴排查错误。
+ 操作系统
    + Ubuntu 18.04.2 LTS
    + Windows 10
+ IDE
    + Intellij IDEA
    + Visual Studio Code
    + Eclipse IDE
+ 客户端
    + Google Chrome 浏览器 76.0.3809.100 (请在浏览器设置里关闭预加载网页。**此版本的adam-server暂不支持预加载网页，不关闭会报错**)
    + Mozilla Firefox for Ubuntu 75.0
    + Microsoft Edge 浏览器
    + Safari 浏览器
    + Apache JMeter (请在选项中取消勾选 Use KeepAlive. **此版本的adam-server暂不支持Use KeepAlive，勾选了会报错**)
    + curl
+ JDK
    + OpenJDK 11.0.6
+ 构建工具
    + Apache Maven 3.6.0

## 鸣谢
各位小伙伴是否可以帮原作者点★Star?
+ 源码
    + 《How Tomcat Works》源代码 https://brainysoftware.com/download
    + Apache Tomcat 源代码 https://github.com/apache/tomcat
    + Spring Framework 源代码 https://github.com/spring-projects/spring-framework
    + Spring Boot 源代码 https://github.com/spring-projects/spring-boot
    + javax.servlet-api 源代码
        + https://github.com/javaee/servlet-spec
        + https://github.com/eclipse-ee4j/servlet-api
+ 类库
    + OpenJDK 11.0.6
    + javax.servlet:javax.servlet-api:4.0.1
    + org.apache.commons:commons-lang3:3.9
    + commons-collections:commons-collections:3.2.2
    + com.google.guava:guava:28.2-jre
    + com.alibaba:fastjson:1.2.66
    + org.slf4j:slf4j-api:1.7.25
    + ch.qos.logback:logback-core:1.2.3
    + ch.qos.logback:logback-classic:1.2.3
    + ch.qos.logback:logback-access:1.2.3
+ 其他
    + 《Java编程思想（第4版）》
    + 《重构:改善既有代码的设计》
    + IDE 插件
    + Git 和 GitHub
    + 翻译软件
    + 搜索引擎
    + 书籍
    + 博客
    + 文章
    + 问答
    + 搜狗拼音输入法Ubuntu版
    + 音乐
    + 计算机网络
    + 所有那些与adam-server直接相关和间接相关的、我想起来的和没想起来的、我用过和没用过的优秀软硬件与资源
    + 其他

