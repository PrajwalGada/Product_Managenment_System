package com.example.demo.Controller;

import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.sql.rowset.serial.SerialException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.example.demo.model.Product;
import com.example.demo.model.Users;
import com.example.demo.service.ProductService;
import com.example.demo.service.UsersService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.xml.ws.Response;

@Controller
public class ProductController {

	@Value("${google.client.clientId}")
	private String googleClientId;

	@Value("${google.client.clientSecret}")
	private String googleClientSecret;

	@Value("${google.client.redirectUri}")
	private String googleRedirectUri;

	@Autowired
	ProductService productService;

	@Autowired
	UsersService usersService;

	@GetMapping("/home")
	public ModelAndView home(HttpSession session, HttpServletRequest httpServletRequest) {
		return new ModelAndView("login");
	}

	@GetMapping("/registrationts")
	public ModelAndView registrationts(HttpSession session, HttpServletRequest httpServletRequest) {
		return new ModelAndView("register");
	}
	@GetMapping("/productRegistration")
	public ModelAndView getMethodName(HttpSession session, HttpServletRequest httpServletRequest) {
		return new ModelAndView("productRegister");
	}
	


	@PostMapping("/addUser")
	public String addEmployee(@RequestParam("firstName") String firstName,
							  @RequestParam("lastName") String lastName, @RequestParam("mobile") String mobile,
							  @RequestParam("address") String address,
							  @RequestParam("dob") @DateTimeFormat(pattern = "yyyy-MM-dd") Date dob, @RequestParam("email") String email,
							  @RequestParam("password") String password, @RequestParam("image") MultipartFile file,
							  @RequestParam(name = "adminid", required = false) Integer adminId, HttpSession session) {

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String formattedDate = dateFormat.format(new Date());

		Map<String, String> response = new HashMap<>();
		try {
			Users users = new Users();
			users.setFirstName(firstName);
			users.setLastName(lastName);
			users.setMobileNo(mobile);
			users.setCity(address);
			users.setRegistrationDate(formattedDate);
			users.setUpdatedDate(formattedDate);
			users.setDob(dob);
			users.setEmail(email);
			users.setPassword(password);
			if (file != null && !file.isEmpty()) {
				byte[] bytes = file.getBytes();
				users.setPhoto(bytes);
			}

			boolean savedUser = usersService.svaeUsers(users);
			if (savedUser) {

				return "redirect:/home";
			} else {
				response.put("message", "User already exists or could not be saved");
				session.setAttribute("response",ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response));
				return "redirect:/notFoundError";
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.put("message", "An error occurred while saving the user");
			session.setAttribute("response",ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response));
			return "redirect:/internalServerErr";
		}
	}

	@GetMapping("/dashboard")
	public ModelAndView dashboard(@RequestParam("userId") Long userId, Model model, HttpSession session, HttpServletRequest httpServletRequest) {

		Users user = (Users) session.getAttribute("loggedInUser");
		String profilePictureUrl = "/user/" + userId + "/profile-picture";
		model.addAttribute("profilePictureUrl", profilePictureUrl);
		model.addAttribute("userId", userId);
		model.addAttribute("userName", user.getFirstName()+" "+user.getLastName());




		return new ModelAndView("dashboard");
	}

	@GetMapping("/user/{id}/profile-picture")
	@ResponseBody
	public ResponseEntity<byte[]> getProfilePicture(@PathVariable Long id,HttpSession session) {
		if(session.getAttribute("loggedInUser")!=null) {
			Users user = (Users) session.getAttribute("loggedInUser");
			byte[] image = user.getPhoto();

			// Return the image with the correct content type
			return ResponseEntity.ok()
					.contentType(MediaType.IMAGE_JPEG)  // or MediaType.IMAGE_PNG if it's PNG
					.body(image);
		}
		return null;
	}

	@PostMapping("/registerProduct")
	public ResponseEntity<Map<String, String>> registerProduct(@RequestParam("productName")String productName,@RequestParam("product_qty")String product_qty,@RequestParam("Product_price")String product_price,
			@RequestParam("description") String desc,
			@RequestParam("image") MultipartFile file) {
		Map<String, String> response = new HashMap<>();
		Product product=new Product();
		product.setDescription(desc);
		product.setProductName(productName);
		product.setQty(Long.parseLong(product_qty));
		product.setPrice(product_price);
		try {
			if (file != null) {
				byte[] bytes = file.getBytes();
				Blob blob = new javax.sql.rowset.serial.SerialBlob(bytes);
				product.setPhoto(blob);
			}
			productService.saveProduct(product);
			response.put("message", "Product saved successfully");
			return ResponseEntity.status(HttpStatus.CREATED).body(response);
		} catch (Exception e) {
			e.printStackTrace();
			response.put("message", "Failed to save product");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@GetMapping("/login/google")
	public String googleLogin() {
		return "redirect:" + "https://accounts.google.com/o/oauth2/auth" + "?client_id=" + googleClientId
				+ "&redirect_uri=" + googleRedirectUri + "&response_type=code" + "&scope=openid%20profile%20email";
	}

	@GetMapping("/google/callback")
	public String googleCallback(@RequestParam(name = "code") String code, HttpSession session) {
		RestTemplate restTemplate = new RestTemplate();
		String accessTokenUrl = "https://www.googleapis.com/oauth2/v4/token";
		String userInfoUrl = "https://www.googleapis.com/oauth2/v3/userinfo";

		MultiValueMap<String, String> accessTokenParams = new LinkedMultiValueMap<>();
		accessTokenParams.add("code", code);
		accessTokenParams.add("client_id", googleClientId);
		accessTokenParams.add("client_secret", googleClientSecret);

		accessTokenParams.add("redirect_uri", googleRedirectUri);
		accessTokenParams.add("grant_type", "authorization_code");

		ResponseEntity<Map> accessTokenResponse = restTemplate.postForEntity(accessTokenUrl, accessTokenParams,
				Map.class);
		String accessToken = (String) accessTokenResponse.getBody().get("access_token");

		org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
		headers.setBearerAuth(accessToken);
		HttpEntity<String> entity = new HttpEntity<>(headers);
		ResponseEntity<Map> userInfoResponse = restTemplate.exchange(userInfoUrl, HttpMethod.GET, entity, Map.class);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String formattedDate = dateFormat.format(new Date());
		if (userInfoResponse.getStatusCode().is2xxSuccessful()) {
			@SuppressWarnings("unchecked")
			Map<String, Object> userInfo = userInfoResponse.getBody();

			String name = (String) userInfo.get("name");
			String email = (String) userInfo.get("email");
			System.out.println(name);
			System.out.println(email);
			Users existingEmployee = usersService.findByEmail(email);
			if (existingEmployee != null) {
				System.out.println("hi");
				return "redirect:/list";
			} else {
				Users user = new Users();
				user.setFirstName(name);
				user.setEmail(email);
				user.setRegistrationDate(formattedDate);
				user.setUpdatedDate(formattedDate);
				usersService.svaeUsers(user);
			}

		}
		return "redirect:/home";
	}

	@GetMapping("/list")
	public ModelAndView list(HttpSession session, HttpServletRequest httpServletRequest) {
		return new ModelAndView("list");
	}

	@GetMapping("/getList")
	public ResponseEntity<List<Users>> getAllUsers() {
		try {
			List<Users> users = usersService.getAll();
			return ResponseEntity.ok(users);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PostMapping("/login")
	public String login(@RequestParam("email") String email, @RequestParam("password") String password,
			HttpSession session) {

		Users user = usersService.findByEmail(email);
		if (user != null && usersService.verifyPassword(password, user.getPassword())) {
			session.setAttribute("loggedInUser", user);
			return "redirect:/dashboard?userId=" + user.getId();

		} else {

			return "redirect:/home";
		}
	}

	@GetMapping("/deleteUser/{id}")
	public ResponseEntity<Boolean> deleteUser(@PathVariable("id") long id) {
		boolean status = usersService.deleteById(id);

		if (status) {
			return new ResponseEntity<>(true, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(false, HttpStatus.NOT_FOUND);
		}
	}

	@GetMapping("/editUser/{id}")
	public ModelAndView editUser(HttpSession session, @PathVariable("id") long id,
			HttpServletRequest httpServletRequest) {
		ModelAndView modelAndView = new ModelAndView("editUser");
		Optional<Users> userOptional = usersService.findByid(id);
		if (userOptional.isPresent()) {
			Users user = userOptional.get();
			modelAndView.addObject("user", user);
		} else {

			modelAndView.addObject("errorMessage", "User not found.");
		}

		return modelAndView;
	}

	@PostMapping("/updateUser")
	public ResponseEntity<Map<String, String>> updateUser(@RequestParam("firstName") String firstName,
			@RequestParam("id") long id, @RequestParam("lastName") String lastName,
			@RequestParam("mobileNo") String mobile,
			@RequestParam("dob") @DateTimeFormat(pattern = "yyyy-MM-dd") Date dob, @RequestParam("email") String email,
			@RequestParam("password") String password, @RequestParam("image") MultipartFile file,
			@RequestParam(name = "adminid", required = false) Integer adminId, HttpSession session)
			throws SerialException, SQLException {

		Map<String, String> response = new HashMap<>();
		Optional<Users> userOptional = usersService.findByid(id);
		if (!userOptional.isPresent()) {
			response.put("status", "error");
			response.put("message", "User not found.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}
		Users user = userOptional.get();
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setEmail(email);
		user.setMobileNo(mobile);
		user.setPassword(password);
		user.setDob(dob);
		user.setUpdatedDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		if (file != null && !file.isEmpty()) {
			try {
				byte[] bytes = file.getBytes();
				user.setPhoto(bytes);
			} catch (IOException e) {
				response.put("status", "error");
				response.put("message", "Failed to upload image.");
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
			}
		}
		boolean status = usersService.svaeUsers(user);
		if (status ) {
			response.put("status", "success");
			response.put("message", "User updated successfully.");
			return ResponseEntity.ok(response);
		} else {
			response.put("status", "error");
			response.put("message", "Failed to update user.");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	} 

//	@GetMapping("/display")
//	public ResponseEntity<byte[]> displayImage(@RequestParam("id") long id) throws IOException, SQLException {
//
//		Optional<Users> image = usersService.findByid(id);
//		if (image != null && image.get().getPhoto() != null) {
//			byte[] imageBytes = image.get().getPhoto().getBytes(1, (int) image.get().getPhoto().length());
//			if (imageBytes != null) {
//				return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(imageBytes);
//			}
//		}
//
//		return ResponseEntity.notFound().build();
//	}

	@GetMapping("/logout")
	public String logout(HttpSession session) {

		session.removeAttribute("loggedInUser");
		return "redirect:/home";
	}
	@GetMapping("/internalServerError")
	public ModelAndView internalServerErr( Model model, HttpSession session) {
		ResponseEntity<String> responseEntity = (ResponseEntity<String>) session.getAttribute("response");
		String responseBody = responseEntity.getBody();
		model.addAttribute("response", responseBody);
		return new ModelAndView("error-500");
	}
	@GetMapping("/notFoundError")
	public ModelAndView notFoundError( Model model, HttpSession session) {
		ResponseEntity<String> responseEntity = (ResponseEntity<String>) session.getAttribute("response");
		String responseBody = responseEntity.getBody();
		model.addAttribute("response", responseBody);
		return new ModelAndView("error-404");
	}
}