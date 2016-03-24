
#include "opencv2/core/core.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc/imgproc.hpp"

#include <iostream>

using namespace cv;
using namespace std;

int LoadImage(const char* filename, Mat* original);

int main(int argc, char* argv[]) {
    if (argc != 2) {
        cout << "usage: " << argv[0] << " <filename>" << endl;
        return -1;
    }
    Mat detect;
    if (LoadImage(argv[1], &detect) == -1) {
        return -1;
    }
    // Wait for user to hit any key to end the program.
    waitKey(0);
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

