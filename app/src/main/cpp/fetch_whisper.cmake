# fetch_whisper.cmake - Download and configure whisper.cpp at CMake configure time
# This runs during Gradle sync, so it must be fast and not require network if already present.

set(WHISPER_VERSION "v1.7.4")  # Known good version with excellent Farsi support
set(WHISPER_URL "https://github.com/ggerganov/whisper.cpp/archive/refs/tags/${WHISPER_VERSION}.tar.gz")
set(WHISPER_DIR "${CMAKE_CURRENT_SOURCE_DIR}/whisper.cpp")
set(WHISPER_STAMP "${WHISPER_DIR}.stamp")

# Check if already fetched
if(EXISTS "${WHISPER_STAMP}")
    file(READ "${WHISPER_STAMP}" WHISPER_STAMP_CONTENT)
    if("${WHISPER_STAMP_CONTENT}" STREQUAL "${WHISPER_VERSION}")
        message(STATUS "whisper.cpp ${WHISPER_VERSION} already fetched")
        set(WHISPER_SOURCES
            ${WHISPER_DIR}/src/whisper.cpp
            ${WHISPER_DIR}/ggml/src/ggml.c
            ${WHISPER_DIR}/ggml/src/ggml-backend.c
            ${WHISPER_DIR}/ggml/src/ggml-alloc.c
            ${WHISPER_DIR}/ggml/src/ggml-impl.c
            ${WHISPER_DIR}/ggml/src/ggml-ops.c
            ${WHISPER_DIR}/ggml/src/ggml-quants.c
            ${WHISPER_DIR}/ggml/src/ggml-metal.m
        )
        set(WHISPER_INCLUDE_DIRS
            ${WHISPER_DIR}/include
            ${WHISPER_DIR}/ggml/include
            ${WHISPER_DIR}/ggml/src
        )
        return()
    endif()
endif()

message(STATUS "Fetching whisper.cpp ${WHISPER_VERSION}...")

# Use file(DOWNLOAD) - works during configure time
file(DOWNLOAD "${WHISPER_URL}" "${WHISPER_DIR}.tar.gz"
    TIMEOUT 120
    SHOW_PROGRESS
    EXPECTED_HASH SHA256= # Skip hash for now, could add
)

# Extract
execute_process(
    COMMAND tar -xzf "${WHISPER_DIR}.tar.gz" -C "${CMAKE_CURRENT_SOURCE_DIR}"
    WORKING_DIRECTORY "${CMAKE_CURRENT_SOURCE_DIR}"
    RESULT_VARIABLE result
)
if(NOT result EQUAL 0)
    message(FATAL_ERROR "Failed to extract whisper.cpp")
endif()

# Rename extracted folder to whisper.cpp
file(RENAME "${CMAKE_CURRENT_SOURCE_DIR}/whisper.cpp-${WHISPER_VERSION}" "${WHISPER_DIR}")

# Clean up
file(REMOVE "${WHISPER_DIR}.tar.gz")

# Write stamp
file(WRITE "${WHISPER_STAMP}" "${WHISPER_VERSION}")

# Set variables for parent scope
set(WHISPER_SOURCES
    ${WHISPER_DIR}/src/whisper.cpp
    ${WHISPER_DIR}/ggml/src/ggml.c
    ${WHISPER_DIR}/ggml/src/ggml-backend.c
    ${WHISPER_DIR}/ggml/src/ggml-alloc.c
    ${WHISPER_DIR}/ggml/src/ggml-impl.c
    ${WHISPER_DIR}/ggml/src/ggml-ops.c
    ${WHISPER_DIR}/ggml/src/ggml-quants.c
    ${WHISPER_DIR}/ggml/src/ggml-metal.m
    PARENT_SCOPE
)
set(WHISPER_INCLUDE_DIRS
    ${WHISPER_DIR}/include
    ${WHISPER_DIR}/ggml/include
    ${WHISPER_DIR}/ggml/src
    PARENT_SCOPE
)

message(STATUS "whisper.cpp ${WHISPER_VERSION} ready at ${WHISPER_DIR}")