import org.eclipse.paho.client.mqttv3.*;
import java.util.Scanner;

public class Main {

    private static final String BROKER = "tcp://broker.hivemq.com:1883"; // URL del broker MQTT
    private static final String CLIENT_ID = "HeladeraReceptorApp";           // Identificador único del cliente
    private static final String RECEIVE_TOPIC = "heladera/recibirMensaje";     // Topic para recibir mensajes
    private static final String SEND_TOPIC = "dds2024/g12/heladeras/apertura";           // Topic para enviar mensajes

    public static void main(String[] args) {
        MqttClient client = null;
        try {
            // Crear cliente MQTT
            client = new MqttClient(BROKER, CLIENT_ID);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);

            // Conectar al broker
            client.connect(options);
            System.out.println("Conectado al broker: " + BROKER);

            // Configurar callback para manejar los mensajes recibidos
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    System.err.println("Conexión perdida: " + cause.getMessage());
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    try {
                        System.out.println("Mensaje recibido en el topic [" + topic + "]: " + new String(message.getPayload()));
                    } catch (Exception e) {
                        System.err.println("Error al procesar el mensaje recibido: " + e.getMessage());
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    try {
                        System.out.println("Mensaje enviado correctamente. ID: " + token.getMessageId());
                    } catch (Exception e) {
                        System.err.println("Error al confirmar la entrega del mensaje: " + e.getMessage());
                    }
                }
            });

            // Suscribirse al topic para recibir mensajes
            try {
                client.subscribe(RECEIVE_TOPIC);
                System.out.println("Suscrito al topic: " + RECEIVE_TOPIC);
            } catch (MqttException e) {
                System.err.println("Error al suscribirse al topic: " + e.getMessage());
            }

            // Iniciar consola para enviar mensajes
            Scanner scanner = new Scanner(System.in);
            System.out.println("Escribe un mensaje para enviar (o 'exit' para salir):");

            while (true) {
                String input = scanner.nextLine();
                if ("exit".equalsIgnoreCase(input)) {
                    break;
                }

                try {
                    // Crear mensaje y publicarlo
                    MqttMessage message = new MqttMessage(input.getBytes());
                    message.setQos(1); // Nivel de QoS
                    client.publish(SEND_TOPIC, message);

                    // Log de mensaje enviado
                    System.out.println("Mensaje enviado al topic [" + SEND_TOPIC + "]: " + input);
                } catch (MqttException e) {
                    System.err.println("Error al publicar el mensaje: " + e.getMessage());
                }
            }

        } catch (MqttException e) {
            System.err.println("Error en la aplicación MQTT: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (client != null && client.isConnected()) {
                try {
                    client.disconnect();
                    System.out.println("Desconectado del broker. Aplicación finalizada.");
                } catch (MqttException e) {
                    System.err.println("Error al desconectar del broker: " + e.getMessage());
                }
            }
        }
    }
}
