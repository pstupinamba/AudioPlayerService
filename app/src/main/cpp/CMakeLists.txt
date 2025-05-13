cmake_minimum_required ( VERSION 3.22.1 )

add_library(
    equalizer_lib
    SHARED
    equalizer.cpp
)

find_library(
    log_lib
    log
)

target_link_libraries(
    equalizer_lib
    ${log_lib}
)