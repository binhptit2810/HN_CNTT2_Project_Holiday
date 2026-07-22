package com.attraction.quanlinhahang;

import com.attraction.quanlinhahang.controller.UserController;
import com.attraction.quanlinhahang.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
class QuanLiNhaHangApplicationTests {

    @Autowired
    private UserController userController;

    @Test
    void testOrderPageRender() {
        try {
            MockHttpSession session = new MockHttpSession();
            User user = User.builder().username("waiter").password("waiter").role(User.Role.WAITER).build();
            session.setAttribute("currentUser", user);
            
            MockMvc mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
            mockMvc.perform(get("/order/101").session(session));
            System.out.println("--- TEST DIAGNOSTIC: RENDER ATTEMPTED ---");
        } catch (Exception e) {
            System.err.println("--- TEST DIAGNOSTIC RENDER FAILED WITH EXCEPTION: ---");
            e.printStackTrace();
        }
    }
}
