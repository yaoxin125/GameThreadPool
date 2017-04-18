package com.snowcattle.game.thread.executor;

/**
 * Created by jiangwenping on 17/3/10.
 */

import com.snowcattle.game.thread.worker.AbstractWork;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 无序队列线程池
 * 
 * @author zhaohui
 *
 */
public class NonOrderedQueuePoolExecutor extends ThreadPoolExecutor {

	public NonOrderedQueuePoolExecutor(int corePoolSize) {
		super(corePoolSize, corePoolSize * 2, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
	}

	public void executeWork(AbstractWork work) {
		execute(work);
	}

}