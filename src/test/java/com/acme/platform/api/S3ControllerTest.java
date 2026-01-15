package com.acme.platform.api;

import com.acme.platform.service.S3ObjectService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(S3Controller.class)
class S3ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private S3ObjectService s3ObjectService;

    @Test
    void listKeys_shouldReturnKeysList() throws Exception {
        List<String> keys = Arrays.asList("key1", "key2", "key3");
        when(s3ObjectService.listKeys(null)).thenReturn(keys);

        mockMvc.perform(get("/api/s3/keys"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("key1"))
                .andExpect(jsonPath("$[1]").value("key2"))
                .andExpect(jsonPath("$[2]").value("key3"));

        verify(s3ObjectService).listKeys(null);
    }

    @Test
    void listKeys_withPrefix_shouldReturnFilteredKeys() throws Exception {
        List<String> keys = Arrays.asList("prefix/key1");
        when(s3ObjectService.listKeys("prefix/")).thenReturn(keys);

        mockMvc.perform(get("/api/s3/keys").param("prefix", "prefix/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("prefix/key1"));

        verify(s3ObjectService).listKeys("prefix/");
    }

    @Test
    void putText_shouldCallServiceAndReturnAccepted() throws Exception {
        mockMvc.perform(post("/api/s3/text")
                        .param("key", "test-key")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("test content"))
                .andExpect(status().isAccepted());

        verify(s3ObjectService).putText("test-key", "test content");
    }

    @Test
    void putText_withNullBody_shouldHandleAsEmptyString() throws Exception {
        mockMvc.perform(post("/api/s3/text")
                        .param("key", "test-key")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(""))
                .andExpect(status().isAccepted());

        verify(s3ObjectService).putText("test-key", "");
    }

    @Test
    void getText_shouldReturnTextContent() throws Exception {
        when(s3ObjectService.getText("test-key")).thenReturn("test content");

        mockMvc.perform(get("/api/s3/text").param("key", "test-key"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string("test content"));

        verify(s3ObjectService).getText("test-key");
    }

    @Test
    void putText_withBlankKey_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/s3/text")
                        .param("key", "   ")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("test content"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getText_withBlankKey_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/s3/text").param("key", ""))
                .andExpect(status().isBadRequest());
    }
}

