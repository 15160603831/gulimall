package com.hwj.mall.search.thread;

import io.swagger.models.auth.In;

import java.util.concurrent.*;

public class TreadTest {
    public static ExecutorService executor = Executors.newFixedThreadPool(10);


    public static void main(String[] args) {
        System.out.println("main ---start");
        Future<Void> future = CompletableFuture.runAsync(() -> {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果：" + i);
        }, executor);
        System.out.println("main -----end");
    }

    public void thread(String[] args) throws ExecutionException, InterruptedException {

        System.out.println("main ---start");
        //todo 1)thread
//        Thread01 thread01 = new Thread01();
//        thread01.start();

        //todo 2） runnable
//        Runnable01 runnable01 = new Runnable01();
//        new Thread(runnable01).start();
//        System.out.println("main -----end");

        //todo 3）callable + futureTask 可以拿到返回结果，可以处理异常
        //   1.线程池创建
//        FutureTask<Integer> task = new FutureTask<>(new Callable01());
//        new Thread(task).start();
//        //阻塞 等待线程完成，拿到结果
//        Integer integer = task.get();
//        System.out.println("main -----end" + integer);

        //TODO 4）线程池 应该将所有异步任务交给线程池执行 减小开销
        //给线程池提交任务。当前线程池只有一两个，提交给线程池让他自己去执行
        executor.execute(new Runnable01());
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5,
                200, 10, TimeUnit.SECONDS, new LinkedBlockingDeque<>(100000),
                Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());


        /**
         * todo 区别
         * 1-2不能得到返回值 3可以获取返回值
         * 1、2、3都不能控制资源
         * 4可以控制资源，性能稳定
         */


    }

    public static class Thread01 extends Thread {
        @Override
        public void run() {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果：" + i);
        }
    }

    public static class Runnable01 implements Runnable {
        @Override
        public void run() {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果：" + i);
        }
    }

    public static class Callable01 implements Callable<Integer> {

        /**
         * Computes a result, or throws an exception if unable to do so.
         *
         * @return computed result
         * @throws Exception if unable to compute a result
         */
        @Override
        public Integer call() throws Exception {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果：" + i);
            return i;
        }
    }
}
