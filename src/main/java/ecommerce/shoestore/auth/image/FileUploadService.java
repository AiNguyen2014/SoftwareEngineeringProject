package ecommerce.shoestore.auth.image;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final Cloudinary cloudinary;

    public String uploadFile(MultipartFile file) throws IOException {
        // Upload lên Cloudinary, folder "shoe_store_avatar"
        return cloudinary.uploader()
                .upload(file.getBytes(), ObjectUtils.asMap(
                        "public_id", UUID.randomUUID().toString(),
                        "folder", "shoe_store_avatar"
                ))
                .get("url")
                .toString();
    }
}