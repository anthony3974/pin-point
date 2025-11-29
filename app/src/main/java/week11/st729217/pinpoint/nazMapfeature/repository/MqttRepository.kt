package week11.st729217.pinpoint.nazMapfeature.repository

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
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

    // --- 1. CALLBACK INTERFACE ---
    interface LocationCallback {
        fun onLocationReceived(friendUid: String, lat: Double, lng: Double)
        fun onUserDisconnect(friendUid: String) // <--- NEW: Handle removal
    }

    private var locationCallback: LocationCallback? = null

    fun setLocationCallback(callback: LocationCallback) {
        this.locationCallback = callback
    }

    // --- 2. CONNECT ---
    suspend fun connect(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                if (mqttClient?.isConnected == true) return@withContext true

                mqttClient = MqttClient(serverUri, clientId, MemoryPersistence())

                // A. Setup the Listener for incoming messages
                mqttClient?.setCallback(object : MqttCallback {
                    override fun connectionLost(cause: Throwable?) {
                        Log.w("MQTT", "Connection lost: ${cause?.message}")
                    }

                    override fun messageArrived(topic: String?, message: MqttMessage?) {
                        try {
                            if (topic != null && message != null) {
                                val payload = String(message.payload)

                                // Parse UID from Topic
                                // Format: pinpoint/user/{uid}/location
                                val topicParts = topic.split("/")

                                if (topicParts.size == 4) {
                                    val friendUid = topicParts[2]

                                    // --- LOGIC SPLIT: OFFLINE vs COORDINATES ---

                                    // Case A: The "Goodbye" Packet
                                    if (payload == "OFFLINE") {
                                        locationCallback?.onUserDisconnect(friendUid)
                                        Log.d("MQTT", "$friendUid went OFFLINE")
                                    }
                                    // Case B: Normal Location Update
                                    else {
                                        val parts = payload.split(",")
                                        if (parts.size == 2) {
                                            val lat = parts[0].toDoubleOrNull()
                                            val lng = parts[1].toDoubleOrNull()

                                            if (lat != null && lng != null) {
                                                locationCallback?.onLocationReceived(friendUid, lat, lng)
                                                Log.d("MQTT", "Received from $friendUid: $lat, $lng")
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("MQTT", "Error parsing message: ${e.message}")
                        }
                    }

                    override fun deliveryComplete(token: IMqttDeliveryToken?) {
                        // Not needed for QoS 0
                    }
                })

                // B. Connect Options
                val options = MqttConnectOptions().apply {
                    isCleanSession = true
                    userName = mqttUsername
                    password = mqttPassword.toCharArray()
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

    // --- 3. PUBLISH LOCATION (Send my data) ---
    suspend fun publishLocation(userUid: String, lat: Double, lng: Double) {
        withContext(Dispatchers.IO) {
            try {
                if (mqttClient?.isConnected == true) {
                    val topic = "pinpoint/user/$userUid/location"
                    val payload = "$lat,$lng"

                    val message = MqttMessage(payload.toByteArray())
                    message.qos = 0

                    mqttClient?.publish(topic, message)
                    // Log.d("MQTT", "Published: $payload")
                }
            } catch (e: Exception) {
                Log.e("MQTT", "Publish failed: ${e.message}")
            }
        }
    }

    // --- 4. PUBLISH OFFLINE (Send Goodbye Packet) ---
    // <--- NEW FUNCTION
    suspend fun publishOffline(userUid: String) {
        withContext(Dispatchers.IO) {
            try {
                if (mqttClient?.isConnected == true) {
                    val topic = "pinpoint/user/$userUid/location"
                    val payload = "OFFLINE" // Special keyword

                    val message = MqttMessage(payload.toByteArray())
                    message.qos = 0

                    mqttClient?.publish(topic, message)
                    Log.d("MQTT", "Sent OFFLINE packet")
                }
            } catch (e: Exception) {
                Log.e("MQTT", "Failed to send offline packet: ${e.message}")
            }
        }
    }

    // --- 5. SUBSCRIBE (Listen to a friend) ---
    fun subscribeToFriend(friendUid: String) {
        try {
            if (mqttClient?.isConnected == true) {
                val topic = "pinpoint/user/$friendUid/location"
                mqttClient?.subscribe(topic, 0)
                Log.d("MQTT", "Subscribed to: $topic")
            }
        } catch (e: Exception) {
            Log.e("MQTT", "Subscribe failed: ${e.message}")
        }
    }

    // --- 6. DISCONNECT ---
    fun disconnect() {
        try {
            mqttClient?.disconnect()
            Log.d("MQTT", "Disconnected")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}