package ch.alptbz.mqtttelegramdemo.mqtt;

import ch.alptbz.mqtttelegramdemo.Main;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.logging.Level;

public class MQTTClient {
  private static Mqtt mqttClientInstance;

  private MQTTClient() {}

  private static void initClient() {
    mqttClientInstance = new Mqtt(Main.getConfig().getProperty("mqtt-url"), Main.getConfig().getProperty("mqtt-client"));
    try {
      mqttClientInstance.start();
    } catch (MqttException e) {
      Main.getLogger().log(Level.SEVERE, "Failed to start MQTT Client", e);
    }
  }

  public static Mqtt getMqttClient() {
    if (mqttClientInstance == null) {
      initClient();
    }
    return mqttClientInstance;
  }

}
