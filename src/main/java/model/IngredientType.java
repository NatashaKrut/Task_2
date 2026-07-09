package model;

import service.TestDataHandler;

public enum IngredientType {
    BUN,
    SAUCE,
    MAIN;

    final TestDataHandler tdh = new TestDataHandler();

    public String getHash() {
        return tdh.getIngredientHashByType(this.name().toLowerCase());
    }
}