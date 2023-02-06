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
// application-oauth.yml 파일의 정보들을 객체로 바인딩한다.
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

### +) 실제 로그인 구현해보기.

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

* notification

```
re-run spring boot configuration annotation processor to update generated metadata
```
해당 문구는 Intellij 상단에 노출되는데, error나 warning이 아닌 단순 notification 이다.<br>
안보이게 할 수는 있으나, 해결방법은 정확히 모르곘음..
