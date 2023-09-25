package com.laioffer.onlineorder.service;

import com.laioffer.onlineorder.entity.CartEntity;
import com.laioffer.onlineorder.entity.CustomerEntity;
import com.laioffer.onlineorder.repository.CartRepository;
import com.laioffer.onlineorder.repository.CustomerRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

    private final CartRepository cartRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsManager userDetailsManager;

    public CustomerService(CartRepository cartRepository,
                           CustomerRepository customerRepository,
                           PasswordEncoder passwordEncoder,
                           UserDetailsManager userDetailsManager) {
        this.cartRepository = cartRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsManager = userDetailsManager;
    }

    @Transactional
    public void signUp(String email, String password, String firstName, String lastName) {
        email = email.toLowerCase();//email不区分大小写 统一小写后存到db
        UserDetails user = User.builder()
                .username(email)
                .password(passwordEncoder.encode(password))
                .roles("USER")
                .build();
        userDetailsManager.createUser(user);//创建user并存到了db里
        customerRepository.updateNameByEmail(email, firstName, lastName);

        CustomerEntity savedCustomer = customerRepository.findByEmail(email);
        CartEntity cart = new CartEntity(null, savedCustomer.id(), 0.0);
        cartRepository.save(cart);
    }


    public CustomerEntity getCustomerByEmail(String email) {
        return customerRepository.findByEmail(email);
    }
}
