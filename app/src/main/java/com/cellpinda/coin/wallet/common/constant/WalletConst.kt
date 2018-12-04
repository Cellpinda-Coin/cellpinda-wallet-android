package com.cellpinda.coin.wallet.common.constant

const val PARAM_NEW_ACC           = "isNewAccount"
const val REQ_CODE_FISRT_RUN      = 99
const val REQ_CODE_LOCK           = 98
const val REQ_CODE_QR_SCAN        = 97
const val REQ_CODE_CAMERA         = 96
const val REQ_CODE_SUB_MENU       = 95
const val REQ_CODE_NETWORK        = 94
const val RESULT_LOCK_CANCELED    = 93

const val KEY_ID                  = "id"
const val KEY_UID                 = "uid"
const val KEY_MY_ADDR             = "myaddr"
const val KEY_USER_NAME           = "name"
const val KEY_LEVEL               = "level"
const val KEY_USER_P              = "p"
const val KEY_ETH_KRW             = "ethKrwVal"
const val KEY_KEY                 = "key"

const val KEY_PREF                = "BASE_APPLICATION"
const val ACTION_SCAN             = "com.google.zxing.client.android.SCAN"
const val IMG_NAME_QR             = "myQR.png"

const val SUCCESS_RES             = 0
const val FAIL_RES                = 1
const val SESS_EXPIRE_RES         = 2
const val ETH_MIN_LIMIT           = 0.1
const val ETH_FEE_LIMIT           = 0.005

const val SYM_KRW                 = "KRW"
const val CMCAP_ID_ETH            = 1027

const val INTENT_PURETX           = 'S'
const val INTENT_BUY_TOKEN        = 'T'
const val INTENT_BONUS_TOKEN      = 'B'
const val INTENT_MANUAL_PAY       = 'M'
const val INTENT_POINT_EXCHANGE   = 'P'

const val STAT_ACCEPTED      = 'A' // ACCEPTED
const val STAT_DEPOSITING    = 'D' // DEPOSITING
const val STAT_PROGRESS      = 'P' // BEFORE WITHDRAWAL
const val STAT_WITHDRAWING   = 'W' // WITHDRAWING
const val STAT_COMPLETED     = 'C' // COMPLETED
const val STAT_FAILED        = 'F' // FAILED

val AVAILABLE_TYPE: CharArray = charArrayOf(INTENT_PURETX, INTENT_BUY_TOKEN, INTENT_BONUS_TOKEN, INTENT_MANUAL_PAY, INTENT_POINT_EXCHANGE)
