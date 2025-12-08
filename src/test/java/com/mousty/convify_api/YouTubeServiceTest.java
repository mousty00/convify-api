package com.mousty.convify_api;

import com.mousty.convify_api.service.YouTubeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static java.nio.file.Files.list;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class YouTubeServiceTest {

    private static final String TEST_DOWNLOAD_DIR = "/tmp/test-yt-downloads";

    @Spy
    @InjectMocks
    private YouTubeService youTubeService = new YouTubeService(TEST_DOWNLOAD_DIR);

    YouTubeServiceTest() throws IOException {}

    @BeforeEach
    void setUp() {
        Path testDir = Paths.get(TEST_DOWNLOAD_DIR);
        try {
            if (!Files.exists(testDir)) {
                Files.createDirectories(testDir);
            }
            list(testDir).forEach(p -> {
                try { Files.delete(p); } catch (IOException ignored) { }
            });
        } catch (IOException e) {
            fail("Failed to setup test directory: " + e.getMessage());
        }
    }


    @Test
    void extractVideoIdFromUrl_validUrl_returnsId() {
        String url = "https://www.youtube.com/watch?v=dQw4w9WgXcQ";
        String expectedId = "dQw4w9WgXcQ";
        assertEquals(expectedId, youTubeService.extractVideoIdFromUrl(url));
    }

    @Test
    void extractVideoIdFromUrl_shortUrl_returnsId() {
        String url = "https://youtu.be/dQw4w9WgXcQ";
        String expectedId = "dQw4w9WgXcQ";
        assertEquals(expectedId, youTubeService.extractVideoIdFromUrl(url));
    }

    @Test
    void extractVideoIdFromUrl_invalidUrl_returnsNull() {
        String url = "https://www.google.com/search?q=video";
        assertNull(youTubeService.extractVideoIdFromUrl(url));
    }

    @Test
    void extractVideoTitle_commandSuccess_returnsCleanedTitle() throws Exception {
        String mockTitle = "My Awesome Vídeo (ft. John Doe)!";

        doReturn(mockTitle).when(youTubeService).runCommand(anyString());

        String expectedTitle = "My Awesome V-deo -ft. John Doe--";
        assertEquals(expectedTitle, youTubeService.extractVideoTitle("some-url"));
    }

    @Test
    void extractVideoTitle_commandFails_throwsException() throws Exception {
        // Mock runCommand to throw an exception
        doThrow(new IOException("yt-dlp error")).when(youTubeService).runCommand(anyString());
        Exception exception = assertThrows(Exception.class, () -> youTubeService.extractVideoTitle("some-url"));

        assertTrue(exception.getMessage().contains("Could not retrieve video title"));
    }

    // Integrated Tests for Conversion Logic (Mocking yt-dlp) ---

    @Test
    void convertAndPrepareResponse_validInput_returnsSuccessMap() throws Exception {
        String url = "https://youtu.be/testvideo";
        String format = "mp4";
        String mockTitle = "Test Video Title";

        doReturn("testvideo").when(youTubeService).extractVideoIdFromUrl(anyString());
        doReturn(mockTitle).when(youTubeService).extractVideoTitle(anyString());

        doReturn("").when(youTubeService).runCommand(anyString());

        Path expectedPath = Paths.get(TEST_DOWNLOAD_DIR, mockTitle + "." + format);
        try {
            Files.createFile(expectedPath);
        } catch (IOException e) {
            fail("Failed to create mock file: " + e.getMessage());
        }

        Map<String, String> response = youTubeService.convertAndPrepareResponse(url, format);

        assertEquals("success", response.get("status"));
        assertEquals(expectedPath.toString(), response.get("filePath"));
        verify(youTubeService, times(1)).runCommand(argThat(cmd -> cmd.contains(format)));
    }

    @Test
    void convertAndPrepareResponse_invalidUrl_throwsIllegalArgumentException() {
        String url = "invalid-url";
        String format = "mp4";

        doReturn(null).when(youTubeService).extractVideoIdFromUrl(anyString());

        assertThrows(IllegalArgumentException.class, () -> youTubeService.convertAndPrepareResponse(url, format));
    }

    @Test
    void convertAndPrepareResponse_conversionFails_throwsIOException() throws Exception {
        String url = "https://youtu.be/testvideo";
        String format = "mp3";
        String mockTitle = "Test Video Title";

        doReturn("testvideo").when(youTubeService).extractVideoIdFromUrl(anyString());
        doReturn(mockTitle).when(youTubeService).extractVideoTitle(anyString());

        doThrow(new IOException("Command failed")).when(youTubeService).runCommand(anyString());

        assertThrows(IOException.class, () -> youTubeService.convertAndPrepareResponse(url, format));
    }

    // --- Integrated Tests for Download Logic ---

    @Test
    void getDownloadData_fileFound_returnsData() throws IOException {
        Path mockFile = Paths.get(TEST_DOWNLOAD_DIR, "video.mp4");
        Files.createFile(mockFile);
        String filepath = mockFile.toString();

        Map<String, Object> data = youTubeService.getDownloadData(filepath);

        assertEquals("video/mp4", data.get("contentType"));
        assertEquals("video.mp4", data.get("filename"));
        assertInstanceOf(Resource.class, data.get("resource"));

        Files.delete(mockFile);
    }

    @Test
    void getDownloadData_fileNotFound_throwsFileNotFoundException() {
        String filepath = Paths.get(TEST_DOWNLOAD_DIR, "non-existent-file.mp3").toString();

        assertThrows(FileNotFoundException.class, () -> youTubeService.getDownloadData(filepath));
    }

    @Test
    void getDownloadData_emptyFilepath_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> youTubeService.getDownloadData(""));
    }
}