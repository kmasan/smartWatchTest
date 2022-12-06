package com.b22706.smartwatch_test.fileUpload

interface ApiResult {
    fun onSuccess(res: String)
    fun onError(res: String?)
}