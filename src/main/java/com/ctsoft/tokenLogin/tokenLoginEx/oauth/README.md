# Oauth2 login Without Spring Security

[oauth without spring security 출처](https://velog.io/@max9106/OAuth4)
### Google
application-oauth.yml 프로퍼티 파일에 정보들을 적어주고, 해당 정보를 바탕으로 객체로 바인딩한다.<br>
(spring security의 oauth 방식을 따와 비슷하게 만들어 간다.)

먼저 현재 구조를 바인딩 받을 수 있는 객체를 생성한다.(OauthProperties.java)<br>
설정 파일(application-oauth.yml)을 보면 oauth2 하위에 크게 user와 provider가 존재하고, 각각의 하위 값들이 존재한다.<br>
각각의 하위에 존재하는 값들을 static class의 필드로 두어 값을 바인딩 받을 수 있는 상태로 만든다.


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
