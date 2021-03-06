#ifndef LIDAR
#include <sys/ioctl.h>
#include <fcntl.h>
#include <stdio.h>
#include <linux/i2c-dev.h>

#define LIDAR

#define DEF_ADDR 0x62

//register addresses
#define CONTROL_REG 0x00
#define STATUS_REG 0x01
#define AQUISIT_REG_MSB 0x0f
#define AQUISIT_REG_LSB 0x10
#define SERIAL_REG 0x96

// commands
#define RESET_FPGA 0x00
#define AQUISIT_DC 0x04
#define AQUISIT_NODC 0x03

// status register bits
#define MEASURE_ERROR 0x40
#define DEVICE_OKAY 0x20
#define INVALID_SIGNAL 0x08
#define DEVICE_READY 0x01

int openDevice(char* filename);
int getDistance(int file);
int isOkay(int file);
int getDeviceStatus(int file);
int isReady(int file);

#endif
