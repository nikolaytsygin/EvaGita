package com.eva.evagita.controller;

import com.eva.evagita.dto.DashboardResponse;
import com.eva.evagita.exception.GlobalExceptionHandler;
import com.eva.evagita.model.User;
import com.eva.evagita.repository.UserRepository;
import com.eva.evagita.security.JwtService;
import com.eva.evagita.security.SecurityConfig;
import com.eva.evagita.service.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService dashboardService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JwtService jwtService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User(
                "test-user",
                "test@example.com",
                "password"
        );

        when(userRepository.findByUsername("test-user"))
                .thenReturn(Optional.of(testUser));
    }

    @Test
    void shouldGetDashboard() throws Exception {
        DashboardResponse response = new DashboardResponse();

        response.setTotalTasks(10L);
        response.setTodoTasks(4L);
        response.setInProgressTasks(3L);
        response.setDoneTasks(3L);
        response.setLowPriorityTasks(2L);
        response.setMediumPriorityTasks(5L);
        response.setHighPriorityTasks(3L);
        response.setOverdueTasks(1L);

        when(dashboardService.getDashboard(testUser))
                .thenReturn(response);

        mockMvc.perform(get("/api/dashboard")
                        .with(user("test-user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTasks").value(10))
                .andExpect(jsonPath("$.todoTasks").value(4))
                .andExpect(jsonPath("$.inProgressTasks").value(3))
                .andExpect(jsonPath("$.doneTasks").value(3))
                .andExpect(jsonPath("$.lowPriorityTasks").value(2))
                .andExpect(jsonPath("$.mediumPriorityTasks").value(5))
                .andExpect(jsonPath("$.highPriorityTasks").value(3))
                .andExpect(jsonPath("$.overdueTasks").value(1));

        verify(userRepository).findByUsername("test-user");
        verify(dashboardService).getDashboard(testUser);
    }
}
