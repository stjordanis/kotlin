/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.js.test.interop

import com.eclipsesource.v8.V8
import com.eclipsesource.v8.V8Array
import com.eclipsesource.v8.V8Object
import com.eclipsesource.v8.utils.V8ObjectUtils
import java.io.File

class InteropV8 : InteropEngine {
    private val myRuntime: V8 = V8.createV8Runtime("global")

    private fun createParams(vararg p: Any?): V8Array {
        val params = V8Array(myRuntime)

        p.forEach {when {
            it == null -> params.pushNull()
            it is Int -> params.push(it)
            it is Double -> params.push(it)
            it is String -> params.push(it)
            it is Boolean -> params.push(it)
            it is V8Object -> params.push(it)
            else -> throw Exception("can not cast ${it}")
        }}

        return params
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> eval(script: String): T {
        return myRuntime.executeScript(script) as T
    }

    override fun evalVoid(script: String) {
        return myRuntime.executeVoidScript(script)
    }

    override fun getGlobalContext(): GlobalRuntimeContext {
        val v8result = eval<V8Object>("this")
        return V8ObjectUtils.toMap(v8result) as GlobalRuntimeContext
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> callMethod(obj: Any, name: String, vararg args: Any?): T {
        if (obj !is V8Object) {
            throw Exception("InteropV8 can deal only with V8Object")
        }

        return obj.executeFunction(name, V8Array(myRuntime)) as T
    }

    override fun loadFile(path: String) {
        evalVoid(File(path).bufferedReader().use { it.readText() })
    }
}