package week11.st729217.pinpoint.nazMapfeature.repository


import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

class MqttRepository {

    // HiveMQ CREDENTIALS
    private val serverUri = "ssl://fe40f1df452e4f16ac02ffb7cf16ac58.s1.eu.hivemq.cloud:8883"
    private val mqttUsername = "nazmul"
    private val mqttPassword = "Nazmul@123"

    private val clientId = MqttClient.generateClientId()
    private var mqttClient: MqttClient? = null

    // 2. CONNECT with Mqtt Server
    suspend fun connect(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // If already connected, don't reconnect
                if (mqttClient?.isConnected == true) return@withContext true

                mqttClient = MqttClient(serverUri, clientId, MemoryPersistence())

                val options = MqttConnectOptions().apply {
                    isCleanSession = true
                    userName = mqttUsername
                    password = mqttPassword.toCharArray() // Password must be CharArray
                    connectionTimeout = 10
                    keepAliveInterval = 20
                }

                mqttClient?.connect(options)
                Log.d("MQTT", "Connected to HiveMQ Cloud!")
                true
            } catch (e: Exception) {
                Log.e("MQTT", "Connection failed: ${e.message}")
                e.printStackTrace()
                false
            }
        }
    }

    // 3. PUBLISH LOCATION
    suspend fun publishLocation(userUid: String, lat: Double, lng: Double) {
        withContext(Dispatchers.IO) {
            try {
                if (mqttClient?.isConnected == true) {

                    //dyna,ic protected topic for each specific user
                    val topic = "pinpoint/user/$userUid/location"
                    // Format: "43.65,-79.38"
                    val payload = "$lat,$lng"

                    val message = MqttMessage(payload.toByteArray())
                    message.qos = 0 // Fastest delivery

                    mqttClient?.publish(topic, message)
                    Log.d("MQTT", "Published: $payload to $topic")
                } else {
                    Log.w("MQTT", "Client not connected, cannot publish.")
                }
            } catch (e: Exception) {
                Log.e("MQTT", "Publish failed: ${e.message}")
            }
        }
    }

    // 4. Disconnect from Mqtt Server
    fun disconnect() {
        try {
            mqttClient?.disconnect()
            Log.d("MQTT", "Disconnected")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}