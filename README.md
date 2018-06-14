# ProtocolAnalysls
电能表DLT645-07的协议解析

## 通信方式
usb转485与电表接口直连

## 通信规约
DLT/645 - 2007

## 文件说明
两个dll文件是rxtx包所需要的依赖，必须有

## 注意事项
1. 单相电表不能取电压电流数据块，三相电表可以取电压电流数据块，GenerateMeterData.java代码中的注释放开即可。
2. 有的电表有4个FE唤醒字节，有的电表没有，Dlt645SendDataService.java中修改注释标注的两处代码即可。
