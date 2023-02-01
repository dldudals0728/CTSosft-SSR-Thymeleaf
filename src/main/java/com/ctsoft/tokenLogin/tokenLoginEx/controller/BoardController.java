package com.ctsoft.tokenLogin.tokenLoginEx.controller;

import com.ctsoft.tokenLogin.tokenLoginEx.constant.Role;
import com.ctsoft.tokenLogin.tokenLoginEx.dto.BoardDto;
import com.ctsoft.tokenLogin.tokenLoginEx.dto.BoardSearchDto;
import com.ctsoft.tokenLogin.tokenLoginEx.entity.Board;
import com.ctsoft.tokenLogin.tokenLoginEx.entity.Img;
import com.ctsoft.tokenLogin.tokenLoginEx.service.BoardService;
import com.ctsoft.tokenLogin.tokenLoginEx.service.ImgService;
import com.ctsoft.tokenLogin.tokenLoginEx.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
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
import java.net.MalformedURLException;
import java.util.List;
import java.util.Objects;

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
        String userRole = userService.getUserRoleFromToken(request, "jdhToken", 7);
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
        model.addAttribute("role", userRole);
        return "board/boardList";
    }

    @GetMapping("/write")
    public String boardWrite(HttpServletRequest request) {
        String userId = userService.getUserIdFromToken(request, "jdhToken", 7);
        if (userId == null) {
            System.out.println("[UserController/user] userId is null!");
            return "redirect:/login";
        } else if (userId.equals("")) {
            System.out.println("[UserController/user] userId is '\"\"'!");
            return "redirect:/expire";
        }
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

        Board board = boardService.write(boardDto, userId);

        if (Objects.equals(file.getOriginalFilename(), "")) {
            System.out.println("there is no upload file.");
        } else {
            System.out.println("there is upload file.");
            Img img = imgService.saveImg(file, board.getId());
            System.out.println(img.toString());
        }
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
        Img img = imgService.findBoardImg(id);
        if (img != null) {
            model.addAttribute("img", img);
        }
        this.boardService.updateViewCount(id);
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
        imgService.deleteImg(id);
        return "redirect:/board/";
    }

    @GetMapping("/update")
    public String update(HttpServletRequest request, @RequestParam(value = "id", required = true) long id, Model model) {
        Board board = boardService.selectBoardById(id);
        Img img = imgService.findBoardImg(id);
        model.addAttribute("prevBoard", board);
        model.addAttribute("img", img);
        model.addAttribute("currentId", id);


        if (img != null) {
            String projectPath = System.getProperty("user.dir") + "/src/main/resources/static";
            String filePath = projectPath + img.getFilepath();
            File file = new File(filePath);

            System.out.println("파일 테스트");
            File file1 = new File(filePath);
            File file2 = new File(img.getFilepath());

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

        Board updateBoard = boardService.update(boardDto, id, userId, viewCount);
        Img updateImg = imgService.update(file, id, currentFilename);
        System.out.println("viewCount : " + viewCount);
        System.out.println("board id : " + id);
        System.out.println("currentFilename : " + currentFilename);
        return "redirect:/board/";
    }
    @PostMapping("/download")
    public ResponseEntity<UrlResource> downloadImage(@RequestParam(value = "id") long id) throws MalformedURLException {
        return imgService.download(id);
    }

    @PostMapping("/readExcel")
    public String readExcel(@RequestParam(value = "file") MultipartFile file) throws IOException {
        System.out.println("readExcel post start");
        if (file == null) {
            System.out.println("file is null!!");
            return "redirect:/board/";
        }
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        assert extension != null;
        if (!extension.equals("xlsx") && !extension.equals("xls")) {
            System.out.println("엑셀 파일만 업로드 가능합니다.");
            return "redirect:/board/";
        }

        Workbook workbook;
        if (extension.equals("xlsx")) {
            workbook = new XSSFWorkbook(file.getInputStream());
        } else {
            workbook = new HSSFWorkbook(file.getInputStream());
        }

        Sheet worksheet = workbook.getSheetAt(0);

        for (int i = 0; i < worksheet.getPhysicalNumberOfRows(); i++) {
            Row row = worksheet.getRow(i);

            Board board = new Board();

            board.setTitle(row.getCell(0).getStringCellValue());
            board.setContent(row.getCell(1).getStringCellValue());
            board.setWriter(row.getCell(2).getStringCellValue());
            board.setCount((long) row.getCell(3).getNumericCellValue());
            board.setRegTime(boardService.dateToLocalDateTime(row.getCell(4).getDateCellValue()));

            boardService.write(board);
            System.out.println("[Excel Input]: " + (i + 1) + "번째 행 게시글 [" + row.getCell(0).getStringCellValue() + "] 가 저장되었습니다.");
        }

        return "redirect:/board/";
    }
}
