

#include <OneWire.h>
#include <DallasTemperature.h>


#define READ_TIMEOUT 5000

#define ONE_WIRE_BUS 52
#define BAUDRATE 57600

#define OP_GET_TEMPERATURE 1000
#define OP_INPUT_PIN 1001
#define OP_READ_DIGIT_PIN 1002
#define OP_INPUT_ANALOG_PIN 1003
#define OP_READ_ANALOG_PIN 1004

#define LOOP_INTERVAL 10
#define PACKET_TERMINATE '\n'

#define BUFFER_SIZE 512
#define SMALL_BUF_SIZE 256

#define MAGIC 31256
#define PACKET_SIZE 20

#define PIN_LENGTH 53

// Setup a oneWire instance to communicate with any OneWire device
OneWire oneWire(ONE_WIRE_BUS);

// Pass oneWire reference to DallasTemperature library
DallasTemperature sensors(&oneWire);

unsigned long prevMils = 0;
char buffer[BUFFER_SIZE];

int pinState[PIN_LENGTH];

// 20 bytes packet
struct FishPacket {
    int magic = MAGIC;
    long id;
    long clientId;
    int opCode;
    int pin;
    int pinMode;
    float data;
};

void serializePacket(FishPacket& packet, unsigned char buffer[]) {
    // Little-endian
    int index = 0;

    unsigned char *b = (unsigned char *) &packet.magic;
    buffer[index++] = *b;
    buffer[index++] = *(b+1);

    b = (unsigned char *) &packet.id;
    buffer[index++] = *(b);
    buffer[index++] = *(b+1);
    buffer[index++] = *(b+2);
    buffer[index++] = *(b+3);

    b = (unsigned char *) &packet.clientId;
    buffer[index++] = *(b);
    buffer[index++] = *(b+1);
    buffer[index++] = *(b+2);
    buffer[index++] = *(b+3);

    b = (unsigned char *) &packet.opCode;
    buffer[index++] = *b;
    buffer[index++] = *(b+1);

    b = (unsigned char *) &packet.pin;
    buffer[index++] = *b;
    buffer[index++] = *(b+1);

    b = (unsigned char *) &packet.pinMode;
    buffer[index++] = *b;
    buffer[index++] = *(b+1);

    b = (unsigned char *) &packet.data;
    buffer[index++] = *b;
    buffer[index++] = *(b+1);
    buffer[index++] = *(b+2);
    buffer[index++] = *(b+3);
}

void deSerializePacket(FishPacket& packet, unsigned char buffer[]) {
    // Little-endian
    int index = 0;
    unsigned char *b = (unsigned char *) &packet.magic;
    *(b) = buffer[index++];
    *(b+1) = buffer[index++];

    b = (unsigned char *) &packet.id;
    *(b) = buffer[index++];
    *(b+1) = buffer[index++];
    *(b+2) = buffer[index++];
    *(b+3) = buffer[index++];

    b = (unsigned char *) &packet.clientId;
    *(b) = buffer[index++];
    *(b+1) = buffer[index++];
    *(b+2) = buffer[index++];
    *(b+3) = buffer[index++];

    b = (unsigned char *) &packet.opCode;
    *(b) = buffer[index++];
    *(b+1) = buffer[index++];

    b = (unsigned char *) &packet.pin;
    *(b) = buffer[index++];
    *(b+1) = buffer[index++];

    b = (unsigned char *) &packet.pinMode;
    *(b) = buffer[index++];
    *(b+1) = buffer[index++];

    b = (unsigned char *) &packet.data;
    *(b) = buffer[index++];
    *(b+1) = buffer[index++];
    *(b+2) = buffer[index++];
    *(b+3) = buffer[index++];
}

void clearPacket(FishPacket& packet) {
    packet.id=0;
    packet.magic=0;
    packet.clientId=0;
    packet.opCode=0;
    packet.pin=0;
    packet.pinMode=0;
    packet.data=0;
}

void sendPacket(FishPacket& packet) {
    Serial1.println("Sendpacket");
    
    unsigned char buffer[PACKET_SIZE];
    memset(buffer, 0, PACKET_SIZE);
    serializePacket(packet, buffer);

    // Print buffer for debugging
    for(unsigned char i:buffer) {
        char hexBuf[3];
        sprintf(hexBuf, "%X", i);
        hexBuf[2] = 0;

        Serial1.print(hexBuf);
        Serial1.print(" ");
    }
    Serial1.println();
    
    Serial1.print("magic=");
    Serial1.print(packet.magic);
    Serial1.print(", id=");
    Serial1.print(packet.id);
    Serial1.print(", clientId=");
    Serial1.print(packet.clientId);
    Serial1.print(", opCode=");
    Serial1.print(packet.opCode);
    Serial1.print(", pin=");
    Serial1.print(packet.pin);
    Serial1.print(", pinMode=");
    Serial1.print(packet.pinMode);
    Serial1.print(", data=");
    Serial1.print(packet.data);
    Serial1.println();

    Serial.write(buffer, PACKET_SIZE);
    Serial.flush();

    Serial1.println("Sendpacket Complete");
    Serial1.println();
}

void readPacket(FishPacket& packet) {
    Serial1.println("ReadPacket");

    byte buffer[PACKET_SIZE];
    memset(buffer, 0, PACKET_SIZE);
    Serial.readBytes(buffer, PACKET_SIZE);

    // Print buffer for debugging
    for(unsigned char i:buffer) {
        char hexBuf[3];
        sprintf(hexBuf, "%X", i);
        hexBuf[2] = 0;

        Serial1.print(hexBuf);
        Serial1.print(" ");
    }
    Serial1.println();

    deSerializePacket(packet, buffer);

    Serial1.print("magic=");
    Serial1.print(packet.magic);
    Serial1.print(", id=");
    Serial1.print(packet.id);
    Serial1.print(", clientId=");
    Serial1.print(packet.clientId);
    Serial1.print(", opCode=");
    Serial1.print(packet.opCode);
    Serial1.print(", pin=");
    Serial1.print(packet.pin);
    Serial1.print(", pinMode=");
    Serial1.print(packet.pinMode);
    Serial1.print(", data=");
    Serial1.print(packet.data);
    Serial1.println();

    Serial1.println("ReadPacket complete");
    Serial1.println();
}

void setup() {
    // Clear pinState
    for(int state:pinState) {
      state = 0;
    }
  
    Serial.begin(BAUDRATE);
    Serial.setTimeout(READ_TIMEOUT);
    Serial.flush();

    Serial1.begin(BAUDRATE);
    sensors.begin();
}

void loop() {
    unsigned long currentMils = millis();

    if(currentMils - prevMils > LOOP_INTERVAL) {
        FishPacket packet;
        readPacket(packet);

        if (packet.id != 0) {
            switch (packet.opCode) {
                case OP_GET_TEMPERATURE: {
                    sensors.requestTemperatures();
                    float temperature = sensors.getTempCByIndex(0);

                    packet.data = temperature;
                    break;
                }
                case OP_INPUT_PIN: {
                    pinMode(packet.pin, packet.pinMode);
                    int value = (int) (packet.data);

                    digitalWrite(packet.pin, value);

                    if(packet.pin < PIN_LENGTH && packet.pin >= 0) {
                      pinState[packet.pin] = value;
                    }
                    
                    break;
                }
                case OP_READ_DIGIT_PIN: {
                  if(packet.pin < PIN_LENGTH && packet.pin >= 0) {
                    packet.data = pinState[packet.pin];
                  }
                  
                    break;
                }
                case OP_INPUT_ANALOG_PIN: {
                  pinMode(packet.pin,packet.pinMode);
                  int value = (int) (packet.data);
                  analogWrite(packet.pin, value);

                  if(packet.pin < PIN_LENGTH && packet.pin >= 0) {
                      pinState[packet.pin] = value;
                    }
                    
                  break;
                }
                case OP_READ_ANALOG_PIN: {
                  packet.data = analogRead(packet.pin);
                  break;
                }
            }

            // Send response
            sendPacket(packet);
        }

        // clear buffer
        clearPacket(packet);

        // Update prevMils.
        prevMils = millis();
    }
}
