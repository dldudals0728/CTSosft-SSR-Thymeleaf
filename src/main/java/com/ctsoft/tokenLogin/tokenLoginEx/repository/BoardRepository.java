package com.ctsoft.tokenLogin.tokenLoginEx.repository;

import com.ctsoft.tokenLogin.tokenLoginEx.entity.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Long> {
//    @Query(value = "SELECT b FROM Board AS b")
//    List<Board> selectAll();
    Board findById(long id);
//    @Query("select b from Board b where b.title like %:title% order by b.id")
//    List<Board> selectBoardByTitle(String title);
//    @Query("select b from Board b where b.content like %:content% order by b.id")
//    List<Board> selectBoardByContent(String content);
//    @Query("select b from Board b where b.title like %:keyword% or b.content like %:keyword% order by b.id")
//    List<Board> selectBoardByTitleOrContent(String keyword);

    // Pageable을 이용하여 페이징
    @Query(value = "SELECT b FROM Board AS b")
    Page<Board> selectAll(Pageable pageable);
    @Query("select b from Board b where b.title like %:title% order by b.id")
    Page<Board> selectBoardByTitle(String title, Pageable pageable);
    @Query("select b from Board b where b.content like %:content% order by b.id")
    Page<Board> selectBoardByContent(String content, Pageable pageable);
    @Query("select b from Board b where b.title like %:keyword% or b.content like %:keyword% order by b.id")
    Page<Board> selectBoardByTitleOrContent(String keyword, Pageable pageable);
    @Modifying  // @Query annotation 을 이용하여 SELECT를 제외한 기능(UPDATE, DELETE 등)을 수행할 때 필수적으로 사용해야 함 !!
    @Query("update Board b set b.count = b.count + 1 where b.id = :id")
    int updateViewCount(@Param("id") long id);
}
