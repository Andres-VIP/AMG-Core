package com.acme.platform.service;

import com.acme.platform.config.properties.S3Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3ObjectServiceTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Properties props;

    private S3ObjectService s3ObjectService;

    @BeforeEach
    void setUp() {
        when(props.bucket()).thenReturn("test-bucket");
        s3ObjectService = new S3ObjectService(s3Client, props);
    }

    @Test
    void listKeys_shouldReturnAllKeys() {
        S3Object obj1 = S3Object.builder().key("key1").build();
        S3Object obj2 = S3Object.builder().key("key2").build();

        ListObjectsV2Response response = ListObjectsV2Response.builder()
                .contents(Arrays.asList(obj1, obj2))
                .isTruncated(false)
                .build();

        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(response);

        List<String> keys = s3ObjectService.listKeys(null);

        assertEquals(2, keys.size());
        assertTrue(keys.contains("key1"));
        assertTrue(keys.contains("key2"));
        verify(s3Client, times(1)).listObjectsV2(any(ListObjectsV2Request.class));
    }

    @Test
    void listKeys_withPrefix_shouldFilterByPrefix() {
        S3Object obj = S3Object.builder().key("prefix/key1").build();

        ListObjectsV2Response response = ListObjectsV2Response.builder()
                .contents(Arrays.asList(obj))
                .isTruncated(false)
                .build();

        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(response);

        List<String> keys = s3ObjectService.listKeys("prefix/");

        assertEquals(1, keys.size());
        assertEquals("prefix/key1", keys.get(0));
        verify(s3Client).listObjectsV2(argThat((ListObjectsV2Request req) -> req.prefix().equals("prefix/")));
    }

    @Test
    void listKeys_withPagination_shouldFetchAllPages() {
        S3Object obj1 = S3Object.builder().key("key1").build();
        S3Object obj2 = S3Object.builder().key("key2").build();

        ListObjectsV2Response firstPage = ListObjectsV2Response.builder()
                .contents(Arrays.asList(obj1))
                .isTruncated(true)
                .nextContinuationToken("token123")
                .build();

        ListObjectsV2Response secondPage = ListObjectsV2Response.builder()
                .contents(Arrays.asList(obj2))
                .isTruncated(false)
                .build();

        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
                .thenReturn(firstPage)
                .thenReturn(secondPage);

        List<String> keys = s3ObjectService.listKeys(null);

        assertEquals(2, keys.size());
        verify(s3Client, times(2)).listObjectsV2(any(ListObjectsV2Request.class));
    }

    @Test
    void putText_shouldUploadTextToS3() {
        s3ObjectService.putText("test-key", "test content");

        verify(s3Client).putObject(
                argThat((PutObjectRequest req) -> req.bucket().equals("test-bucket") && req.key().equals("test-key")),
                any(RequestBody.class)
        );
    }

    @Test
    void getText_shouldRetrieveTextFromS3() {
        String content = "test content";
        GetObjectResponse response = GetObjectResponse.builder().build();
        ResponseBytes<GetObjectResponse> responseBytes = ResponseBytes.fromByteArray(response, content.getBytes(StandardCharsets.UTF_8));

        when(s3Client.getObjectAsBytes(any(GetObjectRequest.class))).thenReturn(responseBytes);

        String result = s3ObjectService.getText("test-key");

        assertEquals(content, result);
        verify(s3Client).getObjectAsBytes(argThat((GetObjectRequest req) -> req.bucket().equals("test-bucket") && req.key().equals("test-key")));
    }
}

