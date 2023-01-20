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
        for (int i = 0; i < 50; i++) {
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

    public Board update(BoardDto boardDto, long id, String writer, long viewCount, String currentFilename, MultipartFile file) throws IOException {
        Board prevBoard = boardRepository.findById(id);
        if (prevBoard == null) {
            return null;
        }

        prevBoard.setTitle(boardDto.getTitle());
        prevBoard.setContent(boardDto.getContent());
        prevBoard.setWriter(writer);
        prevBoard.setCount(viewCount);
        prevBoard.setRegTime(LocalDateTime.now());

        /*
         * 비교 필요 항목
         * 1. 이미지 X -> 이미지 X: prevBoard.filename = null / prevFilename = ""
         * 2. 이미지 X -> 이미지 O
         * 3. 이미지 O -> 이미지 X
         * 4. 이미지 O -> 이미지 O
         * *** 3번과 4번의 경우에는 이미지를 삭제 했는지, 그대로 두는지 판단할 필요가 있음
         *
         * 이전 이미지가 없다: prevBoard.filename == null
         * 이전 이미지가 있다: prevBoard.filename != null ==> 이전 이미지가 있는 경우, prevBoard.filename이 이전 이미지 이름이 되겠네?
         * */

        // 기존 게시글에 이미지가 존재하지 않는 경우
        if (prevBoard.getFilename() == null) {
            // update 시 이미지를 추가한 경우
            if (!Objects.equals(file.getOriginalFilename(), "")) {
                String projectPath = System.getProperty("user.dir") + "/src/main/resources/static/image";
                UUID uuid = UUID.randomUUID();

                String filename = uuid + "_" + Objects.requireNonNull(file.getOriginalFilename()).replaceAll(" ", "_");
                String filepath = "/image/" + filename;
                String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
                prevBoard.setFilename(filename);
                prevBoard.setFilepath(filepath);

                resizeImage(file, projectPath + "/" + filename, extension);
            }

            return boardRepository.save(prevBoard);
        } else {    // 기존 게시글에 이미지가 존재하는 경우
            /*
            * 1. 기존 이미지 삭제 ==> MultipartFile.getOriginalFilename() == "" && currentFilename == "" 일 경우
            * 2. 기존 이미지에서 새로운 이미지로 변경 ==> MultipartFile.getOriginalFilename() != "" 일 경우에는 기존 파일 삭제 후 새로 저장!
            * 3. 기존 이미지 유지 ==> MultipartFile.getOriginalFilename() == "" && currentFilename != "" 일 경우에는 아무것도 안함!
            * */
            // 새로운 이미지를 추가하지 않은 경우
            if (Objects.equals(file.getOriginalFilename(), "")) {
                if (Objects.equals(currentFilename, "")) {  // 기존에 있던 파일을 삭제한 경우
                    this.deleteFile(boardRepository.findById(id));
                    prevBoard.setFilepath(null);
                    prevBoard.setFilename(null);

                }
            } else {    // 새로운 이미지를 추가한 경우
                this.deleteFile(boardRepository.findById(id));

                String projectPath = System.getProperty("user.dir") + "/src/main/resources/static/image";
                UUID uuid = UUID.randomUUID();

                String filename = uuid + "_" + Objects.requireNonNull(file.getOriginalFilename()).replaceAll(" ", "_");
                String filepath = "/image/" + filename;
                String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
                prevBoard.setFilename(filename);
                prevBoard.setFilepath(filepath);

                resizeImage(file, projectPath + "/" + filename, extension);

            }
            return boardRepository.save(prevBoard);
        }
    }

    public void resizeImage(MultipartFile file, String filepath, String formatName) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
        int originWidth = bufferedImage.getWidth();
        int originHeight = bufferedImage.getHeight();

        int newWidth = 500;
        if (originWidth > newWidth) {
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
        deleteFile(board);
        boardRepository.deleteById(id);
    }

    public void deleteFile(Board board) {
        if (board.getFilepath() != null) {
            System.out.println("파일이 존재합니다!");
            String projectPath = System.getProperty("user.dir") + "/src/main/resources/static";
            File file = new File(projectPath + board.getFilepath());

            // .isFile(): 파일이 존재하지 않거나 디렉토리일 경우 false, 파일일 경우 true를 반환 !
            if (file.isFile()) {
                System.out.println("파일 경로에 의한 파일이 존재합니다!");
                if (file.delete()) {
                    System.out.println("파일 삭제에 성공했습니다.");
                } else {
                    System.out.println("파일 삭제에 실패했습니다.");
                }
            } else {
                System.out.println("파일 경로에 의한 파일이 존재하지 않습니다!");
            }
        } else {
            System.out.println("파일이 존재하지 않습니다!");
        }
    }
}
