#include "WiFiEsp.h"
#include <Servo.h>

static int OP_MOTOR = 1;

static int SERVO_PIN = 7;
Servo servo;

char ssid[] = "MarineRoom2G";     // your network SSID (name)
char pwd[] = "11161116";  // your network password

WiFiEspServer server(15324);

void setup() {
  // Servo setup
  servo.attach(SERVO_PIN);
  
  Serial.begin(115200);
  Serial2.begin(115200);
  WiFi.init(&Serial2);

  // Connect
  WiFi.begin(ssid, pwd);

  IPAddress ip = WiFi.localIP();
  Serial.print("IP Address: ");
  Serial.println(ip);

  server.begin();
  Serial.println("Waiting Client!");
}

void loop() {
  WiFiEspClient client = server.available();
  if(client) {
    while(client.connected()) {
      if(client.available()) {
        int opCode = client.read();
        int value = client.read();
        Serial.print("opCode=");
        Serial.print(opCode);
        Serial.print(" Value=");
        Serial.println(value);

        if(opCode == OP_MOTOR) {
          servo.write(value);
        }
      }
    }
  }
}
