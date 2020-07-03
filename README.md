# adam-server: 从零开始学习 HTTP 服务器的开发(Java 语言描述)

adam-server 是面向初级程序员的功能简单的 HTTP 服务器。它有以下缺点：

- 很弱小，只实现了部分的 HTTP/1.0
- 功能特性：
  - 不支持动态网页、文件上传下载、静态资源缓存、视频音频等常用功能
  - 不支持 HTTPS 协议
  - 不支持反向代理、负载均衡
  - 不支持区分 GET、PUT、POST、DELETE 等请求方式；不支持跨域请求
  - 不支持 Filter、Session
  - 不支持EJB
  - 只支持 Content-Type 为 text/html 的静态资源
  - 请求头仅支持ASCII字符，传输其他字符需要转义，且对请求头的字符大小写和空格等格式要求十分苛刻
  - 使用了 BIO
  - 不支持 Use KeepAlive
  - 吞吐量中等偏低
  - 不支持配置文件
  - 没法兼容现代浏览器的一些较新的功能(如 WebSocket 等等)
- 我一个人测试得不充分，可能会有很多功能 bug 没测出来
- 代码：
  - 注释写得不好，且代码注释中有广告
  - 代码风格仅代表我个人的喜好
  - 代码没写单元测试
  - 代码中存在大量我自己不准备实现的 TODO 注释
  - 代码中存在若干没有删除的鸡肋文件
- 不适合不加修改直接用在生产环境
- 由于可移植性差，在生产环境使用需要慎重慎重慎重:
  - 如果将来想更换为 Apache Tomcat、Eclipse Jetty、IBM WebSphere Application Server、Oracle WebLogic Server 等其他产品需要很大代价
  - 存在被爬虫爬取数据的隐患
  - 存在被恶意攻击的安全隐患
  - 没有限制请求头和请求体的长度，存在内存溢出的隐患
- 没测试过整合 Java 生态的主流的开源框架
- 如果用作学习材料，不适合初学者、高级程序员和技术专家
- 其他未知的缺点

## 环境准备

- Java 11+
- Apache Maven (版本不明确)
- 浏览器 (版本不明确)

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

排序不分先后。因为 adam-server 经常报错，提供出来方便各位小伙伴排查错误。

- 操作系统
  - Ubuntu 18.04.2 LTS
  - Windows 10
- IDE
  - Intellij IDEA
  - Visual Studio Code
  - Eclipse IDE
- 客户端
  - Google Chrome 浏览器 83.0.4103.61 (adam-server 可能与旧版本 Chrome 浏览器的预加载网页功能不兼容，我认为 Chrome 浏览器的最新版本不错，建议升级浏览器至最新版)
  - Mozilla Firefox for Ubuntu 76.0.1
  - Microsoft Edge 浏览器
  - Safari 浏览器
  - Apache JMeter (请在选项中取消勾选 Use KeepAlive. **adam-server 不支持 Use KeepAlive，勾选了会报错**)
  - curl (**adam-server 对请求头的字符大小写和空格等格式要求十分苛刻**) 请求样例: `curl -X POST -H "Content-Type: application/json" --data '{"id": "232131", "name": "adam"}' http://localhost:8888/example/json`
- JDK
  - OpenJDK 11.0.7
- 构建工具
  - Apache Maven 3.6.0

## 鸣谢

**各位小伙伴是否可以帮原作者点 ★Star?**  
(如无特殊说明则都包含相关创作者、版权方、软件平台、硬件设备、相关配套资源和竞品)

- 源码
  - 《How Tomcat Works》源代码 https://brainysoftware.com/download
  - Apache Tomcat 源代码 https://github.com/apache/tomcat
  - Spring Framework 源代码 https://github.com/spring-projects/spring-framework
  - Spring Boot 源代码 https://github.com/spring-projects/spring-boot
  - Rust 编程语言官网上提供的文档 https://doc.rust-lang.org/book/ch20-03-graceful-shutdown-and-cleanup.html
  - jakarta.servlet-api 源代码
    - https://github.com/javaee/servlet-spec
    - https://github.com/eclipse-ee4j/servlet-api
- 类库
  - OpenJDK 11.0.7
  - jakarta.servlet:jakarta.servlet-api:5.0.0-M1
  - org.apache.commons:commons-lang3:3.10
  - org.apache.commons:commons-collections4:4.4
  - com.google.guava:guava:29.0-jre
  - com.alibaba:fastjson:1.2.70
  - org.slf4j:slf4j-api:1.7.30
  - ch.qos.logback:logback-core:1.2.3
  - ch.qos.logback:logback-classic:1.2.3
  - ch.qos.logback:logback-access:1.2.3
- 其他
  - 《Java 编程思想（第 4 版）》、《重构:改善既有代码的设计》、其他书籍以及相关在线读书/阅读/销售平台
  - `#include <开发环境.h>`
  - IDE 插件
  - Maven 插件
  - Git 和 GitHub
  - 翻译软件
  - 搜索引擎
  - 互联网上的博客、论坛、问答、公众号、在线 JS 教程、教育、leetcode、百科和其他知识和学习类平台以及创作者
  - 搜狗拼音输入法 Ubuntu 版
  - 音乐、新闻、娱乐以及其他获取灵感的渠道
  - 计算机网络
  - 所有那些与 adam-server 直接相关和间接相关的、我记得的和不记得的、我用过和没用过的、免费的和收费的、开源(不区分开源协议、不区分面向初级程序员和高级程序员、不区分功能简单和复杂)的和闭源的优秀软硬件与资源
  - 其他
