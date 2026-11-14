cmake_minimum_required(VERSION 3.22.1)
project("openbangla_engine")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

include_directories(.)

# Main LatinIME-style suggestion engine
add_library(openbangla_jni SHARED
    patricia_trie.cpp
    ngram_context.cpp
    damerau_levenshtein.cpp
    proximity_info.cpp
    suggest_engine.cpp
    jni_suggest.cpp
)

# Autocorrect phonetic fixer engine
add_library(autocorrect_jni SHARED
    phonetic_fixer.cpp
    jni_autocorrect.cpp
)

find_library(log-lib log)

target_link_libraries(openbangla_jni
    ${log-lib}
)

target_link_libraries(autocorrect_jni
    ${log-lib}
)
