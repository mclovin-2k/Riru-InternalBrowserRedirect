cmake_minimum_required(VERSION 3.4.1)
project(riru_ibr)

include_directories(libriru_utils)

add_definitions(-DRIRU_MODULE)
add_definitions(-DRIRU_MODULE_API_VERSION=${RIRU_MODULE_API_VERSION})
add_definitions(-DRIRU_MODULE_VERSION=${RIRU_MODULE_VERSION})
add_definitions(-DRIRU_MODULE_VERSION_NAME=${RIRU_MODULE_VERSION_NAME})
add_definitions(-DRIRU_MODULE_NAME=${RIRU_MODULE_NAME})

add_subdirectory(libxhook)
find_package(riru REQUIRED CONFIG)
include_directories(.)
add_library(riru_ibr SHARED main.cc inject.cc)
target_link_libraries(riru_ibr xhook riru::riru log)
