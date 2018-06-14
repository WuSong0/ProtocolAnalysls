package mixislink.service;

import gnu.io.*;
import mixislink.datahandle.AgreementMeter;
import mixislink.datahandle.GenerateMeterData;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by WuSong
 * 2017-05-04
 */


public class Dlt645SendDataService implements SerialPortEventListener {
    private static Logger log = Logger.getLogger(Dlt645SendDataService.class);
    private ArrayList<String> portList;
    private CommPortIdentifier portId;
    private SerialPort serialPort;
    private OutputStream outputStream;
    private InputStream inputStream;
    private int packetlength;
    private static long startTime;
    private static long startTimeTotal;
    public static String m_serial = null;
    private static Dlt645SendDataService dlt645SendDataService = null;

    static final char[] HEX_CHAR_TABLE = {
            '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
    };

    public Dlt645SendDataService(){
        this.scanPorts();
        System.out.println("串口：" + this.portList);
        this.openSerialPort(this.portList.get(0));
        //dlt645.openSerialPort("COM4");
        this.setSerialPortParam(2400,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_EVEN);

        /**取电表地址 */
        try{
            do {
                this.sendDataToSerialPort(this.hexStringToBytes("FE68AAAAAAAAAAAA681300DF16"));
                log.info("正在取电表地址...");
                Thread.currentThread().sleep(2000);
            }while (m_serial == null);
        }catch (Exception e){
            log.error("取电表地址出错！",e);
        }

    }

    public void scanPorts(){
        portList = new ArrayList<String>();
        Enumeration<?> en = CommPortIdentifier.getPortIdentifiers();
        CommPortIdentifier portId;
        while(en.hasMoreElements()){
            portId = (CommPortIdentifier)en.nextElement();
            if(portId.getPortType() == CommPortIdentifier.PORT_SERIAL){
                String name = portId.getName();
                if(!portList.contains(name)){
                    portList.add(name);
                }
            }
        }
        if(null == portList || portList.isEmpty()){
            System.out.println("未找到可用的串行端口号，程序无法启动！");
        }
    }

    public void openSerialPort(String portname){
        try{
            portId = CommPortIdentifier.getPortIdentifier(portname);
        }catch (NoSuchPortException e){
            System.out.println("抱歉，没有找到" + portname + "串行端口号！");
            return;
        }
    }

    public void setSerialPortParam(int rate,int data,int stop,int parity){
        try{
            serialPort = (SerialPort)portId.open("test",2000);
        }catch (PortInUseException e){
            System.out.println(serialPort.getName() +"端口已被占用");
            return;
        }

        try {
            serialPort.setSerialPortParams(rate,data,stop,parity);
        } catch (UnsupportedCommOperationException e) {
            e.printStackTrace();
        }
        try{
            outputStream = serialPort.getOutputStream();
            inputStream = serialPort.getInputStream();
        }catch (IOException e){
            e.printStackTrace();
        }

        try {
            serialPort.addEventListener(this);
        } catch (TooManyListenersException e) {
            e.printStackTrace();
        }

        serialPort.notifyOnDataAvailable(true);
    }

    public void sendDataToSerialPort(byte[] b){
        try {
            outputStream.write(b);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void serialEvent(SerialPortEvent e){
        switch (e.getEventType()) {
            case SerialPortEvent.BI:
            case SerialPortEvent.OE:
            case SerialPortEvent.FE:
            case SerialPortEvent.PE:
            case SerialPortEvent.CD:
            case SerialPortEvent.CTS:
            case SerialPortEvent.DSR:
            case SerialPortEvent.RI:
            case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
                break;
            case SerialPortEvent.DATA_AVAILABLE:

               // String data = toHexString(getPack());
                byte[] msgPack=this.getPack();
                log.info("**********************************************************");

                List list = new ArrayList();
                for (int i = 4; i < msgPack.length; i++) {//i=0改为i=4,为了去掉4个FE
                    //将字节转为16进制
                    list.add(Integer.toHexString(msgPack[i]<0?msgPack[i]+256:msgPack[i]));
                }
                Object[] msg=list.toArray(new Object[list.size()]);
                list=new ArrayList();
                for (int i = 0; i < msg.length; i++) {
                    //给单个字节补0
                    list.add(msg[i].toString().length()==1?"0".concat(msg[i].toString()):msg[i]);
                }

                String reciveData = list.toString().replace(",","").replaceAll("\\[|\\]","");
                log.info("返回数据:" + reciveData);
                try {
                    if (reciveData.contains("93")){
                        m_serial = reciveData.substring(3,20);//电表的地址范围，示例：68 03 00 00 00 00 00 68 93 06 36 33 33 33 33 33 A1 16
                        /** 电表地址取反*/
                        /*StringBuffer meter_serial = new StringBuffer();
                        for (int i=serial.length()-1;i >= 1; --i){
                            meter_serial.append(serial.indexOf(i-1));
                            meter_serial.append(serial.indexOf(i));
                            --i;
                        }
                        m_serial = meter_serial.toString();*/


                    }else {
                        log.debug("*************************解析结果*************************");
                        AgreementMeter.analysis(reciveData);
                        log.debug("*************************解析完成*************************");
                    }


                    //耗时测试
                   /* long endTime = System.currentTimeMillis();
                    long elapsedTime = endTime-startTimeTotal;
                    int ss = 1000;
                    int mi = ss * 60;
                    int hh = mi * 60;
                    long hour = elapsedTime/hh;
                    long minute = (elapsedTime-hour*hh)/mi;
                    long second = (elapsedTime-hour*hh-minute*mi)/ss;
                    long milliSecond = elapsedTime-hour*hh-minute*mi-second*ss;

                    log.info("###################当前是第"+ ++i +"条，执行完毕，单条耗时："+(endTime-startTime)+"ms，"+"总耗时："+
                            hour+"小时"+minute+"分"+second+"秒"+milliSecond+"ms");*/
                } catch (Exception e1) {
                    log.error("解析出错！",e1);
                }

        }
    }

    public byte[] getPack(){
            byte[] pack1 = new byte[14];//10改为14,多了4个FE
            for(int i=0;i<14;i++){
                int newData = 0;
                try{
                    if((newData = inputStream.read()) != -1){
                       // System.out.println("数据:" + newData);
                        pack1[i] = (byte) newData;
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
            }


            int packetLength=Integer.parseInt(String.valueOf(pack1[pack1.length-1]));

            byte[] pack2 = new byte[packetLength+2];

        //System.out.println(pack2.length);
            for(int i=0;i<pack2.length;i++){
                int newData = 0;
                try{
                    if((newData = inputStream.read()) != -1){
                        pack2[i] = (byte) newData;
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
            byte[] msgPack = new byte[pack1.length+pack2.length];
            System.arraycopy(pack1,0,msgPack,0,pack1.length);
            System.arraycopy(pack2,0,msgPack,pack1.length,pack2.length);

         // System.out.println("数据长度:" + msgPack.length);
            return msgPack;
        }

    public byte charToByte(char c){
        return (byte)"0123456789ABCDEF".indexOf(c);
    }

    public static void main(String[] args){
        //Dlt645SendDataService dlt645 = new Dlt645SendDataService();
        //dlt645.setPacketlength(18);

        /*for (;;){
            String[] command = mixislink.datahandle.GenerateMeterData.getSendCommand(m_serial);

            for (int i = 0; i < command.length; i++) {
                dlt645.sendDataToSerialPort(dlt645.hexStringToBytes(command[i]));
            }

            try {
                Thread.currentThread().sleep(30 * 1000);
            } catch (InterruptedException e) {
                log.error("sleep出错",e);
            }
        }*/

        Runnable runnable = () -> {
            if (dlt645SendDataService == null){
                dlt645SendDataService = new Dlt645SendDataService();
            }
            if (m_serial != null){
                String[] command = GenerateMeterData.getSendCommand(m_serial.replaceAll(" ",""));

                for (int i = 0; i < command.length; i++) {
                    dlt645SendDataService.sendDataToSerialPort(dlt645SendDataService.hexStringToBytes(command[i]));
                    log.info("发送的信息为："+command[i]);
                    try {
                        Thread.currentThread().sleep(2000);
                    } catch (InterruptedException e) {
                        log.error("发送命令时sleep失败！",e);
                    }
                }
            }else {
                log.error("电表地址为空，无法发送数据！");
            }
        };

        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(runnable,3,30, TimeUnit.SECONDS);

    }







    public byte[] hexStringToBytes(String data){
        if(data == null || "".equals(data))
            return null;
        data = data.toUpperCase();
        int length = data.length()/2;
        char[] dataChars = data.toCharArray();
        byte[] byteData = new byte[length];
        for (int i = 0;i<length;i++){
            int pos = i * 2;
            byteData[i] = (byte)(charToByte(dataChars[pos]) << 4 | charToByte(dataChars[pos + 1]));
        }
        return byteData;
    }
    public String toHexString(byte[] data){
        if(data == null || data.length == 0)
            return null;
        byte[] hex = new byte[data.length * 2];
        int index = 0;
        for(byte b : data){
            int v = b & 0xFF;
            hex[index++] = (byte) HEX_CHAR_TABLE[v >>> 4];
            hex[index++] = (byte) HEX_CHAR_TABLE[v & 0xF];
        }
        return new String(hex);
    }
    public String toHexString(Byte[] data){
        byte[] resultBytes = new byte[data.length];
        for(int i =0 ;i<data.length;i++){
            resultBytes[i] = data[i];
        }
        return toHexString(resultBytes);
    }


}
