package at.technikum.springrestbackend.controller;

import at.technikum.springrestbackend.service.MinioService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class FileControllerTest {

    @Test
    void getFile_validKey_returnsBytesAndContentType() {
        MinioService minioService = mock(MinioService.class);
        FileController controller = new FileController(minioService);

        byte[] bytes = "hello".getBytes();
        when(minioService.download("avatars/123/uuid.png"))
                .thenReturn(new MinioService.DownloadedFile(bytes, "image/png"));

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRequestURI("/api/files/avatars/123/uuid.png");

        ResponseEntity<byte[]> resp = controller.getFile(req);

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals("image/png", resp.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));
        assertArrayEquals(bytes, resp.getBody());
        verify(minioService).download("avatars/123/uuid.png");
    }

    @Test
    void getFile_missingKey_returns400() {
        MinioService minioService = mock(MinioService.class);
        FileController controller = new FileController(minioService);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRequestURI("/api/files/");

        ResponseEntity<byte[]> resp = controller.getFile(req);

        assertEquals(400, resp.getStatusCodeValue());
        verifyNoInteractions(minioService);
    }

    @Test
    void getFile_pathTraversal_returns400() {
        MinioService minioService = mock(MinioService.class);
        FileController controller = new FileController(minioService);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRequestURI("/api/files/../secret.txt");

        ResponseEntity<byte[]> resp = controller.getFile(req);

        assertEquals(400, resp.getStatusCodeValue());
        verifyNoInteractions(minioService);
    }

    @Test
    void getFile_encodedTraversal_returns400() {
        MinioService minioService = mock(MinioService.class);
        FileController controller = new FileController(minioService);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRequestURI("/api/files/%2e%2e/secret.txt"); // decodes to ../secret.txt

        ResponseEntity<byte[]> resp = controller.getFile(req);

        assertEquals(400, resp.getStatusCodeValue());
        verifyNoInteractions(minioService);
    }

    @Test
    void getFile_encodedLeadingSlash_returns400() {
        MinioService minioService = mock(MinioService.class);
        FileController controller = new FileController(minioService);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRequestURI("/api/files/%2Fsecret.txt"); // decodes to /secret.txt

        ResponseEntity<byte[]> resp = controller.getFile(req);

        assertEquals(400, resp.getStatusCodeValue());
        verifyNoInteractions(minioService);
    }
}
