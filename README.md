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