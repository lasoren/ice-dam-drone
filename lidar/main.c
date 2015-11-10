#include <stdlib.h>
#include <stdio.h>
#include "lidar.h"


int main(){
  int file, distance;
  file = openDevice("/dev/i2c-1");

  if(file < 0){
	printf("error opening file\n");
    goto error;
  }

  distance = getDistance(file);

  printf("Distance: %d\n", distance);

  close(file);

error:
  return 0;
}
