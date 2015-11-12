#include <unistd.h>
#include "lidar.h"

int openDevice(char* filename){
  int file;

  if((file = open(filename, O_RDWR)) < 0){
    return -1;
  }

  if((ioctl(file, I2C_SLAVE_FORCE, DEF_ADDR)) < 0){
    close(file);
    return -1;
  }

  /*if(!isOkay(file)){
    close(file);
    return -1;
  }*/
  
  i2c_smbus_write_byte_data(file, CONTROL_REG, RESET_FPGA); 
  usleep(20000);
  return file;
}

int getDeviceStatus(int file){
  return i2c_smbus_read_byte_data(file, STATUS_REG);
}

int isOkay(int file){
  if((getDeviceStatus(file) & DEVICE_OKAY) != DEVICE_OKAY){
    return 0;
  } else {
    return 1;
  }
}

int isReady(int file){

  // device is ready if bit 0 is 0
  if((getDeviceStatus(file) & DEVICE_READY) == 0){
    return 1;
  }

  return 0;
}

int getDistance(int file){
  while(!isReady(file)){} // spin
  
  if(isReady(file)){
    i2c_smbus_write_byte_data(file, CONTROL_REG, AQUISIT_DC);
  } else {
    return -1;
  }

  while(!isReady(file)){} // spin  
  
  return (i2c_smbus_read_byte_data(file, AQUISIT_REG_MSB) << 8) | i2c_smbus_read_byte_data(file, AQUISIT_REG_LSB);
}
