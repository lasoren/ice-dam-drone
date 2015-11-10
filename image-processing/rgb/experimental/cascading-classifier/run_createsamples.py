import os

for i in range(0, 31):
    os.system("opencv_createsamples -img positive_images/" + str(i) + ".jpg -bg bg.txt -info positive_images/" + str(i) + "/info.dat -maxxangle 0.1 -maxyangle 0.1 -maxzangle 0.1 -bgcolor 0 -bgthresh 0")
