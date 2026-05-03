package com.itl.commonres.utils

enum class AdsPlacementsEnum(val value: Int) {
    DASHBOARD_BOOSTX(0),
    DASHBOARD_SCANHUB(1),

    /***scanHub***/
    //home
    SH_HOME_PDF_ICON_CLICK(11),
    SH_HOME_EMPTY_LIST(12),

    //doc
    SH_DOC_EMPTY_LIST(13),

    //scanned images list
    SH_SCANNED_PDF_ICON_CLICK(14),
    SH_SCANNED_IMAGE_LIST(15),

    //ocr
    SH_OCR_RESULT(16),
    SH_OCR_LIST(17),
    SH_OCR_EMPTY_LIST(18),

    //image edit
    SH_IMAGE_EDIT(19),

    //QR_BAR CODE
    SH_QR_BAR_CODE_RESULT(20),

    /***boostx***/
    BX_JUNK_CLEAN(21),

    //duplicate
    BX_DUPLICATES_CLEAN(22),
    BX_DUPLICATES_SCREEN(23),

    //social
    BX_SOCIAL_LIST_SCREEN(24),
    BX_SOCIAL_CLEAN(25),

    //anti malware
    BX_AM_CLEAN(26),

    //others
    BX_CLEAN_RESULT(27),
    BX_APPLICATION_MANAGER_BACKUP_BTN(28),
    BX_FILE_MANAGER_SCREEN(29),
    BX_INFO(30),
    BX_SPACE_SAVER(31);
}