package com.hino.rxjavaplayground;

import org.junit.Test;

import io.reactivex.Observable;

import static org.junit.Assert.*;

public class RxJavaUnitTest {

    String result = "";

    @Test
    public void returnAValue() {
        Observable<String> observable = Observable.just("Hello");  // provides data A
        observable.subscribe(s -> result = s); // Callable as subscriber
        assertTrue(result.equals("Hello"));
    }

}