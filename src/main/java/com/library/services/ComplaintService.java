package com.library.services;

import com.library.bean.ApiResponse;
import com.library.bean.ComplaintDetails;
import java.util.List;
import java.util.Map;

public interface ComplaintService {
	List<Map> getAllComplaints();
	ApiResponse addComplaint(ComplaintDetails details);
	ApiResponse deleteComplaint(Integer complaintId);
	Map<String, Object> getComplaintsByUser(Integer userId);
}
