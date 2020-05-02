package com.lf.springboot.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;


import springboot_webdemo.controller.UserController;

@RunWith(SpringRunner.class)
@SpringBootTest
public class HelloWorldControllerTest {

	private MockMvc mockMvc;

	@Before
	public void setUp() {
		this.mockMvc = MockMvcBuilders.standaloneSetup(UserController.class).build();

	}

	@Test
	public void getHello() throws Exception {
		
		try {
			ResultActions resultActions = mockMvc
					.perform(MockMvcRequestBuilders.get("/hello").accept(MediaType.APPLICATION_JSON));
			MvcResult mvcResult = resultActions.andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().string("hello !")).andDo(MockMvcResultHandlers.print()).andReturn();
			System.out.println(mvcResult.getResponse().getContentAsString());
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
	 }
}
