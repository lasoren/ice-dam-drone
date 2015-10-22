
#include "opencv2/core/core.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc/imgproc.hpp"

#include <iostream>

using namespace cv;
using namespace std;

void LoadAndCreateEdgesImage(const char* filename, Mat* original, Mat* edges);

const char kTemplateName[] = "template.jpg";
const char kTemplateWindowName[] = "Template";
const char kTemplateEdgesWindowName[] = "Template Edges";

const char kDetectIciclesName[] = "detect0.jpg";
const char kDetectWindowName[] = "Source";
const char kDetectEdgesWindowName[] = "Source Edges";

int main(int argc, char* argv[]) {
    // First create an icicle template from the image of a single icicle.
    Mat icicle_template;
    Mat template_edges;
    LoadAndCreateEdgesImage(
            kTemplateName, &icicle_template, &template_edges);
    // Display the images for the template and it's edges.
    namedWindow(kTemplateWindowName, WINDOW_AUTOSIZE);
    imshow(kTemplateWindowName, icicle_template);
    namedWindow(kTemplateEdgesWindowName, WINDOW_AUTOSIZE);
    imshow(kTemplateEdgesWindowName, template_edges);
    
    if (argc != 2) {
        cout << "usage: " << argv[0] << " <filename>\n";
        return -1;
    }
    Mat detect;
    Mat detect_edges;
    LoadAndCreateEdgesImage(
            argv[1], &detect, &detect_edges);
    // Display the images for the source.
    namedWindow(kDetectWindowName, WINDOW_AUTOSIZE);
    imshow(kDetectWindowName, detect);
    namedWindow(kDetectEdgesWindowName, WINDOW_AUTOSIZE);
    imshow(kDetectEdgesWindowName, detect_edges);
    // Wait for the user to end the program.
    waitKey(0);
}

// Loads the template image and performs edge detection to create the template.
void LoadAndCreateEdgesImage(const char* filename, Mat* original, Mat* edges) {
    Mat& orig = *original;
    orig = imread(filename);
    // Convert color to greyscale.
    Mat& edg = *edges;
    cvtColor(orig, edg, CV_BGR2GRAY); 
    // Find edges in the template image, using Canny edge detection algorithm.
    Canny(edg, edg, 60, 240, 3, true);
}

void PerformTemplateMatching(const Mat& templ, Mat* detect) {
    
}

