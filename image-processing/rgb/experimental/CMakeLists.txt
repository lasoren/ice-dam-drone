
cmake_minimum_required(VERSION 2.8)
project( match_icicles )
find_package( OpenCV )
include_directories( ${OpenCV_INCLUDE_DIRS} )
add_executable( match_icicles match_icicles.cpp )
target_link_libraries( match_icicles ${OpenCV_LIBS} )

