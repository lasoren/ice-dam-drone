#include <stdlib.h>
#include <stdio.h>
#include <pthread.h>
#include <signal.h>
#include <string.h>
#include "lidar.h"

#define MIN_THRESHOLD 10 // units are in cm

volatile int keepChecking;
int print;

void monitorDistance(void *arg){
  int distance;
  int *file = (int*) arg;
  while(keepChecking){
    distance = getDistance(*file);
    if(print){
      printf("Distance %d cm\n", distance);
    }
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
  char buff[80];
  if(argc < 2){
    printf("Usage is: lidar <device file>\n");
    goto error;
  }
  file = openDevice(argv[1]);

  if(file < 0){
    printf("error opening file\n");
    goto error;
  }

  if(argc >= 3 && !strcmp(argv[2], "-p")){
    print = 1; 
  } else {
    print = 0;
  }

  signal(SIGUSR1, danger);
  signal(SIGINT, quit);
  keepChecking = 1;
  pthread_create(&monitor, NULL, (void*) &monitorDistance, (void*) &file);

  if(!print){
   while(keepChecking){
     scanf("%80s", buff);
   }
  }

  pthread_join(monitor, NULL);
  close(file);

error:
  return 0;
}
