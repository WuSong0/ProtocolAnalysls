package mixislink.utils;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

/**
 * Created by Administrator on 2017/8/23 0023.
 */
public class RabbitMq {
    private String USERNAME = "test";
    private String PASSWORD = "123456";
    private String HOST = "192.168.100.100";
    private int PORT = 5672;
    private String VIRTUAL_HOST = "/";
    private ConnectionFactory factory = null;
    private Connection connection;
    private Channel channel;

    private static RabbitMq rabbit;

    private RabbitMq() {
        try {
            Properties rabbitProp = FileUtils.loadPropFile("rabbitmq.properties");
            if (!"".equals(rabbitProp.getProperty("username"))) {
                USERNAME = rabbitProp.getProperty("username");
            }
            if (!"".equals(rabbitProp.getProperty("password"))) {
                PASSWORD = rabbitProp.getProperty("password");
            }
            if (!"".equals(rabbitProp.getProperty("host"))) {
                HOST = rabbitProp.getProperty("host");
            }
            if (!"".equals(rabbitProp.getProperty("port"))) {
                PORT = Integer.parseInt(rabbitProp.getProperty("port"));
            }
            if (!"".equals(rabbitProp.getProperty("virtualhost"))) {
                VIRTUAL_HOST = rabbitProp.getProperty("virtualhost");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static RabbitMq init() {
        if (rabbit == null) {
            rabbit = new RabbitMq();
        }
        return rabbit;
    }

    /**
     * 建立rabbitMq连接
     *
     * @return
     * @throws IOException
     * @throws TimeoutException
     */
    public Connection newConnection() throws IOException, TimeoutException {
        if (factory == null) {
            factory = new ConnectionFactory();
            factory.setUsername(USERNAME);
            factory.setPassword(PASSWORD);
            factory.setHost(HOST);
            factory.setVirtualHost(VIRTUAL_HOST);
            factory.setPort(PORT);
        }
        return factory.newConnection();
    }

    private Connection getConnection() {
        if (connection == null) {
            try {
                connection = newConnection();
            } catch (IOException e) {
                e.printStackTrace();
                closeConn();
            } catch (TimeoutException e) {
                e.printStackTrace();
                closeConn();
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                closeChannl();
                closeConn();
                return null;
            }
        }
        return connection;
    }

    private Channel getChannel() {
        if (channel == null) {
            Connection conn = getConnection();
            if (conn != null) {
                try {
                    channel = conn.createChannel();
                    return channel;
                } catch (IOException e) {
                    e.printStackTrace();
                    closeChannl();
                    closeConn();
                    return null;
                } catch (Exception e) {
                    e.printStackTrace();
                    closeChannl();
                    closeConn();
                    return null;
                }
            } else {
                closeChannl();
                return null;
            }
        } else {
            return channel;
        }
    }

    public boolean sendMq(String data, String queueName) {
        channel = getChannel();
        if (channel != null) {
            try {
                channel.queueDeclare(queueName, false, false, false, null);
                channel.basicPublish("", queueName, null, data.getBytes());
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                closeChannl();
                closeConn();
            } catch (Exception e) {
                e.printStackTrace();
                closeChannl();
                closeConn();
            }
        }
        return false;
    }

    private void closeChannl() {
        if (channel != null) {
            try {
                channel.close();
                channel = null;
            } catch (IOException e) {
                e.printStackTrace();
                channel = null;
            } catch (TimeoutException e) {
                e.printStackTrace();
                channel = null;
            } catch (Exception e) {
                e.printStackTrace();
                channel = null;
            }
        }
    }

    private void closeConn() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
            } catch (IOException e) {
                e.printStackTrace();
                connection = null;
            } catch (Exception e) {
                e.printStackTrace();
                connection = null;
            }
        }
    }
}
