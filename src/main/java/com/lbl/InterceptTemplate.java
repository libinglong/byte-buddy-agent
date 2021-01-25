package com.lbl;

import net.bytebuddy.implementation.bind.annotation.*;

import java.lang.reflect.Method;

/**
 * The actual byte-buddy's interceptor to intercept class instance methods. In this class, it provide a bridge between
 * byte-buddy and sky-walking plugin.
 */
public class InterceptTemplate {

    /**
     * Intercept the target static method.
     *

     *
     * @return the return value of target static method.
     * @throws Exception only throw exception because of zuper.call() or unexpected exception in sky-walking ( This is a
     *                   bug, if anything triggers this condition ).
     */
    @RuntimeType
    public static Object intercept(@This Object obj, @AllArguments Object[] allArguments, @Morph OverrideCallable zuper,
                                   @Origin Method method) throws Throwable {
        System.out.println("intercept pre");

        System.out.println("intercept after");
        return null;
    }
}
