TARGET = crengine

TEMPLATE = lib
CONFIG += staticlib

DEFINES += USE_FREETYPE=1 LDOM_USE_OWN_MEM_MAN=1 COLOR_BACKBUFFER=1 USE_DOM_UTF8_STORAGE=1 \
    _LINUX=1 LINUX=1 NDEBUG=1 \
    USE_EXTERNAL_EDICT_DICTIONARY=0 RAM_COMPRESSED_BUFFER_ENABLED=1 \
    DOC_DATA_COMPRESSION_LEVEL=1 DOC_BUFFER_SIZE=0x400000 \
    BIG_PAGE_MARGINS=0 CHM_SUPPORT_ENABLED=1 \
    ENABLE_ANTIWORD=1 USE_FONTCONFIG=1

debug:DEFINES+=_DEBUG=1

QMAKE_CFLAGS_WARN_ON = -w
QMAKE_CXXFLAGS_WARN_ON = -w

INCLUDEPATH += ./include \
    $$(QTDIR)/include/freetype2 \
    ../thirdparty/antiword

HEADERS += include/crsetup.h

SOURCES += src/cp_stats.cpp \
    src/lvstring.cpp \
    src/props.cpp \
    src/lstridmap.cpp \
    src/rtfimp.cpp \
    src/cri18n.cpp \
    src/lvmemman.cpp \
    src/lvstyles.cpp \
    src/crtxtenc.cpp \
    src/lvtinydom.cpp \
    src/lvstream.cpp \
    src/lvxml.cpp \
    src/lvstsheet.cpp \
    src/txtselector.cpp \
    src/crtest.cpp \
    src/lvbmpbuf.cpp \
    src/lvfnt.cpp \
    src/hyphman.cpp \
    src/lvfntman.cpp \
    src/crgui.cpp \
    src/lvimg.cpp \
    src/crskin.cpp \
    src/lvdrawbuf.cpp \
    src/lvdocview.cpp \
    src/lvpagesplitter.cpp \
    src/lvtextfm.cpp \
    src/lvrend.cpp \
    src/wolutil.cpp \
    src/hist.cpp \
    src/chmfmt.cpp \
    src/epubfmt.cpp \
    src/pdbfmt.cpp \
    src/wordfmt.cpp \
    ../thirdparty/chmlib/src/chm_lib.c \
    ../thirdparty/chmlib/src/lzx.c
