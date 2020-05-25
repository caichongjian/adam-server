package org.caichongjian.server.startup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * 阅读Rust编程语言官网上提供的文档时，看到上面有一段代码线程池用得很不错。
 * 刚好adam-server中有一段代码线程池用得十分辣眼睛，就学习参考了以下链接中的代码。
 *
 * @see https://doc.rust-lang.org/book/ch20-03-graceful-shutdown-and-cleanup.html
 */
public class ThreadPool {

    private final ExecutorService executorService;
    private final BlockingQueue<Message> messageBlockingQueue;
    private final List<Worker> workers; // 也可以直接存储一个数字
    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadPool.class);

    public ThreadPool(int size) {
        messageBlockingQueue = new ArrayBlockingQueue<>(size * 2); // 开发时方便调试，可根据实际需要调整
        executorService = Executors.newFixedThreadPool(size);
        workers = new ArrayList<>(size);

        for (int i = 1; i <= size; i++) {
            Worker worker = new Worker(i, messageBlockingQueue);
            workers.add(worker);
            executorService.execute(worker);
        }
    }

    public void execute(Runnable job) throws InterruptedException {
        messageBlockingQueue.put(new JobMessage(job));
    }

    public void drop() throws InterruptedException {
        LOGGER.info("Sending terminate message to all workers.");
        for (int i = 0; i < workers.size(); i++) {
            messageBlockingQueue.put(new TerminateMessage());
        }
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.HOURS);
    }

    /**
     * <p>ThreadPool 向 Worker 发送的消息。</p>
     * <p>消息分两种类型：执行处理请求工作的消息和终止Worker的消息。</p>
     * <p/>
     * <p>实现同样的功能有两种办法：通过类名区分的方式和使用成员变量区分的方式。</p>
     * <p>目前在 adam-server 中选择了第一种办法。</p>
     */
    private interface Message {

    }

    private static final class TerminateMessage implements Message {

    }


    private static class JobMessage implements Message {

        private final Runnable job;

        JobMessage(Runnable job) {
            this.job = job;
        }

        public Runnable getJob() {
            return job;
        }
    }

    private static final class Worker implements Runnable {

        private final int id;
        private final BlockingQueue<Message> messageBlockingQueue;

        public Worker(int id, BlockingQueue<Message> messageBlockingQueue) {
            this.id = id;
            this.messageBlockingQueue = messageBlockingQueue;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    final Message message = messageBlockingQueue.take();
                    if (message instanceof TerminateMessage) {
                        LOGGER.debug("Worker {} was told to terminate.", id);
                        break;
                    }

                    LOGGER.debug("Worker {} got a job; executing.", id);
                    JobMessage jobMessage = (JobMessage) message;
                    jobMessage.getJob().run();

                } catch (InterruptedException e) {
                    LOGGER.error("InterruptedException: ", e);
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
