//
// Created by user on 2022-05-08.
//

#ifndef CLIONFISHTANK_FISHTANK_H
#define CLIONFISHTANK_FISHTANK_H

#define MAGIC 31256
#define PACKET_SIZE 20

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

#endif //CLIONFISHTANK_FISHTANK_H
