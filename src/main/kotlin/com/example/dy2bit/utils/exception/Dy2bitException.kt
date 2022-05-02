package com.example.dy2bit.utils.exception

import okhttp3.internal.http2.ErrorCode
import org.springframework.context.MessageSourceResolvable
import org.springframework.http.HttpStatus

class Dy2bitException private constructor(
    val msg: String?,
    val resolvable: MessageSourceResolvable?,
    val code: ErrorCode,
    val extra: Map<String, String> = emptyMap(),
    val httpStatus: HttpStatus = HttpStatus.BAD_REQUEST,
    val title: String?,
    val titleResolvable: MessageSourceResolvable?
) : RuntimeException() {
    constructor(msg: String, code: ErrorCode = ErrorCode.REFUSED_STREAM, extra: Map<String, String> = emptyMap(), title: String? = null) : this(
        msg = msg,
        resolvable = null,
        code = code,
        extra = extra,
        httpStatus = HttpStatus.BAD_REQUEST,
        title = title,
        titleResolvable = null
    )

    override val message: String?
        get() = msg ?: resolvable?.toString()
}
