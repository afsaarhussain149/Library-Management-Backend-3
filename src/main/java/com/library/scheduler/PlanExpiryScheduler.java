package com.library.scheduler;

import com.library.bean.JavaConstant;
import com.library.dao.IGenericDao;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Equivalent of Node's sheduler/server.js cron job - runs every minute and
 * marks paid plans whose duration has elapsed as inactive + seat-blocked,
 * freeing up the seat for new bookings.
 */
@Component
public class PlanExpiryScheduler {

	@Autowired
	IGenericDao iGenericDao;

	@Scheduled(cron = "0 * * * * *") // every minute, same interval as the Node cron job
	@Transactional
	public void expireOldPlans() {
		try {
			iGenericDao.executeDMLSQL(JavaConstant.EXPIRE_PLANS_JOB, new Object[] {});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
