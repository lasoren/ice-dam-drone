CC=gcc
all: main.o lidar.o
	$(CC) main.o lidar.o -s -lpthread -o lidar
lidar.o:
	$(CC) lidar.c -c -o $@
main.o:
	$(CC) main.c -c -o $@
clean:
	rm -rf *.o lidar
