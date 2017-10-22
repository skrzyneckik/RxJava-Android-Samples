package com.morihacky.android.rxjava

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import timber.log.Timber
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class BatchingDebugTree : Timber.Tree() {

    private val eventsSubject : Subject<Log> = BehaviorSubject.create()

    init {
        this.eventsSubject
                .observeOn(Schedulers.from(Executors.newSingleThreadExecutor()))
                .buffer(Observable.merge(Observable.interval(10, TimeUnit.SECONDS), subject))
                .filter { list -> list.isNotEmpty() }
                .map { list -> Logs(list) }
                .subscribe {
                    logs -> logs.send()
                }
    }

    override fun log(priority: Int, tag: String?, message: String?, t: Throwable?) {
        eventsSubject.onNext(Log(priority, tag, message, t))
    }

    companion object {
        private val subject : Subject<Long> = BehaviorSubject.create()

        fun flush() {
            subject.onNext(0L)
        }
    }
}

data class Log(val priority: Int, val tag: String?, val message: String?, val t: Throwable?)

class Logs constructor(logs: List<Log>) {

    val logs: List<Log> = logs

    fun send() {
        android.util.Log.d(BatchingDebugTree::javaClass.name, String.format("Send %d logs", logs.size), null)
    }
}