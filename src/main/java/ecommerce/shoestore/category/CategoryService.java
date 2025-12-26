package ecommerce.shoestore.category;

import ecommerce.shoestore.shoes.ShoesType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<Category> findByShoesType(ShoesType type) {
        if (type == null) {
            return categoryRepository.findAll();
        }
        return categoryRepository.findCategoriesByShoesType(type);
    }
}
