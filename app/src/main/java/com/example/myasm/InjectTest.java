package com.example.myasm;

public class InjectTest {

    public void TestUnit() throws InterruptedException {
        long l=System.currentTimeMillis();
        Thread.sleep(1000);
        long e=System.currentTimeMillis();
        System.out.println("execute:"+(e-l)+" ms.");
    }
}
