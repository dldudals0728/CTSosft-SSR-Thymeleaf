package com.ctsoft.tokenLogin.tokenLoginEx.controller;

import com.ctsoft.tokenLogin.tokenLoginEx.dto.BoardDto;
import com.ctsoft.tokenLogin.tokenLoginEx.dto.BoardSearchDto;
import com.ctsoft.tokenLogin.tokenLoginEx.entity.Board;
import com.ctsoft.tokenLogin.tokenLoginEx.service.BoardService;
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
import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/board")
@RequiredArgsConstructor
public class BoardController {

    private final UserService userService;
    private final BoardService boardService;
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
        System.out.println(file.getOriginalFilename());
        System.out.println(System.getProperty("user.dir"));
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
    public String content(@RequestParam(value = "id", required = true) long id, Model model) {
        System.out.println("id: " + id);
        Board board = boardService.selectBoardById(id);
        model.addAttribute("board", board);
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
}
