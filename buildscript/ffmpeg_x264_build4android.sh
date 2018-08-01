#!/bin/sh

 build_one() {
    ./configure \
--prefix=$PREFIX \
--arch=arm \
--cpu=armv7-a \
--target-os=android \
--enable-cross-compile \
--cross-prefix=$TOOLCHAIN/bin/arm-linux-androideabi- \
--sysroot=$PLATFORM \
--extra-cflags="-I$X264_INCLUDE -I$PLATFORM/usr/include" \
--extra-ldflags="-L$X264_LIB" \
--cc=$TOOLCHAIN/bin/arm-linux-androideabi-gcc \
--nm=$TOOLCHAIN/bin/arm-linux-androideabi-nm \
--disable-shared \
--enable-static \
--enable-gpl \
--enable-version3 \
--enable-pthreads \
--enable-runtime-cpudetect \
--disable-small \
--disable-network \
--disable-vda \
--disable-iconv \
--enable-asm \
--enable-neon \
--enable-yasm \
--disable-encoders \
--enable-libx264 \
--enable-encoder=libx264 \
--enable-encoder=aac \
--enable-encoder=mpeg4 \
--enable-encoder=mjpeg \
--enable-encoder=png \
--disable-muxers \
--enable-muxer=mov \
--enable-muxer=mp4 \
--enable-muxer=adts \
--enable-muxer=h264 \
--enable-muxer=mjpeg \
--disable-decoders \
--enable-decoder=aac \
--enable-decoder=aac_latm \
--enable-decoder=mp3 \
--enable-decoder=h264 \
--enable-decoder=mpeg4 \
--enable-decoder=mjpeg \
--enable-decoder=png \
--disable-demuxers \
--enable-demuxer=image2 \
--enable-demuxer=h264 \
--enable-demuxer=aac \
--enable-demuxer=mp3 \
--enable-demuxer=mpc \
--enable-demuxer=mpegts \
--enable-demuxer=mov \
--disable-parsers \
--enable-parser=aac \
--enable-parser=ac3 \
--enable-parser=h264 \
--disable-protocols \
--enable-protocol=file \
--enable-protocol=concat \
--enable-filters \
--enable-zlib \
--disable-outdevs \
--disable-doc \
--disable-ffplay \
--disable-ffmpeg \
--disable-ffserver \
--disable-debug \
--disable-ffprobe \
--enable-postproc \
--enable-avdevice \
--enable-avresample \
--disable-symver \
--disable-stripping \
--extra-cflags="-Os -fpic $ADDI_CFLAGS" \
--extra-ldflags="$ADDI_LDFLAGS" \
$ADDITIONAL_CONFIGURE_FLAG


    make clean
    make -j8
    make install



}

createSo(){
	$TOOLCHAIN/bin/arm-linux-androideabi-ld \
-rpath-link=$PLATFORM/usr/lib \
-L$PLATFORM/usr/lib \
-L$PREFIX/lib \
-L$X264_LIB \
-soname libffmpeg.so -shared -nostdlib -Bsymbolic --whole-archive --no-undefined -o \
$PREFIX/libffmpeg.so \
libavcodec/libavcodec.a \
libavdevice/libavdevice.a \
libavresample/libavresample.a \
libpostproc/libpostproc.a \
libavfilter/libavfilter.a \
libswresample/libswresample.a \
libavformat/libavformat.a \
libavutil/libavutil.a \
libswscale/libswscale.a \
$X264_LIB/libx264.a \
-lc -lm -lz -ldl -llog --dynamic-linker=/system/bin/linker \
$TOOLCHAIN/lib/gcc/arm-linux-androideabi/4.9.x/libgcc.a
}

export NDK=/home/xunxun/devtool/android-ndk-r15c
export TOOLCHAIN=$NDK/toolchains/arm-linux-androideabi-4.9/prebuilt/linux-x86_64
export PLATFORM=$NDK/platforms/android-16/arch-arm

x264_lib=/home/xunxun/dev/x264_build4android/arm
X264_INCLUDE=${x264_lib}/include
X264_LIB=${x264_lib}/lib


# arm v7vfp
CPU=arm-v7a
OPTIMIZE_CFLAGS="-mfloat-abi=softfp -mfpu=vfp -marm -march=armv7-a "
ADDI_CFLAGS="-marm"
PREFIX=/home/xunxun/dev/ffmpeg_build4android/android/$CPU
build_one
createSo
