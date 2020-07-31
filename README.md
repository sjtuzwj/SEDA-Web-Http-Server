# SEDA Web Server
**Maintainer: Zhu Wenjie**
## 定义Definition
**SEDA**(Staged Event Driven Architecture) is an asynchronous and pipelined server model.  
Each Stage is composed of an event queue and a thread pool.  
A thread will handle a batch of events, and produce new events.  
SEDA is now applied in Cassandra.   
SEDA(分阶段事件驱动模型)是一个异步流水线服务器模型  
每个阶段由一个事件队列和一个线程池组成  
线程将会对事件进行批处理并制造新的事件到其他阶段   
目前Cassandra中运用了SEDA服务器模型
![avatar](./seda.jpg)

##运行QuickStart
- Run server and use your browser to visit localhost:8001 直接访问浏览器,进行http交互
- Run server and run multiple client 通过提供的client进行tcp交互

##实现Implementation
Now, my implementation simplify the controller of thread and batch, so that the thread pool size and batch size are fixed instead of dynamic.  
Main/Sub Reactor and NIO(IO multiplexing) is the IO model, which means getting request is synchronous but handling request is asynchronous and pipelined.   
Main Reactor use one selector for Accept, Sub Reactor use another for IO. To register the socket to Sub Reactor, timeout is necessary to avoid blocking.   
目前我的实现简化了论文中关于线程和批处理的控制器，单纯使用定值。  
IO模型是**主从Reactor**+多路复用，接收请求的数据是同步的，而处理请求并发送响应则是异步且流水线化的。  
使用两个Selector分别负责accept和io，为了避免main注册sub时sub被select阻塞，因此设定timeout为100ms。  
### Read Stage
读取请求并(待进行HTTP解析)转发给AppStage: 目前没有进行解析
### App Stage
根据请求参数返回相应的响应体: 目前直接返回请求本身
### Write Stage
增加响应头，填充响应体: 目前仅支持HTML格式
##演示DEMO
Now the server will response with the request as its html body.  
目前，单纯地返回整个请求在HTML Body中，更多的HTTP服务等待后续演替
![avatar](./demo.PNG)

##计划表RoadMap
- 除了目前的read write之外，accept和connect也进行异步化(目前没有connect)
- 支持控制器
- 提供对于HTTP请求头的解析
- 提供Application Stage的业务逻辑
- 更多应用层协议支持
- 提供长连接功能

##参考Reference
[System|网络|分阶段事件驱动架构SEDA](https://zhuanlan.zhihu.com/p/161902784 )   
[SEDA: an architecture for well-conditioned, scalable internet services](https://dl.acm.org/doi/abs/10.1145/502034.502057)
