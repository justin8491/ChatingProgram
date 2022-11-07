package ch14.sec09.exam03;

import java.time.LocalTime;
import java.util.concurrent.ExecutionException;

@FunctionalInterface
interface MyCallable {
    int call() throws Exception;
}

@FunctionalInterface
interface MyFuture {
    int get();
}

class MyThread extends Thread {
    MyCallable myclass;
    MyFuture myFuture;
    int result = 0;
    
    public MyFuture submit(MyCallable myclass) {
        myFuture = new MyFuture() {
            public int get() {
                try {
                    synchronized (this) {
                        wait();    
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return result;
            }
        };
        this.myclass = myclass;
        start();
        return myFuture;
    }

    @Override
    public void run() {
        if (this.myclass != null) {
            try {
                result = myclass.call();
                synchronized (myFuture) {
                    myFuture.notify();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

public class CallableSubmitExample2 {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        MyThread myThread = new MyThread();

        MyFuture future = myThread.submit(() -> {
            System.out.println(LocalTime.now() + " Starting runnable");
            Integer sum = 1 + 1;
            Thread.sleep(3000);
            return sum;
        });

        System.out.println(LocalTime.now() + " Waiting the task done");
        Integer result = future.get();
        System.out.println(LocalTime.now() + " Result : " + result);
    }
}