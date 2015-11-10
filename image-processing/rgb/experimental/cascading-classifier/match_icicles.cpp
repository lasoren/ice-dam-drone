#include "opencv2/core/core.hpp"
#include "opencv2/objdetect/objdetect.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc/imgproc.hpp"

#include <iostream>

using namespace cv;
using namespace std;

int LoadImage(const char* filename, Mat* original);
void PerformObjectDetection(CascadeClassifier& cascade, Mat* detect);
void FillShadedRectangle(const Rect& rect, Mat* shaded);

const char kCascadeName[] = "icicle_classifier/cascade.xml";

const char kDetectWindowName[] = "Source with Matched Classifier";
const double kThreshold = 2000000;

int main(int argc, char* argv[]) {
    if (argc != 2) {
        cout << "usage: " << argv[0] << " <filename>" << endl;
        return -1;
    }
    Mat detect;
    if (LoadImage(argv[1], &detect) == -1) {
        return -1;
    }

    CascadeClassifier cascade;
    if (!cascade.load(kCascadeName)) {
        cerr << "ERROR: Could not load classifier cascade" << endl;
        return -1;
    }
    // Display the images for the source after running the classifier.
    PerformObjectDetection(cascade, &detect);
    namedWindow(kDetectWindowName, WINDOW_AUTOSIZE);
    imshow(kDetectWindowName, detect);

    system("mkdir output");
    imwrite("output/matched.jpg", detect);
    // Wait for the user to end the program.
    waitKey(0);
}

void PerformObjectDetection(CascadeClassifier& cascade, Mat* detect) {
    vector<Rect> objects;
    Mat greyscale;
    // Convert the image to greyscale.
    cvtColor(*detect, greyscale, CV_BGR2GRAY); 
    cascade.detectMultiScale(greyscale, objects, 1.05, 1, 0 | CASCADE_DO_ROUGH_SEARCH | CASCADE_SCALE_IMAGE, Size(10, 10));
    Mat shaded(detect->size(), CV_8UC3, Scalar(0, 0, 0));
    for (int i = 0; i < objects.size(); i++) {
        const Rect& rect = objects[i];
        FillShadedRectangle(rect, &shaded);
    }
    // Color the original image where objects were detected.
    const double alpha = 0.3;
    addWeighted(*detect, 1.0 - alpha, shaded, alpha, 0.0, *detect); 
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

void FillShadedRectangle(const Rect& rect, Mat* shaded) {
    Point end;
    end.x = rect.x + rect.width;
    end.y = rect.y + rect.height;
    for (int i = rect.x; i < end.x; i++) {  // Cols
        for (int j = rect.y; j < end.y; j++) {  // Rows
            // Set the R channel to 255.
            shaded->at<Vec3b>(j, i)[2] = 255;
        }
    }
}

