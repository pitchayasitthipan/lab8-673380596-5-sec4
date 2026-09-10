package com.example.demo.strategy;

import org.springframework.stereotype.Component;

@Component
public class DiscountContext {
    public DiscountStrategy getStrategy(String discountType) {
        if (discountType == null) {
            return new NoDiscountStrategy();
        }

        switch (discountType.toUpperCase()) {
            case "MEMBER":
                return new MemberDiscountStrategy();
            case "SEASONAL":
                return new SeasonalSaleStrategy();
            default:
                return new NoDiscountStrategy();
        }
    }
}