package com.bts.entity;

public enum CategoryEnum {
    AUTOMOTIVE("Automotive"),
    BEAUTY("Beauty"),
    BOOKS("Books"),
    CLOTHES("Clothes"),
    ELECTRONICS("Electronics"),
    FASHION("Fashion"),
    FURNITURE("Furniture"),
    GROCERY("Grocery"),
    HOME("Home & Living"),
    MUSIC("Music"),
    SPORTS("Sports"),
    TOYS("Toys");

    private final String displayName;

    CategoryEnum(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}