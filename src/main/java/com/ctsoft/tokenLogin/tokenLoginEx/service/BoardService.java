package com.ctsoft.tokenLogin.tokenLoginEx.service;

import com.ctsoft.tokenLogin.tokenLoginEx.dto.BoardDto;
import com.ctsoft.tokenLogin.tokenLoginEx.entity.Board;
import com.ctsoft.tokenLogin.tokenLoginEx.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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
        return board;
    }

    public Board write(BoardDto boardDto, String writer) {
        Board board = this.createBoard(boardDto, writer);
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
