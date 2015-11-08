#include "lidar.h"

int openDevice(char* filename){
  int file;
  char deviceStatus;

  if((file = open(filename, O_RDWR)) < 0){
    return -1;
  }

  if((ioctl(file, I2C_SLAVE_FORCE, DEF_ADDR)) < 0){
    close(file);
    return -1;
  }

  deviceStatus = getDeviceStatus(file);

  if(!isOkay(file)){
    close(file);
    return -1;
  }


  return file;
}

int getDeviceStatus(int file){
  return i2c_smbus_read_byte(file, STATUS_REG);
}

int isOkay(int file){
  if((deviceStatus & DEVICE_OKAY) != DEVICE_OKAY){
    return 0;
  } else {
    return 1;
  }
}

int isReady(int file){

  // device is ready if bit 0 is 0
  if((getDeviceStatus & DEVICE_READY) == 0){
    return 1;
  }

  return 0;
}

int getDistance(int file){
  int ready, distance;
  ready = 0;

  if(!isOkay(file)){
    return -1;
  }

  if(isReady(file)){
    i2c_smbus_write_byte_data(file, CONTROL_REG, AQUISIT_DC);
  } else {
    return -1;
  }

  while(!isReady(file)){} // spin


  distance = i2c_smbus_read_word_data(file, AQUISIT_REG);

  return distance;
}
