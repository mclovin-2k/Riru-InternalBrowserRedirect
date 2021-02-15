cmake_minimum_required(VERSION 3.4.1)
project(riru_internal_browser_redirect)

include_directories(libriru_utils)

add_definitions(-DRIRU_MODULE)
add_definitions(-DRIRU_MODULE_API_VERSION=${RIRU_MODULE_API_VERSION})
add_definitions(-DRIRU_MODULE_VERSION=${RIRU_MODULE_VERSION})
add_definitions(-DRIRU_MODULE_VERSION_NAME=${RIRU_MODULE_VERSION_NAME})
add_definitions(-DRIRU_MODULE_NAME=${RIRU_MODULE_NAME})

add_subdirectory(libriru_utils)

add_library(riru_internal_browser_redirect SHARED main.c hook.c inject.c)
target_link_libraries(riru_internal_browser_redirect riru_utils log)

