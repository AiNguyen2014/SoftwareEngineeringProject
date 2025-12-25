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
        return uploadFileToFolder(file, "shoe_store_avatar");
        }

        public String uploadFileToFolder(MultipartFile file, String folder) throws IOException {
        // Upload lên Cloudinary với folder truyền vào
        return cloudinary.uploader()
            .upload(file.getBytes(), ObjectUtils.asMap(
                "public_id", UUID.randomUUID().toString(),
                "folder", folder
            ))
            .get("url")
            .toString();
    }
}