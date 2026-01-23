package at.technikum.springrestbackend.service;

import at.technikum.springrestbackend.dto.PostCreateDTO;
import at.technikum.springrestbackend.dto.PostUpdateDTO;
import at.technikum.springrestbackend.entity.Post;
import at.technikum.springrestbackend.entity.Profile;
import at.technikum.springrestbackend.entity.Role;
import at.technikum.springrestbackend.repository.PostRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    PostRepository postRepository;
    @Mock
    MinioService minioService;

    @InjectMocks
    PostService postService;

    private Profile authUser(Long id, Role role) {
        Profile p = new Profile();
        p.setId(id);
        p.setEmail("u@x.com");
        p.setUsernameDisplay("User");
        p.setRole(role);

        var auth = new UsernamePasswordAuthenticationToken(p, null, p.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
        return p;
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void create_unauthenticated_throws401() {
        PostCreateDTO dto = new PostCreateDTO();
        dto.setTitle("T");
        dto.setContent("C");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> postService.create(dto));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void update_notOwnerNotAdmin_throws403() {
        Profile me = authUser(1L, Role.USER);

        Post post = new Post();
        post.setId(10L);

        Profile author = new Profile();
        author.setId(2L);
        author.setUsernameDisplay("Author");
        post.setAuthor(author);

        post.setTitle("Old");
        post.setContent("Old");

        when(postRepository.findWithAuthorById(10L)).thenReturn(Optional.of(post));

        PostUpdateDTO dto = new PostUpdateDTO();
        dto.setTitle("New");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> postService.update(10L, dto));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void update_owner_success() {
        Profile me = authUser(1L, Role.USER);

        Post post = new Post();
        post.setId(10L);
        post.setAuthor(me);
        post.setTitle("Old");
        post.setContent("Old");

        when(postRepository.findWithAuthorById(10L)).thenReturn(Optional.of(post));
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));

        PostUpdateDTO dto = new PostUpdateDTO();
        dto.setTitle("New Title");

        var res = postService.update(10L, dto);
        assertEquals("New Title", res.getTitle());
    }

    @Test
    void uploadImage_invalidType_throws400() {
        Profile me = authUser(1L, Role.USER);

        Post post = new Post();
        post.setId(10L);
        post.setAuthor(me);

        when(postRepository.findWithAuthorById(10L)).thenReturn(Optional.of(post));

        MockMultipartFile file = new MockMultipartFile(
                "file", "x.txt", "text/plain", "abc".getBytes()
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> postService.uploadImage(10L, file));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void uploadImage_success_setsKey_andCallsMinio() {
        Profile me = authUser(1L, Role.USER);

        Post post = new Post();
        post.setId(10L);
        post.setAuthor(me);

        when(postRepository.findWithAuthorById(10L)).thenReturn(Optional.of(post));
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));

        MockMultipartFile file = new MockMultipartFile(
                "file", "img.png", "image/png", new byte[]{1, 2, 3}
        );

        var res = postService.uploadImage(10L, file);

        assertNotNull(res.getImageUrl());
        verify(minioService, times(1)).upload(anyString(), eq(file));
        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    void delete_owner_deletes_post() {
        Profile me = authUser(1L, Role.USER);
        Post post = new Post();
        post.setId(5L);
        post.setAuthor(me);

        when(postRepository.findWithAuthorById(5L)).thenReturn(Optional.of(post));

        postService.delete(5L);
        verify(postRepository, times(1)).delete(post);
    }



    @Test
    void delete_admin_deletes_others_post() {
        authUser(1L, Role.ADMIN);

        Profile other = new Profile();
        other.setId(2L);
        Post post = new Post();
        post.setId(5L);
        post.setAuthor(other);

        when(postRepository.findWithAuthorById(5L)).thenReturn(Optional.of(post));

        postService.delete(5L);
        verify(postRepository, times(1)).delete(post);
    }

    @Test
    void create_with_valid_data_succeeds() {
        authUser(1L, Role.USER);

        PostCreateDTO dto = new PostCreateDTO();
        dto.setTitle("Great Title");
        dto.setContent("Great Content");

        when(postRepository.save(any(Post.class))).thenAnswer(inv -> {
            Post p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        var res = postService.create(dto);
        assertNotNull(res.getId());
        assertEquals("Great Title", res.getTitle());
    }

    @Test
    void create_empty_content_throws400() {
        authUser(1L, Role.USER);

        PostCreateDTO dto = new PostCreateDTO();
        dto.setTitle("Title");
        dto.setContent("");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> postService.create(dto));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void uploadImage_jpeg_success() {
        Profile me = authUser(1L, Role.USER);
        Post post = new Post();
        post.setId(10L);
        post.setAuthor(me);

        when(postRepository.findWithAuthorById(10L)).thenReturn(Optional.of(post));
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));

        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg", new byte[]{1,2,3}
        );

        var res = postService.uploadImage(10L, file);
        assertNotNull(res.getImageUrl());
    }

    @Test
    void uploadImage_webp_success() {
        Profile me = authUser(1L, Role.USER);
        Post post = new Post();
        post.setId(10L);
        post.setAuthor(me);

        when(postRepository.findWithAuthorById(10L)).thenReturn(Optional.of(post));
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));

        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.webp", "image/webp", new byte[]{1,2,3}
        );

        var res = postService.uploadImage(10L, file);
        assertNotNull(res.getImageUrl());
    }

    @Test
    void uploadImage_nonOwner_throws403() {
        authUser(1L, Role.USER);

        Profile other = new Profile();
        other.setId(2L);
        Post post = new Post();
        post.setId(10L);
        post.setAuthor(other);

        when(postRepository.findWithAuthorById(10L)).thenReturn(Optional.of(post));

        MockMultipartFile file = new MockMultipartFile(
                "file", "x.png", "image/png", new byte[]{1}
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> postService.uploadImage(10L, file));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

}
