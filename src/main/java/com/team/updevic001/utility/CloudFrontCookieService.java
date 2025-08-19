package com.team.updevic001.services.impl;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cloudfront.cookie.CloudFrontCookieSigner;
import software.amazon.awssdk.services.cloudfront.cookie.CookiesForCannedPolicy;
import software.amazon.awssdk.services.cloudfront.cookie.CannedSignerRequest;

import java.io.File;
import java.time.Instant;
import java.util.Date;

@Service
public class CloudFrontCookieService {

    private static final String DISTRIBUTION_DOMAIN = "d32vmhzz9hmwha.cloudfront.net"; // Sənin domain
    private static final String KEY_PAIR_ID = "APKAxxxxxxxxxxxx"; // AWS CloudFront Key Pair ID
    private static final String PRIVATE_KEY_PATH = "keys/cloudfront_private_key.pem"; // resources/keys/

    /**
     * Signed cookies yaratmaq üçün metod
     * @param resourcePath video faylının path-i, məsələn: "videos/lesson1.mp4"
     * @param validSeconds cookie-nin etibarlılıq müddəti saniyə ilə
     * @return CookiesForCannedPolicy obyektində Policy, Signature və Key-Pair ID
     */
    public CookiesForCannedPolicy generateSignedCookies(String resourcePath, long validSeconds) throws Exception {
        // resources qovluğundan private key-i oxu
        File privateKeyFile = new ClassPathResource(PRIVATE_KEY_PATH).getFile();

        // expiration vaxtı
        Date expirationDate = Date.from(Instant.now().plusSeconds(validSeconds));

        // CannedPolicy request
        CannedSignerRequest request = CannedSignerRequest.builder()
                .resourceUrl("https://" + DISTRIBUTION_DOMAIN + "/" + resourcePath)
                .privateKey(privateKeyFile.toPath())
                .keyPairId(KEY_PAIR_ID)
                .expirationDate(expirationDate)
                .build();

        // Signed cookies yarat
        CookiesForCannedPolicy cookies = CloudFrontCookieSigner.getCookiesForCannedPolicy(request);
        return cookies;
    }
}
