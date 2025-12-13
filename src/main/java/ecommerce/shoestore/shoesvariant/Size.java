package ecommerce.shoestore.shoesvariant;

public enum Size {
    SIZE_35("35"),
    SIZE_36("36"),
    SIZE_37("37"),
    SIZE_38("38"),
    SIZE_39("39"),
    SIZE_40("40"),
    SIZE_41("41"),
    SIZE_42("42"),
    SIZE_43("43"),
    SIZE_44("44");

    private final String value;

    Size(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static Size fromValue(String value) {
        for (Size size : Size.values()) {
            if (size.value.equals(value)) {
                return size;
            }
        }
        throw new IllegalArgumentException("Invalid size: " + value);
    }
}