package com.example.demo.Controller;

import java.net.http.HttpHeaders;
import java.sql.Blob;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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

	@PostMapping("/add")
	public ResponseEntity<Map<String, String>> addEmployee(@RequestParam("firstName") String firstName,
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
				Blob blob = new javax.sql.rowset.serial.SerialBlob(bytes);
				users.setPhoto(blob);
			}

			boolean message = usersService.svaeUsers(users);
			if (message) {
				response.put("message", "User saved successfully");
				return ResponseEntity.status(HttpStatus.CREATED).body(response);
			} else {
				response.put("message", "User already exists or could not be saved");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.put("message", "An error occurred while saving the user");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@PostMapping("/registerProduct")
	public ResponseEntity<Map<String, String>> registerProduct(@RequestBody Product product,
			@RequestParam("image") MultipartFile file) {
		Map<String, String> response = new HashMap<>();
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

}