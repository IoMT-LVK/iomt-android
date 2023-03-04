package com.iomt.android

/**
 * Functional interface that represents some action
 */
fun interface Action {
    /**
     * @param args arguments that should be passed to [Action]
     */
    fun run(args: Array<String?>?)
}
