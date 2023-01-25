package com.ctsoft.tokenLogin.tokenLoginEx.controller;

import com.ctsoft.tokenLogin.tokenLoginEx.constant.Role;
import com.ctsoft.tokenLogin.tokenLoginEx.dto.BoardDto;
import com.ctsoft.tokenLogin.tokenLoginEx.dto.BoardSearchDto;
import com.ctsoft.tokenLogin.tokenLoginEx.entity.Board;
import com.ctsoft.tokenLogin.tokenLoginEx.service.BoardService;
import com.ctsoft.tokenLogin.tokenLoginEx.service.ImgService;
import com.ctsoft.tokenLogin.tokenLoginEx.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/board")
@RequiredArgsConstructor
public class BoardController {

    private final UserService userService;
    private final BoardService boardService;
    private final ImgService imgService;

    @GetMapping("/")
    public String mainBoard(HttpServletRequest request, Model model,
                            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        String userId = userService.getUserIdFromToken(request, "jdhToken", 7);
        if (userId == null) {
            System.out.println("[UserController/user] userId is null!");
            return "redirect:/login";
        } else if (userId.equals("")) {
            System.out.println("[UserController/user] userId is '\"\"'!");
            return "redirect:/expire";
        }
        Page<Board> boardList = boardService.selectAllBoard(pageable);
        System.out.println("Board Page main.");
        System.out.println(boardList);
        System.out.println(boardList.getTotalPages());
        System.out.println(boardList.getNumber());
        System.out.println(boardList.getPageable().getPageNumber());
        model.addAttribute("boardList", boardList);
        model.addAttribute("boardSearchDto", new BoardSearchDto());
        return "board/boardList";
    }

    @GetMapping("/write")
    public String boardWrite() {
        return "board/boardWrite";
    }

    @PostMapping("/write")
    public String write(HttpServletRequest request, BoardDto boardDto, MultipartFile file) throws IOException {
        System.out.println("write post");
        String userId = userService.getUserIdFromToken(request, "jdhToken", 7);
        if (userId == null) {
            System.out.println("[UserController/user] userId is null!");
            return "redirect:/login";
        } else if (userId.equals("")) {
            System.out.println("[UserController/user] userId is '\"\"'!");
            return "redirect:/expire";
        }

        Board board = boardService.write(boardDto, userId, file);
        System.out.println(board.toString());

        return "redirect:/board/";
    }

    @GetMapping("/content")
    public String content(HttpServletRequest request, @RequestParam(value = "id", required = true) long id, Model model) {
        String userId = userService.getUserIdFromToken(request, "jdhToken", 7);
        if (userId == null) {
            System.out.println("[UserController/user] userId is null!");
            return "redirect:/login";
        } else if (userId.equals("")) {
            System.out.println("[UserController/user] userId is '\"\"'!");
            return "redirect:/expire";
        }
        Role userRole = Role.valueOf(userService.getUserRoleFromToken(request, "jdhToken", 7));
        Board board = boardService.selectBoardById(id);
        model.addAttribute("board", board);
        model.addAttribute("role", userRole);
        model.addAttribute("userId", userId);
        return "/board/boardContent";
    }

    @GetMapping("/search")
    public String search(BoardSearchDto boardSearchDto, Model model,
                         @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        System.out.println(boardSearchDto.toString());
        Page<Board> boardList = boardService.searchBoard(boardSearchDto.getCategory(), boardSearchDto.getKeyword(), pageable);

        model.addAttribute("boardList", boardList);
        model.addAttribute("boardSearchDto", boardSearchDto);
        return "/board/boardList";
    }

    @PostMapping("/delete")
    public String delete(@RequestParam(value = "id", required = true) long id) {
        // role: USER ==> 자신의 게시글만 삭제할 수 있음 | role: ADMIN ==> 전체 게시글 삭제 가능으로 변경 필요.
        boardService.deleteBoard(id);
        return "redirect:/board/";
    }

    @GetMapping("/update")
    public String update(HttpServletRequest request, @RequestParam(value = "id", required = true) long id, Model model) {
        Board board = boardService.selectBoardById(id);
        model.addAttribute("prevBoard", board);
        model.addAttribute("currentId", id);


        if (board.getFilepath() != null) {
            String projectPath = System.getProperty("user.dir") + "/src/main/resources/static";
            String filePath = projectPath + board.getFilepath();
            File file = new File(filePath);

            System.out.println("파일 테스트");
            File file1 = new File(filePath);
            File file2 = new File(board.getFilepath());

            if (file1.isFile()) {
                System.out.println("첫번째 파일이 존재합니다.");
            }

            if (file2.isFile()) {
                System.out.println("두번째 파일이 존재합니다.");
            }
            if (file.isFile()) {
                System.out.println("update: 파일이 존재합니다!");
                model.addAttribute("prevFile", file);
            }
        }
        return "/board/boardWrite";
    }

    @PostMapping("/update")
    public String update(HttpServletRequest request, BoardDto boardDto,
                         @RequestParam(value = "id", required = true) long id,
                         @RequestParam(value = "viewCount", required = true) long viewCount,
                         @RequestParam(value = "currentFilename", required = true) String currentFilename,
                         MultipartFile file) throws IOException {
        System.out.println("update post");
        System.out.println(boardDto.toString());
        System.out.println(id);

        String userId = userService.getUserIdFromToken(request, "jdhToken", 7);
        if (userId == null) {
            return "redirect:/login";
        } else if (userId.equals("")) {
            return "redirect:/expire";
        }

        Board updateBoard = boardService.update(boardDto, id, userId, viewCount, currentFilename, file);
        System.out.println("viewCount : " + viewCount);
        System.out.println("board id : " + id);
        System.out.println("currentFilename : " + currentFilename);
        return "redirect:/board/";
    }
}
