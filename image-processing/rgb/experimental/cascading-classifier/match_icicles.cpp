#include <opencv2/core/core.hpp>
#include <opencv2/objdetect/objdetect.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
// For the clock_gettime function.
#include <time.h>
// For error checking.
#include <errno.h>

#include <iostream>
#include <stdio.h>
using namespace cv;
using namespace std;

int LoadImage(const char* filename, Mat* original);
void PerformObjectDetection(CascadeClassifier& cascade, Mat* detect);
void FillShadedRectangle(const Rect& rect, Mat* shaded);

// some helper functions for dealing with times
double time_in_seconds(struct timespec *t){
    // a timespec has integer values for seconds and nano seconds
    return (t->tv_sec + 1.0e-9 * (t->tv_nsec));
}

const char kCascadeName[] = "icicle_classifier/cascade.xml";

const char kDetectWindowName[] = "Source with Matched Classifier";

int main(int argc, char* argv[]) {

    if (argc != 2) {
        cout << "usage: sudo " << argv[0] << " <filename>" << endl;
        return -1;
    }

    char command[150];
    sprintf(command, "raspistill -w 640 -h 480 -ss 10000 -vs -ex antishake -t 1 -o %s", argv[1]);
	if(system(command) == -1) {
      cout << "Error taking a picture. Are you running as root?" << endl;
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
    // Calculate the time it takes to run the algorithm.
    struct timespec start, finish, resolution;
    double calculation_time;
    int err; // error number for system calls

    err = clock_getres(CLOCK_THREAD_CPUTIME_ID,&resolution);
    if (err){
        perror("Failed to get clock resolution in thread");
        exit(1);
    }

    err = clock_gettime(CLOCK_THREAD_CPUTIME_ID,&start);
    if (err){
        perror("Failed to read thread_clock with error = %d\n");
        exit(1);
    }    
 
    vector<Rect> objects;
    vector<Rect> objects_flipped;
    Mat greyscale;
    // Convert the image to greyscale.
    cvtColor(*detect, greyscale, CV_BGR2GRAY);

    cascade.detectMultiScale(greyscale, objects, 1.05, 2,
            0 | CASCADE_SCALE_IMAGE, Size(10, 10));
    // Try the flipped version of the image.
    flip(greyscale, greyscale, 1);
    cascade.detectMultiScale(greyscale, objects_flipped, 1.05, 2,
            0 | CASCADE_SCALE_IMAGE, Size(10, 10));

    // Calculate the total time taken.
    err = clock_gettime(CLOCK_THREAD_CPUTIME_ID,&finish);
    if (err){
        perror("Failed to read thread_clock with error = %d\n");
        exit(1);
    }
    calculation_time = time_in_seconds(&finish)-time_in_seconds(&start);
    cout << "Calculation time for algorithm: " << calculation_time << endl;
    
    Mat shaded(detect->size(), CV_8UC3, Scalar(0, 0, 0));
    for (int i = 0; i < objects.size(); i++) {
        const Rect& rect = objects[i];
        FillShadedRectangle(rect, &shaded);
    }
    for (int i = 0; i < objects_flipped.size(); i++) {
        Rect& rect = objects_flipped[i];
        // Flip the rectangle so that it appears correctly on the shaded image.
        rect.x = greyscale.cols - rect.x - rect.width;
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
