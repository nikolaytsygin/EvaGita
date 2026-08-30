package com.eva.evagita.security;

import com.eva.evagita.PostgresIntegrationTest;
import com.eva.evagita.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class TagSecurityIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private TagRepository tagRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        tagRepository.deleteAll();
    }

    @Test
    void getTags_shouldBeAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/tags"))
                .andExpect(status().isOk());
    }

    @Test
    void createTag_shouldBeAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/api/tags")
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "security-test"
                                }
                                """))
                .andExpect(status().isCreated());
    }
}
