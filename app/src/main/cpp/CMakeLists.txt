cmake_minimum_required(VERSION 3.22.1)
project("whisper-jni" LANGUAGES C CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Whisper.cpp source directory (populated by build script)
set(WHISPER_DIR ${CMAKE_CURRENT_SOURCE_DIR}/whisper.cpp)

# Check if whisper.cpp sources exist
if(NOT EXISTS ${WHISPER_DIR}/CMakeLists.txt)
    message(FATAL_ERROR "whisper.cpp not found at ${WHISPER_DIR}. Run tools/fetch-whisper.sh first.")
endif()

# ------------------------
# Use whisper.cpp's own CMake build system as subdirectory
# This handles all the complex GGML backend selection automatically
# ------------------------
# Disable unnecessary components for Android
set(WHISPER_BUILD_TESTS OFF CACHE BOOL "" FORCE)
set(WHISPER_BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)
set(WHISPER_BUILD_SERVER OFF CACHE BOOL "" FORCE)
set(WHISPER_CURL OFF CACHE BOOL "" FORCE)
set(WHISPER_SDL2 OFF CACHE BOOL "" FORCE)
set(WHISPER_COREML OFF CACHE BOOL "" FORCE)
set(WHISPER_OPENVINO OFF CACHE BOOL "" FORCE)
set(WHISPER_FFMPEG OFF CACHE BOOL "" FORCE)
set(BUILD_SHARED_LIBS OFF CACHE BOOL "" FORCE)

# Android-specific GGML options
set(GGML_USE_ACCELERATE OFF CACHE BOOL "" FORCE)
set(GGML_USE_BLAS OFF CACHE BOOL "" FORCE)
set(GGML_USE_CUDA OFF CACHE BOOL "" FORCE)
set(GGML_USE_METAL OFF CACHE BOOL "" FORCE)
set(GGML_USE_OPENCL OFF CACHE BOOL "" FORCE)
set(GGML_USE_VULKAN OFF CACHE BOOL "" FORCE)
set(GGML_USE_CPU_AARCH64 ON CACHE BOOL "" FORCE)
set(GGML_USE_CPU_AARCH32 OFF CACHE BOOL "" FORCE)
set(GGML_USE_CPU_X64 OFF CACHE BOOL "" FORCE)
set(GGML_USE_CPU_X86 OFF CACHE BOOL "" FORCE)

add_subdirectory(${WHISPER_DIR} whisper-build)

# ------------------------
# 2. JNI wrapper
# ------------------------
add_library(whisper-jni SHARED
    whisper_jni.cpp
)
target_link_libraries(whisper-jni PRIVATE whisper ggml)
target_include_directories(whisper-jni PRIVATE
    ${WHISPER_DIR}/include
    ${WHISPER_DIR}/ggml/include
    ${CMAKE_CURRENT_SOURCE_DIR}
)
target_compile_definitions(whisper-jni PRIVATE
    GGML_USE_ACCELERATE=0
    GGML_USE_BLAS=0
    GGML_USE_CUDA=0
    GGML_USE_METAL=0
    GGML_USE_OPENCL=0
    GGML_USE_VULKAN=0
    WHISPER_USE_COREML=0
)
if(ANDROID)
    target_compile_options(whisper-jni PRIVATE
        -fPIC
        -O3
        -DNDEBUG
        -fvisibility=hidden
    )
    if(CMAKE_ANDROID_ARCH_ABI STREQUAL "arm64-v8a")
        target_compile_options(whisper-jni PRIVATE -march=armv8-a+simd)
    endif()
endif()

find_library(log-lib log)
target_link_libraries(whisper-jni PRIVATE ${log-lib})