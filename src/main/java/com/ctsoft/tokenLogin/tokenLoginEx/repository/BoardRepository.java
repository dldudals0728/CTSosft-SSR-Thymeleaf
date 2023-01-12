package com.ctsoft.tokenLogin.tokenLoginEx.repository;

import com.ctsoft.tokenLogin.tokenLoginEx.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Long> {
    @Query(value = "SELECT b FROM Board AS b")
    List<Board> selectAll();
    Board findById(long id);
    @Query("select b from Board b where b.title like %:title% order by b.id")
    List<Board> selectBoardByTitle(String title);
    @Query("select b from Board b where b.content like %:content% order by b.id")
    List<Board> selectBoardByContent(String content);
    @Query("select b from Board b where b.title like %:keyword% or b.content like %:keyword% order by b.id")
    List<Board> selectBoardByTitleOrContent(String keyword);
}
