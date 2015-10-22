
#include "opencv2/core/core.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc/imgproc.hpp"

#include <iostream>

using namespace cv;
using namespace std;

void LoadAndCreateEdgesImage(bool blur, const char* filename, Mat* original, Mat* edges);
void PerformTemplateMatching(const Mat& templ, const Mat& detect_templ, Mat* detect);
void DrawShadedRectangle(const Point& max, double scalar, const Mat& templ, Mat* detect);

const char kTemplateName[] = "template.jpg";
const char kTemplateWindowName[] = "Template";
const char kTemplateEdgesWindowName[] = "Template Edges";

const char kDetectWindowName[] = "Source with Matched Template";
const char kDetectEdgesWindowName[] = "Source Edges";

const double kThreshold = 1500000;

int main(int argc, char* argv[]) {
    // First create an icicle template from the image of a single icicle.
    Mat icicle_template;
    Mat template_edges;
    LoadAndCreateEdgesImage(false, kTemplateName, &icicle_template, &template_edges);
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
            true, argv[1], &detect, &detect_edges);
    // Display the images for the source after template matching.
    PerformTemplateMatching(template_edges, detect_edges, &detect);
    namedWindow(kDetectEdgesWindowName, WINDOW_AUTOSIZE);
    imshow(kDetectEdgesWindowName, detect_edges);
    namedWindow(kDetectWindowName, WINDOW_AUTOSIZE);
    imshow(kDetectWindowName, detect);
    // Wait for the user to end the program.
    waitKey(0);
}

// Loads the template image and performs edge detection to create the template.
void LoadAndCreateEdgesImage(bool blur, const char* filename, Mat* original, Mat* edges) {
    Mat& orig = *original;
    orig = imread(filename);
    if (blur) {
        GaussianBlur(orig, orig, Size(7, 7), 0, 0 );
    }
    // Convert color to greyscale.
    Mat& edg = *edges;
    cvtColor(orig, edg, CV_BGR2GRAY); 
    // Find edges in the template image, using Canny edge detection algorithm.
    Canny(edg, edg, 100, 200, 3, true);
}

void PerformTemplateMatching(const Mat& templ, const Mat& detect_templ, Mat* detect) {
    Mat resized_templ; 
    Mat result;
    // Compute max scale value.
    double max_scale = detect_templ.size().height / (double) templ.size().height; 
    double max_correlation_value = 0.0;
    Point max_correlation_location; 
    double max_correlation_scalar = 0.0;
    for (double scale = max_scale; scale > 0.1; scale -= 0.025) {
        resize(templ, resized_templ, Size(), scale, scale);
        // Make sure the resized template does not exceed the frame size width.
        if (resized_templ.size().width > detect_templ.size().width) {
            continue;
        }
        // Perform correlation coefficient template matching.
        matchTemplate(detect_templ, resized_templ, result, CV_TM_CCOEFF);
        double current_max_value; Point current_max_location;
        // Get the maximum value and its location.
        minMaxLoc(result, NULL,
                  &current_max_value, NULL, &current_max_location);

        if (current_max_value > kThreshold) {
            DrawShadedRectangle(current_max_location, scale, templ, detect);
        }

        // Record new globabl maximum if found.
        if (current_max_value > max_correlation_value) {
            max_correlation_value = current_max_value;
            max_correlation_location = current_max_location;
            max_correlation_scalar = scale;
        }
    }

    cout << "Max correlation value: " << max_correlation_value << endl;
}

void DrawShadedRectangle(const Point& max, double scalar, const Mat& templ, Mat* detect) {
    Point end;
    end.x = templ.size().width * scalar;
    end.y = templ.size().height * scalar;
    Mat rect = (*detect)(Rect(
        max.x, max.y, end.x, end.y));
    Mat color(rect.size(), CV_8UC3, cv::Scalar(0, 0, 255)); 
    double alpha = 0.3;
    addWeighted(color, alpha, rect, 1.0 - alpha , 0.0, rect); 
}

