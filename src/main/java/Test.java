import net.sf.json.JSONObject;

import java.awt.*;
import java.util.Iterator;

import static java.awt.BorderLayout.*;


/**
 * Created by WuSong
 * 2017-05-02
 */
public class Test {
    static final char[] HEX_CHAR_TABLE = {
            '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
    };
    public String toHexString(byte[] data){
        if(data == null || data.length == 0)
            return null;
        byte[] hex = new byte[data.length * 2];
        int index = 0;
        for(byte b : data){
            int v = b & 0xFF;
            System.out.println(v);
            hex[index++] = (byte) HEX_CHAR_TABLE[v >>> 4];
            hex[index++] = (byte) HEX_CHAR_TABLE[v & 0xF];
        }
        return new String(hex);
    }
    public static void main(String[] args) {

       /* JSONObject jsonObject = new JSONObject();
        jsonObject.put("zvdz","1");
        jsonObject.put("awefad","2");
        jsonObject.put("hfgsr","3");
        jsonObject.put("jythndg","4");
        jsonObject.put("asexc","5");
        jsonObject.put("awefad","6");
        jsonObject.put("zvdz","null");

        Iterator iterator = jsonObject.keys();
        int i = 0;
        while (iterator.hasNext()){
            String key = iterator.next().toString();
            System.out.print(key+":"+jsonObject.get(key)+" ");
        }*/
      /*  System.out.println("请输入命令");
        Scanner sc=new Scanner(System.in);
        String command=sc.nextLine().replace(",","");
        System.out.println(command);*/
      /*  List list=new ArrayList();
        for (int i = 0; i < 10; i++) {
            list.add("10 ");
        }
        System.out.println(list);
        System.out.println(list.toString().replace("\\s*","").replaceAll("\\[|\\]",""));*/
        //AgreementDemo ad=new AgreementDemo();
      /*  "fe fe 68 61 01 00 00 00 00 68 91 06 33 33 39 35 34 38 09 16 "*/
       /* try {
            ad.analysis("fe fe 68 03 00 00 12 47 11 68 11 04 33 36 34 35 19 16");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("解析出错！");
        }*/


    }
}
/*
*
原始报文：68 61 01 00 00 00 00 68 11 04 33 33 34 33 14 16
地址域:  000000000161
控制码C: 11
数据域长度: 4
帧类型：读数据-主站请求帧
数据项标识: 00010000
数据项名称： 当前正向有功总电能
原始报文：68 61 01 00 00 00 00 68 91 08 33 33 34 33 59 33 33 33 8A 16
地址域:  000000000161
控制码C: 91
数据域长度: 8
帧类型：读数据-表计正常应答帧(无后续数据)
数据项标识: 00010000
数据项名称： 当前正向有功总电能
数据项内容:
当前正向有功总电能: 000000.26 kWh
原始报文：68 61 01 00 00 00 00 68 11 04 33 34 34 35 17 16
地址域:  000000000161
控制码C: 11
数据域长度: 4
帧类型：读数据-主站请求帧
数据项标识: 02010100
数据项名称： A相电压
原始报文：68 61 01 00 00 00 00 68 91 06 33 34 34 35 34 43 10 16
地址域:  000000000161
控制码C: 91
数据域长度: 6
帧类型：读数据-表计正常应答帧(无后续数据)
数据项标识: 02010100
数据项名称： A相电压
数据项内容:
A相电压: 100.1 V
原始报文：68 61 01 00 00 00 00 68 11 04 33 34 35 35 18 16
地址域:  000000000161
控制码C: 11
数据域长度: 4
帧类型：读数据-主站请求帧
数据项标识: 02020100
数据项名称： A相电流
原始报文：68 61 01 00 00 00 00 68 91 07 33 34 35 35 CC 7C 33 16 16
地址域:  000000000161
控制码C: 91
数据域长度: 7
帧类型：读数据-表计正常应答帧(无后续数据)
数据项标识: 02020100
数据项名称： A相电流
数据项内容:
A相电流: 004.999 A
原始报文：68 61 01 00 00 00 00 68 11 04 33 33 36 35 18 16
地址域:  000000000161
控制码C: 11
数据域长度: 4
帧类型：读数据-主站请求帧
数据项标识: 02030000
数据项名称： 瞬时总有功功率
原始报文：68 61 01 00 00 00 00 68 91 07 33 33 36 35 45 58 33 6B 16
地址域:  000000000161
控制码C: 91
数据域长度: 7
帧类型：读数据-表计正常应答帧(无后续数据)
数据项标识: 02030000
数据项名称： 瞬时总有功功率
数据项内容:
瞬时总有功功率: 00.2512 kW
原始报文：68 61 01 00 00 00 00 68 11 04 33 33 37 35 19 16
地址域:  000000000161
控制码C: 11
数据域长度: 4
帧类型：读数据-主站请求帧
数据项标识: 02040000
数据项名称： 瞬时总无功功率
原始报文：68 61 01 00 00 00 00 68 91 07 33 33 37 35 64 76 33 A9 16
地址域:  000000000161
控制码C: 91
数据域长度: 7
帧类型：读数据-表计正常应答帧(无后续数据)
数据项标识: 02040000
数据项名称： 瞬时总无功功率
数据项内容:
瞬时总无功功率: 00.4331 kvar
原始报文：68 61 01 00 00 00 00 68 11 04 33 33 38 35 1A 16
地址域:  000000000161
控制码C: 11
数据域长度: 4
帧类型：读数据-主站请求帧
数据项标识: 02050000
数据项名称： 瞬时总视在功率
原始报文：68 61 01 00 00 00 00 68 91 07 33 33 38 35 39 83 33 8C 16
地址域:  000000000161
控制码C: 91
数据域长度: 7
帧类型：读数据-表计正常应答帧(无后续数据)
数据项标识: 02050000
数据项名称： 瞬时总视在功率
数据项内容:
瞬时总视在功率: 00.5006 kVA
原始报文：68 61 01 00 00 00 00 68 11 04 33 33 39 35 1B 16
地址域:  000000000161
控制码C: 11
数据域长度: 4
帧类型：读数据-主站请求帧
数据项标识: 02060000
数据项名称： 总功率因数
原始报文：68 61 01 00 00 00 00 68 91 06 33 33 39 35 34 38 09 16
地址域:  000000000161
控制码C: 91
数据域长度: 6
帧类型：读数据-表计正常应答帧(无后续数据)
数据项标识: 02060000
数据项名称： 总功率因数
数据项内容:
总功率因数: 0.501
* */