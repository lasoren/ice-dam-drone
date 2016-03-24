
#include "opencv2/core/core.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc/imgproc.hpp"

#include <iostream>

using namespace cv;
using namespace std;

int LoadImage(const char* filename, Mat* original);
void FindHotspots(const Mat& input, Mat* output);

const char kDetectWindowName[] = "Source with Hotspots Found";

int main(int argc, char* argv[]) {
    if (argc != 2) {
        cout << "usage: " << argv[0] << " <filename>" << endl;
        return -1;
    }
    Mat detect;
    if (LoadImage(argv[1], &detect) == -1) {
        return -1;
    }
    Mat output;
    FindHotspots(detect, &output);
    namedWindow(kDetectWindowName, WINDOW_AUTOSIZE);
//    imshow(kDetectWindowName, output);
    // Wait for user to hit any key to end the program.
    waitKey(0);
}

void FindHotspots(const Mat& input, Mat* output) {
    Mat grayscale;
    // Convert the input image to grayscale.
    cvtColor(input, grayscale, CV_BGR2GRAY); 
    // Compute the mean value of the image and the standard deviation.
    Scalar mean, std_dev;
    meanStdDev(grayscale, mean, std_dev); 
    cout << mean << "   " << std_dev << endl;
}

int LoadImage(const char* filename, Mat* original) {
    Mat& orig = *original;
    orig = imread(filename);
    if (!orig.data) {
        cerr << "Could not find or open image: " << filename << endl;
        return -1;
    }
    return 0;
}

