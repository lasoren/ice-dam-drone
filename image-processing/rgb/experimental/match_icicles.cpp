
#include "opencv2/core/core.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc/imgproc.hpp"

#include <iostream>

using namespace cv;
using namespace std;

void LoadAndCreateIcicleTemplate(Mat* icicle_template, Mat* edges);

const char kTemplateName[] = "template.jpg";
const char kTemplateWindowName[] = "Template";
const char kTemplateEdgesWindowName[] = "Template Edges";

int main() {
    // First create an icicle template from the image of a single icicle.
    Mat icicle_template;
    Mat edges;
    LoadAndCreateIcicleTemplate(&icicle_template, &edges);
    // Display the images.
    namedWindow(kTemplateWindowName, WINDOW_AUTOSIZE);
    imshow(kTemplateWindowName, icicle_template);
    namedWindow(kTemplateEdgesWindowName, WINDOW_AUTOSIZE);
    imshow(kTemplateEdgesWindowName, edges);
    // Wait for the user to end the program.
    waitKey(0);
}

// Loads the template image and performs edge detection to create the template.
void LoadAndCreateIcicleTemplate(Mat* icicle_template, Mat* edges) {
    Mat& templ = *icicle_template;
    templ = imread(kTemplateName);
    // Convert color to greyscale.
    Mat& edg = *edges;
    cvtColor(templ, edg, CV_BGR2GRAY); 
    // Find edges in the template image, using Canny edge detection algorithm.
    Canny(edg, edg, 60, 240, 3, true);
}

