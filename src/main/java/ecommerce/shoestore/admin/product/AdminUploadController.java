package ecommerce.shoestore.admin.product;

import ecommerce.shoestore.auth.image.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/admin/uploads")
@RequiredArgsConstructor
@Slf4j
public class AdminUploadController {

    private final FileUploadService fileUploadService;

    @PostMapping("/images")
    public ResponseEntity<?> uploadProductImage(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File không được trống"));
        }

        try {
            String url = fileUploadService.uploadFileToFolder(file, "shoe_store_product");
            return ResponseEntity.ok(Map.of("url", url));
        } catch (IOException e) {
            log.error("Upload ảnh thất bại", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Không thể upload ảnh. Vui lòng thử lại."));
        }
    }
}