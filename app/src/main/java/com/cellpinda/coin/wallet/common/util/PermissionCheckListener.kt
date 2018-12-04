package com.cellpinda.coin.wallet.common.util;

/**
 * Created by lgcns on 2017-06-09.
 */

interface PermissionCheckListener {
    fun onPermissionIsOk()
    fun onPermissionIsNotOk()
}
