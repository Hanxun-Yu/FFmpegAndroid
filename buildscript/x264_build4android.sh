#!/bin/sh

export NDK=/home/xunxun/devtool/android-ndk-r15c
export TOOLCHAIN=$NDK/toolchains/arm-linux-androideabi-4.9/prebuilt/linux-x86_64
PLATFORM=$NDK/platforms/android-16/arch-arm
PREFIX=/home/xunxun/dev/x264_build4android/arm

build_one() {
	./configure \
	--prefix=$PREFIX \
	--disable-shared \
	--enable-static \
	--enable-pic \
	--enable-strip \
	--enable-thread \
	--enable-asm \
	--host=arm-linux-androideabi \
	--cross-prefix=$TOOLCHAIN/bin/arm-linux-androideabi- \
	--sysroot=$PLATFORM \
	--extra-cflags="-Os -fpic" \
	--extra-ldflags="" \

	$ADDITIONAL_CONFIGURE_FLAG
	make clean
	make -j4
	make install
}
build_one

