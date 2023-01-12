package com.ctsoft.tokenLogin.tokenLoginEx.service;

import com.ctsoft.tokenLogin.tokenLoginEx.dto.BoardDto;
import com.ctsoft.tokenLogin.tokenLoginEx.entity.Board;
import com.ctsoft.tokenLogin.tokenLoginEx.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;

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

    public List<Board> selectAllBoard() {
        return boardRepository.selectAll();
    }

    public Board selectBoardById(long id) {
        return boardRepository.findById(id);
    }

    public List<Board> searchBoard(String category, String keyword) {
        List<Board> boardList;
        if (category.equals("title")) {
            boardList = boardRepository.selectBoardByTitle(keyword);
        } else if (category.equals("content")) {
            boardList = boardRepository.selectBoardByContent(keyword);
        } else {
            boardList = boardRepository.selectBoardByTitleOrContent(keyword);
        }
        return boardList;
    }
}
