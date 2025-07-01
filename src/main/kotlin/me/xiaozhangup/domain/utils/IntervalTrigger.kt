package me.xiaozhangup.domain.utils

class IntervalTrigger(private var interval: Int) {
    private var counter = 0

    fun trigger(): Boolean {
        counter++
        if (counter >= interval) {
            counter = 0
            return true
        }
        return false
    }
}