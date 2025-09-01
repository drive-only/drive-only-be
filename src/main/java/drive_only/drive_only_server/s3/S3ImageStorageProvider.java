package drive_only.drive_only_server.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;

import drive_only.drive_only_server.exception.custom.BusinessException;
import drive_only.drive_only_server.exception.errorcode.ErrorCode;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Component
@RequiredArgsConstructor
public class S3ImageStorageProvider {

    private final AmazonS3 amazonS3;

    @Value("${s3.bucket}")
    private String bucketName;

    @Value("${cdn.domain:}")        // 조회용(없으면 S3 URL 반환)
    private String cdnDomain;

    private static final String TEMP_PREFIX = "temp";
    private static final String PROFILE_PREFIX = "profiles";
    private static final Set<String> ALLOWED_EXT = Set.of("jpg","png","webp","gif");

    // === 결과 객체 ===
    @Getter
    @AllArgsConstructor
    public static class UploadResult {
        private final String s3Key;     // S3 object key
        private final String cdnUrl;    // https://cdn.../key (없으면 S3 URL)
        private final long size;
        private final String contentType;
    }

    public String saveBase64Image(String base64Data, String email) {
        try {
            String[] parts = base64Data.split(",");
            String metadata = parts[0]; // data:image/png;base64
            String data = parts[1];

            String extension = metadata.contains("png") ? "png" : "jpg";
            byte[] decoded = Base64.getDecoder().decode(data);

            String fileName = String.format("uploads/%s/%s.%s",
                    LocalDate.now(), UUID.randomUUID(), extension);

            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(decoded.length);
            objectMetadata.setContentType("image/" + extension);

            ByteArrayInputStream inputStream = new ByteArrayInputStream(decoded);

            amazonS3.putObject(bucketName, fileName, inputStream, objectMetadata);

            return amazonS3.getUrl(bucketName, fileName).toString();

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAIL);
        }
    }

    public String saveMultipartFile(MultipartFile file, String ownerEmail) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_DATA);
        }

        // 확장자/콘텐트타입 결정
        String original = file.getOriginalFilename();
        String ext = null;
        if (original != null && original.lastIndexOf('.') != -1) {
            ext = original.substring(original.lastIndexOf('.') + 1).toLowerCase();
        }
        if (ext == null || ext.isBlank()) {
            String ct = file.getContentType(); // e.g. image/jpeg
            if (ct != null && ct.startsWith("image/")) {
                ext = ct.substring("image/".length()).toLowerCase(); // jpeg, png, webp, gif...
            }
        }
        // 확장자 정규화 및 허용 타입 체크
        if ("jpeg".equals(ext)) ext = "jpg";
        if (ext == null || !(ext.equals("jpg") || ext.equals("png") || ext.equals("webp") || ext.equals("gif"))) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_DATA);
        }

        // S3 object key (이메일/날짜 기준으로 디렉토리 구성)
        String owner = (ownerEmail == null || ownerEmail.isBlank()) ? "anonymous" : ownerEmail;
        String key = String.format("uploads/%s/%s/%s.%s",
                owner, LocalDate.now(), UUID.randomUUID(), ext);

        // 메타데이터
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(file.getSize());
        meta.setContentType("image/" + ("jpg".equals(ext) ? "jpeg" : ext));

        try (InputStream in = file.getInputStream()) {
            amazonS3.putObject(bucketName, key, in, meta);
            return amazonS3.getUrl(bucketName, key).toString();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAIL);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAIL);
        }
    }

    // === 키 생성 규칙 ===
    public String buildKey(String ownerEmail, String originalFilename) {
        String owner = (ownerEmail == null || ownerEmail.isBlank())
                ? "anonymous"
                : ownerEmail.replaceAll("[^a-zA-Z0-9_.-]", "_");
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String ext = guessExt(originalFilename);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return "uploads/%s/%s/%s.%s".formatted(owner, date, uuid, ext);
    }

    // === 단건 업로드(키 지정) ===
    public UploadResult uploadOne(String key, MultipartFile file) throws Exception {
        String ct = Optional.ofNullable(file.getContentType()).orElse("application/octet-stream");
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(file.getSize());
        meta.setContentType(ct);
        try (InputStream in = file.getInputStream()) {
            amazonS3.putObject(bucketName, key, in, meta);
        }
        return new UploadResult(key, publicUrl(key), file.getSize(), ct);
    }

    // === 주문 순서대로 배치 업로드 + 실패 시 롤백 ===
    public Map<String, UploadResult> uploadManyInOrder(List<String> order, List<MultipartFile> files, String ownerEmail) {
        List<String> safeOrder = (order == null) ? List.of() : order;
        List<MultipartFile> safeFiles = (files == null) ? List.of() : files;

        if (safeOrder.size() != safeFiles.size())
            throw new BusinessException(ErrorCode.INVALID_PHOTO_MAPPING);
        if (safeFiles.size() > 50)
            throw new BusinessException(ErrorCode.INVALID_IMAGE_DATA);

        Map<String, UploadResult> result = new LinkedHashMap<>();
        List<String> uploadedKeys = new ArrayList<>();

        try {
            for (int i = 0; i < safeOrder.size(); i++) {
                MultipartFile f = safeFiles.get(i);
                if (f == null || f.isEmpty()) throw new BusinessException(ErrorCode.INVALID_IMAGE_DATA);
                String ct = Optional.ofNullable(f.getContentType()).orElse("");
                if (!ct.startsWith("image/")) throw new BusinessException(ErrorCode.INVALID_IMAGE_DATA);

                String key = buildKey(ownerEmail, f.getOriginalFilename());
                UploadResult ur = uploadOne(key, f);
                uploadedKeys.add(ur.getS3Key());

                String logicalKey = safeOrder.get(i).trim(); // 예: cp1_1
                result.put(logicalKey, ur);
            }
            return result;
        } catch (RuntimeException re) {
            deleteQuietly(uploadedKeys);
            throw re;
        } catch (Exception ex) {
            deleteQuietly(uploadedKeys);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAIL);
        }
    }

    // === 일괄 삭제(롤백/정리) ===
    public void deleteQuietly(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) return;
        try {
            DeleteObjectsRequest req = new DeleteObjectsRequest(bucketName)
                    .withKeys(keys.stream().map(DeleteObjectsRequest.KeyVersion::new).toList());
            amazonS3.deleteObjects(req);
        } catch (Exception ignore) {}
    }

    /** 임시 업로드: temp/{yyyy/MM/dd}/{uuid}.{ext} */
    public UploadResult uploadTemp(MultipartFile file, String ownerEmail) {
        if (file == null || file.isEmpty()) throw new BusinessException(ErrorCode.INVALID_IMAGE_DATA);

        String ext = resolveExt(file.getOriginalFilename(), file.getContentType());
        validateExt(ext);

        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String key = "%s/%s/%s.%s".formatted(TEMP_PREFIX, date, uuid(), ext);

        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(safeSize(file.getSize()));
        meta.setContentType(contentTypeFor(ext));
        meta.setCacheControl(defaultCacheControl());

        try (InputStream in = file.getInputStream()) {
            amazonS3.putObject(bucketName, key, in, meta);
            return new UploadResult(key, publicUrl(key), meta.getContentLength(), meta.getContentType());
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAIL);
        }
    }

    /**
     * temp URL(또는 key)을 받아서 profiles/{memberId}/{yyyy/MM/dd}/{uuid}.{ext}로 승격
     * - copy → (옵션) temp 삭제
     * - 최종 CDN URL 반환
     */
    public UploadResult promoteTempToProfile(String tempUrlOrKey, Long memberId) {
        String tempKey = keyFromUrl(tempUrlOrKey);
        if (!isTempKey(tempKey)) {
            // 이미 영구 경로이거나 외부 URL이면 그대로 래핑해서 반환
            return new UploadResult(tempKey, urlOrPassthrough(tempUrlOrKey, tempKey), -1, guessContentTypeFromKey(tempKey));
        }

        String ext = guessExt(tempKey);
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String destKey = "%s/%d/%s/%s.%s".formatted(PROFILE_PREFIX, memberId, date, uuid(), ext);

        try {
            // 메타 복사 + 캐시 정책
            ObjectMetadata srcMeta = amazonS3.getObjectMetadata(bucketName, tempKey);
            CopyObjectRequest copyReq = new CopyObjectRequest(bucketName, tempKey, bucketName, destKey)
                    .withNewObjectMetadata(cloneMetaWithCache(srcMeta));
            amazonS3.copyObject(copyReq);

            // (옵션) 원본 삭제
            amazonS3.deleteObject(bucketName, tempKey);

            String cdn = publicUrl(destKey);
            return new UploadResult(destKey, cdn,
                    Optional.ofNullable(srcMeta.getContentLength()).orElse(0L),
                    Optional.ofNullable(srcMeta.getContentType()).orElse(guessContentTypeFromKey(destKey)));
        } catch (AmazonS3Exception e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAIL);
        }
    }

    // === 내부 유틸 ===
    private String publicUrl(String key) {
        if (cdnDomain != null && !cdnDomain.isBlank()) {
            return "https://" + cdnDomain + "/" + key;  // CloudFront 경유 조회
        }
        return amazonS3.getUrl(bucketName, key).toString(); // fallback
    }

    private String guessExt(String name) {
        if (name == null) return "jpg";
        String lower = name.toLowerCase(Locale.ROOT);
        int i = lower.lastIndexOf('.');
        String ext = (i>0) ? lower.substring(i+1) : "jpg";
        if ("jpeg".equals(ext)) ext = "jpg";
        return switch (ext) { case "jpg","png","webp","gif" -> ext; default -> "jpg"; };
    }

    private String urlOrPassthrough(String urlOrKey, String key) {
        if (urlOrKey != null && (urlOrKey.startsWith("http://") || urlOrKey.startsWith("https://"))) {
            return urlOrKey;
        }
        return publicUrl(key);
    }

    private String resolveExt(String originalFilename, String contentType) {
        String ext = guessExt(originalFilename);
        if (ext == null || ext.isBlank() || !ALLOWED_EXT.contains(ext)) {
            if (contentType != null && contentType.startsWith("image/")) {
                ext = contentType.substring("image/".length()).toLowerCase(Locale.ROOT);
                if ("jpeg".equals(ext)) ext = "jpg";
            }
        }
        return (ext == null || !ALLOWED_EXT.contains(ext)) ? "jpg" : ext;
    }

    private void validateExt(String ext) {
        if (!ALLOWED_EXT.contains(ext)) throw new BusinessException(ErrorCode.INVALID_IMAGE_DATA);
    }

    private String contentTypeFor(String ext) {
        return switch (ext) {
            case "jpg" -> "image/jpeg";
            case "png" -> "image/png";
            case "webp" -> "image/webp";
            case "gif" -> "image/gif";
            default -> "application/octet-stream";
        };
    }

    private String defaultCacheControl() {
        // 7일 캐시(원하는 정책으로 조정)
        return "public, max-age=604800, immutable";
    }

    private String sanitizeOwner(String ownerEmail) {
        return (ownerEmail == null || ownerEmail.isBlank())
                ? "anonymous"
                : ownerEmail.replaceAll("[^a-zA-Z0-9_.-]", "_");
    }

    private String uuid() { return UUID.randomUUID().toString().replace("-", ""); }

    private long safeSize(long size) {
        if (size <= 0) return 1; // 일부 드라이버가 size 0 보고시 에러 방지
        return size;
    }

    private boolean isTempKey(String key) {
        return key != null && (key.startsWith(TEMP_PREFIX + "/") || key.contains("/" + TEMP_PREFIX + "/"));
    }

    /** http(s)://cdn/.../key 또는 key 자체를 받아서 S3 key 추출 */
    private String keyFromUrl(String urlOrKey) {
        if (urlOrKey == null || urlOrKey.isBlank()) return "";
        if (urlOrKey.startsWith("http://") || urlOrKey.startsWith("https://")) {
            int idx = urlOrKey.indexOf('/', urlOrKey.indexOf("//") + 2);
            if (idx >= 0 && idx + 1 < urlOrKey.length()) {
                return urlOrKey.substring(idx + 1);
            }
            return ""; // 도메인만 온 경우
        }
        return urlOrKey;
    }

    private ObjectMetadata cloneMetaWithCache(ObjectMetadata src) {
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentType(src.getContentType());
        meta.setCacheControl(defaultCacheControl());
        meta.setContentLength(Optional.ofNullable(src.getContentLength()).orElse(0L));
        // 필요하면 ContentDisposition/Encoding/ETag 등도 복사
        return meta;
    }

    private String guessContentTypeFromKey(String key) {
        return contentTypeFor(guessExt(key));
    }
}

