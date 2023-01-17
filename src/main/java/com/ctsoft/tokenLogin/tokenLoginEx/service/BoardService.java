package com.ctsoft.tokenLogin.tokenLoginEx.service;

import com.ctsoft.tokenLogin.tokenLoginEx.dto.BoardDto;
import com.ctsoft.tokenLogin.tokenLoginEx.entity.Board;
import com.ctsoft.tokenLogin.tokenLoginEx.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;

    public void testCreate() {
        for (int i=0; i < 50; i++) {
            Board board = new Board();
            board.setCount(i);
            board.setTitle("테스트 제목 " + i);
            board.setContent("테스트 본문 " + i);
            board.setWriter("user");
            board.setRegTime(LocalDateTime.now());

            boardRepository.save(board);
        }
    }

    public Board createBoard(BoardDto boardDto, String writer) {
        Board board = new Board();

        board.setTitle(boardDto.getTitle());
        board.setContent(boardDto.getContent());
        board.setWriter(writer);
        board.setRegTime(LocalDateTime.now());
        board.setCount(0);
        board.setFilename(null);
        board.setFilepath(null);
        return board;
    }

    public Board createBoard(BoardDto boardDto, String writer, String filename, String filepath) {
        Board board = new Board();

        board.setTitle(boardDto.getTitle());
        board.setContent(boardDto.getContent());
        board.setWriter(writer);
        board.setRegTime(LocalDateTime.now());
        board.setCount(0);
        board.setFilename(filename);
        board.setFilepath(filepath);
        return board;
    }

    public Board write(BoardDto boardDto, String writer, MultipartFile file) throws IOException {
        // 이미지 저장 테스트해보기 !! 그 전에 DB 구조 변경 필요.
        Board board;
        String projectPath = System.getProperty("user.dir") + "/src/main/resources/static/image";
        UUID uuid = UUID.randomUUID();

        if (Objects.equals(file.getOriginalFilename(), "")) {
            System.out.println("there is no upload file.");
            board = this.createBoard(boardDto, writer);
        } else {
            System.out.println("there is upload file.");
            String filename = uuid + "_" + file.getOriginalFilename();
            String filepath = "/image/" + filename;
            board = this.createBoard(boardDto, writer, filename, filepath);

            File saveFile = new File(projectPath, filename);
            file.transferTo(saveFile);
        }
        return boardRepository.save(board);
    }

    public Page<Board> selectAllBoard(Pageable pageable) {
//        this.testCreate();
        return boardRepository.selectAll(pageable);
    }

    public Board selectBoardById(long id) {
        return boardRepository.findById(id);
    }

    public Page<Board> searchBoard(String category, String keyword, Pageable pageable) {
        Page<Board> boardList;
        if (category.equals("title")) {
            boardList = boardRepository.selectBoardByTitle(keyword, pageable);
        } else if (category.equals("content")) {
            boardList = boardRepository.selectBoardByContent(keyword, pageable);
        } else {
            boardList = boardRepository.selectBoardByTitleOrContent(keyword, pageable);
        }
        return boardList;
    }
}
