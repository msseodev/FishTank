#include <iostream>
#include "FishPacket.h"

using namespace std;

void printPacket(FishPacket* packet) {
    cout << "id=" << packet->id << " clientId=" << packet->clientId <<
        " opCode=" << packet->opCode <<
         " pin=" << packet->pin << " pinMode=" << packet->pinMode <<
         " data=" << packet->data << " crc=" << packet->crc <<
         " valid=" << packet->validateCrc() <<
         endl;
}

int main() {
    for(int i=0; i<2000; i++) {
        auto *fishPacket = new FishPacket();
        fishPacket->clientId = i;
        fishPacket->id = i+100;
        fishPacket->pin = i+299;
        fishPacket->pinMode = i;
        fishPacket->opCode = i;
        fishPacket->data = i * 0.75;

        unsigned int crc = fishPacket->makeCrc();
        cout << "CRC=" << crc << endl;

        unsigned char buffer[40];
        int packetSize = fishPacket->serializePacket(buffer);
        cout << "PacketSize=" << packetSize << endl;
        printPacket(fishPacket);

        auto *aPacket = new FishPacket();
        aPacket->deSerializePacket(buffer);
        printPacket(aPacket);
    }

    return 0;
}

