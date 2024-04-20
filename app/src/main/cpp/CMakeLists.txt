# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.22.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

# Set the project name
project(ImagePreprocessingJNI)

# Find the Java native interface (JNI)
find_package(JNI REQUIRED)

# Add the JNI include directory to the include paths
include_directories(${JNI_INCLUDE_DIRS})

# Set the source files
set(SOURCE_FILES native-lib.cpp)

# Create the shared library
add_library(native-lib SHARED ${SOURCE_FILES})

# Set C++ standard to C++11
set_target_properties(native-lib PROPERTIES CXX_STANDARD 11)

# Set the output directory for the shared library
set_target_properties(native-lib PROPERTIES LIBRARY_OUTPUT_DIRECTORY ${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI})

# Link libraries
target_link_libraries(native-lib)