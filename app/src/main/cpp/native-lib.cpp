#include <jni.h>

extern "C" JNIEXPORT void JNICALL
Java_com_example_imageclassification_MainActivity_preprocessImage(JNIEnv *env, jobject thiz,
                                                                  jint width, jint height,
                                                                  jintArray pixels) {
  // Get the pixels array from Java
  jint *imgPixels = env->GetIntArrayElements(pixels, nullptr);
  if (imgPixels == nullptr) {
    // Error handling
    return;
  }

  // Allocate memory for resized image
  int newWidth = 224;
  int newHeight = 224;
  jint *resizedPixels = new jint[newWidth * newHeight];

  // Resize image using simple nearest neighbor interpolation
  float x_ratio = static_cast<float>(width) / newWidth;
  float y_ratio = static_cast<float>(height) / newHeight;
  for (int y = 0; y < newHeight; ++y) {
    for (int x = 0; x < newWidth; ++x) {
      int px = static_cast<int>(x * x_ratio);
      int py = static_cast<int>(y * y_ratio);
      resizedPixels[y * newWidth + x] = imgPixels[py * width + px];
    }
  }

  // Convert the resized image to grayscale
  for (int i = 0; i < newWidth * newHeight; ++i) {
    int color = resizedPixels[i];
    int r = (color >> 16) & 0xFF;
    int g = (color >> 8) & 0xFF;
    int b = color & 0xFF;
    int gray = static_cast<int>(0.2989 * r + 0.5870 * g + 0.1140 * b); // Grayscale conversion formula
    resizedPixels[i] = (0xFF << 24) | (gray << 16) | (gray << 8) | gray;
  }

  // Release the pixels array
  env->ReleaseIntArrayElements(pixels, imgPixels, JNI_ABORT);

  // Update the pixels array with the preprocessed image
  env->SetIntArrayRegion(pixels, 0, newWidth * newHeight, resizedPixels);

  // Free allocated memory
  delete[] resizedPixels;
}
