package com.ctsoft.tokenLogin.tokenLoginEx.controller;

import com.ctsoft.tokenLogin.tokenLoginEx.dto.BoardDto;
import com.ctsoft.tokenLogin.tokenLoginEx.dto.BoardSearchDto;
import com.ctsoft.tokenLogin.tokenLoginEx.entity.Board;
import com.ctsoft.tokenLogin.tokenLoginEx.service.BoardService;
import com.ctsoft.tokenLogin.tokenLoginEx.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping("/board")
@RequiredArgsConstructor
public class BoardController {

    private final UserService userService;
    private final BoardService boardService;
    @GetMapping("/")
    public String mainBoard(HttpServletRequest request, Model model) {
        String userId = userService.getUserIdFromToken(request, "jdhToken", 7);
        if (userId == null) {
            System.out.println("[UserController/user] userId is null!");
            return "redirect:/login";
        } else if (userId.equals("")) {
            System.out.println("[UserController/user] userId is '\"\"'!");
            return "redirect:/expire";
        }
        List<Board> boardList = boardService.selectAllBoard();
        System.out.println(boardList);
        model.addAttribute("boardList", boardList);
        model.addAttribute("boardSearchDto", new BoardSearchDto());
        return "board/boardList";
    }

    @GetMapping("/write")
    public String boardWrite() {
        return "board/boardWrite";
    }

    @PostMapping("/write")
    public String write(HttpServletRequest request, BoardDto boardDto) {
        String userId = userService.getUserIdFromToken(request, "jdhToken", 7);
        if (userId == null) {
            System.out.println("[UserController/user] userId is null!");
            return "redirect:/login";
        } else if (userId.equals("")) {
            System.out.println("[UserController/user] userId is '\"\"'!");
            return "redirect:/expire";
        }

        Board board = boardService.write(boardDto, userId);
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
    public String search(BoardSearchDto boardSearchDto, Model model) {
        System.out.println(boardSearchDto.toString());
        List<Board> boardList = boardService.searchBoard(boardSearchDto.getCategory(), boardSearchDto.getKeyword());

        model.addAttribute("boardList", boardList);
        model.addAttribute("boardSearchDto", new BoardSearchDto());
        return "/board/boardList";
    }
}
