package com.eva.evagita.service;

import com.eva.evagita.dto.DashboardResponse;
import com.eva.evagita.model.User;

public interface DashboardService {

    DashboardResponse getDashboard(User user);
}
