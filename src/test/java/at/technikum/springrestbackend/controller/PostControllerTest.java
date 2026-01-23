package at.technikum.springrestbackend.controller;

import at.technikum.springrestbackend.config.TestSecurityConfig;
import at.technikum.springrestbackend.dto.PostCreateDTO;
import at.technikum.springrestbackend.dto.PostDTO;
import at.technikum.springrestbackend.dto.PostUpdateDTO;
import at.technikum.springrestbackend.exception.GlobalExceptionHandler;
import at.technikum.springrestbackend.security.JwtAuthenticationFilter;
import at.technikum.springrestbackend.service.PostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PostController.class)
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("PostController")
class PostControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    PostService postService;

    // ✅ важливо: щоб Spring НЕ створював реальний JwtAuthenticationFilter (і не падав на JwtService)
    @MockitoBean
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @Nested
    @DisplayName("GET /api/posts")
    class GetAllPosts {
        @Test
        @DisplayName("200 OK - returns page")
        void getAll_returnsPageOfPosts() throws Exception {
            PostDTO post = createTestPostDTO(1L, "Test Title", "Content");
            Page<PostDTO> page = new PageImpl<>(List.of(post));

            when(postService.getAll(any(), any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get("/api/posts"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].title").value("Test Title"));
        }
    }

    @Nested
    @DisplayName("GET /api/posts/{id}")
    class GetPostById {
        @Test
        @DisplayName("200 OK")
        void getById_existingPost_returnsPost() throws Exception {
            PostDTO post = createTestPostDTO(1L, "Title", "Content");
            when(postService.getById(1L)).thenReturn(post);

            mockMvc.perform(get("/api/posts/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Title"));
        }

        @Test
        @DisplayName("404 Not Found")
        void getById_nonExisting_returns404() throws Exception {
            when(postService.getById(999L))
                    .thenThrow(new EntityNotFoundException("Post not found"));

            mockMvc.perform(get("/api/posts/999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/posts")
    @WithMockUser
    class CreatePost {
        @Test
        @DisplayName("201 Created")
        void create_validRequest_returns201() throws Exception {
            PostCreateDTO request = new PostCreateDTO();
            request.setTitle("New Post");
            request.setContent("Content");

            PostDTO response = createTestPostDTO(1L, "New Post", "Content");
            when(postService.create(any())).thenReturn(response);

            mockMvc.perform(post("/api/posts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.title").value("New Post"));
        }

        @Test
        @DisplayName("400 Bad Request - empty title")
        void create_emptyTitle_returns400() throws Exception {
            PostCreateDTO request = new PostCreateDTO();
            request.setTitle("");
            request.setContent("Content");

            mockMvc.perform(post("/api/posts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /api/posts/{id}")
    @WithMockUser
    class UpdatePost {
        @Test
        @DisplayName("200 OK")
        void update_validRequest_returns200() throws Exception {
            PostUpdateDTO request = new PostUpdateDTO();
            request.setTitle("Updated Title");

            PostDTO response = createTestPostDTO(1L, "Updated Title", "Content");
            when(postService.update(eq(1L), any())).thenReturn(response);

            mockMvc.perform(put("/api/posts/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Updated Title"));
        }

        @Test
        @DisplayName("403 Forbidden")
        void update_notOwner_returns403() throws Exception {
            PostUpdateDTO request = new PostUpdateDTO();
            request.setTitle("Hacked");

            when(postService.update(eq(1L), any()))
                    .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN));

            mockMvc.perform(put("/api/posts/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("DELETE /api/posts/{id}")
    @WithMockUser
    class DeletePost {
        @Test
        @DisplayName("204 No Content")
        void delete_existingPost_returns204() throws Exception {
            doNothing().when(postService).delete(1L);

            mockMvc.perform(delete("/api/posts/1").with(csrf()))
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("POST /api/posts/{id}/image")
    @WithMockUser
    class UploadImage {
        @Test
        @DisplayName("200 OK")
        void uploadImage_validImage_returns200() throws Exception {
            MockMultipartFile file =
                    new MockMultipartFile("file", "image.png", "image/png", new byte[]{1, 2, 3});
            PostDTO response = createTestPostDTO(1L, "Title", "Content");

            when(postService.uploadImage(eq(1L), any())).thenReturn(response);

            mockMvc.perform(multipart("/api/posts/1/image")
                            .file(file)
                            .with(csrf()))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("400 Bad Request - invalid type")
        void uploadImage_invalidType_returns400() throws Exception {
            MockMultipartFile file =
                    new MockMultipartFile("file", "doc.pdf", "application/pdf", new byte[]{1, 2, 3});

            when(postService.uploadImage(eq(1L), any()))
                    .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST));

            mockMvc.perform(multipart("/api/posts/1/image")
                            .file(file)
                            .with(csrf()))
                    .andExpect(status().isBadRequest());
        }
    }

    private PostDTO createTestPostDTO(Long id, String title, String content) {
        return new PostDTO(
                id, 1L, "Author", title, content, null, LocalDateTime.now(), LocalDateTime.now()
        );
    }
}
