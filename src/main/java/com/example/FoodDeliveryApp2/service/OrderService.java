package com.example.FoodDeliveryApp2.service;

import com.example.FoodDeliveryApp2.dto.response.OrderResponse;
import com.example.FoodDeliveryApp2.exception.CustomerNotFoundException;
import com.example.FoodDeliveryApp2.exception.EmptyCartException;
import com.example.FoodDeliveryApp2.model.*;
import com.example.FoodDeliveryApp2.repository.CustomerRepository;
import com.example.FoodDeliveryApp2.repository.DeliveryPartnerRepo;
import com.example.FoodDeliveryApp2.repository.OrderEntityRepo;
import com.example.FoodDeliveryApp2.repository.RestaurantRespository;
import com.example.FoodDeliveryApp2.transformer.OrderTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class OrderService {

    final CustomerRepository customerRepository;
    final OrderEntityRepo orderEntityRepo;

    final DeliveryPartnerRepo deliveryPartnerRepo;
    private final RestaurantRespository restaurantRespository;

    @Autowired
    public OrderService(CustomerRepository customerRepository,
                        OrderEntityRepo orderEntityRepo,
                        DeliveryPartnerRepo deliveryPartnerRepo,
                        RestaurantRespository restaurantRespository) {
        this.customerRepository = customerRepository;
        this.orderEntityRepo = orderEntityRepo;
        this.deliveryPartnerRepo = deliveryPartnerRepo;
        this.restaurantRespository = restaurantRespository;
    }

    public OrderResponse placeOrder(String customerMobile) {

        // verify the customer
        Customer customer = customerRepository.findByMobileNo(customerMobile);
        if(customer == null){
            throw new CustomerNotFoundException("Invalid mobile number!!!");
        }

        // verify if cart is empty or not
        Cart cart = customer.getCart();
        if(cart.getFoodItems().size()==0){
            throw new EmptyCartException("Sorry! Your cart is empty!!!");
        }

        // find a delivery partner to deliver. Randomly
        DeliveryPartner partner = deliveryPartnerRepo.findRandomDeliveryPartner();
        Restaurant restaurant = cart.getFoodItems().get(0).getMenuItem().getRestaurant();

        // prepare the order entity
        OrderEntity order = OrderTransformer.prepareOrderEntity(cart);

        OrderEntity savedOrder = orderEntityRepo.save(order);

        order.setCustomer(customer);
        order.setDeliveryPartner(partner);
        order.setRestaurant(restaurant);
        order.setFoodItems(cart.getFoodItems());

        customer.getOrders().add(savedOrder);
        partner.getOrders().add(savedOrder);
        restaurant.getOrders().add(savedOrder);

        for(FoodItem foodItem: cart.getFoodItems()){
            foodItem.setCart(null);
            foodItem.setOrder(savedOrder);
        }
        clearCart(cart);

        customerRepository.save(customer);
        deliveryPartnerRepo.save(partner);
        restaurantRespository.save(restaurant);

        // prepare orderresponse
        return OrderTransformer.OrderToOrderResponse(savedOrder);
    }

    private void clearCart(Cart cart) {
        cart.setCartTotal(0);
        cart.setFoodItems(new ArrayList<>());
    }
}
