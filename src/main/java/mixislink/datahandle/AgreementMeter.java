package mixislink.datahandle;

import mixislink.service.Dlt645SendDataService;
import mixislink.utils.RabbitMq;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by WuSong
 * 2017-05-02
 * updated for 2018-06-06 by WuSong
 */
public class AgreementMeter {
    private static Logger log = Logger.getLogger(AgreementMeter.class);
    private static Properties properties = new Properties();
    private static RabbitMq rabbit;
    private static JSONObject m_data = new JSONObject();
    private final static String QUEUE_NAME = "METER_DATA";

    static {
        InputStream is = AgreementMeter.class.getClassLoader().getResourceAsStream("config.properties");
        try {
            InputStreamReader isr = new InputStreamReader(is, "GBK");
            properties.load(isr);
        } catch (IOException e) {
            log.error("加载电表解析配置文件出错",e);
        }
        //初始化数据
        init();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                rabbit = RabbitMq.init();
                m_data.put("SERIAL", Dlt645SendDataService.m_serial);
                rabbit.sendMq(m_data.toString(),QUEUE_NAME);
                log.debug("rabbitMq消息发送成功！");
            }
        };

        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(runnable,1,2, TimeUnit.MINUTES);


    }
    /** 初始化数据，1、保证如果有部分数据没取到时也能插入到数据库，2、排序,与数据库插入时的字段顺序保持一致*/
    private static void init(){
        m_data.put("SERIAL","null");
        m_data.put("POSITIVE_TOTAL",0);
        m_data.put("REVERSE_TOTAL",0);
        m_data.put("UA",0);
        m_data.put("UB",0);
        m_data.put("UC",0);
        m_data.put("IA",0);
        m_data.put("IB",0);
        m_data.put("IC",0);
        m_data.put("POWER",0);
        m_data.put("POWER_FACTOR",0);
    }

    public static void analysis(String command) throws Exception {
        //解析报文格式
        String[] newCommands = command.trim().split(" ");

        if (newCommands.length < 16 || newCommands.length > 26 || Integer.parseInt(newCommands[0]) != 68 || Integer.parseInt(newCommands[newCommands.length - 1]) != 16) {
            System.err.print("非法帧，无法解析！");

        } else {
            log.debug("报文源码：" + command);
            log.debug("帧起始符：" + newCommands[0]);
            String meter_serial = newCommands[6].concat(newCommands[5]).concat(newCommands[4]).concat(newCommands[3]).concat(newCommands[2]).concat(newCommands[1]);
            log.debug("电表地址：" + meter_serial);
            log.debug("控制域：" + newCommands[8]);
            log.debug("数据域长度：" + newCommands[9]);
            log.debug("校验码：" + newCommands[newCommands.length - 2]);
            log.debug("停止位：" + newCommands[newCommands.length - 1]);

            //取单个电表地址，组成单个对象
            String a1=Integer.toHexString(Integer.parseInt(newCommands[13],16) - 51);
            String a2=Integer.toHexString(Integer.parseInt(newCommands[12],16) - 51);
            String a3=Integer.toHexString(Integer.parseInt(newCommands[11],16) - 51);
            String a4=Integer.toHexString(Integer.parseInt(newCommands[10],16) - 51);
            String DTID0 = newCommands[13].equals("32")?newCommands[13] = "FF" :(a1.length() > 1 ? a1:"0".concat(a1));
            String DTID1 = newCommands[12].equals("32")?newCommands[12] = "FF" :(a2.length() > 1 ? a2:"0".concat(a2));
            String DTID2 = newCommands[11].equals("32")?newCommands[11] = "FF" :(a3.length() > 1 ? a3:"0".concat(a3));
            String DTID3 = newCommands[10].equals("32")?newCommands[10] = "FF" :(a4.length() > 1 ? a4:"0".concat(a4));

            String sbr = DTID0.concat(DTID1).concat(DTID2).concat(DTID3);
            log.debug(sbr);
            log.debug("数据项名称：" + properties.getProperty(sbr));

            //解析返回数据
            if (newCommands.length > 16) {
                List<String> list3 = new ArrayList<String>();
                for (int i = 0; i < Integer.parseInt(newCommands[9], 16) - 4; i++) {
                    list3.add(newCommands[newCommands.length - 3 - i]);
                }

                String[] data = list3.toArray(new String[list3.size()]);
                String num = (dataFormat(data)).toString();
                BigDecimal bigDecimal = new BigDecimal(num);

                if (DTID0.equals("02") && DTID1.equals("01") && !DTID2.equals("FF")) { //电压0.1v
                    BigDecimal UA = bigDecimal.multiply(new BigDecimal("0.1"));
                    log.debug(properties.getProperty(sbr) + "：" + UA + "v");
                    m_data.put("UA",UA.doubleValue());
                } else if (DTID0.equals("02") && DTID1.equals("02") && !DTID2.equals("FF")) { //电流0.001A
                    String isNegative = bigDecimal.toString().substring(0,1).equals("8") ? "0".concat(bigDecimal.toString().substring(1)):bigDecimal.toString();
                    BigDecimal IA = new BigDecimal(isNegative).multiply(new BigDecimal("0.001"));
                    log.debug(properties.getProperty(sbr) + "：" + IA + "A");
                    m_data.put("IA",IA.doubleValue());

                } else if ((DTID0.equals("02") && DTID1.equals("03")) || (DTID0.equals("02") && DTID1.equals("04")) || (DTID0.equals("02") && DTID1.equals("05"))) { //有无功功率0.0001kW
                    String isNegative = bigDecimal.toString().substring(0,1).equals("8") ? "0".concat(bigDecimal.toString().substring(1)):bigDecimal.toString();
                    BigDecimal POWER_TOTAL = new BigDecimal(isNegative).multiply(new BigDecimal("0.0001"));
                    log.debug(properties.getProperty(sbr) + "：" + POWER_TOTAL);
                    m_data.put("POWER",POWER_TOTAL.doubleValue());

                } else if (DTID0.equals("02") && DTID1.equals("06")) { //功率因数0.001
                    System.out.println(bigDecimal);
                    String isNegative = bigDecimal.toString().substring(0,1).equals("8") ? "0".concat(bigDecimal.toString().substring(1)):bigDecimal.toString();
                    String pf =Integer.parseInt(isNegative) > 1000 ? "1000" : isNegative;
                    BigDecimal POWER_FACTOR = new BigDecimal(pf).multiply(new BigDecimal("0.001"));

                    log.debug(properties.getProperty(sbr) + "：" + POWER_FACTOR);

                    m_data.put("POWER_FACTOR",POWER_FACTOR.doubleValue());

                } else if (DTID0.equals("00") && DTID1.equals("01")) { //正向有功总电能0.01
                    BigDecimal USE_TOTAL = bigDecimal.multiply(new BigDecimal("0.01"));
                    log.debug(properties.getProperty(sbr) + "：" + USE_TOTAL);

                    m_data.put("POSITIVE_TOTAL",USE_TOTAL.doubleValue());

                } else if (DTID0.equals("00") && DTID1.equals("02")) { //反向有功总电能0.01
                    BigDecimal USE_TOTAL = bigDecimal.multiply(new BigDecimal("0.01"));
                    log.debug(properties.getProperty(sbr) + "：" + USE_TOTAL);

                    m_data.put("REVERSE_TOTAL",USE_TOTAL.doubleValue());

                } else if (DTID0.equals("02") && DTID3.equals("02")) { //电网频率0.01
                    BigDecimal GRID_FREQUENCY = bigDecimal.multiply(new BigDecimal("0.01"));
                    log.debug(properties.getProperty(sbr) + "：" + GRID_FREQUENCY);

                    m_data.put("GRID_FREQUENCY",GRID_FREQUENCY.doubleValue());

                } else if (DTID0.equals("02") && DTID1.equals("01") && DTID2.equals("FF")) { //电压数据块

                    BigDecimal UC = new BigDecimal(String.valueOf(num).substring(0, 4)).multiply(new BigDecimal("0.1"));
                    log.debug("C相电压" + UC);
                    BigDecimal UB = new BigDecimal(String.valueOf(num).substring(4, 8)).multiply(new BigDecimal("0.1"));
                    log.debug("B相电压" + UB);
                    BigDecimal UA = new BigDecimal(String.valueOf(num).substring(8)).multiply(new BigDecimal("0.1"));
                    log.debug("A相电压" + UA);

                    m_data.put("UA",UA.doubleValue());
                    m_data.put("UB",UB.doubleValue());
                    m_data.put("UC",UC.doubleValue());

                } else if (DTID0.equals("02") && DTID1.equals("02") && DTID2.equals("FF")) { //电流数据块
                    String icString = String.valueOf(num).substring(0, 6);
                    String icisNegative = icString.substring(0,1).equals("8") ? "0".concat(icString.substring(1)):icString;
                    BigDecimal IC = new BigDecimal(icisNegative).multiply(new BigDecimal("0.001"));
                    log.debug("C相电流" + IC);

                    String ibString = String.valueOf(num).substring(6, 12);
                    String ibisNegative = ibString.substring(0,1).equals("8") ? "0".concat(ibString.substring(1)):ibString;
                    BigDecimal IB = new BigDecimal(ibisNegative).multiply(new BigDecimal("0.001"));
                    log.debug("B相电流" + IB);

                    String iaString = String.valueOf(num).substring(12);
                    String iaisNegative = iaString.substring(0,1).equals("8") ? "0".concat(iaString.substring(1)):iaString;
                    BigDecimal IA = new BigDecimal(iaisNegative).multiply(new BigDecimal("0.001"));
                    log.debug("A相电流" + IA);

                    m_data.put("IA",IA.doubleValue());
                    m_data.put("IB",IB.doubleValue());
                    m_data.put("IC",IC.doubleValue());

                } else {
                    log.error(properties.getProperty(sbr) + "：" + num+"解析时未找到合适的数据标识！");
                }

            }

        }
    }

    public static StringBuffer dataFormat(String data[]){
        StringBuffer sbr=new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            String data1=String.valueOf(Integer.parseInt(data[i].substring(0,1),16)-3);
            String data2=String.valueOf(Integer.parseInt(data[i].substring(1),16)-3);
            sbr.append(data1);
            sbr.append(data2);
        }
        return sbr;
    }

    public static void main(String[] args) throws Exception {
        analysis("68 59 50 82 12 34 00 68 91 06 33 33 39 35 33 c3 a2 16");
    }

}
