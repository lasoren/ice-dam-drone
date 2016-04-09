#include "opencv2/core/core.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc/imgproc.hpp"

#include <iostream>
#include <stdio.h>

using namespace cv;
using namespace std;

int LoadImage(const char* filename, Mat* original);
int FindHotspots(Mat& input, Mat& output);

const char kDetectWindowName[] = "Source with Hotspots Found";

int main(int argc, char* argv[]) {
    if (argc != 3) {
        cout << "usage: " << argv[0] <<
            " <input-filename> <output-filename>" << endl;
        return -1;
    }
    Mat detect;
    if (LoadImage(argv[1], &detect) == -1) {
        return -1;
    }
    Mat output;
    int num_contours = FindHotspots(detect, output);
    // namedWindow(kDetectWindowName, WINDOW_AUTOSIZE);
    // Wait for user to hit any key to end the program.
    // imshow(kDetectWindowName, detect);
    printf("%d\n", num_contours);

    imwrite(argv[2], detect);
    // waitKey(0);
}

int FindHotspots(Mat& input, Mat& output) {
    // Convert the input image to grayscale.
    cvtColor(input, output, CV_BGR2GRAY); 
    // Compute the mean value of the image and the standard deviation.
    Scalar mean, std_dev;
    meanStdDev(output, mean, std_dev);
    // Threshold the image on the mean plus the standard deviation.
    threshold(output, output, mean[0] + 2.5*std_dev[0], 255, THRESH_BINARY);
    // Blur the image in order to detect blobs. 
    GaussianBlur(output, output, Size(7, 7), 0, 0 );
    // Detect keypoints.
    vector<vector<Point> > contours;
    vector<Vec4i> hierarchy;
    findContours(output, contours, hierarchy,
            CV_RETR_TREE, CV_CHAIN_APPROX_SIMPLE, Point(0, 0));
    cvtColor(output, output, CV_GRAY2BGR); 
    drawContours(output, contours, -1, Scalar(0, 0, 255), CV_FILLED);
    addWeighted(input, 1.0, output, 0.4, 0.0, input);
    // Return the number of contours found to be printed to stdout.
    return contours.size();
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

