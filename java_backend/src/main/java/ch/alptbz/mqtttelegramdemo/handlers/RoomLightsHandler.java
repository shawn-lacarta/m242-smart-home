package ch.alptbz.mqtttelegramdemo.handlers;

import ch.alptbz.mqtttelegramdemo.Main;
import ch.alptbz.mqtttelegramdemo.mqtt.MqttConsumerInterface;
import ch.alptbz.mqtttelegramdemo.telegram.TelegramConsumerInterface;
import ch.alptbz.mqtttelegramdemo.telegram.TelegramSenderInterface;
import com.pengrad.telegrambot.model.Update;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.logging.Level;

import static ch.alptbz.mqtttelegramdemo.Main.getConfig;
import static ch.alptbz.mqtttelegramdemo.mqtt.MQTTClient.getMqttClient;


public class RoomLightsHandler implements MqttConsumerInterface, TelegramConsumerInterface {

    private final String mqttRootTopic;
    private final String mqttRoom;
    private final String mqttColor;
    private final String mqttStatus;
    private String lastRegisteredRoom;
    private String lastRegisteredColor;
    private String lastRegisteredStatus;
    private final TelegramSenderInterface telegramSend;

    public RoomLightsHandler(TelegramSenderInterface telegramSend) {
        this.mqttRootTopic = getConfig().getProperty("mqtt-root");
        this.mqttRoom = getConfig().getProperty("mqtt-room");
        this.mqttColor = getConfig().getProperty("mqtt-color");
        this.mqttStatus = getConfig().getProperty("mqtt-status");
        this.telegramSend = telegramSend;
    }

    @Override
    public boolean acceptsTopic(String topic) {
        return (topic.startsWith(mqttRootTopic));
    }

    @Override
    public String[] subscribesTopics() {
        return new String[]{mqttRootTopic + "/#"};
    }

    @Override
    public void handleTopic(String topic, String messageStr, MqttMessage message) {
        if (topic.endsWith(mqttRoom)) {
            lastRegisteredRoom = messageStr;
        } else if (topic.endsWith(mqttColor)) {
            lastRegisteredColor = messageStr;
        } else if (topic.endsWith(mqttStatus)) {
            lastRegisteredStatus = messageStr;
        }
    }

    @Override
    public boolean acceptsCommand(String command) {
        return command.startsWith("/apartment");
    }

    @Override
    public void handleCommand(Update update, String message) {
        try {
            if (message.startsWith("/apartment-room-1")) {
                telegramSend.sendReply(update, "access to room 1...");
                getMqttClient().publish(mqttRootTopic + mqttRoom, "1");
            } else if (message.startsWith("/apartment-room-2")) {
                telegramSend.sendReply(update, "access to room 2...");
                getMqttClient().publish(mqttRootTopic + mqttRoom, "2");
            } else if (message.startsWith("/apartment-room-3")) {
                telegramSend.sendReply(update, "access to room 3...");
                getMqttClient().publish(mqttRootTopic + mqttRoom, "3");
            } else if (message.startsWith("/apartment-color-red")) {
                telegramSend.sendReply(update, "Setting color to Red");
                getMqttClient().publish(mqttRootTopic + mqttColor, "red");
            } else if (message.startsWith("/apartment-color-blue")) {
                telegramSend.sendReply(update, "Setting color to Blue");
                getMqttClient().publish(mqttRootTopic + mqttColor, "blue");
            }else if (message.startsWith("/apartment-color-white")) {
                telegramSend.sendReply(update, "Setting color to White");
                getMqttClient().publish(mqttRootTopic + mqttColor, "white");
            }else if (message.startsWith("/apartment-status-blink")) {
                telegramSend.sendReply(update, "Setting status to Blink");
                getMqttClient().publish(mqttRootTopic + mqttStatus, "blink");
            }else if (message.startsWith("/apartment-status-on")) {
                telegramSend.sendReply(update, "Setting status to On");
                getMqttClient().publish(mqttRootTopic + mqttStatus, "on");
            }else if (message.startsWith("/apartment-status-off")) {
                telegramSend.sendReply(update, "Setting status to off");
                getMqttClient().publish(mqttRootTopic + mqttStatus, "off");
            }
        } catch (MqttException e) {
            Main.getLogger().log(Level.SEVERE, "failed sending mqtt message", e);
        }
    }
}
