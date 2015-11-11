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
  int distance;

  /*if(!isOkay(file)){
    return -1;
  }

  if(isReady(file)){
    i2c_smbus_write_byte_data(file, CONTROL_REG, AQUISIT_DC);
  } else {
    return -1;
  }

  while(!isReady(file)){} // spin  */
  
  i2c_smbus_write_byte_data(file, CONTROL_REG, RESET_FPGA);
  usleep(20000); // wait for the fpga to reset 
  i2c_smbus_write_byte_data(file, CONTROL_REG, AQUISIT_DC);
  usleep(20000); // for data to be aquired and written to the appropriate registers
  distance = i2c_smbus_read_byte_data(file, 0x0f);
  distance = (distance << 8) | i2c_smbus_read_byte_data(file, 0x10);

  return distance;
}
