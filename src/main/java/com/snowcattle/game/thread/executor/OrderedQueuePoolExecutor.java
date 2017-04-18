package com.snowcattle.game.thread.executor;

import com.snowcattle.game.common.constants.Loggers;
import com.snowcattle.game.thread.ThreadNameFactory;
import com.snowcattle.game.thread.worker.AbstractWork;
import com.snowcattle.game.thread.worker.OrderedQueuePool;
import com.snowcattle.game.thread.worker.TasksQueue;
import org.slf4j.Logger;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by jiangwenping on 17/3/10.
 */
public class OrderedQueuePoolExecutor extends ThreadPoolExecutor {

	protected Logger logger = Loggers.threadLogger;

	private OrderedQueuePool<Long, AbstractWork> pool = new OrderedQueuePool<Long, AbstractWork>();

	private int maxQueueSize;
	private ThreadNameFactory threadNameFactory;

	public OrderedQueuePoolExecutor(String name, int corePoolSize, int maxQueueSize) {
		super(corePoolSize, 2 * corePoolSize, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(),
				new ThreadNameFactory(name));
		this.maxQueueSize = maxQueueSize;
		this.threadNameFactory = (ThreadNameFactory) getThreadFactory();
	}

	/**
	 * 增加执行任务
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean addTask(long key, AbstractWork task) {
		TasksQueue<AbstractWork> queue = pool.getTasksQueue(key);
		boolean run = false;
		boolean result = false;
		synchronized (queue) {
			if (maxQueueSize > 0) {
				if (queue.size() > maxQueueSize) {
					logger.error("队列" + threadNameFactory.getNamePrefix() + "(" + key + ")" + "抛弃指令!");
					queue.clear();
				}
			}
			result = queue.add(task);
			if (result) {
				task.setTasksQueue(queue);
				{
					if (queue.isProcessingCompleted()) {
						queue.setProcessingCompleted(false);
						run = true;
					}
				}
			} else {
				logger.error("队列添加任务失败");
			}
		}
		if (run) {
			execute(queue.poll());
		}
		return result;
	}

	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		super.afterExecute(r, t);

		AbstractWork work = (AbstractWork) r;
		TasksQueue<AbstractWork> queue = work.getTasksQueue();
		if (queue != null) {
			AbstractWork afterWork = null;
			synchronized (queue) {
				afterWork = queue.poll();
				if (afterWork == null) {
					queue.setProcessingCompleted(true);
				}
			}
			if (afterWork != null) {
				execute(afterWork);
			}
		} else {
			logger.error("执行队列为空");
		}
	}

}
