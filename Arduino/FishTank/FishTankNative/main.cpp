#include <iostream>


using namespace std;

unsigned int makeCrc(long id, long clientId, int pin, int pinMode) {
    int crc = (id << 12) & 0xF000;
    crc |= (clientId << 8) & 0x0F00;
    crc |= (pin << 4) & 0x00F0;
    crc |= pinMode & 0x000F;
    return crc;
}

int main() {
    int crc = makeCrc(1,5,2,1);

    cout << "CRC=" << crc;

    return 0;
}

