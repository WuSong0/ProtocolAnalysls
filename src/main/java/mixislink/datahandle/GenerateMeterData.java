package mixislink.datahandle;

import mixislink.utils.Utils;

/**
 * @Author WuSong
 * @Date 2018-06-06
 * @Time 11:36:39
 */
public class GenerateMeterData {

    public static String[] getSendCommand(String meter_serial){
        String type[]=new String[6];
        //type[0]="0201FF00";//电压数据块
        //type[1]="0202FF00";//电流数据块
        type[0]="02010100";//电压a
        type[1]="02020100";//电流a
        type[2]="00010000";//当前正向有功总电能
        type[3]="02030000";//有功功率
        type[4]="02060000";//功率因素
        //type[5]="02800002";//电网频率
        type[5]="00020000";//当前反向有功总电能

        String[] commands = new String[type.length];
        for(int j=0;j<type.length;j++) {
            StringBuilder sendCommand = new StringBuilder();
            //协议起始符
            sendCommand.append("68");
            //电能表地址
            sendCommand.append(meter_serial);
            //电能表结束符以及控制域还有长度
            sendCommand.append("681104");
            //command为命令类型
            String a1 = type[j].substring(0, 2);
            String a2 = type[j].substring(2, 4);
            String a3 = type[j].substring(4, 6);
            String a4 = type[j].substring(6);
            String b1 = Integer.toHexString(Integer.parseInt(a1, 16) + 51);
            String b2 = Integer.toHexString(Integer.parseInt(a2, 16) + 51);
            String b3 = Integer.toHexString(Integer.parseInt(a3, 16) + 51);
            String b4 = Integer.toHexString(Integer.parseInt(a4, 16) + 51);
            String c1 = a1.equals("FF") ? b1.substring(1) : b1;
            String c2 = a2.equals("FF") ? b2.substring(1) : b2;
            String c3 = a3.equals("FF") ? b3.substring(1) : b3;
            String c4 = a4.equals("FF") ? b4.substring(1) : b4;

            String bs = c4 + c3 + c2 + c1;
            sendCommand.append(String.valueOf(bs));

            //计算透明转发内容的校验
            String CS = "68" + meter_serial + "681104" + bs;
            String CS_data = Utils.toHexString(Utils.SumCheck(Utils.hexStringToBytes(CS), 1));
            sendCommand.append(CS_data);
            //透明转发结束符
            sendCommand.append("16");
            commands[j] = sendCommand.toString();
        }
        return commands;
    }

}
