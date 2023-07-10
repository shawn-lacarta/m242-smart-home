package ch.alptbz.mqtttelegramdemo;

import ch.alptbz.mqtttelegramdemo.handlers.RoomLightsHandler;
import ch.alptbz.mqtttelegramdemo.mqtt.MqttConsumerInterface;
import ch.alptbz.mqtttelegramdemo.scheduler.GlobalScheduler;
import ch.alptbz.mqtttelegramdemo.scheduler.RecurringTaskInterface;
import ch.alptbz.mqtttelegramdemo.telegram.TelegramConsumerInterface;
import ch.alptbz.mqtttelegramdemo.telegram.TelegramNotificationBot;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;

import static ch.alptbz.mqtttelegramdemo.mqtt.MQTTClient.getMqttClient;

public class Main {
    private static TelegramNotificationBot tnb;
    private static java.util.logging.Logger loggerInstance;
    private static Properties configProperties;

    private static void addHandler(Object handler) throws MqttException {
        if(handler instanceof TelegramConsumerInterface) {
            tnb.addHandler((TelegramConsumerInterface)handler);
        }
        if(handler instanceof MqttConsumerInterface) {
            getMqttClient().addHandler((MqttConsumerInterface)handler);
        }
        if(handler instanceof RecurringTaskInterface) {
            GlobalScheduler.main().addTask((RecurringTaskInterface)handler);
        }
    }


    public static void main(String[] args) throws InterruptedException {
        tnb = new TelegramNotificationBot(getConfig().getProperty("telegram-apikey"));
        getLogger().info("TelegramBot started");

        try {
            addHandler(new RoomLightsHandler(tnb));
        } catch (MqttException e) {
            getLogger().log(Level.SEVERE, "Failed to init handler", e);
        }
        getLogger().info("Initialization completed");

        while(true) {
            Thread.sleep(1000);
        }
    }

    private static void initLogger() {
        ConsoleHandler ch = new ConsoleHandler();
        ch.setLevel(Level.ALL);
        java.util.logging.Logger.getGlobal().addHandler(ch);
        loggerInstance = java.util.logging.Logger.getLogger("main");
    }

    public static java.util.logging.Logger getLogger() {
        if (loggerInstance == null) {
            initLogger();
        }
        return loggerInstance;
    }

    private static void loadConfig() {
        configProperties = new Properties();
        try (FileReader fr = new FileReader("config.properties")) {
            configProperties.load(fr);
            getLogger().info("Config file loaded");
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Error loading config file", e);
        }
    }

    public static Properties getConfig() {
        if (configProperties == null) {
            loadConfig();
        }
        return configProperties;
    }
}
