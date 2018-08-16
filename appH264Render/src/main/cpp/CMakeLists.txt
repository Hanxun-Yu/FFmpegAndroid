# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

#set(SO_IMPORT_PATH ${PROJECT_SOURCE_DIR}/../../../libs/${ANDROID_ABI})
#使用外部库工程内的库，如用本工程用上面这句
set(SO_IMPORT_PATH ${PROJECT_SOURCE_DIR}/../../../../libffmpeg/libs/${ANDROID_ABI})
set(INCLUDE_PATH ${PROJECT_SOURCE_DIR}/../../../../libffmpeg/src/main/cpp/include)


cmake_minimum_required(VERSION 3.4.1)
ADD_SUBDIRECTORY(yuvutil)
ADD_SUBDIRECTORY(rtsp)



# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.
include_directories(
        .
        include
        util
        ${INCLUDE_PATH}
)

add_library( # Sets the name of the library.
        ffmpegjni

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        RGBRender.cpp
        VideoPlayerJni.cpp
        VideoDecoder.cpp
        util/ThreadHandler.cpp
        H264DecodeThread.hpp
        )
add_library(
        ffmpeg
        SHARED
        IMPORTED
)
add_library(
        jnicom
        SHARED
        IMPORTED
)
set_target_properties(
        jnicom
        PROPERTIES IMPORTED_LOCATION
        ${SO_IMPORT_PATH}/libjnicom.so)
set_target_properties(
        ffmpeg
        PROPERTIES IMPORTED_LOCATION
        ${SO_IMPORT_PATH}/libffmpeg.so)


add_library(
                libyuv
                STATIC
                IMPORTED
        )
        set_target_properties(
                libyuv
                PROPERTIES IMPORTED_LOCATION
                ${SO_IMPORT_PATH}/libyuv_static.a)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)
find_library( # Sets the name of the path variable.
        android-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        android)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        ffmpegjni

        ffmpeg
        jnicom
        libyuv
        # Links the target library to the log library
        # included in the NDK.
        ${log-lib}
        ${android-lib}
        )