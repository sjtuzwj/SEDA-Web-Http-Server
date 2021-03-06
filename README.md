# SEDA Web Server
**Maintainer: Zhu Wenjie**
## 定义Definition
**SEDA**(Staged framework.Util.Event Driven Architecture) is an asynchronous and pipelined server model.  
Each Stage is composed of an event queue and a thread pool.  
A thread will handle a batch of events, and produce new events.  
SEDA is now applied in Cassandra.   
SEDA(分阶段事件驱动模型)是一个异步流水线服务器模型  
每个阶段由一个事件队列和一个线程池组成  
线程将会对事件进行批处理并制造新的事件到其他阶段   
目前Cassandra中运用了SEDA服务器模型
![avatar](./seda.jpg)

## 运行QuickStart
- Run server and use your browser to visit localhost:8001 直接访问浏览器,进行http交互
- Run server and run multiple client 通过提供的client进行tcp交互

## 实现Implementation
Now, my implementation simplify the controller of thread and batch, so that the thread pool size and batch size are fixed instead of dynamic.  
Main/Sub Reactor and NIO(IO multiplexing) is the IO model, which means getting request is synchronous but handling request is asynchronous and pipelined.   
Main Reactor use one selector for Accept, Sub Reactor use another for IO. To register the socket to Sub Reactor, timeout is necessary to avoid blocking.   
目前我的实现简化了论文中关于线程和批处理的控制器，单纯使用定值。  
IO模型是**主从Reactor**+多路复用，所有IO操作以及业务逻辑均为异步且流水线化     
使用两个Selector分别负责accept和io，为了避免main注册sub时sub被select阻塞，因此设定timeout为100ms。  
### EventLoop
- Acceptable: 获取channel并注册在sub Reactor上,绑定buffer
- Readable: 停止监听Readable并发送事件给Read stage异步处理
- Writable: 停止监听Writable并发送事件给Flush Stage异步处理
- Connectable: not supported
### Read Stage
获取数据并恢复监听Readable事件，如果长度-1则进行close，close channel且cancel key,转发给Decode Stage   
### Decode Stage
读取请求并读取header的第一行，获取请求类型、url，转发给AppStage   
### App Stage
对于url进行parse，然后根据路径、请求类型到dispatcher中寻找对应的函数入口，并使用参数进行调用，并将调用结果通过事件转发给EncodeStage  
如果参数不足,则返回Parameter not Found
### Encode Stage
增加响应头，填充响应,将响应结果转发给WriteStage  
响应类型分为JSON和HTML两种
### Write Stage
将响应写入socket缓冲区，并监听writable事件,通知App Stage写入完成
### Flush Stage
刷新socket缓冲区

## 演示DEMO
Now It only support Pow and Add，by visit /pow?base={base}&index={index} and /add?left={left}&right={right}  
目前仅支持Restful格式的幂运算和加法，结果在返回的html中，非法地址将会返回错误信息。
![avatar](./demo.PNG)
![avatar](./error.PNG)


## 计划表RoadMap
- 除了目前的read write之外，accept和connect也进行异步化(目前没有connect)
- 支持Http状态机(目前默认一次收整个http包)
- 支持类似Spring的Controller自动注册(啊这，没有Bean不知道咋整)
- 更多应用层协议支持
- 提供长连接功能

## Version版本
- 0.0 TCP server with staged event driven architecture and single Reactor
- 0.1 Http server with basic Restful support, main/sub Reactor
- 0.2 Add Flush Stage with async NIO write support
- 0.3 Read->Decode->App->Encode->Write->Flush

## 参考Reference
[System|网络|分阶段事件驱动架构SEDA](https://zhuanlan.zhihu.com/p/161902784 )   
[SEDA: an architecture for well-conditioned, scalable internet services](https://dl.acm.org/doi/abs/10.1145/502034.502057)
