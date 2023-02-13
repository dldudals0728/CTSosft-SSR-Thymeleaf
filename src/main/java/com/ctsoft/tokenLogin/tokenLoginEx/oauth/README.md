# Oauth2 login Without Spring Security

[oauth without spring security 출처](https://velog.io/@max9106/OAuth4 "Oauth2 login without Spring Security 공부 출처입니다.")
### Google
`application-oauth.yml` 프로퍼티 파일에 정보들을 적어주고, 해당 정보를 바탕으로 객체로 바인딩한다.<br>
(spring security의 oauth 방식을 따와 비슷하게 만들어 간다.)

```yaml
oauth2:
  user:
    google:
      client-id: [클라이언트 아이디]
      client-secret: [클라이언트 비밀번호]
      redirect-uri: [설정한 redirect uri]
  provider:
    google:
      token-uri: https://www.googleapis.com/oauth2/v4/token
      user-info-uri: https://www.googleapis.com/oauth2/v3/userinfo

```

먼저 현재 구조를 바인딩 받을 수 있는 객체 파일`OauthProperties.java`를 생성한다.
> spring security 에서는 **@ConfigurationProperties**를 사용하여 프로퍼티 값을 객체로 바인딩 해주었다.

설정 파일`application-oauth.yml`을 보면 oauth2 하위에 크게 user와 provider가 존재하고, 각각의 하위 값들이 존재한다.<br>
각각의 하위에 존재하는 값들을 static class의 필드로 두어 값을 바인딩 받을 수 있는 상태로 만든다.
* `OauthProperties.java`
```java
// 값을 바인딩 할 수 있는 상탱로 만들어준다.
// application.yml 파일의 정보들을 객체로 바인딩한다.
// oauth2 하위에 크게 user 와 provider 가 존재하기 때문에 하위에 존재하는 값들을 static class의 필드로 두어 값을 바인딩 받을 수 있는 상태로 만든다.
// OauthProperties(this)를 이용하여 값을 바인딩 할 수 있는 상태로 만들었으면, 실제로 사용하기 위해 설정 파일(OauthConfig)을 만들어 주고,
// @EnableConfigurationProperties annotation을 붙여 사용해준다.
@Getter
@ConfigurationProperties(prefix = "oauth2")
public class OauthProperties {
    
    private final Map<String, User> user = new HashMap<>();
    
    private final Map<String, Provider> provider = new HashMap<>();
    
    @Getter
    @Setter
    public static class User {
        private String clientId;
        private String clientSecret;
        private String redirectUri;
    }
    
    @Getter
    @Setter
    public static class Provider {
        private String tokenUri;
        private String userInfoUri;
        private String userNameAttribute;
    }
}
```

`OauthProperties.java` 파일을 이용하여 <b>값을 바인딩 할 수 있는 상태</b>로 만들었으면
실제로 사용하기 위해 설정 파일 `OauthConfig.java` 를 만들어준다.<br>
설정 파일에는 **@EnableConfigurationProperties** annotation을 붙여준다.

* `OauthConfig.java`
```java
@Getter
@Configuration
@EnableConfigurationProperties(OauthProperties.class)
public class OauthConfig {
    
    private final OauthProperties properties;
    
    public OauthConfig(OauthProperties properties) {
        this.properties = properties;
    }
}
```

여기까지 하면 프로퍼티 파일에 적어준 정보가 하나의 **OauthProperties** 객체로 만들어진다!!<br>
이를 각가의 OAuth 서버 정보로 나눠서 **InMemory** 저장소에 저장해 사용해야 한다.

저장소에 저장하기에 앞서 **OauthProperties**를 분해해야 한다.
> spring security 에서는 **ClientRegistration** 객체를 만들어 준다.
> 
> spring security의 **ClientRegistration** => 우리의 **OauthProvider**

* `OauthProvider.java`
```java
// spring security의 ClientRegistration 역할
@Getter
public class OauthProvider {
    private final String clientId;
    private final String clientSecret;
    private final String redirectUrl;
    private final String tokenUrl;
    private final String userInfoUrl;

    public OauthProvider(OauthProperties.User user, OauthProperties.Provider provider) {
        this(user.getClientId(), user.getClientSecret(), user.getRedirectUri(), provider.getTokenUri(), provider.getUserInfoUri());
    }

    @Builder
    public OauthProvider(String clientId, String clientSecret, String redirectUrl, String tokenUrl, String userInfoUrl) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUrl = redirectUrl;
        this.tokenUrl = tokenUrl;
        this.userInfoUrl = userInfoUrl;
    }
}
```
spring security는 **OAuth2ClientPropertiesRegistrationAdapter**를 통해
**OAuth2ClientProperties**를 **ClientRegistration**으로 바꿔준다.

우리는 **OauthAdapter**를 통해 **OauthProperties**를 **ClientRegistration**으로 바꿔준다.

`OauthAdapter.java`를 만들어준다.
* `OauthAdapter.java`
```java
public class OauthAdapter {
    private OauthAdapter() {}

    // OauthProperties를 OauthProvider로 변환한다.
    public static Map<String, OauthProvider> getOauthProviders(OauthProperties properties) {
        
        Map<String, OauthProvider> oauthProvider = new HashMap<>();

        properties.getUser().forEach(
                (key, value) -> oauthProvider.put(
                        key,
                        new OauthProvider(value, properties.getProvider().get(key))
                )
        );

        return oauthProvider;
    }
}
```
> `spring security`<br>
> **OAuth2ClientPropertiesRegistrationAdapter**가<br>
> **OAuth2ClientProperties** &rarr; **ClientRegistration**으로 변경
> 
> `Without Spring Security`<br>
> **OauthAdapter**가<br>
> **OauthProperties** &rarr; **OauthProvider**로 변경

이제 **OauthProvider**를 저장해 줄 **InMemory** 저장소를 만든다.
* `InMemoryProviderRepository.java`
```java
public class InMemoryProviderRepository {
    
    private final Map<String, OauthProvider> providers;
    
    public InMemoryProviderRepository(Map<String, OauthProvider> providers) {
        this.providers = new HashMap<>(providers);
    }
    
    public OauthProvider findByProviderName(String name) {
        return providers.get(name);
    }
}
```
마지막으로 **OauthConfig**에서 빈으로 등록된 **OauthProperties**를 주입받아
**OauthAdapter를** 사용해 각 OAuth 서버 정보를 가진 **OauthProvider**로 분해하여
**InMemoryProviderRepository**에 저장해 주면 된다.

* `OauthConfig.java`
```java
@Getter
@Configuration
@EnableConfigurationProperties(OauthProperties.class)
public class OauthConfig {
    private final OauthProperties properties;
    public OauthConfig(OauthProperties properties) {
        this.properties = properties;
    }
    
    // OauthConfig.java 추가 코드
    @Bean
    public InMemoryProviderRepository inMemoryProviderRepository() {
        Map<String, OauthProvider> providers = OauthAdapter.getOauthProviders(this.properties);
        return new InMemoryProviderRepository(providers);
    }
}
```
여기까지 진행하면 애플리케이션이 실행될 때, OAuth 서버 정보들을 객체로 만들어 메모리에 저장한다!!

#### controller / service / view
이제 OAuth 토큰을 받도록 하는 controller / service / view를 만든다.

* `oauth-index.html`

주의사항
1. login api 이동 주소 작성 시, 자신의 client-id 와 redirect-uri 를 정확하게 입력할 것.
2. redirect-url 을 값을 받을 수 있는 주소로 controller 상에 정확히 작성할 것(url)

```html
<!-- 예시 -->
<a href="https://accounts.google.com/o/oauth2/v2/auth?scope=https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email&client_id=88477443435-i5aok4n08mthen5cn2rl2mgrsh91s3ik.apps.googleusercontent.com&response_type=code&redirect_uri=http://localhost:8080/login/oauth2/code/google&access_type=offline">Google
    Login</a><br>
<!-- 포맷 -->
<a href="https://accounts.google.com/o/oauth2/v2/auth?scope=https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email&client_id=[사용자 client-id]&response_type=code&redirect_uri=[사용자 redirect-uri]&access_type=offline">Google
    Login</a><br>
```
view는 위에 보이는 것 처럼 요청 주소를 적어야 한다. (google의 경우 client-id / redirect-uri 를 사용자에 맞게 작성한다.)<br>
그리고 redirect 될 수 있도록 컨트롤러를 작성한다.

* `OauthController.java`

```java
@Controller
public class OauthRestController {

    private final OauthService oauthService;

    public OauthRestController(OauthService oauthService) {
        this.oauthService = oauthService;
    }

    @GetMapping("/login/oauth2/code/{provider}")
    @ResponseBody
    public ResponseEntity<LoginResponse> login(@PathVariable String provider, @RequestParam String code) {
        LoginResponse loginResponse = oauthService.login(provider, code);
        return ResponseEntity.ok().body(loginResponse);
    }

    // view 에 접속할 수 있게 하기 위한 mapping
    @GetMapping("/oauth/login")
    public String oauthLoginPage() {
        return "oauth/oauth-index";
    }
}
```
다음은 controller 부분이다. controller 에서 OAuth 토큰을 받아오는 부분이 가장 위에 있는 /login/oauth2/code/{provider} mapping 이다.<br>
여기서도 매핑할 때 redirect-uri에 맞춰서 매핑 주소를 입력해 주어야 한다.<br>
나같은 경우는 "http://localhost:8080/login/oauth2/code/google"로 redirect-uri를 작성했다. 여기서 google 이 provider가 되는 것이다.

* `OauthService.java`

```java
@Service
public class OauthService {

    private final InMemoryProviderRepository inMemoryProviderRepository;

    public OauthService(InMemoryProviderRepository inMemoryProviderRepository) {
        this.inMemoryProviderRepository = inMemoryProviderRepository;
    }

    public LoginResponse login(String providerName, String code) {
        // 프론트에서 넘어온 provider 이름을 통해 InMemoryProviderRepository에서 OauthProvider 가져오기
        OauthProvider provider = inMemoryProviderRepository.findByProviderName(providerName);

        // TODO access token 가져오기
        // TODO 유저 정보 가져오기
        // TODO 유저 DB에 저장
        return null;
    }
}
```

이게 우리가 사용항 service인데, 여기서 보이는 **LoginResponse** 순서에 맞게 나중에 뒤에서 코드를 보여준다.<br>
이제 accessToken 이나 유저 정보를 가져오기 위해 실제 OAuth 서버와 통신을 해야 한다. 그러기 위해 WebClient 의존성을 추가해준다.

* `pom.xml`

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

* `build.gradle`

```gradle
implementation 'org.springframework.boot:spring-boot-starter-webflux'
```

이제 OAuth 서버와 통신해 access token을 받아올 DTO **OauthTokenResponse** 를 작성한다.

* `OauthTokenResponse.java`

```java
@Getter
@NoArgsConstructor
public class OauthTokenResponse {
    @JsonProperty("access_token")
    private  String accessToken;
    private  String scope;
    @JsonProperty("token_type")
    private String tokenType;

    @Builder
    public OauthTokenResponse(String accessToken, String scope, String tokenType) {
        this.accessToken = accessToken;
        this.scope = scope;
        this.tokenType = tokenType;
    }
}
```

이제 service 에서 WebClient 를 이용하여 OAuth 서버와 통신하면 **OauthTokenResponse** 로 값을 받아올 수 있다.

* `OauthService.java`<br>
`mod: login function / add: tokenRequest, getToken function`

```java
@Service
public class OauthService {
    public LoginResponse login(String providerName, String code) {
        // 프론트에서 넘어온 provider 이름을 통해 InMemoryProviderRepository에서 OauthProvider 가져오기
        OauthProvider provider = inMemoryProviderRepository.findByProviderName(providerName);
        
        /////// 추가 부분 ///////
        // access token 가져오기
        OauthTokenResponse tokenResponse = getToken(code, provider);
        //////////////////////

        // TODO access token 가져오기
        // TODO 유저 정보 가져오기
        // TODO 유저 DB에 저장
        return null;
    }

    private OauthTokenResponse getToken(String code, OauthProvider provider) {
        return WebClient.create()
                .post()
                .uri(provider.getTokenUrl())
                .headers(header -> {
                    header.setBasicAuth(provider.getClientId(), provider.getClientSecret());
                    header.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                    header.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
                    header.setAcceptCharset(Collections.singletonList(StandardCharsets.UTF_8));
                })
                .bodyValue(this.tokenRequest(code, provider))
                .retrieve()
                .bodyToMono(OauthTokenResponse.class)
                .block();
    }

    private MultiValueMap<String, String> tokenRequest(String code, OauthProvider provider) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("code", code);
        formData.add("grant_type", "authorization_code");
        formData.add("redirect_uri", provider.getRedirectUrl());

        return formData;
    }
}
```
OAuth 서버와 통신할 때 WebClient를 사용해 OAuth 서버에 access token 요청을 하면 된다. 프로퍼티 파일에 적어줬던 access token을 요청할 수 있는 uri에 요청을 보내면 된다.<br>
이 때 헤더에 client-id와 client-secret값으로 Basic Auth를 추가해주고, 컨텐츠 타입을 APPLICATION_FORM_URLENCODED로 설정해준다.<br>
요청 바디에는 authorization code, redirect_uri 등을 넘겨주면 된다.

이제 유저 정보를 가져온다. OAuth 서버 별로 가져올 수 있는 유저 정보가 다르다.<br>
토큰과 마찬가지로 이를 받을 수 있는 **UserProfile** DTO를 작성해 준다.<br>
<i>이번에는 oauthId, email, name, imageUrl 정도만 가져온다. 추가로 더 가져올 수 있는 항목들이 있는지 알아보자!!</i>

* `UserProfile.java`

```java
@Getter
public class UserProfile {
    private final String oauthId;
    private final String email;
    private final String name;
    private final String imageUrl;

    @Builder
    public UserProfile(String oauthId, String email, String name, String imageUrl) {
        this.oauthId = oauthId;
        this.email = email;
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public Member toMember() {
        return Member.builder()
                .oauthId(this.oauthId)
                .email(this.email)
                .name(this.name)
                .imageUrl(this.imageUrl)
                .role(OauthRole.GUEST)
                .build();
    }
}
```
마찬가지로 OAuth 서버와 WebClient 를 통해 통신하고, map으로 받아온다. Bearer 타입으로 Auth 헤더에 access token 값을 담아준다.

* `OauthService.java`<br>
  `mod: login function / add: getUserAttributes, getUserProfile function`

```java
@Service
public class OauthService {
    public LoginResponse login(String providerName, String code) {
        // 프론트에서 넘어온 provider 이름을 통해 InMemoryProviderRepository에서 OauthProvider 가져오기
        OauthProvider provider = inMemoryProviderRepository.findByProviderName(providerName);
        
        // access token 가져오기
        OauthTokenResponse tokenResponse = getToken(code, provider);

        /////// 추가 부분 ///////
        // 유저 정보 가져오기
        UserProfile userProfile = this.getUserProfile(providerName, tokenResponse, provider);
        //////////////////////
        
        // TODO 유저 DB에 저장
        return null;
    }

    private UserProfile getUserProfile(String providerName, OauthTokenResponse tokenResponse, OauthProvider provider) {
        Map<String, Object> userAttributes = this.getUserAttributes(provider, tokenResponse);
        // 유저 정보(map)를 통해 UserProfile 만들기
        return OauthAttributes.extract(providerName, userAttributes);
    }

    // OAuth 서버에서 유저 정보 map으로 가져오기
    private Map<String, Object> getUserAttributes(OauthProvider provider, OauthTokenResponse tokenResponse) {
        return WebClient.create()
                .get()
                .uri(provider.getUserInfoUrl())
                .headers(header -> header.setBearerAuth(tokenResponse.getAccessToken()))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();
    }
}
```
얻어온 유저 정보를 UserProfile로 만들어 줘야 하는데, OAuth 서버 별로 데이터의 key값이 다르다. 따라서 enum class 를 이용하여 서버별로 다른 key값들을 작성해준다.

* `OauthAttributes.java`

```java
public enum OauthAttributes {
    GITHUB("github") {
        @Override
        public UserProfile of(Map<String, Object> attributes) {
            return UserProfile.builder()
                    .oauthId(String.valueOf(attributes.get("id")))
                    .email((String) attributes.get("email"))
                    .name((String) attributes.get("name"))
                    .imageUrl((String) attributes.get("avatar_url"))
                    .build();
        }
    },
    NAVER("naver") {
        @Override
        public UserProfile of(Map<String, Object> attributes) {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = (Map<String, Object>) attributes.get("response");
            return UserProfile.builder()
                    .oauthId((String)response.get("id"))
                    .email((String) response.get("email"))
                    .name((String) response.get("name"))
                    .imageUrl((String) response.get("profile_image"))
                    .build();
        }
    },
    GOOGLE("google") {
        @Override
        public UserProfile of(Map<String, Object> attributes) {
            return UserProfile.builder()
                    .oauthId(String.valueOf(attributes.get("id")))
                    .email((String) attributes.get("email"))
                    .name((String) attributes.get("name"))
                    .imageUrl((String) attributes.get("picture"))
                    .build();
        }
    };

    private final String providerName;

    OauthAttributes(String name) {
        this.providerName = name;
    }

    public static UserProfile extract(String providerName, Map<String, Object> attributes) {
        return Arrays.stream(values())
                .filter(provider -> providerName.equals(provider.providerName))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new)
                .of(attributes);
    }

    public abstract UserProfile of(Map<String, Object> attributes);
}
```
이렇게 만들어진 UserProfile을 DB에 저장하기 위해 **Member** class를 작성한다.

* `Member.java`

```java
@Entity
@Table
@Getter
@ToString
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String oauthId;
    private String name;
    private String email;
    private String imageUrl;
    @Enumerated(EnumType.STRING)
    private OauthRole role;

    protected Member() {}

    @Builder
    public Member(String oauthId, String name, String email, String imageUrl, OauthRole role) {
        this(null, oauthId, name, email, imageUrl, role);
    }

    public Member(Long id, String oauthId, String name, String email, String imageUrl, OauthRole role) {
        this.id = id;
        this.oauthId = oauthId;
        this.name = name;
        this.email = email;
        this.imageUrl = imageUrl;
        this.role = role;
    }

    public Member update(String name, String email, String imageUrl) {
        this.name = name;
        this.email = email;
        this.imageUrl = imageUrl;
        return this;
    }

    public String getRoleKey() {
        return this.role.getKey();
    }
}
```

그리고 front로 보낼 LoginResposne Dto를 작성한다.

* `LoginResponse.java`

```java
@Getter
@NoArgsConstructor
public class LoginResponse {
    private Long id;
    private String name;
    private String email;
    private String imageUrl;
    private OauthRole role;
    private String tokenType;
    private String accessToken;
//    private String refreshToken;

    @Builder
    public LoginResponse(Long id, String name, String email, String imageUrl, OauthRole role, String tokenType, String accessToken) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.imageUrl = imageUrl;
        this.role = role;
        this.tokenType = tokenType;
        this.accessToken = accessToken;
//        this.refreshToken = refreshToken;
    }
}
```

마지막으로 member를 저장하고, 자체 애플리케이션에서 사용할 access token (refresh token도 있으면 추가한다.)을 생성해서 LoginResponse 에 담아서 전달한다.

* `OauthService.java`<br>
  `mod: login function / add: saveOrUpdate function`

```java
@Service
public class OauthService {
    public LoginResponse login(String providerName, String code) {
        // 프론트에서 넘어온 provider 이름을 통해 InMemoryProviderRepository에서 OauthProvider 가져오기
        OauthProvider provider = inMemoryProviderRepository.findByProviderName(providerName);
        
        // access token 가져오기
        OauthTokenResponse tokenResponse = getToken(code, provider);
        
        // 유저 정보 가져오기
        UserProfile userProfile = this.getUserProfile(providerName, tokenResponse, provider);

        /////// 추가 부분 ///////
        // 유저 DB에 저장
        Member member = this.saveOrUpdate(userProfile);

        // create JWT
        String accessToken = jwtTokenProvider.generateAccessToken(String.valueOf(member.getId()));

        return LoginResponse.builder()
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .imageUrl(member.getImageUrl())
                .role(member.getRole())
                .tokenType("Bearer")
                .accessToken(accessToken)
                .build();
        //////////////////////
    }

    private Member saveOrUpdate(UserProfile userProfile) {
        Member member = memberRepository.findByOauthId(userProfile.getOauthId())
                .map(entity -> entity.update(
                        userProfile.getEmail(), userProfile.getName(), userProfile.getImageUrl()
                ))
                .orElseGet(userProfile::toMember);

        return member;
    }
}
```

이제 테스트 해봐라!! 아주 굿이에요 굿 ~~

### Report
* error
```
Not registered via @EnableConfigurationProperties, marked as Spring component, or scanned via @ConfigurationPropertiesScan
```
OauthProperties 파일을 생성하면서 @ConfigurationProperties annotation을 붙였을 때 나오는 에러이다.<br>
@Component 또는 @Configuration annotation을 사용하여 spring bean 으로 등록하면 해당 에러가 사라진다. 하지만 우리는 해당 클래스를 OauthConfig 파일에서 사용할 때 bean으로 함께 등록할 것이기 때문에

```java
@Getter
@EnableConfigurationProperties(OauthProperties.class)
public class OauthConfig {
}
```
위처럼 @EnableConfigurationProperties annotation 을 이용하여 bean으로 등록하면서 함께 사용해 주자.<br>
이렇게 해도 상단의 에러가 사라지게 된다!!
<hr />

* error
```
No suitable default ClientHttpConnector found
```
WebClient 사용 시 발생하는 오류이다. 해결 방법은 webflux dependency 를 수정해 주면 된다.
```xml
<!-- before -->
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-webflux</artifactId>
</dependency>
```
```xml
<!-- after -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```
<hr />

* error

OAuth2 코드 작성을 마치고 테스트를 진행해보니 NullPointException이 발생했다. 
코드를 하나하나 확인해보니, application-oauth.yml 파일을 객체화 하는 과정에서 오류가 났다.

application-oauth.yml 파일을 객체화 하지 못해서 OauthProperties 객체가 null로 반환되는 것이었다.

진짜 별걸 다 해보다가, application-oauth.yml -> application.yml 로 파일 이름을 변경하니 정상적으로 OAuth2 인증이 되었다...!!
application-oauth.yml 파일을 읽지 못하고, application.yml 파일만 읽을 수 있었던 것이다.
<hr />

* notification

```
re-run spring boot configuration annotation processor to update generated metadata
```
해당 문구는 Intellij 상단에 노출되는데, error나 warning이 아닌 단순 notification 이다.<br>
안보이게 할 수는 있으나, 해결방법은 정확히 모르곘음..
