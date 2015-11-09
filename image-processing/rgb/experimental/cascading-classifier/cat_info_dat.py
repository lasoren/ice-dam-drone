import os

for i in range(0, 31):
    os.system("cat positive_images/" + str(i) + "/info.dat >> positive_samples/info.dat")

