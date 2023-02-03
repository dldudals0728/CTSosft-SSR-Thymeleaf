## JWT Claim

JWT에서 claim이란 사용자에 대한 프로퍼티나 속성을 가리키는 것이다.<br>
쉽게 말해서, JWT token을 이용하여 로그인을 구현할 때 로그인된 사용자의 권한에 대해 담고싶으면 Claim을 이용하여
사용자 권한에 대해 명시할 수 있게 된다.

claim에 정보를 담게 되면 payload 부분에 내가 작성한 정보가 들어가게 된다.

## thymeleaf: form - Dto mapping
controller에서 빈 Dto를 넘겨주어 thymeleaf에서 사용할 수 있다.<br>
컨트롤러에서 다음과 같이 넘겨주었다고 가정해보자

```java
public class SearchDto() {
    private String category;
    private String keyword;
}
```

```java
public class BoardController {
    @GetMapping("/board")
    public String search(Model model) {
        model.addAttribute("searchDto", new SearchDto());
        return "/board/index";
    }
}
```
이렇게 빈 Dto를 넘겨주면 view 단에서 해당 Dto의 값들을 매핑 시켜줄 수 있다.
```thymeleaftemplatesexpressions
<form th:object="${searchDto}" action="/board/search" method="get">
    <select th:field="*{category}">
        <option th:value="title">제목</option>
        <option th:value="content">본문</option>
        <option th:value="both">제목 + 본문</option>
    </select>
    <input placeholder="검색어 입력" th:field="*{keyword}" />
    <button>검색</button>
</form>
```
th:object를 이용하여 form 에서 적용시킬 Dto를 선택(${dto})해주고, 각각의 값들이 매핑되는 Dto의 속성들은 th:field(*{})를 이용하여 매핑한다.

그리고 controller 단에서 받은 Dto를 사용하려면 기존 방식과 같이 매개변수로 받아서 사용할 수 있다!!

## thymeleaf Math
thymeleaf에서 java.lang.Math를 사용할 수 있다.(T(Math))<br>
단, 주의해야 할 사항이 있는데
> exception evaluating springel expression T(math)

위와 같은 에러가 날 수 있다. 그러면

```html
<nav th:with="startPage = ${T(java.lang.Math).floor(pageNumber / pageSize) * pageSize + 1}">
    ...
</nav>
```
T(Math)로 사용하는 것이 아닌, 전체 경로로 표기하여 T(java.lang.Math)로 사용해야 에러가 나지 않는다!

## thymeleaf error: Could not parse as expression:
타임리프에서 다음과 같은 에러가 출력되었으면, th:text=""(예시)에서 "" 안에 ${} 문법이 들어갔는지 확인해보자.<br>
${}을 사용하지 않으면 에러가 난다.


> 잘못된 예
> ```html
> <span th:text="이미지가 존재하지 않습니다."></span>
> ```


> 옳바른 예
> ```html
> <span th:text="${'이미지가 존재하지 않습니다.'}"></span>
> ```

## form tag: file 전송
spring boot 에서 file 을 전달받기 위한 매개변수로 MultipartFile을 사용한다.<br>
해당 데이터를 넘겨주려면 form 태그의 enctype 속성으로 multipart/form-data 값을 넘겨준다.
```html
<form action="" method="post" enctype="multipart/form-data"></form>
```

## thymeleaf: enum data 사용
thymeleaf에서 enum data 사용 및 비교가 가능하다.
```java
public enum Role {
    ADMIN, USER
}
```
위와 같은 enum data가 존재할 때, Model을 통해 Role.ADMIN을 넘겨주었다면, thymeleaf에서 이렇게 사용이 가능하다.
```html
<th:block th:if="${role == T(com.ctsoft.tokenLogin.tokenLoginEx.constant.Role).ADMIN}">
</th:block>
```

T() 문법으로 Role을 감싸주는데, enum data의 <i><b>전체 패키기 경로</b></i>를 이용하여 사용해야 한다!!

## form tag: get method
form tag 에서 get method 는, <i><b>기존의 쿼리를 모두 날리고, form tag 내부에 있는 값들을 전송</b></i>한다!!<br>
따라서, form tag의 get method를 이용하려면, form 태그 내부에 input을 넣고, type="hidden"을 이용하여 값을 전달하면 된다.

> 틀린 예
> ```html
> <form th:action="@{/board/update(id=${board.id})}" method="get">
>     <button>수정</button>
> </form>
> ```

> 맞는 예
> ```html
> <form th:action="@{/board/update(id=${board.id})}" method="get">
>     <input type="hidden" name="id" th:value="${board.id}" />
>     <button>수정</button>
> </form>
> ```

## thymeleaf: innerHTML
thymeleaf에서 innerHTML을 담당하는 속성은 th:utext="${}" 이다.

## form tag: 꿀팁!
from tag 에서 action 값을 빈 문자열로 주고 method를 post로 주면, <i><b>해당 url의 get mapping이 아닌 post mapping으로 값을 전달</b></i>해준다!!! 완전 개꿀팁.

## input: form 형식 전달 시
input 태그에 값을 입력하고 submit 하게 되면 input 값이 전달된다. 그러나 프로젝트 특성 상 input value를 수정할 수 없도록 하였다.<br>
(선택된 파일만을 보여주도록 하기 위해!)

```html
<form action="" method="post">
    <input disabled th:value="${prevBoard.filename}" class="selectedFile" name="currentFilename" />
</form>
```
여기서 input value를 수정할 수 없도록 하기 위해 disabled 속성을 추가해 주었는데, controller 단에서 값을 받지 못한다.

> 그 이유는 <i>disabled 속성은 form으로도 전송되지 않도록</i>하는 속성이기 때문이다!

따라서 이런 경우, 즉 input value를 수정할 수 없도록 하지만, form 형식으로 전송하려고 한다면

```html
<form action="" method="post">
    <input readonly th:value="${prevBoard.filename}" class="selectedFile" name="currentFilename" />
</form>
```
disabled 속성 말고 readonly 속성을 이용해야 한다!!

## JPA: 데이터 수정(UPDATE, DELETE)
JPA를 이용하여 UPDATE, DELETE를 수행할 때 주의햐아 할 사항이다.
1. @Query 어노테이션을 이용할 경우, @Modifying 어노테이션을 <i><b>필수적으로</b></i> 추가해야 한다.
2. 해당 기능을 호출하는 함수에 @Transactional 어노테이션을 <i><b>필수적으로</b></i> 추가해야 한다.

예시: 클릭 시 조회수를 올려주는 기능
```java
@Modifying  // @Query annotation 을 이용하여 SELECT를 제외한 기능(UPDATE, DELETE 등)을 수행할 때 필수적으로 사용해야 함 !!
@Query("update Board b set b.count = b.count + 1 where b.id = :id")
int updateViewCount(@Param("id") long id);
```

```java
@Transactional
public int updateViewCount(long id) {
    return this.boardRepository.updateViewCount(id);
}
```

## spring boot - excel 파일 읽기
의존성 추가
```xml
<dependencies>
    <dependency>
        <groupId>org.apache.poi</groupId>
        <artifactId>poi</artifactId>
        <version>3.11</version>
    </dependency>
    <dependency>
        <groupId>org.apache.poi</groupId>
        <artifactId>poi-ooxml</artifactId> <!-- 엑셀 2007 이상 버전에서 사용 -->
        <version>3.11</version>
    </dependency>
</dependencies>
```
+) 3.11말고 4.1.2 버전을 사용하길 권장!!

## jquery: .text()
jquery를 이용하여 파일 선택 시 파일이 선택되었음을 알려주는 기능을 추가했다.<br>
type=file 인 input tag 를 사용해야 해서, css 변경을 위해 label의 for 속성을 이용하여 연결했다.
```html
<label id="excel__select" class="excel__select" for="file">
    Excel 파일 선택
    <input type="file" name="file" id="file" style="display: none;" />
</label>
```
jquery는 다음과 같이 작성했다.

```html
<script>
    $(document).ready(function() {
        $("#file").change(function (e) {
            const fileSelectInput = $("#excel__select");
            const submitButton = $("#excelUploadButton");
            let isSelected;
            isSelected = e.target.value !== "";
            if (isSelected) {
                fileSelectInput.css("background-color", "deepskyblue");
                fileSelectInput.text("파일이 선택됨");
                submitButton.removeAttr("disabled");
            } else {
                fileSelectInput.css("background-color", "#FF6600");
                fileSelectInput.text("Excel 파일 선택");
                submitButton.attr("disabled", "disabled");
            }
        });
    });
</script>
```
jquery 코드를 보면, 파일이 선택되었을 때 label의 text를 변경하도록 했다.<br>
그런데 나는 label tag를 이용하여 input tag를 감싸는 구조를 채택했고, 그로 인해 input tag가 없어져 버렸다....

for 속성을 사용하려면 label 태그 안쪽에 해당 태그가 있어야 하는 줄 알았는데, 굳이 그럴 필요는 없었다.
```html
<label id="excel__select" class="excel__select" for="file">
    Excel 파일 선택
</label>
<input type="file" name="file" id="file" style="display: none;" />
```
그래서 코드를 위처럼 변경하니까 잘 작동했다.

> summary: $("#tag_id").text() 함수는, 해당 태그의 <i>안쪽의 요소를 변경한다!!!</i>

#### 추가
MultipartFile을 이용하여 파일을 받을 때 오류가 생긴다면, form 태그에 enctype을 지정해 주었는지 확인해보자!!
```html
<form action="/board/readExcel" method="post" enctype="multipart/form-data"></form>
```

## WARNING Report: An illegal reflective access operation has occurred
spring boot 에서 Excel 파일을 읽기 위해 apache poi를 사용하니 아래와 같은 경고가 나왔다.
```
WARNING: An illegal reflective access operation has occurred
WARNING: Illegal reflective access by org.apache.poi.util.DocumentHelper (file:/Users/dev/.m2/repository/org/apache/poi/poi-ooxml/3.11/poi-ooxml-3.11.jar) to method com.sun.org.apache.xerces.internal.util.SecurityManager.setEntityExpansionLimit(int)
WARNING: Please consider reporting this to the maintainers of org.apache.poi.util.DocumentHelper
WARNING: Use --illegal-access=warn to enable warnings of further illegal reflective access operations
WARNING: All illegal access operations will be denied in a future release
```
말 그대로 경고라 서버가 정상적으로 작동하긴 했지만, 뭔가 찜찜했다.<br>
poi 의존성을 추가하여 사용하기 전에는 경고가 없었으나, 사용한 뒤에 해당 경고를 보았으니 문제는 해당 모듈에 있다고 생각하여
모듈 버전을 바꿔보니 경고가 말끔히 사라졌다.

> 3.11 -> 4.1.2로 변경

# OAuth2 Login
의존성 추가
```xml
<dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>
```
의존성 추가 후에 application-oauth.properties 파일을 작성한다.
> git commit 시 해당 파일이 push 되지 않도록 .gitignore 에 필수로 추가해라!!

```properties
spring.security.oauth2.client.registration.google.client-id=[클라이언트 아이디]
spring.security.oauth2.client.registration.google.client-secret=[클라이언트 비밀번호]
spring.security.oauth2.client.registration.google.scope=profile,email
```
scope 기본값은 email, profile, openid 이렇게 3가지 이다.<br>
이 중 openid는 scope에 추가하지 않느데, 그 이유는
> openid가 있으면 OpenId Provider로 인식하기 때문인데, 이렇게 되면 OpenId Provider인 서비스와 그렇지 않은 서비스(네이버/카카오 등)로 나뉘다.<br>
> 따라서 각각 OAuth2Service를 만들어야 하기 때문에 openid는 추가하지 않는다!!

application-oauth.properties 파일을 작성했으면 application.properties 파일에 해당 파일을 등록해 정보를 가져올 수 있도록 해준다.
```properties
# application.properties 파일에 입력
spring.profiles.include=oauth
```
> application-xxx.properties 파일을 만들면 xxx라는 profile이 생성되어 관리할 수 있다!<br>
> 해당 정보를 가져오기 위해 application.properties 파일에 <i>spring.profiles.include=xxx</i> 를 입력하여 관리하도록 한다.

## OAuth2 login: Google
