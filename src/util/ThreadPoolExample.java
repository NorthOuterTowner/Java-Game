package util;

import java.util.concurrent.*;

public class ThreadPoolExample {
    public static void main(String[] args) {
        // 创建线程池
        ExecutorService executor = Executors.newFixedThreadPool(5); // 创建一个固定大小的线程池

        // 提交任务给线程池
        for (int i = 0; i < 5; i++) {
            executor.submit(new Task(i));
        }
        executor.submit(new ATask());

        // 关闭线程池
        executor.shutdown();
    }
}

class ATask implements Runnable {
	public ATask() {
}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println("hujg");
	}
	
}	
class Task implements Runnable {
    private int taskId;

    public Task(int id) {
        this.taskId = id;
    }

    @Override
    public void run() {
        System.out.println("Task ID : " + taskId + " performed by " + Thread.currentThread().getName());
    }
}
