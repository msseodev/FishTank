//
// Created by MSSeo on 2022-08-25.
//

#ifndef FISHTANKNATIVE_FISHTANK_H
#define FISHTANKNATIVE_FISHTANK_H

#include <stdio.h>
#include <string.h>
#include <rpcndr.h>

void setup();
void loop();

class Ser {
public:
    static void println();
    static void println(const char*);

    static void print();
    static void print(const char*);
    static void print(int);
    static void print(float);
    static void print(double);
    static void print(long);

    static void write(uint8_t[], int);
    static void flush();
    static void readBytes(uint8_t[], int);

    static void begin(int);
    static void setTimeout(int);
    static int read();
};

unsigned long millis();
void pinMode(int, int);
void digitalWrite(int, int);
void analogWrite(int, int);

Ser Serial;
Ser Serial1;

#endif //FISHTANKNATIVE_FISHTANK_H
