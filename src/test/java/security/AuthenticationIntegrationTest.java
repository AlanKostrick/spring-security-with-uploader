package security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.logout;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import javax.servlet.ServletContext;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;


@SpringBootTest(classes = SpringSecurityDemoApplication.class)
@RunWith(SpringRunner.class)
@WebAppConfiguration
public class AuthenticationIntegrationTest {

	@Autowired
	private PostRepository postRepo;

	@Autowired
	private WebApplicationContext webApplicationContext;
	
	private MockMvc mvc;
	
	@Before
	public void setup() throws Exception {
	    mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
	    		// Apply security, just like the app will eventually be run
	    		// Enables /login POST form and /logout action
				.apply(springSecurity())
				.build();
	}
	
	// Ensure the SiteController is available, indicating the context is set up correctly
	@Test
	public void givenContext_whenServletContext_thenItProvidesUserController() {
		ServletContext servletContext = webApplicationContext.getServletContext();
		
		Assert.assertNotNull(servletContext);
	    Assert.assertTrue(servletContext instanceof MockServletContext);
	    Assert.assertNotNull(webApplicationContext.getBean("siteController"));
	}
	
	// Ensure the /login page configured by WebConfig.java is set up correctly
	@Test
	  public void loginAvailableForAll() throws Exception {
		mvc
            .perform(get("/login"))
            .andExpect(status().isOk());
	  }
	
	@Test
	public void mvcConfigAndSpringSecurityWiredCorrectly() throws Exception {

		String adminUsername = "admin";
		String adminPassword = "admin";

		String badUsername = "bad";
		String badPassword = "bad";

		// Ensure bad credentials can NOT log in
		mvc
            .perform(formLogin("/login").user(badUsername).password(badPassword))
            .andDo(print())
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/login?error"));
		
		// Ensure the admin's credentials CAN log in
		MvcResult mvcResult = mvc
            .perform(formLogin("/login").user(adminUsername).password(adminPassword))
            .andDo(print())
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/"))
            .andExpect(authenticated().withUsername(adminUsername))
            .andReturn();
		
		// Store the authenticated session for ongoing use
		MockHttpSession session = (MockHttpSession) mvcResult.getRequest().getSession(false);
		
		// Ensure all routes defined in MvcConfig.java are working correctly
		mvc.perform(get("/").session(session)).andDo(print()).andExpect(view().name("index"));
		mvc.perform(get("/posts").session(session)).andDo(print()).andExpect(view().name("layouts/posts"));
		mvc.perform(get("/admin/posts").session(session)).andDo(print()).andExpect(view().name("layouts/post"));
		mvc.perform(get("/login").session(session)).andDo(print()).andExpect(view().name("auth/login"));
		
		// Ensure the authenticated admin can now log out
		mvc
            .perform(logout())
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/login?logout"));
	  }
}
