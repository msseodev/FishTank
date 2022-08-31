#include <iostream>
#include "FishPacket.h"

using namespace std;

int main() {
    auto* fishPacket = new FishPacket();
    fishPacket->clientId = 1;
    fishPacket->id = 2;
    fishPacket->pin = 10;
    fishPacket->pinMode = 1;

    unsigned int crc = fishPacket->makeCrc();

    cout << "CRC=" << crc;

    return 0;
}

