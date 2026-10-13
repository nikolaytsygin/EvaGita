package com.eva.evagita.security;

import com.eva.evagita.PostgresIntegrationTest;
import com.eva.evagita.model.Tag;
import com.eva.evagita.model.User;
import com.eva.evagita.repository.NotificationRepository;
import com.eva.evagita.repository.ProjectRepository;
import com.eva.evagita.repository.TagRepository;
import com.eva.evagita.repository.TaskRepository;
import com.eva.evagita.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class TagSecurityIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    private MockMvc mockMvc;

    private User testUser;

    private User anotherUser;

    private String testUserToken;

    private String anotherUserToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        taskRepository.deleteAll();
        projectRepository.deleteAll();
        tagRepository.deleteAll();
        notificationRepository.deleteAll();
        userRepository.deleteAll();

        testUser = userRepository.save(new User(
                "tag-security-user",
                "tag-security@example.com",
                "password"
        ));

        anotherUser = userRepository.save(new User(
                "another-tag-security-user",
                "another-tag-security@example.com",
                "password"
        ));

        testUserToken = jwtService.generateToken(testUser.getUsername());
        anotherUserToken = jwtService.generateToken(anotherUser.getUsername());
    }

    @Test
    void getTags_shouldRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/tags"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createTag_shouldRequireAuthentication() throws Exception {
        mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "security-test"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAnotherUsersTag_shouldReturnNotFound() throws Exception {
        Tag foreignTag = createTag(anotherUser, "private-tag");

        mockMvc.perform(get("/api/tags/{id}", foreignTag.getId())
                        .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateAnotherUsersTag_shouldReturnNotFoundAndKeepOriginalName()
            throws Exception {
        Tag foreignTag = createTag(anotherUser, "original-tag");

        mockMvc.perform(put("/api/tags/{id}", foreignTag.getId())
                        .header("Authorization", "Bearer " + testUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "changed-by-attacker"
                                }
                                """))
                .andExpect(status().isNotFound());

        assertThat(tagRepository.findById(foreignTag.getId()))
                .isPresent()
                .get()
                .extracting(Tag::getName)
                .isEqualTo("original-tag");
    }

    @Test
    void deleteAnotherUsersTag_shouldReturnNotFoundAndKeepTag()
            throws Exception {
        Tag foreignTag = createTag(anotherUser, "tag-to-protect");

        mockMvc.perform(delete("/api/tags/{id}", foreignTag.getId())
                        .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isNotFound());

        assertThat(tagRepository.findById(foreignTag.getId()))
                .isPresent()
                .get()
                .extracting(Tag::getName)
                .isEqualTo("tag-to-protect");
    }

    private Tag createTag(User owner, String name) {
        Tag tag = new Tag();
        tag.setName(name);
        tag.setUser(owner);
        return tagRepository.save(tag);
    }
}
