#include <stdlib.h>
#include <stdio.h>
#include <pthread.h>
#include <signal.h>
#include "lidar.h"

#define MIN_THRESHOLD 10 // units are in cm

volatile int keepChecking;

void monitorDistance(void *arg){
  int distance;
  int *file = (int*) arg;
  while(keepChecking){
    distance = getDistance(*file);
    printf("Distance %d\n", distance);
    if(distance < MIN_THRESHOLD){
      if(distance){
        raise(SIGUSR1);
      }
    }
  }
}

void danger(int sig){
  // disable the interrupt as we've caught it, no need for more
  signal(SIGUSR1, SIG_IGN);
  printf("You are too close to the target\n");
  usleep(100000);
  // renable the interrupt
  signal(SIGUSR1, danger);
}

void quit(int sig){
  printf("quitting. goodbye\n");
  keepChecking = 0;
}

int main(int argc, char *argv[]){
  pthread_t monitor;
  int file;
  if(argc != 2){
    printf("Usage is: lidar <device file>\n");
    goto error;
  }
  file = openDevice(argv[1]);

  if(file < 0){
    printf("error opening file\n");
    goto error;
  }

  signal(SIGUSR1, danger);
  signal(SIGINT, quit);
  keepChecking = 1;
  pthread_create(&monitor, NULL, (void*) &monitorDistance, (void*) &file);

  pthread_join(monitor, NULL);
  close(file);

error:
  return 0;
}
