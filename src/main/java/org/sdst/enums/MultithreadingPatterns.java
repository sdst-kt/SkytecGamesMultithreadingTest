package org.sdst.enums;

public enum MultithreadingPatterns {
    ANONYMOUS_RUNNABLE, //classical synchronized operations
    EXECUTOR_SERVICE,   //same but using the Executor.newCachedThreadPool
    ATOMIC_INTEGER,     //case of AtomicInteger usage
    REENTRANT_LOCKS     //case of Locks interface usage
}
