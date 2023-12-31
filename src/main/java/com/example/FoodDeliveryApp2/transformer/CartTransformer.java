package com.example.FoodDeliveryApp2.transformer;

import com.example.FoodDeliveryApp2.dto.response.CartResponse;
import com.example.FoodDeliveryApp2.model.Cart;

import java.util.ArrayList;

public class CartTransformer {

    public static CartResponse CartToCartResponse(Cart cart){
        return CartResponse.builder()
                .cartTotal(cart.getCartTotal())
                .foodItems(new ArrayList<>())
                .build();
    }
}
