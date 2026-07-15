package com.library.servicesImpl;

import com.library.bean.JavaConstant;
import com.library.dao.IGenericDao;
import com.library.services.PlanService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Equivalent of Node's controller/plan.js getPlans() -
 * auto-seeds the catalog on first call if empty.
 */
@Service
@SuppressWarnings({ "rawtypes", "unchecked" })
public class PlanServiceImpl implements PlanService {

	@Autowired
	IGenericDao iGenericDao;

	@Override
	@Transactional
	public List<Map<String, Object>> getPlans() {
		List<Map> countRows = iGenericDao.executeDDLSQL(JavaConstant.COUNT_PLAN_CATALOG, new Object[] {});
		long count = ((Number) countRows.get(0).values().iterator().next()).longValue();

		if (count == 0) {
			seedDefaultPlans();
		}

		List<Map> plans = iGenericDao.executeDDLSQL(JavaConstant.GET_ALL_PLAN_CATALOG, new Object[] {});
		List<Map<String, Object>> result = new ArrayList<>();

		for (Map plan : plans) {
			Object planId = plan.get("plan_id");
			List<Map> options = iGenericDao.executeDDLSQL(JavaConstant.GET_OPTIONS_BY_PLAN_ID, new Object[] { planId });

			Map<String, Object> planMap = new LinkedHashMap<>();
			planMap.put("planId", planId);
			planMap.put("hours", plan.get("hours"));
			planMap.put("options", options);
			result.add(planMap);
		}
		return result;
	}

	private void seedDefaultPlans() {
		int[][] hoursAndPrices = {
				{ 4, 500, 1440, 2700, 4800 },
				{ 6, 650, 1860, 3420, 6600 },
				{ 8, 800, 2340, 4500, 8400 },
				{ 12, 1000, 2880, 5640, 10800 },
				{ 14, 1200, 3450, 6600, 12000 }
		};
		String[] optionNames = { "Monthly", "Quarterly", "Half Yearly", "Annual" };

		for (int[] row : hoursAndPrices) {
			int hours = row[0];
			iGenericDao.executeDMLSQL(JavaConstant.INSERT_PLAN_CATALOG, new Object[] { hours });

			List<Map> inserted = iGenericDao.executeDDLSQL(
					"select * from plan_catalog where hours = ?1 order by plan_id desc limit 1",
					new Object[] { hours });
			Object planId = inserted.get(0).get("plan_id");

			for (int i = 0; i < optionNames.length; i++) {
				iGenericDao.executeDMLSQL(JavaConstant.INSERT_PLAN_OPTION,
						new Object[] { planId, optionNames[i], row[i + 1] });
			}
		}
	}
}
