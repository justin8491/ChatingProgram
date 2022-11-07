package client1;

import java.io.IOException;
import java.util.Properties;

public class Env {
    static Properties properties = new Properties();
    static {
        try {
            properties.load(Env.class.getResourceAsStream("env.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static int getPort() {
        return Integer.parseInt(properties.getProperty("server.port", "50001"));
    }
    
    public static String getWorkPath() {
        return properties.getProperty("work.path");
    }

    public static void main(String [] args) {
        System.out.println(getWorkPath());
    }
}
