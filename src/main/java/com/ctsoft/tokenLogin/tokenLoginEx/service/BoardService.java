package com.ctsoft.tokenLogin.tokenLoginEx.service;

import com.ctsoft.tokenLogin.tokenLoginEx.dto.BoardDto;
import com.ctsoft.tokenLogin.tokenLoginEx.entity.Board;
import com.ctsoft.tokenLogin.tokenLoginEx.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
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
            String filename = uuid + "_" + Objects.requireNonNull(file.getOriginalFilename()).replaceAll(" ", "_");
            String filepath = "/image/" + filename;
            String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
            board = this.createBoard(boardDto, writer, filename, filepath);

//            File saveFile = new File(projectPath, filename);
//            file.transferTo(saveFile);
            resizeImage(file, projectPath + "/" + filename, extension);
        }
        return boardRepository.save(board);
    }

    public void resizeImage(MultipartFile file, String filepath, String formatName) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
        int originWidth = bufferedImage.getWidth();
        int originHeight = bufferedImage.getHeight();

        int newWidth = 500;
        if(originWidth > newWidth) {
            int newHeight = (originHeight * newWidth) / originWidth;
            // 이미지 품질 설정
            // Image.SCALE_DEFAULT : 기본 이미지 스케일링 알고리즘 사용
            // Image.SCALE_FAST : 이미지 부드러움보다 속도 우선
            // Image.SCALE_REPLICATE : ReplicateScaleFilter 클래스로 구체화 된 이미지 크기 조절 알고리즘
            // Image.SCALE_SMOOTH : 속도보다 이미지 부드러움을 우선
            // Image.SCALE_AREA_AVERAGING : 평균 알고리즘 사용
            Image resizeImage = bufferedImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
            BufferedImage newImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            Graphics graphics = newImage.getGraphics();
            graphics.drawImage(resizeImage, 0, 0, null);
            graphics.dispose();

            // image 저장
            File newFile = new File(filepath);
            ImageIO.write(newImage, formatName, newFile);
        } else {
            file.transferTo(new File(filepath));
        }
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

    public void deleteBoard(long id) {
        Board board = boardRepository.findById(id);
        if (board == null) {
            return;
        }
        boardRepository.deleteById(id);
    }
}
