#include "DallasTemperature.h"
#include "Arduino.h"


#define READ_TIMEOUT 5000

#define ONE_WIRE_BUS 52
#define BAUD_RATE 57600

#define OP_GET_TEMPERATURE 1000
#define OP_INPUT_PIN 1001
#define OP_READ_DIGIT_PIN 1002
#define OP_INPUT_ANALOG_PIN 1003
#define OP_READ_ANALOG_PIN 1004

#define LOOP_INTERVAL 10

#define BUFFER_SIZE 512
#define SMALL_BUF_SIZE 256

#define MAGIC 31256
#define PACKET_SIZE_MAX 40

#define PIN_LENGTH 53

// Setup a oneWire instance to communicate with any OneWire device
OneWire oneWire(ONE_WIRE_BUS);

// Pass oneWire reference to DallasTemperature library
DallasTemperature sensors(&oneWire);

unsigned long prevMils = 0;

int pinState[PIN_LENGTH];

#define STX 0x02
#define ETX 0x03
#define DLE 0x10

/**
 * STX - DATA - ETX - CRC
 */
class FishPacket {
public:
    const unsigned char stx = STX;

    // Start Data
    long id;
    long clientId;
    int opCode;
    int pin;
    int pinMode;
    float data;
    // End Data

    const unsigned char etx = ETX;
    unsigned char crc;

    /**
     * Write value to target in little endian order.
     * Assume value is variable.
     *
     * @param sizeOfValue
     * @param value value to write to target.
     * @param target target address in buffer.
     * @return count of byte written in this function.
     */
    int write(const unsigned char* value, int sizeOfValue, unsigned char* target) {
        int idx = 0;

        for(int i=0; i<sizeOfValue; i++) {
            // Meta data should be escaped.
            switch (value[i]) {
                case STX:
                case ETX:
                case DLE:
                    // Add DLE for escape.
                    target[idx++] = DLE;
                    break;
            }

            target[idx++] = value[i];
        }

        return idx;
    }

    /**
     * Serialize this packet to byte array.
     *
     * @param bff buffer to serialize.
     * @return byte size of serialized packet.
     */
    int serializePacket(unsigned char bff[]) {
        // Little-endian
        int index = 0;
        bff[index++] = STX;

        index += write((unsigned char *)&id, 4, bff+index);
        index += write((unsigned char *)&clientId, 4, bff+index);
        index += write((unsigned char *)&opCode, 2, bff+index);
        index += write((unsigned char *)&pin, 2, bff+index);
        index += write((unsigned char *)&pinMode, 2, bff+index);
        index += write((unsigned char *)&data, 4, bff+index);

        bff[index++] = ETX;
        bff[index++] = makeCrc();

        return index;
    }

    /**
     *
     * @param value
     * @param sizeOfValue
     * @param buffer
     * @return byte size of read from buffer.
     */
    int read(unsigned char* value, int sizeOfValue, const unsigned char* buffer) {
        int idx = 0;
        int vIdx = 0;

        while(true) {
            unsigned char b = buffer[idx++];
            if(b == DLE) {
                continue;
            }

            value[vIdx++] = b;
            if(vIdx >= sizeOfValue) break;
        }
        return idx;
    }

    int deSerializePacket(unsigned char bff[]) {
        // Little-endian
        int index = 0;
        if(bff[index++] != STX) return -1;

        index += read((unsigned char *)&id, 4, bff+index);
        index += read((unsigned char *)&clientId, 4, bff+index);
        index += read((unsigned char *)&opCode, 2, bff+index);
        index += read((unsigned char *)&pin, 2, bff+index);
        index += read((unsigned char *)&pinMode, 2, bff+index);
        index += read((unsigned char *)&data, 4, bff+index);

        if(bff[index++] != ETX) return -1;
        crc = bff[index++];

        return index;
    }

    void clear() {
        id = 0;
        clientId = 0;
        opCode = 0;
        pin = 0;
        pinMode = 0;
        data = 0;
        crc = 0;
    }

    bool validateCrc() {
        return makeCrc() == crc;
    }

private:
    unsigned char makeCrc() {
        return (id + clientId + opCode + pin + pinMode + data) * 2;
    }
};

void clearPacket(FishPacket &packet) {
    packet.id = 0;
    packet.clientId = 0;
    packet.opCode = 0;
    packet.pin = 0;
    packet.pinMode = 0;
    packet.data = 0;
}

void sendPacket(FishPacket &packet) {
    unsigned char buffer[PACKET_SIZE_MAX];
    memset(buffer, 0, PACKET_SIZE_MAX);

    int size = packet.serializePacket(buffer);

    Serial.write(buffer, size);
    Serial.flush();
}

void readPacket(FishPacket &packet) {
    char firstByte = Serial.read();
    if(firstByte == STX) {
        // Packet received
        unsigned char buffer[PACKET_SIZE_MAX];
        memset(buffer, 0, PACKET_SIZE_MAX);

        int idx = 0;
        bool escaping = false;
        for(int i= 0; i<PACKET_SIZE_MAX; i++) {
            unsigned char b = Serial.read();
            if(escaping) {
                // This byte is escaped. alloc unconditionally.
                buffer[idx++] = b;
                escaping = false;
                continue;
            }

            if(b == DLE) {
                // Next byte is escaped!
                escaping = true;
                continue;
            }

            if(b == ETX){
                // End of packet.
                break;
            }

            buffer[idx++] = b;
        }

        packet.deSerializePacket(buffer);
        if(!packet.validateCrc()) {
            packet.clear();
        }
    }
}

void setup() {
    // Clear pinState
    for (int state: pinState) {
        state = 0;
    }

    Serial.begin(BAUD_RATE);
    Serial.setTimeout(READ_TIMEOUT);
    Serial.flush();

    sensors.begin();
}

void loop() {
    unsigned long currentMils = millis();

    if (currentMils - prevMils > LOOP_INTERVAL) {
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

                    if (packet.pin < PIN_LENGTH && packet.pin >= 0) {
                        pinState[packet.pin] = value;
                    }

                    break;
                }
                case OP_READ_DIGIT_PIN: {
                    if (packet.pin < PIN_LENGTH && packet.pin >= 0) {
                        packet.data = pinState[packet.pin];
                    }

                    break;
                }
                case OP_INPUT_ANALOG_PIN: {
                    pinMode(packet.pin, packet.pinMode);
                    int value = (int) (packet.data);
                    analogWrite(packet.pin, value);

                    if (packet.pin < PIN_LENGTH && packet.pin >= 0) {
                        pinState[packet.pin] = value;
                    }

                    break;
                }
                case OP_READ_ANALOG_PIN: {
                    if (packet.pin < PIN_LENGTH && packet.pin >= 0) {
                        packet.data = pinState[packet.pin];
                    }
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
