# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.
#set(SO_IMPORT_PATH ${PROJECT_SOURCE_DIR}/../../../libs/${ANDROID_ABI})
set(SO_IMPORT_PATH ${PROJECT_SOURCE_DIR}/../../../../libffmpeg/libs/${ANDROID_ABI})
set(INCLUDE_PATH ${PROJECT_SOURCE_DIR}/../../../../libffmpeg/src/main/cpp/include)

cmake_minimum_required(VERSION 3.4.1)


# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.
include_directories(
        .
        ../include
        ${INCLUDE_PATH}
)

add_library( # Sets the name of the library.
        yuvjni

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        YuvJni.cpp)


target_link_libraries( # Specifies the target library.
        yuvjni
        jnihelper
        ffmpeg
        libyuv
        # Links the target library to the log library
        # included in the NDK.
        ${log-lib}
        )