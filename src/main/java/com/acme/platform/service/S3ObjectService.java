package com.acme.platform.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import com.acme.platform.config.properties.S3Properties;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class S3ObjectService {

    private static final Logger logger = LoggerFactory.getLogger(S3ObjectService.class);

    private final S3Client s3Client;
    private final S3Properties props;

    public S3ObjectService(S3Client s3Client, S3Properties props) {
        this.s3Client = s3Client;
        this.props = props;
    }

    public List<String> listKeys(String prefix) {
        logger.debug("Listing keys with prefix: {}", prefix);
        List<String> keys = new ArrayList<>();
        String continuationToken = null;

        do {
            ListObjectsV2Request.Builder req = ListObjectsV2Request.builder()
                    .bucket(props.bucket())
                    .maxKeys(1000);

            if (prefix != null && !prefix.isBlank()) {
                req.prefix(prefix);
            }
            if (continuationToken != null) {
                req.continuationToken(continuationToken);
            }

            ListObjectsV2Response res = s3Client.listObjectsV2(req.build());
            res.contents().forEach(o -> keys.add(o.key()));
            continuationToken = res.isTruncated() ? res.nextContinuationToken() : null;
        } while (continuationToken != null);

        logger.debug("Found {} keys", keys.size());
        return keys;
    }

    public void putText(String key, String text) {
        logger.debug("Putting text object with key: {}", key);
        PutObjectRequest req = PutObjectRequest.builder()
                .bucket(props.bucket())
                .key(key)
                .contentType("text/plain; charset=utf-8")
                .build();

        s3Client.putObject(req, RequestBody.fromString(text, StandardCharsets.UTF_8));
        logger.debug("Successfully put object with key: {}", key);
    }

    public String getText(String key) {
        logger.debug("Getting text object with key: {}", key);
        GetObjectRequest req = GetObjectRequest.builder()
                .bucket(props.bucket())
                .key(key)
                .build();

        ResponseBytes<GetObjectResponse> bytes = s3Client.getObjectAsBytes(req);
        logger.debug("Successfully retrieved object with key: {}", key);
        return bytes.asString(StandardCharsets.UTF_8);
    }
}
