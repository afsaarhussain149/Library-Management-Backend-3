package com.library.servicesImpl;

import com.library.bean.ApiResponse;
import com.library.bean.ComplaintDetails;
import com.library.bean.JavaConstant;
import com.library.dao.IGenericDao;
import com.library.services.ComplaintService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@SuppressWarnings({ "rawtypes" })
public class ComplaintServiceImpl implements ComplaintService {

	@Autowired
	IGenericDao iGenericDao;

	// ============ GET /complaints & /get-complaints ============
	@Override
	public List<Map> getAllComplaints() {
		return iGenericDao.executeDDLSQL(JavaConstant.GET_ALL_COMPLAINTS, new Object[] {});
	}

	// ============ POST /add-complaint ============
	@Override
	@Transactional
	public ApiResponse addComplaint(ComplaintDetails d) {
		try {
			if (d.getUserId() == null || d.getMessage() == null) {
				return new ApiResponse(false, "userId & message required");
			}
			iGenericDao.executeDMLSQL(JavaConstant.INSERT_COMPLAINT,
					new Object[] { d.getUserId(), d.getMessage(), d.getIssueType() != null ? d.getIssueType() : "General" });
			return new ApiResponse(true, "Complaint saved successfully");
		} catch (Exception e) {
			e.printStackTrace();
			return new ApiResponse(false, "Server Error");
		}
	}

	// ============ DELETE /complaints/:id ============
	@Override
	@Transactional
	public ApiResponse deleteComplaint(Integer complaintId) {
		try {
			List<Map> existing = iGenericDao.executeDDLSQL(JavaConstant.GET_COMPLAINT_BY_ID, new Object[] { complaintId });
			if (existing == null || existing.isEmpty()) {
				return new ApiResponse(false, "Complaint not found");
			}
			iGenericDao.executeDMLSQL(JavaConstant.DELETE_COMPLAINT_BY_ID, new Object[] { complaintId });
			return new ApiResponse(true, "Complaint deleted successfully");
		} catch (Exception e) {
			e.printStackTrace();
			return new ApiResponse(false, "Server error");
		}
	}

	// ============ GET /get-complaints/:userId ============
	@Override
	public Map<String, Object> getComplaintsByUser(Integer userId) {
		Map<String, Object> result = new LinkedHashMap<>();
		List<Map> complaints = iGenericDao.executeDDLSQL(JavaConstant.GET_COMPLAINTS_BY_USER_ID, new Object[] { userId });
		result.put("success", true);
		result.put("count", complaints.size());
		result.put("data", complaints);
		return result;
	}
}
