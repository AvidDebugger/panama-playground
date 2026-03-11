cmake_minimum_required(VERSION 3.14)
project(ffmplayground C)

set(CMAKE_C_STANDARD 11)

add_library(ffmplayground SHARED
    src/scalars.c
    src/structs.c
    src/strings.c
    src/callbacks.c
    src/perf.c
)

target_include_directories(ffmplayground PUBLIC
    ${CMAKE_CURRENT_SOURCE_DIR}/include
)

if(UNIX AND NOT APPLE)
    set_target_properties(ffmplayground PROPERTIES PREFIX "lib" SUFFIX ".so")
elseif(APPLE)
    set_target_properties(ffmplayground PROPERTIES PREFIX "lib" SUFFIX ".dylib")
endif()
