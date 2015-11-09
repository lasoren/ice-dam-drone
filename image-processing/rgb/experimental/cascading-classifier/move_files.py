import os

for i in range(0, 31):
    os.system("cp positive_images/" + str(i) + "/*.jpg positive_samples/")
